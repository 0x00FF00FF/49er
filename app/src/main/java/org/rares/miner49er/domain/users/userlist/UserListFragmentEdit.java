package org.rares.miner49er.domain.users.userlist;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
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
import butterknife.OnTextChanged;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.userlist.UserInterfaces.PositionListener;
import org.rares.miner49er.domain.users.userlist.itemdecorator.VerticalGridSpacingItemDecoration;
import org.rares.miner49er.domain.users.userlist.seek.SmallUsersAdapter;
import org.rares.miner49er.ui.custom.functions.Binder;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        if (disposables == null || disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }
        recyclerView.scrollToPosition(0);
        refreshData();
    }

    @Override
    public void onStop() {
        super.onStop();

        searchEditText.setText("");
        TextUtils.hideKeyboardFrom(getView().findFocus());

        if (disposables != null) {
            disposables.dispose();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setDialogSize();

        BiConsumer<List<UserData>, UserAdapter> userDataConsumer = (data, adapter) -> {
            adapter.setData(data);
            recyclerView.scrollToPosition(0);
        };

        disposables.add(
                searchFlowable
                        .debounce(100, TimeUnit.MILLISECONDS)
                        .subscribe(text -> {
                            UserAdapter userAdapter = (UserAdapter) recyclerView.getAdapter();
                            if (userAdapter != null) {
                                Consumer<List<UserData>> consumer = Binder.bindLast(userDataConsumer, userAdapter);
                                if (text != null && text.length() > 0) {
                                    // cache.getMatching just sorts the list by the closest results, data should remain the same
                                    // using lazy:false will use the db
                                    // db.getMatching will only get matching results (probably 0 or 1)
                                    // db.getMatching will crash the app (when users are selected and user clicks on the small list). todo
                                    disposables.add(
                                            usersDAO
                                                    .getMatching(text.toString(), null, true)
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(consumer));
                                } else {
                                    disposables.add(
                                            usersDAO
                                                    .getAll(true)
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(consumer));
                                }
                            }
                        })
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        disposables.clear();
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

    @OnTextChanged(R.id.et_search)
    public void searchUsers(CharSequence text) {
        searchTermProcessor.onNext(text);
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
        Context context = getContext();

        AnimatorListener addPaddingListener = new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                recyclerView.setPadding(0, 0, 0, (int) startTranslation);
            }
        };

        AnimatorListener removePaddingListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                recyclerView.setPadding(0, 0, 0, (int) invisibleMarginHeight);
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

        if (reverse) {
            if (context != null) {
                TextUtils.hideKeyboardFrom(searchEditText);
            }
        } else {
            TextUtils.showKeyboardFor(searchEditText);
        }
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
        v.performClick();   // gaah!
        AppCompatEditText editText;
        if (v instanceof AppCompatEditText) {
            editText = (AppCompatEditText) v;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (
                        editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (editText.getEditableText().length() > 0) {
                        editText.setText("");
                        editText.clearFocus();
                    } else {
                        editText.clearFocus();
                        toggleSearch();
                    }
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

    private void setDialogSize() {
        LayoutParams params = getDialog().getWindow().getAttributes();

        params.horizontalMargin = (int) UiUtil.pxFromDp(getDialog().getContext(), 24);
        params.verticalMargin = (int) UiUtil.pxFromDp(getDialog().getContext(), 24);
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;

        getDialog().getWindow().setAttributes(params);
    }

    private CompositeDisposable disposables;
    private PublishProcessor<CharSequence> searchTermProcessor = PublishProcessor.create();
    private Flowable<CharSequence> searchFlowable = searchTermProcessor
            .onBackpressureLatest()
            .subscribeOn(Schedulers.computation())
            .share();

}