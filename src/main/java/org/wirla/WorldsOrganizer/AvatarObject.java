package org.wirla.WorldsOrganizer;

public class AvatarObject implements WorldList {

    private String name;
    private String value;

    AvatarObject(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String code) {
        this.value = code;
    }
}
