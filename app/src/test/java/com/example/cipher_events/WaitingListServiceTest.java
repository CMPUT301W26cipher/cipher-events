package com.example.cipher_events;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.example.cipher_events.waitinglist.WaitingListService;
import com.example.cipher_events.user.UserEventHistoryRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WaitingListServiceTest {

    private WaitingListService waitingListService;
    private UserEventHistoryRepository historyRepository;

    private Event event;
    private User user1;
    private User user2;

    @Before
    public void setUp() {

        // Mock repository to avoid Firebase / DB calls
        historyRepository = mock(UserEventHistoryRepository.class);

        waitingListService = new WaitingListService(historyRepository);

        event = new Event(
                "Lucky Draw Event",
                "Test lottery event",
                "2026-04-01 18:00",
                "Edmonton",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                null
        );

        user1 = new User(
                "Alice",
                "alice@example.com",
                "pass123",
                "7801111111",
                null
        );

        user2 = new User(
                "Bob",
                "bob@example.com",
                "pass456",
                "7802222222",
                null
        );
    }

    // =========================================================
    // US 01.01.01
    // Join Waiting List
    // =========================================================

    @Test
    public void testJoinWaitingList_validUser_addedSuccessfully() {

        boolean result = waitingListService.joinWaitingList(user1, event);

        assertTrue(result);
        assertEquals(1, event.getEntrants().size());
        assertEquals(user1, event.getEntrants().get(0));
    }

    @Test
    public void testJoinWaitingList_duplicateUser_returnsFalse() {

        waitingListService.joinWaitingList(user1, event);
        boolean result = waitingListService.joinWaitingList(user1, event);

        assertFalse(result);
        assertEquals(1, event.getEntrants().size());
    }

    @Test
    public void testJoinWaitingList_capacityReached_returnsFalse() {

        event.setWaitingListCapacity(1);

        waitingListService.joinWaitingList(user1, event);
        boolean result = waitingListService.joinWaitingList(user2, event);

        assertFalse(result);
        assertEquals(1, event.getEntrants().size());
    }

    // =========================================================
    // US 01.01.02
    // Leave Waiting List
    // =========================================================

    @Test
    public void testLeaveWaitingList_existingUser_removedSuccessfully() {

        waitingListService.joinWaitingList(user1, event);

        boolean removed = waitingListService.leaveWaitingList(user1, event);

        assertTrue(removed);
        assertEquals(0, event.getEntrants().size());
    }

    @Test
    public void testLeaveWaitingList_userNotInList_returnsFalse() {

        boolean removed = waitingListService.leaveWaitingList(user1, event);

        assertFalse(removed);
    }

    // =========================================================
    // US 02.02.01
    // View Waiting List
    // =========================================================

    @Test
    public void testGetWaitingList_returnsCorrectUsers() {

        waitingListService.joinWaitingList(user1, event);
        waitingListService.joinWaitingList(user2, event);

        List<User> list = waitingListService.getWaitingList(event);

        assertEquals(2, list.size());
        assertTrue(list.contains(user1));
        assertTrue(list.contains(user2));
    }

    // =========================================================
    // US 01.05.04
    // View Waiting List Count
    // =========================================================

    @Test
    public void testGetWaitingListCount_returnsCorrectCount() {

        waitingListService.joinWaitingList(user1, event);
        waitingListService.joinWaitingList(user2, event);

        int count = waitingListService.getWaitingListCount(event);

        assertEquals(2, count);
    }

    @Test
    public void testGetWaitingListCount_emptyList_returnsZero() {

        int count = waitingListService.getWaitingListCount(event);

        assertEquals(0, count);
    }

    // =========================================================
    // US 02.03.01
    // Set Waiting List Capacity
    // =========================================================

    @Test
    public void testSetWaitingListCapacity_validCapacity_setSuccessfully() {

        waitingListService.setWaitingListCapacity(event, 5);

        assertEquals(Integer.valueOf(5), event.getWaitingListCapacity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWaitingListCapacity_negativeCapacity_throwsException() {

        waitingListService.setWaitingListCapacity(event, -1);
    }

    // =========================================================
    // US 02.06.01
    // View Invited Entrants List
    // =========================================================

    @Test
    public void testGetInvitedEntrants_returnsCorrectUsers() {
        ArrayList<User> invited = new ArrayList<>();
        invited.add(user1);
        invited.add(user2);
        event.setInvitedEntrants(invited);

        List<User> result = waitingListService.getInvitedEntrants(event);

        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
    }

    @Test
    public void testGetInvitedEntrants_emptyList_returnsEmpty() {
        event.setInvitedEntrants(new ArrayList<>());

        List<User> result = waitingListService.getInvitedEntrants(event);

        assertEquals(0, result.size());
    }

    // =========================================================
    // US 02.06.02
    // View Cancelled Entrants List
    // =========================================================

    @Test
    public void testGetCancelledEntrants_returnsCorrectUsers() {
        ArrayList<User> cancelled = new ArrayList<>();
        cancelled.add(user1);
        event.setCancelledEntrants(cancelled);

        List<User> result = waitingListService.getCancelledEntrants(event);

        assertEquals(1, result.size());
        assertTrue(result.contains(user1));
    }

    @Test
    public void testGetCancelledEntrants_emptyList_returnsEmpty() {
        event.setCancelledEntrants(new ArrayList<>());

        List<User> result = waitingListService.getCancelledEntrants(event);

        assertEquals(0, result.size());
    }

    // =========================================================
    // US 02.06.03
    // View Final Enrolled List
    // =========================================================

    @Test
    public void testGetEnrolledEntrants_returnsCorrectUsers() {
        ArrayList<User> enrolled = new ArrayList<>();
        enrolled.add(user1);
        enrolled.add(user2);
        event.setEnrolledEntrants(enrolled);

        List<User> result = waitingListService.getEnrolledEntrants(event);

        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
    }

    @Test
    public void testGetEnrolledEntrants_emptyList_returnsEmpty() {
        event.setEnrolledEntrants(new ArrayList<>());

        List<User> result = waitingListService.getEnrolledEntrants(event);

        assertEquals(0, result.size());
    }

    // =========================================================
    // US 02.06.04
    // Cancel Non-Responsive Entrants
    // =========================================================

    @Test
    public void testMarkAsNoShow_existingInvitedUser_movedToCancelled() {
        ArrayList<User> invited = new ArrayList<>();
        invited.add(user1);
        event.setInvitedEntrants(invited);
        event.setCancelledEntrants(new ArrayList<>());

        boolean result = waitingListService.markAsNoShow(user1, event);

        assertTrue(result);
        assertEquals(0, event.getInvitedEntrants().size());
        assertEquals(1, event.getCancelledEntrants().size());
        assertTrue(event.getCancelledEntrants().contains(user1));
    }

    @Test
    public void testMarkAsNoShow_userNotInvited_returnsFalse() {
        event.setInvitedEntrants(new ArrayList<>());
        event.setCancelledEntrants(new ArrayList<>());

        boolean result = waitingListService.markAsNoShow(user1, event);

        assertFalse(result);
    }

}