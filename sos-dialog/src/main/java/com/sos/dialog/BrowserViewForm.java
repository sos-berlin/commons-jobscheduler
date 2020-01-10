package com.sos.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.dialog.classes.SOSUrl;

public class BrowserViewForm {

    private Browser browser = null;
    private GridLayout gridLayout;
    private GridData data;
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserViewForm.class);
    private SOSUrl url;
    private final Composite parent;

    public BrowserViewForm(final Composite parent_, final int style, final String url_) {
        parent = parent_;
        url = new SOSUrl(url_);
        showBrowser();
    }

    public BrowserViewForm(final Composite parent_, final int style, final String host, final int port) {
        parent = parent_;
        url = new SOSUrl(String.format("%s:%s", host, port));
        showBrowser();
    }

    public BrowserViewForm(final Composite parent_, final int style, final SOSUrl url_) {
        parent = parent_;
        this.url = url_;
        showBrowser();
    }

    private void showBrowser() {
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        parent.setLayout(gridLayout);
        try {
            browser = new Browser(parent, SWT.NONE);
            initialize(parent.getDisplay(), browser);
            LOGGER.debug("load url = " + url.getUrlValue());
            browser.setUrl(url.getUrlValue());
            browser.addProgressListener(new ProgressAdapter() {

                @Override
                public void completed(final ProgressEvent event) {
                    LOGGER.trace("processed");
                }
            });
            browser.addStatusTextListener(new StatusTextListener() {

                @Override
                public void changed(final StatusTextEvent event) {
                    LOGGER.trace("status = " + event.text);
                }
            });
            data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.verticalAlignment = GridData.FILL;
            data.horizontalSpan = 2;
            data.grabExcessHorizontalSpace = true;
            data.grabExcessVerticalSpace = true;
            browser.setLayoutData(data);
            browser.layout();
        } catch (Exception E) {
            Label label = new Label(parent, SWT.NONE);
            label.setText("Browser could not be startet. Check wether XULRUNNER 1.9.2 is installed");
        }

    }

    static void initialize(final Display display, final Browser browser) {
        browser.addOpenWindowListener(new OpenWindowListener() {

            @Override
            public void open(final WindowEvent event) {
                Shell shell = new Shell(display);
                shell.setLayout(new FillLayout());
                Browser browser = new Browser(shell, SWT.NONE);
                initialize(display, browser);
                event.browser = browser;
            }
        });
        browser.addVisibilityWindowListener(new VisibilityWindowListener() {

            @Override
            public void hide(final WindowEvent event) {
                Browser browser = (Browser) event.widget;
                Shell shell = browser.getShell();
                shell.setVisible(false);
            }

            @Override
            public void show(final WindowEvent event) {
                Browser browser = (Browser) event.widget;
                final Shell shell = browser.getShell();
                if (event.location != null) {
                    shell.setLocation(event.location);
                }
                if (event.size != null) {
                    Point size = event.size;
                    shell.setSize(shell.computeSize(size.x, size.y));
                }
                shell.open();
            }
        });
        browser.addCloseWindowListener(new CloseWindowListener() {

            @Override
            public void close(final WindowEvent event) {
                Browser browser = (Browser) event.widget;
                Shell shell = browser.getShell();
                shell.close();
            }
        });

    }

    public String getUrlCaption() {
        return url.getUrlCaption();
    }

    public void setUrl(final String url_) {
        this.url = new SOSUrl(url_);
        browser.setUrl(url.getHost() + "://" + url.getUrl().getPath());
    }

    public Browser getBrowser() {
        return browser;
    }

}