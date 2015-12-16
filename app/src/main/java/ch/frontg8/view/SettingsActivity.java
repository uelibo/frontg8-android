package ch.frontg8.view;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import ch.frontg8.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
            getPreferenceManager().setSharedPreferencesName(getString(R.string.preferences));
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onResume() {
            super.onResume();
            updateDescription();
        }

        private void updateDescription() {
            ArrayList<String> fields = new ArrayList<>();
            fields.add("edittext_preference_username");
            fields.add("edittext_preference_hostname");
            fields.add("edittext_preference_port");

            for (String field : fields) {
                EditTextPreference preference_field = (EditTextPreference) this.getPreferenceManager().findPreference(field);
                preference_field.setSummary(preference_field.getText());

                preference_field.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        preference.setSummary(newValue.toString());
                        return true;
                    }
                });

            }
        }
    }

}
