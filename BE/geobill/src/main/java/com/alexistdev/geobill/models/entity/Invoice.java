package com.alexistdev.geobill.models.entity;

import com.alexistdev.geobill.config.DatabaseTableNames;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = DatabaseTableNames.TB_INVOICES)
@SQLDelete(sql = "UPDATE " + DatabaseTableNames.TB_INVOICES + " SET is_deleted = true WHERE uuid = ?")
@Where(clause = "is_deleted = false")
public class Invoice extends BaseEntity<String> implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uuid")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hosting_id", referencedColumnName = "uuid", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Hosting hosting;

    @NotNull
    @Column(name = "invoice_code", updatable = false, unique = true, nullable = false, length = 50)
    private String invoiceCode;

    @NotNull
    @Column(name="detail")
    private String detail;

    @NotNull
    @Column(name = "sub_total")
    private Double subTotal;

    @NotNull
    @Column(name = "total")
    private Double total;

    @NotNull
    @Column(name = "tax")
    private Double tax;

    @NotNull
    @Column(name = "discount")
    private Double discount;

    @NotNull
    @Column(name = "cycle")
    private int cycle;

    @NotNull
    private Date startDate;

    @NotNull
    private Date endDate;

    @NotNull
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int status = 0;

}
