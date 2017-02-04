package com.github.cimsbioko.server.domain.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import javax.validation.Constraint;
import javax.validation.Payload;

import com.github.cimsbioko.server.domain.constraint.impl.CheckDeathDateGreaterThanBirthDateImpl;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = CheckDeathDateGreaterThanBirthDateImpl.class)
@Documented
public @interface CheckDeathDateGreaterThanBirthDate {
    String message() default "The death date must be greater than the birth date of the individual.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
