package com.developerhelperhub.ms.id.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Component;

import com.developerhelperhub.ms.id.entity.OauthClientEntity;
import com.developerhelperhub.ms.id.repository.OauthClientRepository;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OauthClient implements ClientDetails, ClientDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OauthClient.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -7265763015228159319L;

	@Id
	private String clientId;

	private Set<String> resourceIds;

	private boolean secretRequired;

	private String clientSecret;

	private boolean scoped;

	private Set<String> scope;

	private Set<String> authorizedGrantTypes;

	private Set<String> registeredRedirectUri;

	private Integer accessTokenValiditySeconds;

	private Integer refreshTokenValiditySeconds;

	private boolean autoApprove;

	private Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

	private Map<String, Object> additionalInformation = new HashMap<String, Object>();

	@Autowired
	private OauthClientRepository clientRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public OauthClient() {
	}

	public OauthClient(OauthClientEntity entity) {

		this.clientId = entity.getClientId();

		this.resourceIds = entity.getResourceIds();

		this.secretRequired = entity.isSecretRequired();

		this.clientSecret = entity.getClientSecret();

		this.scoped = entity.isScoped();

		this.scope = entity.getScope();

		this.authorizedGrantTypes = entity.getAuthorizedGrantTypes();

		this.registeredRedirectUri = entity.getRegisteredRedirectUri();

		this.accessTokenValiditySeconds = entity.getAccessTokenValiditySeconds();

		this.refreshTokenValiditySeconds = entity.getRefreshTokenValiditySeconds();

		this.autoApprove = entity.isAutoApprove();

	}

	public void create() {

		OauthClientEntity entity = new OauthClientEntity();

		entity.setClientId(this.clientId);

		entity.setResourceIds(this.resourceIds);

		entity.setSecretRequired(this.secretRequired);

		entity.setClientSecret(passwordEncoder.encode(this.clientSecret));

		entity.setScoped(this.scoped);

		entity.setScope(this.scope);

		entity.setAuthorizedGrantTypes(this.authorizedGrantTypes);

		entity.setRegisteredRedirectUri(this.registeredRedirectUri);

		entity.setAccessTokenValiditySeconds(this.accessTokenValiditySeconds);

		entity.setRefreshTokenValiditySeconds(this.refreshTokenValiditySeconds);

		entity.setAutoApprove(this.autoApprove);

		clientRepository.save(entity);

		LOGGER.debug("{} client created!", this.clientId);
	}

	public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {

		Optional<OauthClientEntity> entity = clientRepository.findById(clientId);

		if (!entity.isPresent()) {
			throw new ClientRegistrationException("Client does not found!");
		}

		return new OauthClient(entity.get());
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Set<String> getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(Set<String> resourceIds) {
		this.resourceIds = resourceIds;
	}

	public boolean isSecretRequired() {
		return secretRequired;
	}

	public void setSecretRequired(boolean secretRequired) {
		this.secretRequired = secretRequired;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public boolean isScoped() {
		return scoped;
	}

	public void setScoped(boolean scoped) {
		this.scoped = scoped;
	}

	public Set<String> getScope() {
		return scope;
	}

	public void setScope(Set<String> scope) {
		this.scope = scope;
	}

	public Set<String> getAuthorizedGrantTypes() {
		return authorizedGrantTypes;
	}

	public void setAuthorizedGrantTypes(Set<String> authorizedGrantTypes) {
		this.authorizedGrantTypes = authorizedGrantTypes;
	}

	public Set<String> getRegisteredRedirectUri() {
		return registeredRedirectUri;
	}

	public void setRegisteredRedirectUri(Set<String> registeredRedirectUri) {
		this.registeredRedirectUri = registeredRedirectUri;
	}

	public Integer getAccessTokenValiditySeconds() {
		return accessTokenValiditySeconds;
	}

	public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
		this.accessTokenValiditySeconds = accessTokenValiditySeconds;
	}

	public Integer getRefreshTokenValiditySeconds() {
		return refreshTokenValiditySeconds;
	}

	public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
		this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
	}

	public boolean isAutoApprove() {
		return autoApprove;
	}

	public void setAutoApprove(boolean autoApprove) {
		this.autoApprove = autoApprove;
	}

	public boolean isAutoApprove(String scope) {
		return autoApprove;
	}

	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	public Map<String, Object> getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(Map<String, Object> additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	public OauthClientRepository getClientRepository() {
		return clientRepository;
	}

	public void setClientRepository(OauthClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

}
