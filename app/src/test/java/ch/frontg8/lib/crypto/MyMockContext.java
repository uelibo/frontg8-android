package ch.frontg8.lib.crypto;

import java.io.FileInputStream;

import java.io.FileNotFoundException;

import java.io.FileOutputStream;

import android.test.mock.MockContext;

/**
 * Created by tstauber on 03.11.15.
 */

public class MyMockContext extends MockContext {
    private static final String MOCK_FILE_PREFIX = ".frontg8keystore";

    public MyMockContext() {
        super();
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return new FileInputStream(MOCK_FILE_PREFIX + name);
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return new FileOutputStream( name + MOCK_FILE_PREFIX);
    }

}
