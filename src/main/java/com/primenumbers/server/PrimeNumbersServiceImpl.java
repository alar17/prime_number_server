package com.primenumbers.server;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primenumbers.server.grpc.PrimeNumbersResponse;
import com.primenumbers.server.grpc.PrimeNumbersService;
import com.primenumbers.server.grpc.ReadNumbersRequest;
import com.primenumbers.server.series.PrimeNumbersProtocol;
import com.primenumbers.server.series.ReadCommand.CommandResponse;

import akka.NotUsed;
import akka.stream.javadsl.Source;

public class PrimeNumbersServiceImpl implements PrimeNumbersService {
    private static final Logger log = LoggerFactory.getLogger(PrimeNumbersServiceImpl.class);
    
    private PrimeNumbersProtocol protocol;

    public PrimeNumbersServiceImpl(PrimeNumbersProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public Source<PrimeNumbersResponse, NotUsed> sendPrimeNumbersStream(ReadNumbersRequest request) {
        log.debug("Received request for prime number:**** " + request + " ****.");

        CompletionStage<CommandResponse> readItem = protocol.readItem(request);
        try {
            return readItem.toCompletableFuture().get().getStreamSource();
        } catch (InterruptedException | ExecutionException e) {
            return Source.single(PrimeNumbersResponse.newBuilder()
                .setValidationError(e.getMessage()).build());
        }
    }
}
