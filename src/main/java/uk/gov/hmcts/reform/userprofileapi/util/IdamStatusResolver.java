package uk.gov.hmcts.reform.userprofileapi.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

@SuppressWarnings("HideUtilityClassConstructor")
public final class IdamStatusResolver {

    private IdamStatusResolver() {
    }

    public static final String OK = "11 OK";
    public static final String ACCEPTED = "12 User Registration accepted";
    public static final String INVALID_REQUEST = "13 Required parameters or one of request field is missing or invalid";
    public static final String MISSING_TOKEN = "14 Missing Bearer Token";
    public static final String TOKEN_EXPIRED = "15 Bearer token is expired, or it doesn’t have the ‘create-user’ scope";
    public static final String NOT_FOUND = "16 Resource not found";
    public static final String USER_EXISTS = "17 User with this email already exists";
    public static final String UNKNOWN = "18 Unknown error from Idam";
    public static final String NO_IDAM_CALL = "19 No call made to SIDAM to get the user roles as user status is ‘Pending’";
    public static final String NO_CONTENT = "20 User Role Deleted";

    public static final String ACTIVE = "ACTIVE";
    public static final String PENDING = "PENDING";

    //tbc refactor this to an enum and use std valueOf method
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
            default:
                return UNKNOWN;
        }
    }


    public static IdamStatus resolveIdamStatus(Map<Map<String, Boolean>, IdamStatus> statusResolver, IdamRolesInfo idamRolesInfo) {

        Map<String, Boolean> statusMap = new HashMap<>();
        statusMap.put(ACTIVE, idamRolesInfo.getActive() == null ? false : idamRolesInfo.getActive());
        statusMap.put(PENDING, idamRolesInfo.getPending() == null ? false : idamRolesInfo.getPending());

        return statusResolver.get(statusMap) != null ? statusResolver.get(statusMap) : IdamStatus.SUSPENDED;
    }
}
