package ch.frontg8.view;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

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
            addPreferencesFromResource(R.xml.preferences);
        }
        public void onResume() {
            super.onResume();

            EditTextPreference preference_hostname = (EditTextPreference) this.getPreferenceManager().findPreference("edittext_preference_hostname");
            EditTextPreference preference_port = (EditTextPreference) this.getPreferenceManager().findPreference("edittext_preference_port");

            preference_hostname.setSummary(preference_hostname.getText().toString());
            preference_port.setSummary(preference_port.getText().toString());

        }
    }

}
