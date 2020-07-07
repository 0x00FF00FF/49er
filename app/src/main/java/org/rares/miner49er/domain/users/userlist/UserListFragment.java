package org.rares.miner49er.domain.users.userlist;


import androidx.fragment.app.Fragment;

/*import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;*/


public class UserListFragment extends Fragment {
/*
    public static final String TAG = UserListFragment.class.getSimpleName();
    private long projectId = -1;
    private GroupAdapter<ViewHolder> userAdapter = new GroupAdapter<>();
    private Unbinder unbinder;

    private AsyncGenericDao<ProjectData> projectsDAO;
//    private AsyncGenericDao<UserData> usersDAO;

    @BindString(R.string.role_developer)
    String roleDeveloper;
    @BindString(R.string.role_designer)
    String roleDesigner;
    @BindString(R.string.role_project_owner)
    String roleProjectOwner;
    @BindString(R.string.role_project_manager)
    String roleProjectManager;

    public UserListFragment() {
        // Required empty public constructor
    }

    public static UserListFragment newInstance(long projectId) {
        UserListFragment fragment = new UserListFragment();
        Bundle args = new Bundle();
        args.putLong(ProjectsInterfaces.KEY_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getProjectId();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RecyclerView rootRv = (RecyclerView) inflater.inflate(R.layout.fragment_user_list, container, false);
        rootRv.setLayoutManager(new GridLayoutManager(rootRv.getContext(), 2, RecyclerView.HORIZONTAL, false));
//        rootRv.setLayoutManager(new LinearLayoutManager(rootRv.getContext(), RecyclerView.HORIZONTAL, false));
        unbinder = ButterKnife.bind(this, rootRv);
        refresh();
        rootRv.setAdapter(userAdapter);
        return rootRv;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        projectsDAO = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
//        usersDAO = InMemoryCacheAdapterFactory.ofType(UserData.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void refresh() {
        getProjectId();
        if (projectId > 0) {
            ProjectData projectData = projectsDAO.get(projectId, true).blockingGet().get();
            List<UserData> team = projectData.getTeam();
            if (team != null) {
                userAdapter.clear();
                for (UserData userData : team) {
                    userAdapter.add(new UserItem(userData, userData.id % 2 == 0 ? 1 : userData.id % 3 == 0 ? 0 : 2));
                }
            }
        }
        View v = getView();
        if (v != null) {
            v.requestLayout();
        }
    }

    private void getProjectId() {
        if (getArguments() != null) {
            projectId = getArguments().getLong(ProjectsInterfaces.KEY_PROJECT_ID, -1);
        }
    }

    class UserItem extends Item<ViewHolder> {

        int otherProjects;
        UserData userData;

        UserItem(UserData ud, int projects) {
            userData = ud;
            otherProjects = projects;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            String role = userData.id % 2 == 0 ? roleDeveloper : userData.id % 3 == 0 ? roleProjectManager : roleDesigner;
            CircleImageView userPhoto = viewHolder.itemView.findViewById(R.id.img_user_photo);
            ((AppCompatTextView) viewHolder.itemView.findViewById(R.id.tv_user_name)).setText(
                    TextUtils.clearNamePrefix(userData.getName()).replace(" ", "\n"));
            ((AppCompatTextView) viewHolder.itemView.findViewById(R.id.tv_user_role)).setText(role);
            ((AppCompatTextView) viewHolder.itemView.findViewById(R.id.tv_user_projects)).setText(
                    String.format("Currently working on %s other project%s.",
                            otherProjects == 0 ? "no" : otherProjects,
                            otherProjects > 1 ? "s" : ""));
            GlideApp.with(viewHolder.itemView)
                    .load(userData.getPicture())
                    .error(R.drawable.skull)
//                    .apply(RequestOptions.circleCropTransform())
                    .into(userPhoto);
        }

        @Override
        public int getLayout() {
            return *//*userData.getName().length() > 24 ? R.layout.item_user_list : *//*R.layout.item_user_list_v2;
        }
    }*/
}
