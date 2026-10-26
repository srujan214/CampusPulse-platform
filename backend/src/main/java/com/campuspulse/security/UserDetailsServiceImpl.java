package com.campuspulse.security;

import com.campuspulse.entity.User;
import com.campuspulse.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(usernameOrId)
                .or(() -> userRepository.findByStudentOrStaffId(usernameOrId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or ID: " + usernameOrId));

        return UserDetailsImpl.build(user);
    }
}
