package uk.gov.hmcts.reform.userprofileapi.controller.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IdamRegisterUserRequest {

    private String email;

    private String firstName;

    private String lastName;

    private String id;

    private List<String> roles;

}
