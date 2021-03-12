package org.wirla.WorldsOrganizer;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Configuration {

    private File configFile;

    public boolean checkUpdate = true;
    public boolean attemptBackup = true;
    public String theme = "default";

    static final List<String> themes = Arrays.asList("default",
            "dark",
            "darcula",
            "sky",
            "grassy",
            "sand",
            "bootstrap3"
    );

    Configuration() {
    }

    Configuration(File file) throws FileNotFoundException {
        configFile = file;
        try {
            if (!configFile.exists()) {
                write();
            }

            JSONObject config = new JSONObject(new JSONTokener(new FileInputStream(configFile)));
            checkUpdate = config.getBoolean("check-for-updates");
            attemptBackup = config.getBoolean("attempt-backups");
            theme = config.getString("theme");
        } catch (JSONException e) {
            e.printStackTrace();
            Dialog.showException(e);
        }
    }

    public boolean write() {
        return write(configFile);
    }

    public boolean write(Configuration newConf) {

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        JSONObject config = new JSONObject();
        config.put("check-for-updates", newConf.checkUpdate);
        config.put("attempt-backups", newConf.attemptBackup);
        config.put("theme", newConf.theme);

        try {
            FileUtils.writeStringToFile(configFile, config.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean write(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        JSONObject config = new JSONObject();
        config.put("check-for-updates", checkUpdate);
        config.put("attempt-backups", attemptBackup);
        config.put("theme", theme);

        try {
            FileUtils.writeStringToFile(file, config.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
