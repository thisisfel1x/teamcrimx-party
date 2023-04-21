package net.teamcrimx.partyandfriends.api.constants;

import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import org.jetbrains.annotations.NotNull;

public class CloudConstants {

    public static @NotNull PlayerManager playerManager = CloudNetDriver.instance().serviceRegistry()
            .firstProvider(PlayerManager.class);

}
