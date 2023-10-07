package com.example.imagegallery;

import java.io.File;
import java.util.List;

public class ImageObject {
    private String filePath;

    ImageObject(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
    public static void getImage(File folder, List<ImageObject> images) {
        File[] files = folder.listFiles();

        if(files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getImage(file, images);
                } else {
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif"))
                        images.add(new ImageObject(file.getAbsolutePath()));
                }
            }
        }
    }
}
