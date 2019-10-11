package com.rudderlabs.android.sdk.core.torpedo;

import com.rudderlabs.android.sdk.core.BaseTestCase;
import com.rudderlabs.android.sdk.core.RudderMessageBuilder;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.TrackPropertyBuilder;
import org.junit.Test;

public class TorpedoEvents extends BaseTestCase {

    @Test
    public void testUnverifiedRevenue() {
        try {
            RudderProperty property = new TrackPropertyBuilder().setCategory("unverified_revenue").build();
            property.putValue("price", 4.9899997711);
            property.putValue("productId", "piggy_bank_1");
            property.putValue("quantity", 1);
            property.putValue("revenue", 4.9899997711);
            property.putValue("coin_balance", 12291645);
            property.putValue("revenueType", "Android");
            property.putValue("current_module_name", "CasinoGameModule");
            property.putValue("fb_profile", "1");
            property.putValue("game_fps", 30);
            property.putValue("game_name", "JokerWheelSlots");
            property.putValue("gem_balance", 6408);
            property.putValue("graphicsQuality", "HD");
            property.putValue("level", 65);
            property.putValue("lifetime_gem_balance", 6408);
            property.putValue("player_total_battles", 864);
            property.putValue("player_total_shields", 1718);
            property.putValue("total_payments", 34053);
            property.putValue("start_date", "2018-11-07");
            property.putValue("versionSessionCount", 99);
            RudderElement rudderElement = new RudderMessageBuilder().setEventName("unverified_revenue").setProperty(property).build();
//            rudderElement.addIntegration(RudderIntegrationPlatform.AMPLITUDE);
            rudderClient.track(rudderElement);
            rudderClient.flush();
        } catch (RudderException e) {
            e.printStackTrace();
        }
    }
}
