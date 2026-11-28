package com.byahero.service.impl;

import com.byahero.model.Operator;
import com.byahero.repository.Repository;
import com.byahero.service.OperatorAdminService;

import java.util.List;

public class DefaultOperatorAdminService implements OperatorAdminService {

    private final Repository<Operator> operatorRepo;

    public DefaultOperatorAdminService(Repository<Operator> operatorRepo) {
        this.operatorRepo = operatorRepo;
    }

    @Override
    public List<Operator> getAllOperators() {
        return operatorRepo.findAll();
    }

    @Override
    public void addOperator(String name, String contactNumber) {
        operatorRepo.save(new Operator(name, contactNumber));
    }

    @Override
    public void updateOperator(String name, String contactNumber) {
        operatorRepo.update(new Operator(name, contactNumber));
    }

    @Override
    public void deleteOperator(String name) {
        if (!operatorRepo.delete(name)) {
            throw new IllegalArgumentException("No operator found with name " + name + ".");
        }
    }
}
