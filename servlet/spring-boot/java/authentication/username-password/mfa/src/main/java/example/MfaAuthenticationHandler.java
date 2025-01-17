/*
 * Copyright 2021 the original author or authors.
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

/**
 * An authentication handler that saves an authentication either way.
 * <p>
 * The reason for this is so that the rest of the factors are collected, even if earlier
 * factors failed.
 *
 * @author Josh Cummings
 */
public class MfaAuthenticationHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private final AuthenticationSuccessHandler successHandler;

    public MfaAuthenticationHandler(String url) {
        // url 在验证成功后执行跳转
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler(url);
        successHandler.setAlwaysUseDefaultTargetUrl(true);
        this.successHandler = successHandler;
        System.out.println("MfaAuthenticationHandler -> " + url);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        System.out.println("MfaAuthenticationHandler -> onAuthenticationFailure");
        Authentication anonymous = new AnonymousAuthenticationToken("key", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        // 如果前一次登录失败了
        // 将匿名的凭据存储到上下文中
        // 下个页面提交时，将会读取到此匿名上下文，就不会执行提交操作
        saveMfaAuthentication(request, response, new MfaAuthentication(anonymous));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        System.out.println("MfaAuthenticationHandler -> onAuthenticationSuccess");
        // 登录成功，将凭据保存，继续传递到下一个页面
        saveMfaAuthentication(request, response, authentication);
    }

    private void saveMfaAuthentication(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {

        System.out.println("MfaAuthenticationHandler -> saveMfaAuthentication");
        // 将凭据信息，保存到上下文中
        SecurityContextHolder.getContext().setAuthentication(new MfaAuthentication(authentication));
        // 此时，会执行跳转（因为 successHandler 是一个 SimpleUrlAuthenticationSuccessHandler）
        this.successHandler.onAuthenticationSuccess(request, response, authentication);
    }

}
