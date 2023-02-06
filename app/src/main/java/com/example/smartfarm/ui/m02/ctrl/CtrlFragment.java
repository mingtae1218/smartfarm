package com.example.smartfarm.ui.m02.ctrl;
// RGB 레이아웃 여기서 건들여야함.
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartfarm.MainViewModel;
import com.example.smartfarm.databinding.FragmentM02CtrlBinding;


public class CtrlFragment extends Fragment {

    static final byte RED = 12;
    static final byte GREEN = 11;
    static final byte BLUE = 13;

    static final int SYSEX_AUTO_LED_START = 0x11;
    static final int SYSEX_AUTO_LED_STOP = 0x12;
    static final int SYSEX_LED_ON = 0x21;
    static final int SYSEX_LED_OFF = 0x22;

    private FragmentM02CtrlBinding binding;
    CtrlViewModel Model;
    MainViewModel mainModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Model = new ViewModelProvider(this).get(CtrlViewModel.class);
        mainModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding = FragmentM02CtrlBinding.inflate(inflater, container, false);

        if(mainModel.getBluetooth() !=null)
            if(mainModel.getBluetooth().isConnected())
                mainModel.firmata.sendString("Ctrl");

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.switchAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.switchAuto.isChecked()) {
                    binding.switchLed.setEnabled(false);
                    binding.switchLed.setChecked(false);
                    mainModel.firmata.sendSysex((byte)SYSEX_AUTO_LED_START,(byte)0,new byte[]{});
                    //오토모드 on코드 입력
                }else{
                    binding.switchLed.setEnabled(true);
                    mainModel.firmata.sendSysex((byte)SYSEX_AUTO_LED_STOP,(byte)0,new byte[]{});
                    //오토모드 OFF코드 입력
                }

            }
        });
        binding.switchLed.setOnClickListener(view1 -> {
            if(binding.switchLed.isChecked()){
                mainModel.firmata.sendSysex((byte)SYSEX_LED_ON,(byte)0,new byte[]{});
            }else{
                mainModel.firmata.sendSysex((byte)SYSEX_LED_OFF,(byte)0,new byte[]{});
                //LED OFF 코드
            }
        });


        binding.sbRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mainModel.firmata.sendAnalog(RED, progress);
                binding.txtRGB.setBackgroundColor(Color.rgb(progress, binding.sbGreen.getProgress(), binding.sbBlue.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.sbGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mainModel.firmata.sendAnalog(GREEN, progress);
                binding.txtRGB.setBackgroundColor(Color.rgb(binding.sbRed.getProgress(),progress, binding.sbBlue.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.sbBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mainModel.firmata.sendAnalog(BLUE, progress);
                binding.txtRGB.setBackgroundColor(Color.rgb(binding.sbRed.getProgress(),binding.sbGreen.getProgress(),progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mainModel.bluetoothStatus.observe(getViewLifecycleOwner(), status->{
            if(status.equals("Connected")){
                binding.layoutConnect.setVisibility(View.VISIBLE);
                binding.include.layoutDisconnected.setVisibility(View.INVISIBLE);
            }else{
                binding.layoutConnect.setVisibility(View.INVISIBLE);
                binding.include.layoutDisconnected.setVisibility(View.VISIBLE);

            }

        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}