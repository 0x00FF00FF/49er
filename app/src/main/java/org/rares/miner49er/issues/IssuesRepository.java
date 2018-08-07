package org.rares.miner49er.issues;

import android.util.Log;
import io.reactivex.functions.Consumer;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er.issues.model.IssueData;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class IssuesRepository extends Repository {

    public static final String TAG = IssuesRepository.class.getSimpleName();

    private List<Issue> initializeFakeData() {
        List<Issue> dataList = new ArrayList<>();
        for (int i = 0; i < NumberUtils.getRandomInt(5, 30); i++) {
            Issue data = new Issue();
            data.setId(NumberUtils.getNextProjectId());
            data.setName("Issue #" + i);
            dataList.add(data);
        }
        return dataList;
    }

    public void persistIssues(List<Issue> list) {
        storio
                .put()
                .objects(list)
                .prepare()
                .asRxCompletable()
                .subscribe();
    }

    private List<IssueData> db2vm(List<Issue> issues) {
        Log.d(TAG, "db2vm() called with: p = [" + issues + "]");
        List<IssueData> projectDataList = new ArrayList<>();

        for (Issue i : issues) {
            IssueData converted = new IssueData();

            converted.setDateAdded(i.getDateAdded());
            converted.setDateDue(i.getDateDue());
            converted.setId(i.getId());
            converted.setName(i.getName());
            converted.setProjectId(i.getProjectId());
            projectDataList.add(converted);
        }

        return projectDataList;
    }

    @Override
    public void setup() {

    }

    @Override
    public void registerSubscriber(Consumer<List> consumer) {
        try {
            consumer.accept(db2vm(initializeFakeData()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {

    }
}
