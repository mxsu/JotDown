package com.example.iat359project;

public class Journal {

    //Our custom Object used to store all the data within a single journal entry.
    //It is necessary as we want the Journals to be saved in an ArrayList together for RecyclerView compatibility, but they contain multiple variable types

    public String entry, date, img;
    public int mood, temp;

    public Journal(String _entry, String _date, int _mood, int _temp, String _img){
        entry = _entry;
        date = _date;
        mood = _mood;
        temp = _temp;
        img = _img;
    }

    public String getEntry(){
        return entry;
    }

    public String getDate(){
        return date;
    }

    public int getMood(){
        return mood;
    }

    public int getTemp(){
        return temp;
    }

    public String getImage() {return img;}
}
