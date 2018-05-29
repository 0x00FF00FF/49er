package org.rares.miner49er.layoutmanager;

import android.view.View;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ItemAnimationDto {
    private View animatedView;
    private int elevation;
    private int width;
}
