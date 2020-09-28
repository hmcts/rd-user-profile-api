package uk.gov.hmcts.reform.userprofileapi.controller;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUserIdValid;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.validateCreateUserProfileRequest;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.validateUserIds;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;
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

    @Autowired
    private ValidationService validationService;


    @ApiOperation(value = "Create a User Profile",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            })
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "User Profile created successfully",
                    response = UserProfileCreationResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request has been provided"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 409,
                    message = "A User already exists with the given information"
            ),
            @ApiResponse(
                    code = 429,
                    message = "Too many requests made for re-invite"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })

    @PostMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<UserProfileCreationResponse> createUserProfile(
            @Valid @RequestBody UserProfileCreationData userProfileCreationData) {

        UserProfileCreationResponse resource = null;
        validateCreateUserProfileRequest(userProfileCreationData);

        if (userProfileCreationData.isResendInvite()) {
            resource = userProfileService.reInviteUser(userProfileCreationData);
        } else {
            resource = userProfileService.create(userProfileCreationData);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);

    }

    @ApiOperation(value = "Retrieves a User Profile and their Roles with the given ID",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Representation of a User profile with their Roles",
                    response = UserProfileWithRolesResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request has been provided"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "Not User Profile found with the given ID"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @GetMapping(
            path = "/{id}/roles",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileWithRolesResponse> getUserProfileWithRolesById(@PathVariable String id) {
        //Getting user profile by id
        isUserIdValid(id, true);
        UserProfileWithRolesResponse response = userProfileService
                .retrieveWithRoles(new UserProfileIdentifier(IdentifierName.UUID, id));
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Retrieves a User Profile and their Roles with the given Email Address",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization"),
                    @Authorization(value = "UserEmail")
            }
    )

    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Representation of a User profile with their Roles",
                    response = UserProfileWithRolesResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request has been provided"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No User Profile found with the given Email Address"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @GetMapping(
            path = "/roles",
            params = "email",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileWithRolesResponse> getUserProfileWithRolesByEmail(@RequestParam(value = "email",
            required = false) String email) {
        //Getting user profile by email from header or request param
        String userEmail = getUserEmail(email);
        requireNonNull(userEmail, "email cannot be null");
        UserProfileWithRolesResponse response = userProfileService
                .retrieveWithRoles(new UserProfileIdentifier(IdentifierName.EMAIL, userEmail.toLowerCase()));
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Retrieve a User Profile by Email or ID. If both are present then Email is used to retrieve.",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization"),
                    @Authorization(value = "UserEmail")
            })
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Representation of a User profile",
                    response = UserProfileResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request has been provided"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No User Profile found with the given ID"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )

    })
    @GetMapping(
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileResponse> getUserProfileByEmail(@RequestParam(value = "email",
                                                                                 required = false) String email,
                                                                     @RequestParam(value = "userId", required = false)
                                                                             String userId) {
        UserProfileResponse response;
        String userEmail = getUserEmail(email);
        if (userEmail == null && userId == null) {
            return ResponseEntity.badRequest().build();
        } else if (userEmail != null) {

            //Getting user profile by email

            response =
                    userProfileService.retrieve(
                            new UserProfileIdentifier(IdentifierName.EMAIL, userEmail.toLowerCase().trim())
                    );
        } else {
            isUserIdValid(userId, true);
            response = userProfileService.retrieve(
                    new UserProfileIdentifier(IdentifierName.UUID, userId.trim()));
        }
        return ResponseEntity.ok().body(response);
    }

    @ApiOperation(value = "Update a User Profile",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            })
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "User Profile has been Updated successfully",
                    response = UserProfileCreationResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request has been provided"
            ),
            @ApiResponse(
            code = 401,
            message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No User Profile found with the given ID"
            ),
            @ApiResponse(
                    code = 412,
                    message = "One or more of the Roles provided is already assigned to the User"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })

    @PutMapping(
            path = "/{userId}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )

    @ResponseBody
    public ResponseEntity<UserProfileRolesResponse> updateUserProfile(@Valid @RequestBody UpdateUserProfileData
                                                                                  updateUserProfileData,
                                            @PathVariable String userId,
                                            @ApiParam(name = "origin", required = false) @RequestParam(value = "origin",
                                                    required = false) String origin) {
        UserProfileRolesResponse userProfileResponse = null;
        if (CollectionUtils.isEmpty(updateUserProfileData.getRolesAdd())
                && CollectionUtils.isEmpty(updateUserProfileData.getRolesDelete())) {
            //Updating user profile details
            AttributeResponse attributeResponse = userProfileService.update(updateUserProfileData, userId, origin);
            userProfileResponse = new UserProfileRolesResponse();
            userProfileResponse.setAttributeResponse(attributeResponse);
            return ResponseEntity.status(attributeResponse.getIdamStatusCode()).body(userProfileResponse);

        } else { // New update roles behavior
            //Updating user roles
            UserProfileValidator.validateUserProfileDataAndUserId(updateUserProfileData, userId);
            userProfileResponse = userProfileService.updateRoles(updateUserProfileData, userId);
            return ResponseEntity.ok().body(userProfileResponse);
        }

    }

    @ApiOperation(value = "Retrieve multiple User Profiles",
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
                    message = "Successfully retrieved multiple User Profiles",
                    response = UserProfileDataResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request has been provided"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No User Profile found with the given ID"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })

    @PostMapping(
            path = "/users",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<UserProfileDataResponse> retrieveUserProfiles(@ApiParam(name = "showdeleted", required = true)
                                                                            @RequestParam(value = "showdeleted",
                                                                                    required = true) String showDeleted,
                                                                        @ApiParam(name = "rolesRequired",
                                                                                required = true)
                                                                        @RequestParam(value = "rolesRequired",
                                                                                required = true) String rolesRequired,
                                                                        @RequestBody UserProfileDataRequest
                                                                                    userProfileDataRequest) {
        //Retrieving multiple user profiles

        boolean showDeletedBoolean = UserProfileValidator.validateAndReturnBooleanForParam(showDeleted);
        boolean rolesRequiredBoolean = UserProfileValidator.validateAndReturnBooleanForParam(rolesRequired);
        validateUserIds(userProfileDataRequest);
        UserProfileDataResponse userProfileDataResponse =
                userProfileService.retrieveWithRoles(new UserProfileIdentifier(IdentifierName.UUID_LIST,
                        userProfileDataRequest.getUserIds()), showDeletedBoolean, rolesRequiredBoolean);
        return ResponseEntity.status(HttpStatus.OK).body(userProfileDataResponse);

    }

    @ApiOperation(value = "Delete an User Profiles",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            })
    @ApiResponses({
            @ApiResponse(
                    code = 204,
                    message = "User Profiles deleted successfully",
                    response = UserProfilesDeletionResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request has been provided"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })

    @DeleteMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ResponseBody
    public ResponseEntity<UserProfilesDeletionResponse> deleteUserProfiles(@Valid @RequestBody UserProfileDataRequest
                                                                                       userProfilesDeletionDataReq) {
        UserProfilesDeletionResponse resource = null;
        validateUserIds(userProfilesDeletionDataReq);
        resource = userProfileService.delete(userProfilesDeletionDataReq);
        return ResponseEntity.status(resource.getStatusCode()).body(resource);

    }

    private  String getUserEmail(String email) {
        String userEmail = null;
        ServletRequestAttributes servletRequestAttributes =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (nonNull(servletRequestAttributes)) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            userEmail = request.getHeader("UserEmail") != null ? request.getHeader("UserEmail") : email;
        }
        return userEmail;
    }

}