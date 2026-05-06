package com.familybudget.controller.web;

import com.familybudget.model.Family;
import com.familybudget.model.Role;
import com.familybudget.model.User;
import com.familybudget.repository.FamilyRepository;
import com.familybudget.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/family")
public class FamilyController {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final PasswordEncoder passwordEncoder;

    public FamilyController(UserRepository userRepository,
                            FamilyRepository familyRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.familyRepository = familyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Страница управления семьей
    @GetMapping
    public String familyPage(Model model, org.springframework.security.core.Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<User> familyMembers = userRepository.findAllByFamilyId(currentUser.getFamily().getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("familyMembers", familyMembers);
        model.addAttribute("Role", Role.class); // Для доступа к enum в шаблоне

        return "family-management";
    }

    // Обновление данных участника (только для Главы)
    @PostMapping("/update-member")
    public RedirectView updateMember(@RequestParam Long userId,
                                     @RequestParam String displayName,
                                     @RequestParam(required = false) String role,
                                     org.springframework.security.core.Authentication authentication) {
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверка прав
        if (currentUser.getRole() != Role.OWNER) {
            return new RedirectView("/family?error=access_denied");
        }

        User member = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Участник не найден"));

        // Нельзя редактировать самого Главу (кроме отображаемого имени)
        if (member.getRole() == Role.OWNER && member.getId().equals(currentUser.getId())) {
            member.setDisplayName(displayName);
            userRepository.save(member);
            return new RedirectView("/family?success=true");
        }

        member.setDisplayName(displayName);
        if (role != null && !role.isEmpty()) {
            try {
                member.setRole(Role.valueOf(role));
            } catch (IllegalArgumentException e) {
                // Игнорируем невалидные роли
            }
        }

        userRepository.save(member);
        return new RedirectView("/family?success=true");
    }

    // Добавление нового члена семьи (только для Главы)
    @PostMapping("/add-member")
    public RedirectView addMember(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam String displayName,
                                  @RequestParam(defaultValue = "MEMBER") String role,
                                  org.springframework.security.core.Authentication authentication) {
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверка прав: только Глава может добавлять
        if (currentUser.getRole() != Role.OWNER) {
            return new RedirectView("/family?error=access_denied");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return new RedirectView("/family?error=username_exists");
        }

        User newMember = new User();
        newMember.setUsername(username);
        newMember.setPasswordHash(passwordEncoder.encode(password));
        newMember.setDisplayName(displayName);
        newMember.setFamily(currentUser.getFamily());
        newMember.setEmail("");

        try {
            newMember.setRole(Role.valueOf(role));
        } catch (IllegalArgumentException e) {
            newMember.setRole(Role.MEMBER); // По умолчанию
        }

        userRepository.save(newMember);
        return new RedirectView("/family?success=true");
    }

    // Удаление участника (только для Главы, нельзя удалить самого себя)
    @PostMapping("/remove-member")
    public RedirectView removeMember(@RequestParam Long userId,
                                     org.springframework.security.core.Authentication authentication) {
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверка прав
        if (currentUser.getRole() != Role.OWNER) {
            return new RedirectView("/family?error=access_denied");
        }

        User member = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Участник не найден"));

        // Нельзя удалить Главу семьи
        if (member.getRole() == Role.OWNER) {
            return new RedirectView("/family?error=cannot_remove");
        }

        userRepository.delete(member);
        return new RedirectView("/family?success=true");
    }
}