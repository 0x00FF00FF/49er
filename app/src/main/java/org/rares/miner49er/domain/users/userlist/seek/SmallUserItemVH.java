package org.rares.miner49er.domain.users.userlist.seek;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.users.userlist.UserInterfaces;
import org.rares.miner49er.ui.custom.glide.GlideApp;

public class SmallUserItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private AppCompatImageView userPhotoView;
        private UserInterfaces.UserItemClickListener clickListener;

        public SmallUserItemVH(@NonNull View itemView, UserInterfaces.UserItemClickListener listener) {
            super(itemView);
            userPhotoView = itemView.findViewById(R.id.img_user_photo_small);
            itemView.setOnClickListener(this);
            clickListener = listener;
        }

        void bind(String source) {
            GlideApp.with(itemView)
                    .load(source)
                    .error(R.drawable.skull)
                    .into(userPhotoView);
        }

        @Override
        public void onClick(View v) {
            clickListener.onListItemClick(this);
        }
    }