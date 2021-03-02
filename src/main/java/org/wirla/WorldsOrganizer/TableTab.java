package org.wirla.WorldsOrganizer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TableTab {

    int dataType;
    File ourFile;

    TableView table = null;
    Tab tab = null;

    boolean unsaved = false;

    List<WorldDataObject> values = new ArrayList<>();

    public TableTab() {
    }

    public GridPane getStart() {
        return new GridPane();
    }
    
    public Tab getTab(File file) {
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
                indexColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<WorldDataObject, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<WorldDataObject, String> p) {
                        return new ReadOnlyObjectWrapper(mainTable.getItems().indexOf(p.getValue()) + "");
                    }
                });
                indexColumn.setSortable(false);
                indexColumn.setEditable(false);

                TableColumn<WorldDataObject, String> labelColumn = new TableColumn<>("Label");
                labelColumn.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.4));
                labelColumn.setSortable(false);
                labelColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
                labelColumn.setCellFactory(TextFieldTableCell.forTableColumn());

                labelColumn.setOnEditCommit(t -> {
                    ((WorldDataObject) t.getTableView().getItems().get(t.getTablePosition().getRow())).setLabel(t.getNewValue());
                    doUpdate();
                });

                TableColumn<WorldDataObject, String> valueColumn = new TableColumn<>("Value");
                valueColumn.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.5));
                valueColumn.setSortable(false);
                valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
                valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());

                valueColumn.setOnEditCommit(t -> {
                    ((WorldDataObject) t.getTableView().getItems().get(t.getTablePosition().getRow())).setValue(t.getNewValue());
                    doUpdate();
                });

                if (file == null) {
                    this.dataType = 0;
                    WorldDataObject newData = new WorldDataObject(dataType, 1, "New Object", "");
                    mainTable.getItems().add(newData);
                    values.add(newData);
                } else {
                    Restorer restorer = new Restorer(file);
                    this.dataType = restorer.type;
                    try {
                        values = restorer.getValues();
                        for (WorldDataObject wdo : values) {
                            mainTable.getItems().add(wdo);
                        }
                    } catch (IOException e) {
                        Console.sendOutput("IOException encountered! An issue with the file, perhaps?");
                    } catch (InvalidPersisterFile e) {
                        Console.sendOutput("InvalidPersisterFile encountered! Is this a Persister file?");
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

            tab.setOnCloseRequest(event -> {
                event.consume();
                quitTab();
            });

            return tab;
        } else return tab;
    }

    public void addValue() {
        assert table != null;
        WorldDataObject newData;
        switch (dataType) {
            default:
                newData = new WorldDataObject(dataType, 1, "New Object", "");
                break;
            case 1:
                newData = new WorldDataObject(dataType, 1, "New Avatar", "avatar:axel.rwx");
                break;
            case 2:
                newData = new WorldDataObject(dataType, 1, "New WorldsMark", "home:GroundZero/groundzero.world");
                break;
        }

        values.add(newData);
        table.getItems().add(newData);

        doUpdate();
    }

    public void delValue(int i) {
        assert table != null;

        values.remove(i);
        table.getItems().remove(i);

        doUpdate();
    }

    public void moveValue(int i,int moveBy) {
        assert table != null;

        WorldDataObject row = values.get(i);

        values.remove(i);
        table.getItems().remove(i);
        values.add(i + moveBy, row);
        table.getItems().add(i + moveBy, row);

        doUpdate();
    }

    public void setFocus(int i) {
        table.getSelectionModel().select(i);

        doUpdate();
    }

    private void quitTab() {
        if (unsaved) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
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
        EventHandler<Event> handler = tab.getOnClosed();
        if (null != handler) {
            handler.handle(null);
        } else {
            tab.getTabPane().getTabs().remove(tab);
        }
    }

    private void doUpdate() {
        unsaved = true;
        tab.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/save.svg"))));
    }
    
}
