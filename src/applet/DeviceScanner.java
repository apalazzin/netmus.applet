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

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.ParserConfigurationException;

import org.farng.mp3.AbstractMP3Tag;
import org.farng.mp3.MP3File;

/**
 * Nome: DeviceScanner.java
 * Autore:  VT.G
 * Licenza: GNU GPL v3
 * Data Creazione: 18 Febbraio 2011
*/
public class DeviceScanner extends Thread {
   
   private final String linux_path = "/media";
   private final String mac_path = "/Volumes";
   private final FileSystemView fs = FileSystemView.getFileSystemView();
   private String default_path = "";
   private List<File> devices = null;
   private int num_devices = 0;
   private File last_device = null; // inserire DP
   boolean rescan = false;           // inserire DP
   boolean chooser = false;           // inserire DP
   AppletContext app_context;
   
   private boolean is_active;
   private String user;
   
   DeviceScanner(String user, AppletContext app_context) {    
      this.app_context = app_context;
      this.user = user;
   }
   
   /**
    * Individua il tipo di sistema operativo per poi comportarsi in maniera adeguata.
    * Tiene memorizzata la lista di device rilevati all'apertura, per poi individuare l'inserimento
    * di un nuovo dispositivo. Manda lo stato sul display della Barra Applet.
    */
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
                       new URL("javascript:showStatus(\" -- "+System.getProperty("os.name")+" -- \")"));
           } catch (Exception e) {
           }
           
      } catch (Exception e) {}
   }
   
   /**
    * Crea il path relativo sapendo il path base del device
    * @param path Path assoluto
    * @param device_path Path del device
    * @return il path relativo al device
    */
   private String relativePath(String path, String device_path) {
      path = path.replace(device_path, "");
      return path;
   }
   
   /**
    * Adatta al formato UNIX (slash /) il path, anche se in Windows.
    * @param path un path 
    * @return il path in formato UNIX
    */
   private String pathUnix(String path){
	   return path.replaceAll("\\\\","/");
   }
   
   /**
    * Adatta una stringa per rendere possibile l'invio a GWT
    * @param s stringa da preparare per l'invio a GWT
    * @return una stringa passabile a GWT senza problemi in forma di URL in javascript
    */
   private String prepare(String s){
	   return s.replaceAll("'", "\\\\'");
   }
   
   /**
    * Esegue la scansione di una cartella e di tutte le sue sottocartelle,
    * alla ricerca di file MP3 da analizzare. Il metodo e' ricorsivo.
    * @param folder Cartella da scansionare
    * @param log File di log su cui scrivere la lista di file MP3 analizzati
    * @param xml Oggetto TranslateXML su cui aggiungere elementi tag dei brani
    * @param old_mp3 Lista di mp3 gia' analizzati poiche' presenti in un file di log vecchio
    * @param dev_path Path del device su cui si sta scansionando (per creare path relativi)
    * @param actual indice parziale di avanzamento della scansione MP3
    * @param total totale di brani da scansionare
    * @return numero parziale di file MP3 analizzati
    */
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
   
   /**
    * 
    * @param folder Cartella su cui eseguire il conteggio di MP3 presenti
    * @param log File di log su cui scrivere la lista di file MP3 analizzati
    * @param xml Oggetto TranslateXML su cui aggiungere elementi tag dei brani
    * @param old_mp3 Lista di mp3 gia' analizzati poiche' presenti in un file di log vecchio
    * @param dev_path Path del device su cui si sta scansionando (per creare path relativi)
    * @param total numero da incrementare fino ad arrivare al totale dei brani
    * @return parziale del conteggio brani
    */
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
   
   /**
    * Crea una lista con tutti i nomi di file presenti nel log, quindi gia' scansionati
    * @param log file di log
    * @return lista di brani presenti nel log file
    */
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
   
   /**
    * Estrae il tag dl file Mp3
    * @param mp3_file File individuato come MP3
    * @return Tag Mp3 polimorfo dal quale estrarre le varie informazioni
    */
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
   
   /**
    * Metodo ereditato da Thread che viene eseguito quando chiamato il metodo start().
    * Vengono creati dei privilegi all'utente per accedere ai file del File System,
    * poiche' non basta che l'applet sia firmata visto che vengono chiamati i metodi da 
    * javascript con JSNI. Ci vogliono questi particolari privilegi.
    */
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
	   catch (Exception e){}

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
          catch (Exception e){}
         
      }//while(true)
   }//run()
   
   /**
    * Metodo eseguito di continuo in run(), ad intervalli di mezzo secondo
    * Individua una periferica di archiviazione di massa inserita e la scansiona estraendone 
    * tutte le informazioni del tag mp3.
    * Puo' essere effettuata la scansione anche manualmente (rescan, chooser)
    */
   private void listenFileSystem() {
       
       // dorme finche' non viene svegliato dall'applet
       while(!is_active && !chooser) {
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
       boolean chooser_temp = chooser;
       
       // se richiesta CHOOSER, aspetto che venga scelta la cartella
       File user_folder = null;
       if (chooser_temp) {
           try{
               UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
           }catch(Exception e){  }
           
           JFileChooser file_chooser = new JFileChooser();
           file_chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
           if(file_chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
               user_folder = file_chooser.getSelectedFile();
           }else{
               user_folder = null;
               chooser = false;
               chooser_temp = false;
           }
       } // user_folder se chooser_temp == true contiene la cartella scelta dall'utente
       
       if (num_devices_temp > num_devices || rescan_temp || chooser_temp) { // inserita 1 o piu' periferiche
          
          List<File> new_devices = new ArrayList<File>();
          
          if (rescan_temp) {
              new_devices.add(last_device);
              
          } else if (chooser_temp) {
              new_devices.add(user_folder);
              
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
                                new URL("javascript:showStatus(\""+f.getAbsolutePath()+"\")"));
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
                
                file.close();
                
                // pendo la nuova lista di file scansionati
                List<String> new_mp3 = readLog(log);
                if (new_mp3.size() == old_mp3.size()) {
                    // non ci sono nuovi file mp3
                    try {
                        app_context.showDocument(
                                new URL("javascript:showStatus('noNewFiles')"));
                    } catch (Exception e) {}
                } else {
                    String xml_string = xml.toString();
                    try {
                        app_context.showDocument(
                                new URL("javascript:scanResult('"+prepare(xml_string)+"')"));
                    } catch (Exception e) {}
                }
                
                try {
                    if (!chooser_temp) app_context.showDocument(
                            new URL("javascript:rescanVisible()"));
                } catch (Exception e) {}
                
                // mi tengo memorizzato l'ultimo dispositivo scansionato, per un eventuale RESCAN
                last_device = new_device;
                
                // rimetto l'eventuale rescan e file chooser a false
                rescan = false; rescan_temp = false;
                
                // rimetto eventuale chooser true a false, e se attivo elimino
                // il log che non lo voglio nella scansione manuale
                if (chooser_temp && log.exists() || new_mp3.size()==0) log.delete();
                chooser_temp = false; chooser = false;

             } catch (IOException io) {}
          }
          
       }
       else if (num_devices_temp < num_devices) {
           
          try {
               app_context.showDocument(
                       new URL("javascript:showStatus(\"deviceRemoved\")"));
               app_context.showDocument(
                       new URL("javascript:rescanNotVisible()"));
          } catch (Exception e) {}
          
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
   
   /**
    * Setta lo stato del thread, per imporgli la sospensione se l'utente lo richiede.
    * @param b stato che comanda il thread di scansione
    */
   synchronized void setState(boolean b) { // visibilita' package
      is_active = b;
   }
}