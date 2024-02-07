package com.rudderstack.android.sdk.core;

import static org.hamcrest.MatcherAssert.assertThat;

import com.rudderstack.android.sdk.core.util.Utils;

import org.junit.Test;

import java.util.List;
import java.util.Map;

public class UtilsTest {
    @Test
    public void testStringConversiontToMap() {
        String rudderTraits = "{\"dirty\":{\"general\":45.0,\"minValue\":4.9E-324,\"maxValue\":1.7976931348623157E308,\"double\":45.0,\"positiveInfinity\":\"Infinity\",\"nan\":\"NaN\",\"float\":45.0,\"list\":[\"Infinity\",\"-Infinity\",1.7976931348623157E308,4.9E-324,\"NaN\",45.0,45.0],\"map\":{\"general\":45.0,\"minValue\":4.9E-324,\"maxValue\":1.7976931348623157E308,\"double\":45.0,\"positiveInfinity\":\"Infinity\",\"nan\":\"NaN\",\"negativeInfinity\":\"-Infinity\"},\"long\":45.0,\"int\":45.0,\"negativeInfinity\":\"-Infinity\"},\"anonymousId\":\"c5ee2cf1-97a3-4744-80fc-faabce7a7e51\",\"name\":\"Mr. User1\",\"id\":\"new user 2\",\"userId\":\"new user 2\",\"email\":\"user1@gmail.com\"}";
        Map<String,Object> map = Utils.convertToMap(rudderTraits);
        assertThat("Map should not be empty", map.size() > 0);
    }

    @Test
    public void testStringConversionToArray() {
        String externalIds = "[{\"id\":\"idValue\",\"type\":\"idTYpe\"}]";
        List<Map<String, Object>> list = Utils.convertToList(externalIds);
        assertThat("List should not be empty", list.size() > 0);
    }
}
