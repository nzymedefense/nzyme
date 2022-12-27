package app.nzyme.core.util;

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

    @Test
    public void testIsSafeID() {
        assertTrue(Tools.isSafeID("abc"));
        assertTrue(Tools.isSafeID("abc123"));
        assertTrue(Tools.isSafeID("123abc"));
        assertTrue(Tools.isSafeID("ab123c"));
        assertTrue(Tools.isSafeID("abC"));
        assertTrue(Tools.isSafeID("ABC"));
        assertTrue(Tools.isSafeID("ABC123"));
        assertTrue(Tools.isSafeID("_abc"));
        assertTrue(Tools.isSafeID("-abc"));
        assertTrue(Tools.isSafeID("a-bc"));
        assertTrue(Tools.isSafeID("a_bc"));
        assertTrue(Tools.isSafeID("a-123bc"));
        assertTrue(Tools.isSafeID("abc_"));
        assertTrue(Tools.isSafeID("abc-"));

        assertFalse(Tools.isSafeID("a$bc"));
        assertFalse(Tools.isSafeID("a$ bc"));
        assertFalse(Tools.isSafeID("a$bc"));
        assertFalse(Tools.isSafeID("abc "));
        assertFalse(Tools.isSafeID(" abc"));
        assertFalse(Tools.isSafeID("&abc"));
        assertFalse(Tools.isSafeID("a&bc"));
        assertFalse(Tools.isSafeID("1a$bc"));
        assertFalse(Tools.isSafeID("1$abc"));
    }

}