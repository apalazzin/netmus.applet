package applet;

import java.applet.AppletContext;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.ParserConfigurationException;

import org.farng.mp3.AbstractMP3Tag;
import org.farng.mp3.MP3File;

public class DeviceScanner extends Thread {
   
   private final String linux_path = "/media";
   private final String mac_path = "/Volumes";
   private final FileSystemView fs = FileSystemView.getFileSystemView();
   private String default_path = "";
   private List<File> devices = null;
   private int num_devices = 0;
   private File last_device = null; // inserire DP
   boolean rescan = false;           // inserire DP
   AppletContext app_context;
   
   private boolean is_active;
   private String user;
   
   DeviceScanner(String user, AppletContext app_context) {    
      this.app_context = app_context;
      this.user = user;
   }
   
   private void initialize(){
      try {
          if (System.getProperty("os.name").contains("Linux")) { // Linux
              devices = Arrays.asList(fs.getFiles(new File(linux_path), false));
              default_path = linux_path;
           }
           else if (System.getProperty("os.name").contains("Mac")){  //new File(mac_path).exists()) {    
              devices = Arrays.asList(fs.getFiles(new File(mac_path), false));
              default_path = mac_path;
           }
           else if (System.getProperty("os.name").contains("Windows")) { // WINDOWS File.listRoots().length > 1
              devices = Arrays.asList(File.listRoots());
              default_path = "";
           }
           else {
              devices = new ArrayList<File>();
           }
           num_devices = devices.size();
           
           try {
               app_context.showDocument(
                       new URL("javascript:showStatus(\" -> "+System.getProperty("os.name")+"\")"));
           } catch (Exception e) {
           }
           
      } catch (Exception e) {
          try {
              app_context.showDocument(
                      new URL("javascript:showStatus(\" -> "+e.toString()+"\")"));
          } catch (Exception ex) {
          }
      }
   }
   
   //gets the relative path.
   private String relativePath(String path, String device_path) {
      path = path.replace(device_path, "");
      return path;
   }
   
   //format the path following the Unix model
   private String pathUnix(String path){
	   return path.replaceAll("\\\\","/");
   }
   
   //prepare the string to been sent.
   private String prepare(String s){
	   return s.replaceAll("'", "\\\\'");
   }
   
   private int scanMedia(File folder, PrintWriter log, TranslateXML xml, List<String> old_mp3, String dev_path, int actual, int total) {
      
      File[] files = fs.getFiles(folder, false);
      FileNameExtensionFilter audioFilter = new FileNameExtensionFilter("Audio", "mp3");

      for (File f: files) {
         if (f.isDirectory())
            actual = scanMedia(f,log,xml,old_mp3,dev_path,actual,total);
         else if (audioFilter.accept(f) && !old_mp3.contains(pathUnix(relativePath(f.getAbsolutePath(), dev_path)))) {
            xml.addMP3(getTag(f), pathUnix(relativePath(f.getAbsolutePath(), dev_path)));
            log.println(pathUnix(relativePath(f.getAbsolutePath(), dev_path)));
            actual++;
            try {
                app_context.showDocument(
                        new URL("javascript:scanStatus("+actual+","+total+")"));
            } catch (Exception e) {
            }
         }
      }
      return actual;
   }
   
