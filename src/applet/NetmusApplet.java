package applet;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class NetmusApplet extends JApplet {
   
   // Scanner state for the applet
   private boolean isActive;
   private String loggedUser;
   private static DeviceScanner scanner;
   
   
   private String s = "";// da togliere
   private JTextField l = new JTextField(20);// da togliere
   private JButton button = new JButton("Disattiva");// da togliere
   
   @Override
   public void start() {
      
      setLayout(new FlowLayout(FlowLayout.CENTER));// da togliere dopo
      add(l); // da togliere dopo
      
      // bottone per simulare on/off da GWT
      button.addActionListener(new ActionListener() {// da togliere dopo
         @Override
         public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            setState(!isActive);
            if(isActive) {
               button.setText("Disattiva");
            }
            else {
               button.setText("Attiva");
            }
            repaint();
         }
      });
      add(button);// da togliere dopo
      
//      CON QUESTA RICHIESTA VOGLIO ANCHE IL NOME UTENTE, NON SOLO LO STATO
      isActive = true;
      loggedUser = "smile";
      
//      try {
//         getAppletContext().showDocument(new URL("javascript:getState()"));
//      } catch (Exception e) {
//         e.printStackTrace();
//      }
      
      
      scanner = new DeviceScanner(loggedUser);
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
         s = "   true";
         synchronized(scanner) {
            scanner.notify();
         }
      }
      else {
         s = "   false";
      }
      l.setText(s);
      repaint();

//      if (isActive)
//         fai partire il thread scanner; (notify)
//      else
//         ferma il thread scanner (wait)
      
   }
   
}
