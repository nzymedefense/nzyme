package app.nzyme.core.alerts.service.callbacks;

import app.nzyme.core.util.Tools;
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