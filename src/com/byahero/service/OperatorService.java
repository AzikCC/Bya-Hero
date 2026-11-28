package com.byahero.service;

import java.util.List;

/** Knows which bus companies are registered. Used by check-in and the bus admin form. */
public interface OperatorService {

    /** All operator names (used for dropdown lists). */
    List<String> getOperatorNames();

    /**
     * Returns the spelling saved in operators.csv (so "victory liner" becomes "Victory Liner"),
     * or null if the operator is not registered.
     */
    String findKnownOperator(String name);
}
