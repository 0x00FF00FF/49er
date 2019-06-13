package org.rares.miner49er.fakes;

import android.view.View;
import org.rares.miner49er.BaseInterfaces.ActionFragmentDependencyProvider;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.actionmode.ActionEnforcer.FragmentResultListener;

public class ActionFragmentDependencyProviderFake implements ActionFragmentDependencyProvider {
        public AsyncGenericDaoFake<ProjectData> fakePdao = new AsyncGenericDaoFake<>();
        public AsyncGenericDaoFake<IssueData> fakeIdao = new AsyncGenericDaoFake<>();
        public AsyncGenericDaoFake<TimeEntryData> fakeTEdao = new AsyncGenericDaoFake<>();
        public AsyncGenericDaoFake<UserData> fakeUdao = new AsyncGenericDaoFake<>();
        public ViewModelCache cache = ViewModelCacheSingleton.getInstance();

        public View replacedView;

        @Override
        public ViewModelCache getCache() {
            return cache;
        }

        @Override
        public Cache<ProjectData> getProjectDataCache() {
            return cache.getCache(ProjectData.class);
        }

        @Override
        public Cache<IssueData> getIssueDataCache() {
            return cache.getCache(IssueData.class);
        }

        @Override
        public Cache<TimeEntryData> getTimeEntryDataCache() {
            return cache.getCache(TimeEntryData.class);
        }

        @Override
        public Cache<UserData> getUserDataCache() {
            return cache.getCache(UserData.class);
        }

        @Override
        public AsyncGenericDao<ProjectData> getProjectsDAO() {
            return fakePdao;
        }

        @Override
        public AsyncGenericDao<IssueData> getIssuesDAO() {
            return fakeIdao;
        }

        @Override
        public AsyncGenericDao<TimeEntryData> getTimeEntriesDAO() {
            return fakeTEdao;
        }

        @Override
        public AsyncGenericDao<UserData> getUsersDAO() {
            return fakeUdao;
        }

        @Override
        public View getReplacedView() {
            // cannot find Robolectric
//            HomeScrollingActivity activity = Robolectric.buildActivity(HomeScrollingActivity.class).create().get();
//            View toReturn = activity.findViewById(R.id.scroll_views_container);
            return replacedView;
        }

        @Override
        public FragmentResultListener getResultListener() {
            return new FragmentResultListener() {
                @Override
                public void onFragmentDismiss() {
                    System.out.println("Fragment dismiss called.");
                }
            };
        }
    }