package org.wirla.WorldsOrganizer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorldsTab {

    File ourFile;

    Tab tab = null;
    TableView<WorldList> table = null;

    private boolean modified = false;

    WorldListObject worldList = new WorldListObject();

    public WorldsTab() {
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

    public Tab getTable(File file, WorldsType type) {
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

                TableColumn<WorldList, String> indexColumn = new TableColumn<>("#");
                indexColumn.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.05));
                indexColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(mainTable.getItems().indexOf(p.getValue())));
                indexColumn.setSortable(false);
                indexColumn.setEditable(false);

                TableColumn<WorldList, String> labelColumn = new TableColumn<>("Label");
                labelColumn.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.4));
                labelColumn.setSortable(false);
                labelColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
                labelColumn.setCellFactory(TextFieldTableCell.forTableColumn());

                labelColumn.setOnEditCommit(t -> {
                    t.getTableView().getItems().get(t.getTablePosition().getRow()).setName(t.getNewValue());
                    setSaved(false);
                });

                TableColumn<WorldList, String> valueColumn = new TableColumn<>("Value");
                valueColumn.prefWidthProperty().bind(mainTable.widthProperty().multiply(0.525));
                valueColumn.setSortable(false);
                valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
                valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());

                valueColumn.setOnEditCommit(t -> {
                    t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
                    setSaved(false);
                });

                if (file == null) {
                    assert type != null;
                    worldList.classType = type;
                    WorldList newData = newData(type);

                    mainTable.getItems().add(newData);
                    worldList.add(newData);
                } else {
                    Restorer restorer = new Restorer(file);
                    try {
                        worldList = restorer.read();
                        worldList.classType = restorer.listObj.classType;
                        for (WorldList list : worldList.getValues()) {
                            mainTable.getItems().add(list);
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
                addBtn.setTooltip(new Tooltip("Add Value"));
                addBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/plus.svg"))));
                toolBar.getItems().add(addBtn);

                Button delBtn = new Button();
                delBtn.setTooltip(new Tooltip("Delete Value"));
                delBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/delete.svg"))));
                toolBar.getItems().add(delBtn);

                Button mupBtn = new Button();
                mupBtn.setTooltip(new Tooltip("Move Value Up"));
                mupBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/up.svg"))));
                toolBar.getItems().add(mupBtn);

                Button mdwBtn = new Button();
                mdwBtn.setTooltip(new Tooltip("Move Value Down"));
                mdwBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/down.svg"))));
                toolBar.getItems().add(mdwBtn);

                toolBar.getItems().add(new Separator());

                Button checkBtn = new Button();
                checkBtn.setTooltip(new Tooltip("Check Link Status"));
                checkBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/link.svg"))));
                toolBar.getItems().add(checkBtn);


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

                checkBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.getDialogPane().setMinWidth(600);
                    alert.setResizable(true);
                    alert.setTitle("Link Checker");
                    alert.setHeaderText("Checking Links...");

                    TableView<WorldTableItem> checkTable = new TableView<>();
                    checkTable.setEditable(false);

                    checkTable.setMaxWidth(Double.MAX_VALUE);
                    checkTable.setMaxHeight(Double.MAX_VALUE);
                    GridPane.setVgrow(checkTable, Priority.ALWAYS);
                    GridPane.setHgrow(checkTable, Priority.ALWAYS);

                    TableColumn<WorldTableItem, Integer> indexColumn1 = new TableColumn<>("#");
                    indexColumn1.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
                    indexColumn1.setCellValueFactory(new PropertyValueFactory<>("index"));

                    TableColumn<WorldTableItem, String> labelColumn1 = new TableColumn<>("Label");
                    labelColumn1.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
                    labelColumn1.setCellValueFactory(new PropertyValueFactory<>("name"));

                    TableColumn<WorldTableItem, String> valueColumn1 = new TableColumn<>("Value");
                    valueColumn1.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
                    valueColumn1.setCellValueFactory(new PropertyValueFactory<>("value"));

                    TableColumn<WorldTableItem, String> aliveColumn = new TableColumn<>("Status");
                    aliveColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
                    aliveColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

                    checkTable.getColumns().add(indexColumn1);
                    checkTable.getColumns().add(labelColumn1);
                    checkTable.getColumns().add(valueColumn1);
                    checkTable.getColumns().add(aliveColumn);

                    alert.getDialogPane().setContent(checkTable);

                    alert.initOwner(Main.primaryStage.getOwner());

                    List<WorldTableItem> errorItems = new ArrayList<>();

                    Task<Boolean> task = new Task<Boolean>() {
                        @Override public Boolean call() {

                            for (int w = 0; w < table.getItems().size(); w++) {
                                WorldList wl = table.getItems().get(w);

                                String value = wl.getValue();
                                int index = table.getItems().indexOf(wl);

                                WorldTableItem tabItem = new WorldTableItem(index, wl.getName(), value);
                                if (value.startsWith("http")) {
                                    try {
                                        if (!Console.testURL(value)) {
                                            tabItem = new WorldTableItem(index, wl.getName(), value, false);
                                            errorItems.add(tabItem);
                                        } else {
                                            tabItem = new WorldTableItem(index, wl.getName(), value, true);
                                        }
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                        Dialog.showException(ioException);
                                    }
                                    checkTable.getItems().add(0, tabItem);
                                }

                                checkTable.refresh();
                            }

                            return true;
                        }
                    };

                    task.setOnRunning((a) -> alert.show());
                    task.setOnSucceeded((a) -> {
                        alert.close();
                        showErrorTable(errorItems);
                    });
                    task.setOnFailed((a) -> {
                        Dialog.showException(new Exception("Unknown"));
                        alert.close();
                    });
                    new Thread(task).start();

                });

                HBox.setHgrow(toolBar, Priority.ALWAYS);
                hBox = new HBox(toolBar, mainTable);
                this.table = mainTable;
            }

            VBox.setVgrow(hBox, Priority.ALWAYS);

            if (file == null) {
                tab = new Tab("Untitled", hBox);
            } else {
                tab = new Tab(file.getName(), hBox);
            }

            setIcon();
            tab.setTooltip(new Tooltip(worldList.classType.name));

            tab.setOnCloseRequest(event -> {
                event.consume();
                quitTab();
            });
            return tab;
        } else return tab;
    }

    public void addValue() {
        assert table != null;
        WorldList newData = newData(worldList.classType);

        worldList.add(newData);
        table.getItems().add(newData);

        setSaved(false);
    }

    public void delValue(int i) {
        assert table != null;

        worldList.remove(i);
        table.getItems().remove(i);

        setSaved(false);
    }

    public void moveValue(int i,int moveBy) {
        assert table != null;

        WorldList row = worldList.get(i);

        worldList.remove(i);
        table.getItems().remove(i);
        worldList.add(i + moveBy, row);
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
        switch (worldList.classType) {
            default:
                tab.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/file.svg"))));
                break;
            case AVATAR:
                tab.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/avatar.svg"))));
                break;
            case WORLDSMARK:
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

    private WorldList newData(WorldsType type) {
        switch (type) {
            default:
                return null;
            case AVATAR:
                return new AvatarObject("New Avatar", "avatar:holden.mov");
            case WORLDSMARK:
                return new MarkObject("New Mark", "home:GroundZero/groundzero.world");
        }
    }

    public void showErrorTable(List<WorldTableItem> list) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setMinWidth(600);
        alert.setResizable(true);
        alert.setTitle("Link Checker Results");
        alert.setHeaderText("The followings links have been found to be dead.");

        ButtonType deleteBtn = new ButtonType("Delete All");
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

        TableView<WorldTableItem> errorTable = new TableView<>();
        errorTable.setEditable(false);

        errorTable.setMaxWidth(Double.MAX_VALUE);
        errorTable.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(errorTable, Priority.ALWAYS);
        GridPane.setHgrow(errorTable, Priority.ALWAYS);

        TableColumn<WorldTableItem, Integer> indexColumn = new TableColumn<>("#");
        indexColumn.prefWidthProperty().bind(errorTable.widthProperty().multiply(0.05));
        indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));

        TableColumn<WorldTableItem, String> labelColumn = new TableColumn<>("Label");
        labelColumn.prefWidthProperty().bind(errorTable.widthProperty().multiply(0.4));
        labelColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<WorldTableItem, String> valueColumn = new TableColumn<>("Value");
        valueColumn.prefWidthProperty().bind(errorTable.widthProperty().multiply(0.525));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        for (WorldTableItem item : list) {
            errorTable.getItems().add(item);
        }

        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(errorTable, 0, 0);

        errorTable.getColumns().add(indexColumn);
        errorTable.getColumns().add(labelColumn);
        errorTable.getColumns().add(valueColumn);

        alert.getButtonTypes().setAll(deleteBtn, closeBtn);
        alert.getDialogPane().setContent(content);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == deleteBtn) {
            int addition = 0;
            for (WorldTableItem item : list) {
                delValue(item.getIndex() + addition);
                addition--;
            }
        }
        alert.close();
    }
    
}
