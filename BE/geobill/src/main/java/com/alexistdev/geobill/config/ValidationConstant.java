package com.alexistdev.geobill.config;

public class ValidationConstant {

    public static final String nameMax = "Maximum Name's character length is 150 characters";

    public static final String nameNotNull = "Name cannot be null";

    public static String success(String name)
    {
        return String.format("%s successfully added", name);
    }

    public static String deleted(String name)
    {
        return String.format("%s has been deleted", name);
    }
}
