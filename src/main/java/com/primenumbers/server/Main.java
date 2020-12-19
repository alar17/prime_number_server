package com.primenumbers.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorSystem;
import akka.http.javadsl.*;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.primenumbers.server.grpc.*;
import com.primenumbers.server.series.PrimeNumbersProtocol;

import java.util.concurrent.CompletionStage;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        Config config = ConfigFactory.load();
        
        // Akka ActorSystem Boot
        ActorSystem sys = ActorSystem.create("PrimeNumberServer", config);

        try {
            run(sys, config.getInt("server.port")).thenAccept(binding -> {
                log.info("gRPC server bound to: {}", binding.localAddress());
            });
        } catch (Exception e) {
            log.error("System Error: {}", e);
        }

        // ActorSystem threads will keep the app alive until `system.terminate()` is called
        log.info("prime-number-server started");
    }

    /**
     * Create singletons, bind the port and run the actor system.
     * @param config 
     */
    public static CompletionStage<ServerBinding> run(ActorSystem sys, int port) throws Exception {
        Materializer materializer = ActorMaterializer.create(sys);
        PrimeNumbersProtocol protocol = new PrimeNumbersProtocol();        
        PrimeNumbersService primeNumberService = new PrimeNumbersServiceImpl(protocol);

        return Http.get(sys).bindAndHandleAsync(
            PrimeNumbersServiceHandlerFactory.create(primeNumberService, sys),
            ConnectHttp.toHost("127.0.0.1", port), 
            materializer);
    }
}