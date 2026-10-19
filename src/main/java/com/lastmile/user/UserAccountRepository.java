package com.lastmile.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String normalizedEmail);

    boolean existsByEmail(String normalizedEmail);

    List<UserAccount> findByRoleAndActiveTrueOrderByFullNameAsc(Role role);

    List<UserAccount> findAllByOrderByRoleAscFullNameAsc();
}
