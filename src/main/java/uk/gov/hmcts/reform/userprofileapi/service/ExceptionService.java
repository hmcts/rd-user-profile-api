package uk.gov.hmcts.reform.userprofileapi.service;

import org.springframework.http.HttpStatus;

public interface ExceptionService {

    void throwCustomRuntimeException(String className, String msg);

    void throwCustomRuntimeException(String className, String msg, HttpStatus httpStatus);

}
