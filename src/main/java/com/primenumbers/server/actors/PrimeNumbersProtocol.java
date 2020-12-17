package com.primenumbers.server.actors;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primenumbers.server.actors.ReadCommand.CommandResponse;
import com.primenumbers.server.grpc.ReadNumbersRequest;

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
            log.info("Server started: Screening-Core on port: {}");
            return Behaviors.receive(Object.class)
                .onSignal(Terminated.class, (c , m) -> Behaviors.stopped())
                .build();
        });
        
        ActorSystem.create(setup, "typed-actors");
    }
    
    /**
     * Read the numbers
     */
    public CompletionStage<CommandResponse> readItem(ReadNumbersRequest upperbound) {
        CompletionStage<CommandResponse> read = ask(upperbound);
//        read.thenApply(s -> {
//            handleResponse(s);
//            return s;
//        });
//        
        return read;
    }
////    
////    private void handleResponse(Response s) {
////        if (!s.getValidationError().getErrorsList().isEmpty()) {
////            // TODO: Handle the error later in response
////            s.getValidationError().getErrorsList().forEach(error -> {
////                log.error("Validation Error: " + error);
////            });
////        }
////    }

    public void printActorSystem() {
        log.debug("system: " + context.getSystem());
        log.debug("system dead letters: " + context.getSystem().deadLetters());
        log.debug("context Tree: " + context.getSystem().printTree());
    }

    private CompletionStage<CommandResponse> ask(ReadNumbersRequest request) {
//        throw new IllegalStateException("Not implemented yet");
        UUID randomUUID = UUID.randomUUID();
        ActorRef<ReadCommand> actor = context.spawn(PrimeNumbersBehavior.create(), randomUUID.toString());
        //        ActorRef<CommandResponse>  = context.spawn(ReadCommand
//           .create(randomUUID), number);
//        
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

        result.thenAccept(res -> {
            context.stop(actor);
            res.getStreamSource().runForeach(n -> {
                log.debug("[ {} ]", n.getPrimeNumber());
            }, Materializer.matFromSystem(context.getSystem()));
        });
        
        return result;
    }
}
