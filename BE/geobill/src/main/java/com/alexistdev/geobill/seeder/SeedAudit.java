package com.alexistdev.geobill.seeder;

import com.alexistdev.geobill.models.entity.BaseEntity;

import java.util.Date;

/**
 * Fills the audit columns of entities created by a seeder.
 *
 * <p>Nobody is logged in while seeding, so {@code AuditorAware} returns empty and the audit
 * columns have to be filled in by hand here.</p>
 */
public final class SeedAudit {

    public static final String SYSTEM_USER = "System";

    private SeedAudit() {
    }

    public static <E extends BaseEntity<String>> E stamp(E entity) {
        Date now = new Date();
        entity.setCreatedBy(SYSTEM_USER);
        entity.setModifiedBy(SYSTEM_USER);
        entity.setCreatedDate(now);
        entity.setModifiedDate(now);
        entity.setDeleted(Boolean.FALSE);
        return entity;
    }
}
