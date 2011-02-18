package applet;

import java.applet.AppletContext;
import java.net.URL;

import javax.swing.JApplet;

@SuppressWarnings("serial")
public class NetmusApplet extends JApplet {
   
   private boolean isActive;// Scanner state for the applet
   private String loggedUser; // loggedUser in GWT
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

       isActive = state;
       loggedUser = user;
       scanner = new DeviceScanner(loggedUser, app_context);
       setState(isActive);
       scanner.start();
   }
   
   // metodo chiamato da GWT, contenente lo stato dell'applet
   public void setState(boolean b) {
      isActive = b;
      scanner.setState(b);
      
      // da eliminare dopo
      // solo per visualizzare lo stato graficamente
      if (isActive) {
         synchronized(scanner) {
            scanner.notify();
         }
      }
   }
   
}