package com.aalto.paycraft.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final SecurityAuthProvider authProvider;
    private final JWTSecurityFilter jwtSecurityFilter;

    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * Processes incoming requests in the web application.
     * Handles authentication, authorization, and protection against exploits.
     * @param http HttpSecurity configures security for HTTP requests.
     * */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf(AbstractHttpConfigurer::disable) // Disable CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "api/v1/auth/**",
                                "webhook",
                                "api/v1/employer/create",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "api/v1/ussd",
                                "api/v1/company/create",
                                "/",
                                "/error**").permitAll() // Permits all Users to access Authentication Endpoints
                        .anyRequest().authenticated()) // Every other Request has to be authenticated.
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                /*
                    Sets the Authentication Provider to use the Custom Auth Provider Defined
                    Uses the JWT Filter Defined in the Config.
                */
                .authenticationProvider(authProvider.authenticationProvider()).addFilterBefore(
                        jwtSecurityFilter, UsernamePasswordAuthenticationFilter.class
                        // Handles the Logout Mechanism of the Applications
                );
        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("https://paycraft-dev.netlify.app");
        configuration.addAllowedOrigin(frontendUrl);

        log.info("====== frontendURl (security config) =====: {}", frontendUrl);

        // Restrict to specific HTTP methods only
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("PATCH");
        configuration.addAllowedMethod("OPTIONS");
        
        // Restrict to specific headers only
        configuration.addAllowedHeader("Authorization");
        configuration.addAllowedHeader("Content-Type");
        configuration.addAllowedHeader("X-Requested-With");
        configuration.addAllowedHeader("Accept");
        configuration.addAllowedHeader("Origin");
        configuration.addAllowedHeader("Access-Control-Request-Method");
        configuration.addAllowedHeader("Access-Control-Request-Headers");
        
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}