package org.rares.miner49er.layoutmanager.postprocessing.resize;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er.layoutmanager.postprocessing.ResizePostProcessor;
import org.rares.ratv.rotationaware.RotationAwareTextView;

/**
 * Simple post processor that updates an itemView's text.
 */
public class ResizeItemPostProcessor implements ResizePostProcessor.PostProcessor {

    private static final String TAG = ResizeItemPostProcessor.class.getSimpleName();

    private ResizePostProcessor.PostProcessorConsumer consumer = null;

    @Override
    public void postProcess(RecyclerView recyclerView) {
        AbstractAdapter _adapter = (AbstractAdapter) recyclerView.getAdapter();
        int ic = _adapter.getItemCount();
        int cc = recyclerView.getChildCount();
        if (_adapter.getItemCount() < recyclerView.getChildCount()) {
            Log.w(TAG, "postProcess: RV is in animation state, " +
                    "different item count: adapter: " + ic + " views: " + cc);
        } else {
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                ViewGroup listItem = (ViewGroup) recyclerView.getChildAt(i);
                View childView = listItem.getChildAt(0);
                String text = _adapter.resolveData(recyclerView.getChildAdapterPosition(listItem), false);
                if (childView instanceof TextView) {
                    TextView textView = (TextView) childView;
                    if (text != null) {
                        textView.setText(text);
                    }
                }
                if (childView instanceof ViewGroup) {
                    View v = ((ViewGroup) childView).getChildAt(0);
                    if (v instanceof RotationAwareTextView) {
                        RotationAwareTextView textView = (RotationAwareTextView) v;
                        if (text != null) {
                            textView.setText(text);
                        }
                    }
                }
            }
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
