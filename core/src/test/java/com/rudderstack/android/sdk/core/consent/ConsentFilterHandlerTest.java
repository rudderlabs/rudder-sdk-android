package com.rudderstack.android.sdk.core.consent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;

import com.rudderstack.android.sdk.core.ReflectionUtils;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderContext;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderMessageBuilder;
import com.rudderstack.android.sdk.core.RudderOption;
import com.rudderstack.android.sdk.core.RudderServerConfigSource;
import com.rudderstack.android.sdk.core.RudderServerDestination;
import com.rudderstack.android.sdk.core.RudderServerDestinationDefinition;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConsentFilterHandlerTest {

    private ConsentFilterHandler createConsentFilterHandler( List<RudderServerDestination> serverConfigDestinations,
                                                            Set<String> rejectedDestinations){
        final RudderConsentFilter consentFilter = (destinations) -> destinations.stream()
                .map(RudderServerDestination::getDestinationDefinition)
                .map(RudderServerDestinationDefinition::getDisplayName)
                .collect(Collectors.toMap(Function.identity(), destinationName ->
                        ! rejectedDestinations.contains(destinationName)));
        return new ConsentFilterHandler(new RudderServerConfigSource(){
            @Override
            public List<RudderServerDestination> getDestinations() {
                return serverConfigDestinations;
            }
        }, consentFilter);
    }

    @Before
    public void setup() {
    }
    @After
    public void breakdown(){

    }


    @Test
    public void testApplyConsentWithEmptyDestinationList(){
        RudderOption option = new RudderOption();
        option.putIntegration("All", true);
        option.putIntegration("Firebase", false);
        option.putIntegration("Amplitude", true);
        RudderMessage rudderMessage = new RudderMessageBuilder()
                .setUserId("u")
                .setRudderOption(option)
                .build();
        ConsentFilterHandler consentFilterHandler = createConsentFilterHandler(Collections.emptyList(), Collections.emptySet());
        RudderMessage updatedMessage = consentFilterHandler.applyConsent( rudderMessage);

        assertThat(updatedMessage, Matchers.is(rudderMessage));
        assertThat(updatedMessage, Matchers.hasProperty("userId", is("u")));
        assertThat(updatedMessage.getIntegrations(), allOf(hasEntry("All", true),
                hasEntry("Firebase", false),
                hasEntry("Amplitude", true)
                ));
    }
    @Test
    public void testFilterDestinationsWithEmptyDestinationList(){
        ConsentFilterHandler consentFilterHandler = createConsentFilterHandler(Collections.emptyList(), Collections.emptySet());

        List<RudderServerDestination> destinations = IntStream.range(0, 10)
                .mapToObj(counter -> {
                    RudderServerDestinationDefinition rudderServerDestinationDefinitionMock =
                            Mockito.mock(RudderServerDestinationDefinition.class);
                    Mockito.doReturn("d-" + counter).when(rudderServerDestinationDefinitionMock).getDisplayName();
                    RudderServerDestination rudderServerDestinationMock = Mockito.mock(RudderServerDestination.class);
                    Mockito.doReturn(rudderServerDestinationDefinitionMock).when(rudderServerDestinationMock).getDestinationDefinition();
                    return rudderServerDestinationMock;
                }).collect(Collectors.toList());
        List<RudderServerDestination> filteredDestinations = consentFilterHandler.filterDestinationList(destinations);

        assertThat(filteredDestinations, is(destinations));
    }

    @Test
    public void testApplyConsentWithConsentedMap() throws NoSuchFieldException, IllegalAccessException {
        Map<String, Boolean> consentCategoriesMap = new HashMap<>(10);
        for (int i = 1; i <= 10; i++) {
            consentCategoriesMap.put("cat-" + i, i%2 == 0);
        }
        RudderConsentFilter consentFilter = new RudderConsentFilterWithCloudIntegration(){

            @Override
            public Map<String, Boolean> getConsentCategoriesMap() {
                return consentCategoriesMap;
            }

            @Override
            public Map<String, Boolean> filterConsentedDestinations(List<RudderServerDestination> destinationList) {
                return Collections.emptyMap();
            }
        };
        List<RudderServerDestination> destinations = IntStream.range(0, 10)
                .mapToObj(counter -> {
                    RudderServerDestinationDefinition rudderServerDestinationDefinitionMock =
                            Mockito.mock(RudderServerDestinationDefinition.class);
                    Mockito.doReturn("d-" + counter).when(rudderServerDestinationDefinitionMock).getDisplayName();
                    RudderServerDestination rudderServerDestinationMock = Mockito.mock(RudderServerDestination.class);
                    Mockito.doReturn(rudderServerDestinationDefinitionMock).when(rudderServerDestinationMock).getDestinationDefinition();
                    return rudderServerDestinationMock;
                }).collect(Collectors.toList());

        RudderOption option = new RudderOption();
        option.putIntegration("All", true);
        option.putIntegration("d-1", false);
        option.putIntegration("d-3", true);
        option.putIntegration("d-4", true);
        option.putIntegration("d-7", true);
        option.putIntegration("d-8", true);
        RudderMessage rudderMessage = new RudderMessageBuilder()
                .setUserId("u")
                .setRudderOption(option)
                .build();

        ConsentFilterHandler consentFilterHandler = new ConsentFilterHandler(new RudderServerConfigSource(){
            @Override
            public List<RudderServerDestination> getDestinations() {
                return destinations;
            }
        }, consentFilter);
        RudderMessage updatedMessage = consentFilterHandler.applyConsent( rudderMessage);

        assertThat(updatedMessage, Matchers.hasProperty("userId", is("u")));
        assertThat(updatedMessage.getIntegrations(), allOf(hasEntry("All", true),
                hasEntry("d-1", false),
                hasEntry("d-3", true),
                hasEntry("d-4", true),
                hasEntry("d-7", true),
                hasEntry("d-8", true)
        ));
        RudderContext updatedContext = updatedMessage.getContext();
        assertThat(updatedContext, hasProperty("consentManagement"));
        RudderContext.ConsentManagement consentManagement = ReflectionUtils.getObject(updatedContext, "consentManagement");
        List<String> deniedConsentIds = ReflectionUtils.getObject(consentManagement, "deniedConsentIds");
        assertThat(deniedConsentIds,
                allOf(
                        iterableWithSize(5),
                        hasItems("cat-1","cat-3","cat-5","cat-7","cat-9"),
                        not(hasItems("cat-2","cat-4","cat-6","cat-8","cat-10"))
                ));
    }
    @Test
    public void testFilteredDestinations(){
        List<String> rejectedDestinationDefinitionNames = Arrays.asList("d-2", "d-5", "d-7", "d-9", "d-11");

        List<RudderServerDestination> destinations = IntStream.range(0, 10)
                .mapToObj(counter -> {
                    RudderServerDestinationDefinition rudderServerDestinationDefinitionMock =
                            Mockito.mock(RudderServerDestinationDefinition.class);
                    Mockito.doReturn("d-" + counter).when(rudderServerDestinationDefinitionMock).getDisplayName();
                    RudderServerDestination rudderServerDestinationMock = Mockito.mock(RudderServerDestination.class);
                    Mockito.doReturn(rudderServerDestinationDefinitionMock).when(rudderServerDestinationMock).getDestinationDefinition();
                    return rudderServerDestinationMock;
                }).collect(Collectors.toList());
        ConsentFilterHandler consentFilterHandler = createConsentFilterHandler(destinations,
                new HashSet<>(rejectedDestinationDefinitionNames)
        );
        List<RudderServerDestination> filteredDestinations = consentFilterHandler.filterDestinationList(destinations);
        assertThat(filteredDestinations, iterableWithSize(6));
        for (RudderServerDestination destination: filteredDestinations) {
            assertThat(destination.getDestinationDefinition().getDisplayName(), not(isIn(rejectedDestinationDefinitionNames)));
        }
    }

}
