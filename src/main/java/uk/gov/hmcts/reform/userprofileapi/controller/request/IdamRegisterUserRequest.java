package uk.gov.hmcts.reform.userprofileapi.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class IdamRegisterUserRequest {

    private String email;

    private String firstName;

    private String lastName;

    private String id;

    private List<String> roles;

}
