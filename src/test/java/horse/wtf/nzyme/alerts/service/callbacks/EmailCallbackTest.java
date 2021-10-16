package horse.wtf.nzyme.alerts.service.callbacks;

import horse.wtf.nzyme.util.Tools;
import org.simplejavamail.api.email.Recipient;
import org.testng.annotations.Test;

import javax.mail.Message;

import static org.testng.Assert.*;

public class EmailCallbackTest {

    @Test
    public void testParseRecipient() throws Exception {
        Recipient recipient = Tools.parseEmailAddress("Lennart Koopmann <lennart@0x58ed.com>");

        assertEquals(recipient.getName(), "Lennart Koopmann");
        assertEquals(recipient.getAddress(), "lennart@0x58ed.com");
        assertEquals(recipient.getType(), Message.RecipientType.TO);
    }

}