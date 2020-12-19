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
import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.vavr.collection.HashSet;

@RunWith(CuppaRunner.class)
public class PrimeNumbersBehaviorSpec {    
    static Materializer materializer;
    static final ActorTestKit testKit = ActorTestKit.create();
    {
        describe("PrimeNumbersProtocolSpec", () -> {

            before(() -> {
                materializer = ActorMaterializer.create(testKit.system().classicSystem());
            });

            after("Shutdown", () -> {
                testKit.shutdownTestKit();
            });

            when("We send a number which is not valid", () -> {
                Behavior<ReadCommand> behavior = PrimeNumbersBehavior.create();
                TestProbe<CommandResponse> responseActor = testKit.createTestProbe();
                ReadNumbersRequest request = ReadNumbersRequest.newBuilder().setUpperBound(-239).build();
                ActorRef<ReadCommand> primeNumbersActor = testKit.spawn(behavior);

                it("should return a source containing validation error", () -> {
                    primeNumbersActor.tell(new ReadCommand(request, responseActor.ref()));
                    
                    CommandResponse receivedMessage = responseActor.receiveMessage();                    
                    Source<PrimeNumbersResponse, NotUsed> streamSource = receivedMessage.getStreamSource();
                    final CompletionStage<List<PrimeNumbersResponse>> future =
                        streamSource.runWith(Sink.seq(), materializer);
                    
                    List<PrimeNumbersResponse> result = future.toCompletableFuture().get();
                    assertTrue(result.size() == 1);
                    assertTrue(result.get(0).getValidationError().equals("The prime numbers are not defined for numbers lower than 2"));
                });
            });
            
            when("We send a valid integer to get the prime numbers", () -> {
                Behavior<ReadCommand> behavior = PrimeNumbersBehavior.create();
                TestProbe<CommandResponse> responseActor = testKit.createTestProbe();
                ReadNumbersRequest request = ReadNumbersRequest.newBuilder().setUpperBound(14).build();
                ActorRef<ReadCommand> primeNumbersActor = testKit.spawn(behavior);
                it("should return a source containing numbers", () -> {
                    primeNumbersActor.tell(new ReadCommand(request, responseActor.ref()));
                    
                    CommandResponse receivedMessage = responseActor.receiveMessage();                    
                    Source<PrimeNumbersResponse, NotUsed> streamSource = receivedMessage.getStreamSource();
                    final CompletionStage<List<PrimeNumbersResponse>> future =
                        streamSource.runWith(Sink.seq(), materializer);
                    HashSet<Integer> result = HashSet.ofAll(future.toCompletableFuture().get()).map(item -> item.getPrimeNumber());
                    assertTrue(result.size() == 6);
                    assertTrue(result.containsAll(HashSet.of(2,3,5,7,11,13).toJavaList()));
                });
            });
        });
    }
}

