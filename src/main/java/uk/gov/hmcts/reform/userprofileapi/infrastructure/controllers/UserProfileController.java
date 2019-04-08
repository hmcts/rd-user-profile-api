package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.IdentifierName.*;

import io.swagger.annotations.*;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.userprofileapi.domain.service.UserProfileService;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.RequestData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

@Api(
    value = "/profiles",
    consumes = APPLICATION_JSON_UTF8_VALUE,
    produces = APPLICATION_JSON_UTF8_VALUE
)

@RequestMapping(
    path = "/profiles",
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)

@Slf4j
@RestController
public class UserProfileController {

    private UserProfileService<RequestData> userProfileService;

    public UserProfileController(UserProfileService<RequestData> userProfileService) {
        this.userProfileService = userProfileService;
    }

    @ApiOperation("Create a User Profile")
    @ApiResponses({
        @ApiResponse(
            code = 201,
            message = "Create a User Profile using request body",
            response = UserProfileResource.class
        ),
        @ApiResponse(
            code = 400,
            message = "Bad Request",
            response = String.class
        ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error",
            response = String.class
        )
    })

    @PostMapping(
        consumes = APPLICATION_JSON_UTF8_VALUE,
        produces = APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileResource> createUserProfile(@Valid @RequestBody CreateUserProfileData createUserProfileData) {
        log.info("Creating new User Profile");

        requireNonNull(createUserProfileData, "createUserProfileData cannot be null");

        UserProfileResource resource = userProfileService.create(createUserProfileData);

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);

    }

    @ApiOperation("Retrieves user profile data by id")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Representation of a user profile data",
            response = String.class
        ),
        @ApiResponse(
            code = 400,
            message = "Bad Request",
            response = String.class
        ),
        @ApiResponse(
            code = 404,
            message = "Not Found",
            response = String.class
        ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error",
            response = String.class
        )
    })
    @GetMapping(
        path = "/{uuid}",
        consumes = APPLICATION_JSON_UTF8_VALUE,
        produces = APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileResource> getUserProfileById(@PathVariable String uuid) {
        log.info("Getting user profile with id: {}", uuid);

        requireNonNull(uuid, "uuid cannot be null");

        return ResponseEntity.ok(
            userProfileService.retrieve(
                new UserProfileIdentifier(UUID, uuid)
            )
        );
    }

    @ApiOperation("Retrieves user profile queried by email")
    @ApiParam(name = "email", required = true)

    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Representation of a user profile data",
            response = String.class
        ),
        @ApiResponse(
            code = 400,
            message = "Bad Request",
            response = String.class
        ),
        @ApiResponse(
            code = 404,
            message = "Not Found",
            response = String.class
        ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error",
            response = String.class
        )
    })
    @GetMapping(
        params = "email",
        consumes = APPLICATION_JSON_UTF8_VALUE,
        produces = APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileResource> getUserProfileByEmail(@RequestParam String email) {
        log.info("Getting user profile with email: {}", email);

        requireNonNull(email, "email cannot be null");

        return ResponseEntity.ok(
            userProfileService.retrieve(
                new UserProfileIdentifier(EMAIL, email)
            )
        );
    }

    @ApiOperation("Retrieves user profile queried by idamId")
    @ApiParam(name = "idamId", required = true)
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Representation of a user profile data",
            response = String.class
        ),
        @ApiResponse(
            code = 400,
            message = "Bad Request",
            response = String.class
        ),
        @ApiResponse(
            code = 404,
            message = "Not Found",
            response = String.class
        ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error",
            response = String.class
        )
    })

    @GetMapping(
        params = "idamId",
        consumes = APPLICATION_JSON_UTF8_VALUE,
        produces = APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileResource> getUserProfileByIdamId(@RequestParam String idamId) {
        log.info("Getting user profile with idamId: {}", idamId);

        requireNonNull(idamId, "idamId cannot be null");

        return ResponseEntity.ok(
            userProfileService.retrieve(
                new UserProfileIdentifier(IDAMID, idamId)
            )
        );
    }

}
