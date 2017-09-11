package net.raphaelbaron.remotebox.models;

public class Track {
	public String artist;
	public String title;
	public String album;
	public String url;
	
	public Track(String artist, String title, String album, String url) {
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.url = url;
    }
	
	@Override
	public String toString() {
		return this.artist+" - "+this.title+" (from "+this.album+")";
	}
}
