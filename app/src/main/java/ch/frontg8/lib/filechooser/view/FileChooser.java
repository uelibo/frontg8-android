package ch.frontg8.lib.filechooser.view;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ch.frontg8.R;
import ch.frontg8.lib.filechooser.bl.FileType;
import ch.frontg8.lib.filechooser.bl.Item;
import ch.frontg8.lib.filechooser.view.model.FileArrayAdapter;

public class FileChooser extends ListActivity {
    public final static String CHOOSE_DIR = "chooseDir";
    public final static String SHOW_FILES = "showFiles";
    public final static String FILE_EXTENSION = "fileExtension";
    private final static String START_DIR = "sdcard";
    private final static String START_DIR_PATH = "/" + START_DIR + "/";

    private File currentDir;
    private FileArrayAdapter adapter;
    private boolean chooseDir;
    private boolean showFiles;
//    private String fileExtension;


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            chooseDir = (boolean) bundle.getSerializable(CHOOSE_DIR);
            showFiles = (boolean) bundle.getSerializable(SHOW_FILES);
//            fileExtension = (String) bundle.getSerializable(FILE_EXTENSION);
        }

        verifyStoragePermissions(this);

        String extStore = System.getenv("EXTERNAL_STORAGE");
        currentDir = new File(extStore);
        refreshList(currentDir);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Item o = adapter.getItem(position);
        if (o.getFileType() == FileType.DIRECTORY || o.getFileType() == FileType.DIR_UP) {
            currentDir = new File(o.getPath());
            refreshList(currentDir);
        } else {
            onFileClick(o);
        }
    }

    private void refreshList(File currentDir) {
        File[] directoryContent = currentDir.listFiles();
        this.setTitle(getString(R.string.titleCurrentDir) + ": " + currentDir.getName());
        adapter = new FileArrayAdapter(FileChooser.this, R.layout.rowlayout_file_chooser, readDirectory(directoryContent));
        this.setListAdapter(adapter);
    }

    private List<Item> readDirectory(File[] directoryContent) {
        List<Item> dir = new ArrayList<>();
        List<Item> fls = new ArrayList<>();

        if (directoryContent != null) {
            for (File currentItem : directoryContent) {
                if (currentItem.isDirectory()) {
                    dir.add(createDirItem(currentItem));
                } else {
                    if (showFiles) {
                        fls.add(createFileItem(currentItem));
                    }
                }
            }
            Collections.sort(dir);
            Collections.sort(fls);

        }
        if (chooseDir) {
            dir.add(0, new Item(FileType.DIR_CURRENT));
        }
        dir.addAll(fls);
        if (!isStartDir(currentDir))
            dir.add(0, new Item("..", getString(R.string.titleParentDirectory), "", currentDir.getParent(), FileType.DIR_UP));

        return dir;

    }

    private boolean isStartDir(File file) {
        return file.getName().equalsIgnoreCase(START_DIR);
    }

    private Item createDirItem(File currentDirectory) {
        File[] fileBuffer = currentDirectory.listFiles();
        int buf = 0;
        if (fileBuffer != null) {
            buf = fileBuffer.length;
        } else buf = 0;
        String num_item = String.valueOf(buf);
        if (buf == 0) num_item = num_item + " " + getString(R.string.titleItems);
        else if (buf == 1) num_item = num_item + " " + getString(R.string.titleItem);
        else num_item = num_item + " " + getString(R.string.titleItems);
        return new Item(currentDirectory.getName(), num_item, getFormattedDate(currentDirectory), currentDirectory.getAbsolutePath(), FileType.DIRECTORY);
    }

    private Item createFileItem(File currentFile) {
        return new Item(currentFile.getName(), String.valueOf(currentFile.length()) + " Byte", getFormattedDate(currentFile), currentFile.getAbsolutePath(), FileType.FILE);
    }

    private String getFormattedDate(File item) {
        Date lastModDate = new Date(item.lastModified());
        //DateFormat formatter = DateFormat.getDateTimeInstance();
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
        return formatter.format(lastModDate);
    }

    private void onFileClick(Item o) {
        //Toast.makeText(this, "Folder Clicked: " + currentDir, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("GetPath", currentDir.toString());
        intent.putExtra("GetFileName", o.getName());
        setResult(RESULT_OK, intent);
        finish();
    }

}
