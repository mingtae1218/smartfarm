package com.example.smartfarm.ui.m01.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartfarm.MainViewModel;
import com.example.smartfarm.databinding.FragmentM01HomeBinding;
import com.example.smartfarm.util.Firmata;


public class HomeFragment extends Fragment  {
    static final int relayPin = 3;                  // 릴레이 모듈 핀 -> 생장 LED 켜기 위함
    static final int cdsPin = 15;                   // 조도센서 모듈 핀
    static final int DHTPIN = 4;                    // 온습도센서 모듈 핀
    static final int soilmoisturePin = 14;          // 토양수분센서 핀

    static final int BUZ_P = 8;

    static final int RED = 26;
    static final int GREEN = 25;
    static final int BLUE = 27;

    static final int SYSEX_AUTO_LED_START = 0x11;
    static final int SYSEX_AUTO_LED_STOP = 0x12;
    static final int SYSEX_LED_ON = 0x21;
    static final int SYSEX_LED_OFF = 0x22;

    public FragmentM01HomeBinding binding;
    HomeViewModel Model;
    MainViewModel mainModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Model = new ViewModelProvider(this).get(HomeViewModel.class);
        mainModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding = FragmentM01HomeBinding.inflate(inflater, container, false);
        if(mainModel.getBluetooth() !=null)
            if(mainModel.getBluetooth().isConnected())
                mainModel.firmata.sendString("Home");

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainModel.bluetoothStatus.observe(getViewLifecycleOwner(), status-> {
            if (mainModel.bluetoothStatus.equals("Connected"))
                mainModel.firmata.sendString("Home");
        });

        //이벤트처리
        //여기다가 연결 로직 작성
        mainModel.SoilReportData.observe(getViewLifecycleOwner(), soil -> {
            binding.Soiltxt.setText(soil+"%");
        });

        mainModel.CDSReportData.observe(getViewLifecycleOwner(), cds -> {
            binding.brighttxt.setText(cds + "%");
        });

        mainModel.TEMPReportData.observe(getViewLifecycleOwner(), TEMP -> {
            binding.Temptxt.setText(TEMP);
        });

        mainModel.HumiReportData.observe(getViewLifecycleOwner(), HUMI -> {
            binding.Humtxt.setText(HUMI);
        });

        //컨넥 상태라면 레이아웃
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