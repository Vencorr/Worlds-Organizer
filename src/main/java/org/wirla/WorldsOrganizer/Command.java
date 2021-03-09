package org.wirla.WorldsOrganizer;

interface Command {

    public void execute();

    public void undo();

}