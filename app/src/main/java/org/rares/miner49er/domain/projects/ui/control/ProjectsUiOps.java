package org.rares.miner49er.domain.projects.ui.control;

import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.cache.optimizer.DataUpdater;
import org.rares.miner49er.domain.agnostic.SelectedEntityProvider;
import org.rares.miner49er.domain.agnostic.TouchHelperCallback;
import org.rares.miner49er.domain.agnostic.TouchHelperCallback.SwipeDeletedListener;
import org.rares.miner49er.domain.projects.ProjectsInterfaces;
import org.rares.miner49er.domain.projects.adapter.ProjectsAdapter;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.persistence.ProjectsRepository;
import org.rares.miner49er.domain.projects.ui.actions.remove.ProjectRemoveAction;
import org.rares.miner49er.domain.projects.ui.viewholder.ProjectsViewHolder;
import org.rares.miner49er.layoutmanager.ResizeableLayoutManager;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig;
import org.rares.miner49er.ui.custom.glide.GlideApp;
import org.rares.miner49er.ui.custom.glide.preload.MultipleFixedSizeProvider;
import org.rares.miner49er.ui.custom.glide.preload.MultipleListPreloader;
import org.rares.miner49er.ui.custom.glide.preload.ProjectDataModelProvider;
import org.rares.miner49er.ui.custom.glide.preload.RecyclerToListViewScrollListener;
import org.rares.miner49er.util.PermissionsUtil;

import java.util.List;

import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ENABLED;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.FLAGS;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ICON_ID;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ITEM_ID;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ITEM_NAME;


/**
 * @author rares
 * @since 01.03.2018
 */

