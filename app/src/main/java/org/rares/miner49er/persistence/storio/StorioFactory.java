package org.rares.miner49er.persistence.storio;

import android.content.Context;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite;
import lombok.Getter;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.mappings.IssueSQLiteTypeMapping;
import org.rares.miner49er.persistence.storio.mappings.ProjectSQLiteTypeMapping;
import org.rares.miner49er.persistence.storio.mappings.TimeEntrySQLiteTypeMapping;
import org.rares.miner49er.persistence.storio.mappings.UserSQLiteTypeMapping;
import org.rares.miner49er.persistence.storio.resolvers.IssueStorIOSQLiteDeleteResolver;
import org.rares.miner49er.persistence.storio.resolvers.IssueStorIOSQLiteGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.IssueStorIOSQLitePutResolver;
import org.rares.miner49er.persistence.storio.resolvers.LazyIssueGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.LazyProjectGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.LazyTimeEntryGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.ProjectStorIOSQLiteDeleteResolver;
import org.rares.miner49er.persistence.storio.resolvers.ProjectStorIOSQLiteGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.ProjectStorIOSQLitePutResolver;
import org.rares.miner49er.persistence.storio.resolvers.ProjectTeamDeleteResolver;
import org.rares.miner49er.persistence.storio.resolvers.ProjectTeamGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.ProjectTeamPutResolver;
import org.rares.miner49er.persistence.storio.resolvers.TimeEntryStorIOSQLiteDeleteResolver;
import org.rares.miner49er.persistence.storio.resolvers.TimeEntryStorIOSQLiteGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.TimeEntryStorIOSQLitePutResolver;
import org.rares.miner49er.persistence.storio.resolvers.UserProjectDeleteResolver;
import org.rares.miner49er.persistence.storio.resolvers.UserProjectPutResolver;
import org.rares.miner49er.persistence.storio.resolvers.UserStorIOSQLiteDeleteResolver;
import org.rares.miner49er.persistence.storio.resolvers.UserStorIOSQLiteGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.UserStorIOSQLitePutResolver;

public enum StorioFactory {

    INSTANCE;

    public StorIOSQLite get() {
        return storio;
    }

    public StorIOSQLite get(Context c) {
        if (storio == null) {
            setup(c);
        }
        return storio;
    }

    public void setup(Context c) {
        storio = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new StorioDbHelper(c))

                .addTypeMapping(User.class, getUserSQLiteTypeMapping())
                .addTypeMapping(TimeEntry.class, getTimeEntrySQLiteTypeMapping())
                .addTypeMapping(Project.class, getProjectSQLiteTypeMapping())
                .addTypeMapping(Issue.class, getIssueSQLiteTypeMapping())

//                .addInterceptor(LoggingInterceptor.defaultLogger())
                .build();
    }

    private StorIOSQLite storio = null;

    // static because if not, they are not initialized fast enough. todo: use lazy initialization
    @Getter
    private static ProjectSQLiteTypeMapping projectSQLiteTypeMapping = new ProjectSQLiteTypeMapping();
    @Getter
    private static TimeEntrySQLiteTypeMapping timeEntrySQLiteTypeMapping = new TimeEntrySQLiteTypeMapping();
    @Getter
    private static UserSQLiteTypeMapping userSQLiteTypeMapping = new UserSQLiteTypeMapping();
    @Getter
    private static IssueSQLiteTypeMapping issueSQLiteTypeMapping = new IssueSQLiteTypeMapping();

    @Getter
    private IssueStorIOSQLiteDeleteResolver issueStorIOSQLiteDeleteResolver = new IssueStorIOSQLiteDeleteResolver();
    @Getter
    private IssueStorIOSQLiteGetResolver issueStorIOSQLiteGetResolver = new IssueStorIOSQLiteGetResolver();
    @Getter
    private IssueStorIOSQLitePutResolver issueStorIOSQLitePutResolver = new IssueStorIOSQLitePutResolver();
    @Getter
    private LazyIssueGetResolver lazyIssueGetResolver = new LazyIssueGetResolver();
    @Getter
    private LazyProjectGetResolver lazyProjectGetResolver = new LazyProjectGetResolver();
    @Getter
    private LazyTimeEntryGetResolver lazyTimeEntryGetResolver = new LazyTimeEntryGetResolver();
    @Getter
    private ProjectStorIOSQLiteDeleteResolver projectStorIOSQLiteDeleteResolver = new ProjectStorIOSQLiteDeleteResolver();
    @Getter
    private ProjectStorIOSQLiteGetResolver projectStorIOSQLiteGetResolver = new ProjectStorIOSQLiteGetResolver();
    @Getter
    private ProjectStorIOSQLitePutResolver projectStorIOSQLitePutResolver = new ProjectStorIOSQLitePutResolver();
    @Getter
    private ProjectTeamDeleteResolver projectTeamDeleteResolver = new ProjectTeamDeleteResolver();
    @Getter
    private ProjectTeamGetResolver projectTeamGetResolver = new ProjectTeamGetResolver();
    @Getter
    private TimeEntryStorIOSQLiteDeleteResolver timeEntryStorIOSQLiteDeleteResolver = new TimeEntryStorIOSQLiteDeleteResolver();
    @Getter
    private TimeEntryStorIOSQLiteGetResolver timeEntryStorIOSQLiteGetResolver = new TimeEntryStorIOSQLiteGetResolver();
    @Getter
    private TimeEntryStorIOSQLitePutResolver timeEntryStorIOSQLitePutResolver = new TimeEntryStorIOSQLitePutResolver();
    @Getter
    private UserProjectDeleteResolver userProjectDeleteResolver = new UserProjectDeleteResolver();
    @Getter
    private UserProjectPutResolver userProjectPutResolver = new UserProjectPutResolver();
    @Getter
    private ProjectTeamPutResolver projectTeamPutResolver = new ProjectTeamPutResolver(userProjectPutResolver);
    @Getter
    private UserStorIOSQLiteDeleteResolver userStorIOSQLiteDeleteResolver = new UserStorIOSQLiteDeleteResolver();
    @Getter
    private UserStorIOSQLiteGetResolver userStorIOSQLiteGetResolver = new UserStorIOSQLiteGetResolver();
    @Getter
    private UserStorIOSQLitePutResolver userStorIOSQLitePutResolver = new UserStorIOSQLitePutResolver();

}
