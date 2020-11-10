package org.wirla.WorldsOrganizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.awt.*;
import java.net.URI;

public class AboutDialog {

	AboutDialog(Display display) {
		Shell shell = new Shell(display);

		Shell dialog = new Shell(shell, SWT.OK);
		dialog.setText("About");
		dialog.setSize(386, 200);
		dialog.setLayout(new RowLayout());

		Label logo = new Label(dialog, SWT.NONE);
		logo.setImage(new Image(display, Main.class.getClassLoader().getResourceAsStream("resources/logo.png")));

		Composite textCom = new Composite(dialog, SWT.NONE);
		textCom.setLayout(new GridLayout());

		Label programName = new Label(textCom, SWT.BOLD);
		programName.setText("Worlds Organizer");
		programName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		Label version = new Label(textCom, SWT.NONE);
		version.setText("Version " + Detail.getVersion());
		version.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		Label creator = new Label(textCom, SWT.NONE);
		creator.setText("Coded with passion by Wirlaburla");
		creator.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		Label curYear = new Label(textCom, SWT.NONE);
		curYear.setText(Detail.getDate());
		curYear.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		Composite buttonCom = new Composite(dialog, SWT.NONE);
		buttonCom.setLayout(new RowLayout());

		Button gotoPage = new Button(buttonCom, SWT.FLAT);
		gotoPage.setText("Website");

		gotoPage.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				try {
					Desktop desktop = java.awt.Desktop.getDesktop();
					URI oURL = new URI("https://wirlaburla.site/library/WorldsPlayer/tools/WorldsOrganizer/index.html");
					desktop.browse(oURL);
				} catch (Exception e) {
					Main.error("Couldn't open the webpage.");
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {	}
		});

		Button ok = new Button(buttonCom, SWT.FLAT);
		ok.setText("Close");

		ok.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) { dialog.close(); }
			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent) {	}
		});

		dialog.open();
	}
}
