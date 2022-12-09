/**
 * @(#) EE_APIPX_InitiatingAssoc.java
 */

package esa.sle.impl.api.apipx.pxspl;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.ipx.ISLE_SrvProxyInitiate;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_AssocState;
import ccsds.sle.api.isle.it.SLE_BindRole;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_Database;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_LocalApplData;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_PeerApplData;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_PeerApplDataList;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_ProxySettings;
import esa.sle.impl.api.apipx.pxdel.EE_APIPX_PDUTranslator;
import esa.sle.impl.api.apipx.pxspl.types.EE_APIPX_Event;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_ISP1ProtocolAbortDiagnostics;
import esa.sle.impl.ifs.gen.EE_MessageRepository;

/**
 * The class EE_APIPX_InitiatingAssoc implements those aspects of an association
 * that are specific to initiating associations. It is responsible for: -
 * establishment/release of an association to its peer application when a
 * BIND/UNBIND invocation is received from the local application, which includes
 * connect/disconnect call to the TML. - access control for BIND PDU's as
 * specified for initiating associations. - authentication if required. -
 * generation of credentials and version handling. The initiating asociation is
 * created and deleted by the Proxy. Only functionalities specific to initiating
 * associations are implemented in this class. For common functionalities,
 * methods of the base class will be called.
 */
