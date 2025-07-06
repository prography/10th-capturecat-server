package com.capturecat.core.service.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRole;

@Getter
@RequiredArgsConstructor
public class LoginUser implements UserDetails {

	private String username;
	private String password;
	private UserRole role;

	public LoginUser(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.role = user.getRole();
	}

	public LoginUser(String username, String role) {
		this.username = username;
		this.role = UserRole.fromRoleName(role);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add((GrantedAuthority)() -> "ROLE_" + role);
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}
}
