package org.rares.miner49er.domain.entries.ui.actions.add;

import androidx.appcompat.widget.Toolbar;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rares.miner49er.R;
import org.rares.miner49er.fakes.ActionEnforcerFake;
import org.rares.miner49er.fakes.ActionManagerFake;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TimeEntryAddActionListenerTest {

    @Mock
    private Toolbar toolbar;
    private ActionEnforcer enforcer;
    private ActionListenerManager manager;
    private TimeEntryAddActionListener timeEntryAddActionListener;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        enforcer = new ActionEnforcerFake();
        manager = new ActionManagerFake();
        timeEntryAddActionListener = new TimeEntryAddActionListener(enforcer, manager);
        manager.registerActionListener(timeEntryAddActionListener);     //  still seems too coupled
    }

    /*
     *  Given   an ActionEnforcer, an ActionListenerManager and a TimeEntryAddActionListener
     *  When    the TimeEntryAddActionListener is ready to use
     *  Then    the action enforcer (fragment) has a result listener (the TimeEntryAddActionListener)
     */
    @Test
    public void testConstructionLink() {
        // given, when [setup]
        // then
        assertEquals(timeEntryAddActionListener, enforcer.getResultListener());
    }

    /*  Given   an ActionEnforcer, an ActionListenerManager and a TimeEntryAddActionListener
     *  When    back action happens on the toolbar (i.e. ActionListenerManager)
     *  Then    the action is propagated to the fragment (i.e. ActionEnforcer), by the timeEntryAddActionListener
     */
    @Test
    public void testBackOnToolbar() {
        boolean unregister = timeEntryAddActionListener.onToolbarBackPressed();
        assertTrue(((ActionEnforcerFake) enforcer).prepareExitCalled);
        assertFalse(unregister);
    }

    /*  Given   an ActionEnforcer, an ActionListenerManager and a TimeEntryAddActionListener
     *  When    save (i.e. add) action happens on the toolbar (i.e. ActionListenerManager)
     *  Then    the action is handled and propagated to the fragment (i.e. ActionEnforcer), by the timeEntryAddActionListener
     */
    @Test
    public void testSaveOnToolbar() {
        boolean handled = timeEntryAddActionListener.getMenuActionsProvider().add(1L);
        assertTrue(handled);
        assertTrue(((ActionEnforcerFake) enforcer).applyActionCalled);
    }

    /*  Given   an ActionEnforcer, an ActionListenerManager and a TimeEntryAddActionListener
     *  When    an action completes on the fragment (fragment is dismissed)
     *  Then    the toolbar gets a request to unregister the timeEntryAddActionListener
     *  And     the fragment has no result listener
     *  And     future [add] actions on the [missing] fragment will not be handled
     */
    @Test
    public void testToolbarUnregister() {
        enforcer.getResultListener().onFragmentDismiss();
        assertNull(((ActionManagerFake) manager).listener);
        assertNull(enforcer.getResultListener());
        boolean handled = timeEntryAddActionListener.getMenuActionsProvider().add(1L);
        assertFalse(handled);
    }

    /*
     *  Given   an ActionEnforcer, an ActionListenerManager and a TimeEntryAddActionListener
     *  When    the TimeEntryAddActionListener is required to configure the toolbar title,
     *  Then    the config object contains the correct title
     *  And     the config is not generic
     */
    @Test
    public void testToolbarConfig() {
        MenuConfig config = new ToolbarActionManager(toolbar).new MenuConfig();
        timeEntryAddActionListener.configureCustomActionMenu(config);
        assertEquals(R.string.time_entry_form_header_add, config.titleRes);
        assertFalse(config.createGenericMenu);
    }

    /*
     *  Given   a TimeEntryAddActionListener
     *  When    the menu entity id is set
     *  Then    the 'get' method returns the correct id
     */
    @Test
    public void testMenuEntityId() {
        long id = 1L;
        timeEntryAddActionListener.setMenuActionEntityId(id);
        assertEquals(id, timeEntryAddActionListener.getMenuActionEntityId());
    }

    /*
     *  Given   a ActionEnforcer, null instead of ActionListenerManager
     *  When    a TimeEntryAddActionListener is created with these values
     *  Then    the timeEntryAddActionListener will not throw any errors
     *  And     calling onFragmentDismiss on the timeEntryAddActionListener will not throw any errors
     */
    @Test
    public void testNullALM() {
        // given, when
        timeEntryAddActionListener = new TimeEntryAddActionListener(enforcer, null);
        // then
        timeEntryAddActionListener.onFragmentDismiss();
        assertNull(enforcer.getResultListener());
    }

    /*
     *  Given   a TimeEntryAddActionListener that has a dismissed fragment
     *  When    any of the class methods are called
     *  Then    no exception is thrown.
     */
    @Test
    public void testCallingMethodsAfterDismiss() {
        // given
        timeEntryAddActionListener.onFragmentDismiss();

        // when
        timeEntryAddActionListener.onFragmentDismiss();
        timeEntryAddActionListener.getMenuActionsProvider();
        timeEntryAddActionListener.getMenuActionEntityId();
        timeEntryAddActionListener.setMenuActionEntityId(1L);
        timeEntryAddActionListener.onToolbarBackPressed();
        timeEntryAddActionListener.configureCustomActionMenu(new ToolbarActionManager(toolbar).new MenuConfig());
    }

}