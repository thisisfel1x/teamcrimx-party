package net.teamcrimx.party.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class ChatConstants {

    public static Component partyPrefix = Component.text("Party > ", TextColor.fromHexString("#C800C8"))
            .append(Component.empty().color(NamedTextColor.WHITE));

}
