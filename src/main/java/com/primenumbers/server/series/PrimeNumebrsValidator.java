package com.primenumbers.server.series;

/**
 * Validates the request
 */
public class PrimeNumebrsValidator {
    private final ReadCommand readCommand;
    
    public PrimeNumebrsValidator(ReadCommand readCommand) {
        this.readCommand = readCommand;
    }
}
