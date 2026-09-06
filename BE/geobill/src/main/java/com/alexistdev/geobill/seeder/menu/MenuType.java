package com.alexistdev.geobill.seeder.menu;

/**
 * Value stored in {@code tb_menus.type_menu}, telling which sidebar a menu belongs to.
 */
public enum MenuType {

    ADMIN(1),
    USER(2);

    private final int value;

    MenuType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
