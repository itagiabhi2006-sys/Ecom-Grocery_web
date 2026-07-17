package com.zsecurity.demo.handler;

import com.zsecurity.demo.entity.Users;
import com.zsecurity.demo.enums.Roles;
import com.zsecurity.demo.repositories.UserRepo;
import com.zsecurity.demo.services.EmailService;
import com.zsecurity.demo.services.JwtService;
import com.zsecurity.demo.services.TempTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepo repo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TempTokenService tempTokenService;

    @Autowired
    EmailService emailService;

    @Value("${frontend.success.url}")
    private String frontendSuccessUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        String provider = authToken.getAuthorizedClientRegistrationId();

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        Users user = repo.findByEmail(email).orElseGet(() -> {
            Users newUser = null;
            if (provider.equals("google")) {
                newUser = Users.builder()
                        .email(email)
                        .roles(Roles.USER)
                        .firstName(oAuth2User.getAttribute("given_name"))
                        .lastName(oAuth2User.getAttribute("family_name"))
                        .imageLink(oAuth2User.getAttribute("picture"))
                        .build();
            }

            if (provider.equals("github")) {
                newUser = Users.builder()
                        .email(email)
                        .roles(Roles.USER)
                        .firstName(oAuth2User.getAttribute("name"))
                        .lastName(" ")
                        .imageLink(oAuth2User.getAttribute("avatar_url"))
                        .build();
            }

            emailService.emailGenerate(newUser);
            assert newUser != null;
            newUser.setIsActiveNow(true);
            newUser.setIsActive(true);
            return repo.saveAndFlush(newUser);
        });

        if (user.getIsActive()) {
            user.setIsActiveNow(true);
            addCookie(response, "jwtToken", jwtService.generateToken(user), 10 * 60);
            addCookie(response, "refToken", jwtService.generateRefreshToken(user), 2592000);

            String oneTimeToken = tempTokenService.createTempToken(email);

            response.sendRedirect(frontendSuccessUrl + "?token=" + oneTimeToken);
        } else {
            throw new RuntimeException();
        }
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        String cookieHeader = String.format(
                "%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                name, value, maxAgeSeconds
        );
        response.addHeader("Set-Cookie", cookieHeader);
    }
}