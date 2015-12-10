package ch.frontg8.lib.filechooser.view;

import android.app.ListActivity;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import ch.frontg8.R;
import ch.frontg8.lib.filechooser.bl.FileType;
import ch.frontg8.lib.filechooser.view.model.FileArrayAdapter;
import ch.frontg8.lib.filechooser.bl.Item;

public class FileChooser extends ListActivity {

    private File currentDir;
    private FileArrayAdapter adapter;
    private final String START_DIR = "sdcard";
    private final String START_DIR_PATH = "/" + START_DIR + "/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = new File(START_DIR_PATH);
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
        List<Item> dir = new ArrayList<Item>();
        List<Item> fls = new ArrayList<Item>();
        for (File currentItem : directoryContent) {
            if (currentItem.isDirectory()) {
                dir.add(createDirItem(currentItem));
            } else {
                fls.add(createFileItem(currentItem));
            }
        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.add(new Item(FileType.DIR_CURRENT));
        dir.addAll(fls);
        if (!isStartDir(currentDir))
            dir.add(0, new Item("..", getString(R.string.titleParentDirectory), "", currentDir.getParent(), FileType.DIR_UP));

        return dir;
    }

    private boolean isStartDir(File file) {
        return file.getName().equalsIgnoreCase(START_DIR);
    }

    private Item createDirItem(File currentDirectory) {
        File[] fbuf = currentDirectory.listFiles();
        int buf = 0;
        if (fbuf != null) {
            buf = fbuf.length;
        } else buf = 0;
        String num_item = String.valueOf(buf);
        if (buf == 0) num_item = num_item + " " + getString(R.string.titleItem);
        else num_item = num_item + " " + getString(R.string.titleItems);
        return new Item(currentDirectory.getName(), num_item, getFormatedDate(currentDirectory), currentDirectory.getAbsolutePath(), FileType.DIRECTORY);
    }

    private Item createFileItem(File currentFile) {
        return new Item(currentFile.getName(), String.valueOf(currentFile.length()) + " Byte", getFormatedDate(currentFile), currentFile.getAbsolutePath(), FileType.FILE);
    }

    private String getFormatedDate(File item) {
        Date lastModDate = new Date(item.lastModified());
        DateFormat formater = DateFormat.getDateTimeInstance();
        return formater.format(lastModDate);
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
