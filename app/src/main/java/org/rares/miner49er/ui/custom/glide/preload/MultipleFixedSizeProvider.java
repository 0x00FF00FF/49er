package org.rares.miner49er.ui.custom.glide.preload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MultipleFixedSizeProvider<T> implements MultipleListPreloader.PreloadSizeProvider<T> {

    /**
     * Adds sizes to a list of (pre-existing) sizes. <br />
     *
     * @param w width
     * @param h height
     */
    public void addSizes(int w, int h) {
//        Log.d(TAG, "addSizes() called with: w = [" + w + "], h = [" + h + "]");
        if (w == 0 || h == 0) {
            return;
        }
        int newSizes[] = new int[sizes.length + 2];
        newSizes[0] = w;
        newSizes[1] = h;
        if (alreadyAdded(newSizes)) {
            return;
        }
        System.arraycopy(sizes, 0, newSizes, 2, sizes.length);

        sizes = newSizes;
    }

    @Nullable
    @Override
    public int[] getPreloadSize(@NonNull T item, int adapterPosition, int perItemPosition) {
        return sizes;
    }

    private boolean alreadyAdded(int[] newSizes) {
        if (sizes.length == 0) {
            return false;
        }
        for (int i = 0; i < sizes.length; i += 2) {
            if (sizes[i] == newSizes[0] && sizes[i + 1] == newSizes[1]) {
                return true;
            }
        }
        return false;
    }

//    private static final String TAG = MultipleFixedSizeProvider.class.getSimpleName();
    private int[] sizes = new int[0];
}
