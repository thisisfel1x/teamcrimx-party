package net.teamcrimx.partyandfriends.api;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionManagement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public class NetworkPlayer {

    private final UUID uuid;
    private final String name;
    private Component chatColor;
    private Component formattedName;

    public NetworkPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        this.chatColor = this.getDisplayColor();
        this.formattedName = Component.text(name, this.chatColor.color());

    }

    private Component getDisplayColor() {
        PermissionManagement permissionManagement = InjectionLayer.ext().instance(PermissionManagement.class);
        var permissionUser = permissionManagement.user(this.uuid);

        if(permissionUser == null) {
            return Component.empty();
        }

        var group = permissionManagement.highestPermissionGroup(permissionUser);

        if(group == null) {
            return Component.empty();
        }

        var color = group.color();

        if(color.startsWith("#")) {
            return Component.empty().color(TextColor.fromHexString(color));
        } else {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(color);
        }
    }

    public Component chatColor() {
        return chatColor;
    }

    public Component formattedName() {
        return formattedName;
    }
}
