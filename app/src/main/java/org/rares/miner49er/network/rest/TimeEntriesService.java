package org.rares.miner49er.network.rest;

import io.reactivex.Flowable;
import io.reactivex.Single;
import org.rares.miner49er.network.dto.TimeEntryDto;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface TimeEntriesService {


        @Headers({"accept: application/stream+json"})
        @GET("issues/{issueId}/timeentries")
        Flowable<TimeEntryDto> getTimeEntriesForIssue(@Path("issueId") String issueId);

        @GET("issues/{issueId}/timeentries")
        Single<List<TimeEntryDto>> getTimeEntriesForIssueAsSingleList(@Path("issueId") String issueId);

        @HEAD("issues/{issueId}/timeentries")
        Flowable<List<TimeEntryDto>> getNewTimeEntries(
                @Path("issueId") String issueId,
                @Query("since") long sinceDate
        );

        @GET("timeentries/{entryId}")
        Single<TimeEntryDto> getTimeEntry(@Path("entryId") String timeEntryId);

        @POST("timeentries")
        Single<TimeEntryDto> postTimeEntry(TimeEntryDto timeEntry);

        @PUT("timeentries/{entryId}")
        Single<Void> putTimeEntry(@Path("entryId") String entryId, TimeEntryDto timeEntry);

        @PATCH("timeentries/{entryId}")
        Single<Void> patchTimeEntry(@Path("entryId") String entryId, TimeEntryDto timeEntry);

        @DELETE("timeentries/{entryId}")
        Single<Void> deleteTimeEntry(@Path("entryId") String entryId);
    }