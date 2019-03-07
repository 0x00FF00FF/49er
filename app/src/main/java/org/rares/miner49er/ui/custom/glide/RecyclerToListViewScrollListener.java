package org.rares.miner49er.ui.custom.glide;

import android.widget.AbsListView;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.rares.miner49er.layoutmanager.StickyLinearLayoutManager;

public class RecyclerToListViewScrollListener extends RecyclerView.OnScrollListener {
        public static final int UNKNOWN_SCROLL_STATE = Integer.MIN_VALUE;
        private final AbsListView.OnScrollListener scrollListener;
        private int lastFirstVisible = -1;
        private int lastVisibleCount = -1;
        private int lastItemCount = -1;

        public RecyclerToListViewScrollListener(@NonNull AbsListView.OnScrollListener scrollListener) {
            this.scrollListener = scrollListener;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            int listViewState;
            switch (newState) {
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    listViewState = ListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
                    break;
                case RecyclerView.SCROLL_STATE_IDLE:
                    listViewState = ListView.OnScrollListener.SCROLL_STATE_IDLE;
                    break;
                case RecyclerView.SCROLL_STATE_SETTLING:
                    listViewState = ListView.OnScrollListener.SCROLL_STATE_FLING;
                    break;
                default:
                    listViewState = UNKNOWN_SCROLL_STATE;
            }

            scrollListener.onScrollStateChanged(null /*view*/, listViewState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            Log.i(RecyclerToListViewScrollListener.class.getSimpleName(), "onScrolled: ");
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            StickyLinearLayoutManager stickyLm = null;
//            LinearLayoutManager linearLm = null;
            if (lm instanceof StickyLinearLayoutManager) {
                stickyLm = (StickyLinearLayoutManager) lm;

                int firstVisible = stickyLm.firstVisiblePosition();
                int visibleCount = Math.abs(firstVisible - stickyLm.lastVisiblePosition());
                int itemCount = recyclerView.getAdapter().getItemCount();

                if (firstVisible != lastFirstVisible || visibleCount != lastVisibleCount
                        || itemCount != lastItemCount) {
                    scrollListener.onScroll(null, firstVisible, visibleCount, itemCount);
                    lastFirstVisible = firstVisible;
                    lastVisibleCount = visibleCount;
                    lastItemCount = itemCount;
//                    return;
                }
            }

/*            if (lm instanceof LinearLayoutManager) {
                linearLm = (LinearLayoutManager) lm;

                int firstVisible = linearLm.findFirstVisibleItemPosition();
                int visibleCount = Math.abs(firstVisible - linearLm.findLastVisibleItemPosition());
                int itemCount = recyclerView.getAdapter().getItemCount();

                if (firstVisible != lastFirstVisible || visibleCount != lastVisibleCount
                        || itemCount != lastItemCount) {
                    scrollListener.onScroll(null, firstVisible, visibleCount, itemCount);
                    lastFirstVisible = firstVisible;
                    lastVisibleCount = visibleCount;
                    lastItemCount = itemCount;
                }
            }*/
        }
    }