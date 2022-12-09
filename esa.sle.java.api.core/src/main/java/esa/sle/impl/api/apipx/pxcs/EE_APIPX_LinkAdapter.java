/**
 * @(#) EE_APIPX_LinkAdapter.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import esa.sle.impl.api.apipx.pxspl.IEE_ChannelInform;
import esa.sle.impl.ifs.gen.EE_CondVar;

/**
 * This class defines methods, which can be used to send or receive message from
 * the IPC link. Some abstract methods must be implemented in the derived
 * classes for the correct use of EE_APIPX_Link objects.
 */
public abstract class EE_APIPX_LinkAdapter
{
    private final int maxQueueSize = 1000; // performance: 1000 needed for high rates

	private static final Logger LOG = Logger.getLogger(EE_APIPX_LinkAdapter.class.getName());
	
	private final ReentrantLock listRcvDataLock = new ReentrantLock();

    /**
     * List of the next received message to be performed by the link object.
     */
    private final BlockingQueue<TransmittableUnit> listRcvData;

    /**
     * Indicates is the link is closed.
     */
    protected boolean linkClosed;

    private final EE_CondVar condVar;

    protected volatile boolean threadRunning = false;


    public EE_APIPX_LinkAdapter()
    {
        this.listRcvData = new LinkedBlockingQueue<TransmittableUnit>(maxQueueSize);
        this.linkClosed = false;
        this.condVar = new EE_CondVar(new ReentrantLock());
    }

    /**
     * The link object calls this function when some data are received on the
     * IPC link.
     */
    public abstract void takeData(byte[] data, int dataType, EE_APIPX_Link pLink, boolean last_pdu);

    /**
     * This function is called when the IPC connection is closed by the peer
     * side or lost.
     */
    public abstract void ipcClosed(EE_APIPX_Link pLink);

