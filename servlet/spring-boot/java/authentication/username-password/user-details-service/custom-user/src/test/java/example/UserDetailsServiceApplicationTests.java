/*
 * Copyright 2012-2016 the original author or authors.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Rob Winch
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserDetailsServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userWhenNotAuthenticated() throws Exception {

        this.mockMvc
                .perform(get("/user"))
                .andExpect(status().isUnauthorized());

    }

    /**
     * WithUserDetails looks up the user from the UserDetailsService. The advantage is
     * this is easy to use. The disadvantage, is that the user must exist so it relies our
     * our data being set up properly. Alternatively, consider using a custom annotation
     * like {@link #userWhenWithMockCustomUserThenOk()}.
     */
    @Test
    // Spring API
    // 模拟登录用户
    @WithUserDetails("user@example.com")
    void userWhenWithUserDetailsThenOk() throws Exception {

        this.mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)));

    }

    /**
     * WithUser is annotated with WithUserDetails to create a concrete persona for our
     * testing. It is a little extra code, but makes it less error prone.
     */
    @Test
    // 自定义 API
    // 包转 @WithUserDetails("user@example.com")
    @WithUser
    void userWhenWithUserThenOk() throws Exception {

        this.mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)));

    }

    @Test
    // 自定义 API
    // 使用 WithMockCustomUserSecurityContextFactory 进行模拟用户
    @WithMockCustomUser
    void userWhenWithMockCustomUserThenOkByAnonymous() throws Exception {

        this.mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", equalTo("anonymous@example.com")));

    }


    /**
     * WithMockCustomUser is a little more code then using {@link WithUserDetails}, but we
     * don't need to ensure that the
     * {@link org.springframework.security.core.userdetails.UserDetails} is defined. The
     * {@link CustomUser} with email "admin@example.com" is not setup, but we can still
     * use it for testing here.
     */
    @Test
    // 自定义 API
    // 使用 WithMockCustomUserSecurityContextFactory 进行模拟用户
    @WithMockCustomUser(email = "user@example.com")
    void userWhenWithMockCustomUserThenOk() throws Exception {

        this.mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", equalTo("user@example.com")));

    }

    /**
     * {@link WithMockCustomAdmin} is annotated with {@link WithMockCustomUser} to create
     * a concrete persona for our testing. This is a little extra code, but it is less
     * error prone.
     */
    @Test
    // 自定义 API
    // 包装 @WithMockCustomUser 注解
    @WithMockCustomAdmin
    void userWhenWithMockCustomAdminThenOk() throws Exception {

        this.mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", equalTo("admin@example.com")));

    }

}
