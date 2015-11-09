package com.demo.smarthome.device;

/**
 * Created by leishi on 15/10/31.
 */

public class DeviceDataString {

    String name;
    String type;
    String unit;
    public DeviceDataString(String name, String type, String unit) {
        this.name = name;
        this.type = type;
        this.unit = unit;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}

