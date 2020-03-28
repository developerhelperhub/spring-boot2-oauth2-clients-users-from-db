# Spring Boot 2.2.5 Oauth2 Authorization Server and Resource Server

This repository contains the Oauth2 authorization server and resource server implementation with JWT token store. This example is continuation of the [Oauth2 Autherization Server and Client Application](https://github.com/developerhelperhub/spring-boot2-oauth2-server-grant-password-refresh-token/) example. I would suggest, please look previous implementation before looking this source code. In the previous example, I used same authentication server as resource. In this example we have separate resource server. Whenever we have authentication server and resoruces server are different, we need to use central token management. Currently I am using the JWT token store and pervious example we used in memory token store. sprint boot provides default in memory token store.

This repository contains four maven project. 
* my-cloud-service: Its main module, it contains the dependecy management of our application.
* identity-service: This authentication server service. 
* client-application-service: This client application for authentication server.
* resource-service: This resource server to provide the resource services for our application.

### Updation and additions in the identity-service
We need to add maven dependency to manage and support the JWT token service in the ```pom.xml```. Spring boot provides the below dependency to support it.

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-jwt</artifactId>
    <version>1.1.0.RELEASE</version>
</dependency>
```

We need to add the additional code in the ```AuthorizationServerConfig``` to configure and manage the JWT token management. The below code are implemented.

* Need to create the ```JwtAccessTokenConverter``` bean to convert the JWT token. In this example, we are using sign key method to convert JWT token for authorization server and resource server.

```java
	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setSigningKey("123456");
		return converter;
	}
```

* Need to create the TokenStore bean to specify which type of toke store, here we need to specify the ```JwtTokenStore```

```java
	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}
```

* Need to add the token store service in the endpoint configuration of ```AuthorizationServerEndpointsConfigurer```.

```java
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
		endpoints.authenticationManager(authenticationManager).userDetailsService(userDetailsService)
				.accessTokenConverter(accessTokenConverter());
	}
```

We need to make sure to add ```@EnableResourceServer``` annotation in the spring boot ```IdentityServiceApplication``` main class to access the resource services from the authroization server.

Above all changes added in the respective classes, we can run the spring boot application, this application run on 8081 and the context path will ```/auth```. We can use this url ```http://localhost:8081/auth/login``` to check, whether it is working or not.

**Note:** I got an error while implementing the JWT token in the authroization server. ```Cannot convert access token to JSON``` this error we got because of I missed to ```@Bean``` in the ```accessTokenConverter``` method.

### Create the resource-service 
We need to create the new spring boot application with ```security oauth2``` and ```security jwt``` dependencies. We need to add the below properties. 

```properties
logging.level.org.springframework=DEBUG

server.port=8083
server.servlet.context-path=/resources
```

This application is running on ```8083``` port and context path will be ```/resources```. 

We need to create the spring boot ```ResourceServiceApplication```main class:
```java
package com.developerhelperhub.ms.id;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ResourceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourceServiceApplication.class, args);
	}

}
```

We need to create new ```ResourceServerConfig``` class to configure the resource server configurations. In this resource configuration, we are adding the configuration explains below:

* @Override the ```ResourceServerSecurityConfigurer``` configuration method to configure the JWT token service and other resource configurations.
* @Override the ```HttpSecurity``` configuration to configure the endpoint which are the endpoints can be exposed.
* We need to add same token store and access token converter in the authroization server. Please make sure the sign key must be same.
* Need to create the token store service to configure the JWT token store. This token service, we are using to configure in the resource configuration.


```java
package com.developerhelperhub.ms.id.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	private static final String RESOURCE_ID = "resource_id";

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.resourceId(RESOURCE_ID).stateless(false).tokenServices(tokenServices());
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.anonymous().disable().authorizeRequests().antMatchers("/users/**").access("hasRole('ADMIN')").and()
				.exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
	}

	@Bean
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices tokenServices = new DefaultTokenServices();
		tokenServices.setTokenStore(tokenStore());
		return tokenServices;
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setSigningKey("123456");
		return converter;
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

}

```

We need to create ```UserController``` class to add the resource endpoints in the resource service.

```java
package com.developerhelperhub.ms.id.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public String listUser() {
		return "success";
	}

	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public String create() {
		return "success";
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
	public String delete(@PathVariable(value = "id") Long id) {
		return "success";
	}
}
```

Above all classes creation of resource server, we can run the spring boot application, this application run on 8083. We can use this url ```http://localhost:8083/resources/user``` to check, whether it is accessing or not. Now, the API return the 401 unauthorized error. We need to generate the token from authroization server first and then we need to use that access token to access the this API.


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
* [Authorization and Resource Server](https://www.devglan.com/spring-security/spring-boot-oauth2-jwt-example)
* [Oauth2 Autherization Server and Client Application](https://github.com/developerhelperhub/spring-boot2-oauth2-server-grant-password-refresh-token)
