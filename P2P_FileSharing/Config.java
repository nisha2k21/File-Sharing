package com.P2P_FileSharing;
import java.util.*;
import java.io.*;

public class Config {
    static int NUMBEROFPREFERREDNEIGHBORS;
    static int UNCHOKINGINTERVAL;
    static int OPTIMISTICUNCHOKINGINTERVAL;
    static String FILENAME;
    static int FILESIZE;
    static int PIECESIZE;
    static int TOTALPIECES;

    public Config() {}

    public void load() {
        Properties prop = new Properties();

        InputStream is;
        try {
            is = new FileInputStream("Common.cfg");
            prop.load(is);
            NUMBEROFPREFERREDNEIGHBORS  = Integer.parseInt(prop.getProperty("NumberOfPreferredNeighbors"));
            UNCHOKINGINTERVAL           = Integer.parseInt(prop.getProperty("UnchokingInterval"));
            OPTIMISTICUNCHOKINGINTERVAL = Integer.parseInt(prop.getProperty("OptimisticUnchokingInterval"));
            FILENAME                    = prop.getProperty("FileName");
            FILESIZE                    = Integer.parseInt(prop.getProperty("FileSize"));
            PIECESIZE                   = Integer.parseInt(prop.getProperty("PieceSize"));


            if(FILESIZE%PIECESIZE == 0){
                TOTALPIECES = FILESIZE/PIECESIZE;
            } else {
                TOTALPIECES = (FILESIZE/PIECESIZE) + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
