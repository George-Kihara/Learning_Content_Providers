package com.ninja.learn.learn_cp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ninja.learn.learn_cp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setUp();
    }

    private void setUp() {
        try {
            binding.btnAdd.setOnClickListener(view -> onClickAddName());
            binding.btnRetrieve.setOnClickListener(view -> onClickRetrieve());
        } catch (Exception e) {
            Log.e(TAG, "setUp: ", e);
        }
    }

    public void onClickAddName() {
        try {
            // Add a new student record
            ContentValues values = new ContentValues();
            values.put(EplTeamsProvider.TEAM_NAME,
                    binding.etName.getText().toString());

            values.put(EplTeamsProvider.TEAM_POINTS,
                    binding.etPoints.getText().toString());

            Uri uri = getContentResolver().insert(
                    EplTeamsProvider.CONTENT_URI, values);

            Toast.makeText(getBaseContext(),
                    uri.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "onClickAddName: ", e);
        }

    }
    public void onClickRetrieve() {
        try {
            // Retrieve teams records
            String URL = "content://epl";

            Uri teams = Uri.parse(URL);
            Cursor c = managedQuery(teams, null, null,
                    null, "team_name");

            if (c.moveToFirst()) {
                do {
                    Toast.makeText(this,
                            c.getString(c.getColumnIndex(EplTeamsProvider._ID)) +
                                    ".  NAME: " + c.getString(c.getColumnIndex(EplTeamsProvider.TEAM_NAME)) +
                                    " POINTS: " + c.getString(c.getColumnIndex(EplTeamsProvider.TEAM_POINTS)),
                            Toast.LENGTH_SHORT).show();
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "onClickRetrieve: ", e);
        }
    }
    
}