package com.byahero.service;

/** Fare lookups. */
public interface FareService {

    /** The base fare of a destination, or -1 if the destination has no fare. */
    double getBaseFare(String destination);
}
