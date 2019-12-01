package com.lehigh.charlesinwald.mrrelief.ui.dashboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lehigh.charlesinwald.mrrelief.R;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class DashboardFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, OnChartValueSelectedListener {

    private DashboardViewModel dashboardViewModel;
    private BarChart chart;
    private SeekBar seekBarX;
    private TextView tvX;

    private SQLiteDatabase db;
    private ArrayList<MoodEntry> moodentries;
    private ArrayList<BarEntry> entries;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        String dbpath = new File(getContext().getApplicationInfo().dataDir, "databases").getAbsolutePath();
        db = SQLiteDatabase.openOrCreateDatabase(dbpath, null);

        createChart(root);


        return root;
    }

    private void createChart(View root) {
        tvX = root.findViewById(R.id.tvValueCount);

        seekBarX = root.findViewById(R.id.seekbarValues);

        chart = root.findViewById(R.id.chart1);

        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);

        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);

        // chart.setDrawXLabels(false);

        chart.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setLabelCount(6, false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(10f);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(0.1f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(6, false);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(10f);
        rightAxis.setGranularity(0.1f);

        seekBarX.setOnSeekBarChangeListener(this);
        seekBarX.setProgress(150); // set data

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        chart.setOnChartValueSelectedListener(this);

        chart.animateXY(1500, 1500);

        setData(10);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        String text = "Past " + String.valueOf(seekBarX.getProgress()) + " days";
        tvX.setText(text);
        setData(seekBarX.getProgress());
        chart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    private void setData(int count) {
        try {
            moodentries = new ArrayList<>();

            loadData();

            entries = new ArrayList<>();

            Log.d("MoodEntries", String.valueOf(moodentries.size()));

            if (count > moodentries.size()) {
                count = moodentries.size();
            }

            for (int i = 0; i < count; ++i) {
                MoodEntry current = moodentries.get(i);
                entries.add(new BarEntry(i, current.score, current));
            }

            Log.d("ENTRIES", String.valueOf(entries));
            BarDataSet set;

            if (chart.getData() != null &&
                    chart.getData().getDataSetCount() > 0) {
                set = (BarDataSet) chart.getData().getDataSetByIndex(0);
                set.setValues(entries);
                chart.getData().notifyDataChanged();
                chart.notifyDataSetChanged();
            } else {
                set = new BarDataSet(entries, "Mood Scores");
                set.setColor(Color.rgb(240, 120, 124));
            }

            BarData data = new BarData(set);
            data.setValueTextSize(10f);
            data.setDrawValues(false);
            data.setBarWidth(0.8f);

            chart.setData(data);
        } catch (ParseException e) {
            Log.e("TIME", "Parsing ISO8601 datetime failed", e);
        } catch (Exception e) {
            Log.d("ERROR", "Error loading data");
            e.printStackTrace();
        }
    }

    private void loadData() throws ParseException {
        String query = "SELECT * FROM moodData ORDER BY time ASC";
        Cursor c = db.rawQuery(query, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    int score = c.getInt(1);
                    Log.d("SCORE", String.valueOf(score));

                    String entry = c.getString(2);
                    Log.d("ENTRY", entry);

                    String timeString = c.getString(0);
                    Log.d("TIME", String.valueOf(timeString));
                    DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date;
                    date = iso8601Format.parse(timeString);

                    long when = date.getTime();
                    int flags = 0;
                    flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
                    flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
                    flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
                    flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;

                    String finalDateTime = android.text.format.DateUtils.formatDateTime(this.getContext(),
                            when + TimeZone.getDefault().getOffset(when), flags);
                    long timelong = date.getTime();

                    Log.d("TIME", finalDateTime);

                    moodentries.add(new MoodEntry(score, finalDateTime, timelong, entry));
                } while (c.moveToNext());
            }
        }
    }



    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.d("VALUE","VALUE SELECTED");
        MoodEntry currententry = moodentries.get((int) e.getX());
        new AlertDialog.Builder(this.getContext())
                .setTitle(currententry.datetime)
                .setMessage("Score: " + currententry.score + " \n" + currententry.entry)
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("Close", null)
                .setIcon(android.R.drawable.ic_menu_agenda)
                .show();
    }

    @Override
    public void onNothingSelected() {

    }

    class MoodEntry {
        int score;
        String datetime;
        long timelong;
        String entry;

        MoodEntry(int score, String datetime, long timelong, String entry) {
            this.score = score;
            this.datetime = datetime;
            this.timelong = timelong;
            this.entry = entry;
        }
    }
}