
package com.primenumbers.server.series;

import static org.forgerock.cuppa.Cuppa.after;
import static org.forgerock.cuppa.Cuppa.before;
import static org.forgerock.cuppa.Cuppa.describe;
import static org.forgerock.cuppa.Cuppa.it;
import static org.forgerock.cuppa.Cuppa.when;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.forgerock.cuppa.junit.CuppaRunner;
import org.junit.runner.RunWith;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.testkit.javadsl.TestKit;
import io.vavr.collection.HashSet;

@RunWith(CuppaRunner.class)
public class PrimeNumberGeneratorSpec {
    static ActorSystem system;
    static Materializer materializer;
    {
        describe("PrimeNumberGenerator", () -> {

            before(() -> {
                system = ActorSystem.create("PrimeNumberGeneratorSpec");
                materializer = ActorMaterializer.create(system);
            });
            
            after("Shutdown", () -> {
                TestKit.shutdownActorSystem(system);
                system = null;
            });

            when("we call it for prime numbers between 1 and 10", () -> {
                PrimeNumberGenerator png = new PrimeNumberGenerator(10);
                it("should return a source including 2,3,5,7", () -> {
                    final CompletionStage<List<Integer>> future =
                        png.primeSource().runWith(Sink.seq(), materializer);
                    List<Integer> result = future.toCompletableFuture().get();
                    assertTrue(result.size() == 4);
                    assertTrue(result.containsAll(HashSet.of(2,3,5,7).toJavaList()));
                });
            });

            when("we call it for prime numbers between 1 and 1000", () -> {
                PrimeNumberGenerator png = new PrimeNumberGenerator(1000);
                it("should return a source including 145 prime numbers", () -> {
                    final CompletionStage<List<Integer>> future =
                        png.primeSource().runWith(Sink.seq(), materializer);
                    List<Integer> result = future.toCompletableFuture().get();
                    assertTrue(result.size() == 168);
                    assertTrue(result.containsAll(HashSet.of(2,3,7,433, 563, 937).toJavaList()));
                });
            });
            
        });
    }}
