package horse.wtf.nzyme.alerts.service.callbacks;

import org.simplejavamail.api.email.Recipient;
import org.testng.annotations.Test;

import javax.mail.Message;

import static org.testng.Assert.*;

public class EmailCallbackTest {

    @Test
    public void testParseRecipient() throws Exception {
        Recipient recipient = EmailCallback.parseRecipient("Lennart Koopmann <lennart@0x58ed.com>", Message.RecipientType.TO);

        assertEquals(recipient.getName(), "Lennart Koopmann");
        assertEquals(recipient.getAddress(), "lennart@0x58ed.com");
        assertEquals(recipient.getType(), Message.RecipientType.TO);
    }

}