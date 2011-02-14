package applet;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.farng.mp3.AbstractMP3Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TranslateXML {
	
	//node signatures
	private final static String ROOT_NAME = "Library";
	private final static String SONG_NAME = "Song";
	private final static String ALBUMTITLE_NAME = "AlbumTitle";
	private final static String AUTHORCOMPOSER_NAME = "AuthorComposer";
	private final static String LEADARTIST_NAME = "LeadArtist";
	private final static String SONGGENRE_NAME = "SongGenre";
	private final static String SONGTITLE_NAME = "SongTitle";
	private final static String TRACKNUMBER_NAME = "TrackNumber";
	private final static String YEAR_NAME = "Year";
	//name of the attribute file:
	private final static String FILE_NAME = "File";
	
	private DocumentBuilder documentBuilder;
	private Document document;
	private Element root;
	
	
	public TranslateXML() throws ParserConfigurationException {
		//this operation can throw an instance of ParserConfigurationException: in fact, it will never been thrown in our program.
		documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		newMP3List();
	}
	
	public void newMP3List(){
		document = documentBuilder.newDocument();
		root = document.createElement(ROOT_NAME);
		document.appendChild(root);
	}
	
	private String clean(String stringa)
	{
		System.out.print(stringa);
		String t = stringa.replaceAll("\t\b\n\r\f\0", "");
		t = t.replaceAll("&[\\S^;]{1,6};","");
		System.out.println(" - "+t);
		
		
		return t;
	}
	
	public void addMP3(AbstractMP3Tag brano, String file){
		//create the new song node
		Element song = document.createElement(SONG_NAME);
		song.setAttribute(FILE_NAME, file);
		
		if (brano != null)
		{
			String temp = null;
			
			try {
				temp = brano.getAlbumTitle();
				if (temp != null && !temp.isEmpty())
				{
					//fill the new node with data.
					Element albumTitle = document.createElement(ALBUMTITLE_NAME);
					albumTitle.appendChild(document.createTextNode(temp));
					song.appendChild(albumTitle);
				}
			} catch (UnsupportedOperationException e){
				System.err.println("Operazione non supportata.");
			}
			
			try {
				temp = brano.getAuthorComposer();
				if (temp != null && !temp.isEmpty())
				{
					Element authorComposer = document.createElement(AUTHORCOMPOSER_NAME);
					authorComposer.appendChild(document.createTextNode(temp));
					song.appendChild(authorComposer);
				}
			} catch (UnsupportedOperationException e){
				System.err.println("Operazione non supportata.");
			}
			
			try {
				temp = brano.getLeadArtist();
				if (temp != null && !temp.isEmpty())
				{
					Element leadArtist = document.createElement(LEADARTIST_NAME);
					leadArtist.appendChild(document.createTextNode(temp));
					song.appendChild(leadArtist);
				}
			} catch (UnsupportedOperationException e){
				System.err.println("Operazione non supportata.");
			}
			
			try {
				temp = brano.getSongGenre();
				if (temp != null && !temp.isEmpty())
				{
					Element songGenre = document.createElement(SONGGENRE_NAME);
					songGenre.appendChild(document.createTextNode(temp));
					song.appendChild(songGenre);
				}
			} catch (UnsupportedOperationException e){
				System.err.println("Operazione non supportata.");
			}
			
			try {
				temp = brano.getSongTitle();
				if (temp != null && !temp.isEmpty())
				{
					Element songTitle = document.createElement(SONGTITLE_NAME);
					songTitle.appendChild(document.createTextNode(temp));
					song.appendChild(songTitle);
				}
			} catch (UnsupportedOperationException e){
				System.err.println("Operazione non supportata.");
			}
			
			try {
				temp = brano.getTrackNumberOnAlbum();
				if (temp != null && !temp.isEmpty())
				{
					Element trackNumber = document.createElement(TRACKNUMBER_NAME);
					trackNumber.appendChild(document.createTextNode(temp));
					song.appendChild(trackNumber);
				}
			} catch (UnsupportedOperationException e){
				System.err.println("Operazione non supportata.");
			}
			
			try {
				temp = brano.getYearReleased();
				if (temp != null && !temp.isEmpty())
				{
					Element year = document.createElement(YEAR_NAME);
					year.appendChild(document.createTextNode(temp));
					song.appendChild(year);
				}
			} catch (UnsupportedOperationException e){
				System.err.println("Operazione non supportata.");
			}
		}
			
		//add the new node to the tree
		root.appendChild(song);
	}
	
	public String toString(){
		Source source = new DOMSource(document);
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult(stringWriter);
        try {
			Transformer t=TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-15");
			//t.setOutputProperty(OutputKeys.ENCODING,"UTF16");
			t.transform(source, result);
			return clean(stringWriter.getBuffer().toString());
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		return null;
	}
}
