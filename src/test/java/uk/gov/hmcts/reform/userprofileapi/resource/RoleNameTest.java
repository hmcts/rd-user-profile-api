package uk.gov.hmcts.reform.userprofileapi.resource;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RoleNameTest {

    @Test
    public void test_hold_values_after_creation() {
        RoleName roleName = new RoleName("pui-case-manager");
        assertThat(roleName.getName()).isEqualTo("pui-case-manager");

        RoleName roleName1 = new RoleName("pui-manager");
        assertThat(roleName1.getName()).isEqualTo("pui-manager");
    }
}