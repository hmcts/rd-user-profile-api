package uk.gov.hmcts.reform.userprofileapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
import uk.gov.hmcts.reform.userprofileapi.exception.ErrorPersistingException;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.exception.UndefinedException;
import uk.gov.hmcts.reform.userprofileapi.service.ExceptionService;

@Service
@Slf4j
public class ExceptionServiceImpl implements ExceptionService {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    public void throwCustomRuntimeException(ExceptionType className, String msg) {
        throwCustomRuntimeException(className, msg, HttpStatus.OK);
    }

    public void throwCustomRuntimeException(ExceptionType className, String msg, HttpStatus httpStatus) {
        log.error("{}:: {}", loggingComponentName, msg);
        switch (className) {
            case IDAMSERVICEEXCEPTION : throw new IdamServiceException(msg, httpStatus);
            case REQUIREDFIELDMISSINGEXCEPTION : throw new RequiredFieldMissingException(msg);
            case RESOURCENOTFOUNDEXCEPTION : throw new ResourceNotFoundException(msg);
            case ERRORPERSISTINGEXCEPTION : throw new ErrorPersistingException(msg);
            case BADREQUEST: throw new InvalidRequest(msg);
            case TOOMANYREQUESTS: throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, msg);
            default: throw new UndefinedException("Unhandled exception:" + msg);
        }
    }
}
