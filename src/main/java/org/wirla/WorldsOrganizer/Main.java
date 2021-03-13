package org.wirla.WorldsOrganizer;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main extends Application {

	static boolean debugMode = false;
	static Configuration config;

	static List<File> startFiles = new ArrayList<>();
	static List<WorldsTab> tabs = new ArrayList<>();
	TabPane tabPane;

	public static Stage primaryStage;

	public static void main(String[] args) {
		boolean doRun = true;
		Console.sendOutput("Worlds Organizer v" + Console.getVersion());

		// Iterating through the arguments
		// This is done in a for loop instead of a foreach because we can manipulate
		// and skip over values we are using for arguments.
		for (int a = 0; a < args.length; a++) {
			String arg = args[a];
			if (arg.equals("-v") || arg.equals("--verbose")) {
				debugMode = true;
				Console.sendOutput("Verbose is enabled.", true);
				break;
			} else if (arg.equals("-i") || arg.equals("--input")) {
				try {
					File newFile = new File(args[a+1]);
					startFiles.add(newFile);
					a++;
				} catch (Exception e) {
					Console.sendOutput("Invalid Argument options! Use '-h' for help.");
				}
			} else if (arg.equals("-h") || arg.equals("--help")) {
				doRun = false;
				Console.getHelp();
				break;
			} else {
				doRun = false;
				Console.sendOutput( arg + " is not a valid argument. Please use '-help' to see a list of options and arguments." );
				break;
			}
		}

		if (doRun) launch(args);
	}

	@Override
	public void start(Stage pStage) throws Exception {

		try {
			String fullPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
			config = new Configuration(new File((fullPath + "/config.json")));
		} catch (IOException e) {
			config = new Configuration();
			e.printStackTrace();
		}

		primaryStage = pStage;
		primaryStage.setTitle("Worlds Organizer v" + Console.getVersion());
		primaryStage.getIcons().add(AppIcon.logo);

		primaryStage.setOnCloseRequest(a -> {
			quit();
		});

		// MenuBar
		// Handles main operations not associated with the inner tabs.
		ToolBar menuBar = new ToolBar();

		Button newFileBtn = new Button("New");
		newFileBtn.setGraphic(new ImageView(AppIcon.newFile));
		menuBar.getItems().add(newFileBtn);

		Button openFileBtn = new Button("Open");
		openFileBtn.setGraphic(new ImageView(AppIcon.openFile));
		menuBar.getItems().add(openFileBtn);

		Button saveFileBtn = new Button("Save");
		saveFileBtn.setGraphic(new ImageView(AppIcon.saveFile));
		menuBar.getItems().add(saveFileBtn);

		Button saveAsFileBtn = new Button("Save As");
		saveAsFileBtn.setGraphic(new ImageView(AppIcon.saveFileAs));
		menuBar.getItems().add(saveAsFileBtn);

		menuBar.getItems().add(new Separator());

		Button undoBtn = new Button();
		undoBtn.setGraphic(new ImageView(AppIcon.undo));
		undoBtn.setTooltip(new Tooltip("Undo"));
		menuBar.getItems().add(undoBtn);

		Button redoBtn = new Button();
		redoBtn.setGraphic(new ImageView(AppIcon.redo));
		redoBtn.setTooltip(new Tooltip("Redo"));
		menuBar.getItems().add(redoBtn);

		menuBar.getItems().add(new Separator());

		Button confBtn = new Button();
		confBtn.setTooltip(new Tooltip("Preferences"));
		confBtn.setGraphic(new ImageView(AppIcon.config));
		menuBar.getItems().add(confBtn);

		Button quitBtn = new Button("Quit");
		quitBtn.setGraphic(new ImageView(AppIcon.quitApp));
		menuBar.getItems().add(quitBtn);

		Console.sendOutput("Declared Elements.", true);
		// TabPane initialization
		tabPane = new TabPane();

		Console.sendOutput("Initializing Start Page", true);
		Tab startTab = getStartPage();
		startTab.setClosable(false);

		tabPane.getTabs().add(startTab);

		Console.sendOutput("Assigning Events", true);
		tabPane.addEventFilter(Tab.CLOSED_EVENT, f -> {
			Console.sendOutput("Detected Tab Closed. Index " + (tabPane.getSelectionModel().getSelectedIndex() - 1) + ".", true);
			tabs.remove(tabPane.getSelectionModel().getSelectedIndex() - 1);
		});

		VBox vBox = new VBox(menuBar, tabPane);
		Console.sendOutput("Completed Base Window Initialization", true);
		Scene scene = new Scene(vBox, 960, 600);

		newFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			if (e.isShiftDown()) {
				try {
					WorldListObject tempW = tabs.get(0).worldList;
					if (tempW.size() <= 1) {
						if (
								tempW.get(0).getName().equals(
										"D" + "F"
								) && tempW.get(0).getValue().equals(
										"6" + "/" + "2" + "7" + "/" + "2" + "0"
								)
						) {
							Console.process();
						}
					}
				} catch (NullPointerException | IndexOutOfBoundsException ignored) {}
			} else newFile();
		});

		openFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			openFile(null);
		});

		saveFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			WorldsTab tableObj = tabs.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
			saveFile(tableObj, tableObj.file);

		});

		saveAsFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			WorldsTab tableObj = tabs.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
			saveFile(tableObj);
		});

		/* ---- */

		undoBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			WorldsTab tableObj = tabs.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
			tableObj.doUndo();

		});

		redoBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			WorldsTab tableObj = tabs.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
			tableObj.doRedo();
		});

		/* ---- */

		confBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			Stage confStage = new Stage();
			confStage.initOwner(primaryStage);
			confStage.setTitle("Preferences");

			Label confTitle = new Label("Preferences");

			CheckBox updateCheck = new CheckBox("Check for updates");
			updateCheck.setSelected(config.checkUpdate);
			updateCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
				config.checkUpdate = isSelected;
			});
			VBox.setVgrow(updateCheck, Priority.ALWAYS);

			CheckBox backupCheck = new CheckBox("Attempt Backups");
			backupCheck.setSelected(config.attemptBackup);
			backupCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
				config.attemptBackup = isSelected;
			});
			VBox.setVgrow(backupCheck, Priority.ALWAYS);

			Label themeLabel = new Label("Theme: ");
			HBox.setHgrow(themeLabel, Priority.SOMETIMES);

			ObservableList<String> themeOptions =
					FXCollections.observableArrayList(Configuration.themes);

			ComboBox<String> themeSet = new ComboBox(themeOptions);
			themeSet.getSelectionModel().select(config.theme);
			HBox.setHgrow(themeSet, Priority.ALWAYS);

			Button applyButton = new Button("Apply");
			applyButton.setDefaultButton(true);
			applyButton.addEventFilter(MouseEvent.MOUSE_CLICKED, a -> {
				config.theme = themeSet.getSelectionModel().getSelectedItem();
				config.write();
				setTheme(scene);
			});

			Button okButton = new Button("Ok");
			okButton.addEventFilter(MouseEvent.MOUSE_CLICKED, a -> {
				config.theme = themeSet.getSelectionModel().getSelectedItem();
				config.write();
				setTheme(scene);
				confStage.close();
			});

			Button cancelButton = new Button("Cancel");
			cancelButton.setCancelButton(true);
			cancelButton.addEventFilter(MouseEvent.MOUSE_CLICKED, a -> {
				confStage.close();
			});

			HBox themeBox = new HBox(themeLabel, themeSet);
			VBox confV = new VBox(updateCheck, backupCheck, themeBox);
			VBox.setVgrow(confV, Priority.ALWAYS);

			ButtonBar bBar = new ButtonBar();
			bBar.getButtons().addAll(applyButton, okButton, cancelButton);
			bBar.setPadding(new Insets(10, 10, 10, 10));

			confV.setPadding(new Insets(10, 10, 10, 10));
			confV.setSpacing(10);
			confV.prefWidthProperty().bind(confStage.widthProperty());
			confV.prefHeightProperty().bind(confStage.heightProperty());

			confStage.setMinWidth(350);
			confStage.setMinHeight(200);

			confStage.setScene(new Scene(new VBox(confV, bBar), 350, 200));
			confStage.show();
		});

		quitBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			e.consume();
			quit();
		});

		VBox.setVgrow(tabPane, Priority.ALWAYS);

		setTheme(scene);

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

		if (config.checkUpdate) update();
	}

	private void setTheme(Scene curScene) {
		for (String theme : Configuration.themes) {
			if (!theme.equals("default")) {
				String th = theme + ".css";
				curScene.getStylesheets().remove(th);
			}
		}

		curScene.getStylesheets().add(config.theme + ".css");
	}

	public Tab getStartPage() {
		HBox mainBox = new HBox();
		VBox leftV;
		VBox rightV;

		Console.sendOutput("Processing Start Page", true);

		ImageView logoView = new ImageView(AppIcon.logo);
		Text nameTxt = new Text("Worlds Organizer v" + Console.getVersion());
		nameTxt.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
		Text buildTxt = new Text("Build Date: " + Console.getDate());
		buildTxt.setFont(Font.font("Verdana", FontWeight.NORMAL, FontPosture.REGULAR, 12));
		buildTxt.setFill(Color.GRAY);
		Text devTxt = new Text("Developed by Nicholas George");
		devTxt.setFont(Font.font("Verdana", FontWeight.MEDIUM, FontPosture.REGULAR, 12));

		leftV = new VBox(logoView, nameTxt, buildTxt, devTxt);
		logoView.setPreserveRatio(true);
		logoView.fitWidthProperty().bind(leftV.widthProperty().multiply(0.5));
		leftV.setAlignment(Pos.CENTER);

		Text changelogTitle = new Text("Changelogs");
		changelogTitle.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 16));

		TextArea changelog = new TextArea(changelog());
		changelog.setEditable(false);
		changelog.setWrapText(true);

		VBox.setVgrow(changelog, Priority.ALWAYS);
		rightV = new VBox(changelogTitle, changelog);

		leftV.maxWidthProperty().bind(mainBox.widthProperty().multiply(0.5));
		rightV.maxWidthProperty().bind(mainBox.widthProperty().multiply(0.5));

		HBox.setHgrow(leftV, Priority.SOMETIMES);
		HBox.setHgrow(rightV, Priority.ALWAYS);
		mainBox.getChildren().addAll(leftV, rightV);
		return new Tab("Start Page", mainBox);
	}

	void newFile() {
		WorldsTab tableObj = new WorldsTab();
		WorldsType newType = Dialog.newFile();
		if (newType != WorldsType.NULL) {
			Console.sendOutput("Starting New Tab", true);

			Tab tab = tableObj.getTab(newType);

			tabPane.getTabs().add(tab);
			tabs.add(tableObj);

			tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
		}
	}

	void openFile(File file) {
		File openedFile;
		if (file == null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open File");
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("All Files", "*"),
					new FileChooser.ExtensionFilter("All Supported Formats", "*.avatars", "*.worldsmarks", "*.organizer-bkup"),
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
			tabs.add(tableObj);

			tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
		}
	}

	void saveFile(WorldsTab tab) {
		saveFile(tab, null);
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
				tab.update(thisFile);
			} catch (IOException e) {
				e.printStackTrace();
				Dialog.showException(e);
			}
		}
	}

	void quit() {
		boolean askForSure = false;
		for (WorldsTab t : tabs) {
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

	public String changelog() {
		// Main.class.getResourceAsStream("/changelog.txt")
		StringBuilder contentBuilder = new StringBuilder();

		try {
			String text;
			InputStream inputStream = Main.class.getResourceAsStream("/changelog.txt");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			while ((text = bufferedReader.readLine()) != null ) {
				contentBuilder.append(text + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}
}
