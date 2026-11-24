package com.loohp.interactivechatdiscordsrvaddon.resources.definitions.item;

import com.loohp.interactivechat.libs.org.json.simple.JSONObject;
import com.loohp.interactivechat.libs.org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemModelDefinitionComponentConditionTest {

    @Test
    void preservesNestedPredicateObjectAndBranches() throws Exception {
        String value = "{\"damage\":{\"min\":1},\"durability\":{\"max\":100}}";
        ItemModelDefinition.ComponentConditionProperty condition = parse("minecraft:damage", value);

        assertEquals(new JSONParser().parse(value), new JSONParser().parse(condition.getValue()));
        assertEquals("minecraft:damage", condition.getPredicate());
        assertEquals("minecraft:item/diamond", ((ItemModelDefinition.ItemModelDefinitionModel) condition.getModel(true)).getModel());
        assertEquals("minecraft:item/stone", ((ItemModelDefinition.ItemModelDefinitionModel) condition.getModel(false)).getModel());
    }

    @Test
    void preservesLegacySnbtStringWithoutAddingQuotesOrEscaping() throws Exception {
        String value = "{name:\"a\\b\",flag:1b}";
        JSONObject model = model("minecraft:custom_data", "null");
        model.put("value", value);

        assertEquals(value, ((ItemModelDefinition.ComponentConditionProperty) ItemModelDefinition.fromJson(model)).getValue());
    }

    @Test
    void preservesArrayPredicateValueAsJson() throws Exception {
        String value = "[{\"enchantments\":\"minecraft:sharpness\",\"levels\":{\"min\":1}}]";

        assertEquals(new JSONParser().parse(value), new JSONParser().parse(parse("minecraft:enchantments", value).getValue()));
    }

    @Test
    void preservesBooleanValueWithoutStringCast() throws Exception {
        assertEquals("true", parse("example:boolean", "true").getValue());
        assertEquals("false", parse("example:boolean", "false").getValue());
    }

    @Test
    void preservesNumericValueWithoutStringCast() throws Exception {
        assertEquals("12", parse("example:number", "12").getValue());
    }

    @Test
    void keepsExistingNullBehaviorWithoutInventingLiteralNull() throws Exception {
        // This checks the parser contract, not whether a predicate codec accepts null.
        assertNull(parse("minecraft:custom_data", "null").getValue());
        JSONObject model = model("minecraft:custom_data", "null");
        model.remove("value");
        assertNull(((ItemModelDefinition.ComponentConditionProperty) ItemModelDefinition.fromJson(model)).getValue());
    }

    private ItemModelDefinition.ComponentConditionProperty parse(String predicate, String value) throws Exception {
        return (ItemModelDefinition.ComponentConditionProperty) ItemModelDefinition.fromJson(model(predicate, value));
    }

    private JSONObject model(String predicate, String value) throws Exception {
        return (JSONObject) new JSONParser().parse("{\"type\":\"minecraft:condition\",\"property\":\"minecraft:component\","
                + "\"predicate\":\"" + predicate + "\",\"value\":" + value + ","
                + "\"on_true\":{\"type\":\"minecraft:model\",\"model\":\"minecraft:item/diamond\"},"
                + "\"on_false\":{\"type\":\"minecraft:model\",\"model\":\"minecraft:item/stone\"}}");
    }
}
