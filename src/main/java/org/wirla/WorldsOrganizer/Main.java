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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main extends Application {

	static boolean debugMode = false;

	static List<File> startFiles = new ArrayList<>();
	static List<WorldsTab> tables = new ArrayList<>();
	TabPane tabPane;

	public static Stage primaryStage;

	public static void main(String[] args) {
		Console.sendOutput("Worlds Organizer v" + Console.getVersion());

		// Iterating through the arguments
		// This is done in a for loop instead of a foreach because we can manipulate
		// and skip over values we are using for arguments.
		for (int a = 0; a < args.length; a++) {
			String arg = args[a];
			switch (arg) {
				default:
					Console.sendOutput( arg + " is not a valid argument. Please use '-help' to see a list of options and arguments." );
					break;
				case "-v": case "--verbose":
					debugMode = true;
					Console.sendOutput("Verbose is enabled.", true);
					break;
				case "-i": case "--input":
					try {
						File newFile = new File(args[a+1]);
						startFiles.add(newFile);
						a++;
					} catch (Exception e) {
						Console.sendOutput("Invalid File!");
					}
					break;
				case "-h": case "--help":
					Console.getHelp();
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

		Console.sendOutput("JavaFX Successfully Initialized", true);


		// MenuBar
		// Handles main operations not associated with the inner tabs.
		ToolBar menuBar = new ToolBar();

		Button newFileBtn = new Button("New");
		newFileBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/file-plus.svg"))));
		menuBar.getItems().add(newFileBtn);

		Button openFileBtn = new Button("Open");
		openFileBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/folder.svg"))));
		menuBar.getItems().add(openFileBtn);

		Button saveFileBtn = new Button("Save");
		saveFileBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/save.svg"))));
		menuBar.getItems().add(saveFileBtn);

		Button saveAsFileBtn = new Button("Save As");
		saveAsFileBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/save.svg"))));
		menuBar.getItems().add(saveAsFileBtn);

		menuBar.getItems().add(new Separator());

		Button quitBtn = new Button("Quit");
		quitBtn.setGraphic(new ImageView(IMGTranscoder.toFXImage(Main.class.getResourceAsStream("/icons/quit.svg"))));
		menuBar.getItems().add(quitBtn);

		// TabPane initialization
		tabPane = new TabPane();

		Tab startTab = new WorldsTab().getTab();
		startTab.setClosable(false);

		tabPane.getTabs().add(startTab);

		tabPane.addEventFilter(Tab.CLOSED_EVENT, f -> {
			Console.sendOutput("Detected Tab Closed. Index " + (tabPane.getSelectionModel().getSelectedIndex() - 1) + ".", true);
			tables.remove(tabPane.getSelectionModel().getSelectedIndex() - 1);
		});

		VBox vBox = new VBox(menuBar, tabPane);
		Console.sendOutput("Completed Base Window Initialization", true);

		newFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			newFile();
		});

		openFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			openFile(null);
		});

		saveFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			WorldsTab tableObj = tables.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
			saveFile(tableObj, tableObj.file);

		});

		saveAsFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			WorldsTab tableObj = tables.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
			saveFile(tableObj);
		});

		/* ---- */

		quitBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			e.consume();
			quit();
		});

		VBox.setVgrow(tabPane, Priority.ALWAYS);

		Scene scene = new Scene(vBox, 960, 600);
		Console.sendOutput("Scene Prepared.", true);
		primaryStage.setScene(scene);
		primaryStage.show();
		Console.sendOutput("Showing Window...", true);

		Console.sendOutput("Iterating through start files.", true);
		try {
			for (File start : startFiles) {
				openFile(start);
			}
		} catch (Exception e) {
			Console.sendOutput("No start files to iterate through!", true);
		}

		update();
	}

	void saveFile(WorldsTab table) {
		saveFile(table, null);
	}

	void saveFile(WorldsTab tab, File file) {
		File thisFile;
		if (file == null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save As File");
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("Gamma Avatars (*.avatars)", "*.avatars"),
					new FileChooser.ExtensionFilter("Gamma WorldsMarks (*.worldsmarks)", "*.worldsmarks")
			);
			fileChooser.setInitialFileName("gamma");
			thisFile = fileChooser.showSaveDialog(primaryStage);

			if (thisFile != null) {
				switch (fileChooser.getSelectedExtensionFilter().getExtensions().get(0)) {
					default:
					case "*.avatars":
						tab.worldList.classType = WorldsType.AVATAR;
						break;
					case "*.worldsmarks":
						tab.worldList.classType = WorldsType.WORLDSMARK;
						break;
				}
			}
		} else {
			thisFile = file;
		}

		if (thisFile != null) {
			try {
				Saver saver = new Saver(thisFile);
				saver.save(tab.worldList);
				tab.setSaved(true);
			} catch (IOException e) {
				e.printStackTrace();
				Dialog.showException(e);
			}
		}
	}

	void quit() {
		boolean askForSure = false;
		for (WorldsTab t : tables) {
			if (!t.getSaved()) {
				askForSure = true;
				break;
			}
		}

		if (askForSure) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.getDialogPane().setMinSize(200,200);
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

	void newFile() {
		WorldsTab tableObj = new WorldsTab();
		WorldsType newType = Dialog.newFile();
		if (newType != WorldsType.NULL) {
			Tab tab = tableObj.getTab(newType);

			tabPane.getTabs().add(tab);
			tables.add(tableObj);

			tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
		}
	}

	void openFile(File file) {
		File openedFile;
		if (file == null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open File");
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("Gamma Avatars (*.avatars)", "*.avatars"),
					new FileChooser.ExtensionFilter("Gamma WorldsMarks (*.worldsmarks)", "*.worldsmarks")
			);
			openedFile = fileChooser.showOpenDialog(primaryStage);
		} else {
			openedFile = file;
		}

		if (openedFile != null) {
			WorldsTab tableObj = new WorldsTab();
			Tab tab = tableObj.getTab(openedFile);

			tabPane.getTabs().add(tab);
			tables.add(tableObj);

			tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
		} else {
			Console.sendOutput("Error encountered while attempting open. FileDialog closed?", true);
		}
	}

	public void update() {
		Version curVer = new Version(Console.getVersion());
		try {
			URL url = new URL("https://wirlaburla.com/WorldsOrganizer/dw/ver.txt");
			Scanner s = new Scanner(url.openStream());
			Version newVer = new Version(s.next());
			if (newVer.compareTo(curVer) >= 1) {
				Console.sendOutput("Update available! " + curVer.get() + " < " + newVer.get());
				Dialog.showUpdate(newVer);
			}
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
