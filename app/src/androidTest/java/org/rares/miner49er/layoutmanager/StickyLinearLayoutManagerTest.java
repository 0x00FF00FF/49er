package org.rares.miner49er.layoutmanager;

import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.HomeScrollingActivity;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.domain.projects.adapter.ProjectsAdapter;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.network.NetworkingService;

import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertNotNull;
import static org.rares.miner49er.testutils.Matchers.childAtPosition;
import static org.rares.miner49er.testutils.Matchers.withElevation;
import static org.rares.miner49er.testutils.Matchers.withHashCode;

@RunWith(AndroidJUnit4.class)
public class StickyLinearLayoutManagerTest {

    private List<ProjectData> projectList;

    @BeforeClass
    public static void beforeClass() {
        getInstrumentation().getTargetContext().deleteDatabase(BaseInterfaces.DB_NAME);
    }

    @Rule
    public ActivityTestRule<HomeScrollingActivity> activityTestRule = new ActivityTestRule<>(HomeScrollingActivity.class);


    @Before
    public void setUp() throws Exception {
        // need better way to do this.

        UserData user = new UserData();
        user.setId(1L);
        user.setName("T-850 Model 101");

        projectList = new ArrayList<>();

        for (long i = 0; i < 20; i++) {
            ProjectData project = new ProjectData();
            project.setId(i + 1);
            project.setName("project #" + (i + 1));
            project.setDescription("description");
            project.setPicture("");
            project.setOwner(user);
            projectList.add(project);
        }

        ViewModelCacheSingleton.getInstance().loggedInUser = user;
        NetworkingService.INSTANCE.end();
    }

    /*
     *  Given   a recycler view with 20 elements, with a StickyLinearLayoutManager
     *  When    one of the items is selected
     *  Then    the StickyLinearLayoutManager will layout the selected item
     *          with higher elevation than the rest of the items
     */
    @Test
    public void testSelectItem() {
        // given [setup]
        // when
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(HomeScrollingActivity.class.getPackage().getName(), HomeScrollingActivity.class.getName()));
        activityTestRule.launchActivity(intent);

        RecyclerView rv = activityTestRule.getActivity().findViewById(R.id.rv_projects_list);
        StickyLinearLayoutManager lm = (StickyLinearLayoutManager) rv.getLayoutManager();
        ProjectsAdapter adapter = (ProjectsAdapter) rv.getAdapter();

        getInstrumentation().waitForIdleSync();
        activityTestRule.getActivity().runOnUiThread(() -> adapter.accept(projectList));

        ViewInteraction rvI = onView(withId(R.id.rv_projects_list));
        rvI.perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Matcher<View> child = childAtPosition(withId(R.id.rv_projects_list), 0);
        onView(child).check(matches(withElevation(child, BaseInterfaces.MAX_ELEVATION_PROJECTS + 2)));  // +2 from HSA/setupResizeableManager
        Matcher<View> secondChild = childAtPosition(withId(R.id.rv_projects_list), 1);
        onView(secondChild).check(matches(withElevation(secondChild, 0)));
    }

    /*
     *  Given   a rv with 20 elements, with a SLLM with first item selected
     *  When    the list scrolls towards the bottom (new items appear at the bottom)
     *  Then    the selected item remains in the viewport
     */
    @Test
    public void testSelectItem_scrollBottom() throws InterruptedException {
        // given [setup]
        // when
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(HomeScrollingActivity.class.getPackage().getName(), HomeScrollingActivity.class.getName()));
        activityTestRule.launchActivity(intent);

        RecyclerView rv = activityTestRule.getActivity().findViewById(R.id.rv_projects_list);
        StickyLinearLayoutManager lm = (StickyLinearLayoutManager) rv.getLayoutManager();
        ProjectsAdapter adapter = (ProjectsAdapter) rv.getAdapter();
        assertNotNull(adapter);

        getInstrumentation().waitForIdleSync();
        activityTestRule.getActivity().runOnUiThread(() -> adapter.accept(projectList));

        Thread.sleep(100);

        View selected = rv.getChildAt(0);

        ViewInteraction rvI = onView(withId(R.id.rv_projects_list));

        onView(childAtPosition(withId(R.id.rv_projects_list), 0)).perform(click());

        Thread.sleep(100);

        rvI.perform(swipeUp());

        Thread.sleep(100);

        onView(withHashCode(selected.hashCode())).check(matches(isCompletelyDisplayed()));
    }

    /*
     *  Given   a rv with 20 elements, with a SLLM with last item selected
     *  When    the list scrolls towards the top (new items appear at the top)
     *  Then    the selected item remains in the viewport
     */
    @Test
    public void testSelectItem_scrollTop() {
        // given [setup]
        // when
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(HomeScrollingActivity.class.getPackage().getName(), HomeScrollingActivity.class.getName()));
        activityTestRule.launchActivity(intent);

        RecyclerView rv = activityTestRule.getActivity().findViewById(R.id.rv_projects_list);
        StickyLinearLayoutManager lm = (StickyLinearLayoutManager) rv.getLayoutManager();
        ProjectsAdapter adapter = (ProjectsAdapter) rv.getAdapter();
        assertNotNull(adapter);

        getInstrumentation().waitForIdleSync();
        activityTestRule.getActivity().runOnUiThread(() -> adapter.accept(projectList));

        ViewInteraction rvI = onView(withId(R.id.rv_projects_list));
        rvI.perform(swipeUp());
        final int position = rv.getChildCount() - 1;
        View selected = rv.getChildAt(position);

        onView(childAtPosition(withId(R.id.rv_projects_list), position)).perform(click());
        rvI.perform(swipeDown());
        onView(withHashCode(selected.hashCode())).check(matches(isCompletelyDisplayed()));
    }
}