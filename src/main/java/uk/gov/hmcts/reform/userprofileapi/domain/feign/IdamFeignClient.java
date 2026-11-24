package uk.gov.hmcts.reform.userprofileapi.domain.feign;

import com.fasterxml.jackson.annotation.JsonProperty;
import feign.Headers;
import feign.RequestLine;
import feign.Response;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.userprofileapi.config.FeignInterceptorConfiguration;

import java.util.List;
import java.util.Map;


@FeignClient(name = "IdamFeignClient", url = "${idam.api.url}", configuration = FeignInterceptorConfiguration.class)
public interface IdamFeignClient {

    @PostMapping(value = "/api/v1/users/registration")
    @RequestLine("POST /api/v1/users/registration")
    @Headers("Content-Type: application/json")
    public Response createUserProfile(@Valid @RequestBody Object createUserProfileData);

    @GetMapping(value = "/api/v1/users/{userId}")
    @RequestLine("GET /api/v1/users/{userId}")
    @Headers("Content-Type: application/json")
    public Response getUserById(@PathVariable("userId") String userId);

    @PutMapping(value = "/api/v1/users/{userId}/roles")
    @RequestLine("PUT /api/v1/users/{userId}/roles")
    @Headers("Content-Type: application/json")
    public Response updateUserRoles(@RequestBody Object rolesRequest, @PathVariable("userId") String userId);

    @PostMapping(value = "/api/v1/users/{userId}/roles")
    @RequestLine("POST /api/v1/users/{userId}/roles")
    @Headers("Content-Type: application/json")
    public Response addUserRoles(@RequestBody Object rolesRequest, @PathVariable("userId") String userId);

    @DeleteMapping(value = "/api/v1/users/{userId}/roles/{roleName}")
    @RequestLine("DELETE /api/v1/users/{userId}/roles/{roleName}")
    @Headers("Content-Type: application/json")
    public Response deleteUserRole(@PathVariable("userId") String userId, @PathVariable("roleName") String roleName);

    @PatchMapping(value = "/api/v1/users/{userId}")
    @RequestLine("PATCH /api/v1/users/{userId}")
    @Headers({"Content-Type: application/json"})
    public Response updateUserDetails(@RequestBody Object updateUserDetails, @PathVariable("userId") String userId);

    @DeleteMapping(value = "/api/v1/users/{userId}")
    @RequestLine("DELETE /api/v1/users/{userId}")
    @Headers({"Authorization: {authorization}", "Content-Type: application/json"})
    public Response deleteUser(@PathVariable("userId") String userId);

    @GetMapping(value = "/api/v1/users", consumes = {"application/x-www-form-urlencoded"})
    @Headers("authorization: {authorization}")
    public Response getUserFeed(@RequestParam Map<String, String> params);

    @Data
    class User {
        @JsonProperty("active")
        private boolean active;

        @JsonProperty("email")
        private String email;

        @JsonProperty("forename")
        private String forename;

        @JsonProperty("id")
        private String id;

        @JsonProperty("lastModified")
        private String lastModified;

        @JsonProperty("pending")
        private boolean pending;

        @JsonProperty("roles")
        private List<String> roles;

        @JsonProperty("surname")
        private String surname;
    }

}