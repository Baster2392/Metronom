package com.example.metronom.Activities;

import static com.example.metronom.OtherClasses.Keys.BEATS_PER_MINUTE;
import static com.example.metronom.OtherClasses.Keys.PERIOD_SILENT;
import static com.example.metronom.OtherClasses.Keys.IS_MUTE_MODE_ACTIVE;
import static com.example.metronom.OtherClasses.Keys.METER_1;
import static com.example.metronom.OtherClasses.Keys.METER_2;
import static com.example.metronom.OtherClasses.Keys.HOW_LONG_SILENT;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.metronom.OtherClasses.Functions;
import com.example.metronom.R;

public class OptionsActivity extends WearableActivity implements AdapterView.OnItemSelectedListener {

    private SeekBar seekBar;
    private TextView textBPMOptions;
    private Spinner[] noteSpinner;

    private int beatsPerMinute, meter1, meter2, howLongSilent, periodSilent;
    private boolean isMuteModeActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        textBPMOptions = findViewById(R.id.bpm_view);

        // Initialization of variables
        SharedPreferences sharedPreferences = Functions.getMySharedPreferences(this);

        beatsPerMinute = sharedPreferences.getInt(BEATS_PER_MINUTE, 60);
        meter1 = sharedPreferences.getInt(METER_1, 4);
        meter2 = sharedPreferences.getInt(METER_2, 4);
        isMuteModeActive = sharedPreferences.getBoolean(IS_MUTE_MODE_ACTIVE, false);
        howLongSilent = sharedPreferences.getInt(HOW_LONG_SILENT, 0);
        periodSilent = sharedPreferences.getInt(PERIOD_SILENT, 4);

        // Initialization of seekbar and creating tracking method
        seekBar = findViewById(R.id.seekbar_options_activity);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update BPM view
                textBPMOptions.setText(String.valueOf(progress));
                beatsPerMinute = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Pass
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Set variable
                beatsPerMinute = seekBar.getProgress();
            }
        });

        // Initialization of spinners and dropdown lists
        noteSpinner = new Spinner[4];

        for (int i = 0; i < noteSpinner.length; i++) {
            int idSpinnerView = 0;
            int idStringArray = 0;

            switch (i) {
                case 0:  // meter1 spinner
                    idSpinnerView = R.id.meter_1_spinner;
                    idStringArray = R.array.meter_1_spinner_array;
                    break;

                case 1:  // meter2 spinner
                    idSpinnerView = R.id.meter_2_spinner;
                    idStringArray = R.array.meter_2_spinner_array;
                    break;

                case 2: // how long silent beats spinner
                    idSpinnerView = R.id.how_long_silent_spinner_options_activity;
                    idStringArray = R.array.how_long_silent_spinner_array;
                    break;

                case 3: // period silent spinner
                    idSpinnerView = R.id.period_silent_spinner;
                    idStringArray = R.array.period_silent_spinner_array;
                    break;
            }

            ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(
                    this,
                    idStringArray,
                    android.R.layout.simple_spinner_item
            );
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            noteSpinner[i] = findViewById(idSpinnerView);
            noteSpinner[i].setAdapter(arrayAdapter);
            noteSpinner[i].setOnItemSelectedListener(this);
        }

        // Fill layout with data
        seekBar.setProgress(beatsPerMinute);

        switch (meter1) {
            case 4: noteSpinner[0].setSelection(0); break;
            case 8: noteSpinner[0].setSelection(1); break;
            case 16: noteSpinner[0].setSelection(2); break;
        }


        switch (meter2) {
            case 4: noteSpinner[1].setSelection(0); break;
            case 8: noteSpinner[1].setSelection(1); break;
            case 16: noteSpinner[1].setSelection(2); break;
        }

        if (isMuteModeActive) {
            switch (howLongSilent) {
                case 2: noteSpinner[2].setSelection(1); break;
                case 4: noteSpinner[2].setSelection(2); break;
                case 8: noteSpinner[2].setSelection(3); break;
                case 16: noteSpinner[2].setSelection(4); break;
            }
        } else {
            noteSpinner[2].setSelection(0);
        }

        noteSpinner[3].setSelection(periodSilent - 1);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Set style of spinner
        ((TextView) parent.getChildAt(0)).setTextSize(30);
        ((TextView) parent.getChildAt(0)).setGravity(1);

        // Check which spinner is now in use
        // meter1 spinner
        if (parent.getId() == noteSpinner[0].getId()) {
            meter1 = Integer.parseInt(parent.getSelectedItem().toString());
        }
        // meter2 spinner
        if (parent.getId() == noteSpinner[1].getId()) {
            meter2 = Integer.parseInt(parent.getSelectedItem().toString());
        }
        // how long silent spinner
        if (parent.getId() == noteSpinner[2].getId()) {
            if (parent.getSelectedItem().toString().equals("Wył.")) {
                noteSpinner[3].setEnabled(false);
                isMuteModeActive = false;

                ((TextView) parent.getChildAt(0)).setTextSize(25);
            } else {
                noteSpinner[3].setEnabled(true);
                isMuteModeActive = true;
                howLongSilent = Integer.parseInt(parent.getSelectedItem().toString());
            }
        }
        // period silent spinner
        if (parent.getId() == noteSpinner[3].getId()) {
            periodSilent = Integer.parseInt(parent.getSelectedItem().toString());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Pass
    }

    public void onClickSaveButton(View view) {
        // Pass data to previous activity and finish
        Intent intent = new Intent();

        // Put extras for main activity
        intent.putExtra(BEATS_PER_MINUTE, beatsPerMinute);
        intent.putExtra(METER_1, meter1);
        intent.putExtra(METER_2, meter2);
        intent.putExtra(IS_MUTE_MODE_ACTIVE, isMuteModeActive);
        intent.putExtra(HOW_LONG_SILENT, howLongSilent);
        intent.putExtra(PERIOD_SILENT, periodSilent);

        // Return results
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickMinusButton(View view) {
        if (beatsPerMinute > 30) {
            seekBar.setProgress(seekBar.getProgress() - 1);
        }
    }

    public void onClickPlusButton(View view) {
        if (beatsPerMinute < 240) {
            seekBar.setProgress(seekBar.getProgress() + 1);
        }
    }
}