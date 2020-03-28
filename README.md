# Spring Boot 2.2.5 Oauth2 Authorization Server and Resource Server

This repository contains the Oauth2 authorization server and resource server implementation. This example is continuation of the [Oauth2 Autherization Server and Client Application](https://github.com/developerhelperhub/spring-boot2-authorization-and-resource-servers/) example. I would suggest, please look previous implementation before looking this source code. In the previous example, I used same authentication server as resource, but users and clients information are stored in memory. In this example, I am using to load users and clients from database. I am using mongoDB database to store information in application. 

This repository contains four maven project. 
* my-cloud-service: Its main module, it contains the dependecy management of our application.
* identity-service: This authentication server service. 
* client-application-service: This client application for authentication server.
* resource-service: This resource server to provide the resource services for our application.

### Updation and additions in the identity-service
We need to add maven dependency to manage and support the entity management on mongoDB. Spring boot provides the below dependency to support it.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```
Note: When I added the mongoDB dependency in this project, I got an error ```Caused by: java.lang.ClassNotFoundException: org.springframework.data.mongodb.core.convert.MongoCustomConversions``` because I am using older ```org.springframework.data:spring-data-releasetrain:Fowler-SR2``` release train dependency in ```my-cloud-service``` parent project and I changed to ```org.springframework.data:spring-data-releasetrain:Moore-SR6``` latest version.

### Create the services for managing the users.

We need to create the ```UserEntity``` entity class for user. This entity class is used to store the users and the collection name ```users``` in the databae.

```java
package com.developerhelperhub.ms.id.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public class UserEntity {

	@Id
	private String username;

	private String password;

	private boolean accountNonExpired;

	private boolean accountNonLocked;

	private boolean credentialsNonExpired;

	private boolean enabled;

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

}
```

We need to create the mongo db repository ```UserRepository``` for users to manage the CRUD operations on mongo.

```java
package com.developerhelperhub.ms.id.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.developerhelperhub.ms.id.entity.UserEntity;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String>{

}
```

We need to create the ```User``` service manage load the users from DB and create the users on DB. The below functionalities are added

* The User service scpoe is ```prototype``` because spring boot default scope is ```Singletone```. In this usecase user service is required to create new object whenever required in our application. 
* Implements the ```UserDetails``` interface is used to manage the information of user authentication.
* Implements the ```UserDetailsService``` interface is used to load the users from db based on username. This service is using to configure the authentication manager.
- @Override ```loadUserByUsername``` method and added the logic to load the user entity based on username.
* ```create``` method is used to store the user information into the db

```java
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
```

### Create the services for managing the clients.

We need to create the ```OauthClientEntity``` entity class for clients. This entity class is used to store the clients and the collection name ```oauth_clients``` in the databae.

```java
package com.developerhelperhub.ms.id.entity;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("oauth_clients")
public class OauthClientEntity {

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

}
```

We need to create the mongo db repository ```OauthClientRepository``` for clients to manage the CRUD operations on mongo.

```java
package com.developerhelperhub.ms.id.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.developerhelperhub.ms.id.entity.OauthClientEntity;

@Repository
public interface OauthClientRepository extends MongoRepository<OauthClientEntity, String> {

}
```

We need to create the ```OauthClient``` service manage load the clients from DB and create the client detail on DB. The below functionalities are added

* The User service scpoe is ```prototype``` because spring boot default scope is ```Singletone```. In this usecase client service is required to create new object whenever required in our application. 
* Implements the ```ClientDetails``` interface is used to manage the information of client authentication.
* Implements the ```ClientDetailsService``` interface is used to load the clients from db based on ```client id```. This service is using to configure the authorization server.
- @Override ```loadClientByClientId``` method and added the logic to load the client entity based on ```client id```.
* ```create``` method is used to store the client information into the db

```java
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
```

We need to change below code in the ```AuthorizationServerConfig``` to configure client service. The below code is implemented.

* Disable the code client details store in memory and configure our client service.
```java
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(oauthClientDetail);

//		clients.inMemory().withClient(clientID).secret(passwordEncoder.encode(clientSecret))
//				.authorizedGrantTypes("authorization_code", "password", "refresh_token").scopes("user_info")
//				.autoApprove(true).redirectUris(redirectURLs).refreshTokenValiditySeconds(83199)
//				.accessTokenValiditySeconds(43199);

	}
```

We need to add below changes in the ```WebSecurity``` class to configure the user service. 

* Disable the code user details store in memory and configure our user service.
```java
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(user);
		// auth.inMemoryAuthentication().withUser(username).password(passwordEncoder().encode(password)).roles("USER");
	}
```
* Removed the code bean creation ```passwordEncoder``` from this class and moved into new class ```BeanConfiguration``` because we had an issue while starting the spring boot application. This issue was cycle bean creation on ```User``` service and ```Web Security``` service because we are autowiring the ```PasswordEncoder``` class to encode the password of the user.


We added logic in the ```IdentityServiceApplication``` main spring boot application to create the users and clients while run the application.
```java
package com.developerhelperhub.ms.id;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import com.developerhelperhub.ms.id.data.OauthClient;
import com.developerhelperhub.ms.id.data.User;

