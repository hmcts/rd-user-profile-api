package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.*;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileQueryProvider;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileRetrieverTest {

    @InjectMocks
    private UserProfileRetriever userProfileRetriever;

    @Mock
    IdamServiceImpl idamServiceMock;

    UserProfileQueryProvider querySupplier = mock(UserProfileQueryProvider.class);

    @Mock
    private Supplier<Optional<UserProfile>> supplier;

    IdamRolesInfo idamRolesInfoMock = mock(IdamRolesInfo.class);
    AuditRepository auditRepository = mock(AuditRepository.class);
    Audit audit = mock(Audit.class);

    @Test
    public void should_run_query_and_respond_with_user_profile() {

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();

        Stream.of(IdentifierName.values())
            .forEach(identifierName -> {
                    UserProfileIdentifier identifier = new UserProfileIdentifier(
                        identifierName,
                        String.valueOf(new Random().nextInt()));

                    when(querySupplier.getRetrieveByIdQuery(identifier)).thenReturn(supplier);
                    when(supplier.get()).thenReturn(Optional.of(userProfile));

                    UserProfile entity = userProfileRetriever.retrieve(identifier, false);
                    assertThat(entity).isEqualTo(userProfile);
                }
            );
    }

    @Test
    public void should_throw_exception_when_query_returns_empty_result() {

        UserProfileIdentifier identifier =
            new UserProfileIdentifier(
                IdentifierName.UUID,
                UUID.randomUUID().toString());

        when(querySupplier.getRetrieveByIdQuery(identifier)).thenReturn(supplier);
        when(supplier.get()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileRetriever.retrieve(identifier, false))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Could not find resource from database with given identifier: " + identifier.getValue());

    }

    @Test
    public void should_throw_exception_when_query_provider_throws_exception() {

        UserProfileIdentifier identifier =
            new UserProfileIdentifier(
                IdentifierName.UUID,
                UUID.randomUUID().toString());

        when(querySupplier.getRetrieveByIdQuery(identifier)).thenThrow(IllegalStateException.class);

        assertThatThrownBy(() -> userProfileRetriever.retrieve(identifier, false))
            .isInstanceOf(IllegalStateException.class);

    }

    @Test
    public void should_throw_exception_when_query_throws_exception() {

        UserProfileIdentifier identifier =
            new UserProfileIdentifier(
                IdentifierName.UUID,
                UUID.randomUUID().toString());

        when(querySupplier.getRetrieveByIdQuery(identifier)).thenReturn(supplier);
        when(supplier.get()).thenThrow(ResourceNotFoundException.class);

        assertThatThrownBy(() -> userProfileRetriever.retrieve(identifier, false))
            .isInstanceOf(ResourceNotFoundException.class);

    }

    /*@Test
    public List<UserProfile> retrieve_Multiple_Profiles(UserProfileIdentifier identifier, boolean showDeleted) {
        //get all users from UP DB
        List<UserProfile> userProfiles =
                querySupplier.getProfilesByIds(identifier, showDeleted).orElse(new ArrayList<UserProfile>());
        if (CollectionUtils.isEmpty(userProfiles)) {
            throw new ResourceNotFoundException("Could not find resource");
        }
        //get roles from sidam for each user
        List<UserProfile> userProfilesWithRoles = userProfiles.stream().map(profile -> userProfileRetriever.getRolesFromIdam(profile, true)).collect(Collectors.toList());
        return userProfilesWithRoles;
    }*/

    @Test
    public void should_retrieve_Multiple_Profiles() {

        List<String> userIds = new ArrayList<>();
        userIds.add(UUID.randomUUID().toString());
        userIds.add(UUID.randomUUID().toString());
        List<UserProfile> userProfiles = new ArrayList<>();
        UserProfile up1 = UserProfileTestDataBuilder.buildUserProfile();
        up1.setStatus(IdamStatus.ACTIVE);
        UserProfile up2 = UserProfileTestDataBuilder.buildUserProfile();
        userProfiles.add(up1);
        up2.setStatus(IdamStatus.ACTIVE);
        userProfiles.add(up2);
        UserProfileIdentifier identifier =
                new UserProfileIdentifier(
                        IdentifierName.UUID_LIST,
                        userIds);
        when(querySupplier.getProfilesByIds(identifier, true)).thenReturn(Optional.of(userProfiles));
        when(idamServiceMock.fetchUserById(any(String.class))).thenReturn(idamRolesInfoMock);
        when(idamRolesInfoMock.getResponseStatusCode()).thenReturn(HttpStatus.OK);
        when(auditRepository.save(any())).thenReturn(audit);

        List<UserProfile> userProfilesWithRoles = userProfileRetriever.retrieveMultipleProfiles(identifier, true, true);
        assertThat(userProfilesWithRoles.size()).isEqualTo(2);

        UserProfile getUserProfile1 = userProfilesWithRoles.get(0);

        assertThat(getUserProfile1.getEmail()).isEqualTo(up1.getEmail());
        assertThat(getUserProfile1.getFirstName()).isEqualTo(up1.getFirstName());
        assertThat(getUserProfile1.getLastName()).isEqualTo(up1.getLastName());
        assertThat(getUserProfile1.getRoles()).isEqualTo(up1.getRoles());
        assertThat(getUserProfile1.getErrorMessage()).isEqualTo(up1.getErrorMessage());
        assertThat(getUserProfile1.getErrorStatusCode()).isEqualTo(up1.getErrorStatusCode());
    }

    @Test
    public void should_throw_404_when_no_profiles_found_in_db() {

        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        List<UserProfile> userProfiles = new ArrayList<>();
        UserProfile up1 = UserProfileTestDataBuilder.buildUserProfile();
        UserProfile up2 = UserProfileTestDataBuilder.buildUserProfile();
        userProfiles.add(up1);
        userProfiles.add(up2);

        when(querySupplier.getProfilesByIds(identifier, true)).thenThrow(ResourceNotFoundException.class);

        assertThatThrownBy(() -> userProfileRetriever.retrieveMultipleProfiles(identifier, true, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void should_retrieve_user_multiple_profiles_without_roles_when_idam_fails() {

        UserProfile up = UserProfileTestDataBuilder.buildUserProfile();
        up.setStatus(IdamStatus.ACTIVE);
        when(idamServiceMock.fetchUserById(any(String.class))).thenReturn(idamRolesInfoMock);
        when(auditRepository.save(any())).thenReturn(audit);
        when(idamRolesInfoMock.getResponseStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(idamRolesInfoMock.getStatusMessage()).thenReturn("some error message");

        UserProfile profile = userProfileRetriever.getRolesFromIdam(up, true);

        assertThat(profile).isNotNull();
        assertThat(profile.getEmail()).isEqualTo(up.getEmail());
        assertThat(profile.getFirstName()).isEqualTo(up.getFirstName());
        assertThat(profile.getLastName()).isEqualTo(up.getLastName());
        assertThat(profile.getRoles().size()).isEqualTo(0);
        assertThat(profile.getErrorMessage()).isNotEmpty();
        assertThat(profile.getErrorStatusCode()).isEqualTo("404");
    }

    @Test
    public void should_throw_404_single_user_profile_without_roles_when_idam_fails() {

        UserProfile up = UserProfileTestDataBuilder.buildUserProfile();
        up.setStatus(IdamStatus.ACTIVE);

        when(idamServiceMock.fetchUserById(any(String.class))).thenReturn(idamRolesInfoMock);
        when(idamRolesInfoMock.getResponseStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(idamRolesInfoMock.getStatusMessage()).thenReturn("some error message");
        when(auditRepository.save(any())).thenReturn(audit);

        assertThatThrownBy(() -> userProfileRetriever.getRolesFromIdam(up, false))
                .isInstanceOf(IdamServiceException.class);
    }

    @Test
    public void should_not_call_idam_when_status_is_pending() {

        UserProfile up = UserProfileTestDataBuilder.buildUserProfile();
        UserProfile profile = userProfileRetriever.getRolesFromIdam(up, true);
        assertThat(profile).isNotNull();
        assertThat(profile.getEmail()).isEqualTo(up.getEmail());
        assertThat(profile.getFirstName()).isEqualTo(up.getFirstName());
        assertThat(profile.getLastName()).isEqualTo(up.getLastName());
        assertThat(profile.getRoles().size()).isEqualTo(0);
        assertThat(profile.getErrorMessage()).isEqualTo(IdamStatusResolver.NO_IDAM_CALL);
        assertThat(profile.getErrorStatusCode()).isEqualTo(" ");
    }

}
