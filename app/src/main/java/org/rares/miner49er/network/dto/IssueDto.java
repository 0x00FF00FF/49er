package org.rares.miner49er.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueDto {

  private String id;
  private String projectId;
  private String ownerId;
  private Long dateAdded;
  private Long sprint;
  private Boolean closed;
  private String name;

  private List<String> timeEntries;
}

