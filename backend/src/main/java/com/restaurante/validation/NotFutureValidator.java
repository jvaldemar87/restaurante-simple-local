package com.restaurante.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class NotFutureValidator implements ConstraintValidator<NotFuture, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            LocalDate date = LocalDate.parse(value, FORMATTER);
            return !date.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Formato de fecha inválido. Use yyyy-MM-dd"
            ).addConstraintViolation();
            return false;
        }
    }
}
