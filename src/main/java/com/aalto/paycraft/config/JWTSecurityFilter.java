package com.aalto.paycraft.config;

import com.aalto.paycraft.entity.AuthToken;
import com.aalto.paycraft.repository.AuthTokenRepository;
import com.aalto.paycraft.services.EmployerDetailService;
import com.aalto.paycraft.services.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JWTSecurityFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final EmployerDetailService userDetailService;
    private final AuthTokenRepository tokenRepository;
    private final HandlerExceptionResolver handlerExceptionResolver;

    /**
     * Methods that overrides the Default Username and Password Configuration to use JWT
     *
     * @param filterChain Filter chain of the Request
     * @param request The HttpServletRequest sent by the user from the client
     * @param response The HttpServletResponse sent by the CLIENT (Contains the Authentication Header)
     * */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) {
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;


        try {
            if(authHeader == null || authHeader.isBlank()){
                filterChain.doFilter(request,response);
                return;
            }
            // Extracts the JWT AuthToken from the Authorization header.
            jwtToken = authHeader.substring(7);

            // Extracts the Username to confirm he os she exists on the application
            userEmail = jwtService.extractUsername(jwtToken);

            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = userDetailService.loadUserByUsername(userEmail);

                /*Function to  Validate the AuthToken -> Checks if the authToken has expired or has been revoked  */
                Function<AuthToken, Boolean> validateToken = t -> !t.getExpired().equals(true) && !t.getRevoked().equals(true);

                /* Looks for the authToken on the DB, calls function to validate, if it fails, return false */
                var isTokenValid = tokenRepository.findByAccessToken(jwtToken).map(
                        validateToken
                ).orElse(false);

                /* If authToken is valid, Sets the SecurityContext to hold UserDetails and Authorities */
                if(jwtService.isTokenValid(jwtToken, userDetails) && isTokenValid){
                    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    securityContext.setAuthentication(token);
                    SecurityContextHolder.setContext(securityContext);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}