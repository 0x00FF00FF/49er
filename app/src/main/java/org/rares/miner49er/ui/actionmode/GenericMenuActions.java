package org.rares.miner49er.ui.actionmode;

public interface GenericMenuActions {

    /**
     * Start the procedure to add a &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean add(int id);

    /**
     * Start the procedure to edit a &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean edit(int id);

    /**
     * Start the procedure to remove a &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean remove(int id);

    /**
     * Shows details for &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean details(int id);

    /**
     * Adds &lt;something&gt; to the list of favorites.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean favorite(int id);

    /**
     * Search for &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean search(int id);

    /**
     * Filter &lt;something&gt;.
     *
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     * @param id the id of the entity to act upon
     */
    boolean filter(int id);

    /**
     * For other than generic actions.
     *
     * @param menuActionId The id of the action.
     * @param id the id of the entity to act upon
     * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
     */
    boolean menuAction(int menuActionId, int id);
}
