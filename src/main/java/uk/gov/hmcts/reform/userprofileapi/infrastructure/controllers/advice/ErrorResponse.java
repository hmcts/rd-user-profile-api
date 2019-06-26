package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers.advice;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

    private final String errorMessage;

    private final String errorDescription;

    private final String timeStamp;
}
