package com.primenumbers.server.series;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primenumbers.server.protobuf.Primenumberscommand.PrimeCommand;
import com.primenumbers.server.protobuf.Primenumberscommand.PrimeCommand.Read;
import com.primenumbers.server.protobuf.Primenumberscommand.PrimeCommand.Response;
import com.primenumbers.server.protobuf.Primenumberscommand.PrimeCommand.Response.SuccessfullyDone;
import com.primenumbers.server.protobuf.Primenumberscommand.PrimeCommand.Response.ValidationError;

import akka.Done;
import akka.NotUsed;
import akka.actor.ClassicActorSystemProvider;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.japi.Pair;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;

/**
 * A singleton which is responsible for routing the commands to each actor and delivering response to the caller asynchronously
 */
public class PrimeNumbersProtocol {

    private static final Logger log = LoggerFactory.getLogger(PrimeNumbersProtocol.class);
    private ActorContext<Void> context;

    public static PrimeNumbersProtocol create(ActorContext<Void> context) {
        return new PrimeNumbersProtocol(context);
    }

    private PrimeNumbersProtocol(ActorContext<Void> context) {
        this.context = context;
    }
    
    /**
     * Read the numbers
     */
    public CompletionStage<Response> readItem(int upperbound) {
//        CompletionStage<Response> read = ask(upperbound, PrimeCommand.newBuilder()
//            .setRead(Read.getDefaultInstance())
//            .build()
//        );
//        read.thenApply(s -> {
//            handleResponse(s);
//            return s;
//        });
        PrimeNumberGenerator source = new PrimeNumberGenerator(upperbound);
        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach((n) -> {
            log.debug("print the number: {}", n);
            
        });
        RunnableGraph<Pair<NotUsed, CompletionStage<Done>>> runnable = source.primeSource().toMat(sink, Keep.both());
        
        runnable.run(context.getSystem());
        Response r = Response.newBuilder().setDone(SuccessfullyDone.getDefaultInstance())
            .build();
        return CompletableFuture.completedStage(r);
        //return read;
    }
    
    private void handleResponse(Response s) {
        if (!s.getValidationError().getErrorsList().isEmpty()) {
            // TODO: Handle the error later in response
            s.getValidationError().getErrorsList().forEach(error -> {
                log.error("Validation Error: " + error);
            });
        }
    }

    public void printActorSystem() {
        log.debug("system: " + context.getSystem());
        log.debug("system dead letters: " + context.getSystem().deadLetters());
        log.debug("context Tree: " + context.getSystem().printTree());
    }

    private CompletionStage<Response> ask(int number, PrimeCommand cmd) {
        throw new IllegalStateException("Method is not implemented yet");
//
//        ActorRef<Command> themePersistentActor = context.spawn(PrimeNumbersBehavior
//           .create(number), number);
//        
//        CompletionStage<Response> result =
//            AskPattern.ask(
//                themePersistentActor,
//                replyTo -> new Command(cmd, replyTo),
//                // asking someone requires a timeout and a scheduler, if the timeout hits without
//                // response the ask is failed with a TimeoutException
//                Duration.ofSeconds(3),
//                context.getSystem().scheduler());
//
//        result.thenAccept(res -> context.stop(themePersistentActor));
//        return result;
    }
}
