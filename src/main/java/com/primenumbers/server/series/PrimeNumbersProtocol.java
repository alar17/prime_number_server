package com.primenumbers.server.series;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primenumbers.server.grpc.ReadNumbersRequest;
import com.primenumbers.server.series.ReadCommand.CommandResponse;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.Materializer;

/**
 * A singleton which is responsible for routing the commands to each actor and delivering response to the caller asynchronously
 */
public class PrimeNumbersProtocol {

    private static final Logger log = LoggerFactory.getLogger(PrimeNumbersProtocol.class);
    private ActorContext<Object> context;

    public PrimeNumbersProtocol() {
        Behavior<Object> setup = Behaviors.setup(context -> {
            this.context = context;
            // Setup the protocols and directives
            log.info("Main Actor Ref:" + context.getSelf());
            return Behaviors.receive(Object.class)
                .onSignal(Terminated.class, (c , m) -> Behaviors.stopped())
                .build();
        });
        
        ActorSystem.create(setup, "typed-actors");
    }
    
    /**
     * For debugging purposes. 
     */
    public void printActorSystem() {
        log.debug("system: " + context.getSystem());
        log.debug("system dead letters: " + context.getSystem().deadLetters());
        log.debug("context Tree: " + context.getSystem().printTree());
    }

    /**
     * Spawn a PrimeNumbersBehavior actor and also a temp actor to send the ReadCommand to
     * the PrimeNumbers actor. Then it waits for the response and stop the actors when it receives the
     * response.
     */
    public CompletionStage<CommandResponse> readItem(ReadNumbersRequest request) {
        // A random UUID as the name of the actor.
        // We can generate a name based on requested max number, so we can reuse the actors instead of killing them
        // It just means that we need to use stateful actors.
        UUID randomUUID = UUID.randomUUID();
        
        ActorRef<ReadCommand> actor = context.spawn(PrimeNumbersBehavior.create(), randomUUID.toString());
        
        CompletionStage<CommandResponse> result =
            AskPattern.ask(
                actor,
                replyTo -> new ReadCommand(
                    request, 
                    replyTo),
                // asking someone requires a timeout and a scheduler, if the timeout hits without
                // response the ask is failed with a TimeoutException
                Duration.ofSeconds(3),
                context.getSystem().scheduler());
        
        return result;
    }
}
