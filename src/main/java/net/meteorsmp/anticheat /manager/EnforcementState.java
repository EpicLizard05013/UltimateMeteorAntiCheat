package net.meteorsmp.anticheat.manager;

/**
 * Global anticheat kill switch, toggled via /meteor disable and /meteor enable.
 * Deliberately does NOT affect antidupe protections — those stay on regardless,
 * since disabling dupe protection should never be a side effect of muting
 * false-positive anticheat flags.
 *
 * Static/global on purpose: it's a single plugin-wide switch, and threading an
 * instance through 15+ check classes just for one boolean isn't worth it.
 * Resets to enabled on every restart, so a forgotten "disabled" state can't
 * survive a crash and leave the server unprotected indefinitely.
 */
public class EnforcementState {
    private static volatile boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }
}
