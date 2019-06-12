package org.rares.miner49er.domain.entries.ui.actions.edit;

import androidx.appcompat.widget.Toolbar;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.entries.ui.actions.add.TimeEntryAddActionListener;
import org.rares.miner49er.fakes.ActionEnforcerFake;
import org.rares.miner49er.fakes.ActionManagerFake;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TimeEntryEditActionListenerTest {

    @Mock
    private Toolbar toolbar;
    private ActionEnforcer enforcer;
    private ActionManagerFake manager;
    private TimeEntryEditActionListener timeEntryEditActionListener;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        enforcer = new ActionEnforcerFake();
        manager = new ActionManagerFake();
        timeEntryEditActionListener = new TimeEntryEditActionListener(enforcer, manager);
        manager.registerActionListener(timeEntryEditActionListener);     //  still seems too coupled
    }

    /*
     *  Given   an ActionEnforcer, an ActionListenerManager and a TimeEntryEditActionListener
     *  When    the TimeEntryEditActionListener is ready to use
     *  Then    the action enforcer (fragment) has a result listener (the TimeEntryEditActionListener)
     */
    @Test
    public void testConstructionLink() {
        // given, when [setup]
        // then
        assertEquals(timeEntryEditActionListener, enforcer.getResultListener());
    }

    /*  Given   an ActionEnforcer, an ActionListenerManager and a TimeEntryEditActionListener
     *  When    back action happens on the toolbar (i.e. ActionListenerManager)
     *  Then    the action is propagated to the fragment (i.e. ActionEnforcer), by the timeEntryEditActionListener
     */
    @Test
    public void testBackOnToolbar() {
        boolean unregister = manager.listener.onToolbarBackPressed();
        assertTrue(((ActionEnforcerFake) enforcer).prepareExitCalled);
        assertFalse(unregister);
    }

    /*  Given   an ActionEnforcer, an ActionListenerManager and a TimeEntryEditActionListener
     *  When    save (i.e. add) action happens on the toolbar (i.e. ActionListenerManager)
     *  Then    the action is propagated to the fragment (i.e. ActionEnforcer), by the timeEntryEditActionListener
     */
    @Test
    public void testSaveOnToolbar() {
        // usually the TAM calls this
        timeEntryEditActionListener.getMenuActionsProvider().add(1L);
        assertTrue(((ActionEnforcerFake) enforcer).applyActionCalled);
    }

    /*  Given   an ActionEnforcer, an ActionListenerManager and a TimeEntryEditActionListener
     *  When    an action completes on the fragment (fragment is dismissed)
     *  Then    the toolbar gets a request to unregister the timeEntryEditActionListener
     *  And     the fragment has no result listener
     */
    @Test
    public void testToolbarUnregister() {
        enforcer.getResultListener().onFragmentDismiss();
        assertNull(manager.listener);
        assertNull(enforcer.getResultListener());
    }

    /*
     *  Given   an ActionEnforcer, an ActionListenerManager and a TimeEntryEditActionListener
     *  When    the TimeEntryEditActionListener is required to configure the toolbar title,
     *  Then    the config object contains the correct title
     *  And     the config is not generic
     */
    @Test
    public void testToolbarConfig() {
        MenuConfig config = new ToolbarActionManager(toolbar).new MenuConfig();
        timeEntryEditActionListener.configureCustomActionMenu(config);
        assertEquals(R.string.time_entry_form_header_edit, config.titleRes);
        assertFalse(config.createGenericMenu);
    }

    /*
     *  Given   a TimeEntryEditActionListener
     *  When    the menu entity id is set
     *  Then    the 'get' method returns the correct id
     */
    @Test
    public void testMenuEntityId() {
        long id = 1L;
        timeEntryEditActionListener.setMenuActionEntityId(id);
        assertEquals(id, timeEntryEditActionListener.getMenuActionEntityId());
    }

    /*
     *  Given   an ActionEnforcer, null instead of ActionListenerManager
     *  When    a TimeEntryEditActionListener is created with these values
     *  Then    the timeEntryEditActionListener will not throw any errors
     *  And     calling onFragmentDismiss on the timeEntryEditActionListener will not throw any errors
     */
    @Test
    public void testNullALM() {
        // given, when
        timeEntryEditActionListener = new TimeEntryEditActionListener(enforcer, null);
        // then
        timeEntryEditActionListener.onFragmentDismiss();
        assertNull(enforcer.getResultListener());
    }

    /*
     *  Given   an ActionEnforcer, an ActionListenerManager and a TimeEntryAddActionListener
     *  When    a TimeEntryEditActionListener is created and onFragmentDismiss() is called,
     *  Then    all references in all objects are gone
     */
    @Test
    public void testRemoveReferences() {
        TimeEntryAddActionListener addActionListener = new TimeEntryAddActionListener(enforcer, null);
        enforcer.setResultListener(timeEntryEditActionListener);

        timeEntryEditActionListener.setAddActionListener(addActionListener);

        assertEquals(timeEntryEditActionListener, enforcer.getResultListener());

        enforcer.getResultListener().onFragmentDismiss();

        assertNull(manager.listener);
        assertNull(enforcer.getResultListener());

        assertNull(timeEntryEditActionListener.getMenuActionsProvider());
    }

    /*
     *  Given   a TimeEntryEditActionListener that has a dismissed fragment
     *  When    any of the class methods are called
     *  Then    no exception is thrown.
     */
    @Test
    public void testCallingMethodsAfterDismiss() {
        // given
        timeEntryEditActionListener.onFragmentDismiss();

        // when
        timeEntryEditActionListener.onFragmentDismiss();
        timeEntryEditActionListener.getMenuActionsProvider();
        timeEntryEditActionListener.getMenuActionEntityId();
        timeEntryEditActionListener.setMenuActionEntityId(1L);
        timeEntryEditActionListener.onToolbarBackPressed();
        timeEntryEditActionListener.configureCustomActionMenu(new ToolbarActionManager(toolbar).new MenuConfig());
    }
}