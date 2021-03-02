package org.wirla.WorldsOrganizer;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Dialog {

    public static void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().setMinSize(200,200);
        alert.setTitle("An Error Occurred");
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }

    public static void showException(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().setMinSize(200,200);
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

        alert.showAndWait();
    }

    public static int newFile() {
        List<String> choices = new ArrayList<>();
        choices.add("Avatars");
        choices.add("WorldsMarks");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.getDialogPane().setMinSize(200,200);
        dialog.setTitle("New File");
        dialog.setHeaderText("Select a type for the new file.");
        dialog.setContentText("Type:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if (result.get().equals(choices.get(0)))
                return 1;
            else if (result.get().equals(choices.get(1)))
                return 2;
            else return 0;
        } else {
            return 0;
        }
    }

}
