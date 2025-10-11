package com.lul.Stydu4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // CHỈ dùng field có @Include
@ToString(exclude = "permissions")  // Exclude lazy collection khỏi toString
public class RoleEntity {

    @Id
    @EqualsAndHashCode.Include  // CHỈ dùng name cho equals/hashCode
    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_name"),
            inverseJoinColumns = @JoinColumn(name = "permission_name")
    )
    @Builder.Default
    private Set<PermissionEntity> permissions = new HashSet<>();
}
