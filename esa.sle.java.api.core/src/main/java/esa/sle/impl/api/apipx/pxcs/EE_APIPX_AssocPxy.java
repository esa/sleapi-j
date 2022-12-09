/**
 * @(#) EE_APIPX_AssocPxy.java
 */

package esa.sle.impl.api.apipx.pxcs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_BindDiagnostic;
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
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_SrvType;
import esa.sle.impl.api.apipx.pxdb.EE_APIPX_SrvTypeList;
import esa.sle.impl.api.apipx.pxdb.SLE_AuthenticationMode;
import esa.sle.impl.api.apipx.pxdel.EE_APIPX_PDUTranslator;
import esa.sle.impl.api.apipx.pxspl.IEE_ChannelInform;
import esa.sle.impl.api.apipx.pxspl.IEE_ChannelInitiate;
import esa.sle.impl.api.apipx.pxtml.EE_APIPX_ISP1ProtocolAbortDiagnostics;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class is the proxy of the class EE_APISE_Association in the communication
 * server process. It forwards encoded PDU's to the association object residing
 * in the SLE application process. For incoming BIND requests, the class is
 * responsible to obtain the service instance identifier. If the identifier is
 * registered, it sends the encoded PDU to the application process, which
 * performs access control and authentication. If the service instance is not
 * registered, access control and authentication is performed (if applicable)
 * before the BIND return PDU can be sent to the initiator. The AssocPxy object
 * creates a thread in order to send a received PDU to the link object. When the
 * AssocPxy receives a PDU (rcvSLE_PDU), it returns immediately. The additional
 * thread has to manage the sending of the PDU on the IPC link through the link
 * object. To be able to synchronize the thread with the link object, a
 * condition variable is needed.
 */
