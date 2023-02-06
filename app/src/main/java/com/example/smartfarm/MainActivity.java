package com.example.smartfarm;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.example.smartfarm.util.ByteReader;
import com.example.smartfarm.util.Firmata;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfarm.databinding.ActivityMainBinding;

import me.aflak.bluetooth.Bluetooth;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    MainViewModel mainModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        //메뉴와 연동 (네비)
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        /*
         * Connect 권한
         */
        permissionGranted();
        /*
        블루트스 생성
         */
        Bluetooth bluetooth = new Bluetooth(this);//1
        bluetooth.setReader(ByteReader.class); //블루트스 세팅 바이트리더 해줘야됨 //2
        mainModel.setBluetooth(bluetooth); //mainModel에서 받음 connect상태 정보 설정같이함.

        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mainModel.bluetooth.isConnected())
                    Snackbar.make(view, "HC-06-TW", Snackbar.LENGTH_LONG)
                            .setAction("Disconnect", v -> {
                                mainModel.bluetooth.disconnect();
                            }).show();
                else
                    Snackbar.make(view, "HC-06-TW", Snackbar.LENGTH_LONG)
                            .setAction("Connect", v->{
                                mainModel.bluetooth.connectToName("HC-06-TW");
                            }).show();

            }
        });
        //연결하면 연결상태 토스트 출력
        mainModel.bluetoothStatus.observe(this, status->{
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            if(status.equals("Connect"))
                mainModel.firmata.sendString(status);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mainModel.bluetooth.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mainModel.bluetooth.onStop();
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    void permissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                    new String[]{
                            android.Manifest.permission.BLUETOOTH,
                            android.Manifest.permission.BLUETOOTH_SCAN,
                            android.Manifest.permission.BLUETOOTH_ADVERTISE,
                            android.Manifest.permission.BLUETOOTH_CONNECT


                    },
                    1);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{
                            android.Manifest.permission.BLUETOOTH

                    },
                    1);
        }
    }
}