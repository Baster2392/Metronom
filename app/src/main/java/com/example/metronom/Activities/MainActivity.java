package com.example.metronom.Activities;

import static com.example.metronom.OtherClasses.Keys.BEATS_PER_MINUTE;
import static com.example.metronom.OtherClasses.Keys.HOW_LONG_SILENT;
import static com.example.metronom.OtherClasses.Keys.IS_MUTE_MODE_ACTIVE;
import static com.example.metronom.OtherClasses.Keys.IS_VIBRATE_MODE_ACTIVE;
import static com.example.metronom.OtherClasses.Keys.METER_1;
import static com.example.metronom.OtherClasses.Keys.METER_2;
import static com.example.metronom.OtherClasses.Keys.PERIOD_SILENT;
import static com.example.metronom.OtherClasses.Keys.VOLUME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.metronom.OtherClasses.Functions;
import com.example.metronom.R;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends WearableActivity {

    private RelativeLayout parentLayout;
    private ScrollView scrollView;
    private TextView volumeView;
    private ImageButton startButton;
    private Button bpmButton, vibrateModeButton;
    private ImageView[] beats;
    private Drawable[] squares;

    private Timer timer;
    private SoundPool soundPool;
    private Vibrator vibrator;
    private VibrationEffect vibrationEffectStandard, vibrationEffectAccent;

    private int beatsPerMinute, meter1, meter2, howLongSilent, periodSilent;
    private int currentBeat, currentTact, silentTactCounter;
    private int secondCounter;
    private int idStandardSound, idAccentSound;
    private float volume;
    private boolean isMuteModeActive;
    private boolean isPlaying;
    private boolean isSilentCurrently;
    private boolean isSoundLoaded;
    private boolean isLayoutVisible;
    private boolean isVibrateModeActive;

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Disable screen timeout
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialization of variables
        isPlaying = false;
        currentBeat = 0;
        currentTact = 0;

        // Initialization of ScrollView
        scrollView = findViewById(R.id.scroll_view_main_activity);
        scrollView.setOnTouchListener((v, event) -> true);

        // Initialization of vibrator and vibration effects
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrationEffectStandard = VibrationEffect.createOneShot(70, VibrationEffect.EFFECT_CLICK);
            vibrationEffectAccent = VibrationEffect.createOneShot(100, VibrationEffect.EFFECT_HEAVY_CLICK);
        }

        // Getting data from SharedPreferences
        SharedPreferences sharedPreferences = Functions.getMySharedPreferences(this);

        beatsPerMinute = sharedPreferences.getInt(BEATS_PER_MINUTE, 60);
        meter1 = sharedPreferences.getInt(METER_1, 4);
        meter2 = sharedPreferences.getInt(METER_2, 4);
        isMuteModeActive = sharedPreferences.getBoolean(IS_MUTE_MODE_ACTIVE, false);
        howLongSilent = sharedPreferences.getInt(HOW_LONG_SILENT, 0);
        periodSilent = sharedPreferences.getInt(PERIOD_SILENT, 2);
        volume = sharedPreferences.getFloat(VOLUME, 1f);
        isVibrateModeActive = sharedPreferences.getBoolean(IS_VIBRATE_MODE_ACTIVE, false);

        // Initialization of buttons and layout
        parentLayout = findViewById(R.id.parent_layout_main_activity);
        volumeView = findViewById(R.id.volume_view);
        volumeView.setText(String.valueOf(Math.round(volume / 2 * 10)));
        startButton = findViewById(R.id.start_button);
        bpmButton = findViewById(R.id.BPM_button);
        bpmButton.setText("BPM:\n" + beatsPerMinute);
        vibrateModeButton = findViewById(R.id.vibrate_mode_button_main_activity);

        // Change icon on mode button
        if (isVibrateModeActive) {
            vibrateModeButton.setBackground(getDrawable(R.drawable.ic_baseline_vibration_24));
        } else {
            vibrateModeButton.setBackground(getDrawable(R.drawable.ic_baseline_volume_up_24));
        }

        // Initialization of beats
        beats = new ImageView[4];
        beats[0] = findViewById(R.id.beat_square_1);
        beats[1] = findViewById(R.id.beat_square_2);
        beats[2] = findViewById(R.id.beat_square_3);
        beats[3] = findViewById(R.id.beat_square_4);

        // Getting drawables
        squares = new Drawable [3];
        squares[0] = getDrawable(R.drawable.square_white);
        squares[1] = getDrawable(R.drawable.square_green);
        squares[2] = getDrawable(R.drawable.square_red);

        // Initialization of SoundPool
        soundPool = getSoundPool();
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> isSoundLoaded = true);

        // Getting sound id
        idStandardSound = soundPool.load(this, R.raw.standard_metronome_click, 1);
        idAccentSound = soundPool.load(this, R.raw.accent_metronome_click, 1);

        // Enables Always-on
        setAmbientEnabled();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Get data from OptionActivity
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Get data
            beatsPerMinute = data.getIntExtra(BEATS_PER_MINUTE, 60);
            meter1 = data.getIntExtra(METER_1, 4);
            meter2 = data.getIntExtra(METER_2, 4);
            isMuteModeActive = data.getBooleanExtra(IS_MUTE_MODE_ACTIVE, false);
            howLongSilent = data.getIntExtra(HOW_LONG_SILENT, 0);
            periodSilent = data.getIntExtra(PERIOD_SILENT, 0);

            // Update bpm view
            bpmButton.setText("BPM:\n" + beatsPerMinute);

            // Initialization of editor and saving shared variables from OptionActivity
            // in memory of device
            SharedPreferences sp = Functions.getMySharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.putInt(BEATS_PER_MINUTE, beatsPerMinute);
            editor.putInt(METER_1, meter1);
            editor.putInt(METER_2, meter2);
            editor.putBoolean(IS_MUTE_MODE_ACTIVE, isMuteModeActive);
            editor.putInt(HOW_LONG_SILENT, howLongSilent);
            editor.putInt(PERIOD_SILENT, periodSilent);
            editor.apply();
        }
    }

    public void onClickStartButton(View v) {
        // Change state of button when it is pressed
        if (isPlaying) {
            stopMetronome();
        } else if (isSoundLoaded) {
            startMetronome();
        }
    }

    public void onClickBpmButton(View v) {
        // Starting new activity
        Intent intent = new Intent(this, OptionsActivity.class);
        startActivityForResult(intent, 1);
        stopMetronome();
    }

    public void onClickSettingsButton(View v) {
        // Go to settings layout
        scrollView.scrollTo(0, scrollView.getBottom() + 100);
    }

    public void onClickBackSettingsButton(View view) {
        // Return to main view from settings
        scrollView.scrollTo(0, scrollView.getTop());

        SharedPreferences.Editor editor = Functions.getMySharedPreferences(this).edit();
        editor.putFloat(VOLUME, volume);
        editor.putBoolean(IS_VIBRATE_MODE_ACTIVE, isVibrateModeActive);
        editor.apply();
    }

    public void onClickVolumeDownButton(View view) {
        // Make click quieter
        if (volume > 0.3) {
            volume -= 0.2;
            volumeView.setText(String.valueOf(Math.round(volume / 2 * 10)));
        }
    }

    public void onClickVolumeUpButton(View view) {
        // Make click louder
        if (volume < 1.0) {
            volume += 0.2;
            volumeView.setText(String.valueOf(Math.round(volume / 2 * 10)));
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void onClickVibrateModeButton (View view) {
        // Change mode of metronome
        if (isVibrateModeActive) {
            isVibrateModeActive = false;
            vibrateModeButton.setBackground(getDrawable(R.drawable.ic_baseline_volume_up_24));
        } else {
            isVibrateModeActive = true;
            vibrateModeButton.setBackground(getDrawable(R.drawable.ic_baseline_vibration_24));
        }
    }

    public void onClickParentLayout(View view) {
        // Make layout visible when screen is clicked
        setLayoutVisible(true);
        secondCounter = 0;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void startMetronome() {
        startButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_pause_40));
        isPlaying = true;
        isLayoutVisible = true;

        // Initialization of timer and timer task
        // Main timer task, playing beats
        timer = new Timer();
        TimerTask timerTask1;

        // Change TimerTask by mode of metronome
        if (isMuteModeActive) {
            isSilentCurrently = false;
            silentTactCounter = 0;
            currentBeat = 0;

            // Silent TimerTask
            timerTask1 = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        setSquaresToDefault();

                        // Count when to start playing silent
                        if (currentTact == periodSilent) {
                            isSilentCurrently = true;
                        }

                        // Start playing with sound
                        if (silentTactCounter == howLongSilent) {
                            isSilentCurrently = false;
                            silentTactCounter = 0;
                            currentTact = 0;
                        }

                        // Change how to play
                        if (isSilentCurrently) {
                            playBeatSilent();
                        } else {
                            if (isVibrateModeActive) {
                                playBeatVibrate();
                            } else {
                                playBeat();
                            }
                        }

                        // Counting beats
                        if (currentBeat == meter1 - 1) {
                            currentBeat = 0;
                            currentTact += 1;

                            if (isSilentCurrently) {
                                silentTactCounter += 1;
                            }
                        } else {
                            currentBeat += 1;
                        }
                    });
                }
            };
        } else {
            // Standard TimerTask
            timerTask1 = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        setSquaresToDefault();

                        if (isVibrateModeActive) {
                            playBeatVibrate();
                        } else {
                            playBeat();
                        }

                        // Counting beats
                        if (currentBeat == meter1 - 1) {
                            currentBeat = 0;
                            currentTact += 1;
                        } else {
                            currentBeat += 1;
                        }

                    });
                }
            };
        }

        // Counting when turn screen off
        secondCounter = 0;
        TimerTask timerTask2 = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (secondCounter < 15 && isLayoutVisible) {
                        secondCounter += 1;
                    } else if (isLayoutVisible) {
                        setLayoutVisible(false);
                    }
                });
            }
        };

        timer.scheduleAtFixedRate(timerTask1, 0, 60000 / ((long) beatsPerMinute * (meter2 / 4)));
        timer.scheduleAtFixedRate(timerTask2, 0, 1000);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void stopMetronome() {
        if (isPlaying) {
            startButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_arrow_right_60));
            setSquaresToDefault();
            timer.cancel();

            // Reset all parameters
            isPlaying = false;
            currentBeat = 0;
            currentTact = 0;
            silentTactCounter = 0;
        }
    }

    public void playBeat() {
        if (currentBeat == 0) {
            soundPool.play(idAccentSound, volume, volume, 1, 0 , 1);
            beats[currentBeat % 4].setImageDrawable(squares[2]);
        } else {
            soundPool.play(idStandardSound, volume, volume, 1, 0 , 1);
            beats[currentBeat % 4].setImageDrawable(squares[1]);
        }
    }

    public void playBeatSilent() {
        if (currentBeat == 0) {
            beats[currentBeat % 4].setImageDrawable(squares[2]);
        } else {
            beats[currentBeat % 4].setImageDrawable(squares[1]);
        }
    }

    public void playBeatVibrate() {
        if (currentBeat == 0) {
            beats[currentBeat % 4].setImageDrawable(squares[2]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(vibrationEffectStandard);
            } else {
                vibrator.vibrate(50);
            }
        } else {
            beats[currentBeat % 4].setImageDrawable(squares[1]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(vibrationEffectAccent);
            } else {
                vibrator.vibrate(100);
            }
        }
    }

    public SoundPool getSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        return new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(attributes)
                .build();
    }

    public void setLayoutVisible (boolean visible) {
        if (visible) {
            for (int i = 0; i < parentLayout.getChildCount(); i++) {
                parentLayout.getChildAt(i).setVisibility(View.VISIBLE);
            }
            isLayoutVisible = true;
        } else {
            for (int i = 0; i < parentLayout.getChildCount(); i++) {
                parentLayout.getChildAt(i).setVisibility(View.INVISIBLE);
            }
            isLayoutVisible = false;
        }
    }

    public void setSquaresToDefault() {
        for (ImageView beat : beats) {
            beat.setImageDrawable(squares[0]);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop timer when app is minimalized
        if (timer != null) {
            timer.cancel();
        }
    }
}
