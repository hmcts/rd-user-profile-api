package uk.gov.hmcts.reform.userprofileapi.resource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleNameTest {

    @Test
    void test_hold_values_after_creation() {
        RoleName roleName = new RoleName("pui-case-manager");
        assertThat(roleName.getName()).isEqualTo("pui-case-manager");

        RoleName roleName1 = new RoleName("pui-manager");
        assertThat(roleName1.getName()).isEqualTo("pui-manager");
    }
}
