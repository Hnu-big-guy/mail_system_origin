// SecurityConfig.java
package com.hnu.mail.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.hnu.mail.security.JwtAuthenticationEntryPoint;
import com.hnu.mail.security.JwtAuthenticationFilter;
import com.hnu.mail.security.ResponseLoggingFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationEntryPoint unauthorizedHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, ResponseLoggingFilter responseLoggingFilter) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
            .requestMatchers("/ws/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(responseLoggingFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
  // SecurityConfig.java 的 CORS 部分修改
  @Bean
  public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
    CorsConfiguration configuration = new CorsConfiguration();

    if (corsProperties.getAllowedOrigins() != null) {
      configuration.setAllowedOrigins(Arrays.asList(corsProperties.getAllowedOrigins()));
    }

    if (corsProperties.getAllowedMethods() != null) {
      configuration.setAllowedMethods(Arrays.asList(corsProperties.getAllowedMethods()));
    }

    if (corsProperties.getAllowedHeaders() != null) {
      configuration.setAllowedHeaders(Arrays.asList(corsProperties.getAllowedHeaders()));
    }

    configuration.setAllowCredentials(corsProperties.getAllowCredentials());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}