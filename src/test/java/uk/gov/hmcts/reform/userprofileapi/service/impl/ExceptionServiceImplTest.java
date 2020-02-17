package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
import uk.gov.hmcts.reform.userprofileapi.exception.ErrorPersistingException;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.exception.UndefinedException;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionServiceImplTest {

    private ExceptionServiceImpl sut = new ExceptionServiceImpl();

    @Test(expected = ResourceNotFoundException.class)
    public void testThrowRuntimeException() {
        sut.throwCustomRuntimeException(ExceptionType.RESOURCENOTFOUNDEXCEPTION, "ResourceNotFoundException Message");
    }

    @Test(expected = IdamServiceException.class)
    public void testThrowIdamServiceException() {
        sut.throwCustomRuntimeException(ExceptionType.IDAMSERVICEEXCEPTION, "IdamServiceException Message");
    }

    @Test(expected = RequiredFieldMissingException.class)
    public void testThrowRequiredFieldMissingException() {
        sut.throwCustomRuntimeException(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION, "RequiredFieldMissingException Message");
    }

    @Test(expected = UndefinedException.class)
    public void testThrowDefaultException() {
        sut.throwCustomRuntimeException(ExceptionType.UNDEFINDEDEXCEPTION, "ExceptionNotFound Message");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testOverloadedException() {
        sut.throwCustomRuntimeException(ExceptionType.RESOURCENOTFOUNDEXCEPTION, "ResourceNotFoundException Message", HttpStatus.ACCEPTED);
    }

    @Test(expected = ErrorPersistingException.class)
    public void testErrorPersistingException() {
        sut.throwCustomRuntimeException(ExceptionType.ERRORPERSISTINGEXCEPTION, "Error while persisting user profile", HttpStatus.ACCEPTED);
    }

}
