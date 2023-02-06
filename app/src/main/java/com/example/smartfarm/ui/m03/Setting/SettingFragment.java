package com.example.smartfarm.ui.m03.Setting;

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
import com.example.smartfarm.databinding.FragmentM03SettingBinding;

public class SettingFragment extends Fragment {

    private FragmentM03SettingBinding binding;
    SlideshowViewModel Model;
    MainViewModel mainModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Model = new ViewModelProvider(this).get(SlideshowViewModel.class);
        mainModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding = FragmentM03SettingBinding.inflate(inflater, container, false);

        if(mainModel.getBluetooth() !=null)
            if(mainModel.getBluetooth().isConnected())
                mainModel.firmata.sendString("Setting");

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainModel.firmataVersionData.observe(getViewLifecycleOwner(), version->{
            binding.textF9.setText(String.format("Firmata V%d.%d",version.getMajor(),version.getMinor()));

        });
        binding.buttonF9.setOnClickListener(v->{
            mainModel.firmata.sendRequestVersion();
        });
        binding.btnFF.setOnClickListener(v->{
            mainModel.firmata.sendSystemReset();
        });




    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}