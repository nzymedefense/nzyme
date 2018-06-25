package horse.wtf.nzyme.util;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ToolsTest {

    @Test
    public void testIsSafeParameter() {
        assertTrue(Tools.isSafeParameter("abc123"));
        assertTrue(Tools.isSafeParameter("abc_123"));
        assertTrue(Tools.isSafeParameter("abc-123"));
        assertTrue(Tools.isSafeParameter("abc.123"));
        assertTrue(Tools.isSafeParameter("abc/123"));
        assertTrue(Tools.isSafeParameter("abc 123"));
        assertTrue(Tools.isSafeParameter("abc-.123"));
        assertTrue(Tools.isSafeParameter("abc- .123"));

        assertFalse(Tools.isSafeParameter("&"));
        assertFalse(Tools.isSafeParameter("&&"));
        assertFalse(Tools.isSafeParameter("\""));
        assertFalse(Tools.isSafeParameter("'"));
        assertFalse(Tools.isSafeParameter("|"));
        assertFalse(Tools.isSafeParameter("abc\\123"));
        assertFalse(Tools.isSafeParameter("abc\"123"));
        assertFalse(Tools.isSafeParameter("abc123;"));
        assertFalse(Tools.isSafeParameter("abc'123"));
        assertFalse(Tools.isSafeParameter("abc&123"));
        assertFalse(Tools.isSafeParameter("abc|123"));
    }

}