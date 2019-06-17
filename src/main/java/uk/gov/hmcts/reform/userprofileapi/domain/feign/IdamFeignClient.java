package uk.gov.hmcts.reform.userprofileapi.domain.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
//import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamUserResponse;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@FeignClient(name = "IdamFeignClient", url = "172.29.14.129:8888")
public interface IdamFeignClient {


    @PostMapping(
            consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE,
            path = "/user/registration"
    )
    public ResponseEntity createUserProfile(@Valid @RequestBody Object createUserProfileData);

    @GetMapping(
            produces = APPLICATION_JSON_UTF8_VALUE,
            path = "/api/v1/users",
            params = "email"
    )
    public ResponseEntity<IdamUserResponse> getUserByEmail(@RequestParam String email);

    @GetMapping(
            produces = APPLICATION_JSON_UTF8_VALUE,
            path = "/api/v1/users/{userId}"
    )
    public ResponseEntity<IdamUserResponse> getUserById(@PathVariable String userId);
}