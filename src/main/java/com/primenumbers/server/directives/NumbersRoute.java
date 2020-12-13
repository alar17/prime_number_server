package com.primenumbers.server.directives;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primenumbers.server.protobuf.Primenumberscommand.PrimeCommand.Response.Items;
import com.primenumbers.server.series.PrimeNumbersProtocol;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import io.vavr.collection.HashSet;

/**
 * Directive class which deals with Model1 routes
 */
public class NumbersRoute extends AllDirectives {
    private static final Logger log = LoggerFactory.getLogger(NumbersRoute.class);
    private final PrimeNumbersProtocol protocol;
    
    NumbersRoute(PrimeNumbersProtocol protocol) {
        this.protocol = protocol;
    }
    
    public Route numbersRoute() {
        return path(PathMatchers.integerSegment(), number -> 
            get(() -> {
                log.debug("Received get request for Number: {}", number);
                CompletionStage<Route> response = protocol.readItem(number).thenApply(r -> {
                    switch (r.getResponseCase()) {
                        case VALIDATIONERROR:
                            return complete(DirectiveUtils.withValidationError(HashSet.ofAll(r.getValidationError().getErrorsList())));
                        case ITEMS: return complete(withItems(r.getItems()));
                        default: throw new IllegalStateException("Invalid response:" + r);
                    }
                });
                try {
                    return response.toCompletableFuture().get();
                } catch (InterruptedException|ExecutionException e) {
                    log.warn(e.getMessage());
                    throw new IllegalStateException("Invalid response" + e);
                }
            })
        );
    }
    
    /**
     * Returns an HttpResponse including metrics as body
     */
    public static HttpResponse withItems(Items items) {
        return HttpResponse.create()
            .withStatus(StatusCodes.OK)
            .withEntity(items.toByteArray());
    }
    
}
