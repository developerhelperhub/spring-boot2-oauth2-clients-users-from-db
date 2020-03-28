package com.developerhelperhub.ms.id.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.developerhelperhub.ms.id.entity.UserEntity;
import com.developerhelperhub.ms.id.repository.UserRepository;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class User implements UserDetails, UserDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(User.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 6072929707082314818L;

	private String username;

	private String password;

	private boolean accountNonExpired;

	private boolean accountNonLocked;

	private boolean credentialsNonExpired;

	private boolean enabled;

	private Collection<? extends GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public User() {
	}

	public User(UserEntity user) {
		this.username = user.getUsername();

		this.password = user.getPassword();

		this.accountNonExpired = user.isAccountNonExpired();

		this.accountNonLocked = user.isAccountNonLocked();

		this.credentialsNonExpired = user.isCredentialsNonExpired();

		this.enabled = user.isEnabled();

	}

	public void create() {
		UserEntity entity = new UserEntity();

		entity.setUsername(this.username);

		entity.setPassword(passwordEncoder.encode(this.password));

		entity.setAccountNonExpired(this.accountNonExpired);

		entity.setAccountNonLocked(this.accountNonLocked);

		entity.setCredentialsNonExpired(this.credentialsNonExpired);

		entity.setEnabled(this.enabled);

		userRepository.save(entity);

		LOGGER.debug("{} user created!", this.username);
	}

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<UserEntity> entity = userRepository.findById(username);

		if (!entity.isPresent()) {
			throw new UsernameNotFoundException("Username does not found");
		}

		return new User(entity.get());
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
	}
}
