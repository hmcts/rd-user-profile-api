package uk.gov.hmcts.reform.userprofileapi.domain.feign;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import javax.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.userprofileapi.config.FeignInterceptorConfiguration;

@FeignClient(name = "IdamFeignClient", url = "${auth.idam.client.baseUrl}", configuration = FeignInterceptorConfiguration.class)
public interface IdamFeignClient {

    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/users/registration")
    @RequestLine("POST /api/v1/users/registration")
    @Headers("Content-Type: application/json")
    public Response createUserProfile(@Valid @RequestBody Object createUserProfileData);

    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/users", params = "email")
    @RequestLine("GET /api/v1/users")
    @Headers("Content-Type: application/json")
    public Response getUserByEmail(@RequestParam("email") String email);

    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/users/{userId}")
    @RequestLine("GET /api/v1/users/{userId}")
    @Headers("Content-Type: application/json")
    public Response getUserById(@PathVariable("userId") String userId);

    @RequestMapping(method = RequestMethod.PUT, value = "/api/v1/users/{userId}/roles")
    @RequestLine("PUT /api/v1/users/{userId}/roles")
    @Headers("Content-Type: application/json")
    public Response updateUserRoles(@RequestBody Object rolesRequest, @PathVariable("userId") String userId);

    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/users/{userId}/roles")
    @RequestLine("POST /api/v1/users/{userId}/roles")
    @Headers("Content-Type: application/json")
    public Response addUserRoles(@RequestBody Object rolesRequest, @PathVariable("userId") String userId);
}