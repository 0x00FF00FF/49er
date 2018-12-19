package org.rares.miner49er.ui.actionmode;

public interface GenericMenuActions {

    /**
     * Start the procedure to add a &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean add(long id);

    /**
     * Start the procedure to edit a &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean edit(long id);

    /**
     * Start the procedure to remove a &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean remove(long id);

    /**
     * Shows details for &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean details(long id);

    /**
     * Adds &lt;something&gt; to the list of favorites.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean favorite(long id);

    /**
     * Search for &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean search(long id);

    /**
     * Filter &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean filter(long id);

    /**
     * For other than generic actions.
     *
     * @param menuActionId The id of the action.
     * @param id the id of the entity to act upon
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     */
    boolean menuAction(int menuActionId, long id);
}
