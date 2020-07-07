package org.rares.miner49er;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import lombok.Getter;
import org.rares.miner49er.BaseInterfaces.Messenger;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.cache.optimizer.CacheFeederService;
import org.rares.miner49er.domain.agnostic.SelectedEntityProvider;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.entries.ui.actions.add.TimeEntryAddFormFragment;
import org.rares.miner49er.domain.entries.ui.actions.edit.TimeEntryEditFormFragment;
import org.rares.miner49er.domain.entries.ui.control.TimeEntriesUiOps;
import org.rares.miner49er.domain.issues.decoration.AccDecoration;
import org.rares.miner49er.domain.issues.decoration.IssuesItemDecoration;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.ui.actions.add.IssueAddFormFragment;
import org.rares.miner49er.domain.issues.ui.actions.edit.IssueEditFormFragment;
import org.rares.miner49er.domain.issues.ui.control.IssuesUiOps;
import org.rares.miner49er.domain.projects.ProjectsInterfaces.ProjectsResizeListener;
import org.rares.miner49er.domain.projects.adapter.ProjectsAdapter;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.ui.actions.add.ProjectAddFormFragment;
import org.rares.miner49er.domain.projects.ui.actions.edit.ProjectEditFormFragment;
import org.rares.miner49er.domain.projects.ui.control.ProjectsUiOps;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.layoutmanager.ResizeableLayoutManager;
import org.rares.miner49er.layoutmanager.StickyLinearLayoutManager;
import org.rares.miner49er.layoutmanager.postprocessing.ResizePostProcessor;
import org.rares.miner49er.layoutmanager.postprocessing.rotation.SelfAnimatedItemRotator;
import org.rares.miner49er.network.NetworkingService;
import org.rares.miner49er.network.NetworkingService.RestServiceGenerator;
import org.rares.miner49er.network.ObservableNetworkProgress;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.custom.mask.OverlayMask;
import org.rares.miner49er.ui.fragments.login.animated.LoginLandingConstraintSetFragment;
import org.rares.miner49er.ui.fragments.login.simple.SignInFragment.SignInListener;
import org.rares.miner49er.util.PermissionsUtil;
import org.rares.miner49er.util.UiUtil;
import org.rares.miner49er.viewmodel.HierarchyViewModel;
import org.rares.miner49er.viewmodel.NetworkRequestsModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_PROJECTS;
import static org.rares.miner49er.network.ObservableNetworkProgress.ID_PROJECTS_LIGHT;

public class HomeScrollingActivity
        extends
        AppCompatActivity
        implements
//        DataUpdatedListener,
//        SelectedEntityManager,
//        LandingListener,
        SignInListener,
//        SignUpListener,
        ProjectsResizeListener,
