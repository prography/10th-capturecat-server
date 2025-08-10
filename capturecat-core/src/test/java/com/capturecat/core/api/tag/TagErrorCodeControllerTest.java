package com.capturecat.core.api.tag;

import static com.capturecat.core.support.error.ErrorType.TAG_NOT_FOUND;
import static com.capturecat.core.support.error.ErrorType.USER_NOT_FOUND;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.capturecat.core.api.error.ErrorCodeDocumentTest;
import com.capturecat.test.snippet.ErrorCodeDescriptor;

class TagErrorCodeControllerTest extends ErrorCodeDocumentTest {

	@Test
	void 태그_삭제_에러_코드_문서화() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(USER_NOT_FOUND, TAG_NOT_FOUND);
		generateErrorDocs("errorCode/deleteTag", errorCodeDescriptors);
	}
}
