package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamUserResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileQueryProvider;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileRetrieverTest {

    @Spy
    @InjectMocks
    private UserProfileRetriever userProfileRetriever;

    @Mock
    IdamServiceImpl idamServiceMock;

    UserProfileQueryProvider querySupplier = mock(UserProfileQueryProvider.class);

    @Mock
    private Supplier<Optional<UserProfile>> supplier;

    private ResponseEntity<Object> entity;
    private IdamRolesInfo idamRolesInfo;
    private IdamUserResponse idamUserResponse;

    AuditRepository auditRepository = mock(AuditRepository.class);
    Audit audit = mock(Audit.class);

    @Before
    public void setUp() {
        Boolean active = true;
        String email = "some@hmcts.net";
        String foreName = "firstName";
        String userId = UUID.randomUUID().toString();
        List<String> roles = new ArrayList<>();
        roles.add("pui-case-manger");
        String surName = "lastName";
        Boolean pending = false;

        idamUserResponse = new IdamUserResponse(active, email, foreName, userId, pending, roles, surName);
        entity = new ResponseEntity<>(idamUserResponse, HttpStatus.CREATED);
    }

    @Test
    public void test_run_query_and_respond_with_user_profile() {
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
                    verify(querySupplier, times(1)).getRetrieveByIdQuery(identifier);
                });
    }

    @Test
    public void test_run_query_and_respond_with_user_profile_withFetchRolesTrue() {
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.EMAIL,
                String.valueOf(new Random().nextInt()));

        when(querySupplier.getRetrieveByIdQuery(identifier)).thenReturn(supplier);
        when(supplier.get()).thenReturn(Optional.of(userProfile));

        UserProfile entity = userProfileRetriever.retrieve(identifier, true);
        assertThat(entity).isEqualTo(userProfile);
        verify(querySupplier, times(1)).getRetrieveByIdQuery(identifier);
        verify(userProfileRetriever, times(1)).getRolesFromIdam(any(UserProfile.class), any(Boolean.class));
    }

    @Test
    public void test_throw_exception_when_query_returns_empty_result() {
        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());

        when(querySupplier.getRetrieveByIdQuery(identifier)).thenReturn(supplier);
        when(supplier.get()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileRetriever.retrieve(identifier, false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Could not find resource from database with given identifier");
        verify(querySupplier, times(1)).getRetrieveByIdQuery(identifier);

    }

    @Test
    public void test_throw_exception_when_query_provider_throws_exception() {
        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());

        when(querySupplier.getRetrieveByIdQuery(identifier)).thenThrow(IllegalStateException.class);

        assertThatThrownBy(() -> userProfileRetriever.retrieve(identifier, false))
                .isInstanceOf(IllegalStateException.class);
        verify(querySupplier, times(1)).getRetrieveByIdQuery(identifier);
    }

    @Test
    public void test_throw_exception_when_query_throws_exception() {
        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());

        when(querySupplier.getRetrieveByIdQuery(identifier)).thenReturn(supplier);
        when(supplier.get()).thenThrow(ResourceNotFoundException.class);

        assertThatThrownBy(() -> userProfileRetriever.retrieve(identifier, false))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(querySupplier, times(1)).getRetrieveByIdQuery(identifier);
    }

    @Test
    public void test_retrieve_Multiple_Profiles() {
        idamRolesInfo = new IdamRolesInfo(entity);

        List<UserProfile> userProfiles = new ArrayList<>();

        UserProfile up1 = UserProfileTestDataBuilder.buildUserProfile();
        up1.setStatus(IdamStatus.ACTIVE);
        userProfiles.add(up1);

        UserProfile up2 = UserProfileTestDataBuilder.buildUserProfile();
        up2.setStatus(IdamStatus.ACTIVE);
        userProfiles.add(up2);

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID_LIST,
                Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString()));

        when(querySupplier.getProfilesByIds(identifier, true)).thenReturn(Optional.of(userProfiles));
        when(idamServiceMock.fetchUserById(any(String.class))).thenReturn(idamRolesInfo);
        when(auditRepository.save(any())).thenReturn(audit);

        List<UserProfile> userProfilesWithRoles = userProfileRetriever.retrieveMultipleProfiles(identifier,
                true, true);
        assertThat(userProfilesWithRoles.size()).isEqualTo(2);

        UserProfile getUserProfile1 = userProfilesWithRoles.get(0);

        assertThat(getUserProfile1.getEmail()).isEqualTo(up1.getEmail());
        assertThat(getUserProfile1.getFirstName()).isEqualTo(up1.getFirstName());
        assertThat(getUserProfile1.getLastName()).isEqualTo(up1.getLastName());
        assertThat(getUserProfile1.getRoles()).isEqualTo(up1.getRoles());
        assertThat(getUserProfile1.getErrorMessage()).isEqualTo(up1.getErrorMessage());
        assertThat(getUserProfile1.getErrorStatusCode()).isEqualTo(up1.getErrorStatusCode());
        verify(querySupplier, times(1)).getProfilesByIds(identifier, true);
        verify(idamServiceMock, times(2)).fetchUserById(any(String.class));
        verify(auditRepository, times(2)).save(any());
    }

    @Test
    public void test_retrieve_Multiple_Profiles_RolesRequiredFalse() {
        idamRolesInfo = new IdamRolesInfo(entity);

        List<UserProfile> userProfiles = new ArrayList<>();

        UserProfile up1 = UserProfileTestDataBuilder.buildUserProfile();
        up1.setStatus(IdamStatus.ACTIVE);
        userProfiles.add(up1);

        UserProfile up2 = UserProfileTestDataBuilder.buildUserProfile();
        up2.setStatus(IdamStatus.ACTIVE);
        userProfiles.add(up2);

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID_LIST,
                Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString()));

        when(querySupplier.getProfilesByIds(identifier, true)).thenReturn(Optional.of(userProfiles));

        List<UserProfile> userProfilesWithRoles = userProfileRetriever.retrieveMultipleProfiles(identifier,
                true, false);
        assertThat(userProfilesWithRoles.size()).isEqualTo(2);

        UserProfile getUserProfile1 = userProfilesWithRoles.get(0);

        assertThat(getUserProfile1.getEmail()).isEqualTo(up1.getEmail());
        assertThat(getUserProfile1.getFirstName()).isEqualTo(up1.getFirstName());
        assertThat(getUserProfile1.getLastName()).isEqualTo(up1.getLastName());
        assertThat(getUserProfile1.getRoles()).isEqualTo(up1.getRoles());
        assertThat(getUserProfile1.getErrorMessage()).isEqualTo(up1.getErrorMessage());
        assertThat(getUserProfile1.getErrorStatusCode()).isEqualTo(up1.getErrorStatusCode());
        verify(querySupplier, times(1)).getProfilesByIds(identifier, true);
    }

    @Test
    public void test_throw_404_when_no_profiles_found_in_db() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        when(querySupplier.getProfilesByIds(identifier, true)).thenThrow(ResourceNotFoundException.class);

        assertThatThrownBy(() -> userProfileRetriever.retrieveMultipleProfiles(identifier, true,
                true))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(querySupplier, times(1)).getProfilesByIds(identifier, true);
    }

    @Test
    public void test_getRolesFromIdam_should_retrieve_multiple_profiles_without_roles_when_idam_fails() {
        entity = new ResponseEntity<>(idamUserResponse, HttpStatus.NOT_FOUND);
        idamRolesInfo = new IdamRolesInfo(entity);

        UserProfile up = UserProfileTestDataBuilder.buildUserProfile();
        up.setStatus(IdamStatus.ACTIVE);

        when(idamServiceMock.fetchUserById(any(String.class))).thenReturn(idamRolesInfo);
        when(auditRepository.save(any())).thenReturn(audit);

        UserProfile profile = userProfileRetriever.getRolesFromIdam(up, true);

        assertThat(profile).isNotNull();
        assertThat(profile.getEmail()).isEqualTo(up.getEmail());
        assertThat(profile.getFirstName()).isEqualTo(up.getFirstName());
        assertThat(profile.getLastName()).isEqualTo(up.getLastName());
        assertThat(profile.getRoles().size()).isZero();
        assertThat(profile.getErrorMessage()).isNotEmpty();
        assertThat(profile.getErrorStatusCode()).isEqualTo("404");

        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));
    }

    @Test
    public void test_retrieve_user_multiple_profiles_with_roles_when_idam_success() {
        idamRolesInfo = new IdamRolesInfo(entity);

        UserProfile up = UserProfileTestDataBuilder.buildUserProfile();
        up.setStatus(IdamStatus.ACTIVE);
        UserProfile upMock = mock(UserProfile.class);
        when(upMock.getIdamId()).thenReturn(up.getIdamId());
        when(upMock.getEmail()).thenReturn(up.getEmail());
        when(upMock.getFirstName()).thenReturn(up.getFirstName());
        when(upMock.getLastName()).thenReturn(up.getLastName());
        when(upMock.getStatus()).thenReturn(up.getStatus());

        when(idamServiceMock.fetchUserById(any(String.class))).thenReturn(idamRolesInfo);
        when(auditRepository.save(any())).thenReturn(audit);

        UserProfile profile = userProfileRetriever.getRolesFromIdam(upMock, true);

        assertThat(profile).isNotNull();
        assertThat(profile.getEmail()).isEqualTo(up.getEmail());
        assertThat(profile.getFirstName()).isEqualTo(up.getFirstName());
        assertThat(profile.getLastName()).isEqualTo(up.getLastName());
        assertThat(profile.getRoles().size()).isZero();
        assertThat(profile.getErrorMessage()).isNull();
        assertThat(profile.getErrorStatusCode()).isNull();

        verify(auditRepository, Mockito.times(1)).save(any(Audit.class));
        verify(upMock, times(1)).setRoles(any(IdamRolesInfo.class));
        verify(upMock, times(1)).setErrorMessage(any(String.class));
        verify(upMock, times(1)).setErrorStatusCode(any(String.class));
    }

    @Test
    public void should_throw_404_single_user_profile_without_roles_when_idam_fails() {
        entity = new ResponseEntity<>(idamUserResponse, HttpStatus.NOT_FOUND);
        idamRolesInfo = new IdamRolesInfo(entity);

        UserProfile up = UserProfileTestDataBuilder.buildUserProfile();
        up.setStatus(IdamStatus.ACTIVE);

        when(idamServiceMock.fetchUserById(any(String.class))).thenReturn(idamRolesInfo);
        when(auditRepository.save(any())).thenReturn(audit);

        assertThatThrownBy(() -> userProfileRetriever.getRolesFromIdam(up, false))
                .isInstanceOf(IdamServiceException.class);

        Mockito.verify(auditRepository, Mockito.times(1)).save(any(Audit.class));
    }

    @Test
    public void test_not_call_idam_when_status_is_pending() {
        UserProfile up = UserProfileTestDataBuilder.buildUserProfile();
        UserProfile profile = userProfileRetriever.getRolesFromIdam(up, true);

        assertThat(profile).isNotNull();
        assertThat(profile.getEmail()).isEqualTo(up.getEmail());
        assertThat(profile.getFirstName()).isEqualTo(up.getFirstName());
        assertThat(profile.getLastName()).isEqualTo(up.getLastName());
        assertThat(profile.getRoles().size()).isZero();
        assertThat(profile.getErrorMessage()).isEqualTo(IdamStatusResolver.NO_IDAM_CALL);
        assertThat(profile.getErrorStatusCode()).isEqualTo(" ");
    }

}
