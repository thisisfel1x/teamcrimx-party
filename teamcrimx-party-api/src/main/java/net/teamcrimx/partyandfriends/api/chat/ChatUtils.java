package net.teamcrimx.partyandfriends.api.chat;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionManagement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public class ChatUtils {

    public static Component getDisplayColor(UUID uuid) {
        PermissionManagement permissionManagement = InjectionLayer.ext().instance(PermissionManagement.class);
        var permissionUser = permissionManagement.user(uuid);

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

}
