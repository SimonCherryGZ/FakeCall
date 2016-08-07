package com.simoncherry.fakecall.fragment;


import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simoncherry.fakecall.R;
import com.simoncherry.fakecall.util.SettingTextWatcher;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

    public static final String PREFER_NAME = "com.simoncherry.fakecall";

    private EditTextPreference mVadbosPreference;
    private EditTextPreference mVadeosPreference;

    private EditTextPreference mSpeedPreference;
    private EditTextPreference mPitchPreference;
    private EditTextPreference mVolumePreference;

    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTheme(android.R.style.Theme_Black_NoTitleBar);
        getPreferenceManager().setSharedPreferencesName(PREFER_NAME);
        addPreferencesFromResource(R.xml.iflytek_setting);

        mVadbosPreference = (EditTextPreference)findPreference("iat_vadbos_preference");
        mVadbosPreference.getEditText().addTextChangedListener(new SettingTextWatcher(getActivity(), mVadbosPreference,0,10000));

        mVadeosPreference = (EditTextPreference)findPreference("iat_vadeos_preference");
        mVadeosPreference.getEditText().addTextChangedListener(new SettingTextWatcher(getActivity(), mVadeosPreference,0,10000));

        mSpeedPreference = (EditTextPreference)findPreference("speed_preference");
        mSpeedPreference.getEditText().addTextChangedListener(new SettingTextWatcher(getActivity(), mSpeedPreference,0,200));

        mPitchPreference = (EditTextPreference)findPreference("pitch_preference");
        mPitchPreference.getEditText().addTextChangedListener(new SettingTextWatcher(getActivity(), mPitchPreference,0,100));

        mVolumePreference = (EditTextPreference)findPreference("volume_preference");
        mVolumePreference.getEditText().addTextChangedListener(new SettingTextWatcher(getActivity(), mVolumePreference,0,100));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundResource(android.R.color.black);
        return view;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }
}