public class EE_APIPX_InitiatingAssoc extends EE_APIPX_Association
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_InitiatingAssoc.class.getName());

    /**
     * The responder identifier. This attribute is set when a bind invoke is
     * received from the client, and checked when the bind return pdu is
     * received on the network interface.
     */
    private String responderIdentifier;


    /**
     * Constructor of the class.
     */
    public EE_APIPX_InitiatingAssoc(String instanceKey,
    								EE_APIPX_Proxy proxy,
    								EE_APIPX_Database pdatabase,
                                    ISLE_Reporter preporter,
                                    ISLE_OperationFactory popfactory,
                                    ISLE_UtilFactory putilfactory)
    {
    	super(instanceKey, proxy);
        this.responderIdentifier = null;
        this.reporter = preporter;
        this.database = pdatabase;
        if (this.database != null)
        {
            EE_APIPX_ProxySettings pProxySettings = pdatabase.getProxySettings();
            this.queueSize = pProxySettings.getTransmissionQueueSize();
        }
        this.opFactory = popfactory;
        this.utilFactory = putilfactory;
        this.role = SLE_BindRole.sleBR_initiator;
        this.pduTranslator = new EE_APIPX_PDUTranslator(this.opFactory, this.utilFactory);
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
        else if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
     * Reception of a BIND from the peer proxy. Not authorized in the initiator
     * role : calls the abort methods of the association. S_OK The processing is
     * complete. E_FAIL The processing has failed.
     */
    @Override
    public HRESULT rcvBindInvoke(ISLE_Operation poperation)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("BIND received: " + poperation);
        }

        doAbort(SLE_PeerAbortDiagnostic.slePAD_protocolError, SLE_AbortOriginator.sleAO_proxy, true);
        changeState(EE_APIPX_Event.PXSPL_rcvBindInvoke, SLE_AssocState.sleAST_unbound);
        this.unboundStateIsDisconnected = true;
        releaseAssociation();
        return HRESULT.SLE_E_PROTOCOL;
    }

    /**
     * Reception of a BIND Return from the peer proxy. If the state is
     * "bind pend": - checks if the id is registered. - checks if the responder
     * is the expected one. - sets the receiving sequence counter to 1. -
     * forwards the BIND Return pdu to the local client. Otherwise, aborts the
     * association. S_OK The processing is complete. SLE_E_PROTOCOL The
     * operation cannot be accepted in the current state. SLE_E_UNBINDING The
     * pdu can no onger be accepted because an unbinding operation has already
     * been initialised. E_FAIL The processing has failed.
     */
    @Override
    public HRESULT rcvBindReturn(ISLE_Operation poperation)
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("BIND-RETURN received: " + poperation);
        }

        if (getState() == SLE_AssocState.sleAST_unbound)
        {
            return HRESULT.E_FAIL;
        }
        else if (getState() == SLE_AssocState.sleAST_bindPending)
        {
            // get a bind operation
            SLE_PeerAbortDiagnostic diag = SLE_PeerAbortDiagnostic.slePAD_invalid;
            boolean error = false;

            ISLE_Bind pBind = poperation.queryInterface(ISLE_Bind.class);
            if (pBind != null)
            {
                // check if the responder identifier is registered in the peer
                // application list
                EE_APIPX_PeerApplDataList pPeerAppliDataList = this.database.getPeerApplDataList();
                EE_APIPX_PeerApplData pPeerApplData = null;
                String rspid = pBind.getResponderIdentifier();

                if (rspid != null)
                {
                    pPeerApplData = pPeerAppliDataList.getPeerApplDataItemByID(rspid);
                }

                if (pPeerApplData == null)
                {
                    diag = SLE_PeerAbortDiagnostic.slePAD_accessDenied;
                    error = true;
                }
                else
                {
                    // check if the responder is the expected one
                    if (rspid.compareTo(this.responderIdentifier) != 0)
                    {
                        diag = SLE_PeerAbortDiagnostic.slePAD_unexpectedResponderId;
                        error = true;
                    }
                    else
                    {
                        // send the bind return to the service instance
                        if (pBind.getResult() == SLE_Result.sleRES_positive)
                        {
                            // set the state to bound
                            changeState(EE_APIPX_Event.PXSPL_rcvBindReturn, SLE_AssocState.sleAST_bound);
                        }
                        else
                        {
                            // set the state to unbound
                            changeState(EE_APIPX_Event.PXSPL_rcvBindReturn, SLE_AssocState.sleAST_unbound);
                            this.unboundStateIsDisconnected = true;
                            // abort the connection
                            this.objMutex.unlock();
                            this.channelInitiate.sendDisconnect();
                            this.objMutex.lock();
                        }

                        long oldSeq = getSequenceCounter();
                        incSeqCounter();

                        this.objMutex.unlock();
                        try
                        {
                            if (LOG.isLoggable(Level.FINE))
                            {
                                LOG.fine("seqCount : " + oldSeq + "   " + poperation.getOperationType());
                            }
                            getSrvProxyInform().informOpReturn(pBind, oldSeq);
                        }
                        catch (SleApiException e)
                        {
                            LOG.log(Level.FINE, "SleApiException ", e);
                            return e.getHResult();
                        }
                        finally
                        {
                            this.objMutex.lock();
                        }
                    }
                }

                if (error)
                {
                    // generate an alarm
                    ISLE_SII psii = pBind.getServiceInstanceId();
                    String tmp = pBind.print(512);
                    String mess = EE_MessageRepository.getMessage(1002, diag.toString(), tmp);
                    notify(SLE_LogMessageType.sleLM_alarm, SLE_Alarm.sleAL_authFailure, 1002, mess, psii);
                    // abort the association
                    doAbort(diag, SLE_AbortOriginator.sleAO_proxy, true);
                    changeState(EE_APIPX_Event.PXSPL_rcvBindReturn, SLE_AssocState.sleAST_unbound);
                    this.unboundStateIsDisconnected = true;
                    // delete the association
                    releaseAssociation();
                    return HRESULT.E_FAIL;
                }
            }
            else
            {
                // query interface for bind fail!
                return HRESULT.E_FAIL;
            }
        }
        else
        {
            doAbort(SLE_PeerAbortDiagnostic.slePAD_protocolError, SLE_AbortOriginator.sleAO_proxy, true);
            changeState(EE_APIPX_Event.PXSPL_rcvBindReturn, SLE_AssocState.sleAST_unbound);
            this.unboundStateIsDisconnected = true;
            releaseAssociation();
            return HRESULT.SLE_E_PROTOCOL;
        }

        return HRESULT.S_OK;
    }

    /**
     * Reception of a UNBIND Invoke from the peer proxy. Not authorized in the
     * initiator role : calls the abort methods of the association. S_OK The
     * processing is complete. E_FAIL The processing has failed.
     */
    @Override
    public HRESULT rcvUnbindInvoke(ISLE_Operation poperation)
    {
        if (getState() == SLE_AssocState.sleAST_unbound)
        {
            return HRESULT.E_FAIL;
        }
        else
        {
            doAbort(SLE_PeerAbortDiagnostic.slePAD_protocolError, SLE_AbortOriginator.sleAO_proxy, true);
            changeState(EE_APIPX_Event.PXSPL_rcvUnbindInvoke, SLE_AssocState.sleAST_unbound);
            this.unboundStateIsDisconnected = true;
            releaseAssociation();
            return HRESULT.SLE_E_PROTOCOL;
        }
    }

    /**
     * Reception of a UNBIND Return from the peer proxy. If the state is
     * "loc unbind pend": - discards all the operation of the sending queue. -
     * increments the receiving sequence counter. - forwards the UNBIND
     * operation to the local client. Otherwise, aborts the association. S_OK
     * The processing is complete. SLE_E_PROTOCOL The operation cannot be
     * accepted in the current state. E_FAIL The processing has failed.
     */
    @Override
    public HRESULT rcvUnbindReturn(ISLE_Operation poperation)
    {
        if (getState() == SLE_AssocState.sleAST_unbound)
        {
            return HRESULT.E_FAIL;
        }
        else if (getState() == SLE_AssocState.sleAST_localUnbindPending)
        {
            // send the unbind return to the service instance
            // set the state to unbound
            changeState(EE_APIPX_Event.PXSPL_rcvUnbindReturn, SLE_AssocState.sleAST_unbound);
            this.unboundStateIsDisconnected = true;

            this.objMutex.unlock();
            this.oPSequencer.reset(HRESULT.SLE_E_UNBINDING);
            this.objMutex.lock();

            // cleanup
            discardAllInvocationPdu();
            clearAllPendingReturn();

            // terminate the connection
            if (this.channelInitiate != null)
            {
                this.objMutex.unlock();
                this.channelInitiate.sendDisconnect();
                this.objMutex.lock();
            }

            // send the operation to the service element after disconnect
            // otherwise, the service element can do a destroy association
            // which release the channel before the disconnect is sent
            ISLE_ConfirmedOperation pConfOp = poperation.queryInterface(ISLE_ConfirmedOperation.class);
            if (pConfOp != null)
            {
                long oldSeq = getSequenceCounter();
                incSeqCounter();
                this.objMutex.unlock();
                try
                {
                    getSrvProxyInform().informOpReturn(pConfOp, oldSeq);
                }
                catch (SleApiException e)
                {
                    if (LOG.isLoggable(Level.FINEST))
                    {
                        LOG.finest("received hresult code: " + e.getHResult());
                    }

                    return e.getHResult();
                }
                finally
                {
                    this.objMutex.lock();
                }
            }
        }
        else
        {
            doAbort(SLE_PeerAbortDiagnostic.slePAD_protocolError, SLE_AbortOriginator.sleAO_proxy, true);
            changeState(EE_APIPX_Event.PXSPL_rcvUnbindReturn, SLE_AssocState.sleAST_unbound);
            this.unboundStateIsDisconnected = true;
            releaseAssociation();
        }

        return HRESULT.S_OK;
    }

    /**
     * Receives a PEER_ABORT request. Lock and calls the base class.
     */
    @Override
    public void rcvPeerAbort(int diagnostic, boolean originatorIsLocal)
    {

        this.innerLock.lock();
        this.objMutex.lock();

        try
        {
            super.rcvPeerAbort(diagnostic, originatorIsLocal);
        }
        finally
        {

            this.objMutex.unlock();
            this.innerLock.unlock();

        }
    }

    /**
     * Receives a PROTOCOL_ABORT request. Lock and calls the base class.
     */
    @Override
    public void rcvProtocolAbort(EE_APIPX_ISP1ProtocolAbortDiagnostics diagnostic)
    {
        this.innerLock.lock();
        this.objMutex.lock();
        try
        {
            super.rcvProtocolAbort(diagnostic);
        }
        finally
        {
            this.objMutex.unlock();
            this.innerLock.unlock();
        }
    }

    /**
     * Receives a BIND request from the local client : - checks if the responder
     * identifier is valid. - assigns the authentication mode in the
     * association. - assigns the security attributes in the association, if
     * applicable. - inserts the local application identifier in the BIND pdu. -
     * inserts the highest version number in the BIND pdu. - creates the
     * Channel, and connects it. - transmits the BIND pdu to the peer proxy. See
     * specification of ISLE_SrvProxyInitiate.
     */
    @Override
    public HRESULT initiateBindInvoke(ISLE_Operation poperation, boolean reportTransmission)
    {
        if (getState() != SLE_AssocState.sleAST_unbound)
        {
            return HRESULT.SLE_E_PROTOCOL;
        }
        else
        {
            if (!this.unboundStateIsDisconnected)
            {
                // a connect has already been sent to TML
                return HRESULT.SLE_E_PROTOCOL;
            }
        }

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("TH" + Thread.currentThread().getId() + " is calling initAssoc()");
        }
        initAssoc();
        HRESULT res = HRESULT.S_OK;
        ISLE_Bind pBind = poperation.queryInterface(ISLE_Bind.class);
        if (pBind != null)
        {
            String rspid = pBind.getResponderIdentifier();
            if (rspid == null)
            {
                return HRESULT.SLE_E_INVALIDID;
            }

            // check if the responder identifier of the bind invoke is
            // registered in the peer application list
            EE_APIPX_PeerApplDataList pPeerApplDataList = this.database.getPeerApplDataList();
            EE_APIPX_PeerApplData pPeerApplData = pPeerApplDataList.getPeerApplDataItemByID(rspid);
            if (pPeerApplData == null)
            {
                return HRESULT.SLE_E_INVALIDID;
            }
            else
            {
                // set the service type
                this.serviceType = pBind.getOpServiceType();
                // set the version number
                this.version = pBind.getOpVersionNumber();
                // assign authentication mode and security attributes to the
                // association
                setSecurityAttributes(rspid);
                // assign the responder identifier
                this.responderIdentifier = rspid;
                // insert the local application identifier in the bind operation
                EE_APIPX_LocalApplData pLocalApplData = this.database.getLocalApplicationData();
                pBind.setInitiatorIdentifier(pLocalApplData.getID());
                this.unboundStateIsDisconnected = false;
                // connect the association
                String port = pBind.getResponderPortIdentifier();
                // release the previous channel if necessary
                releaseChannel();
                // instantiate a channel through the channel factory
                IEE_ChannelInitiate pChannelInitiate = EE_APIPX_ChannelFactory.createChannel(super.instanceId, true, this.reporter, null);
                if (pChannelInitiate != null)
                {
                    // set the channel initiate interface in the association
                    this.channelInitiate = pChannelInitiate;
                    pChannelInitiate.configure(this.reporter, this.database);

                    // start the trace if necessary in the channel
                    if (this.traceStarted && this.trace != null)
                    {
                        ISLE_TraceControl pTraceControl = pChannelInitiate.queryInterface(ISLE_TraceControl.class);
                        if (pTraceControl != null)
                        {
                            try
                            {
                                pTraceControl.startTrace(this.trace, this.traceLevel, false);
                            }
                            catch (SleApiException e)
                            {
                                LOG.log(Level.FINE, "SleApiException ", e);
                            }
                        }
                    }

                    // set the channelInform interface in the channel
                    IEE_ChannelInform pChannelInform = this.queryInterface(IEE_ChannelInform.class);
                    if (pChannelInform != null)
                    {
                        pChannelInitiate.setChannelInform(pChannelInform);
                    }

                    if (this.traceStarted && this.traceLevel.getCode() >= SLE_TraceLevel.sleTL_medium.getCode()
                        && this.trace != null)
                    {
                        // trace
                        String mess = EE_MessageRepository.getMessage(1012, port);
                        this.trace.traceRecord(SLE_TraceLevel.sleTL_medium, SLE_Component.sleCP_proxy, null, mess);
                    }

                    this.objMutex.unlock();
                    pChannelInitiate.sendConnect(port);
                    this.objMutex.lock();

                    // send the bind invoke to the network
                    // the operation is encoded and sent in the post-processing
                    res = clientPostProcessing(poperation, reportTransmission, true, false);
                }
                else
                {
                    // cannot create a channel!
                    return HRESULT.E_FAIL;
                }
            }

        }
        else
        {
            // query interface failed!
            return HRESULT.E_FAIL;
        }
        return res;
    }

    /**
     * Receives a UNBIND request from the local client. If the state is bound,
     * forward the UNBIND to the peer-proxy. See specification of
     * ISLE_SrvProxyInitiate.
     */
    @Override
    public HRESULT initiateUnbindInvoke(ISLE_Operation poperation, boolean report)
    {
        if (getState() != SLE_AssocState.sleAST_bound)
        {
            return HRESULT.SLE_E_PROTOCOL;
        }

        // set the state to local unbind pend
        changeState(EE_APIPX_Event.PXSPL_initiateUnbindInvoke, SLE_AssocState.sleAST_localUnbindPending);

        // send the unbind invoke to the network
        HRESULT res = clientPostProcessing(poperation, report, true, false);
        return res;
    }

    /**
     * Receives a BIND Return from the local client. For a initiating
     * association, this is a protocol error. Returns the appropriate error
     * code. See specification of ISLE_SrvProxyInitiate.
     */
    @Override
    public HRESULT initiateBindReturn(ISLE_ConfirmedOperation poperation, boolean report)
    {
        return HRESULT.SLE_E_PROTOCOL;
    }

    /**
     * Receives an UNBIND Return from the local client. For a initiating
     * association, this is a protocol error. Returns the appropriate error
     * code. See specification of ISLE_SrvProxyInitiate.
     */
    @Override
    public HRESULT initiateUnbindReturn(ISLE_ConfirmedOperation poperation, boolean report)
    {
        return HRESULT.SLE_E_PROTOCOL;
    }
}
