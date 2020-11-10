package org.wirla.WorldsOrganizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ListTab {

	protected java.util.List<WorldDataObject> origList;

	String type;
	CTabFolder ctf;
	String path;
	List value;
	CTabItem tab;
	Composite list;
	Composite properties;
	boolean isFile;

	Label indexTxt;
	Text labelEntry;
	Text valueEntry;

	boolean hasChanged = false;

	java.util.List<WorldDataObject> openedList = new ArrayList<>();

	ListTab(CTabFolder ctf) { this.ctf = ctf; }

	CTabItem returnTab(String tpath) throws InvalidPersisterFile {
		return returnTab(tpath, true);
	}

	CTabItem returnTab(String tpath, boolean isFile) throws InvalidPersisterFile {
		this.path = tpath;
		this.isFile = isFile;
		String tabTip = "Untitled";
		String file;
		if (isFile) {
			file = new File(path).getName();
			tabTip = tpath;
		}
		else file = "Untitled";
		CTabItem cti = new CTabItem(ctf, SWT.BORDER);
		cti.setText(file);
		cti.setToolTipText(tabTip);

		FillLayout fl = new FillLayout();
		fl.type = SWT.HORIZONTAL;

		Composite c = new Composite(ctf, SWT.FLAT);
		c.setLayout(new GridLayout());

		value = new List(c, SWT.BORDER_SOLID | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
		list = new Composite(c, SWT.NONE);
		RowLayout listLayout = new RowLayout();
		listLayout.pack = false;
		listLayout.wrap = true;
		listLayout.fill = true;
		listLayout.justify = true;
		list.setLayout(listLayout);
		list.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		ctf.addCTabFolder2Listener(new CTabFolder2Listener() {
			@Override
			public void close(CTabFolderEvent event) {
				if (hasChanged && event.item == cti) {
					event.doit = false;
					MessageBox mesB = new MessageBox(ctf.getShell(), SWT.NONE | SWT.OK | SWT.CANCEL);
					mesB.setText("Warning");
					mesB.setMessage("Are you sure you want to close this tab without saving?");
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
			}
			@Override
			public void minimize(CTabFolderEvent cTabFolderEvent) { }
			@Override
			public void maximize(CTabFolderEvent cTabFolderEvent) { }
			@Override
			public void restore(CTabFolderEvent cTabFolderEvent) { }
			@Override
			public void showList(CTabFolderEvent cTabFolderEvent) { }
		});

		ToolBar tb = new ToolBar(list, SWT.FLAT);

		ToolItem addItem = new ToolItem(tb, SWT.PUSH);
		addItem.setImage(new Image(ctf.getDisplay(), Main.class.getClassLoader().getResourceAsStream("resources/add.png")));

		addItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				WorldDataObject newWO = new WorldDataObject(type, 1, "Object " + (value.getItemCount() + 1), "");
				openedList.add(newWO);
				value.add(newWO.label);
				value.setSelection(value.getItemCount() - 1);
				selectedUpdate();
				hasChanged = true;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) { }
		});

		ToolItem delItem = new ToolItem(tb, SWT.PUSH);
		delItem.setImage(new Image(ctf.getDisplay(), Main.class.getClassLoader().getResourceAsStream("resources/delete.png")));

		delItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				int index = value.getSelectionIndex();
				if (index >= 0) {
					openedList.remove(index);
					value.remove(index);
					if (index >= 1) {
						if (index <= value.getItemCount()) value.setSelection(index);
						else value.setSelection(index - 1);
					}
					else value.setSelection(0);
				}
				selectedUpdate();
				hasChanged = true;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) { }
		});

		ToolItem moveUpItem = new ToolItem(tb, SWT.PUSH);
		moveUpItem.setImage(new Image(ctf.getDisplay(), Main.class.getClassLoader().getResourceAsStream("resources/moveup.png")));

		moveUpItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				int index = value.getSelectionIndex();
				if (index > 0) {
					WorldDataObject item = openedList.get(index);
					openedList.remove(index);
					value.remove(index);
					openedList.add(index - 1, item);
					value.add(item.label, index - 1);
					value.setSelection(index - 1);
					indexTxt.setText("Index: " + (value.getSelectionIndex() + 1));
				}
				hasChanged = true;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) { }
		});

		ToolItem moveDownItem = new ToolItem(tb, SWT.PUSH);
		moveDownItem.setImage(new Image(ctf.getDisplay(), Main.class.getClassLoader().getResourceAsStream("resources/movedown.png")));

		moveDownItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				int index = value.getSelectionIndex();
				if (index >= 0 && index < openedList.size() - 1) {
					WorldDataObject item = openedList.get(index);
					openedList.remove(index);
					value.remove(index);
					openedList.add(index + 1, item);
					value.add(item.label, index + 1);
					value.setSelection(index + 1);
					indexTxt.setText("Index: " + (value.getSelectionIndex() + 1));
				}
				hasChanged = true;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) { }
		});

		tb.pack();

		if (isFile) {
			try {
				loadFile();
			} catch (IOException io) {
				return null;
			}
		}
		properties = new Composite(c, SWT.NONE);
		properties.setLayout(new GridLayout());

		indexTxt = new Label(properties, SWT.NONE);
		indexTxt.setText("Index: 0");
		indexTxt.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		Label labelText = new Label(properties, SWT.NONE);
		labelText.setText("Label: ");
		labelEntry = new Text(properties, SWT.BORDER | SWT.SINGLE);
		labelEntry.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		labelEntry.setEnabled(false);

		labelEntry.addModifyListener(modifyEvent -> {
			int index = value.getSelectionIndex();
			if (value.getSelection() != null && index >= 0) {
				WorldDataObject WO = openedList.get(index);
				WO.label = labelEntry.getText();
				value.setItem(index, labelEntry.getText());
				if (labelEntry.isFocusControl()) hasChanged = true;
			}
		});

		Label valueText = new Label(properties, SWT.NONE);
		valueText.setText("Value: ");
		valueEntry = new Text(properties, SWT.BORDER | SWT.SINGLE);
		valueEntry.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		valueEntry.setEnabled(false);

		valueEntry.addModifyListener(modifyEvent -> {
			int index = value.getSelectionIndex();
			if (value.getSelection() != null && index >= 0) {
				WorldDataObject WO = openedList.get(index);
				WO.value = valueEntry.getText();
				if (valueEntry.isFocusControl()) hasChanged = true;
			}
		});

		value.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedUpdate();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {

			}
		});

		cti.setControl(c);
		this.tab = cti;
		return cti;
	}

	void selectedUpdate() {
		try {
			indexTxt.setText("Index: " + (value.getSelectionIndex() + 1));
			labelEntry.setEnabled(true);
			labelEntry.setText(openedList.get(value.getSelectionIndex()).label);
			valueEntry.setEnabled(true);
			valueEntry.setText(openedList.get(value.getSelectionIndex()).value);
		} catch (IndexOutOfBoundsException e) {
			labelEntry.setEnabled(false);
			labelEntry.setText("");
			valueEntry.setEnabled(false);
			valueEntry.setText("");
		}
	}

	void updateTab() {
		tab.setText(new File(path).getName());
		tab.setToolTipText(path);
	}

	void loadFile() throws InvalidPersisterFile, IOException {
		openedList = new Restorer(path).getValues();
		for (WorldDataObject wo : openedList) {
			value.add(wo.label);
			type = wo.type;
		}
		origList = openedList;
	}

	boolean isWritable() {
		if (path != null) {
			File ourFile = new File(path);
			return ourFile.canWrite();
		} else return !isFile;
	}

	boolean isOriginal() {
		return openedList == origList;
	}

	java.util.List<WorldDataObject> getOriginal() {
		return origList;
	}

}
