package com.zsecurity.demo.filter;

import com.zsecurity.demo.entity.Users;
import com.zsecurity.demo.exceptions.UserNotYetLoggedInException;
import com.zsecurity.demo.repositories.UserRepo;
import com.zsecurity.demo.services.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    JwtService jwtService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.equals("/login") || path.equals("/reg") || path.startsWith("/forget-password") || path.startsWith("/reset-password") || path.startsWith("/change-password") || path.equals("/upload") || path.equals("/refresh-token")
                || path.startsWith("/products") || path.startsWith("/categories") || path.equals("/admin/login") || path.startsWith("/analytics")
                || path.startsWith("/deals") || path.startsWith("/festival") || path.equals("/current-festival") || path.startsWith("/api/home")
                || path.equals("/verify-temptoken") || path.equals("/error") || path.startsWith("/oauth2") || path.startsWith("/login/oauth2") || path.startsWith("/all-offers")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (request.getCookies() == null) {
                throw new UserNotYetLoggedInException("User not loggedIn..");
            }

            Cookie token1 = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("jwtToken"))
                    .findFirst()
                    .orElseThrow(() -> new UserNotYetLoggedInException("User not loggedIn"));

            String rawToken = token1.getValue();
            boolean expired = jwtService.isExpired(rawToken);

            if (expired) {
                Cookie expiredCookie = new Cookie("jwtToken", null);
                expiredCookie.setHttpOnly(true);
                expiredCookie.setSecure(false);
                expiredCookie.setPath("/");
                expiredCookie.setMaxAge(0);
                response.addCookie(expiredCookie);
                throw new JwtException("JWT token is expired");
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtService.getEmailByToken(rawToken);
                Users users = userRepo.findByEmail(email).orElse(null);
                UsernamePasswordAuthenticationToken userToken =
                        new UsernamePasswordAuthenticationToken(users, null, users.getAuthorities());
                userToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(userToken);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}