//        DbUpdateFinishedListener,
        Messenger {

    public static final String TAG = HomeScrollingActivity.class.getName();
    private float dp2px;
    private float px2dp;

    List<Unbinder> unbinderList = new ArrayList<>();

//    @BindView(R.id.app_bar)
//    AppBarLayout appBarLayout;

    @BindView(R.id.rv_projects_list)
    RecyclerView projectsRV;

    @BindView(R.id.rv_issues_list)
    RecyclerView issuesRV;

    @BindView(R.id.rv_time_entries_list)
    RecyclerView timeEntriesRv;

    @BindView(R.id.fab2)
    FloatingActionButton fab2;
    @BindView(R.id.fabx)
    FloatingActionButton fabx;

//    @BindView(R.id.fab)
//    FloatingActionButton fab;

    @BindView(R.id.toolbar_c)
    Toolbar toolbar;

    @BindView(R.id.main_container)
    ConstraintLayout mainContainer;

//    @BindView(R.id.toolbar_layout)
//    CollapsingToolbarLayout clt;

    @BindView(R.id.scroll_views_container)
    LinearLayout scrollViewsContainer;

    @BindView(R.id.top_overlay_mask)
    OverlayMask topOverlay;
    @BindView(R.id.bottom_overlay_mask)
    OverlayMask bottomOverlay;

    @BindDimen(R.dimen.projects_rv_collapsed_width)
//    @BindDimen(R.dimen.projects_rv_collapsed_width_with_name)
            int rvCollapsedWidth;

    @BindDimen(R.dimen.projects_rv_collapsed_selected_item_width)
//    @BindDimen(R.dimen.projects_rv_collapsed_selected_item_width_with_name)
            int itemCollapsedSelectedWidth;

    private TimeEntriesUiOps timeEntriesUiOps;
    private IssuesUiOps issuesUiOps;
    private ProjectsUiOps projectsUiOps;

    private LoginLandingConstraintSetFragment llFrag = null;
//    private LoginLandingFragment loginLandingFragment = null;
//    private SignInFragment signInFragment = null;
//    private SignUpFragment signUpFragment = null;


    Unbinder unbinder;

    private NetworkUpdateListener networkUpdateListener = new NetworkUpdateListener();

    private CompositeDisposable startDisposable = new CompositeDisposable();
    private Disposable refreshDisposable = null;
    private Disposable networkTrafficDisposable = null;

    private NetworkRequestsModel networkRequestsModel;
    private ObservableNetworkProgress networkProgress;
    private ViewModelCache cache;
    private HierarchyViewModel hierarchyViewModel;

//    private AsyncGenericDao<ProjectData> asyncDao = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
    // should the view know about the dao/repository? or the cache?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewModelProvider vmp = new ViewModelProvider(this);

        networkRequestsModel = vmp.get(NetworkRequestsModel.class);
        networkProgress = networkRequestsModel.getOnp();
        cache = networkRequestsModel.getVmCache();

        hierarchyViewModel = vmp.get(HierarchyViewModel.class);

        NetworkingService.INSTANCE.start();
        if (cache.lastUpdateTime + 1200 * 1000 <= System.currentTimeMillis()) {
            startCacheUpdate();
        }
//        EntityOptimizer entityOptimizer = new EntityOptimizer.Builder().defaultBuild();
//        NetworkingService.INSTANCE.registerProjectsConsumer(entityOptimizer);   // NS is shut down onDestroy, no leak

//        entityOptimizer.addDbUpdateFinishedListener(this);

        px2dp = UiUtil.dpFromPx(this, 100);
        dp2px = UiUtil.pxFromDp(this, 100);
        Log.i(TAG, "onCreate: px/dp " + px2dp + "|" + dp2px);

        ActivityManager.MemoryInfo memInfo = getAvailableMemory();

        Log.v(TAG, String.format("onCreate: Memory info: available: %s threshold: %s max: %s",
                memInfo.availMem, memInfo.threshold, Runtime.getRuntime().maxMemory()/*memInfo.totalMem*/));

        setContentView(R.layout.activity_home_scrolling);

        if (toolbar == null) {
            toolbar = findViewById(R.id.toolbar_c);
        }

//        toolbar.setNavigationIcon(R.drawable.icon_path_left_arrow);
//        toolbar.setNavigationContentDescription(R.string._toolbar_back_button_description);

//        toolbar.inflateMenu(R.menu.menu_home);
        setSupportActionBar(toolbar);

        unbinder = ButterKnife.bind(this);
//        registerUnbinder(unbinder);

        setupRV();

        if (cache.loggedInUser == null) {
            scrollViewsContainer.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
            topOverlay.setVisibility(View.GONE);
            bottomOverlay.setVisibility(View.GONE);
            fab2.setVisibility(View.GONE);

            /*if (loginLandingFragment == null) {
                loginLandingFragment = LoginLandingFragment.newInstance();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_container, loginLandingFragment, LoginLandingFragment.TAG)
                    .commit();
            */
            llFrag = (LoginLandingConstraintSetFragment) getSupportFragmentManager().findFragmentByTag(LoginLandingConstraintSetFragment.TAG);
            if (llFrag == null) {
                llFrag = LoginLandingConstraintSetFragment.newInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.main_container, llFrag, LoginLandingConstraintSetFragment.TAG)
                        .commit();
            }
        }

        fab2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isNetworkRefresh()) {
                    stopDataUpdateAnimation();
//                    Log.e(TAG, "fab onClick: USER ACTION: REFRESH DATA [stop]");
                    Disposable nuDisposable = networkProgress.getNetworkUpdateDisposable(getSelectedObjectId());
                    if (nuDisposable != null) {
                        nuDisposable.dispose();
                    }
                    nuDisposable = networkUpdateListener.networkDisposable;
                    if (nuDisposable != null) {
                        nuDisposable.dispose();
                    }
//                    fab2.getHandler().postDelayed(() -> {
//                        Completable c = networkProgress.getByObjectId(getSelectedObjectId());
//                        Log.i(TAG, "onClick: " + c);
//                    }, 300);
                    return;
                }

