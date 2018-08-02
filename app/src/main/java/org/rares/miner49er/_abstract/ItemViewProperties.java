package org.rares.miner49er._abstract;

import lombok.Getter;
import lombok.Setter;

/**
 * @author rares
 * @since 02.03.2018
 */

// this is in fact a DTO used to transfer
// properties from parent to child domains
// TODO: 7/9/18 rename and clean up child classes that are not used anymore
public abstract class ItemViewProperties {
    @Getter @Setter
    private int itemBgColor;
}
