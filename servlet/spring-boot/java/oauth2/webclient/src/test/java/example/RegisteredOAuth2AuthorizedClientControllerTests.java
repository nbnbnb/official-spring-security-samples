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

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({SecurityConfiguration.class, RegisteredOAuth2AuthorizedClientController.class})
@AutoConfigureMockMvc
public class RegisteredOAuth2AuthorizedClientControllerTests {

    private static final MockWebServer web = new MockWebServer();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    @AfterAll
    static void shutdown() throws Exception {
        web.shutdown();
    }

    @Test
    void annotationExplicitWhenAuthenticatedThenUsesClientIdRegistration() throws Exception {
        web.enqueue(new MockResponse().setBody("body").setResponseCode(200));

        this.mockMvc.perform(get("/annotation/explicit")
                        .with(oauth2Login())
                        .with(oauth2Client("client-id")))
                .andExpect(status().isOk());

    }

    @Test
    void annotationImplicitWhenAuthenticatedThenUsesDefaultRegistration() throws Exception {
        web.enqueue(new MockResponse().setBody("body").setResponseCode(200));

        this.mockMvc.perform(get("/annotation/implicit")
                        .with(oauth2Login()))
                .andExpect(status().isOk());

    }

    @Test
    void publicAnnotationExplicitWhenAuthenticatedThenUsesClientIdRegistration() throws Exception {
        web.enqueue(new MockResponse().setBody("body").setResponseCode(200));

        this.mockMvc.perform(get("/public/annotation/explicit")
                        .with(oauth2Client("client-id")))
                .andExpect(status().isOk());

    }

    @Test
    void publicAnnotationImplicitWhenAuthenticatedThenUsesDefaultRegistration() throws Exception {
        web.enqueue(new MockResponse().setBody("body").setResponseCode(200));

        this.mockMvc.perform(get("/public/annotation/implicit")
                        .with(oauth2Login()))
                .andExpect(status().isOk());

    }

    @Configuration
    static class WebClientConfig {

        @Bean
        WebClient web() {
            return WebClient.create(web.url("/").toString());
        }

        @Bean
        OAuth2AuthorizedClientRepository authorizedClientRepository() {
            return new HttpSessionOAuth2AuthorizedClientRepository();
        }

    }

}
