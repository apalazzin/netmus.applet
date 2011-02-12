package applet;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
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
	private final static String FILE_NAME = "File";
	
	private DocumentBuilder documentBuilder;
	private Document document;
	private Element root;
	
	
	public TranslateXML() throws ParserConfigurationException {
		//this operation can throw an instance of ParserConfigurationException: in fact, it will never been thrown in our program.
		documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		newMP3List();
	};
	
	public void newMP3List(){
		document = documentBuilder.newDocument();
		root = document.createElement(ROOT_NAME);
		document.appendChild(root);
	};
	
	public void addMP3(AbstractMP3Tag brano, String file){
		//create the new song node
		Element song = document.createElement(SONG_NAME);
		//fill the new node with data.
		Element albumTitle = document.createElement(ALBUMTITLE_NAME);
		albumTitle.appendChild(document.createTextNode(brano.getAlbumTitle()));
		song.appendChild(albumTitle);
		
		Element authorComposer = document.createElement(AUTHORCOMPOSER_NAME);
		authorComposer.appendChild(document.createTextNode(brano.getAuthorComposer()));
		song.appendChild(authorComposer);
		
		Element leadArtist = document.createElement(LEADARTIST_NAME);
		leadArtist.appendChild(document.createTextNode(brano.getLeadArtist()));
		song.appendChild(leadArtist);
		
		Element songGenre = document.createElement(SONGGENRE_NAME);
		songGenre.appendChild(document.createTextNode(brano.getSongGenre()));
		song.appendChild(songGenre);
		
		Element songTitle = document.createElement(SONGTITLE_NAME);
		songTitle.appendChild(document.createTextNode(brano.getSongTitle()));
		song.appendChild(songTitle);
		
		Element trackNumber = document.createElement(TRACKNUMBER_NAME);
		trackNumber.appendChild(document.createTextNode(brano.getTrackNumberOnAlbum()));
		song.appendChild(trackNumber);
		
		Element year = document.createElement(YEAR_NAME);
		year.appendChild(document.createTextNode(brano.getYearReleased()));
		song.appendChild(year);
		
		//filename node
		Element fileNode = document.createElement(FILE_NAME);
		fileNode.appendChild(document.createTextNode(file));
		song.appendChild(fileNode);
		
		//add the new node to the tree
		root.appendChild(song);
	};
	
	public String toString(){
		Source source = new DOMSource(document);
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult(stringWriter);
        try {
			TransformerFactory.newInstance().newTransformer().transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		return null;
	};
}
