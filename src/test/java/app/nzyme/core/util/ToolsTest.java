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
        assertTrue(Tools.isSafeNodeName("abc"));
        assertTrue(Tools.isSafeNodeName("abc123"));
        assertTrue(Tools.isSafeNodeName("123abc"));
        assertTrue(Tools.isSafeNodeName("ab123c"));
        assertTrue(Tools.isSafeNodeName("abC"));
        assertTrue(Tools.isSafeNodeName("ABC"));
        assertTrue(Tools.isSafeNodeName("ABC123"));
        assertTrue(Tools.isSafeNodeName("_abc"));
        assertTrue(Tools.isSafeNodeName("-abc"));
        assertTrue(Tools.isSafeNodeName("a-bc"));
        assertTrue(Tools.isSafeNodeName("a_bc"));
        assertTrue(Tools.isSafeNodeName("a-123bc"));
        assertTrue(Tools.isSafeNodeName("abc_"));
        assertTrue(Tools.isSafeNodeName("abc-"));

        assertFalse(Tools.isSafeNodeName("a$bc"));
        assertFalse(Tools.isSafeNodeName("a$ bc"));
        assertFalse(Tools.isSafeNodeName("a$bc"));
        assertFalse(Tools.isSafeNodeName("abc "));
        assertFalse(Tools.isSafeNodeName(" abc"));
        assertFalse(Tools.isSafeNodeName("&abc"));
        assertFalse(Tools.isSafeNodeName("a&bc"));
        assertFalse(Tools.isSafeNodeName("1a$bc"));
        assertFalse(Tools.isSafeNodeName("1$abc"));
    }

}