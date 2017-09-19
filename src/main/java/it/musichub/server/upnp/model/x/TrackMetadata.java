package it.musichub.server.upnp.model.x;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.dlna.DLNAProfiles;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class TrackMetadata {

	private final static Logger logger = Logger.getLogger(TrackMetadata.class);

	public String id;
	public String parentId;
	public String title;
	public String artist;
	public String genre;
	public String artURI;
	public Res res;
	public String itemClass;
	
	@Override
	public String toString()
	{
		return "TrackMetadata [id=" + id + ", parentId=" + parentId + ", title=" + title + ", artist=" + artist + ", genre=" + genre + ", artURI="
				+ artURI + ", res.value=" + res.getValue() + ", itemClass=" + itemClass + "]";
	}

	public TrackMetadata(String xml) {
		parseTrackMetadata(xml);
	}

	public TrackMetadata() {
	}

	public TrackMetadata(String id, String parentId, String title, String artist, String genre, String artURI,
			Res res, String itemClass) {
		super();
		this.id = id;
		this.parentId = parentId;
		this.title = title;
		this.artist = artist;
		this.genre = genre;
		this.artURI = artURI;
		this.res = res;
		this.itemClass = itemClass;
	}

	private XMLReader initializeReader() throws ParserConfigurationException, SAXException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		XMLReader xmlreader = parser.getXMLReader();
		return xmlreader;
	}

	public void parseTrackMetadata(String xml)
	{
		if (xml == null)
			return;

		logger.debug("XML : " + xml);

		try
		{
			XMLReader xmlreader = initializeReader();
			UpnpItemHandler upnpItemHandler = new UpnpItemHandler();

			xmlreader.setContentHandler(upnpItemHandler);
			xmlreader.parse(new InputSource(new StringReader(xml)));
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			logger.warn("Error while parsing metadata !");
			logger.warn("XML : " + xml);
		}
	}

	public String getXML()
	{
//		XmlSerializer s = Xml.newSerializer();
		XmlSerializer s;
		try {
			s = XmlPullParserFactory.newInstance().newSerializer();
		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		StringWriter sw = new StringWriter();

		try {
			s.setOutput(sw);

			s.startDocument(null,null);
//			s.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

			//start a tag called "root"
			s.startTag(null, "DIDL-Lite");
			s.attribute(null, "xmlns", "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/");
			s.attribute(null, "xmlns:dc", "http://purl.org/dc/elements/1.1/");
			s.attribute(null, "xmlns:upnp", "urn:schemas-upnp-org:metadata-1-0/upnp/");
			s.attribute(null, "xmlns:dlna", "urn:schemas-dlna-org:metadata-1-0/");

			s.startTag(null, "item");
			s.attribute(null, "id", ""+id);
			s.attribute(null, "parentID", ""+parentId);
			s.attribute(null, "restricted", "1");

			if(title!=null)
			{
				s.startTag(null, "dc:title");
				s.text(title);
				s.endTag(null, "dc:title");
			}

			if(artist!=null)
			{
				s.startTag(null, "dc:creator");
				s.text(artist);
				s.endTag(null, "dc:creator");
			}

			if(genre!=null)
			{
				s.startTag(null, "upnp:genre");
				s.text(genre);
				s.endTag(null, "upnp:genre");
			}

			if(artURI!=null)
			{
				s.startTag(null, "upnp:albumArtURI");
				s.attribute(null, "dlna:profileID", DLNAProfiles.JPEG_TN.getCode()); //TODO: should use different album arts based on size: e.g. https://yabb.jriver.com/interact/index.php?topic=73954.0
				s.text(artURI);
				s.endTag(null, "upnp:albumArtURI");
			}

			if(res!=null)
			{
				s.startTag(null, "res");
				if (res.getBitrate() != null)
					s.attribute(null, "bitrate", Long.toString(res.getBitrate()));
				if (res.getDuration() != null)
					s.attribute(null, "duration", res.getDuration());
				if (res.getSize() != null)
					s.attribute(null, "size", Long.toString(res.getSize()));
				if (res.getProtocolInfo() != null)
					s.attribute(null, "protocolInfo", res.getProtocolInfo().toString());
				s.text(res.getValue());
				s.endTag(null, "res");
			}

			if(itemClass!=null)
			{
				s.startTag(null, "upnp:class");
				s.text(itemClass);
				s.endTag(null, "upnp:class");
			}

			s.endTag(null, "item");

			s.endTag(null, "DIDL-Lite");

			s.endDocument();
			s.flush();

		} catch (Exception e) {
			logger.error("error occurred while creating xml file : " + e.toString() );
			e.printStackTrace();
		}

		String xml = sw.toString();
		logger.debug("TrackMetadata : " + xml);

		return xml;
	}

	public class UpnpItemHandler extends DefaultHandler {

		private final StringBuffer buffer = new StringBuffer();

		@Override
		public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
				throws SAXException
		{
			buffer.setLength(0);
			// Log.v(TAG, "startElement, localName="+ localName + ", qName=" + qName);

			if (localName.equals("item"))
			{
				id = atts.getValue("id");
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			// Log.v(TAG, "endElement, localName="+ localName + ", qName=" + qName + ", buffer=" +
			// buffer.toString());

			if (localName.equals("title"))
			{
				title = buffer.toString();
			}
			else if (localName.equals("creator"))
			{
				artist = buffer.toString();
			}
			else if (localName.equals("genre"))
			{
				genre = buffer.toString();
			}
			else if (localName.equals("albumArtURI"))
			{
				artURI = buffer.toString();
			}
			else if (localName.equals("class"))
			{
				itemClass = buffer.toString();
			}
			else if (localName.equals("res"))
			{
//				res = buffer.toString();
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
		{
			buffer.append(ch, start, length);
		}
	}
}