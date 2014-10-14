package com.sos.dialog.components;

/*
 * Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.sos.dialog.swtdesigner.SWTResourceManager;

/**
 * Splash screen for the minutes editor.
 *
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public class SplashScreen {
	private Shell		splash;
	private ProgressBar	bar;
	private Image		image;
	public int			steps;

	public void show() {
		splash.open();
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			// nothing to do here, just continue...
		}
	}

	public void step() {
		int current = bar.getSelection();
		bar.setSelection(++current);
		if (current == steps) {
			splash.close();
			image.dispose();
		}
	}

	public void setSteps(final int pintSteps) {
		steps = pintSteps;
		bar.setMaximum(steps);
	}

	public static SplashScreen newInstance(int steps, final String pstrImageFileName) {
		SplashScreen screen = new SplashScreen();
		screen.steps = steps;

		Display display = Display.getCurrent();
		screen.splash = new Shell(SWT.NONE);

		screen.image = SWTResourceManager.getImageFromResource(pstrImageFileName);
		screen.bar = new ProgressBar(screen.splash, SWT.NONE);
		screen.bar.setMaximum(steps);

		Label label = new Label(screen.splash, SWT.NONE);
		label.setImage(screen.image);
		initLayout(screen.splash, screen.bar, label);

		initPosition(display, screen.splash);
		return screen;
	}

	private static void initLayout(Shell splash, ProgressBar bar, Label label) {
		FormLayout layout = new FormLayout();
		splash.setLayout(layout);

		FormData labelData = new FormData();
		labelData.right = new FormAttachment(100, 0);
		labelData.bottom = new FormAttachment(100, 0);
		label.setLayoutData(labelData);

		FormData progressData = new FormData();
		progressData.left = new FormAttachment(0, 5);
		progressData.right = new FormAttachment(100, -10);
		progressData.bottom = new FormAttachment(100, -5);
		bar.setLayoutData(progressData);
		splash.pack();
		splash.layout(true, true);
	}

	private static void initPosition(Display display, Shell splash) {
		Rectangle splashRect = splash.getBounds();
		Rectangle displayRect = display.getBounds();
		int x = (displayRect.width - splashRect.width) / 2;
		int y = (displayRect.height - splashRect.height) / 2;
		splash.setLocation(x, y);
	}
}
