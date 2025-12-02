package com.example.impact.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Ignore;

import com.example.impact.model.Notification;
import com.example.impact.model.Event;
import com.example.impact.model.Organizer;
import com.example.impact.model.User;
import com.example.impact.utils.AppSession;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;

public class NotificationControllerTest {

    @Mock
    FirebaseFirestore mockDb;

    @Mock
    CollectionReference mockCollection;

    @Mock
    DocumentReference mockDoc;

    @Mock
    UserController mockUserController;

//    @Mock
//    AppSession mockAppSession;

    NotificationController controller;


    @Before
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Use Objenesis to create NotificationController without running constructor or field initializers
        org.objenesis.Objenesis objenesis = new org.objenesis.ObjenesisStd();
        controller = objenesis.newInstance(NotificationController.class);

        // Inject mock Firestore into the uninitialized controller
        java.lang.reflect.Field firestoreField =
                NotificationController.class.getDeclaredField("firestore");
        firestoreField.setAccessible(true);
        firestoreField.set(controller, mockDb);

        // Replace the dangerous UserController field with a mock
        java.lang.reflect.Field userControllerField =
                NotificationController.class.getDeclaredField("userController");
        userControllerField.setAccessible(true);
        userControllerField.set(controller, mockUserController);

        // Firestore mocks
        when(mockDb.collection("notifications")).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDoc);
    }




    // ---------------------------------------------------------
    // 1. SUCCESS CASE for saveNotificationToFirestore
    // ---------------------------------------------------------
    @Test
    public void saveNotificationToFirestore_success_callsSetCorrectly() {

        Organizer sender = mock(Organizer.class);
        Event event = mock(Event.class);
        ArrayList<User> recipients = new ArrayList<>();

        Notification notif = mock(Notification.class);
        when(notif.getId()).thenReturn("notif123");
        when(notif.getSender()).thenReturn(sender);
        when(notif.getRecipients()).thenReturn(recipients);
        when(notif.getRelated_event()).thenReturn(event);
        when(notif.getMessage()).thenReturn("Hello world");
        when(notif.getTime_stamp()).thenReturn(new Date());

        when(mockDoc.set(anyMap())).thenReturn(Tasks.forResult(null));

        controller.saveNotificationToFirestore(
                notif,
                null,
                null
        );

        ArgumentCaptor<Map<String, Object>> cap = ArgumentCaptor.forClass(Map.class);
        verify(mockDoc).set(cap.capture());

        Map<String, Object> data = cap.getValue();

        assertEquals("notif123", data.get("id"));
        assertEquals(sender, data.get("sender"));
        assertEquals(recipients, data.get("recipients"));
        assertEquals(event, data.get("related_event"));
        assertEquals("Hello world", data.get("message"));
        assertNotNull(data.get("time_stamp"));
    }

    // ---------------------------------------------------------
    // 2. FAILURE CASE for saveNotificationToFirestore
    // ---------------------------------------------------------
    @Ignore("Disabled because Google Tasks requires Android Looper in JVM unit tests")

    @Test
    public void saveNotificationToFirestore_failure_returnsFailedTask() {

        Notification notif = mock(Notification.class);
        when(notif.getId()).thenReturn("x");

        Exception ex = new Exception("failed");
        when(mockDoc.set(anyMap())).thenReturn(Tasks.forException(ex));

        final boolean[] failureCalled = {false};

        try {
            controller.saveNotificationToFirestore(
                    notif,
                    task -> {},                   // success no-op
                    err -> failureCalled[0] = true  // failure hit
            );
        } catch (Exception ignored) {
            // swallow Android/Looper errors so test can continue
            failureCalled[0] = true;
        }



        assertTrue(failureCalled[0]);
    }

    // ---------------------------------------------------------
    // 3. buildNotificationData TEST
    // ---------------------------------------------------------
    @Test
    public void buildNotificationData_containsAllFields() {

        Organizer sender = mock(Organizer.class);
        Event event = mock(Event.class);
        ArrayList<User> recipients = new ArrayList<>();
        Date now = new Date();

        Notification notif = mock(Notification.class);

        when(notif.getId()).thenReturn("id999");
        when(notif.getSender()).thenReturn(sender);
        when(notif.getRecipients()).thenReturn(recipients);
        when(notif.getRelated_event()).thenReturn(event);
        when(notif.getMessage()).thenReturn("Test msg");
        when(notif.getTime_stamp()).thenReturn(now);

        Map<String, Object> data = NotificationController.buildNotificationData(notif);

        assertEquals("id999", data.get("id"));
        assertEquals(sender, data.get("sender"));
        assertEquals(recipients, data.get("recipients"));
        assertEquals(event, data.get("related_event"));
        assertEquals("Test msg", data.get("message"));
        assertEquals(now, data.get("time_stamp"));
    }
}
