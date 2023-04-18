package net.teamcrimx.partyandfriends.cloud;

import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.driver.module.ModuleLifeCycle;
import eu.cloudnetservice.driver.module.ModuleTask;
import eu.cloudnetservice.driver.module.driver.DriverModule;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.teamcrimx.partyandfriends.api.database.MongoDatabaseImpl;
import net.teamcrimx.partyandfriends.api.database.MongoMethodsUtil;
import net.teamcrimx.partyandfriends.cloud.friends.listener.ChannelFriendMessageReceiveListener;
import net.teamcrimx.partyandfriends.cloud.friends.listener.player.ProxyConnectListener;
import net.teamcrimx.partyandfriends.cloud.friends.manager.FriendHolder;
import net.teamcrimx.partyandfriends.cloud.friends.manager.FriendManager;
import net.teamcrimx.partyandfriends.cloud.party.listener.ChannelMessageReceiveListener;
import net.teamcrimx.partyandfriends.cloud.party.listener.player.ProxyDisconnectListener;
import net.teamcrimx.partyandfriends.cloud.party.listener.player.ServerSwitchListener;
import net.teamcrimx.partyandfriends.cloud.party.manager.ActivePartiesTracker;
import net.teamcrimx.partyandfriends.cloud.party.manager.PartyManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PartyAndFriendsModule extends DriverModule {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final MiniMessage miniMessage = MiniMessage.builder().build();

    private PartyManager partyManager;
    private ActivePartiesTracker partiesTracker;

    private FriendManager friendManager;
    private FriendHolder friendHolder;

    private PlayerManager playerManager;
    private MongoMethodsUtil mongoMethods;

    @ModuleTask(event = ModuleLifeCycle.STARTED)
    private void onStart() { // Module successfully started
        // PARTY
        CloudNetDriver.instance().eventManager().registerListener(new ChannelMessageReceiveListener(this));
        CloudNetDriver.instance().eventManager().registerListener(new ProxyDisconnectListener(this));
        CloudNetDriver.instance().eventManager().registerListener(new ServerSwitchListener(this));

        // FRIEND
        CloudNetDriver.instance().eventManager().registerListener(new ChannelFriendMessageReceiveListener(this));
        CloudNetDriver.instance().eventManager().registerListener(new ProxyConnectListener(this));
        CloudNetDriver.instance().eventManager().registerListener(new net.teamcrimx.partyandfriends.cloud.friends.listener.player.ProxyDisconnectListener(this));


        /*this.scheduledExecutorService.scheduleAtFixedRate(() -> {
            this.partiesTracker.checkActiveParties();
        }, 30L, 30L, TimeUnit.SECONDS); */
    }

    @ModuleTask(event = ModuleLifeCycle.LOADED)
    private void onLoad() { // Module is being loaded
        this.playerManager = CloudNetDriver.instance().serviceRegistry()
                .firstProvider(PlayerManager.class);

        MongoDatabaseImpl.initializeDatabase();
        this.mongoMethods = MongoDatabaseImpl.mongoMethodsUtil();

        this.partiesTracker = new ActivePartiesTracker(this);
        this.partyManager = new PartyManager(this);

        this.friendManager = new FriendManager(this);
        this.friendHolder = new FriendHolder();
    }

    @ModuleTask(event = ModuleLifeCycle.STOPPED)
    private void onStop() { // Module is being stopped
        //System.out.printf("Trying to disable custom module %s", this.getClass().getName());
    }

    public MiniMessage miniMessage() {
        return miniMessage;
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

    public FriendManager friendManager() {
        return friendManager;
    }

    public MongoMethodsUtil mongoMethods() {
        return mongoMethods;
    }

    public FriendHolder friendHolder() {
        return friendHolder;
    }
}
