package ch.frontg8.lib.filechooser.bl;

import android.support.annotation.NonNull;

public class Item implements Comparable<Item> {
    private String name;
    private String data;
    private String date;
    private String path;
    private FileType fileType;

    public Item(FileType fileType) {
        this("", "", "", "", fileType);
        if (fileType == FileType.DIR_CURRENT) {
            this.name = ".";
            this.data = "(Current Directory)";
        }
    }

    public Item(String name, String data, String date, String path, FileType fileType) {
        this.name = name;
        this.data = data;
        this.date = date;
        this.path = path;
        this.fileType = fileType;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public String getDate() {
        return date;
    }

    public String getPath() {
        return path;
    }

    public FileType getFileType() {
        return fileType;
    }

    public String getImage() {
        return fileType.getPicName();
    }

    public int compareTo(@NonNull Item o) {
        if (this.name != null)
            return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
        else
            throw new IllegalArgumentException();
    }
}