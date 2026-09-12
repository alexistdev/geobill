package com.alexistdev.geolicense.starter.service;

/** Outcome of a verification call, so the caller can tell "rejected" apart from "server down". */
public enum VerificationResult {

    /** The server confirmed the license and the existing activation. */
    VALID,

    /** The server answered and rejected the token or the activation. */
    INVALID,

    /** The server could not be reached; the previous state is unchanged. */
    UNREACHABLE
}
