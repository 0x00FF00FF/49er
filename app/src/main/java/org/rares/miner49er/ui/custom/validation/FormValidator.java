package org.rares.miner49er.ui.custom.validation;

import org.rares.miner49er.ui.custom.functions.AdvancedFunction;
import org.rares.miner49er.ui.custom.functions.NoErrorPredicate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FormValidator<T> {

    private final T t;

    private final Map<Object, String> errorList = new HashMap<>();
    private final Map<Object, Integer> errorListInt = new HashMap<>();

    private FormValidator(T t) {
        this.t = t;
    }

    public static <T> FormValidator<T> of(T t) {
        return new FormValidator<>(Objects.requireNonNull(t));
    }

    private FormValidator<T> validate(
            NoErrorPredicate<T> validation,
            Object target, int messageRes) {
        if (!validation.test(t)) {
            errorListInt.put(target, messageRes);
        }
        return this;
    }

    public <U> FormValidator<T> validate(
            AdvancedFunction<T, U> projection,
            NoErrorPredicate<U> validation,
            Object target, int messageRes) {

        return validate(projection.andThen(validation::test)::apply, target, messageRes);
    }

    private FormValidator<T> validate(
            NoErrorPredicate<T> validation,
            Object target, String message) {
        if (!validation.test(t)) {
            errorList.put(target, message);
        }
        return this;
    }

    public <U> FormValidator<T> validate(
            AdvancedFunction<T, U> projection,
            NoErrorPredicate<U> validation,
            Object target, String message) {

        return validate(projection.andThen(validation::test)::apply, target, message);
    }

    public T get() throws FormValidationException {
        if (errorList.isEmpty()&&errorListInt.isEmpty()) {
            return t;
        } else {
            FormValidationException ex = new FormValidationException();
            ex.getInvalidFields().putAll(errorList);
            ex.getInvalidFieldsInt().putAll(errorListInt);
            throw ex;
        }
    }
}
