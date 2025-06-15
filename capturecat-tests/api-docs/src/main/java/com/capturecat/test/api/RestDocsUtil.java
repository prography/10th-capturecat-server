package com.capturecat.test.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;

public class RestDocsUtil {

    private RestDocsUtil() {
    }

    public static OperationRequestPreprocessor requestPreprocessor() {
        return Preprocessors.preprocessRequest(modifyHeaders().remove(HttpHeaders.CONTENT_TYPE)
                        .add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                , Preprocessors.prettyPrint());
    }

    public static OperationResponsePreprocessor responsePreprocessor() {
        return Preprocessors.preprocessResponse(Preprocessors.prettyPrint());
    }
}
