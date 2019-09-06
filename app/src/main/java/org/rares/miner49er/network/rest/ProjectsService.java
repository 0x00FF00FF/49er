package org.rares.miner49er.network.rest;

import io.reactivex.Flowable;
import io.reactivex.Single;
import org.rares.miner49er.network.dto.IssueDto;
import org.rares.miner49er.network.dto.ProjectDto;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

import java.util.List;

public interface ProjectsService {

    @GET("users/{userId}/projects")
    Flowable<ProjectDto> getUserProjectList(@Path("userId") int userId);

    //        @GET("projects_repaired.json")
    @Streaming
    @Headers({"accept: application/stream+json"})
    @GET("projects?skip=0&take=2")
    Flowable<ProjectDto> getProjectsAsFlowable();

    @GET("projects")
    Single<List<ProjectDto>> getProjectsAsSingleList();

    @GET("projects/archived")
    Flowable<ProjectDto> getArchivedProjects();

    @GET("projects/{projectId}")
    Single<ProjectDto> getProjectById(@Path("projectId") String projectId);

    @GET("projects/{projectId}/issues")
    Flowable<IssueDto> getProjectIssues(@Path("projectId") String projectId);

    @POST("projects")
    Single<ProjectDto> postProject(ProjectDto project);

    @PUT("projects/{projectId}")
    Single<Void> putProject(@Path("projectId") String projectId);

    @PATCH("projects/{projectId}")
    Single<Void> patchProject(@Path("projectId") String projectId);

    @DELETE("projects/{projectId}")
    Single<Void> deleteProject(@Path("projectId") String projectId);
}