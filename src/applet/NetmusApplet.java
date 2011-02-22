package applet;

import java.applet.AppletContext;
import java.net.URL;

import javax.swing.JApplet;

@SuppressWarnings("serial")
public class NetmusApplet extends JApplet {
   
   private boolean is_active;// Scanner state for the applet
   private String logged_user; // loggedUser in GWT
   private static DeviceScanner scanner; // scanner
   AppletContext app_context;

   @Override
   public void start() {
       
       app_context = getAppletContext();
       
       try {
           app_context.showDocument(
                   new URL("javascript:getStarts()"));
       } catch (Exception e) {
       }
   }
   
   public void letsGO(String user, boolean state) {
       try {
           app_context.showDocument(
                   new URL("javascript:showStatus(\"LetsGO\")"));
       } catch (Exception e) {
       }
       
       is_active = state;
       logged_user = user;

       scanner = new DeviceScanner(logged_user, app_context);
       setState(is_active);
       scanner.start();
   }
   
   // metodo chiamato da GWT, contenente lo stato dell'applet
   public void setState(boolean b) {
      is_active = b;
      scanner.setState(b);

      if (is_active) {
         synchronized(scanner) {
            scanner.notify();
         }
      }
      
      String s = "APPLET OFF";
      if (is_active) {
          s = "APPLET ON";
      }
      try {
          app_context.showDocument(
                  new URL("javascript:showStatus(\""+s+"\")"));
      } catch (Exception e) {
      }
      
   }
   
   public void rescanAll() {
       
       scanner.rescan = true;
       if (is_active) {
           synchronized(scanner) {
              scanner.notify();
           }
        }
   }
   
}