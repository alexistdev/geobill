package com.alexistdev.geobill.models.entity;

import com.alexistdev.geobill.config.DatabaseTableNames;
import com.alexistdev.geobill.config.EntityConstant;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@Table(name = DatabaseTableNames.TB_CUSTOMERS)
@Entity
public class Customer extends BaseEntity<String> implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "customer_number", updatable = false, insertable = false, columnDefinition = "SERIAL")
    private Long customerNumber;

    @NotNull(message = EntityConstant.userRequired)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Nullable
    @Size(max = EntityConstant.tableLength255, message = "{customer.businessName.size}")
    @Column(length = EntityConstant.tableLength255, name = "business_name")
    private String businessName;

    @Nullable
    @Size(max = EntityConstant.tableLength255, message = "{customer.address1.size}")
    @Column(length = EntityConstant.tableLength255, name = "address1")
    private String address1;

    @Nullable
    @Size(max = EntityConstant.tableLength255, message = "{customer.address2.size}")
    @Column(length = EntityConstant.tableLength255, name = "address2")
    private String address2;

    @Nullable
    @Size(max = EntityConstant.tableLength255, message = "{customer.city.size}")
    @Column(length = EntityConstant.tableLength255, name = "city")
    private String city;

    @Nullable
    @Size(max = EntityConstant.tableLength255, message = "{customer.state.size}")
    @Column(length = EntityConstant.tableLength255, name = "state")
    private String state;

    @Nullable
    @Size(max = EntityConstant.tableLength255, message = "{customer.country.size}")
    @Column(length = EntityConstant.tableLength255, name = "country")
    private String country;

    @Nullable
    @Size(max = EntityConstant.tableLength255, message = "{customer.postCode.size}")
    @Column(length = EntityConstant.tableLength255, name = "post_code")
    private String postCode;

    @Nullable
    @Size(max = EntityConstant.tableLength255, message = "{customer.phone.size}")
    @Column(length = EntityConstant.tableLength255, name = "phone")
    private String phone;

}
