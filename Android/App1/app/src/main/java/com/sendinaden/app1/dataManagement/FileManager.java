package com.sendinaden.app1.dataManagement;

import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ejalaa on 15/09/15.
 * This class saves the data in files.
 * It manages the folders, the saving and the reading of files
 */
public class FileManager {

    // We have two extenstion
    private final static String ext1 = ".brd";  // brd for Breath Raw Data: contains the raw data (obviously)
    private final static String ext2 = ".sbrd"; // sbrd for Smart B.R.D.: contains features only
    private static File currentFolder;
    private File currentRawDataFile;

    public FileManager(Bundle savedInstanceState) {
        currentFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Breathe Data");
        if (!currentFolder.exists())
            currentFolder.mkdirs();

        if (savedInstanceState != null) {
            String lastSavedName = savedInstanceState.getString("current raw data file");
            if (lastSavedName != null)
                currentRawDataFile = new File(currentFolder, lastSavedName);
        }
    }

    /**
     * Appends the data to the end of the current file
     *
     * @param string
     */
    public void save(String string) {
        if (currentRawDataFile != null) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(currentRawDataFile, true);
                fw.write(string);
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveState(Bundle outState) {
        if (currentRawDataFile != null) {
            outState.putString("current raw data file", currentRawDataFile.getName());
        }
    }

    public void setCurrentRawDataFile(String filename) {
        currentRawDataFile = new File(currentFolder, filename + ext1);
    }

    public void saveSmartData(String smartData) {
        File file = new File(currentFolder, currentRawDataFile.getName().substring(0, currentRawDataFile.getName().length() - 4) + ext2);
        FileWriter fw;
        try {
            fw = new FileWriter(file, true);
            fw.write(smartData);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSmartData(String smartData, String ext) {
        File file = new File(currentFolder, currentRawDataFile.getName().substring(0, currentRawDataFile.getName().length() - 4) + ext);
        FileWriter fw;
        try {
            fw = new FileWriter(file, true);
            fw.write(smartData);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        currentRawDataFile = null;
    }
}
