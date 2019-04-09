package org.rares.miner49er.domain.users.userlist;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.projects.ProjectsInterfaces;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.userlist.UserInterfaces.SelectedUsersListConsumer;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserListFragmentPureRv extends Fragment {

    public static final String TAG = UserListFragmentPureRv.class.getSimpleName();
    protected long projectId = -1;
    protected Unbinder unbinder;

    protected AsyncGenericDao<ProjectData> projectsDAO;
    protected AsyncGenericDao<UserData> usersDAO;

    protected SelectedUsersListConsumer usersListConsumer;
    protected long[] userIds;

    protected RecyclerView recyclerView;

    @BindString(R.string.role_developer)
    String roleDeveloper;
    @BindString(R.string.role_designer)
    String roleDesigner;
    @BindString(R.string.role_project_owner)
    String roleProjectOwner;
    @BindString(R.string.role_project_manager)
    String roleProjectManager;

    public static UserListFragmentPureRv newInstance(long projectId, long[] userIds, SelectedUsersListConsumer consumer) {
        UserListFragmentPureRv fragment = new UserListFragmentPureRv();
        Bundle args = new Bundle();
        args.putLong(ProjectsInterfaces.KEY_PROJECT_ID, projectId);
        if (userIds != null && userIds.length > 0) {
            args.putLongArray(UserInterfaces.KEY_SELECTED_USERS, userIds);
        }
        if (consumer != null) {
            args.putSerializable(UserInterfaces.KEY_SELECTED_USERS_CONSUMER, consumer);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getArgs();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_user_list, container, false);
        unbinder = ButterKnife.bind(this, recyclerView);
        int spanCount = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), spanCount, RecyclerView.HORIZONTAL, false));
//        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), RecyclerView.HORIZONTAL, false));
        recyclerView.addItemDecoration(new HorizontalGridSpacingItemDecoration(spanCount, 24, false));

        UserAdapter userAdapter = UserAdapter.builder()
                .data(Collections.emptyList())
                .selectedData(Collections.emptyList())
                .roleDesigner(roleDesigner)
                .roleDeveloper(roleDeveloper)
                .roleProjectManager(roleProjectManager)
                .clickable(false)
                .build();
        refreshData();
        recyclerView.setAdapter(userAdapter);
        return recyclerView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        projectsDAO = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
        usersDAO = InMemoryCacheAdapterFactory.ofType(UserData.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void refreshData() {
        getArgs();
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
                if (adapter == null) {
                    adapter = UserAdapter.builder()
                            .data(team)
                            .selectedData(Collections.emptyList())
                            .roleProjectManager(roleProjectManager)
                            .roleDesigner(roleDesigner)
                            .roleDeveloper(roleDeveloper)
                            .build();
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.setData(team);
                }
            }
        }
    }


    protected void getArgs() {
        Bundle args = getArguments();
        if (args != null) {
            projectId = args.getLong(ProjectsInterfaces.KEY_PROJECT_ID, -1);
            userIds = args.getLongArray(UserInterfaces.KEY_SELECTED_USERS);
            usersListConsumer = (SelectedUsersListConsumer) args.getSerializable(UserInterfaces.KEY_SELECTED_USERS_CONSUMER);
        }
    }

    public void sendSelectedIds() {
        UserAdapter adapter = (UserAdapter) recyclerView.getAdapter();
        List<Long> selectedList = adapter == null ? Collections.emptyList() : adapter.getSelectedItems();
        if (usersListConsumer != null) {
            usersListConsumer.setSelectedList(selectedList);
            Log.i(TAG, "sendSelectedIds: just sent the list: " + selectedList);
        }
    }

    public List<UserData> getUsers() {
        if (recyclerView != null) {
            UserAdapter adapter = (UserAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                return adapter.getData();
            }
        }
        return Collections.emptyList();
    }

}
