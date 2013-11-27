//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.entity;

/**
 * An entity is a collection of components.
 */
public class Entity
{
    /** The world to which this entity belongs. */
    public final World world;

    /** This entity's unique id. This id will be valid for as long as the entity remains alive.
     * Once {@link #destroy} is called, this id may be reused by a new entity. */
    public final int id;

    /** Creates an entity in the specified world, initializes it with the supplied set of
     * components and queues it to be added to the world on the next update.
     */
    public Entity (World world, Component... components) {
        this.world = world;
        this.id = world.claimId();
        for (Component component : components) component.add(this);
        world.toAdd.add(this);
    }

    /** Returns whether this entity has been destroyed. */
    public boolean isDestroyed () {
        return (_flags & DESTROYED) != 0;
    }

    /** Returns whether this entity is currently enabled. */
    public boolean isEnabled () {
        return (_flags & ENABLED) != 0;
    }

    /** Enables or disables this entity. When an entity is disabled, it is removed from all systems
     * in which it is currently an active participant (prior to the next update). When it is
     * re-enabled, it is added back to all systems that are interested in it (prior to the next
     * update).
     */
    public void setEnabled (boolean enabled) {
        checkDestroyed("Cannot modify destroyed entity.");
        boolean isEnabled = (_flags & ENABLED) != 0;
        if (isEnabled && !enabled) {
            _flags &= ~ENABLED;
            world.toRemove.add(this);
        } else if (!isEnabled && enabled) {
            _flags |= ENABLED;
            world.toAdd.add(this);
        }
    }

    /** Returns true if this entity has the component {@code comp}, false otherwise. */
    public boolean has (Component comp) {
        return (componentsMask & comp.mask) != 0;
    }

    /** Adds the specified component to this entity. This will queue the component up to be added
     * or removed to appropriate systems on the next update.
     */
    public void add (Component comp) {
        checkDestroyed("Cannot add components to destroyed entity.");
        comp.add(this);
        queueChange();
    }

    /** Removes the specified component from this entity. This will queue the component up to be
     * added or removed to appropriate systems on the next update.
     */
    public void remove (Component comp) {
        checkDestroyed("Cannot remove components from destroyed entity.");
        comp.remove(this);
        queueChange();
    }

    /** Destroys this entity, causing it to be removed from the world on the next update. */
    public void destroy () {
        if (!isDestroyed()) {
            _flags |= DESTROYED;
            world.toRemove.add(this);
        }
    }

    /** Indicates that this entity has changed, and causes it to be reconsidered for inclusion or
     * exclusion from systems on the next update. This need not be called when adding or removing
     * components, and should only be called if some other external circumstance changes that
     * requires recalculation of which systems are interested in this entity.
     */
    public void didChange () {
        checkDestroyed("Cannot didChange destroyed entity.");
        queueChange();
    }

    protected final void queueChange () {
        // if we're not yet added, we can stop now because we'll be processed when added; if we're
        // not currently enabled, we can stop now and we'll be processed when re-enabled
        if ((_flags & (ADDED|ENABLED)) == 0) return;
        // if we're already queued up as changing, we've got nothing to do either
        if ((_flags & CHANGING) != 0) return;
        // otherwise, mark ourselves as changing and queue on up
        _flags |= CHANGING;
        world.toChange.add(this);
    }

    protected final void checkDestroyed (String error) {
        if ((_flags & DESTROYED) != 0) throw new IllegalStateException(error);
    }

    void noteAdded () { _flags |= ADDED; }
    void clearChanging () { _flags &= ~CHANGING; }
    void noteRemoved () { /* TODO: if (isDestroyed()) onDestroyed.emit(this); */ }

    /** A bit mask indicating which systems are processing this entity. */
    long systemsMask;

    /** A bit mask indicating which components are possessed by this entity. */
    long componentsMask;

    /** Flags pertaining to this entity's state. */
    protected int _flags;

    protected static final int ENABLED   = 1 << 0;
    protected static final int DESTROYED = 1 << 1;
    protected static final int ADDED     = 1 << 2;
    protected static final int CHANGING  = 1 << 3;
}
