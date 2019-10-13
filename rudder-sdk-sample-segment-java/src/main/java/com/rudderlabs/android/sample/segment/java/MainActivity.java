package com.rudderlabs.android.sample.segment.java;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.rudderlabs.android.sdk.core.RudderClient;
import com.rudderlabs.android.sdk.core.RudderProperty;
import com.rudderlabs.android.sdk.core.RudderTraits;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RudderTraits traits = new RudderTraits();
        traits.putBirthday(new Date());
        traits.putEmail("abc@123.com");
        traits.putFirstName("First");
        traits.putLastName("Last");
        traits.putGender("m");
        traits.putPhone("5555555555");
        RudderTraits.Address address = new RudderTraits.Address();
        address.putCity("City");
        address.putCountry("USA");
        traits.putAddress(address);
        traits.put("boolean", new Boolean(true));
        traits.put("integer", new Integer(50));
        traits.put("float", new Float(120.4));
        traits.put("long", new Long(1234L));
        traits.put("string", "hello");
        traits.put("date", new Date(System.currentTimeMillis()));

        RudderClient.with(this).identify("some_user_id", traits, null);

        String customEvent = "some_custom_event";
        String propertyKey = "some_property_key";
        String propertyValue = "some_property_value";
        RudderClient.with(this).track(customEvent, new RudderProperty().putValue(propertyKey, propertyValue));

        RudderProperty purchaseProperties = new RudderProperty();
        purchaseProperties.put("property_key", "property_value");
        purchaseProperties.putRevenue(10.0);
        purchaseProperties.putCurrency("JPY");
        RudderClient.with(this).track("custom_purchase", purchaseProperties);
        RudderClient.with(this).track("Install Attributed", new RudderProperty()
                .putValue("provider", "Tune/Kochava/Branch")
                .putValue("campaign", new RudderProperty()
                        .putValue("source", "Network/FB/AdWords/MoPub/Source")
                        .putValue("name", "Campaign Name")
                        .putValue("content", "Organic Content Title")
                        .putValue("ad_creative", "Red Hello World Ad")
                        .putValue("ad_group", "Red Ones")));
    }
}
