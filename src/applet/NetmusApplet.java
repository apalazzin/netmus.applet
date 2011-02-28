package applet;

import java.applet.AppletContext;
import java.net.URL;

import javax.swing.JApplet;

@SuppressWarnings("serial")
/**
 * Nome: NetmusApplet.java
 * Autore:  VT.G
 * Licenza: GNU GPL v3
 * Data Creazione: 18 Febbraio 2011
*/
public class NetmusApplet extends JApplet {
   
   private boolean is_active;// Scanner state for the applet
   private String logged_user; // loggedUser in GWT
   private static DeviceScanner scanner; // scanner
   AppletContext app_context;

   @Override
   /**
    * Metodo chiamato in automatico quando viene inizializzata l'applet.
    * Memorizza il context dell'applet ed invia un segnale a GWT grazie a JSNI
    * per farsi mandare stato e nome dell'utente loggato.
    */
   public void start() {
       
       app_context = getAppletContext();
       
       try {
           app_context.showDocument(
                   new URL("javascript:getStarts()"));
       } catch (Exception e) {
       }
   }
   
   /**
    * Metodo chiamato da GWT dopo aver ricevuto il segnale di getStarts().
    * Crea e fa partire il Thread Scanner passandogli l'utente loggato.
    * @param user utente loggato
    * @param state stato dell'applet
    */
   public void letsGO(String user, boolean state) {
       is_active = state;
       logged_user = user;

       scanner = new DeviceScanner(logged_user, app_context);
       setState(is_active);
       scanner.start();
   }
   
   /**
    * Cambia lo stato dentro il thread scanner, per metterlo a riposare o svegliarlo
    * tramite un notify. Mostra lo stato sul display di applet.
    * @param state Stato dell'applet scelto dall'utente
    */
   public void setState(boolean state) {
      is_active = state;
      scanner.setState(state);

      if (is_active) {
         synchronized(scanner) {
            scanner.notify();
         }
      }
      
      String s = "appletOFF";
      if (is_active) {
          s = "appletON";
      }
      try {
          app_context.showDocument(
                  new URL("javascript:showStatus(\""+s+"\")"));
      } catch (Exception e) {
      }
      
   }
   
   /**
    * Fa fare una nuova scansione a scanner, dell'ultimo dispositivo inserito.
    */
   public void rescanAll() {
       
       scanner.rescan = true;
       if (is_active) {
           synchronized(scanner) {
              scanner.notify();
           }
        }
   }
   
   /**
    * Fa mostrare a scanner la finestra di scelta di una cartella locale da scansionare.
    */
   public void showChooser() {
       
       scanner.chooser = true;
       if (!is_active) {
           synchronized(scanner) {
              scanner.notify();
           }
        }
   }
   
}