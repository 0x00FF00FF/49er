package org.rares.miner49er.projects.adapter;

import org.rares.miner49er._abstract.ItemViewProperties;

import lombok.Getter;
import lombok.Setter;

public class ProjectViewProperties extends ItemViewProperties {
    @Getter @Setter
    private String text;
    @Getter @Setter
    private int projectId;
}