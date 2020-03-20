package uk.gov.hmcts.reform.userprofileapi.controller.advice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    public ErrorResponse() {
    }

    private String errorMessage;

    private String errorDescription;

    private String timeStamp;
}
