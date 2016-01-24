package com.samlanning.core.server.util;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Listenable<L> {

    /**
     * An object used purely to synchronize on.
     * 
     * <p>
     * This is used instead os synchronized methods because sub classes may also
     * want to create synchronized methods, that should not interfere with the
     * listener synchronization.
     */
    private final Object mutex = new Object();
    private final Set<L> listeners = new LinkedHashSet<>();
    private Visitor<L> visitor;

    public void addListener(L listener) {
        synchronized (mutex) {
            listeners.add(listener);
            if (this.visitor != null)
                this.visitor.visit(listener);
        }
    }

    public void removeListener(L listener) {
        synchronized (mutex) {
            listeners.remove(listener);
        }
    }

    protected void visitListeners(Visitor<L> visitor) {
        synchronized (mutex) {
            for (L listener : listeners)
                visitor.visit(listener);
        }
    }

    protected void updateNewListenerVisitor(Visitor<L> visitor) {
        synchronized (mutex) {
            this.visitor = visitor;
        }
    }

    public interface Visitor<L> {
        public void visit(L listener);
    }
}
