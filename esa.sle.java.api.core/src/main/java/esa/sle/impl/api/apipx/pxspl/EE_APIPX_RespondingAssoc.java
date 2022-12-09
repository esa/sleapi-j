/**
 * @(#) EE_APIPX_RespondingAssoc.java
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
import ccsds.sle.api.isle.iop.ISLE_PeerAbort;
import ccsds.sle.api.isle.ipx.ISLE_SrvProxyInitiate;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_Locator;
import ccsds.sle.api.isle.ise.ISLE_SrvProxyInform;
import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_AssocState;
import ccsds.sle.api.isle.it.SLE_BindDiagnostic;
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
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_SrvType;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_SrvTypeList;
import esa.sle.impl.api.apipx.pxdel.EE_APIPX_PDUTranslator;
import esa.sle.impl.api.apipx.pxspl.types.EE_APIPX_Event;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_ISP1ProtocolAbortDiagnostics;
import esa.sle.impl.ifs.gen.EE_MessageRepository;

/**
 * The class EE_APIPX_RespondingAssoc implements those aspects of an association
 * that are specific to responding associations. It is responsible for : -
 * processing of BIND and UNBIND invocations received from the peer application.
 * - location of the service instance using ISLE_Locator after reception of a
 * BIND invocation. - access control for BIND PDU's as specified for responding
 * associations. - authentication if required. - generation of credentials and
 * version handling. - initiate TCP connection release after the transmission of
 * an UNBIND return. Responding asociations are created when a bind operation is
 * received from TML, and they are deleted when the UNBIND procedure is
 * complete, or when PEER-ABORT occurs. Only functionalities specific to
 * responding associations are implemented in this class. For common
 * functionalities, methods of the base class will be called.
 */
public class EE_APIPX_RespondingAssoc extends EE_APIPX_Association
{

    private static final Logger LOG = Logger.getLogger(EE_APIPX_RespondingAssoc.class.getName());


