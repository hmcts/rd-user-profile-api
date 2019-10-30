package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.exception.UndefinedException;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionServiceImplTest {

    private ExceptionServiceImpl sut = new ExceptionServiceImpl();

    @Test(expected = ResourceNotFoundException.class)
    public void testThrowRuntimeException() {

        sut.throwCustomRuntimeException(ExceptionType.ResourceNotFoundException,"ResourceNotFoundException Message");

    }

    @Test(expected = IdamServiceException.class)
    public void testThrowIdamServiceException() {

        sut.throwCustomRuntimeException(ExceptionType.IdamServiceException,"IdamServiceException Message");

    }

    @Test(expected = RequiredFieldMissingException.class)
    public void testThrowRequiredFieldMissingException() {

        sut.throwCustomRuntimeException(ExceptionType.RequiredFieldMissingException,"RequiredFieldMissingException Message");

    }

    @Test(expected = UndefinedException.class)
    public void testThrowDefaultException() {

        sut.throwCustomRuntimeException(ExceptionType.UndefinedException,"ExceptionNotFound Message");

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testOverloadedException() {

        sut.throwCustomRuntimeException(ExceptionType.ResourceNotFoundException,"ResourceNotFoundException Message", HttpStatus.ACCEPTED);

    }

}
