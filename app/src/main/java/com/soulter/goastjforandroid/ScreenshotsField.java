package com.soulter.goastjforandroid;

public class ScreenshotsField {
    private String fileName;
    private String fileSize;
    private int isDict;

    public ScreenshotsField(String fileName, String fileSize, int isDict){
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.isDict = isDict;
    }

    public String getfileName() {
        return fileName;
    }

    public String getfileSize() {
        return fileSize;
    }

    public int getIsDict(){
        return isDict;
    }

}