public class EE_APIPX_AssocPxy extends EE_APIPX_LinkAdapter implements IEE_ChannelInform
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_AssocPxy.class.getName());

    private IEE_ChannelInitiate ieeChannelInitiate;

    /**
     * Set when the Channel object has complete.
     */
    private boolean channelComplete;

    /**
     * Indicates if it is a normal close of the IPC link or not.
     */
    public boolean normalStop;

    /**
     * Pointer to the utility factory interface.
     */
    private final ISLE_UtilFactory isleUtilFactory;

    /**
     * Pointer to the database.
     */
    private final EE_APIPX_Database eeAPIPXDatabase;

    private EE_APIPX_Link eeAPIPXLink;

    private final EE_APIPX_PDUTranslator eeAPIPXPDUTranslator;

    private WritingThread writingTh;
    
    private final String instanceId;

    /**
     * Constructor of the class which takes the operation and utility factory as
     * parameter.
     */
    public EE_APIPX_AssocPxy(String instanceKey, 
    						 ISLE_OperationFactory popFactory,
                             ISLE_UtilFactory putilFactory,
                             EE_APIPX_Database pDatabase)
    {
    	this.instanceId = instanceKey;
        this.ieeChannelInitiate = null;
        this.channelComplete = false;
        this.normalStop = false;
        this.isleUtilFactory = putilFactory;
        this.eeAPIPXDatabase = pDatabase;
        this.eeAPIPXPDUTranslator = new EE_APIPX_PDUTranslator(popFactory, putilFactory);
        this.writingTh = null;
    }

    /**
     * The link object calls this function when some data are received on the
     * IPC link and must be performed by the AssocPxy object. When the AssocPxy
     * object receives an encoded PDU from the IPC link, it sends it to the
     * EE_APIPX_Channel thanks to the IEE_ChannelInform interface
     */
    @Override
    public void takeData(byte[] data, int dataType, EE_APIPX_Link pLink, boolean lastPdu)
    {
    	
        if (dataType == PXCS_MessId.mid_NormalStop.getCode())
        {
            this.normalStop = true;
            return;
        }

        if (this.ieeChannelInitiate == null || this.channelComplete)
        {
            return;
        }

        if (dataType == PXCS_MessId.mid_SlePdu.getCode())
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Received SLE PDU to forward to the responding channel, lastPdu=" + lastPdu);
            }
            this.ieeChannelInitiate.sendSLEPDU(data, lastPdu);
            if (lastPdu)
            {
                releaseChannel();
            }
        }
        else if (dataType == PXCS_MessId.mid_PeerAbort.getCode())
        {
            PXCS_AssocChannel_Mess mess = new PXCS_AssocChannel_Mess(data);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(mess.toString());
            }
            this.ieeChannelInitiate.sendPeerAbort(mess.getDiagnostic());
            releaseChannel();
        }
        else if (dataType == PXCS_MessId.mid_Disconnect.getCode())
        {
            this.ieeChannelInitiate.sendDisconnect();
            releaseChannel();
        }
        else if (dataType == PXCS_MessId.mid_Reset.getCode())
        {
            this.ieeChannelInitiate.sendReset();
            releaseChannel();
        }
        else if (dataType == PXCS_MessId.mid_ResumeReceive.getCode())
        {
            this.ieeChannelInitiate.resumeReceive();
        }
        else if (dataType == PXCS_MessId.mid_SuspendReceive.getCode())
        {
            this.ieeChannelInitiate.suspendReceive();
        }
    }

    /**
     * This function is called when the IPC connection is closed by the peer
     * side or lost.
     */
    @Override
    public void ipcClosed(EE_APIPX_Link pLink)
    {
        if (!this.normalStop)
        {
            if (this.ieeChannelInitiate != null)
            {
                SLE_PeerAbortDiagnostic diag = SLE_PeerAbortDiagnostic.slePAD_communicationsFailure;
                this.ieeChannelInitiate.sendPeerAbort(diag.getCode());
            }
        }

        this.linkClosed = true;
        if (this.eeAPIPXLink != null)
        {
            this.eeAPIPXLink = null;
        }

        terminateThread();
    }

    /**
     * Receives an encoded PDU.
     */
    @Override
    public void rcvSLEPDU(byte[] data)
    {
        boolean doSend = false;
        EE_Reference<Boolean> authOk = new EE_Reference<Boolean>();
        authOk.setReference(new Boolean(false));
        SLE_BindDiagnostic diag = SLE_BindDiagnostic.sleBD_invalid;
        ISLE_Operation pOperation = null;
        EE_Reference<Boolean> isInvoke = new EE_Reference<Boolean>();
        isInvoke.setReference(false);
        int dataType = PXCS_MessId.mid_SlePdu.getCode();

        if (data == null)
        {
            return;
        }

        if (this.eeAPIPXLink != null)
        {
            // already received a bind pdu
            // send the pdu on the ipc link
            doSend = true;
        }
        else
        {
            dumpPdu(data);

            // decode the pdu
            try
            {
                pOperation = this.eeAPIPXPDUTranslator.decode(data, SLE_ApplicationIdentifier.sleAI_invalid, isInvoke);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "Invalid operation", e);
                if (e.getHResult() == HRESULT.SLE_E_UNKNOWN)
                {
                    // bad service type
                    diag = SLE_BindDiagnostic.sleBD_serviceTypeNotSupported;
                    // generate a trace
                    ISLE_Trace pIsleTrace = EE_APIPX_ReportTrace.getTraceInterface(this.instanceId);
                    if (pIsleTrace != null)
                    {
                        String error = "Invalid Service Type";
                        String mess = EE_MessageRepository.getMessage(1004,
                                                                      SLE_PeerAbortDiagnostic.slePAD_encodingError
                                                                              .toString(),
                                                                      error);
                        pIsleTrace.traceRecord(SLE_TraceLevel.sleTL_low, SLE_Component.sleCP_proxy, null, mess);
                    }

                    releaseChannel();
                    return;
                }

                if (e.getHResult() == HRESULT.SLE_E_INCONSISTENT)
                {
                    // operation type not supported
                    // generate a trace message
                    ISLE_Trace pIsleTrace = EE_APIPX_ReportTrace.getTraceInterface(this.instanceId);
                    if (pIsleTrace != null)
                    {
                        String error = "Operation Type not supported";
                        String mess = EE_MessageRepository.getMessage(1004,
                                                                      SLE_PeerAbortDiagnostic.slePAD_encodingError
                                                                              .toString(),
                                                                      error);
                        pIsleTrace.traceRecord(SLE_TraceLevel.sleTL_low, SLE_Component.sleCP_proxy, null, mess);
                    }

                    releaseChannel();
                    return;
                }

                if (e.getHResult() != HRESULT.S_OK)
                {
                    // unable to decode the pdu
                    // generate a trace
                    ISLE_Trace pIsleTrace = EE_APIPX_ReportTrace.getTraceInterface(this.instanceId);
                    if (pIsleTrace != null)
                    {
                        String error = "Operation Type not supported";
                        String mess = EE_MessageRepository.getMessage(1004,
                                                                      SLE_PeerAbortDiagnostic.slePAD_encodingError
                                                                              .toString(),
                                                                      error);
                        pIsleTrace.traceRecord(SLE_TraceLevel.sleTL_low, SLE_Component.sleCP_proxy, null, mess);
                    }

                    // close the connection
                    if (this.ieeChannelInitiate != null)
                    {
                        this.ieeChannelInitiate.sendReset();
                    }

                    releaseChannel();
                    return;
                }
            }

            // check the operation type: must be a bind invoke
            if (pOperation.getOperationType() != SLE_OpType.sleOT_bind || !isInvoke.getReference())
            {
                // close the connection
                if (this.ieeChannelInitiate != null)
                {
                    this.ieeChannelInitiate.sendReset();
                }

                releaseChannel();
                return;
            }

            ISLE_Bind pBind = null;
            pBind = pOperation.queryInterface(ISLE_Bind.class);
            if (pBind == null)
            {
                // close the connection
                if (this.ieeChannelInitiate != null)
                {
                    this.ieeChannelInitiate.sendReset();
                }

                releaseChannel();
                return;
            }

            if (processBind(pBind, authOk))
            {
                dataType = PXCS_MessId.mid_BindPdu.getCode();
                doSend = true;
            }
            else
            {
            	diag = pBind.getBindDiagnostic(); // SLEAPIJ-17
                if (!authOk.getReference())
                {
                    // authentication fail
                    this.ieeChannelInitiate.sendReset();
                }
                else
                {
                    if (pBind != null)
                    {
                        sndBindReturn(pBind, diag);
                    }
                }

                releaseChannel();
            }
        }

        if (doSend)
        {
            // send the pdu on the link
            PXCS_Header_Mess header = new PXCS_Header_Mess(false, dataType, data.length);
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(header.toString());
            }

            byte[] headerArray = header.toByteArray();
            byte[] newArray = new byte[headerArray.length + data.length];
            System.arraycopy(headerArray, 0, newArray, 0, headerArray.length);
            System.arraycopy(data, 0, newArray, headerArray.length, data.length);
            sendMessageNoWait(newArray);

            // sendMessageNoWait(header.toByteArray(), data);
        }
    }

    /**
     * Receives a CONNECT request.
     */
    @Override
    public void rcvConnect()
    {
        // nothing in the assocpxy
    }

    /**
     * Receives a PEER_ABORT request.
     */
    @Override
    public void rcvPeerAbort(int diagnostic, boolean originatorIsLocal)
    {
        this.normalStop = true;
        if (this.eeAPIPXLink == null)
        {
            // the first received pdu is not a bind
            if (this.ieeChannelInitiate != null)
            {
                this.ieeChannelInitiate.sendReset();
            }
        }
        else
        {
            // create the Assoc Channel Msg
            PXCS_AssocChannel_Mess acMess = new PXCS_AssocChannel_Mess();
            acMess.setDiagnostic(diagnostic);
            acMess.setOriginatorIsLocal(originatorIsLocal);

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(acMess.toString());
            }

            byte[] acMessByteArray = acMess.toByteArray();

            // create the Header Msg
            PXCS_Header_Mess hMess = new PXCS_Header_Mess(false,
                                                          PXCS_MessId.mid_PeerAbort.getCode(),
                                                          acMessByteArray.length);

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(hMess.toString());
            }

            // create a byte array that contains both
            byte[] data = new byte[acMessByteArray.length + PXCS_Header_Mess.hMsgLength];
            System.arraycopy(hMess.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
            System.arraycopy(acMessByteArray, 0, data, PXCS_Header_Mess.hMsgLength, acMessByteArray.length);

            // send the message
            sendMessageNoWait(data);
            
            this.eeAPIPXLink.setAssocPxy(null); // SLEAPIJ-75
        }
    }

    /**
     * Receives a PROTOCOL_ABORT request.
     */
    @Override
    public void rcvProtocolAbort(EE_APIPX_ISP1ProtocolAbortDiagnostics diagnostic)
    {
        this.normalStop = true;

        if (this.eeAPIPXLink == null)
        {
            // receive a protocol abort before a BIND pdu !!
            // do not report : done by TML
        }
        else
        {
            // create the Assoc Channel Msg
            PXCS_AssocChannel_Mess acMess = new PXCS_AssocChannel_Mess();
            acMess.setPaOriginator(diagnostic);

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(acMess.toString());
            }

            byte[] acMessByteArray = acMess.toByteArray();

            // create the Header Msg
            PXCS_Header_Mess hMess = new PXCS_Header_Mess(false,
                                                          PXCS_MessId.mid_ProtocolAbort.getCode(),
                                                          acMessByteArray.length);

            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest(hMess.toString());
            }

            // create a byte array that contains both
            byte[] data = new byte[acMessByteArray.length + PXCS_Header_Mess.hMsgLength];
            System.arraycopy(hMess.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);
            System.arraycopy(acMessByteArray, 0, data, PXCS_Header_Mess.hMsgLength, acMessByteArray.length);

            // send the message
            sendMessageNoWait(data);
            
            this.eeAPIPXLink.setAssocPxy(null); // SLEAPIJ-18
        }
    }

    /**
     * @Request the suspension of the sending.
     */
    @Override
    public void suspendXmit()
    {
        // create the Assoc Channel Msg
        PXCS_AssocChannel_Mess acMess = new PXCS_AssocChannel_Mess();
        byte[] acMessByteArray = acMess.toByteArray();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(acMess.toString());
        }

        // create the Header Msg
        PXCS_Header_Mess hMess = new PXCS_Header_Mess(false,
                                                      PXCS_MessId.mid_SuspendXmit.getCode(),
                                                      acMessByteArray.length);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(hMess.toString());
        }

        // create a byte array that contains all zeroes
        byte[] data = new byte[acMessByteArray.length + PXCS_Header_Mess.hMsgLength];

        // copy the header
        System.arraycopy(hMess.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);

        // send the message
        sendMessageNoWait(data);
    }

    /**
     * @FunctionRequest a resumption of the sending.@EndFunction
     */
    @Override
    public void resumeXmit()
    {
        // create the Assoc Channel Msg
        PXCS_AssocChannel_Mess acMess = new PXCS_AssocChannel_Mess();
        byte[] acMessByteArray = acMess.toByteArray();
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(acMess.toString());
        }

        // create the Header Msg
        PXCS_Header_Mess hMess = new PXCS_Header_Mess(false,
                                                      PXCS_MessId.mid_ResumeXmit.getCode(),
                                                      acMessByteArray.length);
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest(hMess.toString());
        }

        // create a byte array that contains all zeroes
        byte[] data = new byte[acMessByteArray.length + PXCS_Header_Mess.hMsgLength];

        // copy the header
        System.arraycopy(hMess.toByteArray(), 0, data, 0, PXCS_Header_Mess.hMsgLength);

        // send the message
        sendMessageNoWait(data);
    }

    /**
     * Gets the TraceControl interface of the Channel associated with the
     * AssocPxy.
     */
    public ISLE_TraceControl getChannelTraceControl()
    {
        if (this.ieeChannelInitiate == null)
        {
            return null;
        }

        ISLE_TraceControl pTraceControl = null;
        pTraceControl = this.ieeChannelInitiate.queryInterface(ISLE_TraceControl.class);
        if (pTraceControl != null)
        {
            return pTraceControl;
        }

        return null;
    }

    /**
     * Gets the TraceControl interface of the PDU Translator associated with the
     * AssocPxy.
     */
    public ISLE_TraceControl getTranslatorTraceControl()
    {
        if (this.eeAPIPXPDUTranslator == null)
        {
            return null;
        }

        ISLE_TraceControl pTraceControl = null;
        pTraceControl = this.eeAPIPXPDUTranslator.queryInterface(ISLE_TraceControl.class);
        if (pTraceControl != null)
        {
            return pTraceControl;
        }

        return null;
    }

    /**
     * Set the Channel (through the interface IEE_ChannelInitiate) associated
     * with the AssocPxy object.
     */
    public void setChannelInitiate(IEE_ChannelInitiate pChannelInitiate)
    {
        this.ieeChannelInitiate = pChannelInitiate;
    }

    /**
     * This is the "main  function" of the thread class instance, when it
     * completes, then the thread will terminate. The goal of the thread here is
     * to manage the writing on the link object of a received PDU. This is done
     * in a separate thread in order to be able to respond immediately to the
     * sender. It should be noted that there is no need to pass any objects in
     * here - the class instance itself can contain any reference to data needed
     * by the threadMain function.
     */
    public void threadMain()
    {
        waitForWrite(this.eeAPIPXLink);

        if (this.channelComplete || this.eeAPIPXLink == null || this.eeAPIPXLink.isClosed())
        {
            this.writingTh.terminate();
        }
    }

    /**
     * Indicates if the IPC link is closed.
     */
    public boolean isClosed()
    {
        return this.linkClosed;
    }

    /**
     * Release the Channel interface.
     */
    public void releaseChannel()
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Releasing channel on the association proxy");
        }
        this.channelComplete = true;
        this.ieeChannelInitiate.dispose();
        // release the reference to the link
        if (this.eeAPIPXLink != null)
        {
            // don't set linkClosed otherwise the last pdu is not transmitted
            // over the link !!
            this.eeAPIPXLink.setAssocPxy(null);
            this.eeAPIPXLink = null;
        }

        terminateThread();

        // release the reference from the binder
        EE_APIPX_Binder pBinder = EE_APIPX_Binder.getInstance(this.instanceId);
        if (pBinder != null)
        {
            pBinder.cleanAssoc(this);
        }
    }

    /**
     * Process an incoming BIND PDU: checks the SLE service type, the version
     * number, and checks if the service instance is registered thanks to the
     * Binder. If all the checks are OK, sends the BIND PDU to the application
     * through the IPC link. CodesS_OK The service instance is registered, the
     * BIND PDU can be sent. E_FAIL The service instance is not registered, or
     * an error occurred.
     */
    private boolean processBind(ISLE_Bind bindop, EE_Reference<Boolean> authOk)
    {
        EE_APIPX_Binder pBinder = null;
        EE_APIPX_Link pLink = null;
        ISLE_SII psii = null;
        boolean res = false;
        SLE_BindDiagnostic diag = SLE_BindDiagnostic.sleBD_invalid; // SLEAPIJ-17

        if (bindop == null)
        {
            return false;
        }

        psii = bindop.getServiceInstanceId();
        pBinder = EE_APIPX_Binder.getInstance(this.instanceId);
        pLink = pBinder.getLink(psii);
        if (pBinder == null || psii == null || pLink == null)
        {
            // problem sii not registered
            diag = SLE_BindDiagnostic.sleBD_noSuchServiceInstance;
            bindop.setBindDiagnostic(diag); // SLEAPIJ-17
            res = false;

            // perform control and authentication
            checkBind(bindop, authOk);
        }
        else if (pLink != null && pLink.getAssocPxy() != null)
        {
            // the link is already used by another assocpxy
            // --> must be a second bind for the same sii
            diag = SLE_BindDiagnostic.sleBD_alreadyBound;
            bindop.setBindDiagnostic(diag); // SLEAPIJ-17
            res = false;
        	
            // perform control and authentication
            checkBind(bindop, authOk);
        }
        else
        {        	
            // the service instance is registered --> forward the pdu to the
            // association
            if (this.eeAPIPXLink == null)
            {
                this.eeAPIPXLink = pLink;
                pLink.setAssocPxy(this);

                // create the writing thread after setting of the link
                this.threadRunning = true;
                this.writingTh = new WritingThread();
                this.writingTh.start();
                res = true;
            }
        }

        if (!res)
        {
            if (diag == SLE_BindDiagnostic.sleBD_accessDenied)
            {
                // report the error
                ISLE_Reporter pIsleReporter = EE_APIPX_ReportTrace.getReporterInterface(this.instanceId);
                if (pIsleReporter != null)
                {
                    String tmp = bindop.print(512);
                    String mess = "";
                    if (!authOk.getReference())
                    {
                        mess = EE_MessageRepository.getMessage(1002, SLE_Alarm.sleAL_authFailure.toString(), tmp);
                    }
                    else
                    {
                        mess = EE_MessageRepository.getMessage(1002, diag.toString(), tmp);
                    }
                    pIsleReporter.notify(SLE_Alarm.sleAL_authFailure, SLE_Component.sleCP_proxy, psii, 1002, mess);
                    pIsleReporter
                            .logRecord(SLE_Component.sleCP_proxy, psii, SLE_LogMessageType.sleLM_alarm, 1002, mess);
                }
            }
            else
            {
                // instantiate a trace message indicating that the pdu is not
                // transmitted
                ISLE_Trace pIsleTrace = EE_APIPX_ReportTrace.getTraceInterface(this.instanceId);
                if (pIsleTrace != null)
                {
                    String messOp = bindop.print(512);
                    String mess = "";
                    if (!authOk.getReference())
                    {
                        mess = EE_MessageRepository.getMessage(1002, SLE_Alarm.sleAL_authFailure.toString(), messOp);
                    }
                    else
                    {
                        mess = EE_MessageRepository.getMessage(1002, diag.toString(), messOp);
                    }
                    pIsleTrace.traceRecord(SLE_TraceLevel.sleTL_low, SLE_Component.sleCP_proxy, psii, mess);
                }
            }
        }

        return res;
    }

    /**
     * Checks if the service type and the version number are registered in the
     * database. CodesS_OK The service type and the version number are
     * registered. E_FAIL The service type or the version number is not
     * registered.
     */
    private boolean checkBind(ISLE_Bind bindop, EE_Reference<Boolean> auth_ok)
    {
        SLE_AuthenticationMode authmode = SLE_AuthenticationMode.sleAM_none;
        boolean res = true;
        boolean authOk = true;
        SLE_BindDiagnostic diag = SLE_BindDiagnostic.sleBD_invalid;

        // check if the initiator identifier of the bind invoke is registered in
        // the peer application list
        EE_APIPX_PeerApplDataList pPeerApplDataList = this.eeAPIPXDatabase.getPeerApplDataList();
        EE_APIPX_PeerApplData pPeerApplData = null;
        pPeerApplData = pPeerApplDataList.getPeerApplDataItemByID(bindop.getInitiatorIdentifier());
        if (pPeerApplData == null)
        {
            diag = SLE_BindDiagnostic.sleBD_accessDenied;
            bindop.setBindDiagnostic(diag);
            return false;
        }
        else
        {
            authmode = pPeerApplData.getAuthenticationMode();
        }

        if (authmode == SLE_AuthenticationMode.sleAM_bindOnly || authmode == SLE_AuthenticationMode.sleAM_all)
        {
            ISLE_Credentials pOpCredentials = bindop.getInvokerCredentials();
            ISLE_SecAttributes pIsleSecAttr = null;

            try
            {
                pIsleSecAttr = this.isleUtilFactory.createSecAttributes(ISLE_SecAttributes.class);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);

            }

            if (pOpCredentials == null)
            {
                // no credentials in the op
                diag = SLE_BindDiagnostic.sleBD_accessDenied;
                bindop.setBindDiagnostic(diag);
                authOk = false;
                res = false;
            }
            else if (pIsleSecAttr != null)
            {
                String username = "";
                byte[] passwd = null;

                // get the acceptable delay from the database
                int delay = 0;
                EE_APIPX_ProxySettings pProxySettings = this.eeAPIPXDatabase.getProxySettings();
                delay = pProxySettings.getAuthentAccDelay();

                // take user name and password in the peer application
                username = pPeerApplData.getId();
                passwd = pPeerApplData.getPassword();
                pIsleSecAttr.setUserName(username);
                pIsleSecAttr.setPassword(passwd);

                if (!pIsleSecAttr.authenticate(pOpCredentials, delay, bindop.getOpVersionNumber()))
                {
                    authOk = false;
                    diag = SLE_BindDiagnostic.sleBD_accessDenied;
                    bindop.setBindDiagnostic(diag); // SLEAPIJ-17
                    res = false;
                }
            }
            else
            {
                // cannot create security attributes
                diag = SLE_BindDiagnostic.sleBD_accessDenied;
                bindop.setBindDiagnostic(diag);
                authOk = false;
                res = false;
            }
        }

        if (!res)
        {
            return res;
        }

        // check if the service type is a supported service type in the database
        EE_APIPX_ProxySettings pProxySettings = this.eeAPIPXDatabase.getProxySettings();
        SLE_AppRole sleAppRole = pProxySettings.getRole();
        if (sleAppRole == SLE_AppRole.sleAR_user)
        {
            return true;
        }

        EE_APIPX_SrvType pSrvType = null;
        EE_APIPX_SrvTypeList pSrvTypeList = null;
        pSrvTypeList = this.eeAPIPXDatabase.getSrvTypeList();
        pSrvType = pSrvTypeList.getSrvTypeByType(bindop.getServiceType());
        if (pSrvType == null)
        {
            diag = SLE_BindDiagnostic.sleBD_serviceTypeNotSupported;
            return false;
        }

        // check if the version number match one entry in the database
        int indexMax = pSrvType.getNumVersions();
        int versionNumberBind = bindop.getOpVersionNumber();
        boolean found = false;
        for (int index = 0; index < indexMax; index++)
        {
            int versionNumber = pSrvType.getVersion(index);
            if (versionNumber == versionNumberBind)
            {
                found = true;
                break;
            }
            else if (versionNumber > versionNumberBind)
            {
                break;
            }
        }

        if (!found)
        {
            diag = SLE_BindDiagnostic.sleBD_versionNotSupported;
            return false;
        }

        auth_ok.setReference(authOk);
        return true;
    }

    /**
     * Sends a negative bind return operation.
     */
    private void sndBindReturn(ISLE_Bind pBind, SLE_BindDiagnostic diag)
    {
        byte[] bindReturnPdu = null;
        EE_APIPX_LocalApplData pLocalApplData = this.eeAPIPXDatabase.getLocalApplicationData();

        // fill and send a bind return operation
        pBind.setBindDiagnostic(diag);
        pBind.setResponderIdentifier(pLocalApplData.getID());

        // set the credentials
        if (diag != SLE_BindDiagnostic.sleBD_accessDenied)
        {
            insertSecAttr(pBind);
        }

        if (this.ieeChannelInitiate != null)
        {
            // instantiate a trace message
            ISLE_Trace pIsleTrace = EE_APIPX_ReportTrace.getTraceInterface(this.instanceId);
            if (pIsleTrace != null)
            {
                ISLE_SII psii = pBind.getServiceInstanceId();
                String messop = pBind.print(512);
                String mess = EE_MessageRepository.getMessage(1013, messop);
                pIsleTrace.traceRecord(SLE_TraceLevel.sleTL_low, SLE_Component.sleCP_proxy, psii, mess);
            }

            // encode and send the pdu immediately
            try
            {
                bindReturnPdu = this.eeAPIPXPDUTranslator.encode(pBind, false);
                dumpPdu(bindReturnPdu);
                this.ieeChannelInitiate.sendSLEPDU(bindReturnPdu, true);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
            }
            catch (IOException e)
            {
                LOG.log(Level.FINE, "IOException ", e);
            }
        }
    }

    /**
     * Inserts security attributes in a negative bind return operation.
     */
    private void insertSecAttr(ISLE_Bind pBind)
    {
        ISLE_SecAttributes pIsleSecAtt = null;
        SLE_AuthenticationMode authenticationMode = SLE_AuthenticationMode.sleAM_none;
        String username = "";
        byte[] passwd = null;
        String peerId = pBind.getInitiatorIdentifier();
        pBind.putPerformerCredentials(null);

        if (peerId == null)
        {
            return;
        }

        // set the authentication mode : always taken in the peer-application
        EE_APIPX_PeerApplDataList pPeerApplDataList = this.eeAPIPXDatabase.getPeerApplDataList();
        EE_APIPX_PeerApplData pPeerAppl = pPeerApplDataList.getPeerApplDataItemByID(peerId);
        if (pPeerAppl != null)
        {
            authenticationMode = pPeerAppl.getAuthenticationMode();
        }

        if (authenticationMode != SLE_AuthenticationMode.sleAM_none)
        {
            try
            {
                pIsleSecAtt = this.isleUtilFactory.createSecAttributes(ISLE_SecAttributes.class);
                if (pIsleSecAtt != null)
                {
                    // set the user name and password : the local one to
                    // calculate
                    // performer credentials
                    EE_APIPX_LocalApplData pLocalApplData = this.eeAPIPXDatabase.getLocalApplicationData();
                    username = pLocalApplData.getID();
                    passwd = pLocalApplData.getPassword();
                    pIsleSecAtt.setUserName(username);
                    pIsleSecAtt.setPassword(passwd);
                    ISLE_Credentials pCredentials = pIsleSecAtt.generateCredentials(pBind.getOpVersionNumber());
                    pBind.putPerformerCredentials(pCredentials);
                }
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
            }
        }
    }

    /**
     * This function is called to terminate the thread which waits for writing
     * message on the IPC link. It signals the condition variables, and waits
     * for the deletion of the thread (join).
     */
    private void terminateThread()
    {
        signalResponseReceived();

        // close the writing thread
        try
        {
        	this.writingTh.terminate();
        } catch(Exception e) {} // seems to happen when HB timeout occurs
    }

    /**
     * Dump the content of the PDU by performing a trace.
     */
    private void dumpPdu(byte[] pdu)
    {
        ISLE_Trace pIsleTrace = EE_APIPX_ReportTrace.getTraceInterface(this.instanceId);
        if (pIsleTrace != null)
        {
            EE_APIPX_ProxySettings pProxySettings = this.eeAPIPXDatabase.getProxySettings();
            int maxLg = pProxySettings.getMaxTraceLength();
            if (pdu.length < maxLg)
            {
                maxLg = pdu.length;
            }

            String mess = EE_GenStrUtil.convAscii(pdu, maxLg);
            pIsleTrace.traceRecord(SLE_TraceLevel.sleTL_full, SLE_Component.sleCP_proxy, null, mess);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IEE_ChannelInform.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }


    private class WritingThread extends Thread
    {
        private boolean isRunning = true;

        public WritingThread()
        {
        	super("SLE EE_APIPX_AssocPxy.WritingThread");
        }
        
        @Override
        public void run()
        {
            while (this.isRunning)
            {
                threadMain();
            }
        }

        public void terminate()
        {
            this.isRunning = false;
        }
    }
}
