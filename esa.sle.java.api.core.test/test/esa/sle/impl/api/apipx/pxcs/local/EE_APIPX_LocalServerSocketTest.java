package esa.sle.impl.api.apipx.pxcs.local;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

public class EE_APIPX_LocalServerSocketTest
{

    private static final Logger LOG = Logger.getLogger(EE_APIPX_LocalServerSocketTest.class.getName());


    @Test
    public void testAcceptValue() throws IOException, InterruptedException
    {
        EE_APIPX_LocalServerSocket ssock = new EE_APIPX_LocalServerSocket("LOCAL-ADDRESS");
        new Thread(() -> runTest()).start();
        EE_APIPX_LocalSocket sock = ssock.accept();
        Thread.sleep(2000);
        sock.getOutputStream().write(new byte[23]);
        byte[] buf = new byte[200];
        int read = sock.getInputStream().read(buf);
        System.out.println("Read " + read + " bytes (ssock)");
        sock.close();
    }

    private void runTest()
    {
        System.out.println("Test thread started");
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
            LOG.log(Level.FINE, "InterruptedException ", e);
        }
        System.out.println("Test thread ready to connect");
        try
        {
            EE_APIPX_LocalSocket sock = new EE_APIPX_LocalSocket("LOCAL-ADDRESS");
            System.out.println("Test thread connected");
            byte[] buf = new byte[200];
            int read = sock.getInputStream().read(buf);
            System.out.println("Read " + read + " bytes");
            sock.getOutputStream().write(new byte[] { 0, 1, 2, 3 });
            sock.getOutputStream().flush();
            sock.close();
            System.out.println("Test thread socket closed");
        }
        catch (Exception e)
        {
            LOG.log(Level.FINE, "Exception ", e);
        }
    }

}
