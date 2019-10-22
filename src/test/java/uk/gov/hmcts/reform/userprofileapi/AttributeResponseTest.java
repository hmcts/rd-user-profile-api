package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import org.junit.Test;
import org.springframework.http.HttpStatus;

public class AttributeResponseTest {

    private HttpStatus httpStatus = HttpStatus.OK;

    private AttributeResponse sut = new AttributeResponse(httpStatus);

    @Test
    public void testAttributeResponse() {
        final Integer expectedIdamStatusCode = httpStatus.value();
        final String expectedIdamMessage = resolveStatusAndReturnMessage(httpStatus);

        assertThat(sut.getIdamStatusCode()).isEqualTo(expectedIdamStatusCode);
        assertThat(sut.getIdamMessage()).isEqualTo(expectedIdamMessage);
    }

}