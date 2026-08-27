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

package com.loohp.interactivechatdiscordsrvaddon.utils;

import com.ibm.icu.util.TimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Regresion del ticket QA (b27e3af): ItemModelDefinition.LocalTimeSelectProperty.getTimeZone()
 * puede devolver una instancia de com.ibm.icu.util.TimeZone cargada por un classloader distinto
 * al de ItemRenderUtils (jar hibrido 2026.1.1.0, ver relocation de com.ibm.icu en common/pom.xml).
 * Un cast directo a esa clase lanza ClassCastException y la zona horaria configurada se perdia
 * silenciosamente. resolveForeignTimeZone debe recuperarla via reflexion sin depender de la
 * identidad exacta de la clase.
 */
class ItemRenderUtilsTimeZoneTest {

    private URLClassLoader isolatedLoader;

    @AfterEach
    void closeLoader() throws Exception {
        if (isolatedLoader != null) {
            isolatedLoader.close();
        }
    }

    @Test
    void resolvesTimeZoneFromForeignClassloaderInstance() throws Exception {
        String timeZoneId = "America/Santiago";
        Object foreignTimeZone = loadTimeZoneFromIsolatedClassloader(timeZoneId);

        assertNotSameClassAsLocal(foreignTimeZone);

        TimeZone resolved = ItemRenderUtils.resolveForeignTimeZone(foreignTimeZone);

        assertEquals(TimeZone.getTimeZone(timeZoneId).getClass(), resolved.getClass());
        assertEquals(timeZoneId, resolved.getID());
    }

    @Test
    void resolvesTimeZoneFromSameClassloaderInstance() {
        TimeZone local = TimeZone.getTimeZone("UTC");

        TimeZone resolved = ItemRenderUtils.resolveForeignTimeZone(local);

        assertEquals("UTC", resolved.getID());
    }

    @Test
    void returnsNullWhenObjectHasNoGetIdMethod() {
        assertNull(ItemRenderUtils.resolveForeignTimeZone(new Object()));
    }

    @Test
    void returnsNullWhenInputIsNull() {
        assertNull(ItemRenderUtils.resolveForeignTimeZone(null));
    }

    private void assertNotSameClassAsLocal(Object foreignTimeZone) {
        if (foreignTimeZone.getClass() == TimeZone.class) {
            throw new AssertionError("El classloader aislado debia producir una clase TimeZone distinta a la local");
        }
    }

    private Object loadTimeZoneFromIsolatedClassloader(String timeZoneId) throws Exception {
        CodeSource codeSource = TimeZone.class.getProtectionDomain().getCodeSource();
        URL icu4jJarUrl = codeSource.getLocation();

        isolatedLoader = new URLClassLoader(new URL[]{icu4jJarUrl}, ClassLoader.getSystemClassLoader().getParent());
        Class<?> foreignTimeZoneClass = Class.forName("com.ibm.icu.util.TimeZone", true, isolatedLoader);

        return foreignTimeZoneClass.getMethod("getTimeZone", String.class).invoke(null, timeZoneId);
    }

}
