package com.lastmile.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

import org.springframework.mock.web.MockHttpSession;

import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class LoginTest {

    @Autowired MockMvc mvc;
    @Autowired TestData data;

    UserAccount dispatcher;

    @BeforeEach
    void setUp() {
        data.reset();
        dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
    }

    @Test
    void validCredentialsAuthenticateWithRoleAndEmailIsCaseInsensitive() throws Exception {
        mvc.perform(formLogin("/login").user("  DINA.Dispatcher@lastmile.test").password(TestData.PASSWORD))
                .andExpect(authenticated().withRoles("DISPATCHER"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void wrongPasswordUnknownUserAndInactiveAccountGetTheSameGenericFailure() throws Exception {
        UserAccount inactive = data.inactiveUser("Ina Inactive", Role.DISPATCHER);

        mvc.perform(formLogin("/login").user(dispatcher.getEmail()).password("wrong-password"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
        mvc.perform(formLogin("/login").user("nobody@lastmile.test").password(TestData.PASSWORD))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
        mvc.perform(formLogin("/login").user(inactive.getEmail()).password(TestData.PASSWORD))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void afterLoginTheUserReturnsToThePageTheyOriginallyRequested() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/deliveries/new").session(session))
                .andExpect(redirectedUrl("/login"));

        mvc.perform(formLogin("/login").user(dispatcher.getEmail()).password(TestData.PASSWORD))
                .andExpect(redirectedUrl("/"));
        mvc.perform(post("/login").session(session).with(csrf())
                        .param("username", dispatcher.getEmail()).param("password", TestData.PASSWORD))
                .andExpect(authenticated())
                .andExpect(redirectedUrlPattern("**/deliveries/new?continue"));
    }

    @Test
    void logoutEndsTheSession() throws Exception {
        mvc.perform(logout("/logout")).andExpect(unauthenticated()).andExpect(redirectedUrl("/login?logout"));
    }
}
