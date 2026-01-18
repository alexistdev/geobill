package com.alexistdev.geobill.config;

public class ValidationConstant {

    public static final String nameMax = "Maximum Name's character length is 150 characters";
    public static final String nameMax255 = "Maximum Name's character length is 255 characters";

    public static final String nameNotNull = "Name cannot be null";

    public static final String productTypeRequired = "Product Type is required";
    public static final String productTypeMax = "Maximum ID's character length is 255 characters";

    public static String success(String name)
    {
        return String.format("%s successfully added", name);
    }

    public static String deleted(String name)
    {
        return String.format("%s has been deleted", name);
    }


    //Product
    public static final String priceNotNull = "Price is required";
    public static final String cycleNotNull = "Cycle is required";

}
