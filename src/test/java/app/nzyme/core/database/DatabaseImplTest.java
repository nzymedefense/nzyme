package app.nzyme.core.database;

import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeNode;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DatabaseImplTest {

    @Test
    public void testGetTotalSize() {
        NzymeNode nzyme = new MockNzyme();

        assertTrue(nzyme.getDatabase().getTotalSize() >= 0);
    }

    @Test
    public void testGetDatabaseClock() {
        NzymeNode nzyme = new MockNzyme();

        assertTrue(nzyme.getDatabase().getDatabaseClock().isAfter(DateTime.now().minusHours(1)));
    }

}