    /**
     * This function sends a message on the link and set a timer if necessary
     * (maximum time to wait for a response). This function calls a blocking
     * write, and returns only when the data are sent.
     */
    public HRESULT sendMessage(byte[] data, EE_APIPX_Link link, int timer)
    {
        HRESULT res = HRESULT.E_FAIL;

        if (timer > 0)
        {
            // send the message
            this.condVar.lock();
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("About to send data and wait on link " + link + ", data.length=" + data.length);
            }
            res = sendAndWait(link, data, res);

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Message sent on link " + this + ", result=" + res);
            }
            // wait for the return code
            try
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Waiting for ack on link " + link);
                }
                this.condVar.timeWait(timer, TimeUnit.SECONDS);
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("Timeout or response on link " + link + " received");
                }
                this.condVar.unlock();
            }
            catch (InterruptedException e)
            {
                Thread.interrupted();
            }
        }
        else
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("About to send data with no timeout on link " + link + ", data.length=" + data.length);
            }
            res = sendAndWait(link, data, res);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Message sent on link " + this + ", result=" + res);
            }
        }

        return res;
    }

    private HRESULT sendAndWait(EE_APIPX_Link link, byte[] data, HRESULT result)
    {
        HRESULT res = result;
        if (!this.threadRunning)
        {
            res = link.sndMess(data);
        }
        else
        {
            TransmittableUnit dataToSend = new TransmittableUnit(data);
            try
            {
                //synchronized (this.listRcvData)
            	try // SLEAPIJ-26
                {
            		this.listRcvDataLock.lock();
                    this.listRcvData.put(dataToSend);
                }
            	finally
            	{
            		this.listRcvDataLock.unlock();
            	}
                res = dataToSend.futureResult.get();
            }
            catch (InterruptedException e)
            {
                LOG.log(Level.FINE, "Thread interrupted while trying to add data on the LinkAdapter queue", e);
                Thread.interrupted();
            }
            catch (ExecutionException e)
            {
                LOG.log(Level.WARNING, "Error while sending data to the link", e);
                // This must be a bug
                throw new RuntimeException(e);
            }
        }
        return res;
    }

    public void signalResponseReceived()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Response received, about to signal...");
        }
        this.condVar.lock();
        this.condVar.signalAll();
        this.condVar.unlock();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Response signalled");
        }
    }

    /**
     * This function builds and sends a response message that must be sent on
     * the IPC link.
     */
    protected void sendResultMessage(int messId, HRESULT result, int regid, EE_APIPX_Link link)
    {
        PXCS_Response_Mess rsp = new PXCS_Response_Mess(result, regid);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(rsp.toString());
        }
        byte[] rmByte = rsp.toByteArray();

        PXCS_Header_Mess header = new PXCS_Header_Mess(false, messId, rmByte.length);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(header.toString());
        }

        byte[] data = new byte[rmByte.length + PXCS_Header_Mess.hMsgLength];
        System.arraycopy(header.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
        System.arraycopy(rmByte, 0, data, PXCS_Header_Mess.hMsgLength, rmByte.length);

        sendAndWait(link, data, HRESULT.E_FAIL);
    }

    /**
     * This function waits on the write condition variable. When the condition
     * variable is signaled, this function write data on the link.
     * @return true if something was sent
     */
    protected boolean waitForWrite(EE_APIPX_Link link)
    {
        this.threadRunning = true;
        // retrieve the element from the queue
        TransmittableUnit dataToSend = null;
        try
        {
            //dataToSend = this.listRcvData.take();
        	dataToSend = this.listRcvData.poll(100, TimeUnit.MILLISECONDS); // SLEAPIJ-26
        	if(dataToSend != null)
        	{
        		// send it to the link
        		dataToSend.transmit(link);
        		return true;
        	}        	
        }
        catch (InterruptedException e)
        {
            LOG.log(Level.FINE, "InterruptedException ", e);
        }
        return false;
    }

    /**
     * Waits for write (ie.put the provided data on the queue and calls resumeXmit
     * on the provided ieeChannelInform if the link is writable
     * @param link
     * @param ieeChannelInform
     * @return
     */
    protected boolean waitForWriteAndResumeXmit(EE_APIPX_Link link, IEE_ChannelInform ieeChannelInform)
    {
    	boolean ret = waitForWrite(link);
    	
    	//synchronized (this.listRcvData)

		// the this.listRcvData.put() is done under listRcvDataLock. to avoid a deadlock condition
		// when trying to lock listRcvDataLock with threads holding listRcvDataLock and waiting for listRcvData.put
		// we only try to lock here
		if(this.listRcvDataLock.tryLock()) // SLEAPIJ-26
		{
			try 
			{
				if(this.listRcvData.remainingCapacity() > 0 && ieeChannelInform != null)
				{
					ieeChannelInform.resumeXmit();
				}
			}
			catch(Exception e) // SLEAPIJ-48
			{
				LOG.log(Level.SEVERE, "Exception resuming x-mit", e);
			}
			finally
			{
				this.listRcvDataLock.unlock();
			}
		}
    	
    	return ret;
    }
    
    /**
     * This function allows to send a message on the link in a non blocking mode
     * (the data are no yet sent when it returns).
     */
    protected HRESULT sendMessageNoWait(byte[] data)
    {
    	HRESULT res = HRESULT.SLE_S_TRANSMITTED;
        try
        {
            //synchronized (this.listRcvData)
        	try // SLEAPIJ-26
            {
//        		this.listRcvDataLock.lock();
                this.listRcvData.put(new TransmittableUnit(data, false));
                int remainingCapacity = this.listRcvData.remainingCapacity();
                if(remainingCapacity <= 1) // leave one place for resumeXmit 
                {
                	res = HRESULT.SLE_S_SUSPEND; // SLEAPIJ-26
                }
            }
        	finally
        	{
 //       		this.listRcvDataLock.unlock();
        	}
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
        
        return res;
    }

    /**
     * This function allows to send two messages on the link in a non blocking
     * mode (the data are no yet sent when it returns). The order is preserved
     * and the insertion on the queue is done atomically.
     */
    protected void sendMessageNoWait(byte[] data1, byte[] data2)
    {
        try
        {
            //synchronized (this.listRcvData)
        	try
            {
        		this.listRcvDataLock.lock();
                this.listRcvData.put(new TransmittableUnit(data1));
                this.listRcvData.put(new TransmittableUnit(data2));
            }
        	finally
        	{
        		this.listRcvDataLock.unlock();
        	}
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
    }


    private class TransmittableUnit
    {
        private final CompletableFuture<HRESULT> futureResult;

        private final byte[] data;


        private TransmittableUnit(byte[] data)
        {
            this.data = data;
            this.futureResult = new CompletableFuture<HRESULT>();
        }

        /**
         * Allow to create a tranmittable unit w/o completable future for performance reasons
         * @param data The data to be transmitted
         * @param completeFuture true: create a futureResult, false: futureResult is null
         */
        private TransmittableUnit(byte[] data, boolean completeFuture)
        {
            this.data = data;
            if(completeFuture == true)
            {
            	this.futureResult = new CompletableFuture<HRESULT>();
            }
            else
            {
            	this.futureResult = null;
            }
        }
        
        public void transmit(EE_APIPX_Link link)
        {
            HRESULT result = link.sndMess(this.data);
            if(this.futureResult != null) 
            {
            	this.futureResult.complete(result);
            }
        }
    }

}
