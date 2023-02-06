package com.example.smartfarm;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartfarm.databinding.FragmentM01HomeBinding;
import com.example.smartfarm.ui.m01.home.HomeFragment;
import com.example.smartfarm.ui.m01.home.HomeViewModel;
import com.example.smartfarm.util.Firmata;
import com.example.smartfarm.util.FirmataVersionData;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.DeviceCallback;

public class MainViewModel extends ViewModel {
    static final int relayPin = 3;                  // 릴레이 모듈 핀 -> 생장 LED 켜기 위함
    static final int cdsPin = 15;                   // 조도센서 모듈 핀
    static final int DHTPIN = 4;                    // 온습도센서 모듈 핀
    static final int DHTPIN2 = 5;                    // 온습도센서 모듈 핀
    static final int soilmoisturePin = 14;          // 토양수분센서 핀

    static final int BUZ_P = 8;

    static final int RED = 26;
    static final int GREEN = 25;
    static final int BLUE = 27;

    static final int SYSEX_AUTO_LED_START = 0x11;
    static final int SYSEX_AUTO_LED_STOP = 0x12;
    static final int SYSEX_LED_ON = 0x21;
    static final int SYSEX_LED_OFF = 0x22;


    //다른 화면에서도 사용
    public Bluetooth bluetooth;


    public Firmata firmata;
    FragmentM01HomeBinding m01Binding;
    //블루투스 상태 처리
    public MutableLiveData<String> bluetoothStatus = new MutableLiveData<>();
    public MutableLiveData<FirmataVersionData> firmataVersionData = new MutableLiveData<>();
    public MutableLiveData<String> SoilReportData = new MutableLiveData<>();
    public MutableLiveData<String> CDSReportData = new MutableLiveData<>();
    public MutableLiveData<String> TEMPReportData = new MutableLiveData<>();
    public MutableLiveData<String> HumiReportData = new MutableLiveData<>();

    public MainViewModel(){
        bluetoothStatus.setValue("Disconnected");
        firmataVersionData.setValue(new FirmataVersionData());
    }

    void setBluetooth(Bluetooth bluetooth){
        this.bluetooth = bluetooth;
        firmata =new Firmata(this.bluetooth);


        //상태 정보
        bluetooth.setDeviceCallback(new DeviceCallback() {
            //블루트스 컨넥되면호출
            @Override
            public void onDeviceConnected(BluetoothDevice device) {
                bluetoothStatus.postValue("Connected");

            }
            //블루트스 끊어지면 호출
            @Override
            public void onDeviceDisconnected(BluetoothDevice device, String message) {
                bluetoothStatus.postValue("Disconnected");
            }

            @Override
            public void onMessage(byte[] message) {
                firmata.processInput(message); //데이터가 들어오면 퍼메타가 판단하도록 프로세스 인풋함

            }

            @Override
            public void onError(int errorCode) {
                bluetoothStatus.postValue("Error");

            }

            @Override
            public void onConnectError(BluetoothDevice device, String message) {
                bluetoothStatus.postValue("Connect Error");

            }
        });

        firmata.attach((m,n)->{
            FirmataVersionData version = new FirmataVersionData();
            version.setMajor(m);
            version.setMinor(n);
            firmataVersionData.postValue(version);
        });
        firmata.attach(Firmata.ANALOG_MESSAGE, (pin, value) -> {
            switch (pin) {
                case soilmoisturePin%14:
                    SoilReportData.postValue(value+"");
                    break;
                case cdsPin%14:
                    CDSReportData.postValue(value+"");
                    break;
            }
        });
        firmata.attach(Firmata.DIGITAL_MESSAGE, (pin, value)-> {
            switch (pin) {
                case DHTPIN:
                    TEMPReportData.postValue(value+"");
                    break;
                case DHTPIN2:
                    HumiReportData.postValue(value+"");
                    break;
            }
        });

    }
    public Bluetooth getBluetooth(){

        return bluetooth;
    }

}
