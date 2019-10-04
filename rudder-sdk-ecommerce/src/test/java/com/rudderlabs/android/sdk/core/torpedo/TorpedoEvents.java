package com.rudderlabs.android.sdk.core.torpedo;

import com.rudderlabs.android.sdk.core.BaseTestCase;
import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderMessageBuilder;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.TrackPropertyBuilder;
import org.junit.Test;

public class TorpedoEvents extends BaseTestCase {

    @Test
    public void testUnverifiedRevenue() {
        try {
            RudderProperty property = new TrackPropertyBuilder().setCategory("unverified_revenue").build();
            property.setProperty("price", 4.9899997711);
            property.setProperty("productId", "piggy_bank_1");
            property.setProperty("quantity", 1);
            property.setProperty("revenue", 4.9899997711);
            property.setProperty("coin_balance", 12291645);
            property.setProperty("revenueType", "Android");
            property.setProperty("current_module_name", "CasinoGameModule");
            property.setProperty("fb_profile", "1");
            property.setProperty("game_fps", 30);
            property.setProperty("game_name", "JokerWheelSlots");
            property.setProperty("gem_balance", 6408);
            property.setProperty("graphicsQuality", "HD");
            property.setProperty("level", 65);
            property.setProperty("lifetime_gem_balance", 6408);
            property.setProperty("player_total_battles", 864);
            property.setProperty("player_total_shields", 1718);
            property.setProperty("total_payments", 34053);
            property.setProperty("start_date", "2018-11-07");
            property.setProperty("versionSessionCount", 99);
            RudderElement rudderElement = new RudderMessageBuilder().setEventName("unverified_revenue").setProperty(property).build();
//            rudderElement.addIntegration(RudderIntegrationPlatform.AMPLITUDE);
            rudderClient.track(rudderElement);
            rudderClient.flush();
        } catch (RudderException e) {
            e.printStackTrace();
        }
    }
}
