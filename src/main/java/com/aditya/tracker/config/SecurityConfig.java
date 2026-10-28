package com.aditya.tracker.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import com.aditya.tracker.entity.User;
import com.aditya.tracker.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            UserRepository userRepository) {

        return email -> {
            User user = userRepository.findByEmail(
                    email.trim().toLowerCase())
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Invalid email or password"));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(
                    new HttpSessionCsrfTokenRepository()
                )
            )

            .authorizeHttpRequests(auth -> auth
            	    .requestMatchers(
            	        "/",
            	        "/index.html",
            	        "/login.html",
            	        "/signup.html",
            	        "/auth.js",
            	        "/style.css",
            	        "/script.js",
            	        "/favicon.ico",
            	        "/api/auth/csrf",
            	        "/api/auth/signup",
            	        "/api/auth/login"
            	    ).permitAll()
            	    .anyRequest().authenticated()
            	)

            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"message\":\"Login successful\"}"
                    );
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                    );
                    response.setContentType(
                        MediaType.APPLICATION_JSON_VALUE
                    );
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"error\":\"Invalid email or password\"}"
                    );
                })
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"message\":\"Logout successful\"}"
                    );
                })
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                    );
                    response.setContentType(
                        MediaType.APPLICATION_JSON_VALUE
                    );
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"error\":\"Please log in first\"}"
                    );
                })
            );

        return http.build();
    }
   
}