package com.lastmile.user;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Minimal, read-only account review for administrators (assumption AS2). */
@Controller
public class UserAdminController {

    private final UserAccountRepository users;

    public UserAdminController(UserAccountRepository users) {
        this.users = users;
    }

    public record UserRow(String fullName, String email, Role role, boolean active) {
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String list(Model model) {
        List<UserRow> rows = users.findAllByOrderByRoleAscFullNameAsc().stream()
                .map(u -> new UserRow(u.getFullName(), u.getEmail(), u.getRole(), u.isActive()))
                .toList();
        model.addAttribute("users", rows);
        return "admin/users";
    }
}
