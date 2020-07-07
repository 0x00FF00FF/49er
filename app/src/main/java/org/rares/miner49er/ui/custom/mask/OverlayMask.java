package org.rares.miner49er.ui.custom.mask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import org.rares.miner49er.R;

public class OverlayMask extends View {

    public OverlayMask(Context context) {
        super(context);
        init();
    }

    public OverlayMask(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayMask(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    Drawable mask = null;

    void init() {
        mask = getContext().getResources().getDrawable(R.drawable.drawable_mask_rv_overlay);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mask.setBounds(0, 0, width, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mask.draw(canvas);
        canvas.drawColor(0xFF_232533, PorterDuff.Mode.XOR);
    }
}
