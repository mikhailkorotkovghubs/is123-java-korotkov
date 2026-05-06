package com.familybudget.config;

import com.familybudget.model.User;
import com.familybudget.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 ПОИСК ПОЛЬЗОВАТЕЛЯ: " + username);

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            System.out.println("ПОЛЬЗОВАТЕЛЬ НЕ НАЙДЕН В БАЗЕ!");
            throw new UsernameNotFoundException("Пользователь не найден: " + username);
        }

        User user = userOpt.get();
        System.out.println("ПОЛЬЗОВАТЕЛЬ НАЙДЕН: " + user.getUsername() + ", Роль: " + user.getRole());
        System.out.println("   Хэш в БД: " + user.getPasswordHash());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}