package org.wirla.WorldsOrganizer;

import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Dialog {

    public static void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("An Error Occurred");
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.getDialogPane().setMinSize(200,200);
        alert.showAndWait();
    }

    public static void showException(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("An Exception was encountered.");
        alert.setContentText("Please file a bug report if this problem persists.");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        alert.getDialogPane().setMinSize(200,200);
        alert.showAndWait();
    }

    public static WorldsType newFile() {
        List<String> choices = new ArrayList<>();
        choices.add("Avatars");
        choices.add("WorldsMarks");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("New File");
        dialog.setHeaderText("Select a type for the new file.");
        dialog.setContentText("Type:");

        dialog.getDialogPane().setMinSize(200,200);
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if (result.get().equals(choices.get(0)))
                return WorldsType.AVATAR;
            else if (result.get().equals(choices.get(1)))
                return WorldsType.WORLDSMARK;
            else return WorldsType.NULL;
        } else {
            return WorldsType.NULL;
        }
    }

    public static void showUpdate(Version newVer) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Update Dialog");
        alert.setHeaderText("A new version is available to download.");
        alert.setContentText("You are currently on " + Console.getVersion() + " which is older than " + newVer.get() + ".");

        ButtonType updateButton = new ButtonType("Update");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(updateButton, buttonTypeCancel);

        alert.getDialogPane().setMinSize(200,200);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == updateButton) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("https://wirlaburla.com/WorldsOrganizer/download"));
                } else {
                    showError("An error occurred attempting to show you a webpage.", "Desktop not supported for Action.BROWSE.");
                }
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        } else {
            alert.close();
        }
    }
}
