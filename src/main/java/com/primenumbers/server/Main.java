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

import java.util.concurrent.CompletionStage;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        // important to enable HTTP/2 in ActorSystem's config
        Config conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
            .withFallback(ConfigFactory.defaultApplication());

        // Akka ActorSystem Boot
        ActorSystem sys = ActorSystem.create("PrimeNumberServer", conf);

        try {
            run(sys).thenAccept(binding -> {
                System.out.println("gRPC server bound to: " + binding.localAddress());
            });
        } catch (Exception e) {
            log.error("System Error: {}", e);
        }

        // ActorSystem threads will keep the app alive until `system.terminate()` is called
        log.info("prime-number-server started");
    }

    public static CompletionStage<ServerBinding> run(ActorSystem sys) throws Exception {
        Materializer mat = ActorMaterializer.create(sys);

        // Instantiate implementation
        PrimeNumbersService impl = new PrimeNumbersServiceImpl(mat);

        return Http.get(sys).bindAndHandleAsync(
            PrimeNumbersServiceHandlerFactory.create(impl, sys),
            ConnectHttp.toHost("127.0.0.1", 8090), 
            mat);
    }
}