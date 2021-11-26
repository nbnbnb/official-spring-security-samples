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

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * @author Rob Winch
 * @since 5.0
 */
@SpringBootTest
public class MessageServiceTests {

	@Autowired
	MessageService messages;

	// -- findMessage ---

	@Test
	void findMessageWhenNotAuthenticatedThenDenied() {
		
		StepVerifier.create(this.messages.findMessage())
				.expectError(AccessDeniedException.class)
				.verify();
		
	}

	@Test
	@WithMockUser
	void findMessageWhenUserThenDenied() {
		
		StepVerifier.create(this.messages.findMessage())
				.expectNext("Hello User!")
				.verifyComplete();
		
	}

	// -- findSecretMessage ---

	@Test
	void findSecretMessageWhenNotAuthenticatedThenDenied() {
		
		StepVerifier.create(this.messages.findSecretMessage())
				.expectError(AccessDeniedException.class)
				.verify();
		
	}

	@Test
	@WithMockUser
	void findSecretMessageWhenNotAuthorizedThenDenied() {
		
		StepVerifier.create(this.messages.findSecretMessage())
				.expectError(AccessDeniedException.class)
				.verify();
		
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void findSecretMessageWhenAuthorizedThenSuccess() {
		
		StepVerifier.create(this.messages.findSecretMessage())
				.expectNext("Hello Admin!")
				.verifyComplete();
		
	}

}
