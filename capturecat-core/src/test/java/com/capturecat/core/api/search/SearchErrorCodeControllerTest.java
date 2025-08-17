package com.capturecat.core.api.search;

import static com.capturecat.core.support.error.ErrorType.MISSING_PARAMETER;
import static com.capturecat.core.support.error.ErrorType.USER_NOT_FOUND;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.capturecat.core.api.error.ErrorCodeDocumentTest;
import com.capturecat.test.snippet.ErrorCodeDescriptor;

class SearchErrorCodeControllerTest extends ErrorCodeDocumentTest {

	@Test
	void 검색어_자동완성_에러_코드_문서화() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(MISSING_PARAMETER,
			USER_NOT_FOUND);
		generateErrorDocs("errorCode/autocomplete", errorCodeDescriptors);
	}
}
