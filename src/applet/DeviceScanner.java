package applet;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.ParserConfigurationException;

import org.farng.mp3.AbstractMP3Tag;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;

public class DeviceScanner extends Thread {
   
   private final File linux_path = new File("/media");
   private final File mac_path = new File("/Volumes");
   private final FileSystemView fs = FileSystemView.getFileSystemView();
   private File default_path = null;
   private List<File> devices = null;
   private int num_devices = 0;
   
   private boolean isActive;
   private String user;
   
   DeviceScanner(String user) {
      
      this.user = user;
      
      if(linux_path.exists()) {
         devices = Arrays.asList(fs.getFiles(linux_path, false));
         default_path = linux_path;
      }
      if(mac_path.exists()) {
         devices = Arrays.asList(fs.getFiles(mac_path, false));
         default_path = mac_path;
      }
      if (File.listRoots().length > 1) { // WINDOWS
         devices = Arrays.asList(File.listRoots());
         default_path = null;
      }
      num_devices = devices.size();
   }
   
   
   private void scanMedia(File folder, PrintWriter file, TranslateXML xml, List<String> old_mp3) {
      
      File[] files = fs.getFiles(folder, false);
      FileNameExtensionFilter audioFilter = new FileNameExtensionFilter("Audio", "mp3");

      for (File f: files) {
         if (f.isDirectory())
            scanMedia(f,file,xml,old_mp3);
         else if (audioFilter.accept(f) && !old_mp3.contains(f.getName())) {
            xml.addMP3(getTag(f), f.getName()); // da cambiare con path rel
            System.out.println(f.getName());
            file.println(f.getName());
         }
      }
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
            
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      
      return old_mp3;
   }
   
   private AbstractMP3Tag getTag(File mp3_file) {
      MP3File mp3 = null;
      try {
         mp3 = new MP3File(mp3_file); // puo' dar problemi con MP3 difettosi.
      } catch (IOException e) { // bisogna gestire una possibile IO Exception in maniera da riprendere
         e.printStackTrace();    // normalmente le attivita' chiudendo reader e  writer vari
      } catch (TagException e) {
         e.printStackTrace();
      }
      
      AbstractMP3Tag tag = null;
      if (mp3.hasID3v2Tag()) {
         System.out.println("- id3v2");
         tag = mp3.getID3v2Tag();
      }
      else if (mp3.hasID3v1Tag()) {
         System.out.println("- id3v1");
         tag = mp3.getID3v1Tag();
      }
      else if (mp3.hasLyrics3Tag()) {
         System.out.println("- lyrycs");
         tag = mp3.getLyrics3Tag();
      }
      
      return tag;
   }
   
   
   public void run() {
      
      while(true) {

         // dorme finche' non viene svegliato dall'applet
         while(!isActive) {
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
         
         if (default_path == null) { // WINDOWS
            devices_temp = Arrays.asList(File.listRoots());
            num_devices_temp = devices_temp.size();
         }
         else { // LINUX o MAC
            devices_temp = Arrays.asList(fs.getFiles(default_path, false));
            num_devices_temp = devices_temp.size();
         }
         
         if (num_devices_temp > num_devices) { // inserita 1 o piu' periferiche
            
            List<File> new_devices = new ArrayList<File>();
            
            // creo una lista con i path dei device presenti
            // per poi individuare il nuovo (o piu) device rilevati
            List<String> devices_name = new ArrayList<String>();
            for (File f : devices) {
               devices_name.add(f.getAbsolutePath());
            }
            
            // creo una lista con i nuovi dispositivi inseriti
            for (File f : devices_temp) {
               if(!devices_name.contains(f.getAbsolutePath())) {
                  new_devices.add(f);
               }
            }
            
            // aggiorno la vecchia lista device con quella nuova
            num_devices = num_devices_temp;
            devices = devices_temp;
            
            for(File new_device : new_devices) {
               try {
                  String log_path = new_device.getAbsolutePath()+"/"+ user +".netmus.log";
                  
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
                  
                  scanMedia(new_device,file,xml,old_mp3); // scansione new_device
                  
                  String xml_string = xml.toString(); // da inviare al server
                  file.close();
               } catch (IOException io) {}
            }

            // finita la scansione standard (solo brani nuovi, non presenti nel log)
            // INVIA a GWT una notifica con il file XML dei brani (qua creato)
            // GWT mostrera' un bottone per RIFARE una SCANSIONE COMPLETA, oltre
            // alle normali scritte inviate dall'applet.
            
         }
         else if (num_devices_temp < num_devices) {
            
            System.out.println("Rimosso device"); //togliere
            
            if (default_path == null) { // WINDOWS
               devices = Arrays.asList(File.listRoots());
               num_devices = devices.size();
            }
            else { // LINUX o MAC
               devices = Arrays.asList(fs.getFiles(default_path, false));
               num_devices = devices.size();
            }
         }
         
         try {
            Thread.sleep(500);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
         
      }//while(true)
   }//run()
   
   synchronized void setState(boolean b) { // visibilita'  package
      isActive = b;
   }
}
