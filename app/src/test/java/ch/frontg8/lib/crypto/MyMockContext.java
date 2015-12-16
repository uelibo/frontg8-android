package ch.frontg8.lib.crypto;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.test.mock.MockContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;

/**
 * Created by tstauber on 03.11.15.
 */

public class MyMockContext extends MockContext {
    private static final String MOCK_FILE_SUFFIX = ".test";

    public MyMockContext() {
        super();
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return new FileInputStream(name + MOCK_FILE_SUFFIX);
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return new FileOutputStream(name + MOCK_FILE_SUFFIX);
    }

    @Override
    public File getFilesDir() {
        return new File("");
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {

        return new SharedPreferences() {
            @Override
            public Map<String, ?> getAll() {
                return null;
            }

            @Nullable
            @Override
            public String getString(String key, String defValue) {
                switch (key) {
                    case "edittext_preference_keyfilepath":
                        return "frontg8keystore.ks";
                    default:
                        return "bla";
                }
            }

            @Nullable
            @Override
            public Set<String> getStringSet(String key, Set<String> defValues) {
                return null;
            }

            @Override
            public int getInt(String key, int defValue) {
                return 0;
            }

            @Override
            public long getLong(String key, long defValue) {
                return 0;
            }

            @Override
            public float getFloat(String key, float defValue) {
                return 0;
            }

            @Override
            public boolean getBoolean(String key, boolean defValue) {
                return false;
            }

            @Override
            public boolean contains(String key) {
                return false;
            }

            @Override
            public Editor edit() {
                return null;
            }

            @Override
            public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

            }

            @Override
            public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

            }
        };
    }

}
