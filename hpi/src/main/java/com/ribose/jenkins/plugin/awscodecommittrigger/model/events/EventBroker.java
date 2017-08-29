
package com.ribose.jenkins.plugin.awscodecommittrigger.model.events;

import com.google.common.eventbus.EventBus;


/**
 * Provides a single instance of {@link EventBus}.
 */
public class EventBroker {

    private static EventBroker instance;

    private final EventBus     eventBus = new EventBus();

    public synchronized static EventBroker getInstance() {
        if (instance == null) {
            instance = new EventBroker();
        }
        return instance;
    }

    /**
     * Registers all handler methods on {@code object} to receive events.
     * @param object The object whose handler methods should be registered.
     * @see EventBus#register(Object)
     */
    public void register(final Object object) {
        this.eventBus.register(object);
    }

    /**
     * Unregisters all handler methods on a registered object.
     * @param object The object whose handler methods should be unregistered.
     * @throws IllegalArgumentException if the object was not previously registered.
     * @see EventBus#unregister(Object)
     */
    public void unregister(final Object object) {
        this.eventBus.unregister(object);
    }

    /**
     * Posts an event to all registered handlers. This method will return successfully after the
     * event has been posted to all handlers, and regardless of any exceptions thrown by handlers.
     * @param event The event to post
     * @see EventBus#post(Object)
     */
    public void post(final Object event) {
        this.eventBus.post(event);
    }
}
