package org.rares.miner49er.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;

import java.util.List;

/**
 * @author rares
 * @since 03.10.2017
 */

public class UiUtil {

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static int getBrighterColor(int color, float extraBrightness) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
//        Log.i(TAG, "getBrighterColor: color: " + color + " hsv: " + Arrays.toString(hsv));
        if (hsv[1] < 0.5F) {
            hsv[1] -= extraBrightness;
        } else {
            hsv[1] -= 2 * extraBrightness;
        }
        hsv[2] += extraBrightness;
        return Color.HSVToColor(hsv);
    }

    public static int getTransparentColor(int color, int desiredAlpha) {
        if (desiredAlpha < 0) {
            desiredAlpha = 0;
        }
        if (desiredAlpha > 0xff) {
            desiredAlpha = 0xff;
        }
        return Color.argb(desiredAlpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * contrast : 0 to 10 brightness : -255 to 255
     *
     * @param mBitmap    bitmap to change
     * @param contrast   0:10
     * @param brightness -255:255
     * @return new bitmap with modified brightness/contrast
     */
    public static Bitmap enhanceImage(Bitmap mBitmap, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });
        Bitmap mEnhancedBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), mBitmap
                .getConfig());
        Canvas canvas = new Canvas(mEnhancedBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(mBitmap, 0, 0, paint);
        return mEnhancedBitmap;
    }

    public static void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup) child.getParent();
        if (null != parent) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

    public static String populateInfoString(String template, ProjectData projectData) {
        String in = "-", un = "-", hn = "-";
        int issuesNumber = 0;
        int usersNumber = 0;
        int hoursNumber = 0;

        if (projectData != null) {
            List<IssueData> issues = projectData.getIssues();
            if (issues != null) {
                for (IssueData issueData : issues) {
                    if(!issueData.deleted) {
                        ++issuesNumber;
                        List<TimeEntryData> timeEntryData = issueData.getTimeEntries();
                        if (timeEntryData != null) {
                            for (TimeEntryData ted : timeEntryData) {
                                hoursNumber += ted.deleted ? 0 : ted.getHours();
                            }
                        }
                    }
                }
                in = String.valueOf(issuesNumber);
                hn = String.valueOf(hoursNumber);
            }
            List<UserData> users = projectData.getTeam();
            if (users != null) {
                usersNumber = users.size();
                un = String.valueOf(usersNumber);
            }
        }

        return String.format(template, in, un, hn);
    }


    public static String populateInfoString(String template, IssueData issueData) {
        String en, uh, th;
        int entriesNumber = 0;
        int userHours = 0;
        int totalHours = 0;

        if (issueData != null) {
            List<TimeEntryData> entries = issueData.getTimeEntries();
            if (entries != null) {
                for (TimeEntryData entry : entries) {
                    userHours += entry.getUserId().equals(ViewModelCache.getInstance().loggedInUser.id) ? entry.getHours() : 0;
                    totalHours += entry.deleted ? 0 : entry.getHours();
                    entriesNumber += entry.deleted ? 0 : 1;
                }
            }
        }
        en = String.valueOf(entriesNumber);
        uh = String.valueOf(userHours);
        th = String.valueOf(totalHours);

        return String.format(template, en, uh, th);
    }
}
