package uk.gov.hmcts.reform.userprofileapi.client;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IdamRegisterUserRequest {

    private String email;

    private String firstName;

    private String lastName;

    private String id;

    private List<String> roles;

}
