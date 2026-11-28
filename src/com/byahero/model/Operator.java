package com.byahero.model;

import com.byahero.util.Validator;

/** A bus company (one row of operators.csv). The admin page edits these. */
public class Operator {

    private String name;
    private String contactNumber; // optional, can be empty

    public Operator(String name, String contactNumber) {
        setName(name);
        setContactNumber(contactNumber);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (Validator.isBlank(name)) {
            throw new IllegalArgumentException("Operator name must not be blank.");
        }
        this.name = name.trim();
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = Validator.clean(contactNumber);
    }
}
