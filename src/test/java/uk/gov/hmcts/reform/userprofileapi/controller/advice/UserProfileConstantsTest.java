package uk.gov.hmcts.reform.userprofileapi.controller.advice;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.Constructor;
import org.junit.Test;

public class UserProfileConstantsTest {

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<UserProfileConstants> constructor = UserProfileConstants.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
