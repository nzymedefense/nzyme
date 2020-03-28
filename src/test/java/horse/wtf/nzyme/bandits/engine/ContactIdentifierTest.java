package horse.wtf.nzyme.bandits.engine;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.Contact;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.FingerprintBanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.SSIDIBanditdentifier;
import horse.wtf.nzyme.bandits.identifiers.SignalStrengthBanditIdentifier;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11DeauthenticationFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

import static org.testng.Assert.*;

public class ContactIdentifierTest {

    protected static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, -10, 2400, 1, 0L, false);

    @BeforeMethod
    public void cleanDatabase() {
        Nzyme nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM bandits"));
    }

    @Test
    public void testRegisterBandit() {
        ContactIdentifier i = new ContactIdentifier(new MockNzyme());

        UUID uuid1 = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, uuid1, "foo", "foo", DateTime.now(), DateTime.now(), Lists.newArrayList()));
        assertEquals(i.getBandits().size(), 1);

        UUID uuid2 = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, uuid2, "foo", "foo", DateTime.now(), DateTime.now(), Lists.newArrayList()));
        assertEquals(i.getBandits().size(), 2);

        assertNotNull(i.getBandits().get(uuid1));
        assertNotNull(i.getBandits().get(uuid2));
    }

    @Test
    public void testRemoveBandit() {
        ContactIdentifier i = new ContactIdentifier(new MockNzyme());

        assertEquals(i.getBandits().size(), 0);

        UUID uuid1 = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, uuid1, "foo", "foo", DateTime.now(), DateTime.now(), Lists.newArrayList()));
        assertEquals(i.getBandits().size(), 1);

        UUID uuid2 = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, uuid2, "foo", "foo", DateTime.now(), DateTime.now(), Lists.newArrayList()));
        assertEquals(i.getBandits().size(), 2);

        assertNotNull(i.getBandits().get(uuid1));
        assertNotNull(i.getBandits().get(uuid2));

        i.removeBandit(uuid1);

        assertEquals(i.getBandits().size(), 1);
        assertNull(i.getBandits().get(uuid1));
        assertNotNull(i.getBandits().get(uuid2));
    }

    @Test
    public void testRegisterContact() throws Exception {
        ContactIdentifier i = new ContactIdentifier(new MockNzyme());

        assertEquals(i.getBandits().size(), 0);
        assertEquals(i.findContacts().size(), 0);

        UUID bandit1UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit1UUID, "foo", "foo", DateTime.now(), DateTime.now(), Lists.newArrayList()));
        Bandit bandit1 = i.findBanditByUUID(bandit1UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        i.registerContact(Contact.create( UUID.randomUUID(), bandit1.databaseId(), bandit1, DateTime.now(), DateTime.now(), 0L));

        assertEquals(i.getBandits().size(), 1);
        assertEquals(i.findContacts().size(), 1);

        UUID bandit2UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit2UUID, "foo", "foo", DateTime.now(), DateTime.now(), Lists.newArrayList()));
        Bandit bandit2 = i.findBanditByUUID(bandit2UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        i.registerContact(Contact.create(UUID.randomUUID(), bandit2.databaseId(), bandit2, DateTime.now(), DateTime.now(), 0L));

        assertEquals(i.getBandits().size(), 2);
        assertEquals(i.findContacts().size(), 2);
    }

    @Test
    public void testBanditHasActiveContact() throws Exception {
        ContactIdentifier i = new ContactIdentifier(new MockNzyme());

        assertEquals(i.getBandits().size(), 0);
        assertEquals(i.findContacts().size(), 0);

        UUID bandit1UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit1UUID, "foo", "foo", DateTime.now(), DateTime.now(), Lists.newArrayList()));
        Bandit bandit1 = i.findBanditByUUID(bandit1UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        i.registerContact(Contact.create(UUID.randomUUID(), bandit1.databaseId(), bandit1, DateTime.now(), DateTime.now(), 0L));

        assertEquals(i.getBandits().size(), 1);
        assertEquals(i.findContacts().size(), 1);

        UUID bandit2UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit2UUID, "foo", "foo", DateTime.now(), DateTime.now(), Lists.newArrayList()));
        Bandit bandit2 = i.findBanditByUUID(bandit2UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);

        assertEquals(i.getBandits().size(), 2);
        assertEquals(i.findContacts().size(), 1);

        assertTrue(i.banditHasActiveContact(bandit1));
        assertFalse(i.banditHasActiveContact(bandit2));
        assertFalse(i.banditHasActiveContact(Bandit.create(null, UUID.randomUUID(), "foo", "foo", DateTime.now(), DateTime.now(), Lists.newArrayList())));
    }

    @Test
    public void testRegisterContactFrames() throws Exception, MalformedFrameException {
        ContactIdentifier i = new ContactIdentifier(new MockNzyme());

        assertEquals(i.getBandits().size(), 0);
        assertEquals(i.findContacts().size(), 0);

        UUID bandit1UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit1UUID, "foo", "foo", DateTime.now(), DateTime.now(), Lists.newArrayList()));
        UUID contact1UUID = UUID.randomUUID();
        Bandit bandit1 = i.findBanditByUUID(bandit1UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);
        i.registerContact(Contact.create(contact1UUID, bandit1.databaseId(), bandit1, DateTime.now(), DateTime.now(), 0L));

        assertEquals(i.getBandits().size(), 1);
        assertEquals(i.findContacts().size(), 1);

        assertEquals(i.findContacts().get(contact1UUID).frameCount().longValue(), 0);

        i.registerContactFrame(bandit1, new Dot11BeaconFrameParser(new MetricRegistry()).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP));
        assertEquals(i.findContacts().get(contact1UUID).frameCount().longValue(), 1);

        i.registerContactFrame(bandit1, new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(Frames.PROBE_RESP_1_PAYLOAD, Frames.PROBE_RESP_1_HEADER, META_NO_WEP));
        assertEquals(i.findContacts().get(contact1UUID).frameCount().longValue(), 2);

        i.registerContactFrame(bandit1, new Dot11DeauthenticationFrameParser(new MetricRegistry()).parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, META_NO_WEP));
        assertEquals(i.findContacts().get(contact1UUID).frameCount().longValue(), 3);
    }

    @Test
    public void testIdentifyMatchesSimple() throws MalformedFrameException, Exception {
        ContactIdentifier i = new ContactIdentifier(new MockNzyme());

        UUID bandit1UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit1UUID, "foo", "foo", DateTime.now(), DateTime.now(), new ArrayList<BanditIdentifier>(){{
            add(new FingerprintBanditIdentifier("dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b", null, UUID.randomUUID()));
        }}));
        Bandit bandit1 = i.findBanditByUUID(bandit1UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);

        assertFalse(i.banditHasActiveContact(bandit1));

        i.identify(new Dot11BeaconFrameParser(new MetricRegistry()).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP));
        assertTrue(i.banditHasActiveContact(bandit1));
    }

    @Test
    public void testIdentifyMatchesMultiple() throws MalformedFrameException, Exception {
        ContactIdentifier i = new ContactIdentifier(new MockNzyme());

        UUID bandit1UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit1UUID, "foo", "foo", DateTime.now(), DateTime.now(), new ArrayList<BanditIdentifier>(){{
            add(new FingerprintBanditIdentifier("dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b", null, UUID.randomUUID()));
            add(new SSIDIBanditdentifier(new ArrayList<String>(){{
                add("WTF");
                add("another one");
            }}, null, UUID.randomUUID()));
        }}));
        Bandit bandit1 = i.findBanditByUUID(bandit1UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);

        UUID bandit2UUID = UUID.randomUUID();
        i.registerBandit(Bandit.create(null, bandit2UUID, "foo", "foo", DateTime.now(), DateTime.now(), new ArrayList<BanditIdentifier>(){{
            add(new SignalStrengthBanditIdentifier(-15, -25, null, UUID.randomUUID()));
        }}));
        Bandit bandit2 = i.findBanditByUUID(bandit2UUID).orElseThrow((Supplier<Exception>) RuntimeException::new);

        assertFalse(i.banditHasActiveContact(bandit1));
        assertFalse(i.banditHasActiveContact(bandit2));

        i.identify(new Dot11BeaconFrameParser(new MetricRegistry()).parse(Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP));
        assertTrue(i.banditHasActiveContact(bandit1));
        assertFalse(i.banditHasActiveContact(bandit2));
    }

}