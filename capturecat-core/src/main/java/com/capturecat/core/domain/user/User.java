package com.capturecat.core.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.capturecat.core.domain.BaseTimeEntity;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true, nullable = false, length = 30)
	private String username; //nickname

	@Column(nullable = false, length = 70) //패스워드 인코딩(BCrypt)
	private String password;

	@Column(nullable = false, length = 30)
	private String email;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserRole role;

	@Builder
	public User(Long id, String username, String password, String email, UserRole role) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.email = email;
		this.role = role;
	}
}
