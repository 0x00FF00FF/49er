package org.rares.miner49er.layoutmanager.postprocessing;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

// TODO: this seems a bit tangled up
public interface ResizePostProcessor {
    // coder philosoraptor asks if this is a code smell?
    // https://memegenerator.net/img/instances/64537262/very-interface-implement-so-pro-coder.jpg

    /**
     * A post processor is a unit that applies a set
     * of operations after a certain process has ended.
     * <br />
     * In this case, it should apply positioning rules
     * inside recycler view's items after the recycler
     * view will start changing its width. <br />
     * It was designed to be an abstraction layer that
     * leverages between the "Ops" classes that apply
     * animation to the recycler view semi-closed-drawer
     * mechanism and their child views so that the
     * recycler view children could also be animated.
     */
    interface PostProcessor { // a post processor needs to have these:
        void postProcess(RecyclerView viewGroup);

        PostProcessorValidator getPostProcessorValidator();

        void setPostProcessConsumer(PostProcessorConsumer postProcessConsumer);
    }

    /**
     * A post process consumer is called at certain points of the
     * post process lifecycle.
     */
    interface PostProcessorConsumer {
//        /**
//         * Called when post processing has started.
//         */
//        void onPostProcessStart();

        /**
         * Method called when post processing has finished.
         */
        void onPostProcessEnd();
    }

    /**
     * The class implementing this interface makes sure that the
     * view complies with the post process rules. <br />
     * Additional information: {@link #validateItemPostProcess(View, boolean, boolean)}
     */
    interface PostProcessorValidator {
        /**
         * Applies post process end/start rules to a view.
         * The implementation should decide which rules to apply using
         * the two boolean parameters.
         * This method is called when a post process validator consumer
         * (a layout manager, for example)
         * wants to know that the currently processed view is laid out
         * according to the rules.
         *
         * @param view                 target view
         * @param isViewGroupCollapsed boolean showing whether the parent
         *                             view group is collapsed or not
         * @param isViewSelected       boolean showing whether the view is the
         *                             selected one
         */
        void validateItemPostProcess(View view, boolean isViewGroupCollapsed, boolean isViewSelected);
    }

    /**
     * The class implementing this interface uses a {@link PostProcessorValidator}. <br />
     * The interface's presence is more for semantic purposes, as this interface is implemented
     * only by the custom layout manager,  because the LM doesn't hold any rules specific to
     * internal item positioning (recyclerView.holder.itemView.children).
     */
    interface PostProcessorValidatorConsumer {
        void setPostProcessorValidator(PostProcessorValidator validator);
    }
}
