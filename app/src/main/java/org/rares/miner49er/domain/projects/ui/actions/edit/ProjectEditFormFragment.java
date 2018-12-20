package org.rares.miner49er.domain.projects.ui.actions.edit;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import butterknife.OnClick;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.ui.actions.ProjectActionFragment;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import static org.rares.miner49er.BaseInterfaces.UTFEnc;
import static org.rares.miner49er.domain.projects.ProjectsInterfaces.KEY_PROJECT_ID;

public class ProjectEditFormFragment extends ProjectActionFragment {

    public static final String TAG = ProjectEditFormFragment.class.getSimpleName();


    public static ProjectEditFormFragment newInstance(long projectId) {
        Bundle args = new Bundle();
        args.putLong(KEY_PROJECT_ID, projectId);

        ProjectEditFormFragment fragment = new ProjectEditFormFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ProjectEditFormFragment() {
    }

    @Override
    public String getActionTag() {
        return TAG;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        createView(inflater, container);
        Object pid = getArguments().get(KEY_PROJECT_ID);
        if (pid == null) {
            throw new IllegalStateException("To edit a project you need an id.");
        }
        populateFields(getArguments().getLong(KEY_PROJECT_ID));

        btnApply.setIcon(getResources().getDrawable(R.drawable.icon_path_done));
        btnApply.setText(R.string.action_save);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
//        UiUtil.sendViewToBack(getView());
        if (projectData != null && projectData.getId() > 0) {
            populateFields(projectData.getId());
        }
    }

    @Override
    @OnClick(R.id.btn_add_project)
    public void applyAction() {
        if (validateForm() && saveProject()) {
            prepareExit();
        }
    }

    public boolean validateForm() {
        boolean validForm = true;
        int scrollToY = container.getHeight();
        int diff = (int) UiUtil.pxFromDp(getContext(), 15);

        if (!validateEmptyText(editTextProjectName, inputLayoutProjectName)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectName.getY() - diff, scrollToY);
        } else {
            if (!validateCharacters(editTextProjectName, inputLayoutProjectName)) {
                validForm = false;
                scrollToY = Math.min((int) inputLayoutProjectName.getY() - diff, scrollToY);
            } else {
                if (!validateExistingName(editTextProjectName, inputLayoutProjectName, projectsDAO)) {
                    validForm = false;
                    scrollToY = Math.min((int) inputLayoutProjectName.getY() - diff, scrollToY);
                }
            }
        }

        if (!validateEmptyText(editTextProjectOwner, inputLayoutProjectOwner)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectOwner.getY() - diff, scrollToY);
        } else {
            if (!validateCharacters(editTextProjectOwner, inputLayoutProjectOwner)) {
                validForm = false;
                scrollToY = Math.min((int) inputLayoutProjectOwner.getY() - diff, scrollToY);
            }
        }

        if (!validateCharacters(editTextProjectShortName, inputLayoutProjectShortName)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectShortName.getY() - diff, scrollToY);
        }

        if (!validateCharacters(editTextProjectDescription, inputLayoutProjectDescription)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectDescription.getY() - diff, scrollToY);
        }

        if (!validateCharacters(editTextProjectIcon, inputLayoutProjectIcon)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectIcon.getY() - diff, scrollToY);
        }

        if (!validForm) {
            rootView.smoothScrollTo(0, scrollToY);
        }

        return validForm;
    }


    private boolean saveProject() {

        projectData.setName(editTextProjectName.getEditableText().toString());
        projectData.setDescription(editTextProjectDescription.getEditableText().toString());
        String iconUrl = editTextProjectIcon.getEditableText().toString();
        try {
            iconUrl = URLEncoder.encode(editTextProjectIcon.getEditableText().toString(), UTFEnc);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        projectData.setIcon(iconUrl);
        projectData.setPicture(iconUrl);

        projectsDAO.update(projectData);

        final String snackbarText = String.format(successfulUpdate, editTextProjectName.getEditableText().toString());

        Snackbar snackbar = Snackbar.make(container, snackbarText, Snackbar.LENGTH_LONG);
//        Drawable snackbarBackground = getContext().getResources().getDrawable(R.drawable.background_snackbar);
        View snackbarView = snackbar.getView();

        snackbarView.setBackgroundColor(snackbarBackgroundColor);

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(snackbarTextColor);

        snackbar.setAction(R.string.action_dismiss, v -> {
            snackbar.dismiss();
        });

        resetFields();
        snackbar.show();
        return true;
    }

    private void populateFields(long projectId) {
        if (projectId <= 0) {
            return;
        }

        projectData = projectsDAO.get(projectId);

        inputLayoutProjectName.setError("");
        editTextProjectName.setText(projectData.getName());
        inputLayoutProjectShortName.setError("");
        editTextProjectShortName.setText(TextUtils.extractVowels(projectData.getName()));
        inputLayoutProjectDescription.setError("");
        editTextProjectDescription.setText(projectData.getDescription());
        inputLayoutProjectIcon.setError("");
        String iconUrl = projectData.getIcon();
        try {
            iconUrl = URLDecoder.decode(projectData.getIcon(), UTFEnc);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        editTextProjectIcon.setText(iconUrl);
        inputLayoutProjectOwner.setError("");
        String ownerName = "";
        if (projectData.getOwner() != null) {
            ownerName = projectData.getOwner().getName();
        }
        editTextProjectOwner.setText(ownerName);
    }

    @Override
    protected boolean validateExistingName(
            TextInputEditText editText,
            TextInputLayout layout,
            GenericDAO<? extends AbstractViewModel> dao) {

        List<? extends AbstractViewModel> entities = dao.getMatching(editText.getEditableText().toString());
        if (entities == null || entities.isEmpty()) {
            layout.setError("");
            return true;
        } else {
            if (entities.size() == 1) {
                if (entities.get(0).getId().equals(projectData.getId())) {
                    layout.setError("");
                    return true;
                }
            }
            layout.setError(errExists);
        }
        return false;
    }
}
