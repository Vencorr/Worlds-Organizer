package org.wirla.WorldsOrganizer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
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
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorldsTab {

    File file;

    Tab tab = null;
    Control content = null;
    TableView<WorldList> table = null;
    Pane mainPane;

    private boolean modified = false;

    WorldListObject worldList = new WorldListObject();

    public WorldsTab() {
    }

    public Tab getTab() {
        VBox vibby = new VBox();
        vibby.setAlignment(Pos.CENTER);

        ImageView logoView = new ImageView(new Image(Main.class.getResourceAsStream("/logo.png")));
        Text text1 = new Text("Worlds Organizer v" + Console.getVersion());
        text1.setStyle("-fx-font-size: 20;");

        Text text2 = new Text("Created and Maintained by Wirlaburla");

        Text text3 = new Text("Built on " + Console.getDate());

        vibby.getChildren().addAll(logoView, text1, text2, text3);

        return new Tab("Start Page", vibby);
    }

    public Tab getTab(File file) {
        return getTab(WorldsType.NULL, file);
    }

    public Tab getTab(WorldsType type) {
        return getTab(type, null);
    }

    public Tab getTab(WorldsType type, File file) {
        if (tab != null) {
            return tab;
        } else {
            Restorer restorer;
            if (file != null) {
                this.file = file;
                try {
                    restorer = new Restorer(file);
                    worldList = restorer.read();
                } catch (IOException e) {
                    Dialog.showException(e);
                }
            } else {
                assert type != null;
                worldList.classType = type;
                worldList.add(addToList(type));
            }

            Tab tab;
            switch (worldList.classType) {
                default:
                    tab = new Tab(); break;
                case AVATAR: case WORLDSMARK:
                    tab = new Tab(tabTitle(), getWorldList()); break;
            }
            this.tab = tab;

            setIcon();
            tab.setTooltip(new Tooltip(worldList.classType.name));

            tab.setOnCloseRequest(event -> {
                event.consume();
                quitTab();
            });

            return tab;
        }
    }

    public Pane getWorldList() {
        if (mainPane != null) {
            return mainPane;
        } else {
            content = new TableView<WorldList>();
            ((TableView)content).setEditable(true);

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
            checkBtn.setTooltip(new Tooltip("Link Checker"));
            checkBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/link.svg"))));
            toolBar.getItems().add(checkBtn);

            addBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tables.indexOf(this);

                if (tabIndex >= 0) {
                    this.addValue();
                    this.setFocus(((TableView)content).getItems().size() - 1);

                    Main.tables.set(tabIndex, this);
                }
            });

            delBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tables.indexOf(this);

                if (tabIndex >= 0) {
                    int index = ((TableView)content).getSelectionModel().getFocusedIndex();
                    this.delValue(index);
                    this.setFocus(index < ((TableView)content).getItems().size() ? index : index - 1);

                    Main.tables.set(tabIndex, this);
                }
            });

            mupBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tables.indexOf(this);

                if (tabIndex >= 0) {
                    int index = ((TableView)content).getSelectionModel().getFocusedIndex();
                    this.moveValue(index, -1);
                    this.setFocus(index - 1);

                    Main.tables.set(tabIndex, this);
                }
            });

            mdwBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tables.indexOf(this);

                if (tabIndex >= 0) {
                    int index = ((TableView)content).getSelectionModel().getFocusedIndex();
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

                TableColumn<WorldTableItem, Integer> checkIndexColumn = new TableColumn<>("#");
                checkIndexColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.05));
                checkIndexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));

                TableColumn<WorldTableItem, String> checkLabelColumn = new TableColumn<>("Label");
                checkLabelColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.4));
                checkLabelColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

                TableColumn<WorldTableItem, String> checkValueColumn = new TableColumn<>("Value");
                checkValueColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.4));
                checkValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

                TableColumn<WorldTableItem, String> checkStatusColumn = new TableColumn<>("Status");
                checkStatusColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.1));
                checkStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

                checkTable.getColumns().addAll(checkIndexColumn, checkLabelColumn, checkValueColumn, checkStatusColumn);

                alert.getDialogPane().setContent(checkTable);

                alert.initOwner(Main.primaryStage.getOwner());

                List<WorldTableItem> errorItems = new ArrayList<>();

                Task<Boolean> task = new Task<Boolean>() {
                    @Override public Boolean call() {

                        for (int w = 0; w < ((TableView)content).getItems().size(); w++) {
                            WorldList wl = (WorldList)((TableView)content).getItems().get(w);

                            String value = wl.getValue();
                            int index = ((TableView)content).getItems().indexOf(wl);

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
                    showLinkResults(errorItems);
                });
                task.setOnFailed((a) -> {
                    Dialog.showException(new Exception("Unknown"));
                    alert.close();
                });
                new Thread(task).start();

            });

            TableColumn<WorldList, String> indexColumn = new TableColumn<>("#");
            indexColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.05));
            indexColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(((TableView)content).getItems().indexOf(p.getValue())));
            indexColumn.setSortable(false);
            indexColumn.setEditable(false);

            TableColumn<WorldList, String> labelColumn = new TableColumn<>("Label");
            labelColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.4));
            labelColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            labelColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            labelColumn.setSortable(false);

            labelColumn.setOnEditCommit(t -> {
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setName(t.getNewValue());
                setSaved(false);
            });

            TableColumn<WorldList, String> valueColumn = new TableColumn<>("Value");
            valueColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.525));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            valueColumn.setSortable(false);

            valueColumn.setOnEditCommit(t -> {
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
                setSaved(false);
            });

            for (WorldList list : worldList.getValues()) {
                ((TableView)content).getItems().add(list);
            }

            ((TableView)content).getColumns().addAll(indexColumn, labelColumn, valueColumn);

            HBox.setHgrow(content, Priority.ALWAYS);
            HBox.setHgrow(toolBar, Priority.ALWAYS);

            HBox hBox = new HBox(toolBar, content);
            VBox.setVgrow(hBox, Priority.ALWAYS);

            return hBox;
        }
    }

    public void addValue() {
        assert content instanceof TableView;
        WorldList newData = addToList(worldList.classType);

        worldList.add(newData);
        ((TableView)content).getItems().add(newData);

        setSaved(false);
    }

    public void delValue(int i) {
        assert table != null;

        worldList.remove(i);
        ((TableView)content).getItems().remove(i);

        setSaved(false);
    }

    public void moveValue(int i,int moveBy) {
        assert table != null;

        WorldList row = worldList.get(i);

        worldList.remove(i);
        ((TableView)content).getItems().remove(i);
        worldList.add(i + moveBy, row);
        ((TableView)content).getItems().add(i + moveBy, row);

        setSaved(false);
    }

    public void setFocus(int i) {
        ((TableView)content).getSelectionModel().select(i);
    }

    private void quitTab() {
        if (!getSaved()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Close Tab");
            alert.setHeaderText("Are you sure you want to close this tab?");
            alert.setContentText("You have unsaved changes. Closing now will lose your progress.");

            ButtonType dontSaveButton = new ButtonType("Discard Changes");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(dontSaveButton, buttonTypeCancel);

            alert.getDialogPane().setMinSize(200,200);
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

    private WorldList addToList(WorldsType type) {
        switch (type) {
            default:
                return null;
            case AVATAR:
                return new AvatarObject("New Avatar", "avatar:holden.mov");
            case WORLDSMARK:
                return new MarkObject("New Mark", "home:GroundZero/groundzero.world");
        }
    }

    public void showLinkResults(List<WorldTableItem> list) {
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

    private String tabTitle() {
        if (file != null) {
            return file.getName();
        } else {
            switch (worldList.classType) {
                default:
                    return "Untitled";
                case AVATAR:
                    return "Untitled.avatars";
                case WORLDSMARK:
                    return "Untitled.worldsmarks";
            }
        }
    }
    
}