//                Log.d(TAG, "fab onClick: USER ACTION: REFRESH DATA [start]");
                startDataUpdateAnimation();
                if (getSelectedEntityProvider() != null) {
                    getSelectedEntityProvider().updateEntity();
                }
//                int scrollTo = ((AbstractAdapter) projectsRV.getAdapter()).getLastSelectedPosition();
//                projectsRV.smoothScrollToPosition(scrollTo == -1 ? 0 : scrollTo);
//                projectsUiOps.refreshData(false);

//                ViewModelCache.getInstance().dump();


//                issuesUiOps.refreshData();
//                timeEntriesUiOps.refreshData();
            }
        });

        /*
        fab2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "onTouch: " + event.getAction());
                Log.i(TAG, "onTouch: " + event.getX() + "/" + event.getRawX());
                Log.i(TAG, "onTouch: " + event.getY() + "/" + event.getRawY());
                if (MotionEvent.ACTION_BUTTON_PRESS == event.getAction()) {
                    v.performClick();
                }
                if (MotionEvent.ACTION_MOVE == event.getAction()) {
                    v.setX(event.getRawX() - v.getWidth() / 2);
                    v.setY(event.getRawY() - v.getHeight() / 2);
                    v.setAlpha(.4F);
                }
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    v.setAlpha(1F);
                }
                return true;
            }
        });*/

        networkTrafficDisposable = Observable.interval(500, TimeUnit.MILLISECONDS)
            .subscribe(t -> {
                String objectId = getSelectedObjectId();
                Completable completable = networkProgress.getByObjectId(objectId);
//                Disposable nd = networkUpdateListener.networkDisposable;
                Disposable nd = networkProgress.getNetworkUpdateDisposable(objectId);
//                Log.i(TAG, "onCreate: // networkRefresh " + isNetworkRefresh());
//                Log.i(TAG, "onCreate: // objectId -> " + objectId);
//                Log.i(TAG, "onCreate: // refresh animation: " + (refreshDisposable != null && !refreshDisposable.isDisposed()));
//                Log.i(TAG, "onCreate: // nd: " + nd);
//                if (nd != null) {
//                    Log.i(TAG, "onCreate: nd disposed: " + nd.isDisposed());
//                }

                // TODO: 02.06.2020 see if this if chain can be optimized
                if (completable != null && (nd == null || nd.isDisposed())) {
                    completable.subscribe(networkUpdateListener);
//                    Log.d(TAG, "onCreate: // ----------- subscribed");
                } else {
//                    Log.w(TAG, "onCreate: // ----------- not subscribed");
                    if (nd != null && nd.isDisposed()) {
//                        Log.i(TAG, "onCreate: nd not null, nd disposed");
                        stopDataUpdateAnimation();
                    } else if (nd == null) {
//                        Log.i(TAG, "onCreate: nd null");
                        stopDataUpdateAnimation();
                    } else if (!nd.isDisposed()) {
                        startDataUpdateAnimation();
//                        Log.d(TAG, "onCreate: >>>> start update animation.");
//                    } else {
//                        Log.w(TAG, "onCreate: <><><> not stopping animation.");
                    }
                }
            });
        startDisposable.add(networkTrafficDisposable);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        FragmentManager fm = getSupportFragmentManager();

        List<Fragment> fragments = fm.getFragments();
