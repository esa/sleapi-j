package esa.sle.impl.tst.systst;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIOpFactory;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Diagnostics;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_SlduStatusNotification;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.types.CLTU_ParameterName;
import ccsds.sle.api.isrv.ifsp.IFSP_AsyncNotify;
import ccsds.sle.api.isrv.ifsp.IFSP_GetParameter;
import ccsds.sle.api.isrv.ifsp.IFSP_InvokeDirective;
import ccsds.sle.api.isrv.ifsp.IFSP_Start;
import ccsds.sle.api.isrv.ifsp.IFSP_ThrowEvent;
import ccsds.sle.api.isrv.ifsp.IFSP_TransferData;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_Directive;
import ccsds.sle.api.isrv.ifsp.types.FSP_DirectiveTimeoutType;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopAlert;
import ccsds.sle.api.isrv.ifsp.types.FSP_InvokeDirectiveDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_NotificationType;
import ccsds.sle.api.isrv.ifsp.types.FSP_PacketStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_ParameterName;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_StartDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_ThrowEventDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransferDataDiagnostic;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransmissionMode;
import esa.sle.impl.eapi.dcw.IDCW_EventQueue;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_SYSTST_FSPOpGen extends EE_SYSTST_OpGen
{
    private static final Logger LOG = Logger.getLogger(EE_SYSTST_FSPOpGen.class.getName());


    public EE_SYSTST_FSPOpGen(ISLE_OperationFactory opf, ISLE_SIOpFactory f, boolean playback, UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, opf, f, playback, utl);
    }

    @Override
    public ISLE_Operation createOp(SLE_OpType opt, IDCW_EventQueue eventQueue, IUnknown si)
    {
        ISLE_Operation op = super.createOp(opt, eventQueue, si);
        if (op != null)
        {
            String op_s = op.print(40);
            System.out.print(op_s);
            EE_Reference<String> yn = new EE_Reference<String>();
            System.out.print("\nSet-up operation object (y/n): ");
            utl.read(yn, this.playback);
            if (yn.getReference().equals("y"))
            {
                System.out.println();
                setUpOperation(op, eventQueue, si);
            }
        }
        return op;
    }

    @Override
    public void setUpOperation(ISLE_Operation op, IDCW_EventQueue eventQueue, IUnknown si)
    {
        SLE_OpType opt = op.getOperationType();

        if (opt == SLE_OpType.sleOT_bind || opt == SLE_OpType.sleOT_unbind || opt == SLE_OpType.sleOT_stop
            || opt == SLE_OpType.sleOT_scheduleStatusReport || opt == SLE_OpType.sleOT_peerAbort)
        {
            super.setUpOperation(op, eventQueue, si);
        }
        else if (opt == SLE_OpType.sleOT_getParameter)
        {
            setUpGetParameter(op, this.playback, utl);
        }
        else if (opt == SLE_OpType.sleOT_asyncNotify)
        {
            setUpAsyncNotify(op, this.playback, utl);
        }
        else if (opt == SLE_OpType.sleOT_throwEvent)
        {
            setUpThrowEvent(op, this.playback, utl);
        }
        else if (opt == SLE_OpType.sleOT_transferData)
        {
            setUpTransferData(op, this.playback, utl);
        }
        else if (opt == SLE_OpType.sleOT_start)
        {
            setUpStart(op, this.playback, utl);
        }
        else if (opt == SLE_OpType.sleOT_invokeDirective)
        {
            setUpInvokeDirective(op, this.playback, utl);
        }
    }

    static void setUpAsyncNotify(ISLE_Operation op, boolean playback, UTL utl)
    {

        IFSP_AsyncNotify an = (IFSP_AsyncNotify) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");
        EE_Reference<String> arg = new EE_Reference<String>();
        boolean firstTime = true;

        while (what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("nt     set-up notification type");
                System.out.println("eiid   set-up event thrown id");
                System.out.println("deid   set-up directive execute id");
                System.out.println("fopa   set-up FOP alert");
                System.out.println("pidl   set-up Packet Id list");
                System.out.println("idlp   set-up FSP id - last processed");
                System.out.println("idok   set-up FSP id - last Ok");
                System.out.println("pst    set-up production start time");
                System.out.println("pet    set-up production end time");
                System.out.println("pks    set-up packet status");
                System.out.println("ps     set-up production status");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("nt"))
            {
                System.out.println("Notification type: ");
                System.out.println("0=packetProcessingStarted, 1=packetRadiated, 2=packetAcknowledged, 3=slduExpired,");
                System.out
                        .println("4=packetTransmissionModeMismatch, 5=transmissionModeCapabilityChange, 6=bufferEmpty,");
                System.out.println("7=noInvokeDirectiveCapabilityOnThisVc, 8=posConfRespDir, 9=negConfRespDir");
                System.out
                        .println("10=vcAborted, 11=productionInterrupted, 12=productionHalted, 13=productionOperational,");
                System.out
                        .println("14=actionListCompl, 15=actionListNotCompl, 16=eventCondEvFalse, 17=invDirCapabOnThisVC): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_AsyncNotify.setNotificationType", FSP_NotificationType.getNotificationTypeByCode(n)
                        .toString());
                an.setNotificationType(FSP_NotificationType.getNotificationTypeByCode(n));
            }
            else if (what.getReference().equals("eiid"))
            {
                System.out.print("Event invocation id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_AsyncNotify.setEventThrownId", Integer.toString(n));
                an.setEventThrownId(n);
            }

            else if (what.getReference().equals("deid"))
            {
                System.out
                        .print("DirExecuted:(0=initADwithoutCLCW, 1=initADwithCLCW, 2=initADwithUnlock, 3=initADwithSetVR,\n"
                               + "             4=terminateAD, 5=resumeAD, 6=setVS, 7=setFopSlidingWindow,\n"
                               + "             8=setT1Initial, 9=setTransmissionLimit, 10=setTimeoutType, 11=abortVC,\n"
                               + "             12=modifyMapPollingVector, 13=modifyMapPriorityList): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_AsyncNotify.setDirectiveExecutedId", Integer.toString(n));
                an.setDirectiveExecutedId(n);
            }

            else if (what.getReference().equals("fopa"))
            {
                System.out.print("FOP alert: (0=noAlert, 1=limit, 2=lockOutDetected, 3=synch, 4=invalidNR,\n"
                                 + "           (5=Clcw, 6=lowerLayerOutOfSync, 7=terminateAD): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_AsyncNotify.setFopAlert", Integer.toString(n));
                an.setFopAlert(FSP_FopAlert.getFopAlertByCode(n));
            }

            else if (what.getReference().equals("pidl"))
            {
                long[] pidl = utl.readIntList("Packet Id list (comma-sep, no spaces, 'NULL' for empty list): ",
                                              playback);
                UTL.traceIF1("IFSP_AsyncNotify.setPacketIdentificationList", Long.toString(pidl[0]));
                an.setPacketIdentificationList(pidl);
            }

            else if (what.getReference().equals("idlp"))
            {
                System.out.print("Packet Id - last processed: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_AsyncNotify.setPacketLastProcessed", Integer.toString(n));
                an.setPacketLastProcessed(n);
            }

            else if (what.getReference().equals("idok"))
            {
                System.out.print("Packet Id - last ok: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_AsyncNotify.setPacketLastOk", Integer.toString(n));
                an.setPacketLastOk(n);
            }

            else if (what.getReference().equals("pst"))
            {
                ISLE_Time st_p = utl.readTime("Production start time: ", playback);
                UTL.traceIF0("IFSP_AsyncNotify.setProductionStartTime");
                an.setProductionStartTime(st_p);
            }

            else if (what.getReference().equals("pet"))
            {
                ISLE_Time st_p = utl.readTime("Production end time: ", playback);
                UTL.traceIF0("IFSP_AsyncNotify.setProductionStopTime");
                an.setProductionStopTime(st_p);
            }

            else if (what.getReference().equals("pks"))
            {
                System.out.println("Packet Status: ");
                System.out.println("0=radiated, 1=expired, 2=interrupted, 3=acknowledged, 4=productionStarted,");
                System.out.print("5=productionNotStarted, 6=unsupportedTransmissionMode, -1=invalid: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_AsyncNotify::setPacketStatus", FSP_PacketStatus.getPacketStatusByCode(n).toString());
                an.setPacketStatus(FSP_PacketStatus.getPacketStatusByCode(n));
            }

            else if (what.getReference().equals("ps"))
            {
                System.out.println("Production Status: ");
                System.out.println("0=operational, 1=configured, 2=interrupted, ");
                System.out.print("3=halted,     -1=invalid : ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_AsyncNotify.setProductionStatus", FSP_ProductionStatus.getProductionStatusByCode(n)
                        .toString());
                an.setProductionStatus(FSP_ProductionStatus.getProductionStatusByCode(n));
            }

        }
    }

    static void setUpThrowEvent(ISLE_Operation op, boolean playback, UTL utl)
    {

        IFSP_ThrowEvent te = (IFSP_ThrowEvent) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");
        EE_Reference<String> arg = new EE_Reference<String>();
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("id     set-up event identfier");
                System.out.println("eid    set-up event invocation identfier");
                System.out.println("eq     set-up event qualifier");
                System.out.println("eeid   set-up next expected event invocation identfier (return PDU)");
                System.out.println("sd     set-up diagnostic");
                System.out.println("sp     set-up positive result");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("id"))
            {
                System.out.print("Event Id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_ThrowEvent.setEventId", Integer.toString(n));
                te.setEventId(n);
            }

            else if (what.getReference().equals("sp"))
            {
                te.setPositiveResult();
            }

            else if (what.getReference().equals("eid"))
            {
                System.out.print("Event invocation Id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_ThrowEvent::setEventInvocationId", Integer.toString(n));
                te.setEventInvocationId(n);
            }

            else if (what.getReference().equals("eq"))
            {
                System.out.print("Event qualifier length: ");
                utl.read(arg, playback);
                int lg = Integer.parseInt(arg.getReference());
                final String tmp = "0123456789ABCDEF";
                int lg1 = tmp.length();
                byte[] data = new byte[lg];

                for (int i = 0; i < lg; i++)
                {
                    data[i] = 'Q';
                }
                for (int i = 0; i < lg; i++)
                {
                    data[i] = (byte) tmp.charAt(i % lg1);
                }
                te.setEventQualifier(data);
            }

            else if (what.getReference().equals("eeid"))
            {
                System.out.print("Next expected event invocation Id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_ThrowEvent.setExpectedEventInvocationId", Integer.toString(n));
                te.setExpectedEventInvocationId(n);
            }
            else if (what.getReference().equals("sd"))
            {
                System.out.println("ThrowEvent Diagnostic: ");
                System.out.print("(0=operation not supported, 1=out of sequence, 2=no such event):");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_ThrowEvent.setThrowEventDiagnostic", FSP_ThrowEventDiagnostic
                        .getThrowEventDiagnosticByCode(n).toString());
                te.setThrowEventDiagnostic(FSP_ThrowEventDiagnostic.getThrowEventDiagnosticByCode(n));
            }
        }
    }

    static void setUpGetParameter(ISLE_Operation op, boolean playback, UTL utl)
    {

        IFSP_GetParameter gp = (IFSP_GetParameter) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");

        FSP_ParameterName pn = FSP_ParameterName.fspPN_invalid;
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("Select Parameter Name: ");
                System.out.println("al    get apidList");
                System.out.println("btp   get blockingTimeoutPeriod");
                System.out.println("bu    get blockingUsage");               
                System.out.println("blr   get bitLockRequired");
                System.out.println("cgv   get clcwGlobalVcid ");			// new since SLES v5
                System.out.println("cpc   get clcwPhysicalChannel");		// new since SLES v5
                System.out.println("ccfr  get copControlFramesRepetition");	// new since SLES v5
                System.out.println("dm    get deliveryMode");
                System.out.println("die   get directiveInvocationEnabled");
                System.out.println("edi   get expectedDirectiveId");
                System.out.println("eei   get expectedEventInvocationId");
                System.out.println("esi   get expectedSlduIdentification");
                System.out.println("fsw   get fopSlidingWindow");
                System.out.println("fs    get fopState");
                System.out.println("ml    get mapList");
                System.out.println("mmc   get mapMuxControl");
                System.out.println("mms   get mapMuxScheme");
                System.out.println("mfl   get maximumFrameLength");
                System.out.println("mpl   get maximumPacketLength");
                System.out.println("mrc   get minimumReportingCycle");		// new since SLES v5
                System.out.println("ptm   get permittedTransmissionMode");
                System.out.println("rc    get reportingCycle");
                System.out.println("rtp   get returnTimeoutPeriod");
                System.out.println("rfar  get rfAvailableRequired");
                System.out.println("sh    get segmentHeader");
                System.out.println("scfr  get seqControlFramesRepetition"); // new since SLES v5
                System.out.println("teoe  get throwEventOperationEnabled");	// new since SLES v5
                System.out.println("tt    get timeoutType");
                System.out.println("ti    get timerInitial");
                System.out.println("tl    get transmissionLimit");
                System.out.println("tfsn  get transmitterFrameSequenceNumber");
                System.out.println("vmc   get vcMuxControl");
                System.out.println("vms   get vcMuxScheme");
                System.out.println("vc    get virtualChannel");
                System.out.println("dio   get directiveInvocationOnline");
                System.out.println("ok    set-up completed");
                System.out.println("-1    (invalid parameter name)");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("btp"))
            {
                pn = FSP_ParameterName.fspPN_blockingTimeoutPeriod;
            }
            else if (what.getReference().equals("bu"))
            {
                pn = FSP_ParameterName.fspPN_blockingUsage;
            }
            else if (what.getReference().equals("al"))
            {
                pn = FSP_ParameterName.fspPN_apidList;
            }
            else if (what.getReference().equals("blr"))
            {
                pn = FSP_ParameterName.fspPN_bitLockRequired;
            }
            else if (what.getReference().equals("dm"))
            {
                pn = FSP_ParameterName.fspPN_deliveryMode;
            }
            else if (what.getReference().equals("die"))
            {
                pn = FSP_ParameterName.fspPN_directiveInvocationEnabled;
            }
            else if (what.getReference().equals("edi"))
            {
                pn = FSP_ParameterName.fspPN_expectedDirectiveId;
            }
            else if (what.getReference().equals("eei"))
            {
                pn = FSP_ParameterName.fspPN_expectedEventInvocationId;
            }
            else if (what.getReference().equals("esi"))
            {
                pn = FSP_ParameterName.fspPN_expectedSlduIdentification;
            }
            else if (what.getReference().equals("fsw"))
            {
                pn = FSP_ParameterName.fspPN_fopSlidingWindow;
            }
            else if (what.getReference().equals("fs"))
            {
                pn = FSP_ParameterName.fspPN_fopState;
            }
            else if (what.getReference().equals("ml"))
            {
                pn = FSP_ParameterName.fspPN_mapList;
            }
            else if (what.getReference().equals("mmc"))
            {
                pn = FSP_ParameterName.fspPN_mapMuxControl;
            }
            else if (what.getReference().equals("mms"))
            {
                pn = FSP_ParameterName.fspPN_mapMuxScheme;
            }
            else if (what.getReference().equals("mfl"))
            {
                pn = FSP_ParameterName.fspPN_maximumFrameLength;
            }
            else if (what.getReference().equals("mpl"))
            {
                pn = FSP_ParameterName.fspPN_maximumPacketLength;
            }
            else if (what.getReference().equals("ptm"))
            {
                pn = FSP_ParameterName.fspPN_permittedTransmissionMode;
            }
            else if (what.getReference().equals("rc"))
            {
                pn = FSP_ParameterName.fspPN_reportingCycle;
            }
            else if (what.getReference().equals("rtp"))
            {
                pn = FSP_ParameterName.fspPN_returnTimeoutPeriod;
            }
            else if (what.getReference().equals("rfar"))
            {
                pn = FSP_ParameterName.fspPN_rfAvailableRequired;
            }
            else if (what.getReference().equals("sh"))
            {
                pn = FSP_ParameterName.fspPN_segmentHeader;
            }
            else if (what.getReference().equals("tt"))
            {
                pn = FSP_ParameterName.fspPN_timeoutType;
            }
            else if (what.getReference().equals("ti"))
            {
                pn = FSP_ParameterName.fspPN_timerInitial;
            }
            else if (what.getReference().equals("tl"))
            {
                pn = FSP_ParameterName.fspPN_transmissionLimit;
            }
            else if (what.getReference().equals("tfsn"))
            {
                pn = FSP_ParameterName.fspPN_transmitterFrameSequenceNumber;
            }
            else if (what.getReference().equals("vmc"))
            {
                pn = FSP_ParameterName.fspPN_vcMuxControl;
            }
            else if (what.getReference().equals("vms"))
            {
                pn = FSP_ParameterName.fspPN_vcMuxScheme;
            }
            else if (what.getReference().equals("vc"))
            {
                pn = FSP_ParameterName.fspPN_virtualChannel;
            }
            else if (what.getReference().equals("dio"))
            {
                pn = FSP_ParameterName.fspPN_directiveInvocationOnline;
            }
            // New with SLE V5
            else if (what.getReference().equals("cgv"))
            {
                pn = FSP_ParameterName.fspPN_clcwGlobalVcId;
            }
            else if (what.getReference().equals("cpc"))
            {
                pn = FSP_ParameterName.fspPN_clcwPhysicalChannel;
            }
            else if (what.getReference().equals("ccfr"))
            {
                pn = FSP_ParameterName.fspPN_copCntrFramesRepetion;
            }
            else if (what.getReference().equals("mrc"))
            {
                pn = FSP_ParameterName.fspPN_minReportingCycle;
            }
            else if (what.getReference().equals("scfr"))
            {
                pn = FSP_ParameterName.fspPN_seqCntrFramesRepetition;
            }
            else if (what.getReference().equals("teoe"))
            {
                pn = FSP_ParameterName.fspPN_throwEventOperation;
            }
        }

        if (what.getReference().equals("ill"))
        {
            pn = FSP_ParameterName.getParameterNameByCode(40);
        }
        UTL.traceIF1("IFSP_GetParameter::setRequestedParameter", pn.toString());
        gp.setRequestedParameter(pn);

    }

    static void setUpTransferData(ISLE_Operation op, boolean playback, UTL utl)
    {
        IFSP_TransferData td = (IFSP_TransferData) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");
        EE_Reference<String> arg = new EE_Reference<String>();
        boolean with_data = false;
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("ept    set-up earliest production time (optional)");
                System.out.println("lpt    set-up latest   production time (optional)");
                System.out.println("dt     set-up delay time (optional) ");
                System.out.println("tm     set-up transmission mode");
                System.out.println("rn     set-up radiation notification");
                System.out.println("an     set-up acknowledged notification");
                System.out.println("psn    set-up processing started notification");
                System.out.println("bl     set-up blocking");
                System.out.println("mid    set-up MAP id");
                System.out.println("id     set-up Packet id for the FSP to be transferred");
                System.out.println("eid    set-up next expected Packet id (for return-PDU)");
                System.out.println("sp     set-up positive result");
                System.out.println("sd     set-up diagnostic");
                System.out.println("dl     set-up requ data length");
                System.out.println("nd     set-up no data");
                System.out.println("pba    set-up packet buffer available");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("rn"))
            {
                System.out.println("Radiated Notification: ");
                System.out.print("(0=produce notif., 1=do not produce notif., -1=invalid): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_TransferData.setRadiatedNotification", SLE_SlduStatusNotification
                        .getSlduStatusNotificationByCode(n).toString());
                td.setRadiatedNotification(SLE_SlduStatusNotification.getSlduStatusNotificationByCode(n));
            }

            else if (what.getReference().equals("an"))
            {
                System.out.println("Acknowledged Notification: ");
                System.out.print("(0=produce notif., 1=do not produce notif., -1=invalid): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_TransferData.setAcknowledgedNotification", SLE_SlduStatusNotification
                        .getSlduStatusNotificationByCode(n).toString());
                td.setAcknowledgedNotification(SLE_SlduStatusNotification.getSlduStatusNotificationByCode(n));
            }

            else if (what.getReference().equals("psn"))
            {
                System.out.println("Processing Started Notification: ");
                System.out.print("(0=produce notif., 1=do not produce notif., -1=invalid): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_TransferData.setProcessingStartedNotification", SLE_SlduStatusNotification
                        .getSlduStatusNotificationByCode(n).toString());
                td.setProcessingStartedNotification(SLE_SlduStatusNotification.getSlduStatusNotificationByCode(n));
            }
            else if (what.getReference().equals("bl"))
            {
                SLE_YesNo yn = SLE_YesNo.getYesNoByBool(utl.readYn("Set Blocking (Y/N): ", playback));
                UTL.traceIF1("IFSP_TransferData.setBlocking", yn.toString());
                td.setBlocking(yn);
            }
            else if (what.getReference().equals("mid"))
            {
                System.out.print("MAP Id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_TransferData.setMapId", Integer.toString(n));
                td.setMapId(n);
            }

            else if (what.getReference().equals("id"))
            {
                System.out.print("Packet Id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_TransferData.setPacketId", Integer.toString(n));
                td.setPacketId(n);
            }

            else if (what.getReference().equals("dt"))
            {
                System.out.println("Delay Time (millisec): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_TransferData.setDelayTime", Integer.toString(n));
                td.setDelayTime(n);
            }

            else if (what.getReference().equals("tm"))
            {
                System.out.println("Transmission Mode: ");
                System.out
                        .print("(0=sequence controlled (AD), 1=expedited (BD), 2=sequ. contr. unblock (unblock AD), -1=invalid): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_TransferData.setTransmissionMode", Integer.toString(n));
                td.setTransmissionMode(FSP_TransmissionMode.getTransmissionModeByCode(n));
            }

            else if (what.getReference().equals("eid"))
            {
                System.out.print("Next Expected Packet Id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_TransferData.setExpectedPacketId", Integer.toString(n));
                td.setExpectedPacketId(n);
            }

            else if (what.getReference().equals("ept"))
            {
                ISLE_Time st_p = utl.readTime("Earliest production time: ", playback);
                UTL.traceIF0("IFSP_TransferData.setEarliestProdTime");
                if (LOG.isLoggable(Level.FINEST))
                {
                    LOG.finest("st_p " + st_p);
                }
                td.setEarliestProdTime(st_p);
            }

            else if (what.getReference().equals("lpt"))
            {
                ISLE_Time st_p = utl.readTime("Latest production time: ", playback);
                UTL.traceIF0("IFSP_TransferData.setLatestProdTime");
                td.setLatestProdTime(st_p);
            }
            else if (what.getReference().equals("sd"))
            {
                System.out.println("Transfer Data Diagnostic: ");
                System.out.print("(0=unableToProcess, 1=unableToStore, 2=packetIDoutOfSequence,\n");
                System.out.print(" 3=duplicatePacketID, 4=inconsistentTimeRange, 5=invalidTime,\n");
                System.out.print(" 6=conflictingProductionTimeIntervals, 7=lateSLDU, 8=invalidDelayTime,\n");
                System.out.print(" 9=invalidTransmissionMode, 10=invalidMap, 11=invalidNotificationRequest,\n");
                System.out.print(" 12=packetTooLong, 13=unsupportedPacketVersion, 14=incorrectPacketType,\n");
                System.out.print(" 15=invalidPacketAPID): ");

                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_TransferData.setTransferDataDiagnostic", FSP_TransferDataDiagnostic
                        .getTransferDataDiagnosticByCode(n).toString());
                td.setTransferDataDiagnostic(FSP_TransferDataDiagnostic.getTransferDataDiagnosticByCode(n));
            }
            else if (what.getReference().equals("sp"))
            {
                td.setPositiveResult();
            }
            else if (what.getReference().equals("dl"))
            {
                System.out.print("Data Length : ");
                utl.read(arg, playback);
                int lg = Integer.parseInt(arg.getReference());
                final String tmp = "0123456789ABCDEF";
                int lg1 = tmp.length();
                byte[] data = new byte[lg];

                for (int i = 0; i < lg; i++)
                {
                    data[i] = (byte) tmp.charAt(i % lg1);
                }
                UTL.traceIF1("IFSP_TransferData.setData", Integer.toString(lg));
                td.setData(data);
                with_data = true;
            }
            else if (what.getReference().equals("nd"))
            {
                with_data = true;
            }
            else if (what.getReference().equals("pba"))
            {
                System.out.print("Buffer Size : ");
                utl.read(arg, playback);
                int lg = Integer.parseInt(arg.getReference());
                td.setPacketBufferAvailable(lg);
            }
        }

        if (with_data == false)
        {
            byte[] data = new byte[7];
            data[0] = '1';
            data[1] = '2';
            data[2] = '3';
            data[3] = '4';
            data[4] = '5';
            data[5] = '6';
            data[6] = '7';
            UTL.traceIF1("IFSP_TransferData.setData", Integer.toString(7));
            td.setData(data);
        }

    }

    static void setUpStart(ISLE_Operation op, boolean playback, UTL utl)
    {
        IFSP_Start st = (IFSP_Start) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");

        EE_Reference<String> arg = new EE_Reference<String>();
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("id     set-up first FSP id the provider shall accept");
                System.out.println("spd    set-up start production time (for return-PDU)");
                System.out.println("epd    set-up stop  production time (for return-PDU)");
                System.out.println("sn     set-up negative result with common diag");
                System.out.println("sns    set-up negative result with start diag");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("id"))
            {
                System.out.print("Packet Id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_Start.setFirstPacketId", Integer.toString(n));
                st.setFirstPacketId(n);
            }
            else if (what.getReference().equals("sn"))
            {
                System.out.println("Diagnostic: ");
                System.out.print("(100=duplicateInvokeId, 127=other): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                st.setDiagnostics(SLE_Diagnostics.getDiagnosticsByCode(n));
            }
            else if (what.getReference().equals("sns"))
            {
                System.out.println("Diagnostic: ");
                System.out.print("(0=outOfService, 1=unableToComply, 2=productionTimeExpired, 3=invalidFspId): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("IFSP_Start.setStartDiagnostic", FSP_StartDiagnostic.getStartDiagnosticByCode(n)
                        .toString());
                st.setStartDiagnostic(FSP_StartDiagnostic.getStartDiagnosticByCode(n));
            }
            else if (what.getReference().equals("spd"))
            {
                ISLE_Time st_p = utl.readTime("Start production time: ", playback);
                UTL.traceIF0("IFSP_Start.setStartProductionTime");
                st.setStartProductionTime(st_p);
            }

            else if (what.getReference().equals("epd"))
            {
                ISLE_Time st_p = utl.readTime("Stop production time: ", playback);
                UTL.traceIF0("IFSP_Start.setStopProductionTime");
                st.setStopProductionTime(st_p);
            }

        }

    }

    static void setUpInvokeDirective(ISLE_Operation op, boolean playback, UTL utl)
    {

        IFSP_InvokeDirective id = (IFSP_InvokeDirective) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("dir    set-up directive type");
                System.out.println("id     set-up directive id");
                System.out.println("eid    set-up expected directive id");
                System.out.println("diag   set-up diagnostic");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("id"))
            {
                long dirId = utl.readInt("Directive Id: ", playback);
                UTL.traceIF1("IFSP_InvokeDirective.setDirectiveId", Long.toString(dirId));
                id.setDirectiveId(dirId);
            }
            else if (what.getReference().equals("eid"))
            {
                long dirId = utl.readInt("Expected Directive Id: ", playback);
                UTL.traceIF1("IFSP_InvokeDirective.setExpectedDirectiveId", Long.toString(dirId));
                id.setExpectedDirectiveId(dirId);
            }
            else if (what.getReference().equals("diag"))
            {
                FSP_InvokeDirectiveDiagnostic diag = (FSP_InvokeDirectiveDiagnostic
                        .getInvokeDirectiveDiagnosticByCode(utl
                                .readInt("Dir, Diagnostic (0:InvocationNotAllowed, 1:IdOutOfSequence, 2:Error): ",
                                         playback)));

                UTL.traceIF1("IFSP_InvokeDirective.setInvokeDirectiveDiagnostic", diag.toString());
                id.setInvokeDirectiveDiagnostic(diag);
            }
            else if (what.getReference().equals("dir"))
            {
                String dirPrompt = "directive: (0=initADwithoutCLCW, 1=initADwithCLCW, 2=initADwithUnlock, 3=initADwithSetVR,\n"
                                   + "            4=terminateAD, 5=resumeAD, 6=setVS, 7=setFopSlidingWindow,\n"
                                   + "            8=setT1Initial, 9=setTransmissionLimit, 10=setTimeoutType, 11=abortVC,\n"
                                   + "            12=modifyMapPollingVector, 13=modifyMapPriorityList): ";
                // the last two entries are made-up, to distinguish between PV
                // and PL

                FSP_Directive dir = FSP_Directive.getDirectiveByCode(utl.readInt(dirPrompt, playback));
                switch (dir)
                {
                // directives without input:
                case fspDV_initiateADwithoutCLCW:
                    UTL.traceIF0("IFSP_InvokeDirective.setInitiateADwithoutCLCW");
                    id.setInitiateADwithoutCLCW();
                    break;
                case fspDV_initiateADwithCLCW:
                	UTL.traceIF0("IFSP_InvokeDirective.setInitiateADwithCLCW");
                    id.setInitiateADwithCLCW();
                    break;
                case fspDV_initiateADwithUnlock:
                	UTL.traceIF0("IFSP_InvokeDirective.setInitiateADwithUnlock");
                    id.setInitiateADwithUnlock();
                    break;
                case fspDV_terminateAD:
                	UTL.traceIF0("IFSP_InvokeDirective.setTerminateAD");
                    id.setTerminateAD();
                    break;
                case fspDV_resumeAD:
                	UTL.traceIF0("IFSP_InvokeDirective.setResumeAD");
                    id.setResumeAD();
                    break;
                case fspDV_abortVC:
                	UTL.traceIF0("IFSP_InvokeDirective::setAbortVC");
                    id.setAbortVC();
                    break;

                // directives with simple input:
                case fspDV_initiateADwithSetVR:
                {
                    long vr = utl.readInt("Receiver Frame Sequence No.: ", playback);
                    UTL.traceIF1("IFSP_InvokeDirective::setInitiateADwithSetVR", Long.toString(vr));
                    id.setInitiateADwithSetVR(vr);
                    break;
                }
                case fspDV_setVS:
                {
                    long vs = utl.readInt("Transm. Frame Sequence No.: ", playback);
                    UTL.traceIF1("IFSP_InvokeDirective.setVS", Long.toString(vs));
                    id.setVS(vs);
                    break;
                }
                case fspDV_setFopSlidingWindow:
                {
                    long sww = utl.readInt("FOP sliding window width: ", playback);
                    UTL.traceIF1("IFSP_InvokeDirective.setFopSlidingWindow", Long.toString(sww));
                    id.setFopSlidingWindow(sww);
                    break;
                }
                case fspDV_setT1Initial:
                {
                    long ti = utl.readInt("Timer Initial: ", playback);
                    UTL.traceIF1("IFSP_InvokeDirective.setTimerInitial", Long.toString(ti));
                    id.setTimerInitial(ti);
                    break;
                }
                case fspDV_setTransmissionLimit:
                {
                    long tl = utl.readInt("Transmission Limit: ", playback);
                    UTL.traceIF1("IFSP_InvokeDirective.setTransmissionLimit", Long.toString(tl));
                    id.setTransmissionLimit(tl);
                    break;
                }
                case fspDV_setTimeoutType:
                {
                    FSP_DirectiveTimeoutType tt = FSP_DirectiveTimeoutType.getDirectiveTimeoutTypeByCode(utl
                            .readInt("Timeout Type: ", playback));
                    UTL.traceIF1("IFSP_InvokeDirective.setTimeoutType", tt.toString());
                    id.setTimeoutType(tt);
                    break;
                }

                // directives with complex input:
                case fspDV_modifyMapMuxControl: // ATTENTION made-up for
                                                // modifyMapPollingVector (watch
                                                // out for changes)
                {
                    long[] pPollVector = utl.readIntList("Polling vector (comma-sep, no spaces): ", playback);
                    if (pPollVector.length == 1 && pPollVector[0] < 1)
                    {
                        System.out.println("Illegal Polling Vector input");
                    }
                    else
                    {
                    	UTL.traceIF1("IFSP_InvokeDirective.setModifyMapPollingVector", Long.toString(pPollVector[0]));
                        id.setModifyMapPollingVector(pPollVector);
                    }
                    break;
                }

                default: // 13 ATTENTION made-up for modifyMapPriorityList
                         // (watch out for changes)
                {
                    FSP_AbsolutePriority[] pPrioList = utl.readPriorityList("Priority List (vc1,pr1:vc2,pr2:vc3,pr3:... no spaces!): ", playback);
                    if (pPrioList.length < 1)
                    {
                        System.out.println("Illegal Priority List input");
                    }
                    else
                    {
                        UTL.traceIF1("IFSP_InvokeDirective.setModifyMapPriorityList",
                                     Integer.toString(pPrioList[0].getMapOrVc()));
                        id.setModifyMapPriorityList(pPrioList);
                    }
                    break;
                }
                }

            }
        }
    }

}
