package net.i2p.data;
/*
 * free (adj.): unencumbered; not under the control of others
 * Written by jrandom in 2003 and released into the public domain 
 * with no warranty of any kind, either expressed or implied.  
 * It probably won't make your computer catch on fire, or eat 
 * your children, but it might.  Use at your own risk.
 *
 */

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test harness for loading / storing PublicKey objects
 *
 * @author jrandom
 */
public class PublicKeyTest extends StructureTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    public DataStructure createDataStructure() throws DataFormatException {
        PublicKey publicKey = new PublicKey();
        byte data[] = new byte[PublicKey.KEYSIZE_BYTES];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte)(i%16);
        publicKey.setData(data);
        return publicKey; 
    }
    public DataStructure createStructureToRead() { return new PublicKey(); }

    @Test
    public void testBase64Constructor() throws Exception{
        PublicKey publicKey = new PublicKey();
        byte data[] = new byte[PublicKey.KEYSIZE_BYTES];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte)(i%56);
        publicKey.setData(data);

        PublicKey key2 = new PublicKey(publicKey.toBase64());
        assertEquals(publicKey, key2);
    }

    @Test
    public void testNullEquals(){
        PublicKey publicKey = new PublicKey();
        byte data[] = new byte[PublicKey.KEYSIZE_BYTES];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte)(i%56);
        publicKey.setData(data);

        assertFalse(publicKey.equals(null));
    }

    @Test
    public void testNullData() throws Exception{
        PublicKey publicKey = new PublicKey();
        publicKey.toString();

        exception.expect(DataFormatException.class);
        exception.expectMessage("No data to write out");
        publicKey.writeBytes(new ByteArrayOutputStream());
    }

    @Test
    public void testShortData() throws Exception{
        PublicKey publicKey = new PublicKey();
        byte data[] = new byte[56];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte)(i);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Bad data length: 56; required: " + PublicKey.KEYSIZE_BYTES);
        publicKey.setData(data);
        publicKey.writeBytes(new ByteArrayOutputStream());
    }

    @Test
    public void testShortRead() throws Exception{
        PublicKey publicKey = new PublicKey();
        ByteArrayInputStream in = new ByteArrayInputStream(DataHelper.getASCII("six times nine equals forty-two"));

        exception.expect(EOFException.class);
        exception.expectMessage("EOF after reading 31 bytes of " + PublicKey.KEYSIZE_BYTES + " byte value");
        publicKey.readBytes(in);
    }
}
