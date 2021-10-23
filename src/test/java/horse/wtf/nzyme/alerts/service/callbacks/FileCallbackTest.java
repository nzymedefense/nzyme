package horse.wtf.nzyme.alerts.service.callbacks;

import horse.wtf.nzyme.alerts.BeaconRateAnomalyAlert;
import horse.wtf.nzyme.alerts.ProbeFailureAlert;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.*;

public class FileCallbackTest {

    @Test
    public void testCallback() throws IOException {
        Path path;
        if (System.getProperty("os.name").startsWith("Windows")) {
            path = new File("C:\\temp\\nzyme_alert_log_TEST.txt").toPath();
        } else {
            path = new File("/tmp/nzyme_alert_log_TEST").toPath();
        }

        if (Files.exists(path)) {
            Files.delete(path);
        }

        FileCallback callback = new FileCallback(FileCallback.Configuration.create(path));
        callback.call(ProbeFailureAlert.create(DateTime.now(), "fooProbe", "is broke"));

        String content = Files.readString(path);
        assertEquals(content.split("\n").length, 1);
        assertTrue(content.startsWith("{"));
        assertTrue(content.endsWith("}\n"));

        callback.call(BeaconRateAnomalyAlert.create(DateTime.now(), "foo", "bar", 100, 50));

        content = Files.readString(path);
        assertEquals(content.split("\n").length, 2);
        assertTrue(content.startsWith("{"));
        assertTrue(content.endsWith("}\n"));
    }

}