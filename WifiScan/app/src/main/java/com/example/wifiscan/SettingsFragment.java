package com.example.wifiscan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends PreferenceFragmentCompat  {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if ("feedback_preference".equals(key)) {
            // Handle feedback preference click
            sendFeedback();
            return true;  // Consume the click event
        }
        else if("SO".equals(key)){
            signOut();
            return true;
        } else if ("data_replacement".equals(key)) {

            return true;
        } else if ("ignore_whitelisted".equals(key)) {

            return true;
        }


        return super.onPreferenceTreeClick(preference);
    }

    private void sendFeedback() {
        // Replace this with your feedback handling logic

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "demo@example.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback from the app");
        startActivity(Intent.createChooser(emailIntent, "Send feedback via email"));
    }
    private void signOut() {

        // Assuming you are using Firebase Authentication for sign-in/sign-out
        FirebaseAuth.getInstance().signOut();

        // Navigate to the login page
        Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
        startActivity(loginIntent);
        // Ensure that the user cannot navigate back to the SettingsFragment by pressing the back button
        getActivity().finish();
    }



}