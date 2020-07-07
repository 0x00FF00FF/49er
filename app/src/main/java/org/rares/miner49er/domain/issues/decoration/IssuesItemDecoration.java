package org.rares.miner49er.domain.issues.decoration;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;

public class IssuesItemDecoration extends RecyclerView.ItemDecoration {


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        Resources res = parent.getContext().getResources();
        int colorStart = res.getColor(R.color.indigo_100_blacked_transparent),
                colorCenter = res.getColor(R.color.indigo_100_grayed);

        AbstractAdapter adapter = (AbstractAdapter) parent.getAdapter();
        for (int i = 0; i < parent.getChildCount(); i++) {

            View v = parent.getChildAt(i);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            RecyclerView.ViewHolder vh = parent.findContainingViewHolder(v);
            if (vh == null) {
                continue;
            }

            int selectedPosition = adapter.getLastSelectedPosition();

            boolean parentCollapsed = parent.getLayoutParams().width != -1;

            float x1 = parentCollapsed ? parent.getWidth() / 2 : parent.getWidth() / 5;
            float x2 = parentCollapsed ? parent.getWidth() : 4 * parent.getWidth() / 5;
            float y1 = v.getY();
            float y2 = y1 + v.getHeight();

            final int strokeWidth = parent.getResources().getDimensionPixelOffset(R.dimen.projects_list_item_background_stroke_width);
            int localStrokeWidth = strokeWidth;
            int cornerWidth = parent.getResources().getDimensionPixelOffset(R.dimen._list_item_corner_radius_small);

            if (parentCollapsed) {
                localStrokeWidth /= 2;
            }

            Paint p = new Paint();

            int[] expandedColors = new int[]{colorStart, colorCenter, colorStart};
            int[] collapsedColors = new int[]{colorStart, colorCenter};

            LinearGradient gradient = new LinearGradient(
                    x1, 0, x2, 0,
                    parentCollapsed ? collapsedColors : expandedColors,
                    null,
                    Shader.TileMode.CLAMP);
            p.setShader(gradient);

            p.setColor(colorCenter);
            p.setStrokeWidth(localStrokeWidth);

            int vhPos = vh.getAdapterPosition();
            if (vhPos != selectedPosition && vhPos != selectedPosition - 1) {

                if (i != parent.getChildCount() - 1) {
                    c.drawLine(x1, y2, x2, y2, p);
                }

                if (!parentCollapsed) {
                    localStrokeWidth /= 2;
                    p.setStrokeWidth(localStrokeWidth);

                    int additionalSpace = 18;

                    int bottomX = parentCollapsed ? parent.getWidth() / 2 - additionalSpace : lp.leftMargin;
                    int bottomY = (int) (v.getY()) + v.getHeight() - lp.bottomMargin - additionalSpace;

                    int horizontalBottomY0 = bottomY;
                    int horizontalBottomY1 = bottomY;
                    int verticalBottomY0 = horizontalBottomY0 - localStrokeWidth / 2;
                    int verticalBottomY1 = horizontalBottomY0 - cornerWidth;

                    int horizontalBottomX0 = bottomX - localStrokeWidth / 2;
                    int horizontalBottomX1 = horizontalBottomX0 + cornerWidth;
                    int verticalBottomX0 = bottomX;
                    int verticalBottomX1 = bottomX;

                    int topX = parentCollapsed ?
                            parent.getWidth() / 2 + cornerWidth - localStrokeWidth / 2 + additionalSpace :
                            v.getWidth() - lp.rightMargin + strokeWidth;
                    // ^ added strokeWidth because the background of the recycler view
                    // contains a white line that contributes to the optical illusion
                    // that there is more space from the right limit to the topX point
                    // than there is at the left side between the left limit and the
                    // bottomX point
                    // this should be added conditionally, if the background actually
                    // contains such a line, indeed (^ .^)
                    int topY = (int) (v.getY()) + lp.topMargin + additionalSpace * (parentCollapsed ? 1 : 2);


                    int horizontalTopX0 = topX - localStrokeWidth / 2;
                    int horizontalTopX1 = topX - cornerWidth + localStrokeWidth / 2;
                    int verticalTopX0 = topX;
                    int verticalTopX1 = topX;

                    int horizontalTopY0 = topY;
                    int horizontalTopY1 = topY;
                    int verticalTopY0 = topY - localStrokeWidth / 2;
                    int verticalTopY1 = topY + cornerWidth;

//                    gradient = new LinearGradient(
//                            horizontalBottomX0, horizontalBottomY0, horizontalBottomX1, horizontalBottomY1,
//                            new int[]{colorCenter, colorStart}, null,
//                            Shader.TileMode.CLAMP);
//                    p.setShader(gradient);
//
////                ---
//                    c.drawLine(horizontalBottomX0, horizontalBottomY0, horizontalBottomX1, horizontalBottomY1, p);
////                |
//                    gradient = new LinearGradient(
//                            verticalBottomX0, verticalBottomY0, verticalBottomX1, verticalBottomY1,
//                            new int[]{colorCenter, colorStart}, null,
//                            Shader.TileMode.CLAMP);
//                    p.setShader(gradient);
//
//                    c.drawLine(verticalBottomX0, verticalBottomY0, verticalBottomX1, verticalBottomY1, p);
//
////                ---
//                    gradient = new LinearGradient(
//                            horizontalTopX0, horizontalTopY0, horizontalTopX1, horizontalTopY1,
//                            new int[]{colorCenter, colorStart}, null,
//                            Shader.TileMode.CLAMP);
//                    p.setShader(gradient);
//
//                    c.drawLine(horizontalTopX0, horizontalTopY0, horizontalTopX1, horizontalTopY1, p);
////                |
//                    gradient = new LinearGradient(
//                            verticalTopX0, verticalTopY0, verticalTopX1, verticalTopY1,
//                            new int[]{colorCenter, colorStart}, null,
//                            Shader.TileMode.CLAMP);
//                    p.setShader(gradient);
//
//                    c.drawLine(verticalTopX0, verticalTopY0, verticalTopX1, verticalTopY1, p);
                }
            }
        }
    }
}
