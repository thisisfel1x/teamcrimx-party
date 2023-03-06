package net.teamcrimx.partyandfriends.api.constants;

import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;

public class CloudConstants {

    public static PlayerManager playerManager = CloudNetDriver.instance().serviceRegistry()
            .firstProvider(PlayerManager.class);

}
