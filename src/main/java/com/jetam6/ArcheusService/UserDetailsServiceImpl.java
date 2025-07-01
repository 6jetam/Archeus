package com.jetam6.ArcheusService;

import com.jetam6.ArcheusModel.ArcheusUser;
import com.jetam6.ArcheusRepository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ArcheusUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword()) // heslo je nutné zadať, aj keď sa pri JWT nepoužíva
                .authorities(authority)   // očakávame, že máš role ako "ROLE_USER", "ROLE_ADMIN"
                .build();
    }
}
