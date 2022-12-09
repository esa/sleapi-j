package esa.sle.impl.api.apipx.pxspl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_PeerAbort;
import ccsds.sle.api.isle.iop.ISLE_TransferBuffer;
import ccsds.sle.api.isle.ipx.ISLE_SrvProxyInitiate;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIAdmin;
import ccsds.sle.api.isle.ise.ISLE_SrvProxyInform;
import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_AssocState;
import ccsds.sle.api.isle.it.SLE_BindRole;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_SecAttributes;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_LocalApplData;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_PeerApplData;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_PeerApplDataList;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_ProxySettings;
import esa.sle.impl.api.apipx.pxdb.SLE_AuthenticationMode;
import esa.sle.impl.api.apipx.pxdel.EE_APIPX_PDUTranslator;
import esa.sle.impl.api.apipx.pxspl.types.EE_APIPX_Event;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_ISP1ProtocolAbortDiagnostics;
import esa.sle.impl.ifs.gen.EE_APIOpSequencer;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class EE_APIPX_Association implements the interfaces exported by the
 * component class 'Association', defined in reference [SLE-API]. It is
 * responsible for : - maintaining a send-queue of operations received via the
 * interface ISLE_SrvProxyInitiate. The size of the send-queue is controlled
 * with the database's size queue. - passing the encoded PDU's to the Transport
 * Mapping Layer (TML) when the flow control is ok. The flow control of the
 * association is managed with the suspendXmit attribute. - passing the received
 * operation invocations and returns to the service instance. - state table
 * processing common to initiating and responding associations. - removal of
 * Transfer Buffer PDU's on request. - generation of log records. - generation
 * of trace records that are related to the association. The following tasks are
 * delegated to the class EE_APIPX_PDUTranslator: - encoding and decoding of the
 * operation objects. - decoding PDU's received from the TML. - maintening a
 * list of pending returns. The following tasks are delegated to the TML: -
 * mapping of port identifiers to technology specific address information. -
 * monitoring of the state of the data communication connection. The class is a
 * base-class for further refined association classes, where the base-class
 * provides common functionality for derived classes.@EndResponsibility After
 * receiving an operation from the ISLE_SrvProxyInitiate, or receiving a PDU
 * from TML, the derived classes InitiatingAssoc and RespondingAssoc call some
 * common functionalities of the Association class. The association receives PDU
 * from TML through the IEE_ChannelInform interface, and if all the checks are
 * ok (calls of the state machine), it forwards the decoded operation to the
 * service element through the ISLE_SrvProxyInform. On the other way, for
 * operations received from the ISLE_SrvProxyInitiate interface, the association
 * : - calls the sequencer. - calls the state machine which will call other
 * functions of the association depending of the state of the association and
 * the type of operation. The Association object can be accessed by several
 * threads. To maintain the integrity of the sending queue and of the sequence
 * counting, two mutex are needed.
 */
