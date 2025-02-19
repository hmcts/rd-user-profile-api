package uk.gov.hmcts.reform.userprofileapi.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.util.CollectionUtils.isEmpty;


@SuppressWarnings("HideUtilityClassConstructor")
public final class IdamStatusResolver {

    private IdamStatusResolver() {
    }

    public static final String OK = "11 OK";
    public static final String ACCEPTED = "12 User Registration accepted";
    public static final String INVALID_REQUEST = "13 Required parameters or one of request field is missing or invalid";
    public static final String MISSING_TOKEN = "14 Missing Bearer Token";
    public static final String TOKEN_EXPIRED = "15 Bearer token is expired, or the user or service does not have "
            + "permission to perform this action";

    public static final String NOT_FOUND = "16 Resource not found";
    public static final String USER_EXISTS = "17 User with this email already exists";
    public static final String UNKNOWN = "18 Unknown error from Idam";
    public static final String NO_IDAM_CALL = "19 No call made to SIDAM to get the user roles as user status is not "
            .concat("'ACTIVE'");
    public static final String NO_CONTENT = "20 User Role Deleted";
    public static final String PRECONDITION_FAILED = "Problem while role addition/deletion";

    public static final String ACTIVE = "ACTIVE";
    public static final String PENDING = "PENDING";
    public static final String IDAM_5XX_ERROR_RESPONSE = "The identity server is unable to  process "
            + "the request due to high load.Please try again later";

    public static String resolveStatusAndReturnMessage(HttpStatus httpStatus) {
        switch (httpStatus) {
            case OK:
                return OK;
            case CREATED:
                return ACCEPTED;
            case BAD_REQUEST:
                return INVALID_REQUEST;
            case UNAUTHORIZED:
                return MISSING_TOKEN;
            case FORBIDDEN:
                return TOKEN_EXPIRED;
            case NOT_FOUND:
                return NOT_FOUND;
            case CONFLICT:
                return USER_EXISTS;
            case NO_CONTENT:
                return NO_CONTENT;
            case PRECONDITION_FAILED:
                return PRECONDITION_FAILED;
            default:
                return UNKNOWN;
        }
    }

    public static String resolveStatusAndReturnMessage(ResponseEntity<Object> responseEntity) {
        String errorMessage = EMPTY;// requires this initialisation for Fortify
        if (nonNull(responseEntity)) {
            Object responseBody = responseEntity.getBody();
            if (nonNull(responseBody) && responseBody instanceof IdamErrorResponse) {
                errorMessage = getErrorMessageFromSidamResponse(responseBody);
            }
        } else {
            responseEntity = status(UNAUTHORIZED).build();
        }
        return isNotBlank(errorMessage) ? errorMessage :
                resolveStatusAndReturnMessage(HttpStatus.valueOf(responseEntity.getStatusCode().value()));
    }

    public static String getErrorMessageFromSidamResponse(Object responseBody) {
        String errorMessage;
        IdamErrorResponse idamErrorResponse = (IdamErrorResponse)responseBody;
        if (!isEmpty(idamErrorResponse.getErrorMessages())) {
            errorMessage = idamErrorResponse.getErrorMessages().get(0);
        } else {
            errorMessage = idamErrorResponse.getErrorMessage();
        }
        return errorMessage;
    }

    public static Integer getStatusCodeValueFromResponseEntity(ResponseEntity<Object> responseEntity) {
        return nonNull(responseEntity) ? responseEntity.getStatusCode().value() : UNAUTHORIZED.value();
    }

    public static IdamStatus resolveIdamStatus(IdamRolesInfo idamRolesInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(null == idamRolesInfo.getPending() || !idamRolesInfo.getPending());
        sb.append(null == idamRolesInfo.getActive() || !idamRolesInfo.getActive());

        switch (sb.toString()) {
            case "falsetrue":
                return IdamStatus.PENDING;

            case "truefalse":
                return IdamStatus.ACTIVE;

            default:
                return IdamStatus.SUSPENDED;
        }
    }
}
