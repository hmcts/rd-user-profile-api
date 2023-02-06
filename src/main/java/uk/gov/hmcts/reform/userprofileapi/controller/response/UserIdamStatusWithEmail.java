package uk.gov.hmcts.reform.userprofileapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserIdamStatusWithEmail {


    private String email;
    private String idamStatus;



    public UserIdamStatusWithEmail(String email, String idamStatus) {
        this.email = email;
        this.idamStatus = idamStatus;
    }
}
