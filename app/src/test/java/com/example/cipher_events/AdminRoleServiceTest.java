package com.example.cipher_events;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.cipher_events.admin.AdminRoleService;
import com.example.cipher_events.database.Admin;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;

import org.junit.Before;
import org.junit.Test;

public class AdminRoleServiceTest {

    private DBProxy dbProxy;
    private AdminRoleService adminRoleService;

    @Before
    public void setUp() {
        dbProxy = mock(DBProxy.class);
        adminRoleService = new AdminRoleService(dbProxy);
    }

    private Admin createTestAdmin() {
        Admin admin = new Admin();
        admin.setDeviceID("admin-1");
        admin.setName("Admin One");
        admin.setEmail("admin@example.com");
        admin.setPassword("pass123");
        admin.setPhoneNumber("7801234567");
        admin.setProfilePictureURL(null);
        return admin;
    }

    @Test
    public void testEnableEntrantRole_whenUserMissing_createsUserRole() {
        Admin admin = createTestAdmin();

        when(dbProxy.getAdmin(admin.getDeviceID())).thenReturn(admin);
        when(dbProxy.getUser(admin.getDeviceID())).thenReturn(null);

        User created = adminRoleService.enableEntrantRole(admin.getDeviceID());

        assertNotNull(created);
        assertEquals(admin.getDeviceID(), created.getDeviceID());
        verify(dbProxy, times(1)).addUser(any(User.class));
    }

    @Test
    public void testEnableEntrantRole_whenUserExists_updatesUserRole() {
        Admin admin = createTestAdmin();
        User existing = new User();
        existing.setDeviceID(admin.getDeviceID());
        existing.setName("Old");
        existing.setEmail("old@example.com");
        existing.setPassword("pass");
        existing.setPhoneNumber("111");
        existing.setProfilePictureURL(null);

        when(dbProxy.getAdmin(admin.getDeviceID())).thenReturn(admin);
        when(dbProxy.getUser(admin.getDeviceID())).thenReturn(existing);

        User result = adminRoleService.enableEntrantRole(admin.getDeviceID());

        assertEquals(admin.getDeviceID(), result.getDeviceID());
        verify(dbProxy, times(1)).updateUser(existing);
    }

    @Test
    public void testEnableOrganizerRole_whenOrganizerMissing_createsOrganizerRole() {
        Admin admin = createTestAdmin();


        when(dbProxy.getAdmin(admin.getDeviceID())).thenReturn(admin);
        when(dbProxy.getOrganizer(admin.getDeviceID())).thenReturn(null);

        Organizer created = adminRoleService.enableOrganizerRole(admin.getDeviceID());

        assertNotNull(created);
        assertEquals(admin.getDeviceID(), created.getDeviceID());
        verify(dbProxy, times(1)).addOrganizer(any(Organizer.class));
    }

    @Test
    public void testEnableOrganizerRole_whenOrganizerExists_updatesOrganizerRole() {
        Admin admin = createTestAdmin();
        Organizer existing = new Organizer();
        existing.setDeviceID(admin.getDeviceID());
        existing.setName("Old Org");
        existing.setEmail("old@example.com");
        existing.setPassword("pass");
        existing.setPhoneNumber("111");
        existing.setProfilePictureURL(null);

        when(dbProxy.getAdmin(admin.getDeviceID())).thenReturn(admin);
        when(dbProxy.getOrganizer(admin.getDeviceID())).thenReturn(existing);

        Organizer result = adminRoleService.enableOrganizerRole(admin.getDeviceID());

        assertEquals(admin.getDeviceID(), result.getDeviceID());
        verify(dbProxy, times(1)).updateOrganizer(existing);
    }

    @Test
    public void testDisableEntrantRole_existingUser_deletesUserRole() {
        Admin admin = createTestAdmin();
        User existing = new User();
        existing.setDeviceID(admin.getDeviceID());
        existing.setName("Old");
        existing.setEmail("old@example.com");
        existing.setPassword("pass");
        existing.setPhoneNumber("111");
        existing.setProfilePictureURL(null);

        when(dbProxy.getAdmin(admin.getDeviceID())).thenReturn(admin);
        when(dbProxy.getUser(admin.getDeviceID())).thenReturn(existing);

        adminRoleService.disableEntrantRole(admin.getDeviceID());

        verify(dbProxy, times(1)).deleteUser(admin.getDeviceID());
    }

    @Test
    public void testDisableOrganizerRole_existingOrganizer_deletesOrganizerRole() {
        Admin admin = createTestAdmin();
        Organizer existing = new Organizer();
        existing.setDeviceID(admin.getDeviceID());
        existing.setName("Old Org");
        existing.setEmail("old@example.com");
        existing.setPassword("pass");
        existing.setPhoneNumber("111");
        existing.setProfilePictureURL(null);

        when(dbProxy.getAdmin(admin.getDeviceID())).thenReturn(admin);
        when(dbProxy.getOrganizer(admin.getDeviceID())).thenReturn(existing);

        adminRoleService.disableOrganizerRole(admin.getDeviceID());

        verify(dbProxy, times(1)).deleteOrganizer(admin.getDeviceID());
    }

    @Test
    public void testHasEntrantRole_existingUser_returnsTrue() {
        Admin admin = createTestAdmin();
        User existing = new User();
        existing.setDeviceID(admin.getDeviceID());
        existing.setName("Old");
        existing.setEmail("old@example.com");
        existing.setPassword("pass");
        existing.setPhoneNumber("111");
        existing.setProfilePictureURL(null);

        when(dbProxy.getAdmin(admin.getDeviceID())).thenReturn(admin);
        when(dbProxy.getUser(admin.getDeviceID())).thenReturn(existing);

        assertTrue(adminRoleService.hasEntrantRole(admin.getDeviceID()));
    }

    @Test
    public void testHasOrganizerRole_existingOrganizer_returnsTrue() {
        Admin admin = createTestAdmin();
        Organizer existing = new Organizer();
        existing.setDeviceID(admin.getDeviceID());
        existing.setName("Old Org");
        existing.setEmail("old@example.com");
        existing.setPassword("pass");
        existing.setPhoneNumber("111");
        existing.setProfilePictureURL(null);

        when(dbProxy.getAdmin(admin.getDeviceID())).thenReturn(admin);
        when(dbProxy.getOrganizer(admin.getDeviceID())).thenReturn(existing);

        assertTrue(adminRoleService.hasOrganizerRole(admin.getDeviceID()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnableEntrantRole_missingAdmin_throwsException() {
        when(dbProxy.getAdmin("missing-admin")).thenReturn(null);
        adminRoleService.enableEntrantRole("missing-admin");
    }
}