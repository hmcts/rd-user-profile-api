package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.IdentifierName.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.userprofileapi.domain.service.UserProfileService;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
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

@RestController
public class UserProfileController {
    private static final Logger LOG = LoggerFactory.getLogger(UserProfileController.class);

    private UserProfileService userProfileService;

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
    public ResponseEntity<UserProfileResource> createUserProfile(@NotEmpty @RequestBody CreateUserProfileData createUserProfileData) {
        LOG.info("Creating new User Profile");

        requireNonNull(createUserProfileData, "createUserProfileData cannot be null");
        requireNonNull(createUserProfileData.getEmail(), "email cannot be null");
        requireNonNull(createUserProfileData.getFirstName(), "firstname cannot be null");
        requireNonNull(createUserProfileData.getLastName(), "lastname cannot be null");


        UserProfileResource resource = userProfileService.create(createUserProfileData);

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);

    }

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
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
        LOG.info("Getting user profile with id: {}", uuid);

        requireNonNull(uuid, "uuid cannot be null");

        return ResponseEntity.ok(
            userProfileService.retrieve(
                new UserProfileIdentifier(UUID, uuid)
            )
        );
    }

    @ApiOperation("Retrieves user profile queried by email")
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
        LOG.info("Getting user profile with email: {}", email);

        requireNonNull(email, "email cannot be null");

        return ResponseEntity.ok(
            userProfileService.retrieve(
                new UserProfileIdentifier(EMAIL, email)
            )
        );
    }

    @ApiOperation("Retrieves user profile queried by idamId")
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
        LOG.info("Getting user profile with idamId: {}", idamId);

        requireNonNull(idamId, "idamId cannot be null");

        return ResponseEntity.ok(
            userProfileService.retrieve(
                new UserProfileIdentifier(IDAMID, idamId)
            )
        );
    }

}
