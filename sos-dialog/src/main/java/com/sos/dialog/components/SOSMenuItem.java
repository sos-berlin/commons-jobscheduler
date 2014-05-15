package com.sos.dialog.components;

import java.util.prefs.Preferences;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

 
 
public class SOSMenuItem extends MenuItem {
    private static final String SOS_SCHEDULER = "SOS_SCHEDULER";

     private Preferences prefs;
     private String menuName="";

    public SOSMenuItem(Menu parent, int style, String label) {
        super(parent, style);
        initialize(label,false); 
    }
    

    public SOSMenuItem(Menu parent, int style, String menuName_, String label) {
        super(parent, style);
        this.menuName = menuName_;
        initialize(label,false); 
    }

    public SOSMenuItem(Menu parent, int style, String menuName_, String label, boolean checked) {
        super(parent, style);
        this.menuName = menuName_;
        initialize(label,checked); 
    }
    
   private void initialize(String label,boolean checked) {
       prefs = Preferences.userNodeForPackage(this.getClass());

       setText(label);
       setSelection(getSelectionFromPrefs(checked));
       

       addSelectionListener(new SelectionAdapter() {
           public void widgetSelected(SelectionEvent e) {
               prefs.node(SOS_SCHEDULER).put(prefKey(), getSelectionAsString());;
           }
       });
   
   }
   private String prefKey() {
       
       return "MENU_ITEM:"+ menuName + ":" +getText() + ":selection";
   }
    
   private boolean getSelectionFromPrefs(boolean defaultValue) {
       String defaultS = "false";
       if (defaultValue) {
           defaultS = "true";
       }
       String sel = prefs.node(SOS_SCHEDULER).get(prefKey(), defaultS);
       return sel.equals("true");        
   }
   
   public void setText(String menuName_, String text) {
       this.menuName = menuName_;
       super.setText(text);
   }
    
   private String getSelectionAsString() {
       if (getSelection()) {
           return "true";
       }else {
           return "false";
       }
   }
  
    
    protected void checkSubclass() {

        // Disable the check that prevents subclassing of SWT components
    }


}
