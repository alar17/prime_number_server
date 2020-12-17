package com.primenumbers.server.actors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primenumbers.server.grpc.PrimeNumbersResponse;
import com.primenumbers.server.series.PrimeNumberGenerator;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class ReadCommandHandler {
    
    private static final Logger log = LoggerFactory.getLogger(PrimeNumbersBehavior.class);
    
    private CompletionStage<Source<PrimeNumbersResponse, NotUsed>> process(int upperbound) {
        PrimeNumberGenerator s = new PrimeNumberGenerator(upperbound);
        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach((n) -> {
            log.debug("print the number: {}", n);
            
        });
        RunnableGraph<Pair<NotUsed, CompletionStage<Done>>> runnable = s.primeSource().toMat(sink, Keep.both());
        
        //runnable.run(context.getSystem());
        Source<PrimeNumbersResponse, NotUsed> source = s.primeSource().map(e -> 
            PrimeNumbersResponse.newBuilder()
                .setPrimeNumber(e).build()
        );
        return CompletableFuture.completedStage(source);
    }
}
