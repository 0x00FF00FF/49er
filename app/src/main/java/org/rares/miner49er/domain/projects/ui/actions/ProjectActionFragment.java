package org.rares.miner49er.domain.projects.ui.actions;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.projects.ProjectsInterfaces;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.userlist.UserInterfaces;
import org.rares.miner49er.domain.users.userlist.UserListFragmentEdit;
import org.rares.miner49er.domain.users.userlist.UserListFragmentPureRv;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.util.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.rares.miner49er.BaseInterfaces.UTFEnc;

public abstract class ProjectActionFragment
        extends ActionFragment
        implements UserInterfaces.SelectedUsersListConsumer {

    public static final String TAG = ProjectActionFragment.class.getSimpleName();

    @BindView(R.id.content_container)
    protected ConstraintLayout container;

    @BindView(R.id.project_name_input_layout)
    protected TextInputLayout inputLayoutProjectName;
    @BindView(R.id.project_name_input_layout_edit)
    protected TextInputEditText editTextProjectName;
    @BindView(R.id.project_short_name_input_layout)
    protected TextInputLayout inputLayoutProjectShortName;
    @BindView(R.id.project_short_name_input_layout_edit)
    protected TextInputEditText editTextProjectShortName;
    @BindView(R.id.project_description_input_layout)
    protected TextInputLayout inputLayoutProjectDescription;
    @BindView(R.id.project_description_input_layout_edit)
    protected TextInputEditText editTextProjectDescription;
    @BindView(R.id.project_icon_input_layout)
    protected TextInputLayout inputLayoutProjectIcon;
    @BindView(R.id.project_icon_input_layout_edit)
    protected TextInputEditText editTextProjectIcon;
    @BindView(R.id.project_owner_input_layout)
    protected TextInputLayout inputLayoutProjectOwner;
    @BindView(R.id.project_owner_input_layout_edit)
    protected TextInputEditText editTextProjectOwner;

    @BindView(R.id.btn_add_project)
    protected MaterialButton btnApply;
    @BindView(R.id.btn_cancel_add_project)
    protected MaterialButton btnCancel;

    @BindView(R.id.btn_edit_users)
    protected AppCompatImageView btnEditUsers;

    protected UserListFragmentPureRv userListFragment;
    protected UserListFragmentEdit userListFragmentEdit;

    protected AsyncGenericDao<ProjectData> projectsDAO;
    protected AsyncGenericDao<UserData> usersDAO;

    protected ProjectData projectData = null;
    protected List<UserData> team;

    protected View createView(LayoutInflater inflater, ViewGroup container) {

        rootView = (ScrollView) inflater.inflate(R.layout.fragment_project_edit, container, false);
        setReplacedView(container.findViewById(R.id.scroll_views_container));        //

        userListFragment = (UserListFragmentPureRv) getChildFragmentManager().findFragmentById(R.id.users_rv);

        unbinder = ButterKnife.bind(this, rootView);
        prepareEntry();
        rootView.setSmoothScrollingEnabled(true);

        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        };

        InputFilter[] filters = new InputFilter[]{filter};

        editTextProjectOwner.setFilters(filters);
        editTextProjectName.setFilters(filters);
        editTextProjectShortName.setFilters(filters);
        editTextProjectDescription.setFilters(filters);
        editTextProjectIcon.setFilters(filters);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        projectsDAO = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
        usersDAO = InMemoryCacheAdapterFactory.ofType(UserData.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        rootView.smoothScrollTo(0, 0);
    }

    @Override
    public void prepareExit() {
        resetFields();

        Context context = getContext();
        if (context != null) {
            TextUtils.hideKeyboardFrom(context, rootView.findFocus());
        }

        actionFragmentTransition.prepareExitAnimation(getView(), replacedView);
        resultListener.onFragmentDismiss();
        getFragmentManager().popBackStack();  //
    }

    @Override
    public void prepareEntry() {
        actionFragmentTransition.prepareEntryAnimation(replacedView);
    }

    @OnClick(R.id.btn_cancel_add_project)
    public void cancelAction() {
        /*projectData = null;*/
        resetFields();
        prepareExit();
    }


    protected void updateProjectData() {
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
        projectData.setTeam(userListFragment.getUsers());
    }

    protected void resetFields() {
        rootView.smoothScrollTo(0, 0);
        clearErrors();
        editTextProjectName.setText("");
        editTextProjectShortName.setText("");
        editTextProjectDescription.setText("");
        editTextProjectIcon.setText("");
        editTextProjectOwner.setText("");
    }

    protected void clearErrors() {
        inputLayoutProjectName.setError("");
        inputLayoutProjectShortName.setError("");
        inputLayoutProjectDescription.setError("");
        inputLayoutProjectIcon.setError("");
        inputLayoutProjectOwner.setError("");
    }

    @OnClick(R.id.btn_edit_users)
    public void showUsersEditFragment() {
        btnEditUsers.setEnabled(false);
        List<UserData> users = userListFragment.getUsers();
        long[] ids = new long[users.size()];
        for (int i = 0; i < users.size(); i++) {
            ids[i] = users.get(i).id;
            Log.i(TAG, "showUsersEditFragment: " + ids[i]);
        }
        long prId = projectData == null || projectData.id == null ? -1 : projectData.id;
        if (userListFragmentEdit == null) {
            userListFragmentEdit = UserListFragmentEdit.newInstance(prId, ids, this, 0);
        } else {
            Bundle args = new Bundle();
            args.putLong(ProjectsInterfaces.KEY_PROJECT_ID, prId);
            args.putLongArray(UserInterfaces.KEY_SELECTED_USERS, ids);
            args.putSerializable(UserInterfaces.KEY_SELECTED_USERS_CONSUMER, this);
            userListFragmentEdit.setArguments(args);
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .add(container.getId(), userListFragmentEdit, UserListFragmentEdit.TAG)
                .addToBackStack(userListFragment.getTag())
                .show(userListFragmentEdit)
                .commit();
    }

    @Override
    public void fragmentClosed(String tag) {
        if (tag != null && tag.equals(userListFragmentEdit.getTag())) {
            btnEditUsers.setEnabled(true);
        }
    }

    @Override
    public void setSelectedList(List<Long> selectedUsersList) {
        if (team == null) {
            team = new ArrayList<>();
        }
        team.clear();
        for (Long userId : selectedUsersList) {
            team.add(usersDAO.get(userId, true).blockingGet().get());
        }
        long[] ids = new long[selectedUsersList.size()];
        for (int i = 0; i < selectedUsersList.size(); i++) {
            ids[i] = selectedUsersList.get(i);
        }
        Bundle args = userListFragment.getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putLongArray(UserInterfaces.KEY_SELECTED_USERS, ids);
        userListFragment.setArguments(args);

        userListFragment.refreshData();
    }
}
