package com.primenumbers.server.series;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

/**
 * Class responsible for generating prime number series
 */
public class PrimeNumberGenerator {
    private Set<Integer> primeSet;
    private Source<Integer, NotUsed> initialSource;

    public PrimeNumberGenerator(int max) {
        primeSet = HashSet.empty();
        initialSource = Source.range(2, max);
    }

    /**
     * Creates a source from 2 to Max containing only prime numbers.
     * For the sake of performance, along the way, it keep the already found prime
     * numbers inside of a immutable set, so it can reuse them in order to find the next one.
     * If a number is not divisible by any prime number less than its square, then it's a prime number
     */
    public Source<Integer, NotUsed> primeSource() {
        return initialSource
            .filter(n -> {
                boolean isPrime = !primeSet
                    .filter(p -> p <= Math.floor(Math.sqrt(n))) // We need to check only up to square of the number
                    .exists(p -> isDivisibleBy(n , p));
                if (isPrime) {
                    primeSet = primeSet.add(n);
                }
                return isPrime;
            });
    }

    private boolean isDivisibleBy(int number, int prime) {
        return number % prime == 0;
    }
}
