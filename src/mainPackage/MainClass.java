package mainPackage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;

public class MainClass {
    private static File container;
    private static ArrayList<URL> songs = new ArrayList<>();
    private static String userPath;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) {
        boolean proceed = true;
        boolean ok = false;
        while (!ok) {
            System.out.print("Inserisci la path alla cartella dove vuoi scaricare i file: ");
            userPath = new Scanner(System.in).next();
            if (new File(userPath).isFile()) {
                System.out.print("Percorso non valido.\n");
            }
            if (new File(userPath).exists()) {
                if (new File(userPath).isDirectory()) {
                    ok = true;
                    System.out.print("Ok.\n");
                }
            } else {
                new File(userPath).mkdir();
                ok = true;
                System.out.print("Ok.\n");
            }
        }
        while(proceed) {
            procedure();
            boolean correct=false;
            while(!correct) {
                System.out.print("Vuoi continuare?  [y/n]  ");
                String choice = new Scanner(System.in).next().toLowerCase();
                if (choice.equals("y") || choice.equals("n")) {
                    if (choice.equals("n")) {
                        proceed = false;
                        correct=true;
                    }else{
                        proceed=true;
                        correct=true;
                    }
                } else {
                    System.out.print("y o n.\n");
                }
            }
        }
    }

    private static void procedure(){
        boolean correct_choice = false;
        String choice=null;

        while(!correct_choice) {
            System.out.print("Scaricare da file, link o spotify? [f/l/s]   ");
            choice = new Scanner(System.in).next().toLowerCase();
            if(choice.equals("f")||choice.equals("l")||choice.equals("s")){
                correct_choice=true;
            }else{
                System.out.print("Scelta non contemplata.\n");
            }
        }

        switch(choice){
            case "l":  try {
                            System.out.print("Scrivi il link della canzone: ");
                            String link = new Scanner(System.in).next();
                            URL songUrl = new URL("http://www.youtubeinmp3.com/download/?video=" + link);
                            downloadSongs(songUrl,0);
                        }catch(MalformedURLException e){
                            e.printStackTrace();
                        }
                        break;
            case "s":   try {
                            System.out.print("Inserisci il link di spotify: ");
                            String spotify_link = new Scanner(System.in).next();
                            URL spotify_url = new URL(spotify_link);
                            String spotify_source = getUrlSource(spotify_url);
                            String title = spotify_source.substring(spotify_source.indexOf("<title>")+7, spotify_source.indexOf(","));
                            String artist = spotify_source.substring(spotify_source.indexOf(", a song by ")+12, spotify_source.indexOf(" on Spotify"));
                            String song = artist.replace(" ", "+")+"+-+"+title.replace(" ","+");
                            String youtube_query = "https://www.youtube.com/results?search_query="+song+"&page=&utm_source=opensearch";
                            URL youtube_query_url = new URL(youtube_query);
                            String youtube_source = getUrlSource(youtube_query_url);
                            //ho dovuto ridurre youtube_source perchè ci sono più " class=" " nel documento e dava errore perchè la stringa
                            //assumeva valori di -22
                            youtube_source = youtube_source.substring(youtube_source.indexOf("<a href=\"/watch?v="));
                            //prende l'id del video in testa alla lista
                            String first_video= youtube_source.substring(youtube_source.indexOf("<a href=\"/watch?v=")+9,youtube_source.indexOf("\" class="));
                            URL first_youtube_video = new URL("http://www.youtubeinmp3.com/download/?video=https://www.youtube.com"+first_video);
                            downloadSongs(first_youtube_video,0);
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                        break;
            case "f":   boolean esiste = false;
                        while (!esiste) {
                            System.out.print("Scrivi il percorso del file: ");
                            String path = new Scanner(System.in).next();
                            container = new File(path);
                            if (container.exists()) {
                                if (container.isDirectory()) {
                                    System.out.print("Il percorso porta ad una directory.\n");
                                } else if (container.isFile()) {
                                    System.out.print("Ok.\n");
                                    esiste = true;
                                }
                            } else {
                                System.out.print("Il percorso non porta a nulla.\n");
                            }
                        }
                        break;
            default: System.out.print("Scelta non valida.\n"); break;
        }
    }

    private static void downloadSongs(URL song, int i){
        //se non è un link di youtube non funziona
        if(song.toString().contains("youtube.com")) {
            try {
                String source = getUrlSource(song);
                String name = source.substring(source.indexOf("<span id=\"videoTitle\">") + 22, source.indexOf("</span></h1>"));
                if (!(i == 0)) {
                    System.out.print(ANSI_WHITE+"[" + i + "] Downloading: " + name+".mp3"+ANSI_RESET);
                } else {
                    System.out.print(ANSI_WHITE + "Downloading: " + name+".mp3"+ANSI_RESET);
                }
                File path = new File(userPath + File.separator + name + ".mp3");
                String link = "http://www.youtubeinmp3.com/download/get/" + source.substring(source.indexOf("href=\"/download/get/") + 20, source.indexOf("\"><i class=\"fa fa-cloud-download\"><"));
                URL url = new URL(link);
                //copyURLToFile() è un metodo di org.apache.commons.io, libreria che sto imparando ad amare per tutti i vari lavori sui file
                FileUtils.copyURLToFile(url, path);
                System.out.print(ANSI_GREEN +"  OK\n"+ANSI_RESET);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            System.out.print(ANSI_RED+"ERROR link not valid: "+song.toString().substring(song.toString().indexOf("http://www.youtubeinmp3.com/download/?video=")+44)+"\n"+ANSI_RESET);
        }
    }

    private static String getUrlSource(URL url) throws IOException {
        URLConnection con = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
        String line;
        StringBuilder source = new StringBuilder();
        while ((line = reader.readLine()) != null)
            source.append(line);
        reader.close();
        return source.toString();
    }
}