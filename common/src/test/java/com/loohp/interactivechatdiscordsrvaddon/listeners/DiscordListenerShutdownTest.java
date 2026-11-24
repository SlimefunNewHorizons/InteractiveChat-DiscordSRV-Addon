package com.loohp.interactivechatdiscordsrvaddon.listeners;

import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class DiscordListenerShutdownTest {

    // Reflective close also lets these regressions run against the previous binary.
    private static void close(Object listener) throws Exception {
        listener.getClass().getMethod("close").invoke(listener);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void readyEventAfterShutdownDoesNotInitializeDiscord() throws Exception {
        DiscordReadyEvents ready = new DiscordReadyEvents();
        close(ready);

        // No Bukkit/Discord connection exists here: initialization would fail.
        assertDoesNotThrow(() -> ready.onDiscordReady(null));
        Field init = DiscordReadyEvents.class.getDeclaredField("init");
        init.setAccessible(true);
        assertFalse(init.getBoolean(ready));
    }

    @Test
    void repeatedReadyDoesNotInitializeAgain() throws Exception {
        DiscordReadyEvents ready = new DiscordReadyEvents();
        set(ready, "init", true);

        assertDoesNotThrow(ready::ready);
    }

    @Test
    void queuedMessageAfterShutdownDoesNotAccessPluginOrEvent() throws Exception {
        OutboundToDiscordEvents.JDAEvents listener = new OutboundToDiscordEvents.JDAEvents();
        close(listener);
        close(listener);

        // A callback already queued by JDA must return before touching either.
        assertDoesNotThrow(() -> listener.onMessageReceived(null));
    }

    @Test
    void shutdownRemovesRetainedJdaListenersExactlyOnce() throws Exception {
        List<Object[]> removals = new ArrayList<>();
        JDA jda = (JDA) Proxy.newProxyInstance(JDA.class.getClassLoader(), new Class<?>[]{JDA.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("removeEventListener")) {
                        removals.add((Object[]) arguments[0]);
                        return null;
                    }
                    throw new AssertionError("Unexpected JDA call: " + method.getName());
                });
        DiscordReadyEvents ready = new DiscordReadyEvents();
        OutboundToDiscordEvents.JDAEvents outbound = new OutboundToDiscordEvents.JDAEvents();
        set(ready, "jda", jda);
        set(ready, "outboundEvents", outbound);

        close(ready);
        close(ready);

        assertEquals(1, removals.size());
        assertSame(outbound, removals.get(0)[0]);
        assertDoesNotThrow(() -> outbound.onMessageReceived(null));
    }
}
