package uk.gov.hmcts.reform.userprofileapi.idam;

import com.fasterxml.jackson.annotation.JsonProperty;
import feign.Headers;
import feign.RequestLine;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


public interface IdamApi {

    @RequestMapping(method = RequestMethod.POST, value = "/testing-support/accounts")
    @RequestLine("POST /testing-support/accounts")
    @Headers("Content-Type: application/json")
    void createUser(CreateUserRequest createUserRequest);

    @Data
    @AllArgsConstructor
    @Builder(builderMethodName = "userRequestWith")
    class CreateUserRequest {
        private final String email;
        private final String forename = "John";
        private final String id = " ";
        private final String surname = "Smith";
        private final UserGroup userGroup;
        private final List<Role> roles;
        private final String password;
    }

    @AllArgsConstructor
    @Getter
    class UserGroup {
        private String code;
    }

    @AllArgsConstructor
    @Getter
    class Role {
        private String code;
    }

    @Data
    class AuthenticateUserResponse {
        @JsonProperty("code")
        private String code;
    }

    @Data
    class TokenExchangeResponse {
        @JsonProperty("access_token")
        private String accessToken;
    }
}
