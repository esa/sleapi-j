package esa.sle.impl.api.apipx.pxcs.local;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import esa.sle.impl.api.apipx.pxcs.EE_APIPX_Link;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;

public class EE_APIPX_LocalLink extends EE_APIPX_Link
{

    private static final Logger LOG = Logger.getLogger(EE_APIPX_LocalLink.class.getName());

    public static final String IPC_LOCAL_PREFIX = "LOCAL-";


    public static boolean isLocalAddress(String ipcAddress)
    {
        return ipcAddress != null && ipcAddress.startsWith(IPC_LOCAL_PREFIX);
    }


    private EE_APIPX_LocalSocket socket;
    private final String type;

    public EE_APIPX_LocalLink(String instanceKey)
    {
        super(instanceKey);
        this.type = "responding local link ";
    }

    public EE_APIPX_LocalLink(String instanceKey, EE_APIPX_LocalSocket socket, boolean isDfl)
    {
        super(instanceKey, null, isDfl);
        this.socket = socket;
        this.type = "initiating local link ";
    }

    @Override
    public HRESULT connect(String ipcAddress)
    {
        try
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Creating the client socket on link " + this);
            }
            this.socket = new EE_APIPX_LocalSocket(ipcAddress);
        }
        catch (IOException e1)
        {
            LOG.log(Level.SEVERE, "Error on connect from local link " + this, e1);
            return HRESULT.E_FAIL;
        }

        return HRESULT.S_OK;
    }

    /**
     * Closes the IPC connection.
     */
    @Override
    public void disconnect()
    {
        if (this.disconnectionRequested)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Disconnect has been already invoked on link " + this + ". Ignoring...");
            }
            return;
        }

        this.disconnectionRequested = true;
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Disconnect has been invoked on " + this.type + this);
        }

        // stop the receiving thread
        this.recThread.cancel();

        // close the client socket
        try
        {
            this.socket.close();
        }
        catch (IOException e)
        {
            // No reason to log
        }

        if (this.inComServer)
        {
            // notification first at the assoc pxy --> can send PEER Abort
            // before it is deleted by the binder !
            if (this.eeAPIPXAssocPxy != null)
            {
                this.eeAPIPXAssocPxy.ipcClosed(this);
                this.eeAPIPXAssocPxy = null;
            }

            // binder adapter before the logger proxy.
            if (this.eeAPIPXBinderAdapter != null)
            {
                this.eeAPIPXBinderAdapter.ipcClosed(this);
            }

            if (this.eeAPIPXLoggerPxy != null)
            {
                this.eeAPIPXLoggerPxy.ipcClosed(this);
            }
        }
        else
        {
            if (this.eeAPIPXBinderPxy != null)
            {
                this.eeAPIPXBinderPxy.ipcClosed(this);
                this.eeAPIPXBinderPxy = null;
            }

            if (this.eeAPIPXChannelPxy != null)
            {
                this.eeAPIPXChannelPxy.ipcClosed(this);
                this.eeAPIPXChannelPxy = null;
            }

            if (this.isDefaultLogger)
            {
                if (this.ieeAPIPXLoggerAdapter != null)
                {
                    this.ieeAPIPXLoggerAdapter.ipcClosed(this);
                }
                this.ieeAPIPXLoggerAdapter = null;
            }
        }
    }

    /**
     * Sends a message on the in-memory link. S_OK The message has been sent.
     * E_FAIL The message cannot be sent due to a further unspecified error.
     */
    @Override
    public HRESULT sndMess(byte[] data)
    {
        this.mutex.lock();
        try
        {
            this.socket.getOutputStream().write(data);
            this.socket.getOutputStream().flush();
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Cannot send data on the client socket on link " + this + ":" + e.getMessage(), e);
            return HRESULT.E_FAIL;
        }
        finally
        {
            this.mutex.unlock();
        }

        EE_GenStrUtil.print("Writing to socket on link " + this + ": ", data);

        return HRESULT.S_OK;
    }

    /**
     * Indicates if the in-memory link is closed.
     */
    @Override
    public boolean isClosed()
    {
        return this.socket.isClosed();
    }

    /**
     * This function reads a message on the IPC link. S_OK The data had been
     * received. E_FAIL Cannot receive the data.
     */
    @Override
    protected byte[] readData(int toBeRead)
    {
        int dataRead = 0;
        byte[] data = new byte[toBeRead];
        int length = data.length;
        try
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("About to read " + toBeRead + " data on the " + this.type + " " + this);
            }
            //EGSINTEG-3861 read = this.socket.getInputStream().read(data, 0, data.length);    
    		while(dataRead < length) {
    			int currentlyRead = this.socket.getInputStream().read(data, dataRead, length - dataRead);
    			if(currentlyRead <= 0) {
    				return null;
    			}
    			dataRead += currentlyRead;
    		}
            
            
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Read " + dataRead + " data on the " + this.type + this);
            }
            if (dataRead < 0)
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("The end of the stream has been reached on " + this.type + this);
                }
                return null;
            }
        }
        catch (IOException e)
        {
            if (!this.disconnectionRequested)
            {
                LOG.log(Level.SEVERE, "Link disconnection detected on " + this.type + this + ":" + e.getMessage(), e);
            }
            return null;
        }

        // SLEAPIJ-14
        if (LOG.isLoggable(Level.FINEST))
        {
        	EE_GenStrUtil.print("Read from socket on " + this.type + this + ": ", data);
        }
        return data;
    }
    
    @Override
    protected void threadMain() 
    {
    	try 
    	{
    		super.threadMain();
    	}
    	catch(Throwable t)
    	{
    		LOG.log(Level.SEVERE, "Local link, exception in thread main " + this.type, t);
    	}
    	LOG.fine("Local link, leave thread main of " + this.type);
    }
}
