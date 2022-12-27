package app.nzyme.core.bandits.engine;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.Role;
import app.nzyme.core.bandits.Bandit;
import app.nzyme.core.bandits.Contact;
import app.nzyme.core.bandits.DefaultBandits;
import app.nzyme.core.bandits.identifiers.BanditIdentifier;
import app.nzyme.core.bandits.identifiers.FingerprintBanditIdentifier;
import app.nzyme.core.bandits.identifiers.SSIDIBanditdentifier;
import app.nzyme.core.bandits.identifiers.SignalStrengthBanditIdentifier;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.parsers.Dot11BeaconFrameParser;
import app.nzyme.core.dot11.parsers.Frames;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.testng.Assert.*;

public class ContactIdentifierTest {

    protected static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, -10, 2400, 1, 0L, false);

    @BeforeMethod
    public void cleanDatabase() {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM bandits"));
    }

    @Test
    public void testRegisterBandit() {
        ContactManager i = new ContactManager(new MockNzyme());

        UUID uuid1 = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, uuid1, "foo", "foo", false, DateTime.now(), DateTime.now(), Lists.newArrayList()));
        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size()+1);

        UUID uuid2 = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, uuid2, "foo", "foo", false, DateTime.now(), DateTime.now(), Lists.newArrayList()));
        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size()+2);

        assertNotNull(i.getBandits().get(uuid1));
        assertNotNull(i.getBandits().get(uuid2));
    }

    @Test
    public void testRemoveBandit() {
        ContactManager i = new ContactManager(new MockNzyme());

        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size());

        UUID uuid1 = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, uuid1, "foo", "foo", false, DateTime.now(), DateTime.now(), Lists.newArrayList()));
        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size()+1);

        UUID uuid2 = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, uuid2, "foo", "foo", false, DateTime.now(), DateTime.now(), Lists.newArrayList()));
        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size()+2);

        assertNotNull(i.getBandits().get(uuid1));
        assertNotNull(i.getBandits().get(uuid2));

        i.removeBandit(uuid1);

        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size()+1);
        assertNull(i.getBandits().get(uuid1));
        assertNotNull(i.getBandits().get(uuid2));
    }

    @Test
    public void testRegisterContact() throws Exception {
        ContactManager i = new ContactManager(new MockNzyme());

        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size());
        assertEquals(i.findContacts().size(), 0);

        UUID bandit1UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit1UUID, "foo", "foo", false, DateTime.now(), DateTime.now(), Lists.newArrayList()));
        Bandit bandit1 = i.findBanditByUUID(bandit1UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        i.registerContact(Contact.create(UUID.randomUUID(), DateTime.now(), DateTime.now(), 0L, Role.LEADER, "nzyme-test", 0, bandit1.databaseId(), bandit1));

        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size()+1);
        assertEquals(i.findContacts().size(), 1);

        UUID bandit2UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit2UUID, "foo", "foo", false, DateTime.now(), DateTime.now(), Lists.newArrayList()));
        Bandit bandit2 = i.findBanditByUUID(bandit2UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        i.registerContact(Contact.create(UUID.randomUUID(), DateTime.now(), DateTime.now(), 0L, Role.LEADER, "nzyme-test", 0,bandit2.databaseId(), bandit2));

        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size()+2);
        assertEquals(i.findContacts().size(), 2);
    }

    @Test
    public void testBanditHasActiveContact() throws Exception {
        MockNzyme nzyme = new MockNzyme();
        ContactManager i = new ContactManager(nzyme);

        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size());
        assertEquals(i.findContacts().size(), 0);

        UUID bandit1UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit1UUID, "foo", "foo", false, DateTime.now(), DateTime.now(), Lists.newArrayList()));
        Bandit bandit1 = i.findBanditByUUID(bandit1UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        i.registerContact(Contact.create(UUID.randomUUID(), DateTime.now(), DateTime.now(), 0L, Role.LEADER, nzyme.getNodeID(), 0,bandit1.databaseId(), bandit1));

        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size()+1);
        assertEquals(i.findContacts().size(), 1);

        UUID bandit2UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit2UUID, "foo", "foo", false, DateTime.now(), DateTime.now(), Lists.newArrayList()));
        Bandit bandit2 = i.findBanditByUUID(bandit2UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);

        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size()+2);
        assertEquals(i.findContacts().size(), 1);

        assertTrue(i.banditHasActiveContactOnSource(bandit1, nzyme.getNodeID()));
        assertFalse(i.banditHasActiveContactOnSource(bandit2, nzyme.getNodeID()));
        assertFalse(i.banditHasActiveContactOnSource(Bandit.create(null, UUID.randomUUID(), "foo", "foo", false, DateTime.now(), DateTime.now(), Lists.newArrayList()), nzyme.getNodeID()));
    }

    @Test
    public void testRegisterContactFrames() throws Exception, MalformedFrameException {
        MockNzyme nzyme = new MockNzyme();
        ContactManager i = new ContactManager(nzyme);

        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size());
        assertEquals(i.findContacts().size(), 0);

        UUID banditUUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, banditUUID, "foo", "foo", false, DateTime.now(), DateTime.now(), Lists.newArrayList()));

        UUID contactUUID = UUID.randomUUID();
        Bandit bandit = i.findBanditByUUID(banditUUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        i.registerContact(Contact.create(contactUUID, DateTime.now(), DateTime.now(), 0L, Role.LEADER, nzyme.getNodeID(), 0,bandit.databaseId(), bandit));

        assertEquals(i.getBandits().size(), DefaultBandits.BANDITS.size()+1);
        assertEquals(i.findContacts().size(), 1);

        assertEquals(i.findContacts().get(contactUUID).frameCount().longValue(), 0);
        assertTrue(i.findRecordValuesOfContact(contactUUID, ContactRecorder.RECORD_TYPE.SSID).isEmpty());
        assertTrue(i.findRecordValuesOfContact(contactUUID, ContactRecorder.RECORD_TYPE.BSSID).isEmpty());

        i.registerContactFrame(bandit, nzyme.getNodeID(), 0, "7C:75:5C:AF:E4:71", Optional.of("foo"));
        assertEquals(i.findContacts().get(contactUUID).frameCount().longValue(), 1);

        // Frame with no SSID should not change SSIDs but increase frame count.
        i.registerContactFrame(bandit, nzyme.getNodeID(), 0, "7C:75:5C:AF:E4:71", Optional.empty());
        assertEquals(i.findContacts().get(contactUUID).frameCount().longValue(), 2);
        i.registerContactFrame(bandit, nzyme.getNodeID(), 0,"7C:75:5C:AF:E4:71", Optional.of("bar"));

        i.registerContactFrame(bandit, nzyme.getNodeID(), 0, "7C:75:5C:AF:E4:71", Optional.of("foo"));
        assertEquals(i.findContacts().get(contactUUID).frameCount().longValue(), 4);
    }

    @Test
    public void testIdentifyMatchesSimple() throws MalformedFrameException, Exception {
        MockNzyme nzyme = new MockNzyme();
        ContactManager i = new ContactManager(nzyme);

        UUID bandit1UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit1UUID, "foo", "foo", false, DateTime.now(), DateTime.now(), new ArrayList<BanditIdentifier>(){{
            add(new FingerprintBanditIdentifier("dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b", null, UUID.randomUUID()));
        }}));
        Bandit bandit1 = i.findBanditByUUID(bandit1UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);

        assertFalse(i.banditHasActiveContactOnSource(bandit1, nzyme.getNodeID()));

        i.identify(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP));
        assertTrue(i.banditHasActiveContactOnSource(bandit1, nzyme.getNodeID()));
    }

    @Test
    public void testIdentifyMatchesMultiple() throws MalformedFrameException, Exception {
        MockNzyme nzyme = new MockNzyme();
        ContactManager i = new ContactManager(nzyme);

        UUID bandit1UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit1UUID, "foo", "foo", false, DateTime.now(), DateTime.now(), new ArrayList<BanditIdentifier>(){{
            add(new FingerprintBanditIdentifier("dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b", null, UUID.randomUUID()));
            add(new SSIDIBanditdentifier(new ArrayList<String>(){{
                add("WTF");
                add("another one");
            }}, null, UUID.randomUUID()));
        }}));
        Bandit bandit1 = i.findBanditByUUID(bandit1UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);

        UUID bandit2UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit2UUID, "foo", "foo", false, DateTime.now(), DateTime.now(), new ArrayList<BanditIdentifier>(){{
            add(new SignalStrengthBanditIdentifier(-15, -25, null, UUID.randomUUID()));
        }}));
        Bandit bandit2 = i.findBanditByUUID(bandit2UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);

        assertFalse(i.banditHasActiveContactOnSource(bandit1, nzyme.getNodeID()));
        assertFalse(i.banditHasActiveContactOnSource(bandit2, nzyme.getNodeID()));

        i.identify(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP));
        assertTrue(i.banditHasActiveContactOnSource(bandit1, nzyme.getNodeID()));
        assertFalse(i.banditHasActiveContactOnSource(bandit2, nzyme.getNodeID()));
    }

}