//        for (Fragment fragment : fragments) {
//            Log.i(TAG, "onStart: " + fragment.getClass());
//        }

        Fragment lastFragment = null;
        if (fragments.size() > 0) {
            lastFragment = fragments.get(fragments.size() - 1);
        }

        if (lastFragment instanceof ActionFragment) {
//            Log.i(TAG, "onStart:lastFragment > "+ lastFragment.getClass());
            scrollViewsContainer.setTranslationX(point.x);
        }

//        Log.i(TAG, "onCreate:scrollPositionProjects >>>> " + hierarchyViewModel.scrollPositionProjects);
//        Log.d(TAG, "onCreate:scrollPositionIssues >>>> " + hierarchyViewModel.scrollPositionIssues);
//        Log.i(TAG, "onCreate:scrollPositionTimeEntries >>>> " + hierarchyViewModel.scrollPositionTimeEntries);

        // TODO: 27.05.2020 sticky layout manager -> scroll to position
        projectsRV.scrollToPosition(hierarchyViewModel.scrollPositionProjects);
        issuesRV.scrollToPosition(hierarchyViewModel.scrollPositionIssues);
        timeEntriesRv.scrollToPosition(hierarchyViewModel.scrollPositionTimeEntries);
    }

    @NonNull
    private String getSelectedObjectId() {
        long id = hierarchyViewModel.getSelectedId();
        Class cls = hierarchyViewModel.getSelectedType();
        String objectId = null;
        if (cls != null) {
            // noinspection unchecked
            Cache c = cache.getCache(cls);
            objectId = ((AbstractViewModel) c.getData(id)).getObjectId();
        }
        if (objectId == null) {
            objectId = ID_PROJECTS_LIGHT;
        }
        return objectId;
    }

    private boolean isNetworkRefresh() {
        Disposable nd = networkProgress.getNetworkUpdateDisposable(getSelectedObjectId());
        return nd != null && !nd.isDisposed();
    }

    private SelectedEntityProvider getSelectedEntityProvider() {
        if (hierarchyViewModel.selectedTimeEntryId != -1L) {
            return timeEntriesUiOps;
        }
        if (hierarchyViewModel.selectedIssueId != -1L) {
            return issuesUiOps;
        }
        if (hierarchyViewModel.selectedProjectId != -1L) {
            return projectsUiOps;
        }
        return homeProjectsProvider;
    }

    private void startDataUpdateAnimation() {
        if (refreshDisposable == null) {
//            Log.d(TAG, ">>>>>>>>>>>>>>>>> startDataUpdateAnimation() called");
            refreshDisposable = Flowable.interval(20, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> fab2.setRotation(fab2.getRotation() - 10));
            startDisposable.add(refreshDisposable);
        } /*else {
            Log.v(TAG, ">>>>>>>>>>>>>>>>> startDataUpdateAnimation() called");
        }*/
    }

    private void stopDataUpdateAnimation() {
        if (refreshDisposable != null) {
//            Log.d(TAG, ">>>>>>>>>>>>>>>>> stopDataUpdateAnimation() called");
            refreshDisposable.dispose();
            if (fab2 != null && fab2.getHandler() != null) {
                fab2.getHandler().post(() -> {
                    if (fab2 != null) {
                        int rotations = (int) Math.ceil(fab2.getRotation()) / 180;
                        int toRotation = 180 * (rotations - 1);
                        fab2.animate().rotation(toRotation).withEndAction(() -> {
                            if (fab2 != null) {
                                fab2.setRotation(0);
                            }
                        }).start();
                    }
                });
            }
            refreshDisposable = null;
        } /*else {
            Log.v(TAG, ">>>>>>>>>>>>>>>>> stopDataUpdateAnimation() called");
        }*/
    }

    //    @OnClick(R.id.fab)
    public void onClick(View view) {

        final SpannableStringBuilder snackbarText = new SpannableStringBuilder();
        snackbarText.append("Confucius say\n");
        int italicStart = snackbarText.length();
        snackbarText.append("<< He who click on\nfloating action bar button today...");
        snackbarText.setSpan(new StyleSpan(Typeface.ITALIC), italicStart, snackbarText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Snackbar snackbar = Snackbar.make(view, snackbarText, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setMaxLines(3);

        snackbarText.clear();
        snackbarText.clearSpans();
        snackbarText.append("...will click on floating\naction bar button tomorrow! >>");
        snackbarText.setSpan(new StyleSpan(Typeface.ITALIC), 0, snackbarText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        snackbar.setAction(" ➡️ ", (v) -> {
            Snackbar snackbar_ = Snackbar.make(v, snackbarText, Snackbar.LENGTH_LONG);
            View snackbarView_ = snackbar_.getView();
            TextView textView_ = snackbarView_.findViewById(com.google.android.material.R.id.snackbar_text);
            textView_.setMaxLines(3);
            snackbar_.show();
        }).show();

//        Miner49erApplication.getRefWatcher(this).watch(HomeScrollingActivity.this);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            if (cache.loggedInUser == null) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void inflateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        ToolbarActionManager.addIconToMenuItem(this, menu, R.id.action_add_project, R.drawable.icon_path_add, 0, R.string.action_add_project);
        ToolbarActionManager.addIconToMenuItem(this, menu, R.id.action_settings, R.drawable.icon_path_settings, 0, 0);

        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        ProjectData selectedProject = cache.getCache(ProjectData.class).getData(hierarchyViewModel.selectedProjectId);
        IssueData selectedIssue = cache.getCache(IssueData.class).getData(hierarchyViewModel.selectedIssueId);
        TimeEntryData selectedTimeEntry = cache.getCache(TimeEntryData.class).getData(hierarchyViewModel.selectedTimeEntryId);

        if (fragments.size() > 0) {
            Fragment fragment = fragments.get(fragments.size() - 1);
            if (fragment instanceof ProjectAddFormFragment) {
                projectsUiOps.getMenuActionsProvider().menuAction(R.id.action_add_project, -1);
            }
            if (fragment instanceof ProjectEditFormFragment) {
                // todo: user *can* edit but selects [details]
                if (PermissionsUtil.canEditProject(selectedProject)) {
                    projectsUiOps.getMenuActionsProvider().edit(hierarchyViewModel.selectedProjectId);
                } else {
                    projectsUiOps.getMenuActionsProvider().details(hierarchyViewModel.selectedProjectId);
                }
            }
            if (fragment instanceof IssueAddFormFragment) {
                projectsUiOps.getMenuActionsProvider().add(hierarchyViewModel.selectedProjectId);
            }
            if (fragment instanceof IssueEditFormFragment) {
                if (PermissionsUtil.canAddIssue(selectedProject)) {
                    // todo: user *can* edit but selects [details]
                    projectsUiOps.getMenuActionsProvider().edit(selectedProject.id);
                } else {
                    issuesUiOps.getMenuActionsProvider().details(selectedIssue.id);
                }
            }
            if (fragment instanceof TimeEntryAddFormFragment) {
                issuesUiOps.getMenuActionsProvider().add(selectedIssue.id);
            }
            if (fragment instanceof TimeEntryEditFormFragment) {
//                if (PermissionsUtil.canEditTimeEntry(selectedTimeEntry)) {
                    timeEntriesUiOps.getMenuActionsProvider().edit(selectedTimeEntry.id);
//                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // hw menu key on older phones shows the menu even if no user is logged in.
        MenuItem settingsItem = menu.findItem(R.id.action_settings);

        if (cache.loggedInUser == null) {
            menu.removeItem(R.id.action_add_project);
            settingsItem.setEnabled(false);
            return true;
        } else {
            if (settingsItem != null && !settingsItem.isEnabled()) {
                menu.clear();
                inflateOptionsMenu(menu);
            }
        }

        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProjectsListShrink() {
//        flingBarUp();
    }

    private void setupRV() {

//        projectsRV.setHasFixedSize(true);

        // cache
        projectsRV.setItemViewCacheSize(14);
        issuesRV.setItemViewCacheSize(14);
        timeEntriesRv.setItemViewCacheSize(14);
        timeEntriesRv.getRecycledViewPool().setMaxRecycledViews(0, 14);
        AccDecoration decoration = new AccDecoration();
//        timeEntriesRv.addItemDecoration(new AccDecoration());
        timeEntriesRv.setLayoutManager(new LinearLayoutManager(this));
//        timeEntriesRv.addItemDecoration(new EntriesItemDecoration());
        timeEntriesUiOps = new TimeEntriesUiOps(timeEntriesRv);
        timeEntriesUiOps.setFragmentManager(getSupportFragmentManager());

        RecyclerView.LayoutManager issuesManager = new StickyLinearLayoutManager();
//        RecyclerView.LayoutManager issuesManager = new LinearLayoutManager(this);
        final double shrinkRatio = 1;
        setupResizeableManager(
                issuesManager,
                BaseInterfaces.MAX_ELEVATION_ISSUES,
                (int) UiUtil.pxFromDp(this, 58),
                (int) UiUtil.pxFromDp(this, 56));
        issuesRV.setLayoutManager(issuesManager);
        issuesUiOps = new IssuesUiOps(issuesRV);
        issuesUiOps.setRvCollapsedWidth((int) UiUtil.pxFromDp(this, 56));
        issuesUiOps.setDomainLink(timeEntriesUiOps);
//        decoration.setSelectedPosition(1); // the selected position should get a different color
//        issuesRV.addItemDecoration(decoration);
        issuesRV.addItemDecoration(new IssuesItemDecoration());

        ResizePostProcessor.PostProcessor ipp = new SelfAnimatedItemRotator(issuesRV);
//        ResizePostProcessor.PostProcessor ipp = new AnimatedItemRotator(issuesRV);
//        ResizePostProcessor.PostProcessor ipp = new SimpleItemRotator(issuesRV);

        ipp.setPostProcessConsumer(issuesUiOps);
        issuesUiOps.setResizePostProcessor(ipp);
        issuesUiOps.setFragmentManager(getSupportFragmentManager());

        RecyclerView.LayoutManager projectsLayoutManager = new StickyLinearLayoutManager();
        projectsRV.setLayoutManager(projectsLayoutManager);

        projectsUiOps = new ProjectsUiOps(projectsRV);
        projectsUiOps.setFragmentManager(getSupportFragmentManager());
        projectsUiOps.setProjectsListResizeListener(this);

        ProjectsAdapter projectsAdapter = new ProjectsAdapter(projectsUiOps);
        projectsAdapter.setUnbinderHost(projectsUiOps);
        projectsUiOps.setRvCollapsedWidth(rvCollapsedWidth);
        projectsUiOps.setDomainLink(issuesUiOps);

        projectsRV.setAdapter(projectsAdapter);
//        SimpleLinearLayoutManager projectsLayoutManager = new SimpleLinearLayoutManager(this);
//        LinearLayoutManager projectsLayoutManager = new LinearLayoutManager(this);

        // this condition is true, but here if we want to change
        // our custom managers with the default LLM implementation

        setupResizeableManager(projectsLayoutManager, projectsAdapter.getMaxElevation());
        ResizePostProcessor.PostProcessor pp = new SelfAnimatedItemRotator(projectsRV);
//        ResizePostProcessor.PostProcessor pp = new AnimatedItemRotator();
//        ResizePostProcessor.PostProcessor pp = new SimpleItemRotator();
        pp.setPostProcessConsumer(projectsUiOps);
        projectsUiOps.setResizePostProcessor(pp);

//        projectsRV.addItemDecoration(decoration);

        // pool
        // - cannot share same pool between rvs
        // because of the different types of view holders

//        RecyclerView.RecycledViewPool sharedPool = new RecyclerView.RecycledViewPool();
//        sharedPool.setMaxRecycledViews(R.layout.resizeable_list_item, 40);
//        timeEntriesRv.setRecycledViewPool(sharedPool);
//        issuesRV.setRecycledViewPool(sharedPool);
//        projectsRV.setRecycledViewPool(sharedPool);


        // supportsPredictiveItemAnimations
        Log.e(TAG, "setupRV: DONE");
    }

    private void setupResizeableManager(
            RecyclerView.LayoutManager manager,
            int itemElevation,
            int collapsedSelectedWidth,
            int collapsedWidth
    ) {
        if (manager instanceof ResizeableLayoutManager) {
            // this is not downCasting
            ResizeableLayoutManager mgr = (ResizeableLayoutManager) manager;
            mgr.setItemCollapsedSelectedWidth(collapsedSelectedWidth);
            mgr.setItemCollapsedWidth(collapsedWidth);
            mgr.setMaxItemElevation(itemElevation + 2);
        }
    }

    private void setupResizeableManager(RecyclerView.LayoutManager manager, int itemElevation) {
        setupResizeableManager(manager, itemElevation, itemCollapsedSelectedWidth, rvCollapsedWidth);
    }

    @Override
    public void onBackPressed() {
        ToolbarActionManager toolbarManager = (ToolbarActionManager) toolbar.getTag(R.integer.tag_toolbar_action_manager);
        if (toolbarManager != null) {
            if (toolbarManager.onBackPressed()) {
                return;
            }
        }
        if (llFrag != null && cache.loggedInUser == null) {
            if (llFrag.onBackPressed()) {
                return;
            }
        }
/*        if (signInFragment != null && signInFragment.isResumed()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.item_animation_simple_alpha_in, R.anim.item_animation_simple_alpha_out)
                    .remove(signInFragment)
                    .add(R.id.main_container, loginLandingFragment, LoginLandingFragment.TAG)
                    .commit();
            return;
        }

        if (signUpFragment != null && signUpFragment.isResumed()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.item_animation_simple_alpha_in, R.anim.item_animation_simple_alpha_out)
                    .remove(signUpFragment)
                    .add(R.id.main_container, loginLandingFragment, LoginLandingFragment.TAG)
                    .commit();
            return;
        }*/
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause() called");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume() called");
        super.onResume();
        // make sure we show some data
        // if the user comes back to the app
        // or changes orientation after the cache is filled
        cache.sendEvent(CACHE_EVENT_UPDATE_PROJECTS);
        // can reset an ongoing light projects refresh!
    }

    @Override
    protected void onStart() {
        super.onStart();
        projectsUiOps.setupRepository();
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop() called");
        super.onStop();

        getDisposable(startDisposable).dispose();
        projectsUiOps.shutdown(); // why is this here and the others on destroy?
    }

    @Override
    public void onTrimMemory(int level) {
        // not going to clear cache every time we're not in foreground
        // 10_000 cached items occupy about 2MB of memory
        if (TRIM_MEMORY_RUNNING_CRITICAL == level) {
            cache.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            NetworkingService.INSTANCE.end();
        }

        unbinder.unbind();

        llFrag = null;

        timeEntriesUiOps.shutdown();
        timeEntriesUiOps = null;
        timeEntriesRv = null;

        issuesUiOps.shutdown();
        issuesUiOps = null;
        issuesRV = null;

        projectsUiOps.shutdown();
        projectsUiOps = null;
        projectsRV = null;

        startDisposable.dispose();
    }


    // Get a MemoryInfo object for the device's current memory status.
    private ActivityManager.MemoryInfo getAvailableMemory() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        if (activityManager != null) {
            activityManager.getMemoryInfo(memoryInfo);
        }
        return memoryInfo;
    }

    private CompositeDisposable getDisposable(CompositeDisposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            return disposable;
        }
        return new CompositeDisposable();
    }

    @Override
    public void showMessage(String message, int actionType, Completable action) {
        View container = mainContainer;
        int snackbarBackgroundColor = container.getContext().getResources().getColor(R.color.indigo_100_blacked);
        int snackbarTextColor = container.getContext().getResources().getColor(R.color.pureWhite);

        Snackbar snackbar = Snackbar.make(container, message, Snackbar.LENGTH_LONG);
//        Drawable snackbarBackground = getContext().getResources().getDrawable(R.drawable.background_snackbar);
        View snackbarView = snackbar.getView();

        snackbarView.setBackgroundColor(snackbarBackgroundColor);

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(snackbarTextColor);

        if (UNDOABLE == actionType) {
            snackbar.setAction(R.string.action_undo, v -> action.subscribe(
                    new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            Log.i(TAG, "onComplete: Undid operation.");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: Could not undo the operation.");
                        }
                    }));
        }
        if (DISMISSIBLE == actionType) {
            snackbar.setAction(R.string.action_dismiss, clicklistener -> snackbar.dismiss());
        }
        snackbar.show();
    }

//    @Override
//    public void onDbUpdateFinished(boolean success, int numberOfChanges) {
//        if (success && numberOfChanges > 0) {
//            startCacheUpdate();
//        }
//    }

//    @Override
//    public void dataUpdated(Class cls, List data) {
//        //noinspection unchecked
//        cache.getCache(cls).putData(data, true);
////        Log.v(TAG, "dataUpdated: cache update.");
////        stopDataUpdateAnimation();
//    }

    private void startCacheUpdate() {
        Intent cacheFeederServiceIntent = new Intent(this, CacheFeederService.class);
        startService(cacheFeederServiceIntent);
    }

    /*
        @Override
        public void showSignUp() {
            if (signUpFragment == null) {
                signUpFragment = SignUpFragment.newInstance();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.item_animation_simple_alpha_in, R.anim.item_animation_simple_alpha_out)
                    .remove(loginLandingFragment)
                    .add(R.id.main_container, signUpFragment, SignUpFragment.TAG)
                    .commit();
        }

        @Override
        public void showSignIn() {
            if (signInFragment == null) {
                signInFragment = SignInFragment.newInstance();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.item_animation_simple_alpha_in, R.anim.item_animation_simple_alpha_out)
                    .remove(loginLandingFragment)
                    .add(R.id.main_container, signInFragment, SignInFragment.TAG)
                    .commit();
        }
    */
    @Override
    public void signIn(UserData userData) {
        cache.loggedInUser = userData;
      RestServiceGenerator.INSTANCE.getAuthIntercept().setAuthToken(userData.getApiKey());
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.item_animation_simple_alpha_in, R.anim.item_animation_simple_alpha_out)
//                .remove(signInFragment)
                .remove(llFrag)
                .commit();
        scrollViewsContainer.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        topOverlay.setVisibility(View.VISIBLE);
        bottomOverlay.setVisibility(View.VISIBLE);
        fab2.setVisibility(View.VISIBLE);

        Log.i(TAG, "signIn: >>>>>>>>> cached user: " + cache.loggedInUser + " ");
        projectsUiOps.refreshData();
    }

    public void log(String log) {
        Log.e(TAG, ">>> log: >>> " + log);
    }
