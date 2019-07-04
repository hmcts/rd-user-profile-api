package uk.gov.hmcts.reform.userprofileapi.domain.feign;

import feign.Headers;
import feign.RequestLine;

import javax.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.IdamUserResponse;

@FeignClient(name = "IdamFeignClient", url = "${idamUrl}")
public interface IdamFeignClient {

    @RequestMapping(method = RequestMethod.POST, value = "/user/registration")
    @RequestLine("POST /user/registration")
    @Headers("Content-Type: application/json")
    public ResponseEntity createUserProfile(@Valid @RequestBody Object createUserProfileData);

    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/users", params = "email")
    @RequestLine("GET /api/v1/users")
    @Headers("Content-Type: application/json")
    public ResponseEntity<IdamUserResponse> getUserByEmail(@RequestParam("email") String email);

    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/users/{userId}")
    @RequestLine("GET /api/v1/users/{userId}")
    @Headers("Content-Type: application/json")
    public ResponseEntity<IdamUserResponse> getUserById(@PathVariable("userId") String userId);
}