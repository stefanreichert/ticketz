package net.wickedshell.ticketz.adapter.rest.security.jwt;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@EqualsAndHashCode(callSuper = false)
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    @Getter
    private final String jwt;
    private final String credentials;
    private final String principal;

    public JwtAuthenticationToken(String jwt) {
        super(null);
        this.jwt = jwt;
        principal = null;
        credentials = null;
    }

    public JwtAuthenticationToken(String jwt, String principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.jwt = jwt;
        this.credentials = credentials;
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

}
