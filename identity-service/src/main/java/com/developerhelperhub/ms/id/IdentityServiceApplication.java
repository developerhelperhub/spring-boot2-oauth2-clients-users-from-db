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
