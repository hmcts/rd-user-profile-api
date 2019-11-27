package uk.gov.hmcts.reform.userprofileapi.service;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;

public interface ExceptionService {

    void throwCustomRuntimeException(ExceptionType className, String msg);

    void throwCustomRuntimeException(ExceptionType className, String msg, HttpStatus httpStatus);

}
