package com.edgedx.connectpoc.model;

public enum DeviceType {
    ALERE_PIMA(1, "Alere PIMA", "PIMA"),
    BD_FACSPRESTO(2, "BD FacsPresto", "FACSPresto"),
    CEPHEID_GENEXPERT(3, "Cepheid GeneXpert", "GeneXpert"),
    TRIAGE_METER(4, "Triage Meter Pro", "Meter Pro"),
    ALERE_Q(5, "Alere Q", "Alere Q"),
    CEPHEID_OMNI(6, "Cepheid Omni", "Omni"),
    BD_MGIT(7, "BD MGIT", "MGIT"),
    HOLOGIC_PANTHER(8, "Hologic Panther", "Panther"),
    ABBOTT_M2000(9, "Abbott m2000", "m2000");

    public static DeviceType findByCode(Integer id) {
        if (id != null)
            switch (id) {
                case 1:
                    return DeviceType.ALERE_PIMA;
                case 2:
                    return DeviceType.BD_FACSPRESTO;
                case 3:
                    return DeviceType.CEPHEID_GENEXPERT;
                case 4:
                    return DeviceType.TRIAGE_METER;
                case 5:
                    return DeviceType.ALERE_Q;
                case 6:
                    return DeviceType.CEPHEID_OMNI;
                case 7:
                    return DeviceType.BD_MGIT;
                case 8:
                    return DeviceType.HOLOGIC_PANTHER;
                case 9:
                    return DeviceType.ABBOTT_M2000;
                default:
                    return null;
            }

        return null;
    }


    public static int getCode(DeviceType type) {
        switch (type) {
            case ALERE_PIMA:
                return 1;
            case BD_FACSPRESTO:
                return 2;
            case CEPHEID_GENEXPERT:
                return 3;
            case TRIAGE_METER:
                return 4;
            case ALERE_Q:
                return 5;
            case CEPHEID_OMNI:
                return 6;
            case BD_MGIT:
                return 7;
            case HOLOGIC_PANTHER:
                return 8;
            case ABBOTT_M2000:
                return 9;
            default:
                return 0;
        }
    }

    private final int code;

    private final String description;

    private final String screenName;


    private DeviceType(int code, String description, String screenName) {
        this.code = code;
        this.description = description;
        this.screenName = screenName;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public String getScreenName() {
        return this.screenName;
    }

}
