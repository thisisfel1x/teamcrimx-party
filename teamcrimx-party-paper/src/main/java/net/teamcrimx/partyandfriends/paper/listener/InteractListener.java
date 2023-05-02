package net.teamcrimx.partyandfriends.paper.listener;

import net.teamcrimx.partyandfriends.paper.PaperPartyAndFriendsPlugin;
import net.teamcrimx.partyandfriends.paper.inventories.FriendInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InteractListener implements Listener {

    private final PaperPartyAndFriendsPlugin paperPartyAndFriendsPlugin;

    public InteractListener(PaperPartyAndFriendsPlugin paperPartyAndFriendsPlugin) {
        this.paperPartyAndFriendsPlugin = paperPartyAndFriendsPlugin;
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Action action = event.getAction();
        ItemStack item = event.getItem();

        if (event.getAction() == Action.PHYSICAL) {
            if (event.getClickedBlock() != null
                    && event.getClickedBlock().getType() == Material.FARMLAND) {
                event.setCancelled(true);
            }
        }

        if (item == null) {
            return;
        }

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if(event.hasItem()) {
                switch (item.getType()) {
                    case PLAYER_HEAD -> {
                        this.paperPartyAndFriendsPlugin.friendInventory().getFriendOverviewGui(player);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
