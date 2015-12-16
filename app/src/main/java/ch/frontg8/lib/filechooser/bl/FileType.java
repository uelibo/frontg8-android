package ch.frontg8.lib.filechooser.bl;

public enum FileType {
    DIRECTORY("directory_icon"),
    FILE("file_icon"),
    DIR_UP("directory_up"),
    DIR_CURRENT("directory_icon");

    private final String picName;

    FileType(String picName) {
        this.picName = picName;
    }

    public String getPicName() {
        return picName;
    }
}
