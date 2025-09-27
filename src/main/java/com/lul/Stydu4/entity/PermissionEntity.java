package com.lul.Stydu4.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class PermissionEntity {

    @Id
    private String name;
    private String description;

//    @ManyToMany(mappedBy = "permissions")
//    Set<RoleEntity> roles = new HashSet<>();

}
