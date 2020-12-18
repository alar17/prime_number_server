package com.primenumbers.server;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.forgerock.cuppa.Cuppa.before;
import static org.forgerock.cuppa.Cuppa.describe;
import static org.forgerock.cuppa.Cuppa.it;
import static org.forgerock.cuppa.Cuppa.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.forgerock.cuppa.junit.CuppaRunner;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.primenumbers.server.grpc.PrimeNumbersResponse;
import com.primenumbers.server.grpc.ReadNumbersRequest;
import com.primenumbers.server.series.PrimeNumbersProtocol;
import com.primenumbers.server.series.ReadCommand.CommandResponse;

import akka.stream.javadsl.Source;

@RunWith(CuppaRunner.class)
public class PrimeNumbersServiceImplSpec {
    private PrimeNumbersProtocol protocol;
    private PrimeNumbersServiceImpl service;

    {
        describe("PrimeNumbersServiceImplSpec", () -> {

            before("setup", () -> {
                protocol = Mockito.mock(PrimeNumbersProtocol.class);
                service = new PrimeNumbersServiceImpl(protocol);
            });

            when("we send a request for a prime number series", () -> {
                it("should call the protocol and pass the request", () -> {                    
                    CommandResponse response = CommandResponse.of(Source.single(PrimeNumbersResponse.newBuilder().setPrimeNumber(2).build()));
                    ReadNumbersRequest request = ReadNumbersRequest.newBuilder().setUpperBound(2).build();
                    Mockito.when(protocol.readItem(request)).thenReturn(completedFuture(response));

                    service.sendPrimeNumbersStream(request);

                    verify(protocol, times(1)).readItem(request);
                });
            });
        });
    }}
