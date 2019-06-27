package uk.gov.hmcts.reform.userprofileapi.util;

import org.springframework.http.HttpStatus;

@SuppressWarnings("HideUtilityClassConstructor")
public final class IdamStatusResolver {

    public static final String OK = "11:IDAM: OK Success";
    public static final String ACCEPTED = "12:IDAM: Registration accepted";
    public static final String PARAM_MISSING = "13:IDAM: Required parameters are missing or invalid";
    public static final String MISSING_TOKEN = "14:IDAM: Missing Bearer Token";
    public static final String TOKEN_EXPIRED = "15:IDAM: Bearer token is expired, or it doesn’t have the ‘create-user’ scope";
    public static final String NOT_FOUND = "16:IDAM: Not found";
    public static final String USER_EXISTS = "17:IDAM: User with this email already exists";
    public static final String UNKNOWN = "18:IDAM: Unknown error";

    public static String resolveStatusAndReturnMessage(HttpStatus httpStatus) {
        switch (httpStatus) {
            case OK: return OK;
            case CREATED: return ACCEPTED;
            case BAD_REQUEST: return PARAM_MISSING;
            case UNAUTHORIZED: return MISSING_TOKEN;
            case FORBIDDEN: return TOKEN_EXPIRED;
            case NOT_FOUND: return NOT_FOUND;
            case CONFLICT: return USER_EXISTS;
            default:
                return UNKNOWN;
        }
    }
}
