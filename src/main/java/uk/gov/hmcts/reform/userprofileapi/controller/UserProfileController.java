package uk.gov.hmcts.reform.userprofileapi.controller;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUserIdValid;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.validateCreateUserProfileRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.userprofileapi.client.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfilesRequest;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfilesResponse;
import uk.gov.hmcts.reform.userprofileapi.client.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.client.RequestData;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileService;
import uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator;

@Api(
    value = "/v1/userprofile"
)

@RequestMapping(
    path = "/v1/userprofile"
)

@Slf4j
@RestController
public class UserProfileController {

    @Autowired
    private UserProfileService<RequestData> userProfileService;

    @Autowired
    private IdamService idamService;


    @ApiOperation(value = "Create a User Profile",
                  authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization") 
                  })
    @ApiResponses({
        @ApiResponse(
            code = 201,
            message = "Create a User Profile using request body",
            response = CreateUserProfileResponse.class
        ),
        @ApiResponse(
            code = 400,
            message = "Bad Request",
            response = String.class
        ),
        @ApiResponse(
            code = 409,
            message = "User already exists",
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
    public ResponseEntity<CreateUserProfileResponse> createUserProfile(@Valid @RequestBody CreateUserProfileData createUserProfileData) {
        log.info("Creating new User Profile");

        validateCreateUserProfileRequest(createUserProfileData);

        CreateUserProfileResponse resource = userProfileService.create(createUserProfileData);
        log.info("idamid:" + resource.getIdamId() + " idamRegistrationResponse:" + resource.getIdamRegistrationResponse());
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);

    }

    @ApiOperation(value = "Retrieves user profile with roles by id",
                  authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
                  }
    )
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
        path = "/{id}/roles",
        produces = APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<GetUserProfileWithRolesResponse> getUserProfileWithRolesById(@PathVariable String id) {
        log.info("Getting user profile with id: {}", id);
        isUserIdValid(id, true);
        GetUserProfileWithRolesResponse response = userProfileService.retrieveWithRoles(new UserProfileIdentifier(IdentifierName.UUID, id));
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Retrieves user profile with roles by email",
                  authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
                  }
    )
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
            path = "/roles",
            params = "email",
            produces = APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<GetUserProfileWithRolesResponse> getUserProfileWithRolesByEmail(@RequestParam String email) {
        log.info("Getting user profile with email: {}", email);

        requireNonNull(email, "email cannot be null");
        GetUserProfileWithRolesResponse response = userProfileService.retrieveWithRoles(new UserProfileIdentifier(IdentifierName.EMAIL, email.toLowerCase()));
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Retrieves user profile queried by email or userId. If both provided email is preferred", 
                  authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
                  })
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Representation of a user profile data",
                    response = GetUserProfileResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request"
            ),
            @ApiResponse(
                    code = 404,
                    message = "Not Found"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @GetMapping(
            produces = APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<GetUserProfileResponse> getUserProfileByEmail(@ApiParam(name = "email", required = false) @RequestParam (value = "email", required = false) String email,
                                                                     @ApiParam(name = "userId", required = false) @RequestParam (value = "userId", required = false) String userId) {
        GetUserProfileResponse response = null;
        if (email == null && userId == null) {
            return ResponseEntity.badRequest().build();
        } else if (email != null) {

            log.info("Getting user profile with email: {}", email);

            response =
                    userProfileService.retrieve(
                            new UserProfileIdentifier(IdentifierName.EMAIL, email.toLowerCase().trim())
                    );
        } else {
            isUserIdValid(userId, true);
            response =  userProfileService.retrieve(
                    new UserProfileIdentifier(IdentifierName.UUID, userId.trim()));
        }
        return ResponseEntity.ok().body(response);
    }

    @ApiOperation(value = "Update user profile", 
                  authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
                  })
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Update User Profile using request body",
                    response = CreateUserProfileResponse.class
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

    @PutMapping(
            path = "/{userId}",
            consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE
    )

    @ResponseBody
    public ResponseEntity<UserProfileRolesResponse> updateUserProfile(@Valid @RequestBody UpdateUserProfileData updateUserProfileData,
                                                                      @PathVariable String userId,
                                                                      @ApiParam(name = "origin", required = false) @RequestParam (value = "origin", required = false) String origin) {
        log.info("Updating user profile");
        UserProfileRolesResponse userProfileResponse = new UserProfileRolesResponse();
        if (CollectionUtils.isEmpty(updateUserProfileData.getRolesAdd())
             && CollectionUtils.isEmpty(updateUserProfileData.getRolesDelete())) {

            log.info("Updating user profile without roles");
            AttributeResponse response = userProfileService.update(updateUserProfileData, userId, origin);
            userProfileResponse.setAttributeResponse(response);
            return ResponseEntity.status(Integer.valueOf(response.getIdamStatusCode())).body(userProfileResponse);
        } else {
            UserProfileValidator.validateUserProfileDataAndUserId(updateUserProfileData, userId);
            log.info("Updating user profile with roles");
            userProfileResponse = userProfileService.updateRoles(updateUserProfileData, userId);
            return ResponseEntity.ok(userProfileResponse);
        }
    }

    @ApiOperation(value = "Retrieving multiple user profiles",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            })
    @ApiParam(
            name = "showdeleted",
            required = true
    )

    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Retrieving multiple user profiles",
                    response = GetUserProfilesResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request",
                    response = String.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "Resource not found",
                    response = String.class
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error",
                    response = String.class
            )
    })

    @PostMapping(
            path = "/users",
            consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<GetUserProfilesResponse> retrieveUserProfiles(@ApiParam(name = "showdeleted", required = true)@RequestParam (value = "showdeleted", required = true) String showDeleted,
                                                                        @ApiParam(name = "rolesRequired", required = true)@RequestParam (value = "rolesRequired", required = true) String rolesRequired,
                                                                        @RequestBody GetUserProfilesRequest getUserProfilesRequest) {
        log.info("Retrieving multiple user profiles");

        boolean showDeletedBoolean = UserProfileValidator.validateAndReturnBooleanForParam(showDeleted);
        boolean rolesRequiredBoolean = UserProfileValidator.validateAndReturnBooleanForParam(rolesRequired);
        UserProfileValidator.validateUserIds(getUserProfilesRequest);
        GetUserProfilesResponse getUserProfilesResponse =
                userProfileService.retrieveWithRoles(new UserProfileIdentifier(IdentifierName.UUID_LIST, getUserProfilesRequest.getUserIds()), showDeletedBoolean, rolesRequiredBoolean);
        return ResponseEntity.status(HttpStatus.OK).body(getUserProfilesResponse);

    }

}
