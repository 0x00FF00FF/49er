package org.rares.miner49er._abstract;

import lombok.Data;

/**
 * @author rares
 * @since 02.03.2018
 */

// this is in fact a DTO used to transfer
// properties from parent to child domains
// TODO: 7/9/18 rename and clean up child classes that are not used anymore
@Data
public abstract class ItemViewProperties {

    private int itemBgColor;
    private int id;
}
