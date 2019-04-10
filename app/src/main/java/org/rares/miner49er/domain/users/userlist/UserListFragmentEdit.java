package org.rares.miner49er.domain.users.userlist;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.ProjectsInterfaces;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.userlist.itemdecorator.VerticalGridSpacingItemDecoration;
import org.rares.miner49er.util.UiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserListFragmentEdit extends UserListFragmentPureRv {

    public static final String TAG = UserListFragmentEdit.class.getSimpleName();
    private int spanCount = 2;

    public UserListFragmentEdit() {
    }

    public static UserListFragmentEdit newInstance(
            long projectId, long[] userIds, int zero) {
        UserListFragmentEdit fragment = new UserListFragmentEdit();
        Bundle args = new Bundle();
        args.putLong(ProjectsInterfaces.KEY_PROJECT_ID, projectId);
        if (userIds != null && userIds.length > 0) {
            args.putLongArray(UserInterfaces.KEY_SELECTED_USERS, userIds);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 3;
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_list_edit, container, false);
//        ConstraintLayout.LayoutParams lp = (LayoutParams) v.getLayoutParams();
//        if (lp != null) {
//            lp.topMargin = (int) UiUtil.pxFromDp(v.getContext(), 24);
//            lp.bottomMargin = (int) UiUtil.pxFromDp(v.getContext(), 24);
//            lp.topToTop = LayoutParams.PARENT_ID;
//            lp.bottomToBottom = LayoutParams.PARENT_ID;
//        }
        unbinder = ButterKnife.bind(this, v);
        recyclerView = v.findViewById(R.id.rv_users_list);
        UserAdapter userAdapter = UserAdapter.builder()
                .data(Collections.emptyList())
                .selectedData(Collections.emptyList())
                .roleDesigner(roleDesigner)
                .roleDeveloper(roleDeveloper)
                .roleProjectManager(roleProjectManager)
                .clickable(true)
                .build();
        refreshData();
        recyclerView.setAdapter(userAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), spanCount, RecyclerView.VERTICAL, false));
        int spacing = 12;   // px   //
        boolean includeEdge = true;
        recyclerView.addItemDecoration(new VerticalGridSpacingItemDecoration(spanCount, spacing, includeEdge));

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerView.scrollToPosition(0);
        refreshData();
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();

        params.horizontalMargin = (int) UiUtil.pxFromDp(getDialog().getContext(), 24);
        params.verticalMargin = (int) UiUtil.pxFromDp(getDialog().getContext(), 24);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;

        getDialog().getWindow().setAttributes(params);
    }

    @OnClick(R.id.btn_add_users)
    public void onOk() {
        sendSelectedIds();
        exitWithoutSaving();
    }

    @OnClick(R.id.btn_cancel_users)
    public void exitWithoutSaving() {
        if (getShowsDialog()) {
            getDialog().cancel();
        } else {
            FragmentManager fm = getFragmentManager();
            if (fm != null) {
                fm.popBackStack();
            }
        }
    }

    public void refreshData() {
        getArgs();
        List<UserData> allUsers;
        List<UserData> team = new ArrayList<>();
        if (userIds != null && userIds.length > 0) {
            for (long userId : userIds) {
                team.add(usersDAO.get(userId, true).blockingGet().get());
                // hopefully the users are already in the cache, but
                // this should be one call: dao.getAllIn(long[] ids)
            }
        } else {
            if (projectId > 0) {
                ProjectData projectData = projectsDAO.get(projectId, true).blockingGet().get();
                team = projectData.getTeam();
            }
        }
        if (team != null) {
            if (recyclerView != null) {
                UserAdapter adapter = (UserAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    allUsers = usersDAO.getAll(true).blockingGet();
                    List<Long> selectedUsersIds = new ArrayList<>();
                    for (UserData ud : team) {
                        selectedUsersIds.add(ud.id);
                    }
                    adapter.setSelectedData(selectedUsersIds);
                    adapter.setData(allUsers);
                }
            }
        }
    }

    @Override
    public List<UserData> getUsers() {
        if (recyclerView != null) {
            UserAdapter adapter = (UserAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                List<UserData> users = new ArrayList<>();
                for (Long l : adapter.getSelectedItems()) {
                    users.add(usersDAO.get(l, true).blockingGet().get());
                }
                return users;
            }
        }
        return Collections.emptyList();
    }
}