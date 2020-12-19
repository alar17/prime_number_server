package com.primenumbers.server.series;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primenumbers.server.grpc.PrimeNumbersResponse;
import com.primenumbers.server.series.ReadCommand.CommandResponse;

import akka.Done;
import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.japi.Pair;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

/**
 * A stateless actor, who is responsible for computing primenumber series
 * related only to one specific request.
 */
public class PrimeNumbersBehavior extends AbstractBehavior<ReadCommand> {
    private static final Logger log = LoggerFactory.getLogger(PrimeNumbersBehavior.class);
    private ActorContext<ReadCommand> context;

    public static Behavior<ReadCommand> create() {
        return Behaviors.setup(PrimeNumbersBehavior::new);
    }

    private PrimeNumbersBehavior(ActorContext<ReadCommand> context) {
        super();
        this.context = context;
    }

    @Override
    public Receive<ReadCommand> createReceive() {
        return newReceiveBuilder().onMessage(ReadCommand.class, 
            this::handle).build();
    }

    /**
     * Handles the ReadCommand which is related to actual computations
     */
    private Behavior<ReadCommand> handle(ReadCommand read) {
        Source<PrimeNumbersResponse, NotUsed> response;
        // First we need to validate the command:
        Set<String> errors = validate(read);
        if (!validate(read).isEmpty()) {
            // Send the validation error back to the actor which sent the request
            response = Source.single(PrimeNumbersResponse.newBuilder()
                .setValidationError(errors.head()).build());
        } else {
            PrimeNumberGenerator s = new PrimeNumberGenerator(read.getRequest().getUpperBound());
            Sink<Integer, CompletionStage<Done>> sink = Sink.foreach((n) -> {
                log.debug("print the number: {}", n);
            });
            
            // To see the numbers on the server for debugging purposes.
            RunnableGraph<Pair<NotUsed, CompletionStage<Done>>> runnable = s.primeSource().toMat(sink, Keep.both());

            response = s.primeSource().map(e -> 
                PrimeNumbersResponse.newBuilder().setPrimeNumber(e).build()
            );
        }
        
        // Send the calculated source back to the actor which sent the request
        read.getReplyTo().tell(CommandResponse.of(response));

        return this;
    }

    /**
     * Validates the command before handling it
     */
    private Set<String> validate(ReadCommand command) {
        Set<String> errors = HashSet.empty();
        // Check if the number is >= 2
        if (command.getRequest().getUpperBound() < 2) {
            errors = errors.add("The prime numbers are not defined for numbers lower than 2");
        }

        return errors;
    }
}
