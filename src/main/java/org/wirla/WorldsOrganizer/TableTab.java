package org.wirla.WorldsOrganizer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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

    private boolean modified = false;

    List<WorldDataObject> values = new ArrayList<>();

    public TableTab() {
    }

    public VBox getStart() {
        VBox vibby = new VBox();
        vibby.setAlignment(Pos.CENTER);

        ImageView logoView = new ImageView(new Image(Main.class.getResourceAsStream("/logo.png")));
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
            HBox hBox = new HBox();
            if (table == null) {
                mainTable = new TableView();
                this.ourFile = file;
                if ((file == null)) {
                    Console.sendOutput("Creating fresh table", true);
                } else {
                    Console.sendOutput("Creating table of file " + file.getName(), true);
                }

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
                    setSaved(false);
                });

                TableColumn<WorldDataObject, String> valueColumn = new TableColumn<>("Value");
                valueColumn.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.5));
                valueColumn.setSortable(false);
                valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
                valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());

                valueColumn.setOnEditCommit(t -> {
                    t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
                    setSaved(false);
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

                HBox.setHgrow(mainTable, Priority.ALWAYS);

                ToolBar toolBar = new ToolBar();
                toolBar.setOrientation(Orientation.VERTICAL);

                Button addBtn = new Button();
                addBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/plus.svg"))));
                toolBar.getItems().add(addBtn);

                Button delBtn = new Button();
                delBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/delete.svg"))));
                toolBar.getItems().add(delBtn);

                Button mupBtn = new Button();
                mupBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/up.svg"))));
                toolBar.getItems().add(mupBtn);

                Button mdwBtn = new Button();
                mdwBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/down.svg"))));
                toolBar.getItems().add(mdwBtn);

                addBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                    int tabIndex = Main.tables.indexOf(this);

                    if (tabIndex >= 0) {
                        this.addValue();
                        this.setFocus(this.table.getItems().size() - 1);

                        Main.tables.set(tabIndex, this);
                    }
                });

                delBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                    int tabIndex = Main.tables.indexOf(this);

                    if (tabIndex >= 0) {
                        int index = this.table.getSelectionModel().getFocusedIndex();
                        this.delValue(index);
                        this.setFocus(index < this.table.getItems().size() ? index : index - 1);

                        Main.tables.set(tabIndex, this);
                    }
                });

                mupBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                    int tabIndex = Main.tables.indexOf(this);

                    if (tabIndex >= 0) {
                        int index = this.table.getSelectionModel().getFocusedIndex();
                        this.moveValue(index, -1);
                        this.setFocus(index - 1);

                        Main.tables.set(tabIndex, this);
                    }
                });

                mdwBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                    int tabIndex = Main.tables.indexOf(this);

                    if (tabIndex >= 0) {
                        int index = this.table.getSelectionModel().getFocusedIndex();
                        this.moveValue(index, 1);
                        this.setFocus(index + 1);

                        Main.tables.set(tabIndex, this);
                    }
                });

                HBox.setHgrow(toolBar, Priority.ALWAYS);
                hBox = new HBox(toolBar, mainTable);
                this.table = mainTable;
            } else {
                mainTable = table;
            }

            VBox.setVgrow(hBox, Priority.ALWAYS);

            if (file == null) {
                tab = new Tab("Untitled", hBox);
            } else {
                tab = new Tab(file.getName(), hBox);
            }

            setIcon();
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

        setSaved(false);
    }

    public void delValue(int i) {
        assert table != null;

        values.remove(i);
        table.getItems().remove(i);

        setSaved(false);
    }

    public void moveValue(int i,int moveBy) {
        assert table != null;

        WorldDataObject row = values.get(i);

        values.remove(i);
        table.getItems().remove(i);
        values.add(i + moveBy, row);
        table.getItems().add(i + moveBy, row);

        setSaved(false);
    }

    public void setFocus(int i) {
        table.getSelectionModel().select(i);
    }

    private void quitTab() {
        if (!getSaved()) {
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

    private void setIcon() {
        switch (dataType) {
            default:
                tab.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/file.svg"))));
                break;
            case 1:
                tab.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/avatar.svg"))));
                break;
            case 2:
                tab.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/mark.svg"))));
                break;
        }
    }

    public void setSaved(boolean value) {
        if (!value) {
            modified = true;
            tab.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/save.svg"))));
        } else{
            modified = false;
            setIcon();
        }
    }

    public boolean getSaved() {
        return !modified;
    }
    
}
