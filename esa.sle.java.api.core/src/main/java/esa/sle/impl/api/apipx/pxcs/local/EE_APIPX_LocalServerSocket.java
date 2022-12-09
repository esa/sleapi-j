package esa.sle.impl.api.apipx.pxcs.local;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class EE_APIPX_LocalServerSocket
{

    private static final Logger LOG = Logger.getLogger(EE_APIPX_LocalServerSocket.class.getName());

    private static final ConcurrentHashMap<String, EE_APIPX_LocalServerSocket> serverSockets = new ConcurrentHashMap<String, EE_APIPX_LocalServerSocket>();

    private static final EE_APIPX_LocalSocket CLOSE_SENTRY = new EE_APIPX_LocalSocket();

    private final String ipcAddress;

    private final List<SocketPair> pairs = new CopyOnWriteArrayList<SocketPair>();

    private final BlockingQueue<EE_APIPX_LocalSocket> waitingSockets = new LinkedBlockingQueue<EE_APIPX_LocalSocket>();

    private volatile boolean closed = false;


    static void connectTo(String address, EE_APIPX_LocalSocket remoteSocket) throws IOException
    {
        synchronized (serverSockets)
        {
            EE_APIPX_LocalServerSocket ssock = serverSockets.get(address);
            if (ssock == null)
            {
                throw new IOException("Cannot connect to address " + address + ": no server socket available");
            }
            else
            {
                ssock.offerConnection(remoteSocket);
            }
        }
    }

    public EE_APIPX_LocalServerSocket(String ipcAddress) throws IOException
    {
        this.ipcAddress = ipcAddress;
        // Add the server socket in a static synch map
        // The map is looked up by the LocalSocket upon connect and the accept
        // is notified,
        // i.e. the LocalSocket used to look up is put in a queue, and a
        // corresponding inverse LocalSocket is created to communicate in memory
        // (use the
        // PipedInputStream/PipedOutputStream in a buffered fashion)
        synchronized (serverSockets)
        {
            EE_APIPX_LocalServerSocket old = serverSockets.putIfAbsent(ipcAddress, this);
            if (old != null && old != this)
            {
                throw new IOException("Server socket already bound on address " + ipcAddress);
            }
        }

        LOG.finer("Created local server socket bound to local address " + this.ipcAddress);
    }

    public void close() throws IOException
    {
        LOG.fine("Closing local server socket bound to local address " + this.ipcAddress);
        // Remove the server socket from the map
        synchronized (serverSockets)
        {
            EE_APIPX_LocalServerSocket old = serverSockets.remove(this.ipcAddress);
            if (old != this)
            {
                throw new IOException("Server socket not present in the map for " + this.ipcAddress);
            }
        }
        // Mark all the sockets belonging to this server socket as
        // disconnected
        for (SocketPair sp : this.pairs)
        {
            sp.markDisconnected();
        }
        this.pairs.clear();
        this.waitingSockets.clear();
        this.waitingSockets.offer(CLOSE_SENTRY);
        this.closed = true;
    }

    public EE_APIPX_LocalSocket accept() throws IOException
    {
        if (this.closed)
        {
            throw new IOException("Socket closed");
        }
        try
        {
            EE_APIPX_LocalSocket remote = this.waitingSockets.take();
            if (this.closed)
            {
                if (remote != CLOSE_SENTRY)
                {
                    remote.setRemote(null);
                }
                throw new IOException("Socket closed");
            }

            if (remote != CLOSE_SENTRY)
            {
                EE_APIPX_LocalSocket local = new EE_APIPX_LocalSocket(this.ipcAddress, remote);
                remote.setRemote(local);

                this.pairs.add(new SocketPair(remote, local));
                return local;
            }
            else
            {
                throw new IOException("Socket closed");
            }
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
            return accept();
        }
    }

    private void offerConnection(EE_APIPX_LocalSocket remoteSocket) throws IOException
    {
        boolean result = this.waitingSockets.offer(remoteSocket);
        if (!result)
        {
            throw new IOException("Cannot connect to address " + this.ipcAddress + ": backlog too large");
        }
    }


    private class SocketPair
    {
        private final EE_APIPX_LocalSocket remote;

        private final EE_APIPX_LocalSocket local;


        public SocketPair(EE_APIPX_LocalSocket remote, EE_APIPX_LocalSocket local)
        {
            super();
            this.remote = remote;
            this.local = local;
        }

        public void markDisconnected()
        {
            try
            {
                this.remote.close();
            }
            catch (IOException e)
            {
                // No reason to log
            }
            try
            {
                this.local.close();
            }
            catch (IOException e)
            {
                // No reason to log
            }
        }
    }

}
