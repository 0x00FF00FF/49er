package org.rares.miner49er.domain.issues;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import lombok.Setter;
import org.rares.miner49er.R;

public class AccDecoration extends RecyclerView.ItemDecoration {
    public static final String TAG = AccDecoration.class.getSimpleName();

    @Setter
    private boolean drawBranch = true;

    @Setter
    private int selectedPosition = -1;
    private int mainColor = Color.WHITE;
    private int selectedColor = Color.WHITE;
    private int secondaryColor = Color.TRANSPARENT;

    private int marginLeft = 0;

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (view == null || parent.getChildCount() == 0 || parent.getAdapter() == null) {
            return;
        }
        int strokeWidth = parent.getResources().getDimensionPixelOffset(R.dimen.projects_list_item_background_stroke_width);
//        marginLeft = parent.getResources().getDimensionPixelOffset(R.dimen.sv_margin) * 2;
        boolean first = parent.getChildAdapterPosition(view) == 0;
        outRect.set(strokeWidth + marginLeft, first ? 0 : strokeWidth / 2, 0, strokeWidth / 2);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (!drawBranch || parent.getChildCount() == 0 || parent.getAdapter() == null) {
            return;
        }

        int cornerRadius = parent.getResources().getDimensionPixelOffset(R.dimen._list_item_corner_radius_small);
        Paint p = new Paint();
        int strokeWidth = parent.getResources().getDimensionPixelOffset(R.dimen.projects_list_item_background_stroke_width);
        View lastChild = parent.getChildAt(parent.getChildCount() - 1);
        RecyclerView.ViewHolder vh = parent.findContainingViewHolder(lastChild);
        boolean lastChildShowing = false;

//        selectedColor = parent.getResources().getColor(R.color.indigo_100);
        p.setColor(selectedPosition != -1 ? selectedColor : mainColor);
        p.setStrokeWidth(strokeWidth);

        if (vh != null) {
            lastChildShowing = vh.getAdapterPosition() == parent.getAdapter().getItemCount() - 1;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            p.setShader(null);
            View v = parent.getChildAt(i);
            float y1 = v.getY();
            float y2 = y1 + v.getHeight() - cornerRadius + strokeWidth; // TODO ** optimize cornerRadius **

            // draw child vertical line
            // draw from x1=strokeWidth/2 + margin offset
            // y1 is the top of the child view
            // y2 is y1 + the child view height
            // - (minus)
            // the corner radius [the last child is
            // supposed to end the "branch"], the
            // other children draw all the way along
            // the view's left border
            // + (plus)
            // strokeWidth [to compensate the child
            // margin/offset]
            // this value for y2 is further compensated
            // for the last child, by removing the stroke
            // width and for other children by adding
            // the corner radius back
            c.drawLine(
                    marginLeft + strokeWidth / 2,
                    y1,
                    marginLeft + strokeWidth / 2,
                    (i == parent.getChildCount() - 1 && lastChildShowing ? y2 - strokeWidth : y2 + cornerRadius), p);

            // draw child rounded corner
            // using an algorithm to draw a "round cornered path"
            // [from https://stackoverflow.com/a/37446243]
            // not sure if there are other, better ones
            //
            // this algorithm used to draw the rounded corners
            // is probably not the one used by android:
            // in some cases there are inconsistencies with
            // drawing a curved line after/under another;
            // in this case we draw the curved line under
            // the item background so it would seem they
            // are correct even though they overlap a bit
            //
            // start from x1=strokeWidth (plus marginLeft offset),  // !important: for no background, there is no
            // if not last item. x1=strokeWidth/2 (plus marginLeft  // difference between last and non-last items
            // offset) if current item is the last                  // so that is commented out
            // ^ this is because for the last item, the line
            // only goes to the right, under the item, which
            // is not the case for the other items, where
            // the line splits to the right and downwards
            // y1=y2[the place where the vertical border ends]
            // minus half the radius
            // x2=the radius + two times the stroke width [to
            // compensate the left and right item offset/border]
            // (plus marginLeft offset)
            // y2[for the curved line]=y2[for the vertical line]
            // plus the corner radius minus the stroke width
            // (compensation for margins)
            int x2 = (2 * strokeWidth + cornerRadius);
            int y2cl = (int) (y2 + cornerRadius - strokeWidth);
            drawCurvedLine(c,
                    marginLeft + strokeWidth / 2/*(i == parent.getChildCount() - 1 && lastChildShowing ? 2 : 1)*/,
                    (int) y2 - cornerRadius / 2,
                    marginLeft + x2, y2cl, -cornerRadius, p);

            // draw child horizontal line
            // starts from the end point of the curved line
            // (x minus stroke width compensation plus marginLeft
            // offset, y) until the end of the item [item view
            // width minus corner radius and stroke width].
            // uses a gradient
            LinearGradient gradient = new LinearGradient(
                    0, 0, parent.getWidth() + cornerRadius, 0,
                    new int[]{selectedPosition == -1 ? mainColor : selectedColor, secondaryColor}, new float[]{0.3F, 0.9F},
                    Shader.TileMode.CLAMP);
            p.setShader(gradient);
            c.drawLine(
                    marginLeft + x2 - strokeWidth / 2, y2cl,
                    marginLeft + x2 + v.getWidth() - cornerRadius - strokeWidth, y2cl, p);

        }
    }

    private void drawCurvedLine(Canvas canvas,
                                int x1, int y1, int x2, int y2,
                                int curveRadius,
                                Paint paint) {

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        final Path path = new Path();
        int midX = x1 + ((x2 - x1) / 2);
        int midY = y1 + ((y2 - y1) / 2);
        float xDiff = midX - x1;
        float yDiff = midY - y1;
        double angle = (Math.atan2(yDiff, xDiff) * (180 / Math.PI)) - 90;
        double angleRadians = Math.toRadians(angle);
        float pointX = (float) (midX + curveRadius * Math.cos(angleRadians));
        float pointY = (float) (midY + curveRadius * Math.sin(angleRadians));

        path.moveTo(x1, y1);
        path.cubicTo(x1, y1, pointX, pointY, x2, y2);
        canvas.drawPath(path, paint);
    }
}
