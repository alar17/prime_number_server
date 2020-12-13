package com.primenumbers.server.series;



import com.primenumbers.server.protobuf.Primenumberscommand.PrimeCommand;

import akka.actor.typed.ActorRef;

/**
 * Command for including the replyTo reference of the subscriber actor 
 * as well as the protobuf command
 */
public class Command {
    final PrimeCommand cmd;
    final ActorRef<PrimeCommand.Response> replyTo; // The actorRef to send the response to

    public Command(PrimeCommand cmd, ActorRef<PrimeCommand.Response> replyTo) {
        this.cmd = cmd;
        this.replyTo = replyTo;
    }

    public PrimeCommand getCmd() {
        return cmd;
    }

    public ActorRef<PrimeCommand.Response> getReplyTo() {
        return replyTo;
    }
}
