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

import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;

// 这是一个自定义的 Authentication
// 第一次登录提交的 Authentication 保存在 first 中
public class MfaAuthentication extends AbstractAuthenticationToken {

    private final Authentication first;

    public MfaAuthentication(Authentication first) {
        super(Collections.emptyList());
        this.first = first;
        System.out.println("MfaAuthentication -> ctor");
    }

    @Override
    public Object getPrincipal() {
        return this.first.getPrincipal();
    }

    @Override
    public Object getCredentials() {
        return this.first.getCredentials();
    }

    @Override
    public void eraseCredentials() {
        System.out.println("MfaAuthentication -> eraseCredentials");
        if (this.first instanceof CredentialsContainer) {
            ((CredentialsContainer) this.first).eraseCredentials();
        }
    }

    @Override
    public boolean isAuthenticated() {
        System.out.println("MfaAuthentication -> isAuthenticated");
        // 自定义的 Authentication 总是返回失败
        return false;
    }

    public Authentication getFirst() {
        System.out.println("MfaAuthentication -> getFirst");
        return this.first;
    }

}
