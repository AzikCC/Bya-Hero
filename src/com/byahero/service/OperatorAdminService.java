package com.byahero.service;

import com.byahero.model.Operator;

import java.util.List;

/** Admin actions for operators only. */
public interface OperatorAdminService {

    List<Operator> getAllOperators();

    void addOperator(String name, String contactNumber);

    /** Changes the contact number of an existing operator. */
    void updateOperator(String name, String contactNumber);

    void deleteOperator(String name);
}
