package com.alexistdev.geobill.models.entity;

import com.alexistdev.geobill.config.DatabaseTableNames;
import com.alexistdev.geobill.config.EntityConstant;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@Table(name = DatabaseTableNames.TB_CUSTOMERS)
@Entity
public class Customer implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = EntityConstant.userRequired)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Nullable
    @Column(length = EntityConstant.tableLength255 , name = "first_name")
    private String firstName;

    @Nullable
    @Column(length = EntityConstant.tableLength255 , name = "last_name")
    private String lastName;

    @Nullable
    @Column(length = EntityConstant.tableLength255 , name = "business_name")
    private String businessName;

    @Nullable
    @Column(length = EntityConstant.tableLength255 , name = "address1")
    private String address1;

    @Nullable
    @Column(length = EntityConstant.tableLength255 , name = "address2")
    private String address2;

    @Nullable
    @Column(length = EntityConstant.tableLength255 , name = "city")
    private String city;

    @Nullable
    @Column(length = EntityConstant.tableLength255 , name = "state")
    private String state;

    @Nullable
    @Column(length = EntityConstant.tableLength255 , name = "country")
    private String country;

    @Nullable
    @Column(length = EntityConstant.tableLength255 , name = "post_code")
    private String postCode;

    @Nullable
    @Column(length = EntityConstant.tableLength255 , name = "phone")
    private String phone;

}
