package org.rares.miner49er.network.rest;

import io.reactivex.Flowable;
import io.reactivex.Single;
import org.rares.miner49er.network.dto.IssueDto;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.List;

public interface IssuesService {

    @Headers({"accept: application/stream+json"})
    @GET("projects/{projectId}/issues")
    Flowable<IssueDto> getIssuesForProjectAsFlowable(@Path("projectId") String projectId);


    @GET("projects/{projectId}/issues")
    Single<List<IssueDto>> getIssuesForProjectAsSingleList(@Path("projectId") String projectId);

//    @GET("issues.json")
//    Flowable<Issue> getIssueList();

    @GET("issues/{issueId}")
    Single<IssueDto> getIssue(@Path("issueId") String issueId);

    @POST("issues")
    Single<IssueDto> postIssue(IssueDto issue);

    @PUT("/issue/{issueId}")
    Single<Void> putIssue(@Path("issueId") String issueId, IssueDto issue);

    @PATCH
    Single<Void> patchIssue(@Path("issueId") String issueId, IssueDto issue);

    @DELETE
    Single<Void> deleteIssue(@Path("issueID") String issueId);
}