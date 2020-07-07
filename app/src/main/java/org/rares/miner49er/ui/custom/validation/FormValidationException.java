package org.rares.miner49er.ui.custom.validation;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class FormValidationException extends IllegalStateException {
    @Getter
    private Map<Object, String> invalidFields = new HashMap<>();
    @Getter
    private Map<Object, Integer> invalidFieldsInt = new HashMap<>();
}
