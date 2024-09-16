package com.aalto.paycraft.services;

import com.aalto.paycraft.repository.EmployerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployerDetailService implements UserDetailsService {

    private final EmployerProfileRepository employerProfileRepository;

    // Load user by their email (username in this case) for authentication purposes
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var userOptional = employerProfileRepository.findByEmailAddress(username);

        // If user is found, return it; otherwise, throw an exception
        if (userOptional.isPresent()) {
            return userOptional.orElseThrow();  // This will return the user object if present
        }

        // Exception is thrown if no user is found with the given email
        throw new UsernameNotFoundException("No USER found with emailAddress: " + username);
    }
}
