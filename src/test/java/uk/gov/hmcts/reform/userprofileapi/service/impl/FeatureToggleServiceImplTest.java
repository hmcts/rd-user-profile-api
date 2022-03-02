package uk.gov.hmcts.reform.userprofileapi.service.impl;

import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeatureToggleServiceImplTest {

    LDClient ldClient = mock(LDClient.class);
    FeatureToggleServiceImpl flaFeatureToggleService = mock(FeatureToggleServiceImpl.class);

    @Test
    void testIsFlagEnabled() {
        flaFeatureToggleService = new FeatureToggleServiceImpl(ldClient, "rd");
        Assertions.assertFalse(flaFeatureToggleService.isFlagEnabled("test", "test"));
    }

    @Test
    public void testIsFlagEnabled_true() {
        flaFeatureToggleService = new FeatureToggleServiceImpl(ldClient, "rd");
        when(flaFeatureToggleService.isFlagEnabled("test", "test")).thenReturn(true);
        assertTrue(flaFeatureToggleService.isFlagEnabled("test", "test"));
    }

    @Test
    void mapServiceToFlagTest() {
        flaFeatureToggleService = new FeatureToggleServiceImpl(ldClient, "rd");
        flaFeatureToggleService.mapServiceToFlag();
        Assertions.assertTrue(flaFeatureToggleService.getLaunchDarklyMap().size() >= 1);
    }
}
