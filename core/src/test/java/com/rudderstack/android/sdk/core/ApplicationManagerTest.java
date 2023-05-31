package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.text.TextUtils;

import androidx.test.core.app.ApplicationProvider;

import com.rudderstack.android.sdk.core.util.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ApplicationLifeCycleManager.class})
public class ApplicationManagerTest {
    ApplicationLifeCycleManager applicationLifeCycleManager;
    RudderUserSession userSession;

    @Before
    public void setup() throws Exception {
        userSession = PowerMockito.mock(RudderUserSession.class);
        PowerMockito.whenNew(RudderUserSession.class)
                .withAnyArguments().thenReturn(userSession);
        RudderConfig mockConfig = PowerMockito.mock(RudderConfig.class);
        Application application = Mockito.mock(Application.class);
        PowerMockito.doReturn(false).when(mockConfig).isTrackLifecycleEvents();
        RudderPreferenceManager preferenceManager =
                Mockito.mock(RudderPreferenceManager.class);
        PowerMockito.when(preferenceManager.getBuildNumber()).thenAnswer(
                (Answer<Integer>) invocation -> 2);
        PowerMockito.when(preferenceManager.getOptStatus()).thenAnswer(
                (Answer<Boolean>) invocation -> false);


        EventRepository repo = PowerMockito.spy(new EventRepository());
        PowerMockito.doReturn(false).when(repo).getOptStatus();

        applicationLifeCycleManager = new ApplicationLifeCycleManager(
                preferenceManager,
                repo, new RudderFlushWorkManager(application, mockConfig, preferenceManager),
                mockConfig,
                application
        );
        applicationLifeCycleManager.startSessionTracking();
    }

    @Test
    public void applySessionTracking() throws Exception {
        long testSessionId = 123L;


        applicationLifeCycleManager.startSession(testSessionId);
//        applicationLifeCycleManager.applySessionTracking(spyMessage);
//        Mockito.verify(spyMessage).setSession(userSession);
        Mockito.verify(userSession).startSession(testSessionId);

        PowerMockito.doReturn(testSessionId).when(userSession).getSessionId();
        RudderMessage message = new RudderMessageBuilder().setUserId("u-1").build();
        RudderMessage spyMessage = PowerMockito.spy(message);
//        PowerMockito.doAnswer((Answer<Void>) invocation -> null).when(spyMessage).setSession(Mockito.any());
        PowerMockito.doNothing().when(spyMessage).setSession(userSession);
        applicationLifeCycleManager.applySessionTracking(spyMessage);
        Mockito.verify(spyMessage).setSession(userSession);
    }

    @After
    public void destroy() {
        Mockito.clearInvocations();
    }
}
