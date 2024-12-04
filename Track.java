package com.example.javafxmedia;

public class Track {
    //variables
    private String title;
    private String artist;
    private String path;
    private boolean linked = false;;

    //constructors
    public Track(String title, String artist, String path){
        this.title = title;
        this.artist = artist;
        this.path = path;
    }

    //getters
    public String getTitle(){
        return title;
    }
    public String getArtist(){
        return artist;
    }
    public String getPath(){
        return path;
    }
    public boolean getLinked(){
        return linked;
    }

    //setters
    public void setTitle(String title){
        this.title = title;
    }
    public void setArtist(String artist){
        this.artist = artist;
    }
    public void setPath(String path){
        this.path = path;
    }
    public void setLinked(boolean linked){
        this.linked = linked;
    }
}
