package com.byahero.service;

/** What the "Start loading / Mark departed" button needs. */
public interface StatusService {

    /** Moves a bus to its next status (ARRIVED -> LOADING -> DEPARTED only). */
    void updateStatus(String plate, String nextStatusText);
}
