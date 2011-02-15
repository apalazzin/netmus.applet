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
				temp = getGenreCode(brano.getSongGenre());
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
	
	public String getGenreCode(String StringCode){
		System.out.println(StringCode);
		if(StringCode.matches("[a-z[A-Z]]")) return StringCode;//the genre is already specified in a non-code format in the mp3 tag
		else{
			//let's transform the code in its correspondent genre name
			   StringCode=StringCode.replaceAll("\\D", "");
			   if(StringCode.isEmpty()) return "Unknown";//there wasn't a code or a name of the genre in the mp3 tag
			   System.out.println(StringCode);
				   int intCode = Integer.parseInt(StringCode);
				   String genre="";
				   switch(intCode){
				   		case 0: genre = "Blues"; break;
				   		case 1: genre = "Classic Rock"; break;
				   		case 2: genre = "Country"; break;
				   		case 3: genre = "Dance"; break;
				   		case 4: genre = "Disco"; break;
				   		case 5: genre = "Funk"; break;
				   		case 6: genre = "Grunge"; break;
				   		case 7: genre = "Hip Hop"; break;
				   		case 9: genre = "Metal"; break;
				   		case 10: genre = "New Age"; break;
				   		case 11: genre = "Oldies"; break;
				   		case 12: genre = "Other"; break;
				   		case 13: genre = "Pop"; break;
				   		case 14: genre = "R&B"; break;
				   		case 15: genre = "Rap"; break;
				   		case 16: genre = "Reggae"; break;
				   		case 17: genre = "Rock"; break;
				   		case 18: genre = "Techno"; break;
				   		case 19: genre = "Industrial"; break;
				   		case 20: genre = "Alternative"; break;
				   		case 21: genre = "Ska"; break;
				   		case 22: genre = "Death Metal"; break;
				   		case 23: genre = "Pranks"; break;
				   		case 24: genre = "Soundtrack"; break;
				   		case 25: genre = "Euro-Techno"; break;
				   		case 26: genre = "Ambient"; break;
				   		case 27: genre = "Trip-Hop"; break;
				   		case 28: genre = "Vocal"; break;
				   		case 29: genre = "Jazz+Funk"; break;
				   		case 30: genre = "Fusion"; break;
				   		case 31: genre = "Trance"; break;
				   		case 32: genre = "Classical"; break;
				   		case 33: genre = "Instrumental"; break;
				   		case 34: genre = "Acid"; break;
				   		case 35: genre = "House"; break;
				   		case 36: genre = "Game"; break;
				   		case 37: genre = "Sound Clip"; break;
				   		case 38: genre = "Gospel"; break;
				   		case 39: genre = "Noise"; break;
				   		case 40: genre = "Alternative Rock"; break;
				   		case 41: genre = "Bass"; break;
				   		case 42: genre = "Soul"; break;
				   		case 43: genre = "Punk"; break;
				   		case 44: genre = "Space"; break;
				   		case 45: genre = "Meditative"; break;
				   		case 46: genre = "Instrumental Pop"; break;
				   		case 47: genre = "Instrumental Rock"; break;
				   		case 48: genre = "Ethnic"; break;
				   		case 49: genre = "Gothic"; break;
				   		case 50: genre = "Darkwave"; break;
				   		case 51: genre = "Techno-Industrial"; break;
				   		case 52: genre = "Electronic"; break;
				   		case 53: genre = "Pop-Folk"; break;
				   		case 54: genre = "Eurodance"; break;
				   		case 55: genre = "Dream"; break;
				   		case 56: genre = "Southern Rock"; break;
				   		case 57: genre = "Comedy"; break;
				   		case 58: genre = "Cult"; break;
				   		case 59: genre = "Gangsta"; break;
				   		case 60: genre = "Top 40"; break;
				   		case 61: genre = "Christian Rap"; break;
				   		case 62: genre = "Pop/Funk"; break;
				   		case 63: genre = "Jungle"; break;
				   		case 64: genre = "Native US"; break;
				   		case 65: genre = "Cabaret"; break;
				   		case 66: genre = "New Wave"; break;
				   		case 67: genre = "Psychadelic"; break;
				   		case 68: genre = "Rave"; break;
				   		case 69: genre = "Showtunes"; break;
				   		case 70: genre = "Trailer"; break;
				   		case 71: genre = "Lo-Fi"; break;
				   		case 72: genre = "Tribal"; break;
				   		case 73: genre = "Acid Punk"; break;
				   		case 74: genre = "Acid Jazz"; break;
				   		case 75: genre = "Polka"; break;
				   		case 76: genre = "Retro"; break;
				   		case 77: genre = "Musical"; break;
				   		case 78: genre = "Rock & Roll"; break;
				   		case 79: genre = "Hard Rock"; break;
				   		case 80: genre = "Folk"; break;
				   		case 81: genre = "Folk-Rock"; break;
				   		case 82: genre = "National Folk"; break;
				   		case 83: genre = "Swing"; break;
				   		case 84: genre = "Fast Fusion"; break;
				   		case 85: genre = "Bebob"; break;
				   		case 86: genre = "Latin"; break;
				   		case 87: genre = "Revival"; break;
				   		case 88: genre = "Celtic"; break;
				   		case 89: genre = "Bluegrass"; break;
				   		case 90: genre = "Avantgarde"; break;
				   		case 91: genre = "Gothic Rock"; break;
				   		case 92: genre = "Progressive Rock"; break;
				   		case 93: genre = "Psychedelic Rock"; break;
				   		case 94: genre = "Symphonic Rock"; break;
				   		case 95: genre = "Slow Rock"; break;
				   		case 96: genre = "Big Band"; break;
				   		case 97: genre = "Chorus"; break;
				   		case 98: genre = "Easy Listening"; break;
				   		case 99: genre = "Acoustic"; break;
				   		case 100: genre = "Humour"; break;
				   		case 101: genre = "Speech"; break;
				   		case 102: genre = "Chanson"; break;
				   		case 103: genre = "Opera"; break;
				   		case 104: genre = "Chamber Musica"; break;
				   		case 105: genre = "Sonata"; break;
				   		case 106: genre = "Symphony"; break;
				   		case 107: genre = "Booty Bass"; break;
				   		case 108: genre = "Primus"; break;
				   		case 109: genre = "Porn Groove"; break;
				   		case 110: genre = "Satire"; break;
				   		case 111: genre = "Slow Jam"; break;
				   		case 112: genre = "Club"; break;
				   		case 113: genre = "Tango"; break;
				   		case 114: genre = "Samba"; break;
				   		case 115: genre = "Folklore"; break;
				   		case 116: genre = "Ballad"; break;
				   		case 117: genre = "Powee Ballad"; break;
				   		case 118: genre = "Rhythmic Soul"; break;
				   		case 119: genre = "Freestyle"; break;
				   		case 120: genre = "Duet"; break;
				   		case 121: genre = "Punk Rock"; break;
				   		case 122: genre = "Drum Solo"; break;
				   		case 123: genre = "Acapella"; break;
				   		case 124: genre = "Euro-House"; break;
				   		case 125: genre = "Dance Hall"; break;
				   		case 126: genre = "Goa"; break;
				   		case 127: genre = "Drum & Bass"; break;
				   		case 128: genre = "Club - House"; break;
				   		case 129: genre = "Hardcore"; break;
				   		case 130: genre = "Terror"; break;
				   		case 131: genre = "Indie"; break;
				   		case 132: genre = "BirtPop"; break;
				   		case 133: genre = "Negerpunk"; break;
				   		case 134: genre = "Polsk Punk"; break;
				   		case 135: genre = "Beat"; break;
				   		case 136: genre = "Christian Gangsta Rap"; break;
				   		case 137: genre = "Heavy Metal"; break;
				   		case 138: genre = "Black Metal"; break;
				   		case 139: genre = "Crossover"; break;
				   		case 140: genre = "Contemporary Christian"; break;
				   		case 141: genre = "Christian Rock"; break;
				   		case 142: genre = "Merengue"; break;
				   		case 143: genre = "Salsa"; break;
				   		case 144: genre = "Thrash Metal"; break;
				   		case 145: genre = "Anime"; break;
				   		case 146: genre = "JPop"; break;
				   		case 147: genre = "Synthpop"; break;
				   		default: genre = "Unknown"; break;
				   }
				   return genre;
			}
		
	   
	}

}
