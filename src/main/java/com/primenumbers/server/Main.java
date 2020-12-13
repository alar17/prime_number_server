package com.primenumbers.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primenumbers.server.directives.Directives;
import com.primenumbers.server.series.PrimeNumbersProtocol;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;

public class Main {
    
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        try {
            // Create the main ActorSystem
            ActorSystem.create(Main.create(), "primenumbers-server");
        } catch (Exception x) {
            log.error("Server Stoped with an error: {}", x);
            System.exit(1);
        }
        log.info("prime-number-server started");
    }

    private static Behavior<Void> create() {
        return Behaviors.setup(context -> {
            // Setup the protocols and directives
            PrimeNumbersProtocol primeNumbersProtocol = PrimeNumbersProtocol.create(context);
            new Directives(primeNumbersProtocol, 8080);
            log.debug("Main Actor Ref:, {}", context.getSelf());
            return Behaviors.receive(Void.class)
                .onSignal(Terminated.class, sig -> Behaviors.stopped())
                .build();
        });
    }
}