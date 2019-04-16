package org.rares.miner49er.domain.projects.ui.actions.edit;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import butterknife.OnClick;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.pushtorefresh.storio3.Optional;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.ui.actions.ProjectActionFragment;
import org.rares.miner49er.domain.users.userlist.UserInterfaces;
import org.rares.miner49er.domain.users.userlist.UserListFragmentPureRv;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.ui.custom.glide.GlideApp;
import org.rares.miner49er.ui.custom.validation.FormValidationException;
import org.rares.miner49er.ui.custom.validation.FormValidator;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

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
        Log.d(TAG, "onCreateView() called ");

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
    @OnClick(R.id.btn_add_project)
    public void applyAction() {
        if (validateForm() && saveProject()) {
            prepareExit();
        }
    }

    public boolean validateForm() {
        clearErrors();
        updateProjectData();
        FormValidator<ProjectData> validator = FormValidator.of(projectData);
        try {
            validator.validate(ProjectData::getName, n -> !n.isEmpty(), inputLayoutProjectName, errRequired)
                    .validate(ProjectData::getName, p -> !p.contains("#"), inputLayoutProjectName, errCharacters)
                    .validate(ProjectData::getName, name -> {
                        List<? extends AbstractViewModel> entities =
                                projectsDAO.getMatching(name, Optional.of(null), true).blockingGet();
                        if ((entities == null || entities.isEmpty())) {
                            return true;
                        }
                        // TODO: optimize this by querying the database for results not including projectData.id
                        for (int i = 0; i < entities.size(); i++) {
                            ProjectData pd = (ProjectData) entities.get(i);
                            if (pd.id.equals(projectData.id)) {
                                entities.remove(pd);
                                break;
                            }
                        }
                        return entities.isEmpty();
                    }, inputLayoutProjectName, errExists)
                    .validate(ProjectData::getOwner, o -> o != null, inputLayoutProjectOwner, errRequired)
                    .validate(ProjectData::getDescription, d -> !d.contains("#"), inputLayoutProjectDescription, errCharacters)
                    .validate(ProjectData::getIcon, d -> !d.contains("#"), inputLayoutProjectIcon, errCharacters)
                    .get();
        } catch (FormValidationException e) {
            int scrollToY = container.getHeight();
            int diff = (int) UiUtil.pxFromDp(getContext(), 15);
            Map<Object, String> errors = e.getInvalidFields();
            for (Object o : errors.keySet()) {
                TextInputLayout layout = ((TextInputLayout) o);
                layout.setError(errors.get(o));
                scrollToY = Math.min((int) layout.getY() - diff, scrollToY);
            }
            rootView.smoothScrollTo(0, scrollToY);
            return false;
        }
        return true;
    }

    private boolean saveProject() {

        updateProjectData();

        boolean updated = projectsDAO.update(projectData).blockingGet();

        final String snackbarText = String.format(updated ? successfulUpdate : errNotCompleted, editTextProjectName.getEditableText().toString());

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
        Log.d(TAG, "populateFields() called with: projectId = [" + projectId + "]");
        if (projectId <= 0) {
            return;
        }

        projectData = projectsDAO.get(projectId, true).blockingGet().get().clone();        ////

        long[] ids = getUsersIds(projectData.getTeam());
        if (userListFragment == null) {
            userListFragment = UserListFragmentPureRv.newInstance(ids);
        } else {
            Bundle args = new Bundle();
            args.putLongArray(UserInterfaces.KEY_SELECTED_USERS, ids);
            userListFragment.setArguments(args);
        }
        userListFragment.refreshData();

        getChildFragmentManager().beginTransaction()
                .show(userListFragment)
                .commit();

        clearErrors();
        editTextProjectName.setText(projectData.getName());
        editTextProjectShortName.setText(projectData.getName().length() > 4 ? TextUtils.extractVowels(projectData.getName()) : projectData.getName());
        editTextProjectDescription.setText(projectData.getDescription());
        String iconUrl = projectData.getIcon();
        try {
            if (!iconUrl.startsWith("/")) {
                iconUrl = URLDecoder.decode(projectData.getIcon(), UTFEnc);
            }
            GlideApp
                    .with(getContext())
                    .load(iconUrl)
                    .into(projectIconImage);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        editTextProjectIcon.setText(iconUrl);
        String ownerName = "";
        if (projectData.getOwner() != null) {
            ownerName = projectData.getOwner().getName();
        } else {
            Log.i(TAG, "populateFields: OWNER NULL");
        }
        editTextProjectOwner.setText(ownerName);

        Log.i(TAG, "populateFields: " + editTextProjectOwner.getEditableText() + " " +
                editTextProjectName.getEditableText() + " " +
                editTextProjectShortName.getEditableText() + " " +
                editTextProjectDescription.getEditableText() + " " +
                editTextProjectIcon.getEditableText());
    }
}
