package com.alexistdev.geobill.models.entity;

import com.alexistdev.geobill.config.DatabaseTableNames;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(
        name = DatabaseTableNames.TB_ROLE_MENUS,
        uniqueConstraints = {
                @UniqueConstraint(name="uk_role_menu",columnNames = {"role_id", "menu_id"})
        },
        indexes = {
                @Index(name="idx_role_menu_role_id",columnList = "role_id"),
                @Index(name="idx_role_menu_menu_id",columnList = "menu_id")
        }
)
public class RoleMenu extends BaseEntity<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role",nullable = false, length = 50)
    private Role role;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_uuid", nullable = false)
    private Menu menu;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }
}
