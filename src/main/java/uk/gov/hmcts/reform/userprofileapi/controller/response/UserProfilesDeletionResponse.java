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

    public UserProfilesDeletionResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;

    }
}
