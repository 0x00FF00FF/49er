package org.rares.miner49er.ui.custom;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class FormValidationException extends IllegalStateException {
    @Getter
    private Map<Object, String> invalidFields = new HashMap<>();
}
