package uk.gov.hmcts.reform.userprofileapi.client;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleRequestTest {

    @Test
    public void should_hold_values_after_creation() {

        RoleRequest roleReq = new RoleRequest();
        List<Map> roles = new ArrayList<>();
        Map<String, String> rolesMap = new HashMap<String, String>();
        rolesMap.put("name", "pui-case-manager");
        roles.add(rolesMap);
        roleReq.setRoles(roles);
        assertThat(roleReq.getRoles().size()).isEqualTo(1);
    }
}