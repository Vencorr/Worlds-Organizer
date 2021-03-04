package org.wirla.WorldsOrganizer;

public class WorldTableItem implements WorldList {
    private int index;
    private String name;
    private String value;
    private String status;

    WorldTableItem(int i, String n, String v) {
        index = i;
        name = n;
        value = v;
    }

    WorldTableItem(int i, String n, String v, boolean s) {
        index = i;
        name = n;
        value = v;
        status = (s ? "PASS" : "FAIL");
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
