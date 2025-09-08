package com.capturecat.core.api.user;

import static com.capturecat.core.support.error.ErrorType.TAG_NOT_FOUND;
import static com.capturecat.core.support.error.ErrorType.TOO_MANY_USER_TAGS;
import static com.capturecat.core.support.error.ErrorType.USER_NOT_FOUND;
import static com.capturecat.core.support.error.ErrorType.USER_TAG_ALREADY_EXISTS;
import static com.capturecat.core.support.error.ErrorType.USER_TAG_NOT_FOUND;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.capturecat.core.api.error.ErrorCodeDocumentTest;
import com.capturecat.test.snippet.ErrorCodeDescriptor;

class UserErrorCodeControllerTest extends ErrorCodeDocumentTest {

	@Test
	void 유저_태그_생성_에러_코드_문서화() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(USER_TAG_ALREADY_EXISTS,
			TOO_MANY_USER_TAGS, USER_NOT_FOUND);
		generateErrorDocs("errorCode/createUserTag", errorCodeDescriptors);
	}

	@Test
	void 유저_태그_수정_에러_코드_문서화() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(USER_TAG_ALREADY_EXISTS,
			USER_NOT_FOUND, TAG_NOT_FOUND, USER_TAG_NOT_FOUND);
		generateErrorDocs("errorCode/updateUserTag", errorCodeDescriptors);
	}
}
