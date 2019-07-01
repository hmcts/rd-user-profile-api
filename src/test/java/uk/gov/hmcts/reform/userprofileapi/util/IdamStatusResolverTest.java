package uk.gov.hmcts.reform.userprofileapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.ACCEPTED;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.INVALID_REQUEST;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.MISSING_TOKEN;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.NOT_FOUND;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.OK;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.TOKEN_EXPIRED;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.UNKNOWN;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.USER_EXISTS;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class IdamStatusResolverTest {

    @Test
    public void should_return_error_message_by_HttpStatus_provided() {
        String httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.CREATED);
        assertThat(httpStatusString).isEqualTo(ACCEPTED);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.OK);
        assertThat(httpStatusString).isEqualTo(OK);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.BAD_REQUEST);
        assertThat(httpStatusString).isEqualTo(INVALID_REQUEST);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.UNAUTHORIZED);
        assertThat(httpStatusString).isEqualTo(MISSING_TOKEN);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.FORBIDDEN);
        assertThat(httpStatusString).isEqualTo(TOKEN_EXPIRED);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.FORBIDDEN);
        assertThat(httpStatusString).isEqualTo(TOKEN_EXPIRED);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.NOT_FOUND);
        assertThat(httpStatusString).isEqualTo(NOT_FOUND);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.CONFLICT);
        assertThat(httpStatusString).isEqualTo(USER_EXISTS);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.ALREADY_REPORTED);
        assertThat(httpStatusString).isEqualTo(UNKNOWN);
    }
}
