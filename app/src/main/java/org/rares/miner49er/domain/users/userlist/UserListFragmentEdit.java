package org.rares.miner49er.domain.users.userlist;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.userlist.UserInterfaces.PositionListener;
import org.rares.miner49er.domain.users.userlist.itemdecorator.VerticalGridSpacingItemDecoration;
import org.rares.miner49er.domain.users.userlist.seek.SmallUsersAdapter;
import org.rares.miner49er.util.UiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserListFragmentEdit extends UserListFragmentPureRv {

    public static final String TAG = UserListFragmentEdit.class.getSimpleName();
    private int spanCount = 2;
    @BindView(R.id.rv_small_users_list)
    RecyclerView smallUsersRv;

    @BindView(R.id.search_container)
    FrameLayout searchContainer;
    @BindView(R.id.et_search)
    AppCompatEditText searchEditText;

    @BindView(R.id.btn_search)
    AppCompatImageButton searchButton;

    @BindDimen(R.dimen.users_list_search_height)
    float searchHeight;

    @BindDimen(R.dimen._list_item_invisible_margin)
    float invisibleMarginHeight;

    private float startTranslation = 0;

    public UserListFragmentEdit() {
    }

    public static UserListFragmentEdit newInstance(long[] userIds, int zero) {
        UserListFragmentEdit fragment = new UserListFragmentEdit();
        Bundle args = new Bundle();
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
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
//        recyclerView = v.findViewById(R.id.rv_users_list);
        UserAdapter userAdapter = UserAdapter.builder()
                .data(Collections.emptyList())
                .selectedData(Collections.emptyList())
                .roleDesigner(roleDesigner)
                .roleDeveloper(roleDeveloper)
                .roleProjectManager(roleProjectManager)
                .positionListener(plLargeToSmall)
                .clickable(true)
                .build();
        recyclerView.setAdapter(userAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), spanCount, RecyclerView.VERTICAL, false));
        int spacing = 12;   // px   //
        boolean includeEdge = true;
        recyclerView.addItemDecoration(new VerticalGridSpacingItemDecoration(spanCount, spacing, includeEdge));
        smallUsersRv.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), RecyclerView.HORIZONTAL, false));
        smallUsersRv.setAdapter(new SmallUsersAdapter(plSmallToLarge));

        searchEditText.setOnTouchListener(deleteSearchOnTouch);

        startTranslation = searchHeight + invisibleMarginHeight;
        searchContainer.setTranslationY(-startTranslation);
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
        }
        if (recyclerView != null) {
            UserAdapter adapter = (UserAdapter) recyclerView.getAdapter();
            SmallUsersAdapter smallAdapter = (SmallUsersAdapter) smallUsersRv.getAdapter();
            if (adapter != null) {
                allUsers = usersDAO.getAll(true).blockingGet();
                List<Long> selectedUsersIds = new ArrayList<>();
                for (UserData ud : team) {
                    selectedUsersIds.add(ud.id);
                }
                adapter.setSelectedData(selectedUsersIds);
                adapter.setData(allUsers);
                if (smallAdapter != null) {
                    smallAdapter.updateData(team);
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

    @OnClick(R.id.btn_search)
    void toggleSearch() {
        boolean reverse = searchContainer.getTranslationY() == 0;

        AnimatorListener addPaddingListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.i(TAG, "onAnimationStart + : " + recyclerView.getPaddingBottom());
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                recyclerView.setPadding(0, 0, 0, (int) startTranslation);
                Log.i(TAG, "onAnimationEnd + : " + recyclerView.getPaddingBottom());
            }
        };

        AnimatorListener removePaddingListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.i(TAG, "onAnimationStart - : " + recyclerView.getPaddingBottom());
                recyclerView.setPadding(0, 0, 0, (int) invisibleMarginHeight);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.i(TAG, "onAnimationEnd - : " + recyclerView.getPaddingBottom());
            }
        };

        searchContainer.animate()
                .translationY(reverse ? -startTranslation : 0)
                .setStartDelay(reverse ? 150 : 0)
                .start();

        recyclerView
                .animate()
                .translationY(reverse ? 0 : startTranslation)
                .setStartDelay(reverse ? 0 : 150)
                .setListener(reverse ? removePaddingListener : addPaddingListener)
                .start();
    }

    private PositionListener plSmallToLarge = userId -> {
        UserAdapter adapter = (UserAdapter) recyclerView.getAdapter();
        int position = -1;
        if (adapter != null) {
            position = adapter.getData().indexOf(usersDAO.get(userId, true).blockingGet().get());
        }
        recyclerView.smoothScrollToPosition(position);
    };

    private PositionListener plLargeToSmall = userId -> {
        SmallUsersAdapter adapter = (SmallUsersAdapter) smallUsersRv.getAdapter();
        if (adapter != null) {
            UserData userData = usersDAO.get(userId, true).blockingGet().get();
            List<UserData> adapterData = adapter.getData();
            int position = deepContains(adapterData, userData);
            if (position > -1) {
                adapterData.remove(position);
                adapter.notifyItemRemoved(position);
            } else {
                adapterData.add(userData);
                adapter.notifyItemInserted(adapterData.size());
            }
        }
    };

    private OnTouchListener deleteSearchOnTouch = (v, event) -> {
//        final int DRAWABLE_LEFT = 0;
//        final int DRAWABLE_TOP = 1;
//        final int DRAWABLE_BOTTOM = 3;
        final int DRAWABLE_RIGHT = 2;
        v.performClick();   // gaah
        AppCompatEditText editText;
        if (v instanceof AppCompatEditText) {
            editText = (AppCompatEditText) v;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (
                        editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    editText.setText("");
                    return true;
                }
            }
        }
        return false;
    };

    private int deepContains(List<UserData> list, UserData data) {
        for (int i = 0; i < list.size(); i++) {
            UserData userData = list.get(i);
            if (userData.id.equals(data.id)) {
                return i;
            }
        }
        return -1;
    }
}