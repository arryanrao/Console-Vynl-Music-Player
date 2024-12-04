package com.example.javafxmedia;
import java.util.ArrayList;

public class Playlist {
    //variables
    private String title;
    private ArrayList<Track> tracks = new ArrayList<Track>();

    //constructor (playlist begins empty)
    public Playlist(String title){
        this.title = title;
    }

    //getters
    public String getTitle(){
        return title;
    }
    public ArrayList<Track> getTracks(){
        return tracks;
    }

    //setters
    public void setTitle(String title){
        this.title = title;
    }
    public void add(Track track){
        tracks.add(track);
    }
    public void remove(Track track){
        tracks.remove(track);
    }
}
