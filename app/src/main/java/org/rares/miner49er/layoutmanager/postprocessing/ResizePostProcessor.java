package org.rares.miner49er.layoutmanager.postprocessing;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public interface ResizePostProcessor {
    // coder philosoraptor asks if this is a code smell?
    // https://memegenerator.net/img/instances/64537262/very-interface-implement-so-pro-coder.jpg

    interface PostProcessor {
        void postProcess(RecyclerView viewGroup);
        PostProcessorValidator getPostProcessorValidator();
        void setPostProcessConsumer(PostProcessorConsumer postProcessConsumer);
    }

    interface PostProcessorConsumer {
        void onPostProcessEnd();
    }

    interface PostProcessorValidator {
        void validateItemPostProcess(View view, boolean isViewGroupCollapsed, boolean isViewSelected);
    }

    interface PostProcessorValidatorConsumer {
        void setPostProcessorValidator(PostProcessorValidator validator);
    }
}
