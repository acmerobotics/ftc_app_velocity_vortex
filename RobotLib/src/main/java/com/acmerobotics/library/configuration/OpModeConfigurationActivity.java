package com.acmerobotics.library.configuration;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.acmerobotics.library.R;

public class OpModeConfigurationActivity extends Activity {

    private OpModeConfiguration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opmode_configuration);

        configuration = new OpModeConfiguration(this);

        Spinner allianceColorSpinner = (Spinner) findViewById(R.id.alliance_color_spinner);
        allianceColorSpinner.setSelection(configuration.getAllianceColor().getIndex());
        allianceColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                configuration.setAllianceColor(OpModeConfiguration.AllianceColor.fromIndex(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final TextView delayValueTextView = (TextView) findViewById(R.id.delay_value_text_view);

        SeekBar delaySeekBar = (SeekBar) findViewById(R.id.delay_seek_bar);
        int delay = configuration.getDelay();
        delaySeekBar.setProgress(delay);
        delayValueTextView.setText(delay + "s");
        delaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                configuration.setDelay(progress);
                delayValueTextView.setText(progress + "s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Spinner robotTypeSpinner = (Spinner) findViewById(R.id.robot_type_spinner);
        robotTypeSpinner.setSelection(configuration.getRobotType().getIndex());
        robotTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                configuration.setRobotType(OpModeConfiguration.RobotType.fromIndex(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        configuration.commit();
    }
}
