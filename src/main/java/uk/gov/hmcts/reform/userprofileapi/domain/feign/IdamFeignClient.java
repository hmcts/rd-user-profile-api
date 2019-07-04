package uk.gov.hmcts.reform.userprofileapi.domain.feign;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "IdamFeignClient", url = "${idamUrl}")
public interface IdamFeignClient {


    @PostMapping(
            consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE,
            path = "/user/registration"
    )
    public Response createUserProfile(@RequestBody Object createUserProfileData);

    @GetMapping(
            produces = APPLICATION_JSON_UTF8_VALUE,
            path = "/api/v1/users",
            params = "email"
    )
    public Response getUserByEmail(@RequestParam String email);

    @GetMapping(
            produces = APPLICATION_JSON_UTF8_VALUE,
            path = "/api/v1/users/{userId}"
    )
    public Response getUserById(@PathVariable String userId);

    @PutMapping(
            consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE,
            path = "/api/v1/users/{userId}/roles"
    )
    public Response updateUserRoles(@RequestBody Object rolesRequest, @PathVariable String userId);
}