package org.rares.miner49er.layoutmanager.postprocessing.resize;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er.layoutmanager.postprocessing.ResizePostProcessor;

public class ResizeItemPostProcessor implements ResizePostProcessor.PostProcessor {

    private ResizePostProcessor.PostProcessorConsumer consumer = null;

    @Override
    public void postProcess(RecyclerView viewGroup) {
        AbstractAdapter _adapter = (AbstractAdapter) viewGroup.getAdapter();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            ViewGroup group = (ViewGroup) viewGroup.getChildAt(i);
            TextView textView = (TextView) group.getChildAt(0);
            String text = _adapter.resolveData(viewGroup.getChildAdapterPosition(group));
            textView.setText(text);
        }
        if (consumer != null) {
            consumer.onPostProcessEnd();
        }
    }

    @Override
    public ResizePostProcessor.PostProcessorValidator getPostProcessorValidator() {
        return null;
    }

    @Override
    public void setPostProcessConsumer(ResizePostProcessor.PostProcessorConsumer postProcessConsumer) {
        consumer = postProcessConsumer;
    }

}
