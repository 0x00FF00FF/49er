package org.rares.miner49er.persistence.dao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractViewModel {
    public Long id;
    public long lastUpdated;
    public Long parentId;
}
