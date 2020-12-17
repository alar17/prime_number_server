package com.primenumbers.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import com.primenumbers.server.actors.PrimeNumbersProtocol;
import com.primenumbers.server.actors.ReadCommand.CommandResponse;
import com.primenumbers.server.grpc.*;

import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class PrimeNumbersServiceImpl implements PrimeNumbersService {
    private final Materializer materializer;
    private PrimeNumbersProtocol protocol;

    public PrimeNumbersServiceImpl(PrimeNumbersProtocol protocol, Materializer materializer) {
        this.protocol = protocol;
        this.materializer = materializer;
    }

    @Override
    public Source<PrimeNumbersResponse, NotUsed> sendPrimeNumbersStream(ReadNumbersRequest upperbound) {
        System.out.println("Received request for prime number:**** " + upperbound + " ****.");

        CompletionStage<CommandResponse> readItem = protocol.readItem(upperbound);
        try {
            return readItem.toCompletableFuture().get().getStreamSource();
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            return Source.single(PrimeNumbersResponse.newBuilder()
                .setValidationError(e.getMessage()).build());
        }
    }
}
