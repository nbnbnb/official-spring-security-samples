/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Minimal method security configuration.
 *
 * @author Rob Winch
 * @since 5.0
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

	@Bean
	SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
		
		http
			// Demonstrate that method security works
			// Best practice to use both for defense in depth
			.authorizeExchange((authorize) -> authorize
				.anyExchange().permitAll()
			)
			.httpBasic(withDefaults());
		
		return http.build();
	}

	@Bean
	MapReactiveUserDetailsService userDetailsService() {
		
		UserDetails user = User.withDefaultPasswordEncoder()
			.username("user")
			.password("password")
			.roles("USER")
			.build();
		UserDetails admin = User.withDefaultPasswordEncoder()
			.username("admin")
			.password("password")
			.roles("ADMIN", "USER")
			.build();
		
		return new MapReactiveUserDetailsService(user, admin);
	}

}
