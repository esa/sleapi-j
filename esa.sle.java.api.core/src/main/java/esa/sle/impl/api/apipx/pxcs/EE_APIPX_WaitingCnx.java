/**
 * @(#) EE_APIPX_WaitingCnx_Linux.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;

public class EE_APIPX_WaitingCnx
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_WaitingCnx.class.getName());

    private final String ipcAddress;

    private final boolean isDfl;

    private final List<EE_APIPX_Link> eeAPIPXLink;

    private ServerSocket serverSocket;

    private ConnAcceptingThread connAccTh;

    private boolean useNagleFlag;

    private final String instanceId;

    public EE_APIPX_WaitingCnx(String instanceKey, String ipcAddress, boolean isDfl)
    {
    	this.instanceId = instanceKey;
        this.ipcAddress = ipcAddress;
        this.isDfl = isDfl;
        this.eeAPIPXLink = new ArrayList<EE_APIPX_Link>();
        this.serverSocket = null;
        this.useNagleFlag = false;
    }

    public synchronized HRESULT start()
    {
        HRESULT res = HRESULT.E_FAIL;

        if (this.serverSocket != null)
        {
            return HRESULT.E_FAIL;
        }

        // create and bind the socket
        res = servListen();

        if (res == HRESULT.S_OK)
        {
            // create and start the Connection Acceptance Thread
            this.connAccTh = new ConnAcceptingThread();
            this.connAccTh.start();
        }
        else
        {
            return HRESULT.E_FAIL;
        }

        return HRESULT.S_OK;
    }

    public void shutdown()
    {
        terminateThread();

        try
        {
            this.serverSocket.close();
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
        }
    }

    private HRESULT servListen()
    {
        try
        {
            this.serverSocket = new ServerSocket(Integer.parseInt(this.ipcAddress));
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Cannot create and bind the server socket: " + e.getMessage(), e);
            return HRESULT.E_FAIL;
        }

        return HRESULT.S_OK;
    }

    public HRESULT waitMsg()
    {
        cleanLink(false);
        Socket socket = null;
        try
        {
            socket = this.serverSocket.accept();
        }
        catch (IOException e)
        {
            if (this.connAccTh.isRunning)
            {
                LOG.log(Level.SEVERE, "Cannot accept connection: " + e.getMessage(), e);
            }
            return HRESULT.E_FAIL;
        }

        if (!this.useNagleFlag)
        {
            // disable the NAGLE Algorithm
            try
            {
                socket.setTcpNoDelay(false);
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("***** NAGLE ALGORITHM DISABLED");
                }
            }
            catch (SocketException e)
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.log(Level.FINEST, "***** Cannot disable Nagle algorithm", e);
                }
                return HRESULT.E_FAIL;
            }
        }

        // a socket is connected, create a link
        EE_APIPX_Link pLink = new EE_APIPX_Link(this.instanceId, socket, this.isDfl);
        synchronized (this)
        {
            // add the link to the list
            this.eeAPIPXLink.add(pLink);
        }
        // create the waiting thread message
        pLink.waitMsg();

        return HRESULT.S_OK;
    }

    public synchronized void cleanLink(boolean forceClose)
    {
        for (Iterator<EE_APIPX_Link> it = this.eeAPIPXLink.iterator(); it.hasNext();)
        {
            EE_APIPX_Link link = it.next();
            if (forceClose)
            {
                // force the link to be closed and deleted
                link.disconnect();
                it.remove();
            }
            else if (link.isClosed())
            {
                it.remove();
            }
        }
    }

    private void terminateThread()
    {
        this.connAccTh.stopRunning();
    }

    public void setUseNagleFlag(boolean useNagleFlag)
    {
        this.useNagleFlag = useNagleFlag;
    }


    private class ConnAcceptingThread extends Thread
    {
        private volatile boolean isRunning;


        public ConnAcceptingThread()
        {
            this.isRunning = true;
        }

        @Override
        public void run()
        {
            HRESULT res = HRESULT.S_OK;

            // waits for incoming connections
            while (this.isRunning && res == HRESULT.S_OK)
            {
                res = waitMsg();
            }

            cleanLink(true);
        }

        public void stopRunning()
        {
            this.isRunning = false;
        }
    }
}
