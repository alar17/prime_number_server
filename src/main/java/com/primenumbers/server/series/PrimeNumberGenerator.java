package com.primenumbers.server.series;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class PrimeNumberGenerator {
    private final int upperBound;
    private Set<Integer> primeSet;
    private Source<Integer, NotUsed> initialSource;

    public PrimeNumberGenerator(int upperBound) {
        this.upperBound = upperBound;
        primeSet = HashSet.empty();
        initialSource = Source.range(2, upperBound);
    }

    private boolean isDivisibleBy(int number, int prime) {
        return number % prime == 0;
    }

    public Source<Integer, NotUsed> primeSource() {
    
        // 1) Start from 3 until the upperbound
        return initialSource
            .filter( n -> {
                boolean isPrime = !primeSet
                    .filter( p -> p <= Math.floor(Math.sqrt(upperBound)))
                    .exists(p -> isDivisibleBy(n , p));
                if (isPrime) {
                    primeSet = primeSet.add(n);
                    //System.out.println("Current Prime Set:" + primeSet);
                }
                return isPrime;
            });
        // 2) if the number is prime
    }
}
