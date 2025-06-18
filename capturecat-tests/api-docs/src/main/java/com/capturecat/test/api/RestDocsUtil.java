package com.capturecat.test.api;

import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;

public class RestDocsUtil {

    private RestDocsUtil() {
    }

    public static OperationRequestPreprocessor requestPreprocessor() {
        return Preprocessors.preprocessRequest(
                Preprocessors.modifyUris().scheme("https").host("dev.capture-cat.com").removePort(),
                Preprocessors.prettyPrint());
    }

    public static OperationResponsePreprocessor responsePreprocessor() {
        return Preprocessors.preprocessResponse(Preprocessors.prettyPrint());
    }
}
