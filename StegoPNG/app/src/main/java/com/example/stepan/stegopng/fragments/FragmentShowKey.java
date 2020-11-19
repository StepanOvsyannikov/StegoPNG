package com.example.stepan.stegopng.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.stepan.stegopng.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;


public class FragmentShowKey extends Fragment {
    View rootView;

    TextView textView_showKey;
    String OpenKEY = "file_for_OpenKEY";
    String Key;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_show_key, container, false);

        textView_showKey = rootView.findViewById(R.id.textView_showKey);
        read_openKEY();
        textView_showKey.setText("стегоключ = " + Key);

        return rootView;
    }


    void read_openKEY() {
        Key = "";
        try {
            BufferedReader br_for_open_text = new BufferedReader(new InputStreamReader(getActivity().openFileInput(OpenKEY)));
            String str_for_open_text = "";
            while ((str_for_open_text = br_for_open_text.readLine()) != null) {
                Key = Key + str_for_open_text;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
