package com.itsvks.layouteditor.fragments.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.itsvks.layouteditor.LayoutEditor;
import com.itsvks.layouteditor.ProjectFile;
import com.itsvks.layouteditor.activities.EditorActivity;
import com.itsvks.layouteditor.adapters.ProjectListAdapter;
import com.itsvks.layouteditor.databinding.FragmentHomeBinding;
import com.itsvks.layouteditor.databinding.TextinputlayoutBinding;
import com.itsvks.layouteditor.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("unused")
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SharedPreferences projectTimes;

    private ArrayList<ProjectFile> projects = new ArrayList<>();
    private ProjectListAdapter adapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        projectTimes = PreferenceManager.getDefaultSharedPreferences(LayoutEditor.getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.fab.setOnClickListener(v -> showCreateProjectDialog());
        adapter = new ProjectListAdapter(projects);

        binding.listProjects.setAdapter(adapter);
        binding.listProjects.setLayoutManager(
                new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

        binding.noProjectsView.setVisibility(
                adapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
        binding.listProjects.setVisibility(
                binding.noProjectsView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint({"SimpleDateFormat", "RestrictedApi"})
    @SuppressWarnings("deprecation")
    private void showCreateProjectDialog() {
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Create project");

        final TextinputlayoutBinding bind = TextinputlayoutBinding.inflate(getLayoutInflater());
        final TextInputEditText editText = bind.textinputEdittext;
        final TextInputLayout inputLayout = bind.textinputLayout;

        builder.setView(bind.getRoot(), 10, 10, 10, 10);
        builder.setNegativeButton("Cancel", (di, which) -> {});
        builder.setPositiveButton(
                "Create",
                (di, which) -> createProject(bind.textinputEdittext.getText().toString()));

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        inputLayout.setHint("Enter new project name");
        editText.setText("NewProject" + System.currentTimeMillis());
        editText.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {}

                    @Override
                    public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {}

                    @Override
                    public void afterTextChanged(Editable p1) {
                        checkNameErrors(editText.getText().toString(), null, inputLayout, dialog);
                    }
                });

        editText.requestFocus();

        InputMethodManager inputMethodManager =
                (InputMethodManager)
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

        if (!editText.getText().toString().isEmpty()) {
            editText.setSelection(0, editText.getText().toString().length());
        }

        checkNameErrors(editText.getText().toString(), "", inputLayout, dialog);
    }

    private void loadProjects() {
        projects.clear();

        File root = new File(FileUtil.getPackageDataDir(requireContext()) + "/projects/");

        if (!root.exists()) {
            FileUtil.makeDir(FileUtil.getPackageDataDir(requireContext()) + "/projects/");
        }

        for (File file : root.listFiles()) {
            String path = file.getPath();
            projects.add(new ProjectFile(path, projectTimes.getString(path, getCurrentTime())));
        }

        adapter.notifyDataSetChanged();
    }

    private void createProject(String name) {

        final String projectDir =
                FileUtil.getPackageDataDir(requireContext()) + "/projects/" + name;
        final String time = Calendar.getInstance().getTime().toString();
        FileUtil.makeDir(projectDir);
        FileUtil.makeDir(projectDir + "/drawable/");
        FileUtil.copyFileFromAsset("default_image.png", projectDir + "/drawable");

        ProjectFile project = new ProjectFile(projectDir, time);
        project.saveLayout("");
        projects.add(project);
        adapter.notifyDataSetChanged();

        projectTimes.edit().putString(projectDir, time).apply();

        final Intent intent = new Intent(requireContext(), EditorActivity.class);
        intent.putExtra(EditorActivity.EXTRA_KEY_PROJECT, project);
        startActivity(intent);
    }

    private void checkNameErrors(
            String name, String currentName, TextInputLayout inputLayout, AlertDialog dialog) {
        if (name.equals("")) {
            inputLayout.setErrorEnabled(true);
            inputLayout.setError("Field cannot be empty!");
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            return;
        }

        for (ProjectFile file : projects) {
            if (name.equals(currentName)) break;

            if (file.getName().equals(name)) {
                inputLayout.setErrorEnabled(true);
                inputLayout.setError("Current name is unavailable!");
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                return;
            }
        }

        inputLayout.setErrorEnabled(false);
        inputLayout.setError("");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProjects();
    }

    private String getCurrentTime() {
        return Calendar.getInstance().getTime().toString();
    }
}
