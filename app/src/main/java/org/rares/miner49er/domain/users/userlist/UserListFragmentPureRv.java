package org.rares.miner49er.domain.users.userlist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.userlist.UserInterfaces.SelectedUsersListConsumer;
import org.rares.miner49er.domain.users.userlist.itemdecorator.HorizontalGridSpacingItemDecoration;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserListFragmentPureRv extends DialogFragment {

    public static final String TAG = UserListFragmentPureRv.class.getSimpleName();
    protected Unbinder unbinder;

    //    protected AsyncGenericDao<ProjectData> projectsDAO;
    protected WeakReference<AsyncGenericDao<UserData>> usersDAO;

    protected WeakReference<SelectedUsersListConsumer> usersListConsumer;
    protected long[] userIds;

    @BindView(R.id.rv_users_list)
    protected RecyclerView recyclerView;

    @BindString(R.string.role_developer)
    String roleDeveloper;
    @BindString(R.string.role_designer)
    String roleDesigner;
    @BindString(R.string.role_project_owner)
    String roleProjectOwner;
    @BindString(R.string.role_project_manager)
    String roleProjectManager;

    public UserListFragmentPureRv() {
    }

    public static UserListFragmentPureRv newInstance(long[] userIds) {
        UserListFragmentPureRv fragment = new UserListFragmentPureRv();
        Bundle args = new Bundle();
        if (userIds != null && userIds.length > 0) {
            args.putLongArray(UserInterfaces.KEY_SELECTED_USERS, userIds);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        projectsDAO = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
        usersDAO = new WeakReference<>(InMemoryCacheAdapterFactory.ofType(UserData.class));

        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            usersListConsumer = new WeakReference<>((SelectedUsersListConsumer) parentFragment);
        } else {
            if (context instanceof SelectedUsersListConsumer) {
                usersListConsumer = new WeakReference<>((SelectedUsersListConsumer) context);
            } else {
                throw new UnsupportedOperationException("This fragment expects a SelectedUsersListConsumer parent.");
            }
        }
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
        int spanCount = 1;
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), spanCount, RecyclerView.HORIZONTAL, false));
//        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), RecyclerView.HORIZONTAL, false));
        recyclerView.addItemDecoration(new HorizontalGridSpacingItemDecoration(spanCount, 18, false));
        if (savedInstanceState != null) {
            userIds = savedInstanceState.getLongArray(UserInterfaces.KEY_SELECTED_USERS);
            Bundle args = getArguments();   // this... smh
            if (args != null) {
                args.putLongArray(UserInterfaces.KEY_SELECTED_USERS, userIds);
            }
            setArguments(args);
        }

        UserAdapter userAdapter = UserAdapter.builder()
                .data(Collections.emptyList())
                .selectedData(Collections.emptyList())
                .roleDesigner(roleDesigner)
                .roleDeveloper(roleDeveloper)
                .roleProjectManager(roleProjectManager)
                .clickable(false)
                .build();
        recyclerView.setAdapter(userAdapter);
        refreshData();
        return recyclerView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (usersListConsumer != null && usersListConsumer.get() != null) {
            usersListConsumer.get().UsersListFragmentClosed(getTag());
            usersListConsumer.clear();
        }
        if (usersDAO != null) {
            usersDAO.clear();
        }
        usersDAO = null;
        usersListConsumer = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void refreshData() {
        getArgs();
        List<UserData> team = new ArrayList<>();
        if (userIds != null) {
            for (long userId : userIds) {
                team.add(usersDAO.get().get(userId, true).blockingGet().get());
                // hopefully the users are already in the cache, but
                // this should be one call: dao.getAllIn(long[] ids)
            }
        }
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
            GridLayoutManager glm = (GridLayoutManager) recyclerView.getLayoutManager();
            if (glm != null) {
                if (adapter.getItemCount() > 2) {
                    glm.setSpanCount(2);
                } else {
                    glm.setSpanCount(1);
                }
            }
        }
    }


    void getArgs() {
        Bundle args = getArguments();
        if (args != null) {
            userIds = args.getLongArray(UserInterfaces.KEY_SELECTED_USERS);
        }
    }

    public void sendSelectedIds() {
        UserAdapter adapter = (UserAdapter) recyclerView.getAdapter();
        long[] selectedList = {};
        if (adapter != null) {
            selectedList = adapter.getSelectedItems();
        }
        if (usersListConsumer != null) {
            usersListConsumer.get().setSelectedList(selectedList);
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLongArray(UserInterfaces.KEY_SELECTED_USERS, userIds);
    }
}
