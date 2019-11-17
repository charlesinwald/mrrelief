package com.lehigh.charlesinwald.mrrelief.ui.home;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.lehigh.charlesinwald.mrrelief.R;

import java.io.File;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private SeekBar bar;

    private BootstrapEditText journalEntry;

    private BootstrapButton saveButton;
    private View root;
    private SQLiteDatabase db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        bar = root.findViewById(R.id.seekBar);
        journalEntry = root.findViewById(R.id.journalentry);
        saveButton = root.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getInputData();
            }
        });
        String dbpath = new File(getContext().getApplicationInfo().dataDir, "databases").getAbsolutePath();
        db = SQLiteDatabase.openOrCreateDatabase(dbpath, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS moodData(time DATETIME,score INTEGER, entry TEXT);");
        return root;
    }

    private void getInputData() {
        int score = bar.getProgress();
        Log.d("SEEK", String.valueOf(score));
        String msg = String.valueOf(journalEntry.getText());
        Log.d("TEXT", msg);
        String query = "INSERT INTO moodData VALUES(DATETIME('now'), " + "'" + score + "', '" + msg + "');";
        Log.d("Query", query);
        db.execSQL(query);
    }


}