package com.samlanning.core.server.hue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.samlanning.core.server.Server;
import com.samlanning.core.server.lighting.RGBLightValue;
import com.samlanning.core.server.util.Logging;

/**
 * A Thread-Safe collection of light states
 */
public class HueLightStates {

    private static final org.slf4j.Logger logger = Logging.logger(Server.class);

    private final Map<String, LightState> states = new HashMap<>();
    private final Set<LightStateListener> listeners = new HashSet<>();

    // Public Methods

    public synchronized void updateStates(Map<String, LightState> newStates) {
        for (Map.Entry<String, LightState> entry : newStates.entrySet()) {
            String key = entry.getKey();
            LightState newState = entry.getValue();
            if (!newState.equals(states.get(key))) {
                states.put(key, newState);
                lightChanged(key, newState);
            }
        }
    }

    public synchronized void addLightStateListener(LightStateListener listener) {
        listeners.add(listener);
        for (Map.Entry<String, LightState> entry : states.entrySet())
            listener.lightChanged(entry.getKey(), entry.getValue());
    }

    private void lightChanged(String key, LightState newState) {
        logger.info(String.format(
            "Light Changed: %s - [%s] %s", key, (newState.on ? "on" : "off"), newState.color
        ));
        for (LightStateListener listener : listeners)
            listener.lightChanged(key, newState);
    }

    /**
     * Encapsulate on state and color
     */
    public static class LightState {
        public final boolean on;
        public final RGBLightValue color;

        public LightState(boolean on, RGBLightValue color) {
            this.on = on;
            this.color = color;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((color == null) ? 0 : color.hashCode());
            result = prime * result + (on ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LightState other = (LightState) obj;
            if (color == null) {
                if (other.color != null)
                    return false;
            } else if (!color.equals(other.color))
                return false;
            if (on != other.on)
                return false;
            return true;
        }
    }

    public static interface LightStateListener {
        /**
         * Called when a particular light has changed
         */
        public void lightChanged(String key, LightState newState);
    }

}
