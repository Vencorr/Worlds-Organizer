package org.wirla.WorldsOrganizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

	static Display display;
	static Shell shell;

	static List<ListTab> valuePages = new ArrayList<>();

	static CTabFolder ctf;

	public static void main(String[] args) {
		System.out.println("Running version " + Detail.getVersion());
		initWindow();
	}

	static void initWindow() {
		display = new Display();
		shell = new Shell(display);

		// Detect on close button
		// Don't want any unsaved work, do we?
		shell.addListener(SWT.Close, event -> {
			int unsaved = 0;
			for (ListTab vp : valuePages) {
				if (vp.hasChanged) unsaved++;
			}
			if (unsaved > 0) {
				event.doit = false;
				MessageBox mesB = new MessageBox(shell, SWT.NONE | SWT.OK | SWT.CANCEL);
				mesB.setMessage("Are you sure you want to quit? All unsaved work will be lost!");
				int status = mesB.open();
				switch (status) {
					case SWT.OK:
						event.doit = true;
						break;
					case SWT.CANCEL:
						event.doit = false;
						break;
				}
			}
		});


		FillLayout fl = new FillLayout();
		fl.type = SWT.VERTICAL;
		shell.setLayout(fl);

		Image icon = new Image(display, Main.class.getClassLoader().getResourceAsStream("resources/icon.png"));

		// Main Window Details
		shell.setText("Worlds Organizer");
		shell.setImage(icon);
		shell.setSize(600, 400);
		shell.setMinimumSize(600, 400);

		// Menu
		Menu menuBar = new Menu(shell, SWT.BAR);

		Menu fileMenu = new Menu(menuBar);
		Menu helpMenu = new Menu(menuBar);

		MenuItem fileItem = new MenuItem(menuBar, SWT.CASCADE);
		fileItem.setText("File");
		fileItem.setMenu(fileMenu);

		MenuItem helpItem = new MenuItem(menuBar, SWT.CASCADE);
		helpItem.setText("Help");
		helpItem.setMenu(helpMenu);

		MenuItem aboutItem = new MenuItem(helpMenu, SWT.NONE);
		aboutItem.setText("About");

		aboutItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				new AboutDialog(display);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {

			}
		});


		MenuItem newItem = new MenuItem(fileMenu, SWT.NONE);
		newItem.setText("New");

		newItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) { newItem(); }

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {
			}
		});




		MenuItem openItem = new MenuItem(fileMenu, SWT.NONE);
		openItem.setText("Open...");

		openItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {
			}
		});

		MenuItem saveItem = new MenuItem(fileMenu, SWT.NONE);
		saveItem.setText("Save");

		saveItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				try {
					int index = ctf.getSelectionIndex();
					ListTab curPage = valuePages.get(index);
					if (curPage.isFile) {
						if (curPage.type.equals("NET.worlds.console.SavedAvMenuItem"))
							new Saver(curPage.path).saveAvatars(curPage.openedList);
						else if (curPage.type.equals("NET.worlds.console.BookmarkMenuItem"))
							new Saver(curPage.path).saveMark(curPage.openedList);
						else throw new InvalidPersisterFile();
					} else {
						save();
					}
					curPage.hasChanged = false;
				} catch (IOException e) {
					error("Unable to read/write file! Permissions problem?", SWT.ICON_ERROR);
				} catch (InvalidPersisterFile e) {
					error("Invalid Persister File! Organizer does not support this format!", SWT.ICON_ERROR);
				} catch (NullPointerException e) {
					error("An error occurred attempting to save.", SWT.ICON_ERROR);
				} catch (ArrayIndexOutOfBoundsException e) {
					// I do this because this is thrown if the button is pressed with no tab.
					System.out.println("An ArrayIndexOutOfBoundsException was thrown and caught during saveItem. Ignoring.");
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {
			}
		});


		MenuItem saveAsItem = new MenuItem(fileMenu, SWT.NONE);
		saveAsItem.setText("Save As...");

		saveAsItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				save();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {
			}
		});


		// Setup Tab System
		ctf = new CTabFolder(shell, SWT.DEFAULT);

		// Drag 'n' Drop
		DropTarget dt = new DropTarget(ctf, DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
		dt.setTransfer(new Transfer[]{FileTransfer.getInstance()});
		try {
			dt.addDropListener(new DropTargetAdapter() {
				public void drop(DropTargetEvent event) {
					FileTransfer ft = FileTransfer.getInstance();
					if (ft.isSupportedType(event.currentDataType)) {
						for (String a : (String[]) event.data) {
							openFile(a);
						}
					}
				}
			});
		} catch (NullPointerException e) {
			error("Unable to read/write file! Permissions problem?", SWT.ICON_ERROR);
		}

		// Finalize and open the shell
		shell.setMenuBar(menuBar);
		shell.open();

		// Main Loop
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// Disabling Save/Save as if no tab is selected
				if (ctf.getSelectionIndex() < 0) {
					saveItem.setEnabled(false);
					saveAsItem.setEnabled(false);
				} else {
					saveItem.setEnabled(true);
					saveAsItem.setEnabled(true);
				}

				for (ListTab vp : valuePages) {
					try {
						if (!vp.isOriginal()) vp.hasChanged = true;

						// Save Icon
						// Let's loop through everything, shall we?
						if (vp.hasChanged && vp.tab.getImage() == null) {
							vp.tab.setImage(new Image(ctf.getDisplay(), Main.class.getClassLoader().getResourceAsStream("resources/save.png")));
						} else if (!vp.hasChanged && vp.tab.getImage() != null) {
							vp.tab.setImage(null);
						}

						if (ctf.getSelection() == vp.tab) {
							saveItem.setEnabled(vp.isWritable());
						}

						// This is probably absolutely terrible and most likely there is a better way to do this
						// without having every frame resize everything. It's a small program though so it shouldn't
						// come with much of a performance hit.
						// If I don't do this, the sizes are all wrong no matter where I set them on setSize().
						// Even when you don't resize the window.
						vp.list.setSize(vp.value.getSize().x, 48);
						vp.value.setSize(192, vp.value.getParent().getSize().y - vp.list.getSize().y);
						vp.list.setLocation(vp.list.getLocation().x, vp.value.getSize().y);
						vp.properties.setSize((ctf.getSize().x - vp.value.getSize().x) - 4, vp.properties.getSize().y);
						vp.properties.setLocation(vp.value.getSize().x + 4, vp.properties.getParent().getLocation().y);
					} catch (NullPointerException e) {
						int index = valuePages.indexOf(vp);
						System.out.println("An error occurred during the pages main loop. Removing faulty page.");
						valuePages.remove(vp);
						ctf.getItem(index).dispose();
						break;
					}
				}
			}
		}
		display.dispose();
	}

	// Removing any leftovers. Logging in the output so it's debugging, right?
	void TabListener(ListTab vp) {
		try {
			vp.tab.addDisposeListener(disposeEvent -> valuePages.remove(vp));
		} catch (NullPointerException e) {
			System.out.println("Tab is null. Not applying listener.");
		}
	}


	// I do this to avoid having to copy and paste this code many times.
	static void error(String message, int icon) {
		MessageBox mesB = new MessageBox(shell, icon);
		mesB.setMessage(message);
		mesB.open();
	}

	static void save() {
		try {
			int index = ctf.getSelectionIndex();
			ListTab curPage = valuePages.get(index);
			boolean hasEmpty = false;
			for (WObject wo : curPage.openedList) {
				if (wo.label.isEmpty() || wo.value.isEmpty()) {
					hasEmpty = true;
					break;
				}
			}

			if (hasEmpty) {
				error("Cannot save file with empty entries!", SWT.ICON_ERROR);
			} else {
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setText("Save As");
				fd.setFilterNames(new String[]{
						"WorldsPlayer Avatar Data (*.avatars)",
						"WorldsPlayer Worldsmarks Data (*.worldsmarks)"});
				fd.setFilterExtensions(new String[]{"*.avatars", "*.worldsmarks"});
				String savedPath = fd.open();
				if (savedPath != null) {
					switch (fd.getFilterIndex()) {
						case 0:
							new Saver(savedPath).saveAvatars(curPage.openedList);
							break;
						case 1:
							new Saver(savedPath).saveMark(curPage.openedList);
							break;
						case 2:
							MessageBox mesB = new MessageBox(ctf.getShell(), SWT.ICON_ERROR);
							mesB.setMessage("This feature isn't supported yet!");
							mesB.open();
					}
					curPage.path = savedPath;
					curPage.updateTab();
					curPage.hasChanged = false;
					curPage.openedList = curPage.getOriginal();
				}
			}
		} catch (IOException | NullPointerException e) {
			error("Unable to read/write file! Permissions problem?", SWT.ICON_ERROR);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("An ArrayIndexOutOfBoundsException was thrown and caught during saveAsItem. Ignoring.");
		}
	}

	// These are in separate sections so I don't have to deal with the same code twice for Drag 'n' Drop and normal Open operation
	static void open() {
		try {
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
			fd.setText("Open");
			fd.setFilterNames(new String[]{
					"All Files",
					"WorldsPlayer Avatar Data (*.avatars)",
					"WorldsPlayer Worldsmarks Data (*.worldsmarks)"});
			fd.setFilterExtensions(new String[]{"*", "*.avatars", "*.worldsmarks"});
			String openedPath = fd.open();
			if (openedPath != null) openFile(openedPath);
		} catch (IllegalArgumentException e) {
			error("Unable to read/write file! Permissions problem?", SWT.ICON_ERROR);
		}
	}

	static void invalidPersister() {
		MessageBox mesB = new MessageBox(ctf.getShell(), SWT.ICON_ERROR);
		mesB.setMessage("Invalid File Format! File is not a valid Persister file!");
		mesB.open();
	}

	static void newItem() {
		try {
			ListTab curPage = new ListTab(ctf);
			valuePages.add(curPage);
			curPage.type = "NET.worlds.console.SavedAvMenuItem";
			curPage.returnTab("Untitled", false);
			new Main().TabListener(curPage);
			ctf.setSelection(curPage.tab);
		} catch (NullPointerException e) {
			error("Unable to read/write file! Permissions problem?", SWT.ICON_ERROR);
		} catch (InvalidPersisterFile e) {
			MessageBox mesB = new MessageBox(ctf.getShell(), SWT.ICON_ERROR);
			mesB.setMessage("Invalid File Format! File is not a valid Persister file!");
			mesB.open();
		} catch (ArrayIndexOutOfBoundsException ignored) {
		}
	}

	static void openFile(String filename) {
		try {
			if (filename != null) {
				if (filename.endsWith(".library")) {
					error("This format isn't supported yet!", SWT.ICON_ERROR);
				} else {
					ListTab newPage = new ListTab(ctf);
					valuePages.add(newPage);
					newPage.returnTab(filename);
					new Main().TabListener(newPage);
					ctf.setSelection(newPage.tab);
					if (newPage.value.getItemCount() > 0) {
						newPage.value.setSelection(0);
						newPage.selectedUpdate();
					}
				}
			}
		} catch (InvalidPersisterFile e) {
			invalidPersister();
		}
	}
}