public abstract class EE_APIPX_Association implements ISLE_SrvProxyInitiate, ISLE_TraceControl, IEE_ChannelInform
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_Association.class.getName());

    /**
     * the pointer to the ISLE_SrvProxyInform interface.
     */
    private ISLE_SrvProxyInform srvProxyInform;

    /**
     * The pointer to the IEE_ChannelInitiate interface.
     */
    protected volatile IEE_ChannelInitiate channelInitiate;

    /**
     * Pointer to the ISLE_Trace interface.
     */
    protected volatile ISLE_Trace trace;

    /**
     * The service type handled by the association.
     */
    protected volatile SLE_ApplicationIdentifier serviceType;

    /**
     * The state of the association.
     */
    private volatile SLE_AssocState state;

    /**
     * The state of the association when the state is unbound. Can be
     * "Disconnected" or "Connect Pend".
     */
    protected volatile boolean unboundStateIsDisconnected;

    /**
     * The sequence counter used when transmitting the operation to the service
     * element through the ISLE_SrvProxyInform interface..
     */
    private volatile long sequenceCounter;

    /**
     * Indicates if the traces had been started for the association.
     */
    protected volatile boolean traceStarted;

    /**
     * Trace level.
     */
    protected volatile SLE_TraceLevel traceLevel;

    /**
     * The role of the association.
     */
    protected volatile SLE_BindRole role;

    /**
     * Indicates if the sending of PDU to TML is suspended or not.
     */
    protected volatile boolean suspendXmit;

    protected volatile EE_APIPX_Database database;

    protected volatile ISLE_Reporter reporter;

    protected volatile ISLE_OperationFactory opFactory;

    protected volatile ISLE_UtilFactory utilFactory;

    /**
     * The authentication mode set in the association.
     */
    protected volatile SLE_AuthenticationMode authenticationMode;

    /**
     * Indicates if the association has been aborted.
     */
    protected volatile boolean isAborted;

    /**
     * Indicates if the association has been released.
     */
    protected volatile boolean isReleased;

    protected volatile int queueSize;

    protected volatile int version;

    protected volatile EE_APIOpSequencer oPSequencer;

    protected volatile EE_APIPX_Proxy proxy;

    private final LinkedList<PXSPL_Operation> iOperation;

    protected volatile EE_APIPX_PDUTranslator pduTranslator;

    private volatile ISLE_SecAttributes iSecAttr;

    protected volatile ReentrantLock objMutex;

    protected final ReentrantLock innerLock = new ReentrantLock();
    
    protected final String instanceId;

    public EE_APIPX_Association(String instanceKey, EE_APIPX_Proxy proxy)
    {
    	this.instanceId = instanceKey;
    	this.srvProxyInform = null;
        this.channelInitiate = null;
        this.trace = null;
        this.serviceType = SLE_ApplicationIdentifier.sleAI_invalid;
        this.state = SLE_AssocState.sleAST_unbound;
        this.unboundStateIsDisconnected = true;
        this.sequenceCounter = 0;
        this.traceStarted = false;
        this.traceLevel = SLE_TraceLevel.sleTL_low;
        this.role = SLE_BindRole.sleBR_initiator;
        this.suspendXmit = true;
        this.database = null;
        this.reporter = null;
        this.opFactory = null;
        this.utilFactory = null;
        this.authenticationMode = SLE_AuthenticationMode.sleAM_none;
        this.isAborted = false;
        this.isReleased = false;
        this.queueSize = 1;
        this.version = 0;
        this.oPSequencer = new EE_APIOpSequencer();
        this.proxy = proxy;
        this.iOperation = new LinkedList<PXSPL_Operation>();
        this.pduTranslator = null;
        this.iSecAttr = null;
        this.objMutex = new ReentrantLock();

        initAssoc();
    }

    /**
     * Starts tracing for the association, and forwards the StartTrace to the
     * channel object (TML) and to the PDU translator. S_OK Tracing is started.
     * SLE_E_STATE The tracing is already active. E_FAIL The request fails due
     * to a further unspecified error.
     */
    @Override
    public void startTrace(ISLE_Trace trace, SLE_TraceLevel level, boolean forward) throws SleApiException
    {
        HRESULT res = HRESULT.S_OK;
        if (this.traceStarted)
        {
            return;
        }

        if (trace == null)
        {
            throw new SleApiException(HRESULT.E_INVALIDARG);
        }

        // check the trace level
        assert (level.getCode() >= SLE_TraceLevel.sleTL_low.getCode() && level.getCode() <= SLE_TraceLevel.sleTL_full
                .getCode()) : "Trace level unknown";
        if (level.getCode() < SLE_TraceLevel.sleTL_low.getCode()
            || level.getCode() > SLE_TraceLevel.sleTL_full.getCode())
        {
            throw new SleApiException(HRESULT.E_INVALIDARG);
        }

        if (this.channelInitiate != null)
        {
            // get the trace control interface of the channel

            ISLE_TraceControl pTraceControl = this.channelInitiate.queryInterface(ISLE_TraceControl.class);
            if (pTraceControl != null)
            {
                try
                {
                    pTraceControl.startTrace(trace, level, forward);
                }
                catch (SleApiException e)
                {
                    res = e.getHResult();
                }
            }
            else
            {
                res = HRESULT.E_NOINTERFACE;
                throw new SleApiException(res);
            }
        }

        if (this.pduTranslator != null)
        {
            this.pduTranslator.startTrace(trace, level, forward);
        }

        if (res == HRESULT.S_OK)
        {
            this.traceStarted = true;
            this.traceLevel = level;
            this.trace = trace;
        } 
        LOG.fine("APIPX_Association, trace started (" + this.traceStarted + ") at level " + this.traceLevel 
        		+ " role: "  + this.role + " instance ID: " + this.instanceId);
        if(this.traceStarted == false)
        {
        	throw new SleApiException(HRESULT.SLE_E_STATE);
        }
    }

    /**
     * Stops tracing for the association, and forwards the StopTrace to the
     * channel object (TML) and to the PDU translator. S_OK Tracing was stopped.
     * SLE_E_STATE Tracing already stopped. E_FAIL The request fails due to a
     * further unspecified error.
     */
    @Override
    public void stopTrace() throws SleApiException
    {

        if (!this.traceStarted)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        HRESULT res = HRESULT.S_OK;
        if (this.channelInitiate != null)
        {
            // get the trace control interface of the channel
            ISLE_TraceControl pTraceControl = this.channelInitiate.queryInterface(ISLE_TraceControl.class);
            if (pTraceControl != null)
            {
                try
                {
                    pTraceControl.stopTrace();
                }
                catch (SleApiException e)
                {
                    res = e.getHResult();
                }
            }
            else
            {
                res = HRESULT.E_NOINTERFACE;
                throw new SleApiException(res);
            }
        }

        if (this.pduTranslator != null)
        {
            this.pduTranslator.stopTrace();
        }
        LOG.fine("APIPX_Association, trace stopped (" + this.traceStarted + ")");
        if (res == HRESULT.S_OK)
        {
            this.traceStarted = false;
        }
        else
        {
        	throw new SleApiException(HRESULT.SLE_E_STATE);
        }
    }

    /**
     * See specification of IUnknown.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IEE_ChannelInform.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_SrvProxyInitiate.class)
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
     * See specification of ISLE_SrvProxyInitiate.
     */
    @Override
    public void discardBuffer() throws SleApiException
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Discard buffer request received");
        }
        this.objMutex.lock();
        if (this.state != SLE_AssocState.sleAST_bound)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        // check all the operations of the sending queue
        boolean onebufDiscarded = false;
        ListIterator<PXSPL_Operation> listIter = this.iOperation.listIterator();
        while (listIter.hasNext())
        {
            ISLE_Operation pIsleOperation = listIter.next().getpOperation();
            if (pIsleOperation != null)
            {
                if (pIsleOperation.getOperationType() == SLE_OpType.sleOT_transferBuffer)
                {
                    this.iOperation.remove(this.iOperation.indexOf(pIsleOperation));
                    onebufDiscarded = true;
                }
            }
            else
            {
                this.iOperation.remove(this.iOperation.indexOf(pIsleOperation));
            }
        }

        if (onebufDiscarded)
        {
            this.objMutex.unlock();
            throw new SleApiException(HRESULT.SLE_S_DISCARDED);
        }

        this.objMutex.unlock();
        throw new SleApiException(HRESULT.SLE_S_NOTDISCARDED);
    }

    /**
     * Returns the current state of the association.
     */
    @Override
    public SLE_AssocState getAssocState()
    {
        return getState();
    }

    /**
     * Makes a context switch to a new thread to call
     * "doStateProcessingAndResumeRecv" For PEER-ABORT / PROTOCOL-ABORT no
     * context switch is done S_OK The state-processing is complete.
     * SLE_E_PROTOCOL The operation cannot be accepted in the current state.
     * SLE_E_UNBINDING The pdu can no longer be accepted because an unbinding
     * operation has already been initialized. E_FAIL The state-processing has
     * failed.
     */
    protected void rcvSLEPDUBlocking(byte[] data)
    {
        this.innerLock.lock();
        this.objMutex.lock();
        try
        {
            dumpPdu(true, data);

            EE_Reference<ISLE_Operation> poperation = new EE_Reference<ISLE_Operation>();
            poperation.setReference(null);
            EE_Reference<Boolean> isInvoke = new EE_Reference<Boolean>();
            isInvoke.setReference(false);

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("poperation before netwPreProcessing: " + poperation.getReference());
            }
            HRESULT result = netwPreProcessing(data, poperation, isInvoke);

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("poperation after netwPreProcessing: " + poperation.getReference());
            }
            if (result == HRESULT.S_OK)
            {
                doStateProcessing(true, poperation.getReference(), isInvoke.getReference(), false);
            }
        }
        finally
        {
            this.objMutex.unlock();
            this.innerLock.unlock();
        }
    }

    /**
     * Receives an encoded PDU from the Channel. The pre-processing of the PDU
     * is done before the operation is given to the state machine.
     */
    @Override
    public void rcvSLEPDU(byte[] data)
    {
        rcvSLEPDUBlocking(data);
    }

    /**
     * Reception of a CONNECT request. Nothing to do in the association.
     */
    @Override
    public void rcvConnect()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Connect request received");
        }
        this.objMutex.lock();

        if (this.traceStarted && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode()
            && this.trace != null)
        {
            traceRecord(SLE_TraceLevel.sleTL_medium, null, 1011);
        }

        if (this.state == SLE_AssocState.sleAST_unbound && this.role == SLE_BindRole.sleBR_initiator
            && !this.unboundStateIsDisconnected)
        {
            // set the state to bind pending
            changeState(EE_APIPX_Event.PXSPL_initiateBindInvoke, SLE_AssocState.sleAST_bindPending);

            // send the bind
            this.objMutex.unlock();
            resumeXmit();
        }
        else
        {
            this.objMutex.unlock();
        }
    }

    /**
     * Reception of a BIND from the peer proxy. This a pure virtual methods
     * which is implemented in the derived class. S_OK The processing is
     * complete. SLE_E_PROTOCOL The operation cannot be accepted in the current
     * state. SLE_E_UNBINDING The pdu can no onger be accepted because an
     * unbinding operation has already been initialised. E_FAIL The processing
     * has failed.
     */
    public abstract HRESULT rcvBindInvoke(ISLE_Operation poperation);

    /**
     * Reception of a BIND Return from the peer proxy. This a pure virtual
     * methods which is implemented in the derived class. S_OK The processing is
     * complete. SLE_E_PROTOCOL The operation cannot be accepted in the current
     * state. SLE_E_UNBINDING The pdu can no onger be accepted because an
     * unbinding operation has already been initialised. E_FAIL The processing
     * has failed.
     */
    public abstract HRESULT rcvBindReturn(ISLE_Operation poperation);

    /**
     * Reception of a UNBIND Invoke from the peer proxy. This a pure virtual
     * methods which is implemented in the derived class. S_OK The processing is
     * complete. SLE_E_PROTOCOL The operation cannot be accepted in the current
     * state. SLE_E_UNBINDING The pdu can no onger be accepted because an
     * unbinding operation has already been initialised. E_FAIL The processing
     * has failed.
     */
    public abstract HRESULT rcvUnbindInvoke(ISLE_Operation poperation);

    /**
     * Reception of a UNBIND Return from the peer proxy. This a pure virtual
     * methods which is implemented in the derived class. S_OK The processing is
     * complete. SLE_E_PROTOCOL The operation cannot be accepted in the current
     * state. SLE_E_UNBINDING The pdu can no onger be accepted because an
     * unbinding operation has already been initialised. E_FAIL The processing
     * has failed.
     */
    public abstract HRESULT rcvUnbindReturn(ISLE_Operation poperation);

    /**
     * Requests the suspension of the sending.
     */
    @Override
    public void suspendXmit()
    {
        this.objMutex.lock();
        this.suspendXmit = true;
        this.objMutex.unlock();
    }

    /**
     * Requests a resumption of the sending.
     */
    @Override
    public void resumeXmit()
    {
        this.objMutex.lock();

        PXSPL_Operation theOp = null;

        // the channel is ready for transmission
        if (this.suspendXmit)
        {
            // send the next operation if one
            if (!this.iOperation.isEmpty())
            {
                // take the first operation in the list
                theOp = this.iOperation.pollFirst();
            }
        }

        if (theOp != null)
        {
            // encode and send the pdu immediately
            byte[] encodedPdu = null;
            try
            {
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("theOp.getpOperation() " + theOp.getpOperation());
                }
                encodedPdu = this.pduTranslator.encode(theOp.getpOperation(), theOp.isInvoke());
                dumpPdu(false, encodedPdu);
                this.suspendXmit = true;

                this.objMutex.unlock();
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("encodedPdu.length " + encodedPdu.length);
                }
                this.channelInitiate.sendSLEPDU(encodedPdu, theOp.isLastPdu());
                
                if (theOp.isReportTransmission())
                {
                    try
                    {
                        this.srvProxyInform.pduTransmitted(theOp.getpOperation());
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                    }
                    finally
                    {
                        this.objMutex.lock();
                    }
                }
                else
                {
                    this.objMutex.lock();
                }
            }
            catch (SleApiException | IOException e)
            {
                this.suspendXmit = false;
                String mess = EE_MessageRepository.getMessage(1004,
                                                              SLE_PeerAbortDiagnostic.slePAD_encodingError.toString(),
                                                              e.getMessage());
                notify(SLE_LogMessageType.sleLM_alarm, SLE_Alarm.sleAL_localAbort, 1004, mess, null);
            }
        }
        else
        {
            this.suspendXmit = false;
        }

        this.objMutex.unlock();
    }

    /**
     * Returns the role of the association.
     */
    public SLE_BindRole getRole()
    {
        return this.role;
    }

    /**
     * Returns the is released attribute.
     */
    public boolean getIsReleased()
    {
        return this.isReleased;
    }

    /**
     * Returns the IEE_ChannelInitiate pointer.
     */
    public IEE_ChannelInitiate getChannelInitiate()
    {
        return this.channelInitiate;
    }

    public void setChannelInitiate(IEE_ChannelInitiate value)
    {
        this.channelInitiate = value;
    }

    /**
     * Sets the SrvProxyInform interface.
     */
    protected void setSrvProxyInform(ISLE_SrvProxyInform srvproxyinform)
    {
        if (this.srvProxyInform != null)
        {
            this.srvProxyInform = null;
        }

        this.srvProxyInform = srvproxyinform;
    }

    /**
     * Removes the association from the list of association in the Proxy, and
     * releases the reference to the association.
     */
    protected void releaseAssociation()
    {
        this.objMutex.unlock();
        this.proxy.deregisterAssoc();
        this.objMutex.lock();
        releaseChannel();

        this.isReleased = true;
        this.state = SLE_AssocState.sleAST_unbound;
        this.unboundStateIsDisconnected = true;
    }

    /**
     * Removes and discard all operation invocations that are queued for
     * transmission.
     */
    protected void discardAllInvocationPdu()
    {
        if (this.traceStarted && this.trace != null && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_full.getCode())
        {
            traceRecord(SLE_TraceLevel.sleTL_full,
                        null,
                        1004,
                        "Discard all invocation PDU",
                        "operation queue now empty");
        }

        // release all the operation of the sending queue
        this.iOperation.clear();
    }

    /**
     * Releases all the confirmed operations inserted in the list of pending
     * return, and removes the list.
     */
    protected void clearAllPendingReturn()
    {
        if (this.pduTranslator != null)
        {
            this.pduTranslator.removeAllPendingReturns();
        }
    }

    /**
     * Receives a PEER_ABORT request from the Channel. If the originator is not
     * local : - decodes the PEER-ABORT to get a peer-abort operation. -
     * forwards the PEER-ABORT operation to the local client. - discards all the
     * PDU's of the transmission queue. - clears the list of pending returns
     * operation. - sets the state to unbound.
     */
    @Override
    public void rcvPeerAbort(int diagnostic, boolean originatorIsLocal)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("PEER-ABORT received, diagnostic " + diagnostic + ", originator local " + originatorIsLocal);
        }
        if (this.state == SLE_AssocState.sleAST_unbound)
        {
            if (this.unboundStateIsDisconnected)
            {
                // protocol error
                return;
            }
            else
            {
                // reset the connection
                if (this.channelInitiate != null)
                {
                    this.objMutex.unlock();
                    this.channelInitiate.sendReset();
                    this.objMutex.lock();
                }

                this.unboundStateIsDisconnected = true;
            }
        }

        ISLE_Operation poperation = netwPreProcessing(diagnostic, originatorIsLocal);
        if (poperation != null)
        {
            doStateProcessing(true, poperation, false, false);
        }
    }

    /**
     * Receives a PROTOCOL_ABORT request : - sends a PROTOCOL-ABORT to the local
     * client. - discards all the PDU's of the transmission queue. - clears the
     * list of pending returns operation. - sets the state to unbound.
     */
    @Override
    public void rcvProtocolAbort(EE_APIPX_ISP1ProtocolAbortDiagnostics diagnostic)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Protocol abort received, diagnostic " + diagnostic);
        }
        if (this.state != SLE_AssocState.sleAST_unbound
            || (this.state == SLE_AssocState.sleAST_unbound && !this.unboundStateIsDisconnected))
        {
            // sends a protocol abort to the client
            if (this.srvProxyInform != null)
            {
                this.objMutex.unlock();
                try
                {
                    this.srvProxyInform.protocolAbort(diagnostic.getDiagAsByteArray());
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                }
                finally
                {
                    this.objMutex.lock();
                }
            }
        }

        changeState(EE_APIPX_Event.PXSPL_rcvProtocolAbort, SLE_AssocState.sleAST_unbound);
        this.unboundStateIsDisconnected = true;
        initAssoc();

        this.objMutex.unlock();
        this.oPSequencer.reset(HRESULT.SLE_E_ABORTED);
        this.objMutex.lock();

        // cleanup
        discardAllInvocationPdu();
        clearAllPendingReturn();
        releaseChannel();
    }

    /**
     * Receives a PDU from the peer-proxy with an invocation that is valid for
     * the service type. If the state is "bound", or "loc unbind pend", or
     * "rem unbind pend": - increments the sequence counter. - sends the
     * operation to the local client. S_OK The processing is complete.
     * SLE_E_PROTOCOL The operation cannot be accepted in the current state.
     * SLE_E_UNBINDING The pdu can no onger be accepted because an unbinding
     * operation has already been initialised. E_FAIL The processing has failed.
     */
    protected HRESULT rcvSrvPduInvoke(ISLE_Operation poperation)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Server PDU invoke called, operation " + poperation.getOperationType());
        }
        if (this.state == SLE_AssocState.sleAST_unbound)
        {
            return HRESULT.SLE_E_PROTOCOL;
        }
        else if (this.state == SLE_AssocState.sleAST_bound || this.state == SLE_AssocState.sleAST_localUnbindPending)
        {
            if (this.srvProxyInform != null)
            {
                long seqc = this.sequenceCounter++;
                this.objMutex.unlock();
                this.innerLock.unlock(); // SLEAPIJ-TBD
                try
                {
                    this.srvProxyInform.informOpInvoke(poperation, seqc);
                }
                catch (SleApiException e)
                {
                    return e.getHResult();
                }
                finally
                {
                	this.innerLock.lock(); // SLEAPIJ-TBD
                    this.objMutex.lock();
                }
            }
        }
        else if (this.state == SLE_AssocState.sleAST_bindPending
                 || this.state == SLE_AssocState.sleAST_remoteUnbindPending)
        {
            doAbort(SLE_PeerAbortDiagnostic.slePAD_protocolError, SLE_AbortOriginator.sleAO_proxy, true);
            return HRESULT.SLE_E_PROTOCOL;
        }

        return HRESULT.S_OK;
    }

    /**
     * Receives a return PDU from the peer-proxy with an invocation that is
     * valid for the service type. If the state is "bound", or
     * "loc unbind pend", or "rem unbind pend": - checks if a pending return
     * exists for this operation. - increments the sequence counter. - sends the
     * operation to the local client. S_OK The processing is complete.
     * SLE_E_PROTOCOL The operation cannot be accepted in the current state.
     * SLE_E_UNBINDING The pdu can no onger be accepted because an unbinding
     * operation has already been initialised. E_FAIL The processing has failed.
     */
    protected HRESULT rcvSrvPduReturn(ISLE_Operation poperation)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Server PDU return called, operation " + poperation.getOperationType());
        }
        if (this.state == SLE_AssocState.sleAST_unbound)
        {
            return HRESULT.SLE_E_PROTOCOL;
        }
        else if (this.state == SLE_AssocState.sleAST_bound || this.state == SLE_AssocState.sleAST_localUnbindPending)
        {
            if (this.srvProxyInform != null)
            {
                ISLE_ConfirmedOperation pConfOp = poperation.queryInterface(ISLE_ConfirmedOperation.class);
                if (pConfOp != null)
                {
                    long seqc = this.sequenceCounter++;
                    if (LOG.isLoggable(Level.FINE))
                    {
                        LOG.fine("seqCount : " + seqc + "   " + poperation.getOperationType());
                    }
                    this.objMutex.unlock();
                    this.innerLock.unlock(); // SLEAPIJ-TBD
                    try
                    {
                        this.srvProxyInform.informOpReturn(pConfOp, seqc);
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                        return e.getHResult();
                    }
                    finally
                    {
                    	this.innerLock.lock(); // SLEAPIJ-TBD
                        this.objMutex.lock();
                    }
                }
            }
        }
        else if (this.state == SLE_AssocState.sleAST_bindPending
                 || this.state == SLE_AssocState.sleAST_remoteUnbindPending)
        {
            doAbort(SLE_PeerAbortDiagnostic.slePAD_protocolError, SLE_AbortOriginator.sleAO_proxy, true);
            return HRESULT.SLE_E_PROTOCOL;
        }

        return HRESULT.S_OK;
    }

    /**
     * Abort the association : - releases all the operations of the sending
     * queue. - sends a PEER-ABORT pdu to the peer-proxy. - sends a PEER-ABORT
     * to the local client. - clears the list of pending returns operation. -
     * sets the state to unbound. S_OK The processing is complete. E_FAIL The
     * processing has failed.
     */
    protected HRESULT doAbort(ISLE_PeerAbort pPeerAbort, boolean sendToPeer)
    {
        HRESULT res = HRESULT.S_OK;

        if (this.isAborted)
        {
            return HRESULT.S_OK;
        }

        this.isAborted = true;

        SLE_AbortOriginator ao = pPeerAbort.getAbortOriginator();

        if (this.channelInitiate != null && ao != SLE_AbortOriginator.sleAO_peer && sendToPeer)
        {
            // send the peer abort to the remote proxy
            if (this.pduTranslator != null)
            {
                int diag = -1;
                try
                {
                    diag = this.pduTranslator.encode(pPeerAbort);
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                    res = e.getHResult();
                    return res;
                }

                this.objMutex.unlock();
                this.channelInitiate.sendPeerAbort(diag);
                this.objMutex.lock();
            }
        }

        if (ao == SLE_AbortOriginator.sleAO_peer)
        {
            changeState(EE_APIPX_Event.PXSPL_rcvPeerAbort, SLE_AssocState.sleAST_unbound);
        }
        else
        {
            changeState(EE_APIPX_Event.PXSPL_initiatePeerAbort, SLE_AssocState.sleAST_unbound);
        }

        this.unboundStateIsDisconnected = true;

        // send a peer abort operation to the client
        if (this.srvProxyInform != null
            && (ao == SLE_AbortOriginator.sleAO_peer || ao == SLE_AbortOriginator.sleAO_proxy))
        {
            // send the peer abort operation
            long seqc = this.sequenceCounter++;

            //
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("th" + Thread.currentThread().getId() + " is calling initAssoc()");
            }
            initAssoc();

            // cleanup
            discardAllInvocationPdu();
            clearAllPendingReturn();
            //

            this.objMutex.unlock();

            try
            {
                this.srvProxyInform.informOpInvoke(pPeerAbort, seqc);
            }
            catch (SleApiException e)
            {

                LOG.log(Level.FINE, "SleApiException ", e);
                res = e.getHResult();
                return res;
            }
            finally
            {

                this.objMutex.lock();

            }

            res = HRESULT.S_OK;
        }

        // if (LOG.isLoggable(Level.FINEST))
        // {
        // LOG.finest(">>>>>>>>>>>>>>>>>>>>>>>>>>>> TH" +
        // Thread.currentThread().getId() + " is calling initAssoc()");
        // }
        // initAssoc();
        //
        // this.objMutex.unlock();
        this.oPSequencer.reset(HRESULT.SLE_E_ABORTED);
        // this.objMutex.lock();
        //
        // // cleanup
        // discardAllInvocationPdu();
        // clearAllPendingReturn();

        return res;
    }

    /**
     * Abort the association : - releases all the operations of the sending
     * queue. - sends a PEER-ABORT pdu to the peer-proxy. - sends a PEER-ABORT
     * to the local client. - clears the list of pending returns operation. -
     * sets the state to unbound. S_OK The processing is complete. E_FAIL The
     * processing has failed.
     */
    protected HRESULT doAbort(SLE_PeerAbortDiagnostic diagnostic,
                              SLE_AbortOriginator abortoriginator,
                              boolean sendToPeer)
    {
        ISLE_PeerAbort pPeerAbort = null;

        if (this.isAborted)
        {
            return HRESULT.S_OK;
        }

        try
        {
            pPeerAbort = this.opFactory.createOperation(ISLE_PeerAbort.class,
                                                        SLE_OpType.sleOT_peerAbort,
                                                        this.serviceType,
                                                        this.version);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return HRESULT.E_FAIL;
        }

        // fill the operation
        pPeerAbort.setPeerAbortDiagnostic(diagnostic);
        pPeerAbort.setAbortOriginator(abortoriginator);

        doAbort(pPeerAbort, sendToPeer);

        return HRESULT.S_OK;
    }

    /**
     * Checks if the type of the service and of the operation contained in the
     * PDU is compatible with the service type.
     */
    protected boolean checkPDUType(SLE_ApplicationIdentifier applicationType, SLE_OpType pduType)
    {

        if ((this.serviceType == SLE_ApplicationIdentifier.sleAI_invalid) && (pduType == SLE_OpType.sleOT_bind))
        {
            return true;
        }

        if (applicationType != this.serviceType)
        {
            return false;
        }

        // PDU valid for all services
        if ((pduType == SLE_OpType.sleOT_bind) || (pduType == SLE_OpType.sleOT_unbind)
            || (pduType == SLE_OpType.sleOT_peerAbort) || (pduType == SLE_OpType.sleOT_stop)
            || (pduType == SLE_OpType.sleOT_scheduleStatusReport) || (pduType == SLE_OpType.sleOT_transferBuffer))
        {
            return true;
        }

        switch (this.serviceType)
        {
        case sleAI_rtnAllFrames:
        case sleAI_rtnChFrames:
        case sleAI_rtnChOcf:
        {
            if ((pduType == SLE_OpType.sleOT_getParameter) || (pduType == SLE_OpType.sleOT_start)
                || (pduType == SLE_OpType.sleOT_statusReport) || (pduType == SLE_OpType.sleOT_transferData)
                || (pduType == SLE_OpType.sleOT_syncNotify))
            {
                return true;
            }
            break;
        }
        case sleAI_fwdCltu:
        {
            if ((pduType == SLE_OpType.sleOT_getParameter) || (pduType == SLE_OpType.sleOT_start)
                || (pduType == SLE_OpType.sleOT_statusReport) || (pduType == SLE_OpType.sleOT_transferData)
                || (pduType == SLE_OpType.sleOT_throwEvent) || (pduType == SLE_OpType.sleOT_asyncNotify))
            {
                return true;
            }
            break;
        }
        case sleAI_fwdTcSpacePkt:
        {
            if ((pduType == SLE_OpType.sleOT_getParameter) || (pduType == SLE_OpType.sleOT_start)
                || (pduType == SLE_OpType.sleOT_statusReport) || (pduType == SLE_OpType.sleOT_transferData)
                || (pduType == SLE_OpType.sleOT_throwEvent) || (pduType == SLE_OpType.sleOT_invokeDirective)
                || (pduType == SLE_OpType.sleOT_asyncNotify))
            {
                return true;
            }
            break;
        }
        default:
            break;
        }
        return false;
    }

    /**
     * If authentication is required, authenticates the identity of the
     * peer-proxy thanks to : - the EE_SLE_SecAttributes set in the association.
     * - the peer credentials.
     */
    protected boolean authenticate(ISLE_Operation poperation, boolean isInvoke)
    {
        boolean res = false;

        if (this.authenticationMode == SLE_AuthenticationMode.sleAM_none)
        {
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("Authentication NONE");
            }
            return true;
        }

        if (this.iSecAttr == null || this.database == null)
        {
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("Authentication fails, secAttr or db null");
            }
            return false;
        }

        // get the acceptable delay from the database
        int delay = 0;
        EE_APIPX_ProxySettings pProxySettings = this.database.getProxySettings();
        delay = pProxySettings.getAuthentAccDelay();

        if (this.authenticationMode == SLE_AuthenticationMode.sleAM_bindOnly)
        {
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("Authenticate BIND ONLY");
            }

            if (poperation.getOperationType() == SLE_OpType.sleOT_bind)
            {
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.fine("Authenticate BIND operation");
                }

                res = authenticateOperation(poperation, isInvoke, delay);
            }
            else
            {
                res = true;
            }
        }
        else
        {
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("Authenticate NOT BIND ONLY");
            }

            res = authenticateOperation(poperation, isInvoke, delay);
        }

        return res;
    }

    /**
     * If authentication is required, inserts the security attributes of the
     * association in the operation.
     */
    protected void insertSecurityAttributes(ISLE_Operation poperation, boolean isInvoke)
    {
        ISLE_SecAttributes pSecAttr = this.proxy.getSecurityAttribures();

        if (isInvoke)
        {
            if (pSecAttr == null)
            {
                insertCredentialsInOp(poperation, null);
                return;
            }

            if (this.authenticationMode == SLE_AuthenticationMode.sleAM_none
                || (this.authenticationMode == SLE_AuthenticationMode.sleAM_bindOnly && poperation.getOperationType() != SLE_OpType.sleOT_bind))
            {
                insertCredentialsInOp(poperation, null);
                return;
            }

            ISLE_Credentials pCredentials = pSecAttr.generateCredentials(poperation.getOpVersionNumber());
            insertCredentialsInOp(poperation, pCredentials);
        }
        else
        {
            ISLE_ConfirmedOperation pConfOp = poperation.queryInterface(ISLE_ConfirmedOperation.class);
            if (pConfOp != null)
            {
                if (pSecAttr == null)
                {
                    insertCredentialsInConfOp(pConfOp, null);
                    return;
                }

                if (this.authenticationMode == SLE_AuthenticationMode.sleAM_none
                    || (this.authenticationMode == SLE_AuthenticationMode.sleAM_bindOnly && poperation
                            .getOperationType() != SLE_OpType.sleOT_bind))
                {
                    insertCredentialsInConfOp(pConfOp, null);
                    return;
                }

                ISLE_Credentials pcredentials = pSecAttr.generateCredentials(pConfOp.getOpVersionNumber());
                insertCredentialsInConfOp(pConfOp, pcredentials);
            }
        }
    }

    /**
     * Checks if the trace level given as parameter is compatible with the
     * attribute traceLevel set by the StartTrace() method.
     */
    protected boolean checkTraceLevel(SLE_TraceLevel traceLevel)
    {
        boolean res;
        if (!this.traceStarted)
        {
            res = false;
        }
        else
        {
            if (this.traceLevel.getCode() >= traceLevel.getCode())
            {
                res = true;
            }
            else
            {
                res = false;
            }
        }

        return res;
    }

    /**
     * Receives a PEER_ABORT request from the local client, or the proxy invokes
     * the PEER-ABORT itself : - discards all the PDU's of the transmission
     * queue. - encodes and sends the PEER-ABORT to the peer proxy. - terminates
     * the data communication association. - sets the state to unbound. - if the
     * PEER-ABORT is initiated by the proxy itself, forwards the PEER-ABORT
     * operation to the local client. See specification of
     * ISLE_SrvProxyInitiate.
     */
    protected HRESULT initiatePeerAbort(ISLE_PeerAbort pPeerAbort, boolean report)
    {
        if (this.state == SLE_AssocState.sleAST_unbound)
        {
            if (this.unboundStateIsDisconnected)
            {
                return HRESULT.SLE_E_PROTOCOL;
            }
            else
            {
                // send a reset to TML
                this.objMutex.unlock();
                this.channelInitiate.sendReset();
                this.objMutex.lock();

                this.unboundStateIsDisconnected = true;

                discardAllInvocationPdu();
                clearAllPendingReturn();
                return HRESULT.S_OK;
            }
        }

        if (report && this.srvProxyInform != null)
        {
            this.objMutex.unlock();
            try
            {
                this.srvProxyInform.pduTransmitted(pPeerAbort);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
            }
            finally
            {
                this.objMutex.lock();
            }
        }

        HRESULT res = doAbort(pPeerAbort, true);
        return res;
    }

    /**
     * Receives an operation from the local client with an invocation that is
     * valid for the service type. If the state is bound and if the sending
     * queue is empty : - encodes the operation and if it is a confirmed
     * operation, inserts the pending return in the list. - sends the PDU to the
     * peer-proxy. Otherwise, inserts the operation in the sending queue. See
     * specification of ISLE_SrvProxyInitiate.
     */
    protected HRESULT initiateSrvOperationInvoke(ISLE_Operation poperation, boolean report)
    {
        if (this.state == SLE_AssocState.sleAST_unbound || this.state == SLE_AssocState.sleAST_bindPending
            || this.state == SLE_AssocState.sleAST_localUnbindPending)
        {
            return HRESULT.SLE_E_PROTOCOL;
        }
        else if (this.state == SLE_AssocState.sleAST_remoteUnbindPending)
        {
            return HRESULT.SLE_E_UNBINDING;
        }

        // the operation is encoded and sent in the post-processing
        HRESULT res = clientPostProcessing(poperation, report, true, false);
        return res;
    }

    /**
     * Receives a return operation from the local client. If the sending queue
     * is empty : - encodes the operation. - sends the PDU to the peer-proxy.
     * Otherwise, inserts the operation in the sending queue. See specification
     * of ISLE_SrvProxyInitiate.
     */
    protected HRESULT initiateSrvOperationReturn(ISLE_ConfirmedOperation poperation, boolean report)
    {
        if (this.state == SLE_AssocState.sleAST_unbound || this.state == SLE_AssocState.sleAST_bindPending
            || this.state == SLE_AssocState.sleAST_localUnbindPending)
        {
            return HRESULT.SLE_E_PROTOCOL;
        }

        // the operation is encoded and sent in the post-processing
        HRESULT res = clientPostProcessing(poperation, report, false, false);
        return res;
    }

    /**
     * Receives an invoke operation from the service element. The operation is
     * given to the sequencer, and when the sequencer returns, the operation is
     * given to the client-pre-processing and then to the state machine. See
     * specification of ISLE_SrvProxyInitiate.
     * 
     * @throws SleApiException
     */
    @Override
    public void initiateOpInvoke(ISLE_Operation poperation, boolean reportTransmission, long seqCount) throws SleApiException
    {
        HRESULT res = HRESULT.S_OK;
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Before locking obj mutex");
        }
        this.innerLock.lock();
        this.objMutex.lock();
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("After locking obj mutex");
        }

        res = clientPreProcessing(poperation, seqCount);

        if (res == HRESULT.S_OK)
        {
            res = doStateProcessing(false, poperation, true, reportTransmission);
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("poperation on to be invoked " + poperation + "  res = " + res);
            }

            if (res == HRESULT.SLE_S_TRANSMITTED && poperation.getOperationType() != SLE_OpType.sleOT_transferBuffer) // SLEAPIJ-26 do not resume for TD - backpressure!
            {
                resumeXmit();
            }

            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("Before unlocking obj mutex");
            }
            this.objMutex.unlock();
            this.innerLock.unlock();
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("After unlocking obj mutex");
            }

            // sequencer continues delivering the next operation
            this.oPSequencer.cont();
        }
        else
        {
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("Before unlocking obj mutex");
            }
            this.objMutex.unlock();
            this.innerLock.unlock();
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("After unlocking obj mutex");
            }
        }
        
        if(res == HRESULT.SLE_S_SUSPEND || res == HRESULT.SLE_S_QUEUED) // SLEAPIJ-26 propagate SUSPEND or QUEUED
        {
        	throw new SleApiException(res);
        }
    }

    /**
     * Receives a BIND request from the local client. This a pure virtual
     * methods which is implemented in the derived class. See specification of
     * ISLE_SrvProxyInitiate.
     */
    protected abstract HRESULT initiateBindInvoke(ISLE_Operation poperation, boolean reportTransmission);

    /**
     * Receives a UNBIND request from the local client.This a pure virtual
     * methods which is implemented in the derived class. See specification of
     * ISLE_SrvProxyInitiate.
     */
    protected abstract HRESULT initiateUnbindInvoke(ISLE_Operation poperation, boolean report);

    /**
     * Receives a return operation from the service element. The operation is
     * given to the sequencer, and when the sequencer returns, the operation is
     * given to the client-pre-processing and then to the state machine. See
     * specification of ISLE_SrvProxyInitiate.
     */
    @Override
    public void initiateOpReturn(ISLE_ConfirmedOperation poperation, boolean report, long seqCount) throws SleApiException
    {
        HRESULT res;
        this.innerLock.lock();
        this.objMutex.lock();

        if ((res = clientPreProcessing(poperation, seqCount)) == HRESULT.S_OK)
        {

            res = doStateProcessing(false, poperation, false, report);
            if (res == HRESULT.SLE_S_TRANSMITTED)
            {
                resumeXmit();
            }
            this.objMutex.unlock();
            this.innerLock.unlock();
            this.oPSequencer.cont(); // sequencer continues delivering the next
                                     // operation
        }
        else
        {
            this.objMutex.unlock();
            this.innerLock.unlock();
        }

        if (res != HRESULT.S_OK)
        {
            throw new SleApiException(res);
        }
    }

    /**
     * Receives a BIND Return from the local client. This a pure virtual methods
     * which is implemented in the derived class. See specification of
     * ISLE_SrvProxyInitiate.
     */
    protected abstract HRESULT initiateBindReturn(ISLE_ConfirmedOperation poperation, boolean report);

    /**
     * Receives an UNBIND Return from the local client. This a pure virtual
     * methods which is implemented in the derived class. See specification of
     * ISLE_SrvProxyInitiate.
     */
    protected abstract HRESULT initiateUnbindReturn(ISLE_ConfirmedOperation poperation, boolean report);

    /**
     * Performs state processing as specified in the state-table. The
     * member-function performs a state change if necessary, and initiates all
     * necessary actions e.g. transmitting the operation to the post-processing,
     * aborting an association, encoding and transmitting an operation to the
     * network, etc. Note that this member-function is only called after a
     * successful pre-processing. S_OK The state-processing is complete.
     * SLE_E_PROTOCOL The operation cannot be accepted in the current state.
     * SLE_E_UNBINDING The pdu can no onger be accepted because an unbinding
     * operation has already been initialised. E_FAIL The state-processing has
     * failed.
     */
    private HRESULT doStateProcessing(boolean originatorIsNetwork,
                                      ISLE_Operation pOperation,
                                      boolean isInvoke,
                                      boolean reportTransmission)
    {
        HRESULT res = HRESULT.E_FAIL;

        switch (pOperation.getOperationType())
        {
        case sleOT_bind:
        {
            if (originatorIsNetwork)
            {
                if (isInvoke)
                {
                    res = rcvBindInvoke(pOperation);
                }
                else
                {
                    res = rcvBindReturn(pOperation);
                }
            }
            else
            {
                if (isInvoke)
                {
                    res = initiateBindInvoke(pOperation, reportTransmission);
                }
                else
                {
                    ISLE_ConfirmedOperation pConfOp = pOperation.queryInterface(ISLE_ConfirmedOperation.class);

                    if (pConfOp != null)
                    {
                        res = initiateBindReturn(pConfOp, reportTransmission);
                    }
                }
            }
            break;
        }
        case sleOT_unbind:
        {
            if (originatorIsNetwork)
            {
                if (isInvoke)
                {
                    res = rcvUnbindInvoke(pOperation);
                }
                else
                {
                    res = rcvUnbindReturn(pOperation);
                }
            }
            else
            {
                if (isInvoke)
                {
                    res = initiateUnbindInvoke(pOperation, reportTransmission);
                }
                else
                {
                    ISLE_ConfirmedOperation pConfOp = pOperation.queryInterface(ISLE_ConfirmedOperation.class);
                    if (pConfOp != null)
                    {
                        res = initiateUnbindReturn(pConfOp, reportTransmission);
                    }
                }
            }
            break;
        }
        case sleOT_peerAbort:
        {
            ISLE_PeerAbort pPeerAbort = pOperation.queryInterface(ISLE_PeerAbort.class);
            if (originatorIsNetwork)
            {
                if (pPeerAbort != null)
                {
                    res = doAbort(pPeerAbort, false);
                }
            }
            else
            {
                if (pPeerAbort != null)
                {
                    res = initiatePeerAbort(pPeerAbort, reportTransmission);
                }
            }
            break;
        }
        default:
        {
            if (originatorIsNetwork)
            {
                if (isInvoke)
                {
                    res = rcvSrvPduInvoke(pOperation);
                }
                else
                {
                    res = rcvSrvPduReturn(pOperation);
                }
            }
            else
            {
                if (isInvoke)
                {
                    res = initiateSrvOperationInvoke(pOperation, reportTransmission);
                }
                else
                {
                    ISLE_ConfirmedOperation pConfOp = pOperation.queryInterface(ISLE_ConfirmedOperation.class);
                    if (pConfOp != null)
                    {
                        res = initiateSrvOperationReturn(pConfOp, reportTransmission);
                    }
                }
            }
            break;
        }
        }

        return res;
    }

    /**
     * Performs pre-processing of events received from the network interface, as
     * specified in the state-table. S_OK The pre-processing is complete. E_FAIL
     * The pre-processing has failed.
     */
    protected HRESULT netwPreProcessing(byte[] data,
                                        EE_Reference<ISLE_Operation> poperation,
                                        EE_Reference<Boolean> isInvoke)
    {
        HRESULT res = HRESULT.S_OK;

        if (this.pduTranslator != null)
        {
            try
            {
                ISLE_Operation op = this.pduTranslator.decode(data, this.serviceType, isInvoke);
                poperation.setReference(op);

                // no decode exception
                // for bind invoke pdu, the authenticate is done after
                // location
                if (poperation.getReference().getOperationType() != SLE_OpType.sleOT_bind
                    || !isInvoke.getReference())
                {
                    // authenticate the PDU
                    if (!authenticate(poperation.getReference(), isInvoke.getReference()))
                    {
                        // generate an authentication alarm
                        String tmp = poperation.getReference().print(512);
                        String mess = EE_MessageRepository.getMessage(1002,
                                                                      SLE_Alarm.sleAL_authFailure.toString(),
                                                                      tmp,
                                                                      null);
                        notify(SLE_LogMessageType.sleLM_alarm, SLE_Alarm.sleAL_authFailure, 1002, mess, null);

                        SLE_OpType optype = poperation.getReference().getOperationType();
                        switch (optype)
                        {
	                        case sleOT_bind:
	                        case sleOT_unbind:
	                        {
	                            if (optype == SLE_OpType.sleOT_unbind && !isInvoke.getReference())
	                            {
	                                // unbind return must be ignored
	                                break;
	                            }
	
	                            if (this.role == SLE_BindRole.sleBR_responder)
	                            {
	                                if (optype == SLE_OpType.sleOT_bind)
	                                {
	                                    // responder side, authenticate fails
	                                    // for bind
	                                    // invoke
	                                    // reset the connection
	                                    this.objMutex.unlock();
	                                    this.channelInitiate.sendReset();
	                                    this.objMutex.lock();
	                                }
	                                else
	                                {
	                                    // responder side, authenticate fails
	                                    // for a
	                                    // unbind invoke.
	                                    // reset the connection and send peer
	                                    // abort to
	                                    // service element
	                                    this.objMutex.unlock();
	                                    this.channelInitiate.sendReset();
	                                    this.objMutex.lock();
	                                    doAbort(SLE_PeerAbortDiagnostic.slePAD_otherReason,
	                                            SLE_AbortOriginator.sleAO_proxy,
	                                            false);
	                                }
	                            }
	                            else if (this.role == SLE_BindRole.sleBR_initiator && optype == SLE_OpType.sleOT_bind)
	                            {
	                                // initiator side. authenticate fails for
	                                // bind
	                                // return
	                                // reset the connection
	                                this.objMutex.unlock();
	                                this.channelInitiate.sendReset();
	                                this.objMutex.lock();
	                                // abort the connection, but do not send
	                                // peer abort
	                                // to peer
	                                doAbort(SLE_PeerAbortDiagnostic.slePAD_otherReason,
	                                        SLE_AbortOriginator.sleAO_proxy,
	                                        false);
	                            }
	                            else
	                            {
	                                // abort the connection
	                                doAbort(SLE_PeerAbortDiagnostic.slePAD_otherReason,
	                                        SLE_AbortOriginator.sleAO_proxy,
	                                        true);
	                            }
	
	                            // cleanup
	                            discardAllInvocationPdu();
	                            clearAllPendingReturn();
	
	                            if (this.role == SLE_BindRole.sleBR_responder)
	                            {
	                                // delete the association
	                                releaseAssociation();
	                            }
	                            break;
	                        }
	                        default:
	                        {
	                            // the PDU must be ignored
	                            break;
	                        }
                        }
                    }
                }                
            }
            catch (SleApiException e)
            {
                if (e.getHResult() == HRESULT.SLE_E_INVALIDPDU)
                {
                    // the pdu is not the expected one
                    String mess = EE_MessageRepository.getMessage(1004, SLE_PeerAbortDiagnostic.slePAD_encodingError
                            .toString(), e.getMessage());
                    notify(SLE_LogMessageType.sleLM_alarm, SLE_Alarm.sleAL_localAbort, 1004, mess, null);

                    if (this.role == SLE_BindRole.sleBR_responder && this.state == SLE_AssocState.sleAST_unbound)
                    {
                        // the first received pdu is not a bind
                        // reset the connection
                        this.objMutex.unlock();
                        this.channelInitiate.sendReset();
                        this.objMutex.lock();

                        // cleanup
                        discardAllInvocationPdu();
                        clearAllPendingReturn();

                        // delete the association
                        releaseAssociation();
                    }
                    else
                    {
                        doAbort(SLE_PeerAbortDiagnostic.slePAD_encodingError, SLE_AbortOriginator.sleAO_proxy, true);
                    }
                }
                else if (e.getHResult() == HRESULT.E_FAIL)
                {
                    String mess = EE_MessageRepository.getMessage(1004, SLE_PeerAbortDiagnostic.slePAD_encodingError
                            .toString(), e.getMessage(), null);
                    notify(SLE_LogMessageType.sleLM_alarm, SLE_Alarm.sleAL_localAbort, 1004, mess, null);

                    doAbort(SLE_PeerAbortDiagnostic.slePAD_encodingError, SLE_AbortOriginator.sleAO_proxy, true);
                }
                else if (e.getHResult() == HRESULT.E_PENDING)
                {
                    doAbort(SLE_PeerAbortDiagnostic.slePAD_unsolicitedInvokeId, SLE_AbortOriginator.sleAO_proxy, true);
                }
                res = e.getHResult();
            }

            // check the PDU type
            if (res == HRESULT.S_OK
                && !checkPDUType(poperation.getReference().getOpServiceType(), poperation.getReference()
                        .getOperationType()))
            {
                // abort the association
                doAbort(SLE_PeerAbortDiagnostic.slePAD_encodingError, SLE_AbortOriginator.sleAO_proxy, true);
            }
        }

        return res;
    }

    /**
     * Performs pre-processing of PEER-ABORT event received from the network
     * interface, as specified in the state-table. S_OK The pre-processing is
     * complete. E_FAIL The pre-processing has failed.
     */
    protected ISLE_Operation netwPreProcessing(int peerabortDiag, boolean peerabortOriginatorIsLocal)
    {
        SLE_AbortOriginator abortOriginator;

        if (peerabortOriginatorIsLocal)
        {
            abortOriginator = SLE_AbortOriginator.sleAO_proxy;
        }
        else
        {
            abortOriginator = SLE_AbortOriginator.sleAO_peer;
        }

        ISLE_Operation poperation = null;

        try
        {
            poperation = this.pduTranslator.decode(peerabortDiag, abortOriginator, this.serviceType);
        }
        catch (SleApiException e)
        {
            doAbort(SLE_PeerAbortDiagnostic.slePAD_encodingError, SLE_AbortOriginator.sleAO_proxy, true);
            String mess = EE_MessageRepository.getMessage(1004,
                                                          SLE_PeerAbortDiagnostic.slePAD_encodingError.toString(),
                                                          e.getMessage());
            notify(SLE_LogMessageType.sleLM_alarm, SLE_Alarm.sleAL_localAbort, 1004, mess, null);
        }

        return poperation;
    }

    /**
     * Performs pre-processing of events received from the client interface, as
     * specified in the state-table. S_OK The pre-processing is complete.
     * SLE_E_SEQUENCE Sequence count out of acceptable window. SLE_E_INVALIDPDU
     * The operation is not supported for the service type. SLE_E_ABORTED The
     * association has been aborted. SLE_E_OVERFLOW The queuing capability has
     * been exceeded. E_FAIL The pre-processing has failed.
     */
    protected HRESULT clientPreProcessing(ISLE_Operation pOperation, long seqCount)
    {
        if (this.isAborted && this.role == SLE_BindRole.sleBR_responder)
        {
            return HRESULT.SLE_E_ABORTED;
        }

        this.objMutex.unlock();
        this.innerLock.unlock();

        HRESULT res = this.oPSequencer.serialise(pOperation, seqCount);

        this.innerLock.lock();
        this.objMutex.lock();

        if (res == HRESULT.SLE_E_SEQUENCE)
        {
            // the sequence counter is out of window size
            return HRESULT.SLE_E_SEQUENCE;
        }
        else if (res == HRESULT.SLE_E_ABORTED)
        {
            // association is aborted
            return HRESULT.SLE_E_ABORTED;
        }

        // the operation is the expected one
        // check the pdu type
        if (!checkPDUType(pOperation.getOpServiceType(), pOperation.getOperationType()))
        {
            return HRESULT.SLE_E_INVALIDPDU;
        }

        // check if the sending queue is full
        if (this.iOperation.size() >= this.queueSize && pOperation.getOperationType() != SLE_OpType.sleOT_peerAbort)
        {
            if (this.traceStarted && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_high.getCode()
                && this.trace != null)
            {
                // trace
                String messop = pOperation.print(512);
                traceRecord(SLE_TraceLevel.sleTL_high, null, 1009, messop);
            }

            return HRESULT.SLE_E_OVERFLOW;
        }

        return HRESULT.S_OK;
    }

    /**
     * Performs post-processing of events received from the client interface, as
     * specified in the state-table. This method is called only if the
     * pre-processing and the state-processing have return ok. SLE_S_TRANSMITTED
     * The pdu has been passed to the communication system for transmission.
     * SLE_S_QUEUED The pdu has been queud locally. SLE_E_COMMS The
     * communication system has failed. E_FAIL The post-processing has failed.
     */
    protected HRESULT clientPostProcessing(ISLE_Operation pOperation,
                                           boolean reportTransmission,
                                           boolean isInvoke,
                                           boolean lastPdu)
    {

        HRESULT res = HRESULT.E_FAIL;

        if (this.channelInitiate == null)
        {
            return HRESULT.SLE_S_QUEUED;
        }

        insertSecurityAttributes(pOperation, isInvoke);

        if (pOperation.getOperationType() == SLE_OpType.sleOT_bind)
        {
            // insertion of local application identifier
            EE_APIPX_LocalApplData pLocalApplData = this.database.getLocalApplicationData();
            String localId = pLocalApplData.getID();
            ISLE_Bind pBind = pOperation.queryInterface(ISLE_Bind.class);
            if (pBind != null)
            {
                pBind.setInitiatorIdentifier(localId);
            }
        }

        if (!this.suspendXmit)
        {
            // encode and send the pdu immediately
            byte[] data = null;
            try
            {
                data = this.pduTranslator.encode(pOperation, isInvoke);
                //this.suspendXmit = true;
                dumpPdu(false, data);
                this.objMutex.unlock();
                HRESULT sendRes = HRESULT.E_FAIL;
                if (this.channelInitiate != null)
                {
                    sendRes = this.channelInitiate.sendSLEPDU(data, lastPdu);
                }
                this.objMutex.lock();
                
                if(sendRes == HRESULT.SLE_S_SUSPEND)
                {
                	this.suspendXmit = true; // SLEAPIJ-26
                }

                res = HRESULT.SLE_S_TRANSMITTED; // OK, we are not yet suspended!
            }
            catch (SleApiException | IOException e)
            {
                String mess = EE_MessageRepository.getMessage(1004,
                                                              SLE_PeerAbortDiagnostic.slePAD_encodingError.toString(),
                                                              e.getMessage());
                notify(SLE_LogMessageType.sleLM_alarm, SLE_Alarm.sleAL_localAbort, 1004, mess, null);
            }
        }
        else
        {
            PXSPL_Operation theOp = new PXSPL_Operation();
            if (this.traceStarted && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_high.getCode()
                && this.trace != null)
            {
                // trace
                String messop = pOperation.print(512);
                traceRecord(SLE_TraceLevel.sleTL_high, null, 1007, messop);
            }

            theOp.setpOperation(pOperation);
            theOp.setInvoke(isInvoke);
            theOp.setLastPdu(lastPdu);
            theOp.setReportTransmission(reportTransmission);

            // queue the pdu
            this.iOperation.addLast(theOp);
 
            res = HRESULT.SLE_S_QUEUED;
        }

        return res;
    }

    /**
     * Notify an alarm.
     */
    protected void notify(SLE_LogMessageType type, SLE_Alarm alarm, int messId, String text, ISLE_SII psii)
    {
        if (this.reporter != null)
        {
            if (psii == null)
            {
                if (this.srvProxyInform != null)
                {
                    ISLE_SIAdmin pSIAdmin = this.srvProxyInform.queryInterface(ISLE_SIAdmin.class);

                    if (pSIAdmin != null)
                    {
                        psii = pSIAdmin.getServiceInstanceIdentifier();
                    }
                }

                this.reporter.notify(alarm, SLE_Component.sleCP_proxy, psii, messId, text);
                this.reporter.logRecord(SLE_Component.sleCP_proxy, psii, type, messId, text);
            }
        }
    }

    /**
     * Sets the authentication mode and the security attributes to the
     * association.
     */
    protected void setSecurityAttributes(String peerId)
    {
        if (peerId == null)
        {
            if (this.iSecAttr != null)
            {
                this.iSecAttr = null;
            }
            return;
        }

        if (this.iSecAttr == null)
        {
            ISLE_SecAttributes pIsleSecAtt = null;

            try
            {
                pIsleSecAtt = this.utilFactory.createSecAttributes(ISLE_SecAttributes.class);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
                return;
            }

            String username = null;
            byte[] passwd = null;

            this.iSecAttr = pIsleSecAtt;

            // set the authentication mode: always taken in the peer-application
            EE_APIPX_PeerApplDataList pPeerApplList = this.database.getPeerApplDataList();
            EE_APIPX_PeerApplData pPeerAppl = pPeerApplList.getPeerApplDataItemByID(peerId);
            if (pPeerAppl != null)
            {
                this.authenticationMode = pPeerAppl.getAuthenticationMode();

                // set the user name and password
                // take the user name and password in the peer application
                username = pPeerAppl.getId();
                passwd = pPeerAppl.getPassword();
            }

            this.iSecAttr.setUserName(username);
            this.iSecAttr.setPassword(passwd);
        }

    }

    /**
     * Changes the state of the association and performs a trace if necessary.
     */
    protected void changeState(EE_APIPX_Event event, SLE_AssocState newState)
    {
        SLE_AssocState oldState = this.state;

        if (oldState == newState)
        {
            return;
        }

        this.state = newState;

        if (this.traceStarted && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_low.getCode() && this.trace != null)
        {
            ISLE_SII psii = null;
            ISLE_SIAdmin pSiAdmin = this.srvProxyInform.queryInterface(ISLE_SIAdmin.class);

            if (this.srvProxyInform != null && pSiAdmin != null)
            {
                psii = pSiAdmin.getServiceInstanceIdentifier();
            }

            traceRecord(SLE_TraceLevel.sleTL_low,
                        psii,
                        1005,
                        event.toString(),
                        oldState.toString(),
                        newState.toString());
        }
    }

    /**
     * Dump the content of the PDU by performing a trace.
     */
    public void dumpPdu(boolean rcv_from_network, byte[] pdu)
    {
        if (this.traceStarted && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_full.getCode() && this.trace != null)
        {
            ISLE_SII psii = null;
            if (this.srvProxyInform != null)
            {
                ISLE_SIAdmin pSiAdmin = this.srvProxyInform.queryInterface(ISLE_SIAdmin.class);
                if (this.srvProxyInform != null && pSiAdmin != null)
                {
                    psii = pSiAdmin.getServiceInstanceIdentifier();
                }
            }

            int max_lg;
            final EE_APIPX_ProxySettings pProxySettings = this.database.getProxySettings();
            max_lg = pProxySettings.getMaxTraceLength();
            int lg = pdu.length;
            if (lg < max_lg)
            {
                max_lg = lg;
            }
            String mess = EE_GenStrUtil.convAscii(pdu, max_lg);
            this.trace.traceRecord(SLE_TraceLevel.sleTL_full, SLE_Component.sleCP_application, psii, mess);
        }
    }

    /**
     * Release the Channel interface.
     */
    public void releaseChannel()
    {
        if (this.channelInitiate != null)
        {
            this.channelInitiate.dispose();
            this.channelInitiate = null;
        }
    }

    /**
     * Inserts the credentials in the operation.
     */
    private void insertCredentialsInOp(ISLE_Operation poperation, ISLE_Credentials pcredentials)
    {
        if (poperation.getOperationType() == SLE_OpType.sleOT_transferBuffer)
        {
            ISLE_Operation pCurrentOp = null;

            // set the credentials for the transfer buffer op
            poperation.putInvokerCredentials(pcredentials);

            ISLE_TransferBuffer pTransferBufferOperation = poperation.queryInterface(ISLE_TransferBuffer.class);
            if (pTransferBufferOperation != null)
            {
                // for all the operation of the transfer buffer
                pTransferBufferOperation.reset();
                while (pTransferBufferOperation.moreData())
                {
                    pCurrentOp = pTransferBufferOperation.next();
                    pCurrentOp.putInvokerCredentials(pcredentials);
                }
            }
        }
        else
        {
            poperation.putInvokerCredentials(pcredentials);
        }
    }

    /**
     * Inserts the credentials in the operation.
     */
    private void insertCredentialsInConfOp(ISLE_ConfirmedOperation poperation, ISLE_Credentials pcredentials)
    {
        poperation.putPerformerCredentials(pcredentials);
    }

    /**
     * If authentication is required, authentication for one operation.
     */
    private boolean authenticateOperation(ISLE_Operation poperation, boolean isInvoke, int delay)
    {
        ISLE_Credentials pOpCredentials = null;
        boolean res = false;

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Authenticate Operation: Invoke=" + isInvoke + ", operation type="
                       + poperation.getOperationType() + " delay=" + delay);
        }

        if (isInvoke)
        {
            if (poperation.getOperationType() == SLE_OpType.sleOT_transferBuffer)
            {
                ISLE_Operation pCurrentOp = null;
                ISLE_TransferBuffer pTransferBufferOperation = poperation.queryInterface(ISLE_TransferBuffer.class);
                if (pTransferBufferOperation != null)
                {
                    // for all the operation of the transfer buffer
                    pTransferBufferOperation.reset();
                    while (pTransferBufferOperation.moreData())
                    {
                        pCurrentOp = pTransferBufferOperation.next();
                        pOpCredentials = pCurrentOp.getInvokerCredentials();
                        if (pOpCredentials == null)
                        {
                            return false;
                        }
                        // Added 
                        res = this.iSecAttr.authenticate(pOpCredentials, delay, poperation.getOpVersionNumber());
                        if (!res)
                        {
                            break;
                        }
                    }
                }
            }
            else
            {
                pOpCredentials = poperation.getInvokerCredentials();
                if (pOpCredentials == null)
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("Operation Credentials NULL");
                    }
                    return false;
                }

                res = this.iSecAttr.authenticate(pOpCredentials, delay, poperation.getOpVersionNumber());
            }
        }
        else
        {
            ISLE_ConfirmedOperation pConfOp = poperation.queryInterface(ISLE_ConfirmedOperation.class);
            if (pConfOp != null)
            {
                pOpCredentials = pConfOp.getPerformerCredentials();
            }

            if (pOpCredentials == null)
            {
                return false;
            }

            res = this.iSecAttr.authenticate(pOpCredentials, delay, pConfOp.getOpVersionNumber());
        }

        return res;
    }

    /**
     * Initialises the association.
     */
    protected void initAssoc()
    {
        this.isAborted = false;
        this.isReleased = false;
        this.suspendXmit = true;
        this.serviceType = SLE_ApplicationIdentifier.sleAI_invalid;
        setSecurityAttributes(null);
        this.sequenceCounter = 1;
        this.authenticationMode = SLE_AuthenticationMode.sleAM_none;
    }

    /**
     * Increments the sequence counting.
     */
    protected long incSeqCounter()
    {
        return this.sequenceCounter++;
    }

    private void traceRecord(SLE_TraceLevel level, ISLE_SII psii, long msgId, String... p)
    {
        String msg = EE_MessageRepository.getMessage(msgId, p);
        this.trace.traceRecord(level, SLE_Component.sleCP_proxy, psii, msg);
    }

    public ISLE_Trace getTrace()
    {
        return this.trace;
    }

    public void setTrace(ISLE_Trace trace)
    {
        this.trace = trace;
    }

    public SLE_ApplicationIdentifier getServiceType()
    {
        return this.serviceType;
    }

    public void setServiceType(SLE_ApplicationIdentifier serviceType)
    {
        this.serviceType = serviceType;
    }

    protected SLE_AssocState getState()
    {
        return this.state;
    }

    public boolean isUnboundStateIsDisconnected()
    {
        return this.unboundStateIsDisconnected;
    }

    public void setUnboundStateIsDisconnected(boolean unboundStateIsDisconnected)
    {
        this.unboundStateIsDisconnected = unboundStateIsDisconnected;
    }

    protected long getSequenceCounter()
    {
        return this.sequenceCounter;
    }

    protected void setSequenceCounter(long sequenceCounter)
    {
        this.sequenceCounter = sequenceCounter;
    }

    public SLE_TraceLevel getTraceLevel()
    {
        return this.traceLevel;
    }

    public void setTraceLevel(SLE_TraceLevel traceLevel)
    {
        this.traceLevel = traceLevel;
    }

    public boolean isSuspendXmit()
    {
        return this.suspendXmit;
    }

    public void setSuspendXmit(boolean suspendXmit)
    {
        this.suspendXmit = suspendXmit;
    }

    public EE_APIPX_Database getDatabase()
    {
        return this.database;
    }

    public void setDatabase(EE_APIPX_Database database)
    {
        this.database = database;
    }

    public ISLE_Reporter getReporter()
    {
        return this.reporter;
    }

    public void setReporter(ISLE_Reporter reporter)
    {
        this.reporter = reporter;
    }

    public ISLE_OperationFactory getOpFactory()
    {
        return this.opFactory;
    }

    public void setOpFactory(ISLE_OperationFactory opFactory)
    {
        this.opFactory = opFactory;
    }

    public ISLE_UtilFactory getUtilFactory()
    {
        return this.utilFactory;
    }

    public void setUtilFactory(ISLE_UtilFactory utilFactory)
    {
        this.utilFactory = utilFactory;
    }

    public SLE_AuthenticationMode getAuthenticationMode()
    {
        return this.authenticationMode;
    }

    public void setAuthenticationMode(SLE_AuthenticationMode authenticationMode)
    {
        this.authenticationMode = authenticationMode;
    }

    public boolean isAborted()
    {
        return this.isAborted;
    }
    
    public boolean isTraceStarted()
    {
    	return this.traceStarted;
    }

    public void setAborted(boolean isAborted)
    {
        this.isAborted = isAborted;
    }

    public boolean isReleased()
    {
        return this.isReleased;
    }

    public void setReleased(boolean isReleased)
    {
        this.isReleased = isReleased;
    }

    public int getQueueSize()
    {
        return this.queueSize;
    }

    public void setQueueSize(int queueSize)
    {
        this.queueSize = queueSize;
    }

    public int getVersion()
    {
        return this.version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    protected ISLE_SrvProxyInform getSrvProxyInform()
    {
        return this.srvProxyInform;
    }

    public void setRole(SLE_BindRole role)
    {
        this.role = role;
    }

}
