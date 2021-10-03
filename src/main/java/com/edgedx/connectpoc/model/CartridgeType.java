package com.edgedx.connectpoc.model;

public enum CartridgeType {
    HIV_QUAL(1, "HIV-1 Qual"),
    HIV_VIRAL_LOAD(2, "HIV-1 Viral Load"),
    FACS_PRESTO(3, "Facs Presto"),
    ABBOTT_M2000(4, "Abbott m2000");

    public static CartridgeType findByCode(Integer id) {
        if (id != null)
            switch (id) {
                case 1:
                    return CartridgeType.HIV_QUAL;
                case 2:
                    return CartridgeType.HIV_VIRAL_LOAD;
                case 3:
                    return CartridgeType.FACS_PRESTO;
                case 4:
                    return CartridgeType.ABBOTT_M2000;
                default:
                    return null;
            }

        return null;
    }


    private final int code;

    private final String description;

    private CartridgeType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }
}