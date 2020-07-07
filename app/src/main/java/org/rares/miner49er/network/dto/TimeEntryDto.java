package org.rares.miner49er.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class TimeEntryDto {

    private String id;
    private String issueId;
    private String ownerId;
    private Integer hours;
    private Long workDate;
    private Long dateAdded;
    private String comments;
}
