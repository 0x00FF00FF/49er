package org.rares.miner49er.ui.custom.spannable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import org.rares.miner49er.util.UiUtil;

import java.lang.ref.WeakReference;

public class CenteredImageSpan extends ImageSpan {
    private WeakReference<Drawable> mDrawableRef;

    public static final String TAG = CenteredImageSpan.class.getSimpleName();

    private Context ctx;
    private int drawableResource;

    public CenteredImageSpan(Context context, final int drawableRes) {
        super(context, drawableRes);
        ctx = context;
        drawableResource = drawableRes;
//        Log.d(TAG, "CenteredImageSpan: " + hashCode() + " " + drawableRes);
    }

    @Override
    public int getSize(Paint paint, CharSequence text,
                       int start, int end,
                       Paint.FontMetricsInt fm) {
        final int iconDimensions = (int) UiUtil.pxFromDp(ctx, 24);
        Drawable d = getCachedDrawable();
        d.setBounds(0, 0, iconDimensions, iconDimensions);
        Rect rect = d.getBounds();

        if (fm != null) {
            Paint.FontMetricsInt pfm = paint.getFontMetricsInt();
            // keep it the same as paint's fm
            fm.ascent = pfm.ascent;
            fm.descent = pfm.descent;
            fm.top = pfm.top;
            fm.bottom = pfm.bottom;
        }

        return rect.right;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text,
                     int start, int end, float x,
                     int top, int y, int bottom, @NonNull Paint paint) {
        Drawable b = getCachedDrawable();
        canvas.save();

        int drawableHeight = b.getIntrinsicHeight();
        int fontAscent = paint.getFontMetricsInt().ascent;
        int fontDescent = paint.getFontMetricsInt().descent;
        int transY = bottom - b.getBounds().bottom +  // align bottom to bottom
                (drawableHeight - fontDescent + fontAscent) / 2;  // align center to center

        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }

    // Redefined locally because it is a private member from DynamicDrawableSpan
    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;

        if (wr != null)
            d = wr.get();

        if (d == null) {
            d = getDrawable();
            if (d == null) {
                d = AppCompatResources.getDrawable(ctx, drawableResource);
            }
            mDrawableRef = new WeakReference<>(d);
        }

        return d;
    }
}