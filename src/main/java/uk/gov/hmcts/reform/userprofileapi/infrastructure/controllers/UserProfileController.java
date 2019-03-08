package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.userprofileapi.domain.service.RequestManager;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileCreationData;
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

    private RequestManager requestManager;

    public UserProfileController(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @ApiOperation("Retrieves user profile data by id")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Representation of a user profile data",
            response = String.class
        )
    })
    @GetMapping(
        path = "/{uuid}",
        consumes = APPLICATION_JSON_UTF8_VALUE,
        produces = APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileResource> getUserProfile(@PathVariable String uuid) {
        LOG.info("Getting user profile with id: {}", uuid);

        return ResponseEntity.ok(
            requestManager.handle(
                new UserProfileIdentifier(new Pair<>("UUID", uuid))
            )
        );

    }

    @PostMapping(
        consumes = APPLICATION_JSON_UTF8_VALUE,
        produces = APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileResource> createUserProfile(@RequestBody UserProfileCreationData userProfileRequestData) {

        LOG.info("Creating new User Profile");

        UserProfileResource resource = requestManager.handle(userProfileRequestData);

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);

    }

}
