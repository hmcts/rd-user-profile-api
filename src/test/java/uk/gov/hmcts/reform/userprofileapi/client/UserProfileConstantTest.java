package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.constant.UserProfileConstant;


@RunWith(MockitoJUnitRunner.class)
public class UserProfileConstantTest {

    @Test
    public void should_Return_Correct_Values() {

        assertThat(UserProfileConstant.CASE_WORKER).isEqualTo("caseworker");
        assertThat(UserProfileConstant.LANGUAGE_PREFERENCE).isEqualTo("LANGUAGEPREFERENCE");
        assertThat(UserProfileConstant.STATUS).isEqualTo("STATUS");
        assertThat(UserProfileConstant.USER_TYPE).isEqualTo("USERTYPE");
        assertThat(UserProfileConstant.USER_CATEGORY).isEqualTo("USERCATEGORY");
        assertThat(UserProfileConstant.PUI_CASE_MANAGER).isEqualTo("pui-case-manager");
        assertThat(UserProfileConstant.PUI_ORGANISATION_MANAGER).isEqualTo("pui-organisation-manager");

    }

}