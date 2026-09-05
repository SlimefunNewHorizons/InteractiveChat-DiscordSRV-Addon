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

package com.loohp.interactivechatdiscordsrvaddon.resources;

import com.loohp.interactivechat.objectholders.ValuePairs;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.TintColorProvider;
import com.loohp.interactivechatdiscordsrvaddon.resources.ModelRenderer.PlayerModelItem;
import com.loohp.interactivechatdiscordsrvaddon.resources.ModelRenderer.PlayerModelItemPosition;
import com.loohp.interactivechatdiscordsrvaddon.resources.models.BlockModel;
import com.loohp.interactivechatdiscordsrvaddon.resources.textures.TextureResource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelRendererPlayerModelItemTest {

    @Test
    void replacesNullAndMissingPostResolveFunctionsWithDefault() {
        ModelLayer firstLayer = layer();
        ModelLayer secondLayer = layer();
        PlayerModelItem item = new PlayerModelItem(
                PlayerModelItemPosition.HELMET,
                Arrays.asList(firstLayer, secondLayer),
                Collections.singletonList(null),
                false,
                null,
                null
        );

        assertEquals(2, item.getPostResolveFunction().size());
        for (Function<BlockModel, ValuePairs<BlockModel, Map<String, TextureResource>>> function : item.getPostResolveFunction()) {
            assertNotNull(function);
            ValuePairs<BlockModel, Map<String, TextureResource>> result = function.apply(null);
            assertNull(result.getFirst());
            assertTrue(result.getSecond().isEmpty());
        }
    }

    private ModelLayer layer() {
        return new ModelLayer(null, Collections.emptyMap(), Collections.emptyMap(), TintColorProvider.EMPTY_INSTANCE, null);
    }

}
