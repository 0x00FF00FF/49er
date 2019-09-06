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
public class ProjectDto {

  private String id;

  private UserDto owner;
  private String name;
  private String description;
  private String icon;
  private Boolean archived; // todo primitive?
  private Long dateAdded;

  private List<UserDto> team;
  private List<String> issues;
}

