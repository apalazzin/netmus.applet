package applet;

import java.net.URL;
import javax.swing.JApplet;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class NetmusApplet extends JApplet {
   
   // Scanner state for the applet
   private boolean isActive = false;
   private String s = "";
   private JTextField l = new JTextField(20);
   
   @Override
   public void start() {
      add(l);
      try {
         getAppletContext().showDocument(new URL("javascript:getState()"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   public void setState(boolean b) {
      isActive = b;
      
      // da eliminare dopo
      if (isActive)
         s = "   true";
      else
         s = "   false";
      l.setText(s);
      repaint();
      
//      if (isActive)
//         fai partire il thread scanner; (notify)
//      else
//         ferma il thread scanner (wait)
      
   }
   
}
