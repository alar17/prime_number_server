package com.primenumbers.server.actors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primenumbers.server.actors.ReadCommand.CommandResponse;
import com.primenumbers.server.grpc.PrimeNumbersResponse;
import com.primenumbers.server.series.PrimeNumberGenerator;

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

    private Behavior<ReadCommand> handle(ReadCommand read) {
        PrimeNumberGenerator s = new PrimeNumberGenerator(read.getRequest().getUpperBound());
        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach((n) -> {
            log.debug("print the number: {}", n);
            
        });
        RunnableGraph<Pair<NotUsed, CompletionStage<Done>>> runnable = s.primeSource().toMat(sink, Keep.both());
        
        //runnable.run(context.getSystem());
        Source<PrimeNumbersResponse, NotUsed> source = s.primeSource().map(e -> 
            PrimeNumbersResponse.newBuilder()
                .setPrimeNumber(e).build()
        );
        
        read.getReplyTo().tell(CommandResponse.of(source));
        
        return this;
    }

    /*
     * 
  public static class ChangeGreeting {
    public final String newGreeting;
    public ChangeGreeting(String newGreeting) {
      this.newGreeting = newGreeting;
    }
  }
  public static class GetGreeting {}
  public static GetGreeting GET_GREETING = new GetGreeting();

  public static class Greeting {
    public final String greeting;
    public Greeting(String greeting) {
      this.greeting = greeting;
    }
  }

  public static Props props(final String initialGreeting) {
    return Props.create(GreeterActor.class, () -> new GreeterActor(initialGreeting));
  }

  private Greeting greeting;

  public GreeterActor(String initialGreeting) {
    greeting = new Greeting(initialGreeting);
  }

  public AbstractActor.Receive createReceive() {
    return receiveBuilder()
        .match(GetGreeting.class, this::onGetGreeting)
        .match(ChangeGreeting.class, this::onChangeGreeting)
        .build();
  }

  private void onGetGreeting(GetGreeting get) {
    getSender().tell(greeting, getSelf());
  }

  private void onChangeGreeting(ChangeGreeting change) {
    greeting = new Greeting(change.newGreeting);
  }
     */
}