public class ProjectsUiOps
    extends ResizeableItemsUiOps
    implements
    SelectedEntityProvider,
    ToolbarActionManager.MenuActionListener,
    ResizeableLayoutManager.PreloadSizeConsumer,
    SwipeDeletedListener {

  @Setter
  private ProjectsInterfaces.ProjectsResizeListener projectsListResizeListener;

  private static final String TAG = ProjectsUiOps.class.getSimpleName();

  private ProjectsRepository projectsRepository;

  private ToolbarActionManager toolbarManager = null;

  private ProjectMenuActionsProvider menuActionsProvider;

  private boolean requireActionMode = false;

  @Getter
  @Setter
  private long menuActionEntityId;
  private TouchHelperCallback<ProjectsViewHolder, ProjectData> touchHelperCallback = new TouchHelperCallback<>();
  private ItemTouchHelper itemTouchHelper;

  public ProjectsUiOps(RecyclerView rv) {
//        Miner49erApplication.getRefWatcher(activity).watch(this);
    setRv(rv);
    projectsRepository = new ProjectsRepository();
    repository = projectsRepository;

    selectedDrawableRes = R.drawable.transient_semitransparent_rectangle_tr_bl;

    if (rv.getLayoutManager() instanceof ResizeableLayoutManager) {
      ((ResizeableLayoutManager) rv.getLayoutManager()).addMeasureCompleteListener(this);
    }

    glidePreloadModelProvider = new ProjectDataModelProvider(getRv().getContext(), ViewModelCacheSingleton.getInstance());

    MultipleListPreloader<String> projectListPreloader = new MultipleListPreloader<>(
        GlideApp.with(getRv()),
        glidePreloadModelProvider,
        sizeProvider,
        10);
    RecyclerToListViewScrollListener scrollListener = new RecyclerToListViewScrollListener(projectListPreloader);
    getRv().addOnScrollListener(scrollListener);

    itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
    itemTouchHelper.attachToRecyclerView(getRv());
    touchHelperCallback.setDao(InMemoryCacheAdapterFactory.ofType(ProjectData.class));
    touchHelperCallback.setDeletedListener(this);

    startDisposable = new CompositeDisposable();
  }

  /**
   * Should be called on activity start.
   */
  public void setupRepository() {
//        Log.e(TAG, "setupRepository() called");
    projectsRepository.setup();
    projectsRepository.registerSubscriber((Consumer<List>) getRv().getAdapter());
    touchHelperCallback.setAdapter((ProjectsAdapter) getRv().getAdapter());
  }

  @Override
  public boolean onListItemClick(ResizeableItemViewHolder holder) {
    boolean enlarge = super.onListItemClick(holder);

    if (toolbarManager == null) {
      provideToolbarActionManager();
    }

    if (enlarge) {
      requireActionMode = false;
      toolbarManager.unregisterActionListener(this);
      selectedEntityManager.deregisterProvider(this);
      itemTouchHelper.attachToRecyclerView(getRv());
    } else {
      requireActionMode = true;
      menuActionEntityId = holder.getItemProperties().getId();
//            toolbarManager.setEntityId(holder.getItemProperties().getId()); //
      toolbarManager.registerActionListener(this);
      selectedEntityManager.registerProvider(this);
      itemTouchHelper.attachToRecyclerView(null);
    }

    return enlarge;
  }

  @Override
  public void onListItemChanged(ItemViewProperties ivp) {
    super.onListItemChanged(ivp);
    toolbarManager.refreshActionMode();
  }

  @Override
  public boolean onToolbarBackPressed() {
    ResizeableItemViewHolder vh = getSelectedViewHolder();
    if (vh != null) {
      onListItemClick(vh);
    }
    return vh == null; // toolbarManager should not unregister this component if there is something selected
  }

  @Override
  public void configureCustomActionMenu(MenuConfig config) {
    ProjectsAdapter adapter = (ProjectsAdapter) getRv().getAdapter();
    config.menuId = 0;      // set this to 0 to end action mode when add project menu has ended.
    config.requireActionMode = requireActionMode;

    if (adapter == null || adapter.getLastSelectedPosition() == -1) {
      return;
    }

    ProjectData projectData = adapter.getData().get(adapter.getLastSelectedPosition());

    config.menuId = R.menu.menu_generic_actions;
    config.additionalMenuId = R.menu.menu_additional_projects;
    config.createGenericMenu = true;
    config.titleRes = 0;
    config.subtitleRes = 0;

    config.overrideGenericMenuResources = new int[3][5];
    config.overrideGenericMenuResources[0][ITEM_ID] = R.id.action_add;
    config.overrideGenericMenuResources[0][ICON_ID] = R.drawable.icon_path_add;
    config.overrideGenericMenuResources[0][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
    config.overrideGenericMenuResources[0][ITEM_NAME] = R.string.action_add_issue;
    config.overrideGenericMenuResources[0][ENABLED] = PermissionsUtil.canAddIssue(projectData) ? 1 : 0;

    config.overrideGenericMenuResources[1][ITEM_ID] = R.id.action_edit;
    config.overrideGenericMenuResources[1][ICON_ID] = R.drawable.icon_path_edit;
    config.overrideGenericMenuResources[1][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
    config.overrideGenericMenuResources[1][ITEM_NAME] = 0;
    config.overrideGenericMenuResources[1][ENABLED] = PermissionsUtil.canEditProject(projectData) ? 1 : 0;

    config.overrideGenericMenuResources[2][ITEM_ID] = R.id.action_remove;
    config.overrideGenericMenuResources[2][ICON_ID] = R.drawable.icon_path_remove;
    config.overrideGenericMenuResources[2][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
    config.overrideGenericMenuResources[2][ITEM_NAME] = 0;
    config.overrideGenericMenuResources[2][ENABLED] = PermissionsUtil.canRemoveProject(projectData) ? 1 : 0;

    config.additionalResources = new int[1][5];
    config.additionalResources[0][ITEM_ID] = R.id.action_add_user;
    config.additionalResources[0][ICON_ID] = R.drawable.icon_path_add_user;
    config.additionalResources[0][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
    config.additionalResources[0][ITEM_NAME] = 0;
    config.additionalResources[0][ENABLED] = PermissionsUtil.canEditProject(projectData) ? 1 : 0;

    config.title = projectData.getName();
    config.subtitle = adapter.getToolbarData(getRv().getContext(), adapter.getLastSelectedPosition());
  }


  @Override
  public GenericMenuActions getMenuActionsProvider() {
    return menuActionsProvider;
  }

  @Override
  protected void configureMenuActionsProvider(FragmentManager fm) {
    if (toolbarManager == null) {
      provideToolbarActionManager();
    }
    if (menuActionsProvider == null) {
      menuActionsProvider = new ProjectMenuActionsProvider(fragmentManager, toolbarManager, new ProjectRemoveAction(this));
    }
    toolbarManager.registerActionListener(this);
  }

  private void provideToolbarActionManager() {
    // TODO: 12/4/18 have the toolbar supplied, do not "grab"
    Toolbar t = ((AppCompatActivity) getRv().getContext()).findViewById(R.id.toolbar_c);

    if (t.getTag(R.integer.tag_toolbar_action_manager) == null) {
      toolbarManager = new ToolbarActionManager(t);
      t.setTag(R.integer.tag_toolbar_action_manager, toolbarManager);
    } else {
      toolbarManager = (ToolbarActionManager) t.getTag(R.integer.tag_toolbar_action_manager);
    }
  }

  /**
   * Should be called on activity stop.
   */
  public void shutdown() {
    projectsRepository.shutdown();
    startDisposable.dispose();
  }

  @Override
  protected AbstractAdapter createNewAdapter(ItemViewProperties itemViewProperties) {
    return null;
  }

  @Override
  public void onMeasureComplete(int[] dimensions) {
//        Log.d(TAG, "onMeasureComplete() called with: dimensions = [" + Arrays.toString(dimensions) + "]");
    if (dimensions.length < 2) {
      return;
    }
    for (int i = 0; i < dimensions.length; i += 2) {
      sizeProvider.addSizes(dimensions[i], dimensions[i + 1]);
    }
  }

  private CompositeDisposable getDisposable(CompositeDisposable d) {
    if (d == null || d.isDisposed()) {
      d = new CompositeDisposable();
    }
    return d;
  }

  @Override
  public void onItemDeleted(ViewHolder vh) {
//        toolbarManager.refreshActionMode();
  }

  @Override
  public void onItemPseudoDeleted(ViewHolder vh) {
    toolbarManager.refreshActionMode();
  }

  @Override
  public int getEntityType() {
    return SelectedEntityProvider.ET_PROJECT;
  }


  @Override
  public void updateEntity(DataUpdater dataUpdater) {
    AbstractViewModel projectData = getSelectedEntity();
    if (projectData != null) {
      dataUpdater.fullyUpdateProjects(projectData.objectId);
    }
  }

  private ProjectDataModelProvider glidePreloadModelProvider;
  private MultipleFixedSizeProvider<String> sizeProvider = new MultipleFixedSizeProvider<>();
  private CompositeDisposable startDisposable;
}
