package uk.gov.hmcts.reform.userprofileapi.config;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
public class AccessDecisionManagerAuthorizationManagerAdapter implements AuthorizationManager<Authentication> {

    //TODO improve this code before merge
    /*
        authentication - the Supplier of the Authentication to check
        object - the AuthorizationManager object to check
        Throws: AccessDeniedException - if access is not granted
    */
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, Authentication object) {
        List<GrantedAuthority> userAuthorities = authentication.get().getAuthorities()
                .stream()
                .map(authority -> (GrantedAuthority) authority)
                .toList();

        List<GrantedAuthority> requiredAuthorities = object.getAuthorities()
                .stream()
                .map(authority -> (GrantedAuthority) authority)
                .toList();

        return new AuthorizationDecision(userAuthorities.containsAll(requiredAuthorities));
    }

}
