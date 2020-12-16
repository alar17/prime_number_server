package com.primenumbers.server;

import com.primenumbers.server.grpc.*;

import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.scaladsl.Source;

public class PrimeNumberServerImpl implements PrimeNumbersService {
    private final Materializer mat;
    
    public PrimeNumberServerImpl(Materializer mat) {
        this.mat = mat;
    }

    @Override
    public Source<PrimeNumberResponse, NotUsed> sendPrimeNumbersStream(ReadNumbersRequest in) {
      System.out.println("Received request for prime number:**** " + in + " ****.");
      return Source.empty();
    }
}
