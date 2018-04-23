package org.rares.miner49er;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.rares.miner49er.entries.TimeEntriesUiOps;
import org.rares.miner49er.issues.IssuesUiOps;
import org.rares.miner49er.layoutmanager.ResizeableLayoutManager;
import org.rares.miner49er.layoutmanager.StickyLinearLayoutManager;
import org.rares.miner49er.projects.ProjectsInterfaces.ProjectsResizeListener;
import org.rares.miner49er.projects.ProjectsUiOps;
import org.rares.miner49er.projects.adapter.ProjectsAdapter;
import org.rares.miner49er.util.BehaviorFix;
import org.rares.miner49er.util.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class HomeScrollingActivity
        extends
        AppCompatActivity
        implements
        BaseInterfaces.UnbinderHost,
        ProjectsResizeListener {

    public static final String TAG = HomeScrollingActivity.class.getName();
    private float dp2px;
    private float px2dp;

    List<Unbinder> unbinderList = new ArrayList<>();

    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;

    @BindView(R.id.rv_projects_list)
    RecyclerView projectsRV;

    @BindView(R.id.rv_issues_list)
    RecyclerView issuesRV;

    @BindView(R.id.rv_time_entries_list)
    RecyclerView timeEntriesRv;

    @BindView(R.id.fab2)
    FloatingActionButton fab2;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.main_container)
    CoordinatorLayout mainContainer;

    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout clt;

    @BindView(R.id.scroll_views_container)
    LinearLayout scrollViewsContainer;

    @BindDimen(R.dimen.projects_rv_collapsed_width)
    int rvCollapsedWidth;

    @BindDimen(R.dimen.projects_rv_collapsed_selected_item_width)
    int itemCollapsedSelectedWidth;

    private IssuesUiOps issuesUiOps;
    private ProjectsUiOps projectsUiOps;

    Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        px2dp = DisplayUtil.dpFromPx(this, 100);
        dp2px = DisplayUtil.pxFromDp(this, 100);
        Log.i(TAG, "onCreate: px/dp " + px2dp + "|" + dp2px);

        setContentView(R.layout.activity_home_scrolling);

        unbinder = ButterKnife.bind(this);
        registerUnbinder(unbinder);

        setSupportActionBar(toolbar);

        setupRV();

        fab2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                projectsUiOps.removeItem();
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


//        Miner49erApplication.getRefWatcher(this).watch(this);
    }


    @OnClick(R.id.fab)
    public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_scrolling, menu);
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
        flingBarUp();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e(TAG, "onTouchEvent");
        return super.onTouchEvent(event);
    }

    private void setupRV() {

        // cache
        projectsRV.setItemViewCacheSize(4);
        issuesRV.setItemViewCacheSize(4);
        timeEntriesRv.setItemViewCacheSize(4);

        timeEntriesRv.setLayoutManager(new LinearLayoutManager(this));
        TimeEntriesUiOps timeEntriesOps = new TimeEntriesUiOps();
        timeEntriesOps.setRv(timeEntriesRv);

        issuesRV.setLayoutManager(new LinearLayoutManager(this));
        issuesUiOps = new IssuesUiOps();
        issuesUiOps.setRvCollapsedWidth(rvCollapsedWidth);  /////// dagger here? or maybe just butterKnife?
        issuesUiOps.setRv(issuesRV);
        issuesUiOps.setDomainLink(timeEntriesOps);

        projectsUiOps = new ProjectsUiOps();
        projectsUiOps.setRv(projectsRV);
        projectsUiOps.setProjectsListResizeListener(this);

        ProjectsAdapter projectsAdapter = new ProjectsAdapter(projectsUiOps);
        projectsUiOps.setRvCollapsedWidth(rvCollapsedWidth); /////// dagger here? or maybe just butterKnife?
        projectsUiOps.setDomainLink(issuesUiOps);

        projectsRV.setAdapter(projectsAdapter);
        RecyclerView.LayoutManager layoutManager = new StickyLinearLayoutManager();
//        SimpleLinearLayoutManager layoutManager = new SimpleLinearLayoutManager(this);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // this condition is true, but here if we want to change
        // our custom managers with the default LLM implementation
        if (layoutManager instanceof ResizeableLayoutManager) {
            // this is not downCasting
            ResizeableLayoutManager mgr = (ResizeableLayoutManager) layoutManager;
            mgr.setItemCollapsedSelectedWidth(itemCollapsedSelectedWidth);
            mgr.setMaxItemElevation(projectsAdapter.getMaxElevation() + 2);
        }
        projectsRV.setLayoutManager(layoutManager);

        // pool
        // - cannot share same pool between rvs
        // because of the different types of view holders

//        RecyclerView.RecycledViewPool sharedPool = new RecyclerView.RecycledViewPool();
//        sharedPool.setMaxRecycledViews(R.layout.resizeable_list_item, 40);
//        timeEntriesRv.setRecycledViewPool(sharedPool);
//        issuesRV.setRecycledViewPool(sharedPool);
//        projectsRV.setRecycledViewPool(sharedPool);

        // supportsPredictiveItemAnimations
    }

    /**
     * Called when the project list is getting shrinked,
     * as a follow-up to a project being selected.
     */
    private void flingBarUp() {
        // fling the app bar up
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        BehaviorFix behavior = (BehaviorFix) params.getBehavior();
        if (behavior != null) {
            behavior.onNestedFling(mainContainer, appBarLayout, null, 0, 7000, true);
//            behavior.onNestedPreScroll(mainContainer, appBarLayout, appBarLayout, 0, 1000, new int[] {0, 0});
        }
    }

    @Override
    public void registerUnbinder(Unbinder unbinder) {
        unbinderList.add(unbinder);
    }

    @Override
    public boolean deRegisterUnbinder(Unbinder unbinder) {
        return
                unbinderList != null
                        && unbinderList.size() > 0
                        && unbinderList.remove(unbinder);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: " + unbinderList.size());
        for (Unbinder u : unbinderList) {
            Log.i(TAG, "onDestroy: " + u);
            u.unbind();
        }
        unbinderList.clear();

        projectsUiOps = null;
//        projectsRV.setAdapter(null);
        projectsRV = null;
        issuesUiOps = null;
        issuesRV = null;
    }
}
