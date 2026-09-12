package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WhitelistManager {

    private final Set<UUID> whitelistedPlayers = ConcurrentHashMap.newKeySet();

    public WhitelistManager(UltimateMeoterAnticheat plugin) {}

    public void addWhitelist(UUID uuid) { whitelistedPlayers.add(uuid); }
    public void removeWhitelist(UUID uuid) { whitelistedPlayers.remove(uuid); }
    public boolean isWhitelisted(UUID uuid) { return whitelistedPlayers.contains(uuid); }
}
