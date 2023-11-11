package com.example.iat359project;

public class Constants {
    public static final String DB_NAME = "journaldatabase";
    public static final String TABLE_NAME = "JOURNALTABLE";
    public static final String UID = "_id";
    public static final int DB_VERSION = 1;
    public static final String SP_NAME = "Journal";

    //Table columns
    public static final String TEXT = "Text";
    public static final String DATE = "Date";
    public static final String TEMP = "Temperature";
    public static final String MOOD = "Mood";
    public static final String IMAGE = "Image";

    //Moods
    public static final int HAPPY   = 1;
    public static final int SAD     = 2;
    public static final int ANGRY   = 3;
    public static final int LOVE    = 4;
    public static final int TIRED   = 5;
    public static final int WORRIED = 6;
    public static final int EXCITED = 7;
    public static final int NEUTRAL = 8;

}
