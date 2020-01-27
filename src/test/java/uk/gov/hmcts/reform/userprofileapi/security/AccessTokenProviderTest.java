package uk.gov.hmcts.reform.userprofileapi.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AccessTokenProviderTest {

    AccessTokenProvider accessTokenProvider;

    public AccessTokenProviderTest(AccessTokenProvider accessTokenProvider) {
        this.accessTokenProvider = accessTokenProvider;
    }

    @Test(expected = IllegalStateException.class)
    public final void getAccessTokenShouldThrowIllegalStateExceptionTest() {
        accessTokenProvider.getAccessToken();
    }

    @Test(expected = IllegalStateException.class)
    public final void tryGetAccessTokenShouldThrowIllegalStateExceptionTest() {
        accessTokenProvider.tryGetAccessToken();
    }

    @Parameterized.Parameters
    public static Object instancesToTest() {
        return new Object[]{new RequestUserAccessTokenProvider()};
    }
}
