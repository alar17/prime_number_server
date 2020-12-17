package com.primenumbers.server.actors;

import com.primenumbers.server.grpc.PrimeNumbersResponse;
import com.primenumbers.server.grpc.ReadNumbersRequest;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;

/**
 * Command for including the replyTo reference of the subscriber actor 
 * as well as the protobuf command
 */
public class ReadCommand {
    private final ReadNumbersRequest request;
    private final ActorRef<CommandResponse> replyTo; // The actorRef to send the response to

    public ReadCommand(ReadNumbersRequest request, 
        ActorRef<CommandResponse> replyTo) {
        this.request = request;
        this.replyTo = replyTo;
    }

    public ActorRef<CommandResponse> getReplyTo() {
        return replyTo;
    }

    public ReadNumbersRequest getRequest() {
        return request;
    }

    public static class CommandResponse {
        private final Source<PrimeNumbersResponse, NotUsed> streamSource;

        public static CommandResponse of(Source<PrimeNumbersResponse, NotUsed> streamSource) {
            return new CommandResponse(streamSource);
        }
        
        private CommandResponse(Source<PrimeNumbersResponse, NotUsed> streamSource) {
            this.streamSource = streamSource;
        }

        public Source<PrimeNumbersResponse, NotUsed> getStreamSource() {
            return streamSource;
        }
    }
}
