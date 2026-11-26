package com.byahero.service.impl;

import com.byahero.model.Operator;
import com.byahero.repository.ReadRepository;
import com.byahero.service.OperatorService;
import com.byahero.util.Validator;

import java.util.ArrayList;
import java.util.List;

/** Needs only READ access to the operators. */
public class DefaultOperatorService implements OperatorService {

    private final ReadRepository<Operator> operatorRepo;

    public DefaultOperatorService(ReadRepository<Operator> operatorRepo) {
        this.operatorRepo = operatorRepo;
    }

    @Override
    public List<String> getOperatorNames() {
        List<String> names = new ArrayList<String>();
        List<Operator> operators = operatorRepo.findAll();
        for (int i = 0; i < operators.size(); i++) {
            names.add(operators.get(i).getName());
        }
        return names;
    }

    @Override
    public String findKnownOperator(String name) {
        if (Validator.isBlank(name)) {
            return null;
        }
        Operator operator = operatorRepo.findById(name);
        if (operator == null) {
            return null;
        }
        return operator.getName();
    }
}
