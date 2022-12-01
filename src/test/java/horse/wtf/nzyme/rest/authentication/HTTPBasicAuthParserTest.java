package horse.wtf.nzyme.rest.authentication;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class HTTPBasicAuthParserTest {

    @Test
    public void testParse() {
        HTTPBasicAuthParser.Credentials c = HTTPBasicAuthParser.parse("Basic bGVubmFydDphdGhvdXNhbmRzdW5z");
        assertNotNull(c);
        assertEquals(c.getUsername(), "lennart");
        assertEquals(c.getPassword(), "athousandsuns");
    }

    @Test
    public void testParseTrims() {
        HTTPBasicAuthParser.Credentials c = HTTPBasicAuthParser.parse("Basic bGVubmFydDphdGhvdXNhbmRzdW5zIA==");
        assertNotNull(c);
        assertEquals(c.getUsername(), "lennart");
        assertEquals(c.getPassword(), "athousandsuns");

        HTTPBasicAuthParser.Credentials c2 = HTTPBasicAuthParser.parse("Basic bGVubmFydDogYXRob3VzYW5kc3Vucw==");
        assertNotNull(c2);
        assertEquals(c2.getUsername(), "lennart");
        assertEquals(c2.getPassword(), "athousandsuns");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "Invalid header format: Scheme")
    public void testExceptionForWrongType() {
        HTTPBasicAuthParser.parse("Foo bGVubmFydDphdGhvdXNhbmRzdW5z");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "Invalid header format: Credentials")
    public void testExceptionForMissingValue() {
        HTTPBasicAuthParser.parse("Basic ");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = ".*Unrecognized character: %.*")
    public void testExceptionForInvalidBase64() {
        HTTPBasicAuthParser.parse("Basic abc%$&4564");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "Invalid authorization payload")
    public void testExceptionForPairWithNoColon() {
        HTTPBasicAuthParser.parse("Basic Zm9vYmFy");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "Invalid authorization payload")
    public void testExceptionForPairWithLeadingColon() {
        HTTPBasicAuthParser.parse("Basic OmZvb2Jhcg==");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "Invalid authorization payload")
    public void testExceptionForPairWithTrailingColon() {
        HTTPBasicAuthParser.parse("Basic Zm9vYmFyOg==");
    }



}