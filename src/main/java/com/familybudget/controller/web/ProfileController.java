package com.familybudget.controller.web;

import com.familybudget.model.User;
import com.familybudget.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);
        return "settings";
    }

    @PostMapping("/update")
    public RedirectView updateProfile(@RequestParam String displayName,
                                      @RequestParam(required = false) String newPassword,
                                      Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        user.setDisplayName(displayName);
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
        }

        userRepository.save(user);
        return new RedirectView("/profile/settings?success=true");
    }
}