package com.rudderstack.android.sdk.core.consent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;

import com.rudderstack.android.sdk.core.RudderConfig;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
//        consentFilterHandler.filterDestinationList()
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
    public void testApplyConsentWithConsentedMap() {
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

        ConsentFilterHandler consentFilterHandler = createConsentFilterHandler(destinations,
                new HashSet<>(Arrays.asList("d-3", "d-7","d-8", "d-9"))
        );
//        consentFilterHandler.filterDestinationList()
        RudderMessage updatedMessage = consentFilterHandler.applyConsent( rudderMessage);

        assertThat(updatedMessage, Matchers.hasProperty("userId", is("u")));
        assertThat(updatedMessage.getIntegrations(), allOf(hasEntry("All", true),
                hasEntry("d-1", false),
                hasEntry("d-3", false),
                hasEntry("d-4", true),
                hasEntry("d-7", false),
                hasEntry("d-8", false),
                hasEntry("d-9", false)
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