/*
    @Override
    public void signUp(UserData userData) {
        ViewModelCache.getInstance().loggedInUser = userData;
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.item_animation_simple_alpha_in, R.anim.item_animation_simple_alpha_out)
                .remove(signUpFragment)
                .commit();
        scrollViewsContainer.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        topOverlay.setVisibility(View.VISIBLE);
        bottomOverlay.setVisibility(View.VISIBLE);
        fab2.setVisibility(View.VISIBLE);
    }
*/

    private class NetworkUpdateListener implements CompletableObserver {

        @Getter
        Disposable networkDisposable = null;

        @Override
        public void onSubscribe(Disposable d) {
            startDataUpdateAnimation();
            networkDisposable = d;
            startDisposable.add(networkDisposable);
        }

        @Override
        public void onError(Throwable t) {
//            Log.w(TAG, "onError: >>> ");
            stopDataUpdateAnimation();
            showMessage(t.getMessage(), Messenger.DISMISSIBLE, null);
            networkDisposable.dispose();
        }

        @Override
        public void onComplete() {
//            Log.d(TAG, "onComplete: >>> ");
            stopDataUpdateAnimation();
            networkDisposable.dispose();
        }
    }


    SelectedEntityProvider homeProjectsProvider = new SelectedEntityProvider() {

        @Override
        public int getEntityType() {
            return -1;
        }

        @Override
        public AbstractViewModel getSelectedEntity() {
            return null;
        }

        @Override
        public void updateEntity() {
            networkRequestsModel.getDataUpdater().lightProjectsUpdate();
        }
    };
}
