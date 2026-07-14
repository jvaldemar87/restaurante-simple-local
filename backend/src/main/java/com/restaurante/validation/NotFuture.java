package com.restaurante.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotFutureValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotFuture {
    String message() default "La fecha no puede ser mayor a la fecha actual";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
