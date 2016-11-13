package org.firstinspires.ftc.robotcontroller.internal;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.qualcomm.ftcrobotcontroller.R;

public class OpModeConfigurationActivity extends Activity {

    private OpModeConfiguration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_op_mode_configuration);

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
    }

    @Override
    protected void onPause() {
        super.onPause();

        configuration.commit();
    }
}
