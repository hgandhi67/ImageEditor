package com.app.image.editor.javaclasses.models;

import android.os.Environment;

public class Constants {
    private static String HOME_DIRECTORY_PATH = Environment.getExternalStorageDirectory().toString();
    public static String APP_FOLDER_PATH = HOME_DIRECTORY_PATH + "/ImageEditor";
}
