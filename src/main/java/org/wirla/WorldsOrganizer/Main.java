package org.wirla.WorldsOrganizer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main extends Application {

	static boolean debugMode = false;

	static List<File> startFiles = new ArrayList<>();
	static List<TableTab> tables = new ArrayList<>();
	TabPane tabPane;

	private Stage primaryStage;

	public static void main(String[] args) {
		System.out.println("Worlds Organizer v" + Console.getVersion());

		// Iterating through the arguments
		// This is done in a for loop instead of a foreach because we can manipulate
		// and skip over values we are using for arguments.
		for (int a = 0; a < args.length; a++) {
			String arg = args[a];
			switch (arg) {
				default:
					break;
				case "-v":
					debugMode = true;
					Console.sendOutput("Debug Mode enabled.", true);
					break;
				case "-i":
					try {
						File newFile = new File(args[a+1]);
						startFiles.add(newFile);
						a++;
					} catch (Exception e) {
						Console.sendOutput("Invalid File!");
					}
					break;
				case "-h": case "-?":
					Console.sendOutput("Someday, I will make this help output.");
					break;
			}
		}
		launch(args);
	}

	@Override
	public void start(Stage pStage) throws Exception {
		this.primaryStage = pStage;
		primaryStage.setTitle("Worlds Organizer v" + Console.getVersion());
		primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/icon.png")));

		primaryStage.setOnCloseRequest(a -> {
			quit();
		});

		Console.sendOutput("Initialized FX.", true);

		ToolBar toolBar = new ToolBar();

		Button newFileBtn = new Button("New");

		newFileBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/file-plus.svg"))));
		toolBar.getItems().add(newFileBtn);

		Button openFileBtn = new Button("Open");
		openFileBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/folder.svg"))));
		toolBar.getItems().add(openFileBtn);

		Button saveFileBtn = new Button("Save");
		saveFileBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/save.svg"))));
		toolBar.getItems().add(saveFileBtn);

		Button saveAsFileBtn = new Button("Save As");
		saveAsFileBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/save.svg"))));
		toolBar.getItems().add(saveAsFileBtn);

		toolBar.getItems().add(new Separator());

		Button addBtn = new Button("Add");
		addBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/plus.svg"))));
		toolBar.getItems().add(addBtn);

		Button delBtn = new Button("Delete");
		delBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/delete.svg"))));
		toolBar.getItems().add(delBtn);

		Button mupBtn = new Button("Move Up");
		mupBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/chevron-up.svg"))));
		toolBar.getItems().add(mupBtn);

		Button mdwBtn = new Button("Move Down");
		mdwBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/chevron-down.svg"))));
		toolBar.getItems().add(mdwBtn);

		toolBar.getItems().add(new Separator());

		Button quitBtn = new Button("Quit");
		quitBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/delete.svg"))));
		toolBar.getItems().add(quitBtn);

		tabPane = new TabPane();
		VBox.setVgrow(tabPane, Priority.ALWAYS);

		Tab startTab = new Tab("Welcome", new TableTab().getStart());
		startTab.setClosable(false);

		tabPane.getTabs().add(startTab);

		tabPane.addEventFilter(Tab.CLOSED_EVENT, f -> {
			Console.sendOutput("Detected Tab Closed. Index " + (tabPane.getSelectionModel().getSelectedIndex() - 1) + ".", true);
			tables.remove(tabPane.getSelectionModel().getSelectedIndex() - 1);
		});

		VBox vBox = new VBox(toolBar, tabPane);
		Console.sendOutput("Completed Base Window Initialization. If we got this far, shit loads and it's a good time.", true);

		newFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			TableTab tableObj = new TableTab();
			Tab tab = tableObj.getTab(null);

			tabPane.getTabs().add(tab);
			tables.add(tableObj);

			tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
		});

		openFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open File");
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("Gamma Avatars (*.avatars)", "*.avatars"),
					new FileChooser.ExtensionFilter("Gamma WorldsMarks (*.worldsmarks)", "*.worldsmarks")
			);
			File openedFile = fileChooser.showOpenDialog(primaryStage);

			if (openedFile != null) {
				TableTab tableObj = new TableTab();
				Tab tab = tableObj.getTab(openedFile);

				tabPane.getTabs().add(tab);
				tables.add(tableObj);
			} else {
				Console.sendOutput("Error encountered while attempting open. FileDialog closed?", true);
			}
		});

		saveFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			TableTab tableObj = tables.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
			saveFile(tableObj, tableObj.ourFile);

		});

		saveAsFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			TableTab tableObj = tables.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
			saveFile(tableObj);
		});

		/* ---- */

		addBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			int tabIndex = tabPane.getSelectionModel().getSelectedIndex() - 1;

			if (tabIndex >= 0) {
				TableTab tableObj = tables.get(tabIndex);

				tableObj.addValue();
				tableObj.setFocus(tableObj.table.getItems().size());

				tables.set(tabIndex, tableObj);
			}
		});

		delBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			int tabIndex = tabPane.getSelectionModel().getSelectedIndex() - 1;

			if (tabIndex >= 0) {

				TableTab tableObj = tables.get(tabIndex);

				int index = tableObj.table.getSelectionModel().getFocusedIndex();
				tableObj.delValue(index);
				tableObj.setFocus(index < tableObj.table.getItems().size() ? index : index - 1);

				tables.set(tabIndex, tableObj);
			}
		});

		mupBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			int tabIndex = tabPane.getSelectionModel().getSelectedIndex() - 1;

			if (tabIndex >= 0) {
				TableTab tableObj = tables.get(tabIndex);

				int index = tableObj.table.getSelectionModel().getFocusedIndex();
				tableObj.moveValue(index, -1);
				tableObj.setFocus(index - 1);

				tables.set(tabIndex, tableObj);
			}
		});

		mdwBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			int tabIndex = tabPane.getSelectionModel().getSelectedIndex() - 1;

			if (tabIndex >= 0) {
				TableTab tableObj = tables.get(tabIndex);

				int index = tableObj.table.getSelectionModel().getFocusedIndex();
				tableObj.moveValue(index, 1);
				tableObj.setFocus(index + 1);

				tables.set(tabIndex, tableObj);
			}
		});

		quitBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			e.consume();
			quit();
		});

		Scene scene = new Scene(vBox, 960, 600);
		Console.sendOutput("Scene Prepared.", true);
		primaryStage.setScene(scene);
		primaryStage.show();
		Console.sendOutput("Showing Window.", true);
	}

	void saveFile(TableTab table) {
		saveFile(table, null);
	}

	void saveFile(TableTab table, File file) {
		File theFile;
		if (file == null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save As File");
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("Gamma Avatars (*.avatars)", "*.avatars"),
					new FileChooser.ExtensionFilter("Gamma WorldsMarks (*.worldsmarks)", "*.worldsmarks")
			);
			fileChooser.setInitialFileName("gamma");
			theFile = fileChooser.showSaveDialog(primaryStage);

			switch (fileChooser.getSelectedExtensionFilter().getExtensions().get(0)) {
				default:
				case "*.avatars":
					table.dataType = 1;
					break;
				case "*.worldsmarks":
					table.dataType = 2;
					break;
			}
		} else {
			theFile = file;
		}

		try {
			Saver saver = new Saver(theFile);
			saver.save(table.values, table.dataType);
			table.unsaved = false;
		} catch (IOException ea) {
			Console.sendOutput("IOException encountered while attempting save. This isn't supposed to happen.", true);
			showError("An IOException was encountered.", ea.getMessage());
		}
	}

	void quit() {
		boolean askForSure = false;
		for (TableTab t : tables) {
			if (t.unsaved = true) {
				askForSure = true;
				break;
			}
		}

		if (askForSure) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Quit");
			alert.setHeaderText("Are you sure you want to quit?");
			alert.setContentText("You have unsaved changes. Quitting now will lose your progress.");

			ButtonType dontSaveButton = new ButtonType("Discard Changes");
			ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

			alert.getButtonTypes().setAll(dontSaveButton, buttonTypeCancel);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == dontSaveButton) {
				primaryStage.close();
			} else {
				alert.close();
			}
		} else {
			primaryStage.close();
		}
	}

	void showError(String header, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("An Error Occurred");
		alert.setHeaderText(header);
		alert.setContentText(content);

		alert.showAndWait();
	}
}
