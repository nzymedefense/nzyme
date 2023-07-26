package app.nzyme.core.util;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ToolsTest {

    @Test
    public void testIsValidMacAddress() {
        assertTrue(Tools.isValidMacAddress("18:7C:0B:D7:14:38"));
        assertTrue(Tools.isValidMacAddress("18:7c:0B:D7:14:38"));
        assertTrue(Tools.isValidMacAddress("18:7c:0b:d7:14:38"));
        assertFalse(Tools.isValidMacAddress("1x:7c:0b:d7:14:38"));
        assertFalse(Tools.isValidMacAddress("18:7c:0b:d7:14:38:14"));
        assertFalse(Tools.isValidMacAddress("18-7C-0B-D7-14-38"));
        assertFalse(Tools.isValidMacAddress(""));
        assertFalse(Tools.isValidMacAddress(" "));
        assertFalse(Tools.isValidMacAddress("abc"));
        assertFalse(Tools.isValidMacAddress("187C0BD71438"));
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