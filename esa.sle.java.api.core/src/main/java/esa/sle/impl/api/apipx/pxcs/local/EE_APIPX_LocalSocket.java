
package esa.sle.impl.api.apipx.pxcs.local;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The PipedInputStream has a property which does not fit to the
 * SLE API architecture: If a thread which has send data into this input stream
 * (via receive) terminates, an IOException is thrown on the next read. In SLE API
 * This is not compatible with the fact that the write threads of the links representing the
 * external SLE (TML) connections are terminated on disconnect.    
 */
class MyPipedInputStream extends PipedInputStream  {
	private volatile boolean closed = false; // SLEAPIJ-46
	private volatile boolean writerClosed = false;
	
	public MyPipedInputStream() {
		super();
	}
	
	public MyPipedInputStream(int pipeSize) {
		super(pipeSize);
	}
	
	public synchronized int read()  throws IOException {	        
	        while (in < 0) {
	        	if(this.closed == true) {
	        		throw new IOException("Pipe closed for MyPipedInputStream"); // SLEAPIJ-46
	        	}
	        	
	        	if(this.writerClosed == true) {
	        		return -1;
	        	}
	        	
	            /* might be a writer waiting */
	            notifyAll();
	            try {
	                wait(1000);
	            } catch (InterruptedException ex) {
	                throw new java.io.InterruptedIOException();
	            }
	        }
	        int ret = buffer[out++] & 0xFF;
	        if (out >= buffer.length) {
	            out = 0;
	        }
	        if (in == out) {
	            /* now empty */
	            in = -1;
	        }

	        return ret;	
	}

	@Override
	public synchronized int read(byte b[], int off, int len)  throws IOException {
		int ret = super.read(b, off, len);
		notifyAll(); // in case a writer was waiting
		return ret;
	}
		
	public void writerClosed() {
		this.writerClosed = true;
	}
	
	/**
	 * Overridden to capture the fact that the stream is closed.
	 * Causes read to return. SLEAPIJ-46
	 */
	@Override
	public void close() throws IOException {
		this.closed = true;
		super.close();
	}	
}

/**
 * A variant of PipedOutputStream to forwards a notification about the closed output stream.
 * Emulates the PipedOutputStream#closed() { sink.receivedLast();} notification
 * which is needed to realize the closed input stream connected to this output stream.
 * SLEAPIJ-46
 */ 
class MyPipedOutputStream extends PipedOutputStream {
	
	private volatile MyPipedInputStream inputStream;

	@Override
	public void close()  throws IOException {
		if(inputStream instanceof MyPipedInputStream) {
			((MyPipedInputStream)this.inputStream).writerClosed(); // inform the other side as well
		}
		super.close();
	}
	
	@Override
	public synchronized void connect(PipedInputStream snk) throws IOException {
		if(snk instanceof MyPipedInputStream) {
			this.inputStream = (MyPipedInputStream)snk;
		}
		super.connect(snk);
	}
}

public class EE_APIPX_LocalSocket
{

    private final String ipcAddress;

    private PipedInputStream inputStream = null;

    private PipedOutputStream outputStream = null;

    private volatile EE_APIPX_LocalSocket remotePeer = null;

    private volatile boolean markedAsClosed = false;

    private final Lock mutex = new ReentrantLock();

    private final Condition remoteReady = this.mutex.newCondition();

    // Used internally by the server socket
    EE_APIPX_LocalSocket(String ipcAddress, EE_APIPX_LocalSocket remotePeer) throws IOException
    {
        this.ipcAddress = ipcAddress;
        this.inputStream = new MyPipedInputStream(2048000*10);
        this.outputStream = new MyPipedOutputStream();		// SLEAPIJ-46
        setRemote(remotePeer);
        wireChannels();
    }

    // Used as closing sentry
    EE_APIPX_LocalSocket()
    {
        this.ipcAddress = null;
    }

    // Used by connecting clients
    public EE_APIPX_LocalSocket(String ipcAddress) throws IOException
    {
        this.ipcAddress = ipcAddress;
        this.inputStream = new MyPipedInputStream();
        this.outputStream = new MyPipedOutputStream(); // SLEAPIJ-46
        connect();
    }

    private void wireChannels() throws IOException
    {
        this.inputStream.connect(this.remotePeer.outputStream);
        this.outputStream.connect(this.remotePeer.inputStream);
    }

    private void connect() throws IOException
    {
        EE_APIPX_LocalServerSocket.connectTo(this.ipcAddress, this);
        this.mutex.lock();
        try
        {
            while (this.remotePeer == null && !this.markedAsClosed)
            {
                try
                {
                    this.remoteReady.await();
                }
                catch (InterruptedException e)
                {
                    Thread.interrupted();
                }
            }
            if (this.markedAsClosed)
            {
                throw new IOException("Socket closed");
            }
        }
        finally
        {
            this.mutex.unlock();
        }
    }

    public void close() throws IOException
    {
        this.mutex.lock();
        try
        {
            this.markedAsClosed = true;
            this.inputStream.close();
            this.outputStream.close();
            this.remotePeer = null;
            this.remoteReady.signalAll();
        }
        finally
        {
            this.mutex.unlock();
        }
    }

    public OutputStream getOutputStream() throws IOException
    {
        this.mutex.lock();
        try
        {
            if (this.remotePeer == null)
            {
                throw new IOException("Socket not connected");
            }
        }
        finally
        {
            this.mutex.unlock();
        }
        return this.outputStream;
    }

    public boolean isClosed()
    {
        return this.remotePeer == null;
    }

    public InputStream getInputStream() throws IOException
    {
        this.mutex.lock();
        try
        {
            if (this.remotePeer == null)
            {
                throw new IOException("Socket not connected");
            }
        }
        finally
        {
            this.mutex.unlock();
        }
        return this.inputStream;
    }

    void setRemote(EE_APIPX_LocalSocket remote)
    {
        this.mutex.lock();
        try
        {
            if (remote == null)
            {
                this.markedAsClosed = true;
            }
            else
            {
                this.remotePeer = remote;
            }
            this.remoteReady.signalAll();
        }
        finally
        {
            this.mutex.unlock();
        }
    }

}
