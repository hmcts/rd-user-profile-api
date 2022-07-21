package uk.gov.hmcts.reform.userprofileapi.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class IdamErrorResponse {
    private int status;
    private List<String> errorMessages;
    private String errorMessage;
}
