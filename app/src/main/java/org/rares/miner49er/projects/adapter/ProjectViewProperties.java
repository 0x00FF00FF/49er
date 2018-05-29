package org.rares.miner49er.projects.adapter;

import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er._abstract.ItemViewProperties;

public class ProjectViewProperties extends ItemViewProperties {
    @Getter @Setter
    private String text;
    @Getter @Setter
    private int projectId;
}