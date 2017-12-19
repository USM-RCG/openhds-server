package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.constraint.AppContextAware;
import com.github.cimsbioko.server.service.ValueConstraintService;
import com.github.cimsbioko.server.domain.constraint.ExtensionStringConstraint;

public class ExtensionStringConstraintImpl extends AppContextAware implements ConstraintValidator<ExtensionStringConstraint, String> {

    private String constraint;
    private boolean allowNull;
    private ValueConstraintService service;

    public void initialize(ExtensionStringConstraint arg0) {
        service = (ValueConstraintService) context.getBean("valueConstraintService");
        this.constraint = arg0.constraint();
        this.allowNull = arg0.allowNull();
    }

    public boolean isValid(String arg0, ConstraintValidatorContext arg1) {

        if (arg0 == null)
            return true;

        if (allowNull && arg0.equals(""))
            return true;

        return service.isValidConstraintValue(constraint, arg0);
    }
}

