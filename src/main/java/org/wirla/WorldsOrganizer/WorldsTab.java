package org.wirla.WorldsOrganizer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WorldsTab {

    File file;

    Tab tab = null;
    Control content = null;
    Pane mainPane;

    private boolean showingFinder = false;
    private boolean modified = false;

    WorldListObject worldList = new WorldListObject();

    private CommandStack commandStack = new CommandStack();

    public WorldsTab() {
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
                worldList.add(createItem(type));
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

            addBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tabs.indexOf(this);

                if (tabIndex >= 0) {
                    this.addItem();
                    this.setFocus(((TableView)content).getItems().size() - 1);

                    Main.tabs.set(tabIndex, this);
                }
            });

            Button delBtn = new Button();
            delBtn.setTooltip(new Tooltip("Delete Value"));
            delBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/delete.svg"))));
            toolBar.getItems().add(delBtn);

            delBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tabs.indexOf(this);

                if (tabIndex >= 0) {
                    int index = ((TableView)content).getSelectionModel().getFocusedIndex();
                    this.deleteItem(index);
                    this.setFocus(index < ((TableView)content).getItems().size() ? index : index - 1);

                    Main.tabs.set(tabIndex, this);
                }
            });

            Button mupBtn = new Button();
            mupBtn.setTooltip(new Tooltip("Move Value Up"));
            mupBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/up.svg"))));
            toolBar.getItems().add(mupBtn);

            mupBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tabs.indexOf(this);

                if (tabIndex >= 0) {
                    int index = ((TableView)content).getSelectionModel().getFocusedIndex();
                    this.moveValue(index, -1);
                    this.setFocus(index - 1);

                    Main.tabs.set(tabIndex, this);
                }
            });

            Button mdwBtn = new Button();
            mdwBtn.setTooltip(new Tooltip("Move Value Down"));
            mdwBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/down.svg"))));
            toolBar.getItems().add(mdwBtn);

            mdwBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tabs.indexOf(this);

                if (tabIndex >= 0) {
                    int index = ((TableView)content).getSelectionModel().getFocusedIndex();
                    this.moveValue(index, 1);
                    this.setFocus(index + 1);

                    Main.tabs.set(tabIndex, this);
                }
            });

            toolBar.getItems().add(new Separator());

            Button findBtn = new Button();
            findBtn.setTooltip(new Tooltip("Find/Replace"));
            findBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/find.svg"))));
            toolBar.getItems().add(findBtn);

            VBox findingBox = getFindPane();
            VBox.setVgrow(findingBox, Priority.ALWAYS);
            findingBox.setVisible(false);
            findingBox.setManaged(false);

            findBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                findingBox.setManaged(!showingFinder);
                findingBox.setVisible(!showingFinder);
                showingFinder = !showingFinder;
            });

            Button checkBtn = new Button();
            checkBtn.setTooltip(new Tooltip("Link Checker"));
            checkBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/link.svg"))));
            toolBar.getItems().add(checkBtn);

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

                AtomicBoolean haltThread = new AtomicBoolean(false);

                Task<Boolean> task = new Task<Boolean>() {
                    @Override public Boolean call() {

                        for (int w = 0; w < ((TableView)content).getItems().size(); w++) {
                            if (haltThread.get()) return true;
                            WorldList wl = (WorldList)((TableView)content).getItems().get(w);

                            String value = wl.getValue();
                            int index = ((TableView)content).getItems().indexOf(wl);

                            WorldTableItem tabItem;
                            if (value.startsWith("http")) {
                                try {
                                    if (!Console.testURL(value)) {
                                        tabItem = new WorldTableItem(index, wl.getName(), value, false);
                                        errorItems.add(tabItem);
                                    } else {
                                        tabItem = new WorldTableItem(index, wl.getName(), value, true);
                                    }
                                    checkTable.getItems().add(0, tabItem);
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                    Dialog.showException(ioException);
                                    return false;
                                }
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
                Thread taskTh = new Thread(task);
                taskTh.start();

                alert.setOnCloseRequest((a) -> {
                    haltThread.set(true);
                    alert.close();
                });

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
                commandStack.doCommand(new Command() {
                    final String oldValue = t.getOldValue();
                    final String newValue = t.getNewValue();
                    WorldList item = t.getTableView().getItems().get(t.getTablePosition().getRow());

                    @Override
                    public void execute() {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setName(newValue);
                        ((TableView) content).refresh();
                        setSaved(false);
                    }

                    @Override
                    public void undo() {
                        item.setName(oldValue);
                        ((TableView)content).refresh();
                        setSaved(false);
                    }
                });
            });

            TableColumn<WorldList, String> valueColumn = new TableColumn<>("Value");
            valueColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.525));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            valueColumn.setSortable(false);

            valueColumn.setOnEditCommit(t -> {
                commandStack.doCommand(new Command() {

                    final String oldValue = t.getOldValue();
                    final String newValue = t.getNewValue();
                    WorldList item = t.getTableView().getItems().get(t.getTablePosition().getRow());

                    @Override
                    public void execute() {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(newValue);
                        ((TableView) content).refresh();
                        setSaved(false);
                    }

                    @Override
                    public void undo() {
                        item.setValue(oldValue);
                        ((TableView)content).refresh();
                        setSaved(false);
                    }
                });
            });

            for (WorldList list : worldList.getValues()) {
                ((TableView)content).getItems().add(list);
            }

            ((TableView)content).getColumns().addAll(indexColumn, labelColumn, valueColumn);

            VBox.setVgrow(content, Priority.ALWAYS);
            HBox.setHgrow(toolBar, Priority.ALWAYS);

            VBox endV = new VBox(content, findingBox);
            HBox.setHgrow(endV, Priority.ALWAYS);

            HBox hBox = new HBox(toolBar, endV);

            return hBox;
        }
    }

    public void addItem() {
        assert content instanceof TableView;

        commandStack.doCommand(new Command() {

            WorldList newData;

            @Override
            public void execute() {
                newData = createItem(worldList.classType);
                worldList.add(newData);
                ((TableView)content).getItems().add(newData);
                setSaved(false);
            }

            @Override
            public void undo() {
                worldList.remove(newData);
                ((TableView)content).getItems().remove(newData);
                setSaved(false);
            }
        });
    }

    public void deleteItem(int i) {
        assert content instanceof TableView;

        commandStack.doCommand(new Command() {

            WorldList item = (WorldList)((TableView)content).getItems().get(i);
            int index = i;

            @Override
            public void execute() {
                worldList.remove(i);
                ((TableView)content).getItems().remove(i);
                setSaved(false);
            }

            @Override
            public void undo() {
                worldList.add(index, item);
                ((TableView)content).getItems().add(index, item);
                setSaved(false);
            }
        });
    }

    public void moveValue(int i, int moveBy) {
        assert content instanceof TableView;

        commandStack.doCommand(new Command() {

            WorldList item = (WorldList)((TableView)content).getItems().get(i);
            WorldList rItem = (WorldList)((TableView)content).getItems().get(i + moveBy);
            int index = i;
            int moved = moveBy;

            @Override
            public void execute() {
                doMove(i, rItem);
                doMove(i + moveBy, item);
                setSaved(false);
            }

            @Override
            public void undo() {
                doMove(index + moved, rItem);
                doMove(index, item);
                setSaved(false);
            }

            private void doMove(int i, WorldList o) {
                ((TableView)content).getItems().set(i, o);
                worldList.set(i, o);
            }
        });


    }

    public void setFocus(int i) {
        ((TableView)content).getSelectionModel().select(i);
        ((TableView)content).scrollTo(i);
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
        Main.tabs.remove(index);

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

    public WorldList createItem(WorldsType type) {
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
        AtomicInteger addition = new AtomicInteger();
        addition.set(0);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setMinWidth(800);
        alert.setResizable(true);
        alert.setTitle("Link Checker Results");
        alert.setHeaderText("The followings links have been found to be dead: " + list.size() + " out of " + worldList.size());

        Button deleteBtn = new Button();
        deleteBtn.setTooltip(new Tooltip("Delete"));
        deleteBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/delete.svg"))));
        Button deleteAllBtn = new Button("Delete All");
        deleteAllBtn.setTooltip(new Tooltip("Delete All"));
        deleteAllBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/delete-all.svg"))));

        TableView<WorldTableItem> errorTable = new TableView<>();
        errorTable.setEditable(true);

        errorTable.setMaxWidth(Double.MAX_VALUE);
        errorTable.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(errorTable, Priority.ALWAYS);
        GridPane.setHgrow(errorTable, Priority.ALWAYS);

        TableColumn<WorldTableItem, Integer> indexColumn = new TableColumn<>("#");
        indexColumn.prefWidthProperty().bind(errorTable.widthProperty().multiply(0.05));
        indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
        indexColumn.setEditable(false);

        TableColumn<WorldTableItem, String> labelColumn = new TableColumn<>("Label");
        labelColumn.prefWidthProperty().bind(errorTable.widthProperty().multiply(0.4));
        labelColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        labelColumn.setCellFactory(TextFieldTableCell.<WorldTableItem>forTableColumn());
        labelColumn.setEditable(true);
        labelColumn.setOnEditCommit(t -> {
            commandStack.doCommand(
                    new Command() {

                        String oldValue = t.getOldValue();
                        String newValue = t.getNewValue();
                        WorldList item = t.getTableView().getItems().get(t.getTablePosition().getRow());
                        WorldList tableItem = ((WorldList) ((TableView) content).getItems().get(t.getRowValue().getIndex()));

                        @Override
                        public void execute() {
                            item.setName(newValue);
                            tableItem.setName(newValue);
                            ((TableView) content).refresh();
                            setSaved(false);
                        }

                        @Override
                        public void undo() {
                            item.setName(oldValue);
                            tableItem.setName(oldValue);
                            ((TableView) content).refresh();
                            setSaved(false);
                        }
                    });
        });

        TableColumn<WorldTableItem, String> valueColumn = new TableColumn<>("Value");
        valueColumn.prefWidthProperty().bind(errorTable.widthProperty().multiply(0.525));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory(TextFieldTableCell.<WorldTableItem>forTableColumn());
        valueColumn.setEditable(true);
        valueColumn.setOnEditCommit(t -> {
            commandStack.doCommand(
                    new Command() {

                        String oldValue = t.getOldValue();
                        String newValue = t.getNewValue();
                        WorldList item = t.getTableView().getItems().get(t.getTablePosition().getRow());
                        WorldList tableItem = ((WorldList) ((TableView) content).getItems().get(t.getRowValue().getIndex()));

                        @Override
                        public void execute() {
                            item.setValue(newValue);
                            tableItem.setValue(newValue);
                            ((TableView) content).refresh();
                            setSaved(false);
                        }

                        @Override
                        public void undo() {
                            item.setValue(oldValue);
                            tableItem.setValue(oldValue);
                            ((TableView) content).refresh();
                            setSaved(false);
                        }
                    });
        });

        for (WorldTableItem item : list) {
            errorTable.getItems().add(item);
        }


        ToolBar btnBar = new ToolBar();
        btnBar.getItems().addAll(deleteBtn, deleteAllBtn);

        errorTable.getColumns().addAll(indexColumn, labelColumn, valueColumn);

        VBox vBox = new VBox(errorTable, btnBar);

        alert.getDialogPane().setContent(vBox);

        deleteBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> { commandStack.doCommand(
            new Command() {

                int selected = errorTable.getSelectionModel().getSelectedIndex();
                int tableSel = errorTable.getItems().get(selected).getIndex();
                WorldTableItem item = errorTable.getItems().get(selected);
                int added = tableSel + addition.get();

                @Override
                public void execute() {
                    worldList.remove(tableSel + addition.get());
                    ((TableView)content).getItems().remove(tableSel + addition.get());

                    errorTable.getItems().remove(selected);
                    list.remove(selected);
                    addition.getAndDecrement();
                    setSaved(false);
                }

                @Override
                public void undo() {
                    worldList.add(added, item);
                    ((TableView)content).getItems().add(added, item);

                    errorTable.getItems().add(selected, item);
                    list.add(selected, item);
                    addition.getAndIncrement();
                    setSaved(false);
                }
            });
        });

        deleteAllBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            Alert askSure = new Alert(Alert.AlertType.CONFIRMATION);
            askSure.setTitle("Delete All?");
            askSure.setHeaderText("Are you sure you want to delete all the items in this list?");
            askSure.setContentText("You won't be able to undo this change!");

            ButtonType delSureButton = new ButtonType("Discard Changes");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            askSure.getButtonTypes().setAll(delSureButton, buttonTypeCancel);

            askSure.getDialogPane().setMinSize(200,200);
            Optional<ButtonType> result = askSure.showAndWait();
            if (result.get() == delSureButton) {
                for (WorldTableItem item : list) {
                    errorTable.getItems().remove(0);
                    worldList.remove(item.getIndex() + addition.get());
                    ((TableView) content).getItems().remove(item.getIndex() + addition.get());
                    addition.getAndDecrement();
                }
                setSaved(false);
            } else {
                askSure.close();
            }
        });

        alert.showAndWait();
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

    private VBox getFindPane() {
        Text findText = new Text("Find Text");
        findText.setFont(Font.font("Verdana", FontWeight.NORMAL, FontPosture.REGULAR, 12));
        TextField findInput = new TextField();

        Text replText = new Text("Replace Text");
        replText.setFont(Font.font("Verdana", FontWeight.NORMAL, FontPosture.REGULAR, 12));
        TextField replInput = new TextField();

        Slider selSlider = new Slider(-1,1,0);
        selSlider.setMajorTickUnit(1);
        selSlider.setMinorTickCount(0);
        selSlider.setSnapToTicks(true);
        Text labelTxt = new Text("Label");
        Text valueTxt = new Text("Value");

        HBox.setHgrow(selSlider, Priority.ALWAYS);
        HBox.setHgrow(labelTxt, Priority.ALWAYS);
        HBox.setHgrow(valueTxt, Priority.ALWAYS);

        GridPane findBar = new GridPane();

        Button findButton = new Button("Find");
        findButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            int curI = ((TableView)content).getSelectionModel().getSelectedIndex();
            if (curI < 0) curI = 0;
            for (int a = curI+1; a < worldList.size(); a++) {
                if (worldList.get(a).getName().contains(findInput.getCharacters()) || worldList.get(a).getValue().contains(findInput.getCharacters())) {
                    setFocus(a);
                    break;
                }
            }
        });

        Button replButton = new Button("Replace");
        replButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            commandStack.doCommand(new Command() {

                WorldList origItem;
                WorldList item;
                int index;

                @Override
                public void execute() {
                    item = origItem = (WorldList)((TableView)content).getSelectionModel().getSelectedItem();
                    index = ((TableView)content).getSelectionModel().getSelectedIndex();
                    if (item != null) {
                        if (selSlider.getValue() >= 0) item.setValue(item.getValue().replace(findInput.getCharacters(), replInput.getCharacters()));
                        if (selSlider.getValue() <= 0) item.setName(item.getName().replace(findInput.getCharacters(), replInput.getCharacters()));
                    }
                    ((TableView) content).refresh();
                    setSaved(false);
                }

                @Override
                public void undo() {
                    item = origItem;
                    ((TableView) content).refresh();
                    setSaved(false);
                }
            });
        });

        Button replAllButton = new Button("Replace All");
        replAllButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            commandStack.doCommand(new Command() {

                final List<WorldTableItem> replaced = new ArrayList<>();
                final double slider = selSlider.getValue();

                @Override
                public void execute() {
                    for (WorldList item : worldList.getValues()) {
                        if (item.getName().contains(findInput.getCharacters()) || item.getValue().contains(findInput.getCharacters())) {
                            WorldTableItem tableItem = new WorldTableItem(((TableView)content).getItems().indexOf(item), item.getName(), item.getValue());
                            if (selSlider.getValue() >= 0) item.setValue(item.getValue().replace(findInput.getCharacters(), replInput.getCharacters()));
                            if (selSlider.getValue() <= 0) item.setName(item.getName().replace(findInput.getCharacters(), replInput.getCharacters()));
                            replaced.add(tableItem);
                        }
                    }
                    ((TableView)content).refresh();
                    setSaved(false);
                }

                @Override
                public void undo() {
                    for (WorldTableItem tableItem : replaced) {
                        WorldList item = (WorldList)((TableView)content).getItems().get(tableItem.getIndex());
                        if (slider >= 0) item.setValue(tableItem.getValue());
                        if (slider <= 0) item.setName(tableItem.getName());
                    }
                    ((TableView) content).refresh();
                    setSaved(false);
                }
            });
        });

        ButtonBar btns = new ButtonBar();
        btns.getButtons().addAll(findButton, replButton, replAllButton);
        GridPane.setColumnSpan(btns, 2);

        findBar.add(findText, 0, 0);
        findBar.add(findInput, 1, 0);

        findBar.add(replText, 0, 1);
        findBar.add(replInput, 1, 1);

        findBar.add(new HBox(labelTxt, selSlider, valueTxt), 0, 2);
        findBar.add(btns, 1, 2);

        GridPane.setHgrow(findInput, Priority.ALWAYS);
        GridPane.setHgrow(replInput, Priority.ALWAYS);
        GridPane.setHgrow(btns, Priority.ALWAYS);
        findBar.setHgap(5);
        findBar.setVgap(5);

        findBar.setPadding(new Insets(10, 10, 10, 10));
        VBox.setVgrow(findBar, Priority.ALWAYS);

        return new VBox(findBar);
    }

    public void doUndo() {
        if (commandStack.canUndo()) commandStack.undo();
        else Console.sendOutput("Nothing to undo!");
    }

    public void doRedo() {
        if (commandStack.canRedo()) commandStack.redo();
        else Console.sendOutput("Nothing to redo!");
    }
}