package com.lul.Stydu4.entity;

import com.lul.Stydu4.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class UserEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String username;
    private String firstName;
    private String lastName;

    @Column(nullable = true)
    private String password;

    private LocalDate dob;

    private String email;
    private String phoneNumber;

    // Thêm các field mới cho OAuth2
    @Column(name = "auth_provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider; // LOCAL, GOOGLE

    @Column(name = "provider_id")
    private String providerId; // Google user ID

    @Column(name = "is_banned")
    @Builder.Default
    private Boolean isBanned = false; // User account status

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    private Set<RoleEntity> roles;


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ResultEntity> results;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

}
