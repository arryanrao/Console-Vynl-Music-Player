package com.example.javafxmedia;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class VynlController {
    private MediaPlayer mediaPlayer;
    private ArrayList<Playlist> playlists = new ArrayList<>();
    private ArrayList<Link> links = new ArrayList<>();
    private ArrayList<Integer> queue = new ArrayList<>();

    private int currentTrack;
    private int currentPlaylist;
    private boolean playing = true;
    private boolean shuffle = false;

    private boolean tempSetup = false;


    String PURPLE ="\u001b[35m";
    String GREEN = "\u001B[32m";
    String RED = "\u001B[31m";
    String BOLD = "\033[1m";
    String NORMAL = "\033[0m";

    // constructor
    public VynlController() throws IOException {
        // Initialize playlists and read tracks from the CSV file
        tempSetup= false;
        initializePlaylists();
    }

    // initialize (load) playlists
    protected void initializePlaylists() throws IOException {
        //variables
        boolean check = false;
        int first = 0;
        int counter = -1;

        //reading playlists file
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\zhouc\\Downloads\\School\\Programming\\Grade 12 Computer Science\\Console Vynl\\src\\main\\resources\\com\\example\\javafxmedia\\CSVs\\Playlists.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                //"**********" tells the program that this is a new playlist and that the next line is a title
                if (line.equals("**********")){
                    check = true;
                    first++;
                } else if (check){
                    Playlist playlist = new Playlist(line);
                    playlists.add(playlist);
                    check = false;
                    counter++;
                }

                //the other lines are all tracks
                else {
                    if (first == 1){ //on the first run-through, it loads the track information
                        String[] values = line.split(",%,");
                        String path = "C:\\Users\\zhouc\\Downloads\\School\\Programming\\Grade 12 Computer Science\\Console Vynl\\src\\main\\resources\\com\\example\\javafxmedia\\Tracks\\" + values[2];
                        Track track = new Track(values[0], values[1], path);
                        playlists.get(counter).add(track);
                    } else { //only the first playlist saves track info. the rest are indexes referencing the first playlist
                        int index = Integer.parseInt(line);
                        Track track = playlists.get(0).getTracks().get(index);
                        playlists.get(counter).add(track);
                    }
                }
            }
        }

        //reading links file (same concepts are playlists but no track information, only indexes)
        counter = -1;
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\zhouc\\Downloads\\School\\Programming\\Grade 12 Computer Science\\Console Vynl\\src\\main\\resources\\com\\example\\javafxmedia\\CSVs\\Links.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals("**********")){
                    check = true;
                } else if (check){
                    Link link = new Link(line);
                    links.add(link);
                    check = false;
                    counter++;
                } else {
                    int index = Integer.parseInt(line);
                    Track track = playlists.get(0).getTracks().get(index);
                    links.get(counter).add(track);
                }
            }
        }


        // initialize the queue with all tracks
        for (int i = 0; i < playlists.get(0).getTracks().size(); i++) {
            queue.add(i);
        }


        // start with the first track from all tracks
        currentPlaylist = 0;
        currentTrack = 0;
        playMusic();

    }




    // MUSIC METHODS
    // plays the music
    void playMusic() {
        //mediaplayer setup, sets a new song if the track is changed
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        Track current = playlists.get(0).getTracks().get(queue.get(currentTrack));
        String path = current.getPath(); // Modify this to point to the actual file location
        Media media = new Media(new File(path).toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
            playing = true;
        });

        //background task that checks when the song is nearing the end
        //always running so it goes to next-song even if the user doesn't make any inputs
        new Thread(new Runnable() {
            @Override public void run() {
                while(true) {
                    if ((mediaPlayer.getTotalDuration().toSeconds() - mediaPlayer.getCurrentTime().toSeconds()) < 1){
                        onNext();
                        System.out.println();
                    }
                }
            }
        }).start();

        //main menu loop
        while (true){
            space();

            //prints program name on first run-through
            if (!tempSetup){
                System.out.print("\n-------------------------------------\n" +
                        BOLD + PURPLE + "WELCOME TO VYNL\n" + NORMAL + "Created by ABC Studios");
            }

            Scanner scanner = new Scanner(System.in);
            String response = "";

            // handles the initial gui on the screen
            System.out.println("\n-------------------------------------\n" + BOLD + PURPLE + "HOME" + NORMAL + "\n-------------------------------------");
            // changes based on whether the song is playing, paused, or if it's the first run-through
            if (!tempSetup){
                System.out.print(RED + "[▷] Paused: " + NORMAL + "[N/A]");
            } else if(playing) {
                System.out.print(GREEN + "[▶] Now Playing: " + NORMAL +
                        playlists.get(0).getTracks().get(queue.get(currentTrack)).getTitle()
                        + " by " +playlists.get(0).getTracks().get(queue.get(currentTrack)).getArtist() +
                        " from [" + playlists.get(currentPlaylist).getTitle() + "]");
            } else{
                System.out.print(RED + "[▷] Paused: " + NORMAL + playlists.get(0).getTracks().get(queue.get(currentTrack)).getTitle()
                        + " by " +playlists.get(0).getTracks().get(queue.get(currentTrack)).getArtist() +
                        " from [" + playlists.get(currentPlaylist).getTitle() + "]");
            }


            System.out.print(
                    BOLD + "\n[`]" + NORMAL + " Pause/Play\n" +
                    BOLD + "[1]" + NORMAL + " Prev Song\n" +
                    BOLD + "[2]" + NORMAL + " Next Song\n" +
                    BOLD + "[3]" + NORMAL + " Queue\n"+
                    BOLD + "[4]" + NORMAL + " Toggle Shuffle [Currently: ");

            if (shuffle){
                System.out.print(GREEN + "ON" + NORMAL);
            } else {
                System.out.print(RED + "OFF" + NORMAL);
            }

            System.out.print("]\n" +
                    "-------------------------------------\n" +
                    BOLD + "[5]" + NORMAL + " Your Playlists\n" +
                    BOLD + "[6]" + NORMAL + " Your Linked Songs\n" +
                    BOLD + "[Q]" + NORMAL + RED + " QUIT PLAYER\n" + NORMAL +
                    "-------------------------------------\n");
            System.out.print("> ");

            //pauses the music on launch
            if (!tempSetup) {
                onPlayPause();
                tempSetup = true;
            }

            response = scanner.next();
            response = response.toUpperCase();

            space();

            // handles responses and runs methods for user input
            if (response.equals("`")){
                onPlayPause();
            } else if (response.equals("1")){
                onPrev();
                playing = true;
                playMusic();
            } else if (response.equals("2")){
                onNext();
                playing = true;
                playMusic();
            } else if (response.equals("3")){
                viewQueue();
            } else if (response.equals("4")){
                onShuffleToggle();
                playing = true;
                playMusic();
            } else if (response.equals("5")){
                playlistMenu();
            } else if (response.equals("6")){
                linkMenu();
            } else if (response.equals("Q")){
                save();
                System.out.println("\n-------------------------------------\n" + PURPLE + BOLD + " THANK YOU FOR USING VYNL" + NORMAL + "\n-------------------------------------\n");
                shutdown();
                System.exit(0);
            } else {
                System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
            }
        }
    }

    // pauses the music
    protected void onPlayPause() {
        if (playing){
            mediaPlayer.pause();
            playing = false;
        } else {
            mediaPlayer.play();
            playing = true;
        }
    }

    // previous song
    protected void onPrev() {
        if (currentTrack > 0) {
            currentTrack--;
        } else { // loop to the last track
            currentTrack = queue.size() - 1;
        }
    }

    // next song
    protected void onNext() {
        if (currentTrack < queue.size() - 1) {
            currentTrack++;
        } else { // loop back to the first track
            currentTrack = 0;
        }
    }

    // toggles the shuffle
    protected void onShuffleToggle() {
        // clears and repopulates the queue
        queue.clear();
        for (int i = 0; i < playlists.get(currentPlaylist).getTracks().size(); i++) {
            queue.add(playlists.get(0).getTracks().indexOf(playlists.get(currentPlaylist).getTracks().get(i)));
        }

        if (!shuffle) { // randomizes the queue
            Collections.shuffle(queue);
            shuffle = true;
        } else {
            shuffle = false;
        }

        //checks whether the queue contains any linked songs
        for (int i = 0; i < queue.size(); i++){
            // if it does, it replaces the song with the entire link
            if (checkLink(playlists.get(0).getTracks().get(queue.get(i))) != null){
                Link link = checkLink(playlists.get(0).getTracks().get(queue.get(i)));
                queue.remove(i);
                for (int j = 0; j < link.getTracks().size(); j++){
                    queue.add(i+j, playlists.get(0).getTracks().indexOf(link.getTracks().get(j)));
                }
                i += link.getTracks().size()-1;
            }
        }

        // reset to the beginning of the queue
        currentTrack = 0;
    }




    // QUEUE METHODS
    // queue menu loop
    protected void viewQueue(){
        while (true){
            Scanner scanner = new Scanner(System.in);
            String response = "";

            System.out.println("\n--------------------------------------------------\n" +
                    BOLD + PURPLE + "QUEUE" + NORMAL +
                    "\n--------------------------------------------------");

            //displays the current track
            Track track = playlists.get(0).getTracks().get(queue.get(currentTrack));
            if (playing){
                System.out.println(BOLD + GREEN + "[▶ NOW PLAYING] " + NORMAL + track.getTitle() + " by " + track.getArtist());
            } else {
                System.out.println(BOLD + RED + "[▷ PAUSED] " + NORMAL + track.getTitle() + " by " + track.getArtist());
            }

            System.out.println("--------------------------------------------------\n" + BOLD + "[NEXT IN QUEUE]" + NORMAL);

            //displays the queue
            int orderNumber = 1;
            for (int i = currentTrack+1; i < queue.size(); i++) {       //for each track, prints index, artist, and title
                track = playlists.get(0).getTracks().get(queue.get(i));
                System.out.println(orderNumber + ". " + track.getTitle() + " by " + track.getArtist());
                orderNumber++;
            }

            System.out.print("--------------------------------------------------" +
                    BOLD + "\n[`]" + NORMAL + " Pause/Play\n" +
                    BOLD + "[1]" + NORMAL + " Prev Song\n" +
                    BOLD + "[2]" + NORMAL + " Next Song\n" +
                    BOLD + "[3]" + NORMAL + " Add to Queue\n"+
                    BOLD + "[4]" + NORMAL + " Refresh Queue\n"+
                    BOLD + RED + "[B]" + NORMAL + " BACK\n" + NORMAL +
                    "--------------------------------------------------");
            System.out.print("> ");

            response = scanner.next();
            response = response.toUpperCase();

            space();

            if (response.equals("`")){
                onPlayPause();
            } else if (response.equals("1")){
                onPrev();
                playing = true;
                playMusic();
            } else if (response.equals("2")){
                onNext();
                playing = true;
                playMusic();
            } else if (response.equals("3")){
                addToQueue();
            } else if (response.equals("4")){
                onShuffleToggle();
                onShuffleToggle();
                playing = true;
                playMusic();
            }else if (response.equals("B")){
                return;
            } else {
                System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
            }
        }
    }

    // adding songs to the queue
    protected void addToQueue() {
        Scanner scanner = new Scanner(System.in);
        String response = "";

        System.out.println("\n-------------------------------------\n" +
                BOLD + PURPLE + "Adding songs to the queue: " + NORMAL +
                "\n-------------------------------------");

        // Display all tracks from the 'All Tracks' playlist
        for (int i = 0; i < playlists.get(0).getTracks().size(); i++) { //validates user input is within song range
            Track track = playlists.get(0).getTracks().get(i);
            System.out.println(BOLD + "[" + i + "] " + NORMAL + track.getTitle() + " by " + track.getArtist());
        }
        System.out.print("-------------------------------------\n> ");

        response = scanner.next();
        response = response.toUpperCase();

        if (response.equals("D")) {
            return; // Exit the method if 'D' is entered
        }

        try {
            int trackIndex = Integer.parseInt(response);
            if (trackIndex >= 0 && trackIndex < playlists.get(0).getTracks().size()) {
                queue.add(trackIndex); // Add the index to the end of the queue
                space();
                System.out.println("----------------------------" +
                        GREEN + "\nTrack added to queue\n" + NORMAL +
                        "----------------------------");
            } else {
                System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
            }
        } catch (NumberFormatException e) {
            System.out.println(RED + "ERROR Invalid Input" + NORMAL);
        }
    }




    // PLAYLIST METHODS
    // playlist menu loop
    protected void playlistMenu(){
        while (true){
            Scanner scanner = new Scanner(System.in);
            String response = "";

            System.out.println(
                    "\n-------------------------------------\n" +
                    BOLD + PURPLE + "PLAYLISTS" + NORMAL +
                    "\n-------------------------------------");

            for (int i = 0; i < playlists.size(); i++){
                System.out.print(BOLD + "[" + (i+1) + "] " + NORMAL + playlists.get(i).getTitle() + "\n");
            }

            System.out.println("-------------------------------------\n" +
                    BOLD + "[A]" + NORMAL + " Create Playlist\n" +
                    BOLD + RED + "[B]" + NORMAL + " BACK\n" + NORMAL +
                    "-------------------------------------");

            System.out.print("> ");

            response = scanner.next();
            response = response.toUpperCase();

            space();

            if (response.equals("A")){
                createPlaylist();
                save();
            } else if (response.equals("B")){
                return;
            } else {
                boolean check = false;
                try {
                    if (Integer.valueOf(response) >= 1 && Integer.valueOf(response) <= playlists.size()) {
                        check = true;
                    }
                } catch (Exception e){
                    System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
                }

                if (check){
                    viewPlaylist(Integer.valueOf(response)-1);
                } else {
                    System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
                }
            }
        }
    }

    // interface for viewing a selected playlist
    protected void viewPlaylist(int num){
        while (true){
            Scanner scanner = new Scanner(System.in);
            String response = "";

            Playlist playlist = playlists.get(num);

            System.out.println(
                    "\n-------------------------------------\n" +
                            BOLD + PURPLE + playlist.getTitle().toUpperCase() + NORMAL +
                            "\n-------------------------------------");
            for (int i = 0; i < playlist.getTracks().size(); i++){
                System.out.println(BOLD + "> " + NORMAL + playlist.getTracks().get(i).getTitle() + " by " + playlist.getTracks().get(i).getArtist());
            }

            if (playlist.getTracks().isEmpty()){
                System.out.println("Playlist is empty");
            }

            System.out.println("-------------------------------------\n"+
                    BOLD + "[P]" + NORMAL + " Play\n" + NORMAL +
                    BOLD  + "[A]" + NORMAL + " Add Songs\n" + NORMAL +
                    BOLD + "[R]" + NORMAL + " Remove Songs\n" + NORMAL +
                    BOLD + RED + "[D]" + NORMAL + " Delete Playlist\n" +
                    BOLD + RED +"[B]" + NORMAL + " BACK\n" + NORMAL +
                    "-------------------------------------"
            );

            System.out.print("> ");
            response = scanner.next();
            response = response.toUpperCase();

            space();

            if (response.equals("P")){
                if (!playlist.getTracks().isEmpty()){
                    currentPlaylist = num;
                    currentTrack = 0;

                    onShuffleToggle();
                    onShuffleToggle();

                    playing = true;

                    playMusic();
                } else {
                    System.out.println(RED + "\nERROR: Unable to play an empty playlist" + NORMAL);
                }
            }
            else if (response.equals("A")){
                if (playlist == playlists.get(0)){
                    System.out.println(RED + "ERROR: Cannot add to this playlist" + NORMAL);
                } else {
                    addSongsP(num);
                    save();
                }
            } else if (response.equals("R")){
                if (playlist == playlists.get(0)){
                    System.out.println(RED + "ERROR: Cannot remove from this playlist" + NORMAL);
                } else {
                    removeSongsP(num);
                    save();
                }
            } else if (response.equals("D")) {
                if (playlist == playlists.get(0)){
                    System.out.println(RED + "ERROR: Cannot delete this playlist" + NORMAL);
                } else {
                    while (true) {
                        System.out.print("-------------------------------------\n" + RED + BOLD + "[WARNING]" + NORMAL + " Are you sure you want to delete this playlist?\n-------------------------------------\n" + BOLD + "[Y/N]\n> " + NORMAL);
                        response = scanner.next();
                        response = response.toUpperCase();

                        if (response.equals("Y")){
                            if (playlist == playlists.get(currentPlaylist)){
                                currentPlaylist = 0;
                                currentTrack = 0;

                                onShuffleToggle();
                                onShuffleToggle();
                            }
                            playlists.remove(playlist);
                            space();
                            System.out.println("-------------------------------------\n" + RED + BOLD + "Playlist Deleted\n" + NORMAL + "-------------------------------------");
                            save();
                            break;
                        } else if (response.equals("N")) {
                            return;
                        } else {
                            System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
                        }
                    }
                    save();
                    return;
                }
            } else if (response.equals("B")) {
                return;
            } else {
                System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
            }
        }
    }

    // create a playlist
    protected void createPlaylist(){
        Scanner scanner = new Scanner(System.in);
        String response = "";
        System.out.print("------------------\n" +
                PURPLE + "CREATING A PLAYLIST" + NORMAL +
                "\n------------------" +
                "\nInput Title: ");
        response = scanner.next();

        Playlist playlist = new Playlist(response);
        playlists.add(playlist);

        space();
        System.out.println("------------------\n"
                + GREEN + "PLAYLIST CREATED" + NORMAL +
                "\n------------------");;
    }

    // add a song to a selected playlist
    protected void addSongsP(int num){
        while (true){
            Scanner scanner = new Scanner(System.in);
            String response = "";

            System.out.println(
                    "\n-------------------------------------\n" +
                            BOLD + PURPLE + "Adding Songs To: " + NORMAL + playlists.get(num).getTitle().toUpperCase() +
                            "\n-------------------------------------");
            // creates the checkmark icon for the chosen songs
            for (int i = 0; i < playlists.get(0).getTracks().size(); i++){
                if (playlists.get(num).getTracks().contains(playlists.get(0).getTracks().get(i))){
                    System.out.println(BOLD + "[" + "✓" + "] " + NORMAL + playlists.get(0).getTracks().get(i).getTitle() + " by " + playlists.get(0).getTracks().get(i).getArtist());
                } else {
                    System.out.println(BOLD + "[" + i + "] " + NORMAL + playlists.get(0).getTracks().get(i).getTitle() + " by " + playlists.get(0).getTracks().get(i).getArtist());
                }
            }
            System.out.println("-------------------------------------" + BOLD + GREEN + "\n[D]" + NORMAL + " Done" + NORMAL + "\n-------------------------------------");

            System.out.print("> ");
            response = scanner.next();
            response = response.toUpperCase();

            space();

            // ends loop if user is done choosing, same functionality as other methods
            if (response.equals("D")){
                return;
            }

            try {
                int temp = Integer.valueOf(response);
                if (temp >= 0 && temp <= playlists.get(0).getTracks().size()){
                    playlists.get(num).add(playlists.get(0).getTracks().get(temp));
                } else {
                    System.out.println(RED + "\nERROR: Invalid Input" + NORMAL);
                }
            } catch (Exception e){
                System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
            }
        }
    }

    // remove songs from a selected playlist
    protected void removeSongsP(int num){
        while (true){
            Scanner scanner = new Scanner(System.in);
            String response = "";

            System.out.println(
                    "\n-------------------------------------\n" +
                            BOLD + PURPLE + "Removing songs from: " + NORMAL + playlists.get(num).getTitle().toUpperCase() +
                            "\n-------------------------------------");
            for (int i = 0; i < playlists.get(num).getTracks().size(); i++){
                System.out.println(BOLD + "[" + i + "] " + NORMAL + playlists.get(num).getTracks().get(i).getTitle() + " by " + playlists.get(num).getTracks().get(i).getArtist());
            }
            if (playlists.get(num).getTracks().isEmpty()){
                System.out.println("Playlist is empty");
            }
            System.out.println("-------------------------------------" + BOLD + GREEN + "\n[D]" + NORMAL + " Done" + "\n-------------------------------------");

            System.out.print("> ");
            response = scanner.next();
            response = response.toUpperCase();

            space();

            if (response.equals("D")){
                return;
            }

            try {
                int temp = Integer.valueOf(response);
                if (temp >= 0 && temp <= playlists.get(num).getTracks().size()){
                    playlists.get(num).getTracks().remove(temp);
                } else {
                    System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
                }
            } catch (Exception e){
                System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
            }
        }
    }




    // LINK METHODS
    // link menu loop
    protected void linkMenu(){
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String response = "";

            System.out.println(
                "\n-------------------------------------\n" +
                PURPLE + "LINKED SONGS\n" + NORMAL +
                "-------------------------------------");
            for (int i = 0; i < links.size(); i++) {
                System.out.print(BOLD + "[" + (i+1) + "] " + NORMAL + links.get(i).getTitle() + "\n");
            }
            if (links.isEmpty()){
                System.out.println("No Links");
            }
            System.out.println("-------------------------------------\n" +
                BOLD + "[A]" + NORMAL + " Create Link\n" +
                BOLD + RED + "[B]" + NORMAL + " Back\n" +
                "-------------------------------------");

            System.out.print("> ");

            response = scanner.next();
            response = response.toUpperCase();

            space();

            if (response.equals("A")) {
                createLink();
                save();
            } else if (response.equals("B")) {
                return;
            } else {
                boolean check = false;
                try {
                    if (Integer.parseInt(response) >= 1 && Integer.parseInt(response) <= links.size()) {
                        check = true;
                    }
                } catch (Exception e) {
                    System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
                }

                if (check) {
                    viewLink(Integer.parseInt(response) - 1);
                }
            }
        }
    }

    // interface for viewing a selected link
    protected void viewLink(int num){
        while (true){
            Scanner scanner = new Scanner(System.in);
            String response = "";

            Link link = links.get(num);
            System.out.println(
                "\n-------------------------------------\n" +
                BOLD + PURPLE + link.getTitle().toUpperCase() + NORMAL +
                "\n-------------------------------------");
            for (int i = 0; i < link.getTracks().size(); i++){
                System.out.println(BOLD + "> " + NORMAL + link.getTracks().get(i).getTitle() + " by " + link.getTracks().get(i).getArtist());
            }
            if (link.getTracks().isEmpty()){
                System.out.println("Link is empty");
            }
            System.out.println("-------------------------------------\n" +
                BOLD + "[A]" + NORMAL + " Add Songs\n" +
                BOLD + "[R]" + NORMAL + " Remove Songs\n" +
                BOLD + RED + "[D]" + NORMAL + " Delete Link\n" +
                BOLD + RED + "[B]" + NORMAL + " BACK\n" +
                "-------------------------------------"
            );

            System.out.print("> ");
            response = scanner.next();
            response = response.toUpperCase();

            space();

            if (response.equals("A")){
                addSongsL(num);
                save();
            } else if (response.equals("R")){
                removeSongsL(num);
                save();
            } else if (response.equals("D")) {
                while (true) {
                    System.out.print("-------------------------------------\n" + RED + BOLD + "[WARNING]" + NORMAL + " Are you sure you want to delete this link?\n-------------------------------------\n" + BOLD + "[Y/N]\n> " + NORMAL);
                    response = scanner.next();
                    response = response.toUpperCase();

                    if (response.equals("Y")){
                        links.remove(link);
                        space();
                        System.out.println("-------------------------------------\n" + RED + BOLD + "Link Deleted\n" + NORMAL + "-------------------------------------");
                        save();
                        break;
                    } else if (response.equals("N")) {
                        return;
                    } else {
                        System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
                    }
                }
                return;
            } else if (response.equals("B")) {
                return;
            } else {
                System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
            }
        }
    }

    //create a new link
    protected void createLink(){
        Scanner scanner = new Scanner(System.in);
        String response = "";
        System.out.print("------------------\n" + PURPLE + "CREATING A LINK" + NORMAL + "\n------------------" + "\nInput Title: ");
        response = scanner.next();

        Link link = new Link(response);
        links.add(link);

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n------------------\n"
                + GREEN + "LINK CREATED" + NORMAL + "\n------------------");
    }

    // check whether a track is linked
    protected Link checkLink(Track track){
        for (int i = 0; i < links.size(); i++){ //loops through all links
            if (links.get(i).getTracks().contains(track)){ //checks if contains the track
                return links.get(i); //should only be possible to be in one link
            }
        }
        return null;
    }

    // add songs to a link
    protected void addSongsL(int num){
        while (true){
            Scanner scanner = new Scanner(System.in);
            String response = "";

            System.out.println(
                    "\n-------------------------------------\n" +
                            BOLD + "Adding songs to: " + PURPLE + links.get(num).getTitle().toUpperCase() + NORMAL +
                            "\n-------------------------------------"
            );

            for (int i = 0; i < playlists.get(0).getTracks().size(); i++){
                if (links.get(num).getTracks().contains(playlists.get(0).getTracks().get(i))){
                    System.out.println(BOLD + "[" + "✓" + "] " + NORMAL + playlists.get(0).getTracks().get(i).getTitle() + " by " + playlists.get(0).getTracks().get(i).getArtist());
                } else {
                    System.out.println(BOLD + "[" + i + "] " + NORMAL + playlists.get(0).getTracks().get(i).getTitle() + " by " + playlists.get(0).getTracks().get(i).getArtist());
                }
            }
            System.out.println("-------------------------------------" + BOLD + "\n[D]" + GREEN + " Done (changes will take effect when refreshing queue)" + NORMAL + "\n-------------------------------------");
            System.out.print("> ");
            response = scanner.next();
            response = response.toUpperCase();

            space();

            if (response.equals("D")){
                return;
            }

            try {
                int temp = Integer.parseInt(response);

                boolean check = false;
                if (temp >= 0 && temp <= playlists.get(0).getTracks().size()) {
                    if (checkLink(playlists.get(0).getTracks().get(temp)) == null){
                        links.get(num).add(playlists.get(0).getTracks().get(temp));
                    } else {
                        System.out.println(RED + "\nERROR: Song already in a link" + NORMAL);
                    }
                } else {
                    System.out.println(RED + "\nERROR: Invalid Input" + NORMAL);
                }
            } catch (Exception e){
                System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
            }
        }
    }

    // remove songs from a link
    protected void removeSongsL(int num){
        while (true){
            Scanner scanner = new Scanner(System.in);
            String response = "";

            System.out.println(
                "\n-------------------------------------\n" +
                BOLD + PURPLE + "Removing songs from: " + NORMAL + links.get(num).getTitle().toUpperCase() +
                "\n-------------------------------------"
            );
            for (int i = 0; i < links.get(num).getTracks().size(); i++){
                System.out.println(BOLD + "[" + i + "] " + NORMAL + links.get(num).getTracks().get(i).getTitle() + " by " + links.get(num).getTracks().get(i).getArtist());
            }
            if (links.get(num).getTracks().isEmpty()){
                System.out.println("Link is empty");
            }
            System.out.println("-------------------------------------");

            System.out.print("> ");
            response = scanner.next();
            response = response.toUpperCase();

            if (response.equals("D")){
                return;
            }

            try {
                int temp = Integer.parseInt(response);
                if (temp >= 0 && temp <= links.get(num).getTracks().size()){
                    links.get(num).getTracks().remove(temp);
                } else {
                    System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
                }
            } catch (Exception e){
                System.out.println(RED + "ERROR: Invalid Input" + NORMAL);
            }
        }
    }




    // SAVING AND MISC METHODS
    // method for saving all information by implementing use of savePlaylist and saveLink
    protected void save() {

        // saving all the playlists
        String playlistsFilePath = "C:\\Users\\zhouc\\Downloads\\School\\Programming\\Grade 12 Computer Science\\Console Vynl\\src\\main\\resources\\com\\example\\javafxmedia\\CSVs\\Playlists.csv";

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playlistsFilePath)))) {
            for (int i = 0; i < playlists.size(); i++) {
                Playlist playlist = playlists.get(i);
                if (i == 0){ // for ALL TRACKS (the first playlist)

                    writer.write("**********"); // insert new playlist identifier
                    writer.newLine();
                    writer.write(playlist.getTitle()); // insert title
                    writer.newLine();

                    // loop-insert all the tracks (and their information)
                    for (int j = 0; j < playlist.getTracks().size(); j++){
                        Track track = playlist.getTracks().get(j);
                        String cleanedPath = track.getPath().replace("C:\\Users\\zhouc\\Downloads\\School\\Programming\\Grade 12 Computer Science\\Console Vynl\\src\\main\\resources\\com\\example\\javafxmedia\\Tracks\\", "");
                        String line = track.getTitle() + ",%," + track.getArtist() + ",%," + cleanedPath;
                        writer.write(line);
                        writer.newLine();
                    }
                } else { // for all other playlists,
                    writer.write("**********");
                    writer.newLine();
                    writer.write(playlist.getTitle());
                    writer.newLine();
                    for (Track track : playlist.getTracks()) { // write the tracks as index reference numbers
                        int index = playlists.get(0).getTracks().indexOf(track);
                        String line = Integer.toString(index);
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
            // just in case, force the writer to write the csv info to the file and close itself
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // saving all links (same concept as playlists, except you only save indexes (no track information)
        String linksFilePath = "C:\\Users\\zhouc\\Downloads\\School\\Programming\\Grade 12 Computer Science\\Console Vynl\\src\\main\\resources\\com\\example\\javafxmedia\\CSVs\\Links.csv";

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(linksFilePath)))) {
            for (Link link : links) {
                writer.write("**********");
                writer.newLine();
                writer.write(link.getTitle());
                writer.newLine();
                for (Track track : link.getTracks()) {
                    int index = playlists.get(0).getTracks().indexOf(track);
                    String line = Integer.toString(index);
                    writer.write(line);
                    writer.newLine();
                }
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // make sure to call this method when the application closes to release resources
    protected void shutdown() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }

    // creates space in the console, easier than printing it each time, purely for aesthetic purposes
    protected void space(){
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
    }
}