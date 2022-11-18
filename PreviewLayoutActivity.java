package com.itsvks.layouteditor.activities;

import android.os.Bundle;

import com.itsvks.layouteditor.BaseActivity;
import com.itsvks.layouteditor.databinding.ActivityPreviewLayoutBinding;

public class PreviewLayoutActivity extends BaseActivity {

    public static final String EXTRA_KEY_XML = "xml";

    private ActivityPreviewLayoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreviewLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String result = getIntent().getStringExtra(EXTRA_KEY_XML);
    }
}
