package net.raphaelbaron.remotebox;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import android.util.Base64;

import net.raphaelbaron.remotebox.models.Track;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class MyXMLParser {
	
	// We don't use namespaces
    private static final String ns = null;
	
	//public List<Track> parse(InputStream in) throws XmlPullParserException, IOException {
    public List<Track> parse(StringReader ir) throws XmlPullParserException, IOException {
          try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            //parser.setInput(in, null);
            parser.setInput(ir);
            parser.nextTag(); //<xml>
            parser.nextTag(); //<tracks>
            return readTracks(parser);
        } finally {
            //in.close(); //Only for InputStream
        }
    }
	
	private List<Track> readTracks(XmlPullParser parser) throws XmlPullParserException, IOException {
	    List<Track> tracks = new ArrayList();

	    parser.require(XmlPullParser.START_TAG, ns, "tracks");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        // Starts by looking for the entry tag
	        if (name.equals("track")) {
	            tracks.add(readTrack(parser));
	        } else {
	            skip(parser);
	        }
	    }  
	    return tracks;
	}
	
	private Track readTrack(XmlPullParser parser) throws XmlPullParserException, IOException {
	    parser.require(XmlPullParser.START_TAG, ns, "track");
	    String artist = null;
	    String title = null;
	    String album = null;
	    String url = null;
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        if (name.equals("artist")) {
	            artist = readArtist(parser);
	        } else if (name.equals("title")) {
	            title = readTitle(parser);
	        } else if (name.equals("album")) {
	            album = readAlbum(parser);
	        } else if (name.equals("url")) {
	            url = readURL(parser);
	        } else {
	            skip(parser);
	        }
	    }
	    
	    //Decode Base64
	    artist = new String(Base64.decode(artist, Base64.DEFAULT));
	    title = new String(Base64.decode(title, Base64.DEFAULT));
	    album = new String(Base64.decode(album, Base64.DEFAULT));
	    url = new String(Base64.decode(url, Base64.DEFAULT));
	    
	    return new Track(artist, title, album, url);
	}


	private String readArtist(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, "artist");
	    String artist = readText(parser);
	    parser.require(XmlPullParser.END_TAG, ns, "artist");
	    return artist;
	}
	  

	private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, "title");
	    String title = readText(parser);
	    parser.require(XmlPullParser.END_TAG, ns, "title");
	    return title;
	}

	private String readAlbum(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, "album");
	    String title = readText(parser);
	    parser.require(XmlPullParser.END_TAG, ns, "album");
	    return title;
	}

	private String readURL(XmlPullParser parser) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, "url");
	    String url = readText(parser);
	    parser.require(XmlPullParser.END_TAG, ns, "url");
	    return url;
	}


	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }
}
