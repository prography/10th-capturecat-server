package com.capturecat.test.snippet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.TemplatedSnippet;

public class ErrorCodeSnippet extends TemplatedSnippet {

    private List<ErrorCodeDescriptor> descriptors;

    public ErrorCodeSnippet(List<ErrorCodeDescriptor> descriptors) {
        super("error-codes", null);
        this.descriptors = descriptors;
    }

    @Override
    protected Map<String, Object> createModel(Operation operation) {
        Map<String, Object> model = new HashMap<>();
        model.put("fields", descriptors);
        return model;
    }
}
