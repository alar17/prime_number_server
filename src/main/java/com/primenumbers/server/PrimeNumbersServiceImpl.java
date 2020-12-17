package com.primenumbers.server;

import com.primenumbers.server.grpc.*;
import com.primenumbers.server.grpc.PrimeNumbersResponse.ValidationError;

import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class PrimeNumbersServiceImpl implements PrimeNumbersService {
    private final Materializer materializer;
    
    public PrimeNumbersServiceImpl(Materializer materializer) {
        this.materializer = materializer;
    }

    @Override
    public Source<PrimeNumbersResponse, NotUsed> sendPrimeNumbersStream(ReadNumbersRequest upperbound) {
      System.out.println("Received request for prime number:**** " + upperbound + " ****.");
      Set<PrimeNumbersResponse> responses = HashSet.of(PrimeNumbersResponse.newBuilder()
          .setValidationError(ValidationError.newBuilder()
              .addErrors("Not Implemented Yet"))
      .build());
      return Source.from(responses);
    }
}
