package org.wirla.WorldsOrganizer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TableTab {

    int dataType = 0;
    File ourFile;

    Tab tab = null;
    TableView table = null;

    boolean unsaved = false;

    List<WorldDataObject> values = new ArrayList<>();

    public TableTab() {
    }

    public VBox getStart() {
        VBox vibby = new VBox();
        vibby.setAlignment(Pos.CENTER);

        ImageView logoView = new ImageView(new Image("file:logo.png"));
        Text text1 = new Text("Worlds Organizer v" + Console.getVersion());
        text1.setStyle("-fx-font-size: 20;");

        Text text2 = new Text("Created and Maintained by Wirlaburla");

        Text text3 = new Text("Built on " + Console.getDate());

        vibby.getChildren().addAll(logoView, text1, text2, text3);

        return vibby;
    }

    public Tab getObjectTab(File file, int type) {
        if (tab == null) {
            TableView mainTable;
            if (table == null) {
                mainTable = new TableView();
                this.ourFile = file;
                if ((file == null)) {
                    Console.sendOutput("Creating fresh table", true);
                } else {
                    Console.sendOutput("Creating table of file " + file.getName(), true);
                }


                VBox.setVgrow(mainTable, Priority.ALWAYS);
                mainTable.setEditable(true);

                TableColumn<WorldDataObject, String> indexColumn = new TableColumn<>("#");
                indexColumn.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.05));
                indexColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(mainTable.getItems().indexOf(p.getValue())));
                indexColumn.setSortable(false);
                indexColumn.setEditable(false);

                TableColumn<WorldDataObject, String> labelColumn = new TableColumn<>("Label");
                labelColumn.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.4));
                labelColumn.setSortable(false);
                labelColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
                labelColumn.setCellFactory(TextFieldTableCell.forTableColumn());

                labelColumn.setOnEditCommit(t -> {
                    t.getTableView().getItems().get(t.getTablePosition().getRow()).setLabel(t.getNewValue());
                    setUnsaved(true);
                });

                TableColumn<WorldDataObject, String> valueColumn = new TableColumn<>("Value");
                valueColumn.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.5));
                valueColumn.setSortable(false);
                valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
                valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());

                valueColumn.setOnEditCommit(t -> {
                    t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
                    setUnsaved(true);
                });

                if (file == null) {
                    assert type != 0;
                    this.dataType = type;
                    WorldDataObject newData = WorldDataObject.newType(type);
                    mainTable.getItems().add(newData);
                    values.add(newData);
                } else {
                    Restorer restorer = new Restorer(file);
                    try {
                        values = restorer.getValues();
                        this.dataType = restorer.type;
                        for (WorldDataObject wdo : values) {
                            mainTable.getItems().add(wdo);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Dialog.showException(e);
                    } catch (InvalidPersisterFile e) {
                        Console.sendOutput("InvalidPersisterFile encountered! Is this a Persister file?");
                        Dialog.showException(e);
                    }
                }

                mainTable.getColumns().add(indexColumn);
                mainTable.getColumns().add(labelColumn);
                mainTable.getColumns().add(valueColumn);

                this.table = mainTable;
            } else {
                mainTable = table;
            }
            if (file == null) {
                tab = new Tab("Untitled", mainTable);
            } else {
                tab = new Tab(file.getName(), mainTable);
            }
            tab.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/file.svg"))));
            tab.setTooltip(new Tooltip(WorldDataObject.getTypeString(dataType)));

            tab.setOnCloseRequest(event -> {
                event.consume();
                quitTab();
            });

            return tab;
        } else return tab;
    }

    public void addValue() {
        assert table != null;
        WorldDataObject newData = WorldDataObject.newType(dataType);

        values.add(newData);
        table.getItems().add(newData);

        setUnsaved(true);
    }

    public void delValue(int i) {
        assert table != null;

        values.remove(i);
        table.getItems().remove(i);

        setUnsaved(true);
    }

    public void moveValue(int i,int moveBy) {
        assert table != null;

        WorldDataObject row = values.get(i);

        values.remove(i);
        table.getItems().remove(i);
        values.add(i + moveBy, row);
        table.getItems().add(i + moveBy, row);

        setUnsaved(true);
    }

    public void setFocus(int i) {
        table.getSelectionModel().select(i);
    }

    private void quitTab() {
        if (unsaved) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.getDialogPane().setMinSize(200,200);
            alert.setTitle("Close Tab");
            alert.setHeaderText("Are you sure you want to close this tab?");
            alert.setContentText("You have unsaved changes. Closing now will lose your progress.");

            ButtonType dontSaveButton = new ButtonType("Discard Changes");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(dontSaveButton, buttonTypeCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == dontSaveButton) {
                closeTab();
            } else {
                alert.close();
            }
        } else {
            closeTab();
        }
    }

    private void closeTab() {
        int index = tab.getTabPane().getSelectionModel().getSelectedIndex() - 1;
        Main.tables.remove(index);

        EventHandler<Event> handler = tab.getOnClosed();
        if (null != handler) {
            handler.handle(null);
        } else {
            tab.getTabPane().getTabs().remove(tab);
        }
    }

    public void setUnsaved(boolean isUnsaved) {
        if (isUnsaved) {
            unsaved = true;
            tab.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/save.svg"))));
        } else{
            unsaved = false;
            tab.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/file.svg"))));
        }
    }
    
}
