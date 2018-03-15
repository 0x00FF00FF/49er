package org.rares.miner49er._abstract;

import lombok.Getter;
import lombok.Setter;

/**
 * @author rares
 * @since 02.03.2018
 */

public abstract class ItemViewProperties {
    @Getter @Setter
    boolean selected = false;
    @Getter @Setter
    int itemContainerCustomId = -1;
    @Getter @Setter
    String data;
    @Getter @Setter
    private int itemBgColor;
}
