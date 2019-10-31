package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.exception.UndefinedException;
import uk.gov.hmcts.reform.userprofileapi.service.ExceptionService;

@Service
public class ExceptionServiceImpl implements ExceptionService {

    public void throwCustomRuntimeException(ExceptionType className, String msg) {
        throwCustomRuntimeException(className, msg, HttpStatus.OK);
    }

    public void throwCustomRuntimeException(ExceptionType className, String msg, HttpStatus httpStatus) {
        switch (className) {
            case IDAMSERVICEEXCEPTION : throw new IdamServiceException(msg, httpStatus);
            case REQUIREDFIELDMISSINGEXCEPTION : throw new RequiredFieldMissingException(msg);
            case RESOURCENOTFOUNDEXCEPTION : throw new ResourceNotFoundException(msg);
            default: throw new UndefinedException("Unhandled exception:" + msg);
        }
    }
}