@SpringBootApplication
@EnableResourceServer
public class IdentityServiceApplication implements CommandLineRunner {

	@Autowired
	private User user;

	@Autowired
	private OauthClient client;

	public static void main(String[] args) {
		SpringApplication.run(IdentityServiceApplication.class, args);
	}

	public void run(String... args) throws Exception {
		user.setUsername("mycloud");
		user.setPassword("mycloud@1234");
		user.setAccountNonExpired(true);
		user.setAccountNonLocked(true);
		user.setCredentialsNonExpired(true);
		user.setEnabled(true);

		user.create();

		client.setClientId("my-cloud-identity");
		client.setClientSecret("VkZpzzKa3uMq4vqg");
		client.setResourceIds(new HashSet<String>(Arrays.asList("identity_id", "resource_id")));
		client.setSecretRequired(true);
		client.setScoped(true);
		client.setScope(new HashSet<String>(Arrays.asList("user_info")));
		client.setAuthorizedGrantTypes(
				new HashSet<String>(Arrays.asList("authorization_code", "password", "refresh_token")));
		client.setRegisteredRedirectUri(new HashSet<String>(Arrays.asList("http://localhost:8082/login/oauth2/code/")));
		client.setAccessTokenValiditySeconds(43199);
		client.setRefreshTokenValiditySeconds(83199);
		client.setAutoApprove(true);

		client.create();
	}

}
```

Above all changes added in the respective classes, we can run the spring authentication server and resource server to test.

### To generate the tokens with grant type "password"

Here, I am using Postman to test the grant types. Please open the Postman and open a new tab. We have to add below configuration and data in the tab.
* Method: POST
* URL: http://localhost:8081/auth/oauth/token
* Select the "Autherization" tab and change the type to "Basic Auth". Enter the username and password of client id and client secrete. Click the "Update Request" button
* Select the "Body" tab and select "x-www-form-urlencoded" option
* Add the keys and values in the form
  - grant_type is password
  - username is mycloud
  - password is mycloud@1234
* Click the "Send" button.

The below respone we can see the access and refresh token are generated by the authroization server through the JWT token service. 
```json
{
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODUyMDE3MTIsInVzZXJfbmFtZSI6Im15Y2xvdWQiLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiZWU3NGRmZDUtYjE3Mi00ZTdmLWJhMjUtNWQ1Yzc3NDFhNDU0IiwiY2xpZW50X2lkIjoibXktY2xvdWQtaWRlbnRpdHkiLCJzY29wZSI6WyJ1c2VyX2luZm8iXX0.f8L_jXAGTw-l0wqWSEkSrO9tnJmtDB_9C0ZQffzn1HU",
    "token_type": "bearer",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJteWNsb3VkIiwic2NvcGUiOlsidXNlcl9pbmZvIl0sImF0aSI6ImVlNzRkZmQ1LWIxNzItNGU3Zi1iYTI1LTVkNWM3NzQxYTQ1NCIsImV4cCI6MTU4NTI0MTcxMiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6IjhmNjA5NTE1LWFmOTItNGEzMS1iNDYyLTQ0ZjE2OGI2ZTYxYiIsImNsaWVudF9pZCI6Im15LWNsb3VkLWlkZW50aXR5In0.l4dvchYz6Od5pxx3McSze2MU6y5o2OO6BvbO6aX1bEE",
    "expires_in": 43198,
    "scope": "user_info",
    "jti": "ee74dfd5-b172-4e7f-ba25-5d5c7741a454"
}
```

### To authorize the resource server APIs

Open a new tab. We have to add below configuration and data in the tab.
* Method: GET
* URL: http://localhost:8083/resources/user
* In the "Header" tab we need to add two keys and values
 - One is "Autherization" header. Its values we have to provide access token with "bearer" key word like ```bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODUyMDE3MTIsInVzZXJfbmFtZSI6Im15Y2xvdWQiLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiZWU3NGRmZDUtYjE3Mi00ZTdmLWJhMjUtNWQ1Yzc3NDFhNDU0IiwiY2xpZW50X2lkIjoibXktY2xvdWQtaWRlbnRpdHkiLCJzY29wZSI6WyJ1c2VyX2luZm8iXX0.f8L_jXAGTw-l0wqWSEkSrO9tnJmtDB_9C0ZQffzn1HU```
* Another one is "Content-Type" and its value is "application/json"
* Click the "Send" button.
 
The API give the response contains
```json
success
```

### Reference
* [Oauth2 Autherization and Resource Servers](https://github.com/developerhelperhub/spring-boot2-authorization-and-resource-servers/)
* [Spring Boot 2 MongoDB Example](https://www.journaldev.com/18156/spring-boot-mongodb)
* [Client Details Java Doc](https://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/provider/ClientDetails.html)
* [User Details Java Doc](https://docs.spring.io/spring-security/site/docs/3.0.x/apidocs/org/springframework/security/core/userdetails/UserDetails.html)
* [Install MongoDB on Mac](https://docs.mongodb.com/manual/tutorial/install-mongodb-on-os-x/)
