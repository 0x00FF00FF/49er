package org.rares.miner49er.layoutmanager;

import android.view.View;
import lombok.Getter;

@Getter
public class ItemAnimationDto {

    private final View animatedView;
    private final int elevation;
    private final int width;
    private final boolean selected;
    private final boolean previouslySelected;

    public static class Builder {
        private View animatedView;
        private int elevation = 0;
        private int width = -1;
        private boolean selected = false;
        private boolean previouslySelected = false;

        public Builder(View animatedView) {
            this.animatedView = animatedView;
        }

        public Builder animatedView(View animatedView) {
            this.animatedView = animatedView;
            return this;
        }

        public Builder elevation(int elevation) {
            this.elevation = elevation;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder selected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public Builder previouslySelected(boolean selected) {
            this.previouslySelected = selected;
            return this;
        }

        public ItemAnimationDto build() {
            return new ItemAnimationDto(this);
        }
    }

    private ItemAnimationDto(Builder builder) {
        this.animatedView = builder.animatedView;
        this.width = builder.width;
        this.elevation = builder.elevation;
        this.selected = builder.selected;
        this.previouslySelected = builder.previouslySelected;
    }

}
