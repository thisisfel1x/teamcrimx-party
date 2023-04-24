package net.teamcrimx.partyandfriends.cloud;

import eu.cloudnetservice.driver.event.EventManager;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.module.ModuleLifeCycle;
import eu.cloudnetservice.driver.module.ModuleTask;
import eu.cloudnetservice.driver.module.driver.DriverModule;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.teamcrimx.partyandfriends.api.database.MongoDatabaseImpl;
import net.teamcrimx.partyandfriends.api.database.MongoMethodsUtil;
import net.teamcrimx.partyandfriends.api.friends.SimpleFriend;
import net.teamcrimx.partyandfriends.cloud.friends.listener.ChannelFriendMessageReceiveListener;
import net.teamcrimx.partyandfriends.cloud.friends.listener.player.ProxyConnectListener;
import net.teamcrimx.partyandfriends.cloud.friends.manager.FriendHolder;
import net.teamcrimx.partyandfriends.cloud.friends.manager.FriendManager;
import net.teamcrimx.partyandfriends.cloud.party.listener.ChannelMessageReceiveListener;
import net.teamcrimx.partyandfriends.cloud.party.listener.player.ProxyDisconnectListener;
import net.teamcrimx.partyandfriends.cloud.party.listener.player.ServerSwitchListener;
import net.teamcrimx.partyandfriends.cloud.party.manager.ActivePartiesTracker;
import net.teamcrimx.partyandfriends.cloud.party.manager.PartyManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PartyAndFriendsModule extends DriverModule {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final MiniMessage miniMessage = MiniMessage.builder().build();
    private PlayerManager playerManager;;
    private PartyManager partyManager;
    private ActivePartiesTracker partiesTracker;
    private FriendManager friendManager;
    private FriendHolder friendHolder;
    private MongoMethodsUtil mongoMethods;

    @Inject
    public PartyAndFriendsModule(@NonNull @Named("module") InjectionLayer<?> injectionLayer) {
    }

    @ModuleTask(order = 10, lifecycle = ModuleLifeCycle.STARTED)
    private void onStart() { // Module successfully started
        this.playerManager = ServiceRegistry.first(PlayerManager.class);

        this.scheduledExecutorService.scheduleAtFixedRate(() -> {
            for (SimpleFriend simpleFriend : this.friendHolder.simpleFriendMap().values()) {
                simpleFriend.update(false);
            }

            this.partiesTracker.checkActiveParties();
        }, 30L, 10L, TimeUnit.SECONDS);
    }

    @ModuleTask(order = 5, lifecycle = ModuleLifeCycle.STARTED)
    public void registerListener(EventManager eventManager) {
        // PARTY
        eventManager.registerListener(new ChannelMessageReceiveListener(this));
        eventManager.registerListener(new ProxyDisconnectListener(this));
        eventManager.registerListener(new ServerSwitchListener(this));

        // FRIEND
        eventManager.registerListener(new ChannelFriendMessageReceiveListener(this));
        eventManager.registerListener(new ProxyConnectListener(this));
        eventManager.registerListener(new net.teamcrimx.partyandfriends.cloud.friends.listener.player.ProxyDisconnectListener(this));
    }

    @ModuleTask(lifecycle = ModuleLifeCycle.LOADED)
    private void onLoad() { // Module is being loaded
        //this.playerManager = ServiceRegistry.first(PlayerManager.class);

        MongoDatabaseImpl.initializeDatabase();
        this.mongoMethods = MongoDatabaseImpl.mongoMethodsUtil();

        this.partiesTracker = new ActivePartiesTracker(this);
        this.partyManager = new PartyManager(this);

        this.friendManager = new FriendManager(this);
        this.friendHolder = new FriendHolder();
    }

    @ModuleTask(lifecycle = ModuleLifeCycle.STOPPED)
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
