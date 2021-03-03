package org.wirla.WorldsOrganizer;

import java.util.*;

enum WorldsType {
    NULL(""),
    AVATAR("NET.worlds.console.SavedAvMenuItem"),
    WORLDSMARK("NET.worlds.console.BookmarkMenuItem"),
    LIBRARY("NET.worlds.scape.Library");

    public final String name;

    WorldsType(String name) {
        this.name = name;
    }

    public static WorldsType valueOfClass(String name) {
        for (WorldsType e : values()) {
            if (e.name.equals(name)) {
                return e;
            }
        }
        return null;
    }
}

public class WorldListObject {

    public WorldsType classType = null;
    private List<WorldList> values = new ArrayList<>();

    WorldListObject() {
    }

    public void determineClassType(String className) {
        switch (className) {
            default:
                break;
            case "NET.worlds.console.SavedAvMenuItem":
                classType = WorldsType.AVATAR;
                break;
            case "NET.worlds.console.BookmarkMenuItem":
                classType = WorldsType.WORLDSMARK;
                break;
            case "NET.worlds.scape.Library":
                classType = WorldsType.LIBRARY;
                break;
        }
    }

    public List<WorldList> getValues() {
        return values;
    }

    public void setValues(List<WorldList> values) {
        this.values = values;
    }


    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean contains(WorldList o) {
        return values.contains(o);
    }

    public Iterator iterator() {
        return values.iterator();
    }

    public WorldList[] toArray() {
        return values.toArray(new WorldList[0]);
    }

    public boolean add(WorldList o) {
        return values.add(o);
    }

    public boolean remove(Object o) {
        return values.remove(o);
    }

    public boolean addAll(Collection collection) {
        return values.addAll(collection);
    }

    public boolean addAll(int i, Collection collection) {
        return values.addAll(i, collection);
    }

    public void clear() {
        values = new ArrayList<>();
    }

    public WorldList get(int i) {
        return values.get(i);
    }

    public WorldList set(int i, WorldList o) {
        return values.set(i, o);
    }

    public void add(int i, WorldList o) {
        values.add(i, o);
    }

    public WorldList remove(int i) {
        return values.remove(i);
    }

    public int indexOf(WorldList o) {
        return values.indexOf(o);
    }

    public int lastIndexOf(WorldList o) {
        return values.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return values.listIterator();
    }

    public ListIterator listIterator(int i) {
        return values.listIterator(i);
    }

    public List subList(int i, int i1) {
        return values.subList(i, i1);
    }

    public boolean retainAll(Collection collection) {
        return values.retainAll(collection);
    }

    public boolean removeAll(Collection collection) {
        return values.removeAll(collection);
    }

    public boolean containsAll(Collection collection) {
        return values.containsAll(collection);
    }

    public WorldList[] toArray(WorldList[] lists) {
        return values.toArray(lists);
    }
}
