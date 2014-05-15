package com.sos.dialog.classes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.sos.dialog.components.IntegerField;
 
public class SOSIntegerInputDialog extends Dialog {
 

private static final String CAPTION_CANCEL = "Cancel";
private static final String CAPTION_OK = "Ok";
private Integer value;
private Shell shell;
private Label lbNumber;
 
  public SOSIntegerInputDialog(Shell parent) {
    super(parent);
  }
 
 
  public SOSIntegerInputDialog(Shell parent, int style) {
    super(parent, style);
  }
 
 
  public Integer open() {
    Shell parent = getParent();
    shell =  new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
    shell.setSize(152, 90);
 
    shell.setLayout(new GridLayout(2, true));
    Point pt = shell.getDisplay().getCursorLocation();
    shell.setLocation(pt.x, pt.y);
    
    lbNumber = new Label(shell, SWT.NULL);
    lbNumber.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
    lbNumber.setText("Number");
    final IntegerField integerValue = new IntegerField(shell, SWT.SINGLE | SWT.BORDER);
    integerValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    integerValue.setText(value);
 
    final Button buttonOK = new Button(shell, SWT.PUSH);
    buttonOK.setText(CAPTION_OK);
    GridData gd_buttonOK = new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd_buttonOK.grabExcessHorizontalSpace = true;
    buttonOK.setLayoutData(gd_buttonOK);
    buttonOK.setEnabled(true);
    Button buttonCancel = new Button(shell, SWT.PUSH);
    buttonCancel.setText(CAPTION_CANCEL);
  
    buttonOK.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        value = integerValue.getIntegerValue(500);
        shell.dispose();
      }
    });
 
    buttonCancel.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        value = null;
        shell.dispose();
      }
    });
    shell.addListener(SWT.Traverse, new Listener() {
      public void handleEvent(Event event) {
        if(event.detail == SWT.TRAVERSE_ESCAPE)
          event.doit = false;
      }
    });
 
    integerValue.setText(value);
    shell.pack();
    shell.open();
 
    Display display = parent.getDisplay();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
 
    return value;
  }
  
  public void setDialogCaption(String caption) {
      shell.setText(caption);
  }
  
  public void setLableCaption(String caption) {
      lbNumber.setText(caption);
      
  }
 
  public static void main(String[] args) {
    Shell shell = new Shell();
    SOSIntegerInputDialog dialog = new SOSIntegerInputDialog(shell);
    System.out.println(dialog.open());
  }


public void setValue(Integer value) {
    this.value = value;
}
}