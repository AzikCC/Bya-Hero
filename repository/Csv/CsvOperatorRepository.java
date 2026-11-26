package com.byahero.repository.csv;

import com.byahero.model.Operator;

/** Saves and loads operators using operators.csv. The "id" of an operator is its name. */
public class CsvOperatorRepository extends CsvRepository<Operator> {

    public static final String HEADER = "name,contactNumber";

    public CsvOperatorRepository(String filePath) {
        super(filePath, HEADER, 2, "operator");
    }

    @Override
    protected Operator fromRow(String[] row) {
        return new Operator(row[0], row[1]);
    }

    @Override
    protected String toLine(Operator operator) {
        return CsvUtil.clean(operator.getName()) + "," + CsvUtil.clean(operator.getContactNumber());
    }

    @Override
    protected String idOf(Operator operator) {
        return operator.getName();
    }

    @Override
    protected String alreadyExistsMessage(Operator operator) {
        return "Operator " + operator.getName() + " already exists.";
    }

    @Override
    protected String notFoundMessage(Operator operator) {
        return "No operator found with name " + operator.getName() + ".";
    }
}
