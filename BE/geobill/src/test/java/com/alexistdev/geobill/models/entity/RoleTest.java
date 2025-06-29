package com.alexistdev.geobill.models.entity;

import org.apache.commons.lang3.EnumUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RoleTest {

    @Test
    void testEnumValues(){
        Role[] roles = Role.values();
        Assertions.assertEquals(3, roles.length);
        Assertions.assertEquals(Role.USER, roles[0]);
        Assertions.assertEquals(Role.STAFF, roles[1]);
        Assertions.assertEquals(Role.ADMIN, roles[2]);
    }

    @Test
    void testEnumValuesExist(){
        Assertions.assertTrue(EnumUtils.isValidEnum(Role.class, "ADMIN"));
        Assertions.assertTrue(EnumUtils.isValidEnum(Role.class, "USER"));
        Assertions.assertTrue(EnumUtils.isValidEnum(Role.class, "STAFF"));
    }

    @Test
    void testEnumValuesNotExist(){
        Assertions.assertFalse(EnumUtils.isValidEnum(Role.class, "MANAGER"));
        Assertions.assertFalse(EnumUtils.isValidEnum(Role.class, "SUPER ADMIN"));
    }

    @Test
    void testEnumOrdinal(){
        Assertions.assertSame(0,Role.USER.ordinal());
        Assertions.assertSame(1,Role.STAFF.ordinal());
        Assertions.assertSame(2,Role.ADMIN.ordinal());
    }

    @Test
    void testEnumValueOf() {
        Assertions.assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        Assertions.assertEquals(Role.STAFF, Role.valueOf("STAFF"));
        Assertions.assertEquals(Role.USER, Role.valueOf("USER"));
    }
}
