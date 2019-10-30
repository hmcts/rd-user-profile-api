package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.service.ExceptionService;

import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionServiceImplTest {

    private ExceptionServiceImpl sut = new ExceptionServiceImpl();

    @Test(expected = ResourceNotFoundException.class)
    public void testThrowRuntimeException() {

        sut.throwCustomRuntimeException("ResourceNotFoundException","ResourceNotFoundException Message");

    }

    @Test(expected = IdamServiceException.class)
    public void testThrowIdamServiceException() {

        sut.throwCustomRuntimeException("IdamServiceException","IdamServiceException Message");

    }

    @Test(expected = RequiredFieldMissingException.class)
    public void testThrowRequiredFieldMissingException() {

        sut.throwCustomRuntimeException("RequiredFieldMissingException","RequiredFieldMissingException Message");

    }

    @Test(expected = RuntimeException.class)
    public void testThrowDefaultException() {

        sut.throwCustomRuntimeException("ExceptionNotFound","ExceptionNotFound Message");

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testOverloadedException() {

        sut.throwCustomRuntimeException("ResourceNotFoundException","ResourceNotFoundException Message", HttpStatus.ACCEPTED);

    }

}
