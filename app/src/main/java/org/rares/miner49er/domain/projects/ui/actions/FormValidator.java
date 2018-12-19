package org.rares.miner49er.domain.projects.ui.actions;

import org.rares.miner49er.ui.custom.FormValidationException;
import org.rares.miner49er.ui.custom.functions.AdvancedFunction;
import org.rares.miner49er.ui.custom.functions.NoErrorPredicate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FormValidator<T> {

    private final T t;

    private final Map<Object, String> errorList = new HashMap<>();

    private FormValidator(T t) {
        this.t = t;
    }

    public static <T> FormValidator of(T t) {
        return new FormValidator<>(Objects.requireNonNull(t));
    }

    public FormValidator<T> validate(
            NoErrorPredicate<T> validation,
            Object target, String message) {
        try {
            if (!validation.test(t)) {
                errorList.put(target, message);
            }
        } catch (Exception x) {

        }
        return this;
    }

    public <U> FormValidator<T> validate(
            AdvancedFunction<T, U> projection,
            NoErrorPredicate<U> validation,
            Object target, String message) {

        return validate(projection.andThen(validation::test)::apply, target, message);
    }

    public T get() {
        if (errorList.isEmpty()) {
            return t;
        } else {
            FormValidationException ex = new FormValidationException();
            ex.getInvalidFields().putAll(errorList);
            throw ex;
        }
    }
}