    /**
     * Creator of the Responding Association. The reference to the
     * ChannelInitiate, and the reference to the reporter and to the database
     * are given as parameter.
     */
    public EE_APIPX_RespondingAssoc(String instanceKey,
    								EE_APIPX_Proxy proxy,
    								IEE_ChannelInitiate pChannelInitiate,
                                    EE_APIPX_Database pdatabase,
                                    ISLE_Reporter preporter,
                                    ISLE_OperationFactory popfactory,
                                    ISLE_UtilFactory putilfactory)
    {
    	super(instanceKey, proxy);
        this.reporter = preporter;
        this.database = pdatabase;
        if (pdatabase != null)
        {
            EE_APIPX_ProxySettings pProxySettings = pdatabase.getProxySettings();
            this.queueSize = pProxySettings.getTransmissionQueueSize();
        }
        this.opFactory = popfactory;
        this.utilFactory = putilfactory;
        this.channelInitiate = pChannelInitiate;
        this.role = SLE_BindRole.sleBR_responder;
        this.suspendXmit = false;

        if (this.channelInitiate != null)
        {
            // set the channel inform
            IEE_ChannelInform pChannelInform = queryInterface(IEE_ChannelInform.class);
            this.channelInitiate.setChannelInform(pChannelInform);
            this.channelInitiate.configure(preporter, pdatabase);
        }

        this.pduTranslator = new EE_APIPX_PDUTranslator(popfactory, putilfactory);
        this.serviceType = SLE_ApplicationIdentifier.sleAI_invalid;
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
     * Reception of a BIND Invoke from the peer proxy. If the state is unbound :
     * - checks if the responder identifier is registered. - locates the
     * initiator. - assigns the authentication mode and security attributes. -
     * sets the receiving sequence counter to 1. - forwards the BIND pdu to the
     * ISLE_SrvProxyInform interface. Otherwise, aborts the association, and
     * deletes it. S_OK The processing is complete. SLE_E_PROTOCOL The operation
     * cannot be accepted in the current state. SLE_E_UNBINDING The pdu can no
     * onger be accepted because an unbinding operation has already been
     * initialized. E_FAIL The processing has failed.
     */
    @Override
    public HRESULT rcvBindInvoke(ISLE_Operation poperation)
    {
        if (getState() != SLE_AssocState.sleAST_unbound)
        {
            doAbort(SLE_PeerAbortDiagnostic.slePAD_protocolError, SLE_AbortOriginator.sleAO_proxy, true);
            changeState(EE_APIPX_Event.PXSPL_rcvBindInvoke, SLE_AssocState.sleAST_unbound);
            this.unboundStateIsDisconnected = true;
            releaseAssociation();
            return HRESULT.SLE_E_PROTOCOL;
        }

        // get a bind operation
        ISLE_Bind pBind = null;
        SLE_BindDiagnostic diag = SLE_BindDiagnostic.sleBD_invalid;
        boolean error = false;
        boolean doAlarm = false;

        pBind = poperation.queryInterface(ISLE_Bind.class);
        if (pBind != null)
        {
            EE_APIPX_PeerApplDataList pPeerApplDataList = this.database.getPeerApplDataList();
            EE_APIPX_PeerApplData pPeerApplData = pPeerApplDataList.getPeerApplDataItemByID(pBind
                    .getInitiatorIdentifier());
            if (pPeerApplData == null)
            {
                diag = SLE_BindDiagnostic.sleBD_accessDenied;
                error = true;
                doAlarm = true;
            }
            else
            {
                // set the authentication mode and the security attributes
                setSecurityAttributes(pBind.getInitiatorIdentifier());
                // authenticate the pdu
                if (!authenticate(poperation, true))
                {
                    diag = SLE_BindDiagnostic.sleBD_accessDenied;
                    error = true;
                    doAlarm = true;
                    ISLE_SII psii = pBind.getServiceInstanceId();

                    // generate an authentication alarm
                    String tmp = pBind.print(512);
                    String mess = EE_MessageRepository.getMessage(1002, SLE_Alarm.sleAL_authFailure.toString(), tmp);
                    notify(SLE_LogMessageType.sleLM_alarm, SLE_Alarm.sleAL_authFailure, 1002, mess, psii);

                    if (getState() == SLE_AssocState.sleAST_unbound)
                    {
                        // reset the connection
                        this.objMutex.unlock();
                        this.channelInitiate.sendReset();
                        this.objMutex.lock();
                    }
                    else
                    {
                        // abort the connection
                        doAbort(SLE_PeerAbortDiagnostic.slePAD_otherReason, SLE_AbortOriginator.sleAO_proxy, true);
                    }

                    // cleanup
                    discardAllInvocationPdu();
                    clearAllPendingReturn();

                    changeState(EE_APIPX_Event.PXSPL_rcvBindInvoke, SLE_AssocState.sleAST_unbound);
                    this.unboundStateIsDisconnected = true;

                    // delete the association
                    releaseAssociation();

                    return HRESULT.E_FAIL;
                }

                // check if the service type is supported
                EE_APIPX_SrvTypeList pSrvTypeList = this.database.getSrvTypeList();
                EE_APIPX_SrvType pSrvType = pSrvTypeList.getSrvTypeByType(pBind.getServiceType());
                if (pSrvType == null)
                {
                    diag = SLE_BindDiagnostic.sleBD_serviceTypeNotSupported;
                    error = true;
                }
                else
                {
                    // set the service type
                    this.serviceType = pBind.getOpServiceType();
                    // set the version number
                    this.version = pBind.getVersionNumber();
                    // check if the version is supported
                    int indexMax = pSrvType.getNumVersions();
                    int versionNumber = -1;
                    int versionNumberBind = pBind.getVersionNumber();
                    boolean founded = false;

                    for (int index = 0; index < indexMax; index++)
                    {
                        versionNumber = pSrvType.getVersion(index);
                        if (versionNumber != -1)
                        {
                            if (versionNumber == versionNumberBind)
                            {
                                founded = true;
                                break;
                            }
                            else if (versionNumber > versionNumberBind)
                            {
                                break;
                            }
                        }
                    }

                    if (!founded)
                    {
                        diag = SLE_BindDiagnostic.sleBD_versionNotSupported;
                        error = true;
                    }
                    else
                    {
                        ISLE_SrvProxyInform pSrvInform = null;
                        ISLE_SrvProxyInitiate pSrvInitiate = queryInterface(ISLE_SrvProxyInitiate.class);
                        if (pSrvInitiate != null)
                        {
                            // try t locate the service instance
                            ISLE_Locator pLocator = this.proxy.getLocator();
                            long seqCount = incSeqCounter();
                            this.objMutex.unlock();
                            try
                            {
                                pSrvInform = pLocator.locateInstance(pSrvInitiate, pBind);
                                // set the proxy inform interface
                                setSrvProxyInform(pSrvInform);
                                // set the state to be pend
                                changeState(EE_APIPX_Event.PXSPL_rcvBindInvoke, SLE_AssocState.sleAST_bindPending);
                                // send the bind invoke to the service instance
                                try
                                {
                                    pSrvInform.informOpInvoke(pBind, seqCount);
                                }
                                catch (SleApiException e)
                                {
                                    LOG.log(Level.FINE, "SleApiException ", e);
                                }
                            }
                            catch (SleApiException e)
                            {
                                // the bind diag is set by the locator
                                diag = pBind.getBindDiagnostic();
                                error = true;
                            }
                            finally
                            {
                                this.objMutex.lock();
                            }
                        }
                        else
                        {
                            // query interface fail
                            diag = SLE_BindDiagnostic.sleBD_otherReason;
                            error = true;
                        }
                    }
                }
            }

            if (error)
            {
                EE_APIPX_LocalApplData pLocalApplData = this.database.getLocalApplicationData();
                String tmp = pBind.print(512);
                String mess = EE_MessageRepository.getMessage(1002, diag.toString(), tmp);
                ISLE_SII psii = pBind.getServiceInstanceId();

                if (doAlarm)
                {
                    notify(SLE_LogMessageType.sleLM_alarm, SLE_Alarm.sleAL_authFailure, 1002, mess, psii);
                }
                else
                {
                    if (checkTraceLevel(SLE_TraceLevel.sleTL_high))
                    {
                        this.trace.traceRecord(SLE_TraceLevel.sleTL_high, SLE_Component.sleCP_proxy, psii, mess);
                    }
                }

                // send a bind return operation without credentials
                pBind.setBindDiagnostic(diag);
                pBind.setResponderIdentifier(pLocalApplData.getID());

                // send the unbind return to the network
                clientPostProcessing(pBind, false, false, true);

                if (checkTraceLevel(SLE_TraceLevel.sleTL_low))
                {
                    tmp = poperation.print(512);
                    mess = EE_MessageRepository.getMessage(1013, tmp);
                    this.trace.traceRecord(SLE_TraceLevel.sleTL_low, SLE_Component.sleCP_proxy, psii, mess);
                }

                // don't disconnect the connection in the responder side
                // cleanup
                discardAllInvocationPdu();
                clearAllPendingReturn();
                changeState(EE_APIPX_Event.PXSPL_rcvBindInvoke, SLE_AssocState.sleAST_unbound);
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

        return HRESULT.S_OK;
    }

    /**
     * Reception of a BIND Return from the peer proxy. Not authorized in the
     * responder role : calls the abort methods of the association, and deletes
     * it. S_OK The processing is complete. E_FAIL The processing has failed.
     */
    @Override
    public HRESULT rcvBindReturn(ISLE_Operation poperation)
    {
        if (getState() == SLE_AssocState.sleAST_unbound)
        {
            return HRESULT.E_FAIL;
        }
        else
        {
            doAbort(SLE_PeerAbortDiagnostic.slePAD_protocolError, SLE_AbortOriginator.sleAO_proxy, true);
            changeState(EE_APIPX_Event.PXSPL_rcvBindReturn, SLE_AssocState.sleAST_unbound);
            this.unboundStateIsDisconnected = true;
            releaseAssociation();
            return HRESULT.SLE_E_PROTOCOL;
        }
    }

    /**
     * Receives a UNBIND Invoke pdu from the peer proxy. If the state is bound :
     * - discards all the operation of the sending queue. - forwards the UNBIND
     * operation to the local client. - increments the receiving sequence
     * counter. Otherwise, aborts the association, and deletes it. S_OK The
     * processing is complete. SLE_E_PROTOCOL The operation cannot be accepted
     * in the current state. E_FAIL The processing has failed.
     */
    @Override
    public HRESULT rcvUnbindInvoke(ISLE_Operation poperation)
    {
        if (getState() == SLE_AssocState.sleAST_unbound)
        {
            return HRESULT.E_FAIL;
        }
        else if (getState() == SLE_AssocState.sleAST_bound)
        {
            // discard all operation in the sending queue
            discardAllInvocationPdu();
            // send the unbind invoke to the service instance
            changeState(EE_APIPX_Event.PXSPL_rcvUnbindInvoke, SLE_AssocState.sleAST_remoteUnbindPending);
            long seqCount = incSeqCounter();
            this.objMutex.unlock();
            try
            {
                getSrvProxyInform().informOpInvoke(poperation, seqCount);
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
            doAbort(SLE_PeerAbortDiagnostic.slePAD_protocolError, SLE_AbortOriginator.sleAO_proxy, true);
            changeState(EE_APIPX_Event.PXSPL_rcvUnbindInvoke, SLE_AssocState.sleAST_unbound);
            this.unboundStateIsDisconnected = true;
            releaseAssociation();
            return HRESULT.SLE_E_PROTOCOL;
        }

        return HRESULT.S_OK;
    }

    /**
     * Reception of a UNBIND Return from the peer proxy. Not authorized in the
     * responder role : calls the abort methods of the association, and deletes
     * it. S_OK The processing is complete. E_FAIL The processing has failed.
     */
    @Override
    public HRESULT rcvUnbindReturn(ISLE_Operation poperation)
    {
        if (getState() == SLE_AssocState.sleAST_unbound)
        {
            return HRESULT.E_FAIL;
        }
        else
        {
            doAbort(SLE_PeerAbortDiagnostic.slePAD_protocolError, SLE_AbortOriginator.sleAO_proxy, true);
            changeState(EE_APIPX_Event.PXSPL_rcvUnbindReturn, SLE_AssocState.sleAST_unbound);
            this.unboundStateIsDisconnected = true;
            releaseAssociation();
            return HRESULT.SLE_E_PROTOCOL;
        }
    }

    /**
     * Receives a PEER_ABORT request from the Channel. If the originator is not
     * local : - calls the rcvPeer_Abort of the base class (Association). -
     * calls the releaseAssociation of the base class (Association).
     */
    @Override
    public void rcvPeerAbort(int diagnostic, boolean originatorIsLocal)
    {
        this.objMutex.lock();
        // cleanup
        discardAllInvocationPdu();
        clearAllPendingReturn();
        if (getState() != SLE_AssocState.sleAST_unbound)
        {
            super.rcvPeerAbort(diagnostic, originatorIsLocal);
        }
        changeState(EE_APIPX_Event.PXSPL_rcvPeerAbort, SLE_AssocState.sleAST_unbound);
        this.unboundStateIsDisconnected = true;
        releaseAssociation();
        this.objMutex.unlock();
    }

    /**
     * Receives a PROTOCOL_ABORT request from the Channel : - calls the
     * rcvProtocol_Abort of the base class (Association). - calls the
     * releaseAssociation of the base class (Association) .
     */
    @Override
    public void rcvProtocolAbort(EE_APIPX_ISP1ProtocolAbortDiagnostics diagnostic)
    {
        this.objMutex.lock();
        super.rcvProtocolAbort(diagnostic);
        releaseAssociation();
        this.objMutex.unlock();
    }

    /**
     * Receives a BIND request from the local client. For a responding
     * association, this is a protocol error. Returns the appropriate error
     * code. See specification of ISLE_SrvProxyInitiate.
     */
    @Override
    public HRESULT initiateBindInvoke(ISLE_Operation poperation, boolean reportTransmission)
    {
        return HRESULT.SLE_E_PROTOCOL;
    }

    /**
     * Receives a UNBIND request from the local client. For a responding
     * association, this is a protocol error. Returns the appropriate error
     * code. See specification of ISLE_SrvProxyInitiate.
     */
    @Override
    public HRESULT initiateUnbindInvoke(ISLE_Operation poperation, boolean report)
    {
        return HRESULT.SLE_E_PROTOCOL;
    }

    /**
     * Receives a BIND Return from the local client : - forwards the BIND Return
     * to the peer-proxy. - if the result of the Bind Return is negative,
     * terminates the connection, discards the PDU's and the pending return
     * operations, and deletes the association. See specification of
     * ISLE_SrvProxyInitiate.
     */
    @Override
    public HRESULT initiateBindReturn(ISLE_ConfirmedOperation poperation, boolean report)
    {
        HRESULT res = HRESULT.E_FAIL;

        if (getState() != SLE_AssocState.sleAST_bindPending)
        {
            return HRESULT.SLE_E_PROTOCOL;
        }

        SLE_Result result = poperation.getResult();

        ISLE_Bind pBind = poperation.queryInterface(ISLE_Bind.class);
        if (pBind != null)
        {
            // set the responder idenifier
            EE_APIPX_LocalApplData pLocalApplData = this.database.getLocalApplicationData();
            pBind.setResponderIdentifier(pLocalApplData.getID());
        }

        if (result == SLE_Result.sleRES_positive)
        {
            // set the state to bound
            changeState(EE_APIPX_Event.PXSPL_initiateBindReturn, SLE_AssocState.sleAST_bound);
            // send the bind return to the network
            res = clientPostProcessing(poperation, report, false, false);
        }
        else if (result == SLE_Result.sleRES_negative)
        {
            // send the bind return to the network
            res = clientPostProcessing(poperation, report, false, true);
            changeState(EE_APIPX_Event.PXSPL_initiateBindReturn, SLE_AssocState.sleAST_unbound);
            this.unboundStateIsDisconnected = true;
            // cleanup
            discardAllInvocationPdu();
            clearAllPendingReturn();
            // delete the association
            releaseAssociation();
            return HRESULT.S_OK;
        }

        if (res == HRESULT.E_FAIL || res == HRESULT.SLE_E_COMMS)
        {
            // terminate the connection
            this.objMutex.unlock();
            this.channelInitiate.sendDisconnect();
            this.objMutex.lock();

            changeState(EE_APIPX_Event.PXSPL_initiateBindReturn, SLE_AssocState.sleAST_unbound);
            this.unboundStateIsDisconnected = true;

            // cleanup
            discardAllInvocationPdu();
            clearAllPendingReturn();

            // delete the association
            releaseAssociation();
        }

        return res;
    }

    /**
     * Receives a UNBIND Return from the local client : - forwards the UNBIND
     * Return to the peer-proxy. - terminates the connection, discards the PDU's
     * and the pending return operations, and deletes the association. See
     * specification of ISLE_SrvProxyInitiate.
     */
    @Override
    public HRESULT initiateUnbindReturn(ISLE_ConfirmedOperation poperation, boolean report)
    {
        if (getState() != SLE_AssocState.sleAST_remoteUnbindPending)
        {
            return HRESULT.SLE_E_PROTOCOL;
        }

        // send the unbind return to the network
        HRESULT res = clientPostProcessing(poperation, report, false, true);

        // don't terminate the connection, done by TML
        changeState(EE_APIPX_Event.PXSPL_initiateUnbindReturn, SLE_AssocState.sleAST_unbound);
        this.unboundStateIsDisconnected = true;

        this.objMutex.unlock();
        this.oPSequencer.reset(HRESULT.SLE_E_UNBINDING);
        this.objMutex.lock();

        // cleanup
        discardAllInvocationPdu();
        clearAllPendingReturn();

        // delete the association
        releaseAssociation();

        return res;
    }

    /**
     * Receives a PEER_ABORT request from the local client, or the proxy invokes
     * the PEER-ABORT itself : - calls the initiatePeer_Abort of the base class
     * (Association). - calls the releaseAssociation of the base class
     * (Association). See specification of ISLE_SrvProxyInitiate.
     */
    @Override
    public HRESULT initiatePeerAbort(ISLE_PeerAbort pPeerAbort, boolean report)
    {
        super.initiatePeerAbort(pPeerAbort, report);

        changeState(EE_APIPX_Event.PXSPL_initiatePeerAbort, SLE_AssocState.sleAST_unbound);
        this.unboundStateIsDisconnected = true;
        releaseAssociation();
        return HRESULT.S_OK;
    }

}
