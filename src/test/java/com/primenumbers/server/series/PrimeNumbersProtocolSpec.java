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

import com.primenumbers.server.grpc.PrimeNumbersResponse;
import com.primenumbers.server.grpc.ReadNumbersRequest;
import com.primenumbers.server.series.ReadCommand.CommandResponse;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.testkit.javadsl.TestKit;
import io.vavr.collection.HashSet;

@RunWith(CuppaRunner.class)
public class PrimeNumbersProtocolSpec {    
    static ActorSystem system;
    static Materializer materializer;
    static PrimeNumbersProtocol protocol = new PrimeNumbersProtocol();
    {
        describe("PrimeNumbersProtocolSpec", () -> {

            before(() -> {
                system = ActorSystem.create("PrimeNumberGeneratorSpec");
                materializer = ActorMaterializer.create(system);
            });

            after("Shutdown", () -> {
                TestKit.shutdownActorSystem(system);
                system = null;
                protocol = null;
            });

            when("we call for a new primenumber series calculation", () -> {
                it("should spawn a new actor and call it", () -> {
                    ReadNumbersRequest req = ReadNumbersRequest.newBuilder().setUpperBound(10).build();
                    CompletionStage<CommandResponse> readItem = protocol.readItem(req);
                    Source<PrimeNumbersResponse, NotUsed> streamSource = readItem.toCompletableFuture().get().getStreamSource();
                    final CompletionStage<List<PrimeNumbersResponse>> future =
                        streamSource.runWith(Sink.seq(), materializer);
                    HashSet<Integer> result = HashSet.ofAll(future.toCompletableFuture().get()).map(item -> item.getPrimeNumber());
                    assertTrue(result.size() == 4);
                    assertTrue(result.containsAll(HashSet.of(2,3,5,7)));
                });
            });
        });
    }}
