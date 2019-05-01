package org.rares.miner49er.domain.users.userlist;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.ui.custom.glide.GlideApp;
import org.rares.miner49er.util.TextUtils;

public class UserViewHolder extends RecyclerView.ViewHolder {

    @Getter
    private AppCompatTextView tvUserName;
    @Getter
    private AppCompatTextView tvUserRole;
    @Getter
    private AppCompatTextView tvOtherProjects;
    @Getter
    private CircleImageView userPhoto;
    @Getter
    private AppCompatImageView statusView;
    @Setter
    private UserInterfaces.UserItemClickListener eventListener;

    private String userInfoTemplate;
    private String userInfoTemplateZeroProjects;
    private String userInfoTemplatePluralSuffix;

    public UserViewHolder(@NonNull View itemView, boolean clickable) {
        super(itemView);
        tvUserName = itemView.findViewById(R.id.tv_user_name);
        tvUserRole = itemView.findViewById(R.id.tv_user_role);
        tvOtherProjects = itemView.findViewById(R.id.tv_user_projects);
        userPhoto = itemView.findViewById(R.id.img_user_photo);
        statusView = itemView.findViewById(R.id.img_status);
        if (clickable) {
            itemView.setOnClickListener(click -> {
                eventListener.onListItemClick(this);
                toggleStatusIconVisibility();
            });
        }
        userInfoTemplate = itemView.getContext().getResources().getString(R.string._users_info_template);
        userInfoTemplateZeroProjects = itemView.getContext().getResources().getString(R.string._users_info_template_zero);
        userInfoTemplatePluralSuffix = itemView.getContext().getResources().getString(R.string._users_info_template_plural_suffix);
    }

    public void bindData(UserData userData, int otherProjects, String userRole, boolean member) {
        tvUserName.setText(
                TextUtils.clearNamePrefix(userData.getName()).replace(" ", "\n"));
        tvUserRole.setText(userRole);
        tvOtherProjects.setText(
                String.format(userInfoTemplate,
                        otherProjects == 0 ? userInfoTemplateZeroProjects : otherProjects,
                        otherProjects > 1 ? userInfoTemplatePluralSuffix : ""));
        GlideApp.with(itemView)
                .load(userData.getPicture())
                .error(R.drawable.skull)
//                    .apply(RequestOptions.circleCropTransform())
                .into(userPhoto);
        if (member) {
//            statusView.setImageDrawable(itemView.getContext().getResources().getDrawable(android.R.drawable.star_off));
            statusView.setVisibility(View.VISIBLE);
        } else {
            statusView.setVisibility(View.GONE);
        }
/*            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                // elevation is problematic
                userPhoto.setElevation(4);
            }
*/
    }

    private void toggleStatusIconVisibility() {
        if (statusView.getVisibility() == View.VISIBLE) {
            statusView.setVisibility(View.GONE);
            return;
        }
        if (statusView.getVisibility() == View.GONE) {
            statusView.setVisibility(View.VISIBLE);
        }
    }
}