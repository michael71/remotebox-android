package net.raphaelbaron.remotebox.models;

public class TrackNode extends Node {
	public String artist, album, title, url;
	public int duration;
	
	public TrackNode(String artist, String album, String title, String url) {
		super("TrackNode");
		
		this.artist = artist;
		this.album = album;
		this.title = title;
		this.url = url;
	}
	
	@Override
	public String toString() {
		return this.title;
	}
}
