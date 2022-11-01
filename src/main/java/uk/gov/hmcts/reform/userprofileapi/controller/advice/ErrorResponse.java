package uk.gov.hmcts.reform.userprofileapi.controller.advice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    public ErrorResponse() {
        // required for parsing error response in integration/functional test cases
    }

    private String errorMessage;

    private String errorDescription;

    private String timeStamp;
    private int status;
}
