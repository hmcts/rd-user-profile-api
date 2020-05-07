package uk.gov.hmcts.reform.userprofileapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserProfilesDeletionResponse {

    private int statusCode;
    private String message;
    private String errorDescription;

    public UserProfilesDeletionResponse(int statusCode, String message, String errorDescription) {
        this.statusCode = statusCode;
        this.message = message;
        this.errorDescription = errorDescription;

    }

}