   private int countMedia(File folder, PrintWriter log, TranslateXML xml, List<String> old_mp3, String dev_path,int total) {
       
       File[] files = fs.getFiles(folder, false);
       FileNameExtensionFilter audioFilter = new FileNameExtensionFilter("Audio", "mp3");

       for (File f: files) {
          if (f.isDirectory())
             total = countMedia(f,log,xml,old_mp3,dev_path,total);
          else if (audioFilter.accept(f) && !old_mp3.contains(pathUnix(relativePath(f.getAbsolutePath(), dev_path)))) {
             total++;
          }
       }
       return total;
    }
   
   
   // crea una lista con tutti i nomi di file presenti nel log, quindi gia' scansionati
   private List<String> readLog(File log) {
      List<String> old_mp3 = new ArrayList<String>();

      if (log.exists()) {
         try {
            FileInputStream fstream = new FileInputStream(log);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            String line;
            while ((line = br.readLine()) != null)
                old_mp3.add(line);

            in.close();
            
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      return old_mp3;
   }
   
   private AbstractMP3Tag getTag(File mp3_file) {
      MP3File mp3 = null;
      AbstractMP3Tag tag = null;
      try {
         mp3 = new MP3File(mp3_file); // puo' dar problemi con MP3 difettosi.
         if (mp3.hasID3v2Tag()) {
             tag = mp3.getID3v2Tag();
          }
          else if (mp3.hasID3v1Tag()) {        
             tag = mp3.getID3v1Tag();
          }
          else if (mp3.hasLyrics3Tag()) {     
             tag = mp3.getLyrics3Tag();
          }
          //else System.out.println(mp3_file.getAbsolutePath()+" has no tag.");
      } catch (Exception e) { // bisogna gestire una possibile IO Exception in maniera da riprendere
         e.printStackTrace();    // normalmente le attivita' chiudendo reader e  writer vari
      }
      
      return tag;
   }
   
   
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public void run() {
	   //per la lettura/scrittura dei file ho bisogno di eseguire un'azione privilegiata: da guardare le regole per i parametri/ritorni.
	   try {
		   AccessController.doPrivileged (
			          new PrivilegedAction() {
			            public Object run() {
			            	initialize();
			                return null; // still need this
			            }
			          }
			        );
	   }
	   catch (Exception e){
		   try {
			      app_context.showDocument(
		                  new URL("javascript:showStatus(\"Ecx-> "+e+"\")"));
		      } catch (Exception ex) {
		      }
	   }


      while(true) {
          
          try {
              AccessController.doPrivileged (
                         new PrivilegedAction() {
                           public Object run() {
                               listenFileSystem();
                               return null; // still need this
                           }
                         }
                       );
          }
          catch (Exception e){
              try {
                     app_context.showDocument(
                             new URL("javascript:showStatus(\"Ecx-> "+e+"\")"));
                 } catch (Exception ex) {
                 }
          }
         
      }//while(true)
   }//run()
   
   private void listenFileSystem() { // ex corpo while(true) di run
       
       // dorme finche' non viene svegliato dall'applet
       while(!is_active) {
          synchronized (this) {
             try {
                wait();
             } catch (InterruptedException e) {
                e.printStackTrace();
             }
          }
       }
       
       List<File> devices_temp = null;
       int num_devices_temp = 0;
       
       if (default_path.equals("")) { // WINDOWS
          devices_temp = Arrays.asList(File.listRoots());
          num_devices_temp = devices_temp.size();
       }
       else { // LINUX o MAC
          devices_temp = Arrays.asList(fs.getFiles(new File(default_path), false));
          num_devices_temp = devices_temp.size();
       }
       
       // controllo se e' stato richiesto RESCAN per il LAST DEVICE
       // utilizzo un rescan_temp per non aver interferenze nei controlli durane il processo di scansione ed elaborazione
       boolean rescan_temp = rescan;
       
       if (num_devices_temp > num_devices || rescan_temp) { // inserita 1 o piu' periferiche
          
          List<File> new_devices = new ArrayList<File>();
          
          
          
          if (rescan_temp) {
              new_devices.add(last_device);
              
          } else {
              
              // creo una lista con i path dei device presenti
              // per poi individuare il nuovo (o piu) device rilevati
              List<String> devices_name = new ArrayList<String>();
              for (File f : devices) {
                 devices_name.add(f.getAbsolutePath());
              }
              
              // individuo i nuovi dispositivi inseriti
              for (File f : devices_temp) {
                 if(!devices_name.contains(f.getAbsolutePath())) {
                    new_devices.add(f);
                    try {
                        app_context.showDocument(
                                new URL("javascript:showStatus(\"Trovato: "+f.getAbsolutePath()+"\")"));
                    } catch (Exception e) {
                    }
                 }
              }
              
              // aggiorno la vecchia lista device con quella nuova
              num_devices = num_devices_temp;
              devices = devices_temp;
          }
          

          // scansiono il nuovo device ed invio i dati all'applicazione (anche piu' di uno sequenzialmente)
          for(File new_device : new_devices) {
             try {
                
                String slash = System.getProperty("file.separator");
                //if (default_path != "") slash = "/";
                String log_path = new_device.getAbsolutePath() + slash + user +".netmus.log";
                
                if (rescan_temp) {
                    File log = new File(log_path);
                    log.delete();
                }
                File log = new File(log_path);
                List<String> old_mp3 = readLog(log);
                
                FileWriter open_log = new FileWriter(log_path, true);
                
                PrintWriter file = new PrintWriter(open_log);
                
                TranslateXML xml = null;
                try {
                   xml = new TranslateXML();
                } catch (ParserConfigurationException e) {
                   e.printStackTrace();
                }
                
                if (default_path.equals("")) {
                    slash = ""; // per il path_device
                }
                // path da togliere nel log e nel xml dal nome del file. in modo che sia un path relativo.
                String device_path = new_device.getAbsolutePath() + slash;
                
                int total_file = countMedia(new_device,file,xml,old_mp3,device_path,0);
                
                scanMedia(new_device,file,xml,old_mp3,device_path,0,total_file); // scansione new_device
                
                file.close(); // vedere
                
                String xml_string = xml.toString();
                
                // mi tengo memorizzato l'ultimo dispositivo scansionato, per un eventuale RESCAN
                last_device = new_device;
                // rimetto l'eventuale rescan a false
                rescan = false;
 
                try {
                    app_context.showDocument(
                            new URL("javascript:scanResult('"+prepare(xml_string)+"')"));
                    app_context.showDocument(
                            new URL("javascript:rescanVisible()"));
                    
                } catch (Exception e) {
                    try {
                        app_context.showDocument(
                                new URL("javascript:showStatus(\"ERRORE\")"));
                    } catch (Exception ex) {
                    }
                }

             } catch (IOException io) {}
          }
          
       }
       else if (num_devices_temp < num_devices) {
           
          try {
               app_context.showDocument(
                       new URL("javascript:showStatus(\"Rimosso device\")"));
               app_context.showDocument(
                       new URL("javascript:rescanNotVisible()"));
          } catch (Exception e) {
          }
          
          if (default_path.equals("")) { // WINDOWS
             devices = Arrays.asList(File.listRoots());
             num_devices = devices.size();
          }
          else { // LINUX o MAC
             devices = Arrays.asList(fs.getFiles(new File(default_path), false));
             num_devices = devices.size();
          }
       }
       
       try {
          Thread.sleep(500);
       } catch (InterruptedException e) {
          e.printStackTrace();
       }
       
   }
   
   synchronized void setState(boolean b) { // visibilita' package
      is_active = b;
   }
}