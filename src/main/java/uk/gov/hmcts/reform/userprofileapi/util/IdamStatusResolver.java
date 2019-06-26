package uk.gov.hmcts.reform.userprofileapi.util;

import org.springframework.http.HttpStatus;

@SuppressWarnings("HideUtilityClassConstructor")
public final class IdamStatusResolver {

    public static final String OK = "OK Success";
    public static final String ACCEPTED = "Registration accepted";
    public static final String PARAM_MISSING = "Required parameters are missing or invalid";
    public static final String MISSING_TOKEN = "Missing Bearer Token";
    public static final String TOKEN_EXPIRED = "Bearer token is expired, or it doesn’t have the ‘create-user’ scope";
    public static final String NOT_FOUND = "Not found";
    public static final String USER_EXISTS = "User with this email already exists";
    public static final String UNKNOWN = "Unknown error";

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
