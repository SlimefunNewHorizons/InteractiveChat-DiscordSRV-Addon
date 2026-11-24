/*
 * This file is part of InteractiveChatDiscordSrvAddon2.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechatdiscordsrvaddon.listeners;

import com.loohp.interactivechatdiscordsrvaddon.InteractiveChatDiscordSrvAddon;
import com.loohp.interactivechatdiscordsrvaddon.debug.Debug;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.Permission;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.GuildChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

public class DiscordReadyEvents {

    private boolean init;
    private boolean closed;
    private JDA jda;
    private OutboundToDiscordEvents.JDAEvents outboundEvents;
    private DiscordInteractionEvents interactionEvents;
    private DiscordCommands discordCommands;

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onDiscordReady(DiscordReadyEvent event) {
        ready();
    }

    public synchronized void ready() {
        if (closed || init) {
            return;
        }
        init = true;
        Debug.debug("Triggering discord ready...");
        DiscordSRV discordsrv = InteractiveChatDiscordSrvAddon.discordsrv;
        jda = discordsrv.getJda();
        outboundEvents = new OutboundToDiscordEvents.JDAEvents();
        interactionEvents = new DiscordInteractionEvents();
        jda.addEventListener(outboundEvents, interactionEvents);

        discordCommands = new DiscordCommands(discordsrv);
        discordCommands.init();
        Bukkit.getPluginManager().registerEvents(discordCommands, InteractiveChatDiscordSrvAddon.plugin);
        DiscordSRV.api.addSlashCommandProvider(discordCommands);
        discordCommands.reload();

        for (String channelId : discordsrv.getChannels().values()) {
            if (channelId != null) {
                try {
                    GuildChannel channel = jda.getGuildChannelById(channelId);
                    if (channel != null) {
                        Guild guild = channel.getGuild();
                        Member self = guild.getMember(jda.getSelfUser());
                        for (Permission permission : InteractiveChatDiscordSrvAddon.requiredPermissions) {
                            if (!self.hasPermission(channel, permission)) {
                                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[ICDiscordSrvAddon] DiscordSRV Bot is missing the \"" + permission.getName() + "\" permission in the channel \"" + channel.getName() + "\" (Id: " + channel.getId() + ")");
                            }
                        }
                    }
                } catch (Exception e) {
                    new RuntimeException("Error when getting guild from channelId (" + channelId + ")", e).printStackTrace();
                }
            }
        }
    }

    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (outboundEvents != null) {
            // Drain any current callback before its classloader can be closed.
            outboundEvents.close();
        }
        if (jda != null) {
            jda.removeEventListener(outboundEvents, interactionEvents);
        }
        if (discordCommands != null) {
            DiscordSRV.api.removeSlashCommandProvider(discordCommands);
        }
    }

}
