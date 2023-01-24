package net.teamcrimx.party.cloud;

import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.driver.module.ModuleLifeCycle;
import eu.cloudnetservice.driver.module.ModuleTask;
import eu.cloudnetservice.driver.module.driver.DriverModule;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.teamcrimx.party.cloud.listener.ChannelMessageReceiveListener;
import net.teamcrimx.party.cloud.listener.player.ProxyDisconnectListener;
import net.teamcrimx.party.cloud.track.ActivePartiesTracker;
import net.teamcrimx.party.cloud.track.PartyManager;

public class PartyModule extends DriverModule {

    private ActivePartiesTracker partiesTracker;
    private PartyManager partyManager;

    private PlayerManager playerManager;

    @ModuleTask(event = ModuleLifeCycle.STARTED)
    private void onStart() { // Module successfully started
        CloudNetDriver.instance().eventManager().registerListener(new ChannelMessageReceiveListener(this));
        CloudNetDriver.instance().eventManager().registerListener(new ProxyDisconnectListener(this));
    }

    @ModuleTask(event = ModuleLifeCycle.LOADED)
    private void onLoad() { // Module is being loaded
        this.playerManager = CloudNetDriver.instance().serviceRegistry()
                .firstProvider(PlayerManager.class);

        this.partiesTracker = new ActivePartiesTracker();
        this.partyManager = new PartyManager(this);
    }

    @ModuleTask(event = ModuleLifeCycle.STOPPED)
    private void onStop() { // Module is being stopped
        //System.out.printf("Trying to disable custom module %s", this.getClass().getName());
    }

    public PlayerManager playerManager() {
        return playerManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public ActivePartiesTracker getPartiesTracker() {
        return partiesTracker;
    }
}
