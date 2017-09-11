package net.raphaelbaron.remotebox.data;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Collections;
import java.util.Comparator;

import android.util.Log;

import net.raphaelbaron.remotebox.models.Track;
import net.raphaelbaron.remotebox.models.Node;
import net.raphaelbaron.remotebox.models.TrackNode;

/*
 * Tree implementation:
 * 
 * 		Root
 * 			Artists
 * 				Albums
 * 					Tracks
 * 
 */

public class RemoteboxData {
	static private List<Track> tracks;
	
	static public Node root = new Node("RootNode");
	
	public static synchronized void setTracks(List<Track> newTracks) {
		RemoteboxData.tracks = newTracks;
	}
	
	public static synchronized List<Track> getTracks() {
		return RemoteboxData.tracks;
	}
	
	public static synchronized void parseTrackList(List<Track> tracks) {
		int size = tracks.size();
		String newAlbum = "", newArtist = "", newTitle = "";
		
		Node artistNode, albumNode;
		
		int index;
		Node newArtistNode, newAlbumNode, newTrackNode;
		
		//Create Root node and first artist node and album node
		//root = new Node("RootNode");
		
		if(tracks.size() > 0) {
			/*
			//Add artist0 to root node
			artistNode =  new Node(tracks.get(0).artist);
			root.addChild(artistNode);
			
			//Add album0 to first artist
			albumNode = new Node(tracks.get(0).album);
			artistNode.addChild(albumNode);
			*/
			
			for(int i=0; i<size; i++) {
				/*
				newArtist = tracks.get(i).artist;
				newAlbum = tracks.get(i).album;
				newTitle = tracks.get(i).title;
				
				
				//Is it the same album and artist?
				if(newAlbum.equalsIgnoreCase(albumNode.name) && newArtist.equalsIgnoreCase(artistNode.name)) {
					//Add new track node last album node
					albumNode.addChild(new Node(newTitle));
				}
				//Or is it the same artist but a new album
				else if(newArtist.equalsIgnoreCase(artistNode.name)) {
					//Create a new album node
					albumNode = new Node(newAlbum);
					artistNode.addChild(albumNode);
					
					//Add track to new album
					albumNode.addChild(new Node(newTitle)); 
				}
				//Or is it a completely new artist?
				else {
					//Add new artist to root
					artistNode = new Node(newArtist);
					root.addChild(artistNode);
					
					//Add new album to artist
					albumNode = new Node(newAlbum);
					artistNode.addChild(albumNode);
					
					//Add new track to new album
					albumNode.addChild(new Node(newTitle));
				}
				*/
				
				newArtistNode = new Node(tracks.get(i).artist);
				newAlbumNode = new Node(tracks.get(i).album);
				//newTrackNode = new Node(tracks.get(i).title);
				newTrackNode = new TrackNode(tracks.get(i).artist, tracks.get(i).album, tracks.get(i).title, tracks.get(i).url);
				
				
				//Is there already an artist with the same name?
				//if((index = root.children.indexOf(newArtistNode)) != -1) {
				if((index = existsOnIndex(root.children, newArtistNode)) != -1) {
					//Artist already exists
					artistNode = root.children.get(index);
					
					//Does the album already exist?
					//if((index = artistNode.children.indexOf(newAlbumNode)) != -1) {
					if((index = existsOnIndex(artistNode.children, newAlbumNode)) != -1) {
						//Album already exists
						albumNode = artistNode.children.get(index);
						
						//Add new track to album
						albumNode.addChild(newTrackNode);						
					}
					
					//Album does not exist
					else {
						//Create album and add track
						artistNode.addChild(newAlbumNode);
						
						//PODE TER PROBLEMA AQUI?
						newAlbumNode.addChild(newTrackNode);	
					}
				}
				
				//Artist does not exist
				else {
					//Add artist, album and track
					root.addChild(newArtistNode);
					
					newArtistNode.addChild(newAlbumNode);
					
					newAlbumNode.addChild(newTrackNode);
				}
			}
		}
		
		//Order!
		Collections.sort(root.children, new Comparator() {
			public int compare(Object a, Object b) {
			    return ((Node) a).toString().compareTo(((Node) b).toString());
			  }
		});
	}
	
	private static synchronized int existsOnIndex(Vector<Node> v, Node n) {
		//for(Iterator<Node>i = v.iterator(); i.hasNext(); i.next()) {
		for(int i = 0; i < v.size(); i++) {
			if(v.get(i).name.compareToIgnoreCase(n.name) == 0) {
				//Log.d(null, "Achei");
				return i;
			}
		}
		return -1;
	}
}
