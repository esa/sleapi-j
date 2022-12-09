package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TimeoutProcessor;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_TMLData;
import esa.sle.impl.api.apipx.pxspl.IEE_ChannelInform;
import esa.sle.impl.api.apipx.pxspl.IEE_ChannelInitiate;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_ISP1ProtocolAbortOriginator;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLTimer;
import esa.sle.impl.ifs.gen.EE_LogMsg;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.time.EE_Duration;
import esa.sle.impl.ifs.time.EE_ElapsedTimer;

public abstract class EE_APIPX_Channel implements IEE_ChannelInitiate, ISLE_TimeoutProcessor, ISLE_TraceControl
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_Channel.class.getName());

    protected ITMLState channelState;

    protected EE_APIPX_TCPCommMng commMng;

    protected boolean localPeerAbort;

    protected ReentrantLock objMutex;

    private ISLE_Reporter reporter;

    protected Socket connectedSock;

    protected ServerSocket serverSocket;

    /**
     * Records whether the channel is using the heartbeat mechanism or not.
     */
    protected boolean usingHBT;

    /**
     * Contains the heartbeat transmit interval duration.
     */
    protected EE_Duration hbtDuration;

    /**
     * Contains the heartbeat receive interval duration.
     */
    protected EE_Duration hbrDuration;

    protected EE_Duration tmsDuration;

    protected SLE_TraceLevel traceLevel;

    protected ISLE_Trace trace;

    private final EE_ElapsedTimer tmsTimer;

    private final EE_ElapsedTimer cpaTimer;

    private final EE_ElapsedTimer hbtTimer;

    private final EE_ElapsedTimer hbrTimer;

    private final int[] timerInvokeIDS;

    public IEE_ChannelInform inform;

    protected EE_APIPX_Database db;

    protected boolean isFirstPDU;

    protected EE_APIPX_TMLMessageFactory tmlMsgFactory;

    protected int urgentByte;

    private boolean threadsRunning;

    protected volatile boolean aboutToClose;
    
    private volatile boolean forwardingRcvData = false; // SLEAPIJ-27
    
    private ReentrantLock forwardingSndDataLock;  // SLEAPIJ-27

    public EE_APIPX_Channel()
    {
        this.channelState = new EE_APIPX_ClosedState(this);
        this.localPeerAbort = false;
        this.reporter = null;
        this.connectedSock = null;
        this.serverSocket = null;
        this.usingHBT = false;
        this.hbtDuration = null;
        this.hbrDuration = null;
        this.tmsDuration = null;
        this.traceLevel = SLE_TraceLevel.sleTL_low;
        this.trace = null;
        this.tmsTimer = new EE_ElapsedTimer();
        this.cpaTimer = new EE_ElapsedTimer();
        this.hbrTimer = new EE_ElapsedTimer();
        this.hbtTimer = new EE_ElapsedTimer();
        this.timerInvokeIDS = new int[4];
        Arrays.fill(this.timerInvokeIDS, 0);
        this.inform = null;
        this.db = null;
        this.isFirstPDU = false;
        this.tmlMsgFactory = new EE_APIPX_TMLMessageFactory();
        this.urgentByte = -1;
        this.objMutex = new ReentrantLock();
        this.threadsRunning = false;
        this.commMng = new EE_APIPX_TCPCommMng(this);
        this.aboutToClose = false;
        this.forwardingSndDataLock = new ReentrantLock(); // SLEAPIJ-27 we cannot use objLock - it is also used for reading
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IEE_ChannelInitiate.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_TimeoutProcessor.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_TraceControl.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
     * Provides the database and reporter interface needed by the channel - see
     * the documentation of IEE_ChannelInitiate.
     */
    @Override
    public void configure(ISLE_Reporter preporter, EE_APIPX_Database pdatabase)
    {
        this.objMutex.lock();

        if (this.reporter != null || this.db != null)
        {
            logInvokeError("configure");
        }
        else
        {
            this.reporter = preporter;
            this.db = pdatabase;
        }

        this.objMutex.unlock();
    }

    @Override
    public void processTimeout(Object timer, int invocationId)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("---------------PROCESS TIMEOUT--------------");
        }

        if (this.hbrTimer.equals(timer)
            && this.timerInvokeIDS[EE_APIPX_TMLTimer.eeAPIPXtt_HBR.getCode()] == invocationId)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("HBR Timer expired. Timer object: " + timer + " forwarding rcv data state: " + this.forwardingRcvData);
            }

            if(this.forwardingRcvData == false) // SLEAPIJ-27 ignore during forwarding state
            {
	            if (this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_high.getCode())
	            {
	                String sb = "HBR Timeout in " + this.channelState.toString() + " state";
	                trace(EE_LogMsg.TMLTR_TIMEOUT.getCode(), SLE_TraceLevel.sleTL_full, sb);
	            }
	
	            logError(EE_LogMsg.TMLCONNECTEDTIMEOUT.getCode());
	
	            this.channelState.hbrTimeout();
            }
            else
            {
            	startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR); // restart the HBR timer when forwarding
            }
        }
        else if (this.hbtTimer.equals(timer)
                 && this.timerInvokeIDS[EE_APIPX_TMLTimer.eeAPIPXtt_HBT.getCode()] == invocationId)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("HBT Timer expired");
            }

            if (this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_high.getCode())
            {
                String sb = "HBT Timeout in " + this.channelState.toString() + " state";
                trace(EE_LogMsg.TMLTR_TIMEOUT.getCode(), SLE_TraceLevel.sleTL_full, sb);
            }

            this.channelState.hbtTimeout();
        }
        else if (this.tmsTimer.equals(timer)
                 && this.timerInvokeIDS[EE_APIPX_TMLTimer.eeAPIPXtt_TMS.getCode()] == invocationId)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("TMS Timer expired");
            }

            if (this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_high.getCode())
            {
                String sb = "TMS Timeout in " + this.channelState.toString() + " state";
                trace(EE_LogMsg.TMLTR_TIMEOUT.getCode(), SLE_TraceLevel.sleTL_full, sb);
            }

            this.channelState.tmsTimeout();
        }
        else if (this.cpaTimer.equals(timer)
                 && this.timerInvokeIDS[EE_APIPX_TMLTimer.eeAPIPXtt_CPA.getCode()] == invocationId)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("CPA expired");
            }

            if (this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_high.getCode())
            {
                String sb = "CPA Timeout in " + this.channelState.toString() + " state";
                trace(EE_LogMsg.TMLTR_TIMEOUT.getCode(), SLE_TraceLevel.sleTL_full, sb);
            }

            this.channelState.cpaTimeout();
        }
    }

    @Override
    public void handlerAbort(Object timer)
    {
        // Nothing to do here
    }

    protected void startTimer(EE_APIPX_TMLTimer which)
    {
    
    	 if (LOG.isLoggable(Level.FINEST))
         {
             LOG.finest("Start Timer hbt and hbr durations: "+this.hbtDuration.getSeconds()+" "+this.hbrDuration.getSeconds());
         }	

        try
        {
            if (this.timerInvokeIDS[which.getCode()] == 0)
            {
                switch (which)
                {
                case eeAPIPXtt_HBT:
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("Start the HBT Timer " + this.hbtDuration);
                    }					
                    if (this.hbtDuration.getSeconds() > 0)
                    {
                    	try
                    	{
                    		this.hbtTimer.start(this.hbtDuration, this, ++this.timerInvokeIDS[which.getCode()]);
                    	}
                    	catch(Exception e)
                    	{
                    		// OK , may have been cancelled already
                    	}
                    } else{
                    	 if (LOG.isLoggable(Level.FINEST))
                         {
                             LOG.finest("HBT Duration is 0");
                         }
                    	 return;
                    }
                    break;
                }
                case eeAPIPXtt_HBR:
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("Start the HBR Timer " + this.hbrDuration);
                    }					
                    if (this.hbrDuration.getSeconds()>0){
                    	this.hbrTimer.start(this.hbrDuration, this, ++this.timerInvokeIDS[which.getCode()]);
                    }else{
                    	 if (LOG.isLoggable(Level.FINEST))
                         {
                             LOG.finest("HBR Duration is 0");
                         }
                    	 return;
                    }
                    break;
                }
                case eeAPIPXtt_TMS:
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("Start the TMS Timer " + this.tmsDuration);
                    }

                    this.tmsTimer.start(this.tmsDuration, this, ++this.timerInvokeIDS[which.getCode()]);
                    break;
                }
                case eeAPIPXtt_CPA:
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("Start the CPA Timer " + this.hbrDuration);
                    }

                    this.cpaTimer.start(this.hbrDuration, this, ++this.timerInvokeIDS[which.getCode()]);
                    break;
                }
                default:
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("No timer found ");
                    }
                    break;
                }
                }
            }
            else if (this.timerInvokeIDS[which.getCode()] > 0)
            {
                switch (which)
                {
                case eeAPIPXtt_HBT:
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("Restart the HBT Timer " + this.hbtDuration);
                    }					
                    if (this.hbtDuration.getSeconds()>0){
                    	this.hbtTimer.restart(this.hbtDuration, ++this.timerInvokeIDS[which.getCode()]);
                    }else{
                    	 if (LOG.isLoggable(Level.FINEST))
                         {
                             LOG.finest("HBT Duration is 0");
                         }
                    	 return;
                    }

                    break;
                }
                case eeAPIPXtt_HBR:
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("Restart the HBR Timer " + this.hbrDuration + " timer object: " + this.hbrTimer);
                    }					
                    if (this.hbrDuration.getSeconds()>0){
                    	this.hbrTimer.restart(this.hbrDuration, ++this.timerInvokeIDS[which.getCode()]);
                    } else{
                    	 if (LOG.isLoggable(Level.FINEST))
                         {
                             LOG.finest("HBR Duration is 0");
                         }
                    	 return;
                    }
                    break;
                }
                case eeAPIPXtt_TMS:
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("Restart the TMS Timer " + this.hbrDuration);
                    }

                    this.tmsTimer.restart(this.hbrDuration, ++this.timerInvokeIDS[which.getCode()]);
                    break;
                }
                case eeAPIPXtt_CPA:
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("Restart the CPA Timer " + this.hbrDuration);
                    }

                    this.cpaTimer.restart(this.hbrDuration, ++this.timerInvokeIDS[which.getCode()]);
                    break;
                }
                default:
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("No timer found");
                    }
                    break;
                }
                }
            }
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            assert (e.getHResult() != HRESULT.S_OK) : "BAD TIMER INVOCATION";
        }
        catch (Throwable t) {
        	LOG.log(Level.SEVERE, "Exception starting timer " + which + ": ", t);
        }
    }

    /**
     * Requests a timer to be stopped from one of the four timers.
     */
    protected void stopTimer(EE_APIPX_TMLTimer which)
    {
        switch (which)
        {
        case eeAPIPXtt_HBT:
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Cancel the HBT Timer");
            }
            this.hbtTimer.cancel();
            break;
        }
        case eeAPIPXtt_HBR:
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Cancel the HBR Timer");
            }

            this.hbrTimer.cancel();
            break;
        }
        case eeAPIPXtt_TMS:
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Cancel the TMS Timer");
            }

            this.tmsTimer.cancel();
            break;
        }
        case eeAPIPXtt_CPA:
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Cancel the CPA Timer");
            }

            this.cpaTimer.cancel();
            break;
        }
        }

        this.timerInvokeIDS[which.getCode()] = 0;
    }

    /**
     * @param newState
     */
    public void setChannelState(ITMLState newState)
    {
        this.objMutex.lock();

        if (this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_low.getCode())
        {
            trace(EE_LogMsg.EE_SE_LM_StateChange.getCode(),
                  SLE_TraceLevel.sleTL_low,
                  this.channelState.toString(),
                  newState.toString());
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Changing the channel state from: " + this.channelState + ", to: " + newState);
        }

        this.channelState = newState;

        this.objMutex.unlock();
    }

    /**
     * Requests a PDU be sent, see the documentation of IEE_ChannelInitiate
     * @return SLE_S_TRANSMITTED
     */
    @Override
    public HRESULT sendSLEPDU(byte[] data, boolean last)
    {
        this.channelState.delSLEPDUReq(this.tmlMsgFactory.createPDUMsg(data), last);
        return HRESULT.SLE_S_TRANSMITTED;
    }

    @Override
    public void sendDisconnect()
    {
        this.channelState.hlDisconnectReq();
    }

    /**
     * Requests to connect - see the documentation of IEE_ChannelInitiate
     */
    @Override
    public void sendConnect(String rspPortId)
    {
        this.channelState.hlConnectReq(rspPortId);
    }

    /**
     * Requests a peer abort be sent to the remote socket - see the
     * documentation of IEE_ChannelInitiate
     */
    @Override
    public void sendPeerAbort(int diagnostic)
    {
        this.channelState.hlPeerAbortReq(diagnostic);
    }

    /**
     * Requests that the current connection be reset - see the documentation of
     * IEE_ChannelInitiate.
     */
    @Override
    public void sendReset()
    {
        trace(EE_LogMsg.TMLTR_SENDRESET.getCode(), SLE_TraceLevel.sleTL_medium);
        this.channelState.hlResetReq();
    }

    @Override
    public void suspendReceive()
    {
        // Nothing to do for this implementation
    }

    @Override
    public void resumeReceive()
    {
        // Nothing to do for this implementation
    }

    public void tcpError(int code, boolean traceAlso, String... param)
    {
        this.channelState.tcpError(code, traceAlso, param);
    }

    public void manageHbrTimeOut()
    {
        this.channelState.hbrTimeout();
    }

    public void manageBadFormattedMsg()
    {
        this.channelState.manageBadFormMsg();
    }

    public void updateTimeoutOptions(EE_APIPX_CtxMessage msg)
    {
        this.channelState.tcpDataInd(msg);
    }

    public void pduReceived(EE_APIPX_PDUMessage msg)
    {
    	this.forwardingRcvData = true; // SLEAPIJ-27 the call below may block. HBR timeout uring this time must be ignored
    	try
    	{
    		this.channelState.tcpDataInd(msg);
    	}
    	finally
    	{
    		this.forwardingRcvData = false; // SLEAPIJ-27
    	}
    }

    public void hbtReceived(EE_APIPX_HBMessage msg)
    {
        this.channelState.tcpDataInd(msg);
    }

    /**
     * Forwards the SLE-PDU to the application
     * 
     * @param data
     */
    protected void forwardPDU(byte[] data)
    {
        IEE_ChannelInform chInform = getChannelInform();
        if (chInform != null)
        {
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("Forwarding PDU (length: " + data.length + " received on channel " + this);
            }
            chInform.rcvSLEPDU(data);
        }
    }

    public void cleanup()
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Cleanup invoked on channel " + this);
        }

        stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBT);
        stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
        stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_TMS);
        stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_CPA);

        this.commMng.stopThreads();

        // #hd# make this thread-safe
        this.objMutex.lock();
        final Socket cs = this.connectedSock;
        this.connectedSock = null;
        this.objMutex.unlock();
        
        if (cs != null)
        {
            try
            {
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.fine("Ready to close socket " + cs);
                }
                cs.close();
            }
            catch (IOException e)
            {
                LOG.log(Level.SEVERE, "Failure while closing the socket on channel " + this, e);
                String msg = "Failure while closing the socket";
                // logError(EE_LogMsg.TMLTR_IOEVENT.getCode(), true, msg);
                tcpError(EE_LogMsg.TMLTR_IOEVENT.getCode(), true, msg);
            }

            
        }

        this.objMutex.lock();
        if (this.trace != null)
        {
            this.trace = null;
        }

        if (this.reporter != null)
        {
            this.reporter = null;
        }

        if (this.inform != null)
        {
            this.inform = null;
        }
        this.objMutex.unlock();
    }

    public void sendPDU(EE_APIPX_TMLMessage pduMsg)
    {
        this.objMutex.lock();

        if (this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_high.getCode())
        {
            trace(EE_LogMsg.TMLTR_SENDPDU.getCode(), SLE_TraceLevel.sleTL_full);
        }

        EE_APIPX_TCPCommMng tmpCommMng = this.commMng; // SLEAPIJ-27

        this.objMutex.unlock();
        
        this.forwardingSndDataLock.lock(); // if HB transmission blocks, we wait here.        
        try
        {
        	tmpCommMng.sendMsg(pduMsg); // SLEAPIJ-27 this call potentially blocks. Don't block under obLock, also used for reading - HBR timeout can occur!
        }
        finally
        {
	        this.forwardingSndDataLock.unlock();
        }
    }

    public void sendHbMsg()
    {
        this.objMutex.lock();
        EE_APIPX_TCPCommMng tmpCommMng = this.commMng; // SLEAPIJ-27
        this.objMutex.unlock();
        
        if(tmpCommMng != null && this.forwardingSndDataLock.tryLock())
        {
        	try
        	{
        		trace(EE_LogMsg.TMLTR_SENDPDU.getCode(), SLE_TraceLevel.sleTL_full);
		        tmpCommMng.sendMsg(new EE_APIPX_HBMessage());  // SLEAPIJ-27 this call potentially blocks. Don't block under objLock
        	}
        	finally
        	{
        		this.forwardingSndDataLock.unlock();
        	}
        }
        else
        {
        	LOG.fine("Discard HB to send, data is being sent out");
        }
    }


    /**
     * This is called before the connection is established and provides the
     * channel inform interface which will receive PDUs and Peer and Protocol
     * Aborts - see the documentation of IEE_ChannelInitiate.
     */
    @Override
    public void setChannelInform(IEE_ChannelInform channelInform)
    {
        this.objMutex.lock();
        try
        {
            if (this.inform == null)
            {
                this.inform = channelInform;
            }
            else
            {
                logInvokeError("setChannelInform");
            }
        }
        finally
        {
            this.objMutex.unlock();
        }
    }

    private IEE_ChannelInform getChannelInform()
    {
        IEE_ChannelInform channelInform = null;
        this.objMutex.lock();
        try
        {
            channelInform = this.inform;
        }
        finally
        {
            this.objMutex.unlock();
        }
        return channelInform;
    }

    /**
     * @param sock
     * @param hbt
     * @param hbr
     */
    protected void setConnectedSocket(Socket sock, ServerSocket ssock, int hbt, int hbr)
    {
        this.objMutex.lock();

        this.connectedSock = sock;
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("--------- CONNECTED SOCKET SET : " + this.connectedSock.getInetAddress() + ", "
                       + this.connectedSock.getPort() + ", hbt = " + hbt + " seconds, hbr = " + hbr + " seconds");
        }

        this.serverSocket = ssock;
        this.hbtDuration = new EE_Duration(hbt);
        this.hbrDuration = new EE_Duration(hbr);
        this.tmsDuration = this.hbrDuration;
        EE_APIPX_TMLData ptml = this.db.getTMLData();

        this.usingHBT = !ptml.getNonUseHB();

        this.objMutex.unlock();
    }

    public Socket getConnectedSock()
    {
        this.objMutex.lock();
        Socket cs = this.connectedSock;
        this.objMutex.unlock();
        return cs;
    }

    public void hlConnectedInd()
    {
        IEE_ChannelInform chInform = getChannelInform();
        if (chInform != null)
        {
            chInform.rcvConnect();
        }

    }

    public void startCommThreads()
    {
        this.objMutex.lock();
        if (!this.threadsRunning)
        {
            this.commMng.startThreads();
            this.threadsRunning = true;
        }
        this.objMutex.unlock();
    }

    // ////////////////////////////////////////////////////////////

    /**
     * Sends a rcvPeerAbort or rcvProtocolAbort to the channel inform interface
     * if present
     * 
     * @param originator
     * @param diagnosticByte
     * @param isPeerAbort
     * @param error_code
     */
    protected void forwardAbort(EE_APIPX_ISP1ProtocolAbortOriginator originator,
                                int diagnosticByte,
                                boolean isPeerAbort,
                                int errorCode)
    {
        if (isLocalPeerAbort())
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("The peer has confirmed that its side of connection has been closed");
            }

            this.channelState.tcpDisconnectInd();
            return;
        }
        IEE_ChannelInform chInform = getChannelInform();

        if (chInform != null)
        {
            if (isPeerAbort)
            {
                logError(EE_LogMsg.TMLABORTRCV.getCode(), true, SLE_PeerAbortDiagnostic.getDiagByCode(diagnosticByte)
                        .toString());
                chInform.rcvPeerAbort(diagnosticByte, (originator == EE_APIPX_ISP1ProtocolAbortOriginator.localTML));
            }
            else
            {
                logError(EE_LogMsg.TMLABORTRCV.getCode(), true);
                EE_APIPX_ISP1ProtocolAbortDiagnostics diag = new EE_APIPX_ISP1ProtocolAbortDiagnostics(originator,
                                                                                                       diagnosticByte,
                                                                                                       errorCode);
                chInform.rcvProtocolAbort(diag);
            }
        }
    }

    public void tcpAbortReq()
    {
        this.objMutex.lock();
        try
        {
        	if(this.connectedSock != null)
        	{
        		this.connectedSock.close();
        	}
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
            String msg = "Failure while closing the socket";
            // logError(EE_LogMsg.TMLTR_IOEVENT.getCode(), true, msg);
            tcpError(EE_LogMsg.TMLTR_IOEVENT.getCode(), true, msg);
        }
        finally
        {
            this.objMutex.unlock();
        }
    }

    public void tcpAbortInd()
    {
        this.channelState.tcpAbortInd();
    }

    public void peerAbortInd(EE_APIPX_TMLMessage msg)
    {
        this.channelState.tcpUrgentDataInd();
        setUrgentByte(((EE_APIPX_UrgentByteMessage) msg).getUBDiagnostic());
        this.channelState.tcpDataInd(msg);
    }

    public void peerAbortReq(int diag)
    {
        this.objMutex.lock();
        try
        {
            if (this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode())
            {
                String msg = " (" + diag + ") ";
                trace(EE_LogMsg.TMLTR_SENDABORT.getCode(), SLE_TraceLevel.sleTL_medium, msg);
            }
            // write urgent data with diagnostic
            this.connectedSock.sendUrgentData(diag);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("URGENT DATA SENT");
            }
            // stop the hbt and hbr timers
            stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBR);
            stopTimer(EE_APIPX_TMLTimer.eeAPIPXtt_HBT);
            // start the CPA timer
            startTimer(EE_APIPX_TMLTimer.eeAPIPXtt_CPA);

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("CPA timer started");
            }

            // the peer has to close its side of connection before the CPA timer
            // expires
            // otherwise a TCP-ABORT is invoked
        }
        catch (IOException e)
        {
            LOG.log(Level.FINE, "IOException ", e);
            String msg = "Failure while sending urgent data";
            // logError(EE_LogMsg.TMLTR_IOEVENT.getCode(), true, msg);
            tcpError(EE_LogMsg.TMLTR_IOEVENT.getCode(), true, msg);
        }
        finally
        {
            this.objMutex.unlock();
        }
    }

    // ////////////////////////////////////////////////////////////

    /**
     * Logs the error specified.
     * 
     * @param Error
     * @param traceAlso
     * @param param
     */
    protected void logError(int Error, boolean traceAlso, String... param)
    {
        String msg = EE_MessageRepository.getMessage(Error, param);
        if (this.reporter != null)
        {
            this.reporter.logRecord(SLE_Component.sleCP_proxy, null, SLE_LogMessageType.sleLM_alarm, Error, msg);
        }

        if (this.trace != null && traceAlso)
        {
            if (this.connectedSock != null)
            {
                StringBuilder sb = new StringBuilder(msg);
                sb.append(" ");
                sb.append(this.connectedSock.toString());
                sb.append('\n');
            }
            this.trace.traceRecord(SLE_TraceLevel.sleTL_low, SLE_Component.sleCP_proxy, null, msg);
        }
    }

    /**
     * An internal utility function that logs that an inappropriate invocation
     * was made.
     * 
     * @param invocation
     */
    protected void logInvokeError(String invocation)
    {
        String msg = EE_MessageRepository.getMessage(EE_LogMsg.TMLBADINVOCATION.getCode(), invocation);
        if (this.reporter == null)
        {
            return;
        }
        else
        {
            this.reporter.logRecord(SLE_Component.sleCP_proxy,
                                    null,
                                    SLE_LogMessageType.sleLM_alarm,
                                    EE_LogMsg.TMLBADINVOCATION.getCode(),
                                    msg);
        }

        if (this.trace != null)
        {
            this.trace.traceRecord(SLE_TraceLevel.sleTL_low, SLE_Component.sleCP_proxy, null, msg);
        }
    }

    /**
     * Logs the error specified.
     * 
     * @param Error
     * @param traceAlso
     * @param param0
     * @param param1
     * @param param2
     */
    protected void logError(int Error)
    {
        logError(Error, true);
    }

    /**
     * Begins tracing - see the documentation of ISLE_TraceControl.
     * 
     * @param trace
     * @param level
     * @param forward
     * @return
     */
    @Override
    public void startTrace(ISLE_Trace trace, SLE_TraceLevel level, boolean forward) throws SleApiException
    {
        this.objMutex.lock();

        this.trace = trace;
        this.traceLevel = level;
        if (this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_high.getCode() && this.connectedSock != null)
        {
            trace(EE_LogMsg.TMLTR_TRACEON.getCode(), SLE_TraceLevel.sleTL_high, this.connectedSock.toString());
        }

        this.objMutex.unlock();
    }

    @Override
    public void stopTrace() throws SleApiException
    {
        this.objMutex.lock();

        if (this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_high.getCode() && this.connectedSock != null)
        {
            trace(EE_LogMsg.TMLTR_TRACEOFF.getCode(), SLE_TraceLevel.sleTL_high, this.connectedSock.toString());
        }
        if (this.trace != null)
        {
            this.trace = null;
        }

        this.objMutex.unlock();
    }

    /**
     * Traces the message ID specified.
     * 
     * @param msgid
     * @param level
     * @param param
     */
    public void trace(int msgid, SLE_TraceLevel level, String... param)
    {
        if (this.trace == null)
        {
            return;
        }

        if (level.getCode() <= this.traceLevel.getCode())
        {
            String msg = EE_MessageRepository.getMessage(msgid, param);
            if (this.connectedSock != null)
            {
                StringBuilder sb = new StringBuilder(msg);
                sb.append(" ");
                sb.append(this.connectedSock.toString());
                sb.append('\n');
                msg = sb.toString();
            }

            this.trace.traceRecord(level, SLE_Component.sleCP_proxy, null, msg);
        }
    }

    // GETTERS AND SETTERS

    public void setHbtDuration(EE_Duration hbtDuration)
    {
        this.hbtDuration = hbtDuration;
    }

    public void setHbrDuration(EE_Duration hbrDuration)
    {
        this.hbrDuration = hbrDuration;
    }

    public SLE_TraceLevel getTraceLevel()
    {
        return this.traceLevel;
    }

    public EE_APIPX_Database getDb()
    {
        return this.db;
    }

    public void setFirstPDU(boolean isFirstPDU)
    {
        this.isFirstPDU = isFirstPDU;
    }

    public boolean isLocalPeerAbort()
    {
        this.objMutex.lock();
        boolean tmp = this.localPeerAbort;
        this.objMutex.unlock();
        return tmp;
    }

    public void setLocalPeerAbort(boolean localPeerAbort)
    {
        this.objMutex.lock();
        this.localPeerAbort = localPeerAbort;
        this.objMutex.unlock();
    }

    public boolean isUsingHBT()
    {
    	 if (LOG.isLoggable(Level.FINEST))
         {
             LOG.finest("isUsingHBT value " + this.usingHBT);
         }	
    	
        return this.usingHBT;
    }

    public void setUsingHBT(boolean usingHBT)
    {
    	 if (LOG.isLoggable(Level.FINEST))
         {
             LOG.finest("usingHBT is set to: " + usingHBT);
         }	
        this.usingHBT = usingHBT;
    }

    public EE_APIPX_TMLMessageFactory getTmlMsgFactory()
    {
    	 if (LOG.isLoggable(Level.FINEST))
         {
    		 LOG.finest("Thread: "+Thread.currentThread().getId()+" getTmlMsgFactory executed" );
         }
        return this.tmlMsgFactory;
    }

    public void setTmlMsgFactory(EE_APIPX_TMLMessageFactory tmlMsgFactory)
    {
        this.tmlMsgFactory = tmlMsgFactory;
    }

    public int getUrgentByte()
    {
        this.objMutex.lock();
        int ub = this.urgentByte;
        this.objMutex.unlock();
        return ub;
    }

    public void setUrgentByte(int urgentByte)
    {
        this.objMutex.lock();
        this.urgentByte = urgentByte;
        this.objMutex.unlock();
    }

    @Override
    public void dispose()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("dispose() invoked on channel " + this);
        }
        cleanup();
    }

    public void onFinalPdu()
    {
        this.aboutToClose = true;
    }

    public boolean isAboutToClose()
    {
        return this.aboutToClose;
    }
}
