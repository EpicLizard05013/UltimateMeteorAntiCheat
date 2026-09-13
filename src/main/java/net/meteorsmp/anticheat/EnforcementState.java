package net.meteorsmp.anticheat;

/**
 * Manages the global enforcement state of the anticheat system.
 * Controls whether anticheat detection is currently active.
 */
public class EnforcementState {
    private static boolean enabled = true;

    /**
     * Set the enforcement state
     * @param state true to enable enforcement, false to disable
     */
    public static void setEnabled(boolean state) {
        enabled = state;
    }

    /**
     * Check if enforcement is currently enabled
     * @return true if enforcement is enabled, false otherwise
     */
    public static boolean isEnabled() {
        return enabled;
    }
}
