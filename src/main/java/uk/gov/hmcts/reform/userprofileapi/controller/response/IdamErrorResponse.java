package uk.gov.hmcts.reform.userprofileapi.controller.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IdamErrorResponse {
    private int status;
    private List<String> errorMessages;
    private String errorMessage;
}
