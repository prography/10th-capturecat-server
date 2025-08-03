package com.capturecat.core.domain.user;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.capturecat.core.domain.BaseTimeEntity;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String username;

	@Column(length = 70) //패스워드 인코딩(BCrypt)
	private String password;

	@Column(nullable = false, length = 50)
	private String email;

	@Column(nullable = false, length = 50)
	private String nickname;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserRole role;

	private boolean tutorialCompleted = false;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserSocialAccount> socialAccounts = new ArrayList<>();

	@Builder
	public User(Long id, String username, String password, String email, String nickname, UserRole role) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.email = email;
		this.nickname = nickname;
		this.role = role;
	}

	public void tutorialComplete() {
		this.tutorialCompleted = true;
	}
}
