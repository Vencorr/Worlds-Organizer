package org.wirla.WorldsOrganizer;

import javafx.scene.image.Image;

public class AppIcon {

    public static Image logo = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/logo.svg"));

    public static Image newFile = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/file-plus.svg"));
    public static Image openFile = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/folder.svg"));
    public static Image saveFile = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/save.svg"));
    public static Image saveFileAs = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/save-as.svg"));

    public static Image undo = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/undo.svg"));
    public static Image redo = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/redo.svg"));

    public static Image quitApp = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/quit.svg"));

    public static Image add = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/plus.svg"));
    public static Image remove = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/delete.svg"));
    public static Image removeAll = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/delete-all.svg"));
    public static Image moveUp = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/up.svg"));
    public static Image moveDown = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/down.svg"));

    public static Image findReplace = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/find.svg"));
    public static Image linkCheck = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/link.svg"));

    public static Image unknownFile = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/file.svg"));
    public static Image avatarFile = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/avatar.svg"));
    public static Image markFile = IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/mark.svg"));

}
