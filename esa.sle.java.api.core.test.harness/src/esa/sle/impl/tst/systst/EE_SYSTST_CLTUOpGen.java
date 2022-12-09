package esa.sle.impl.tst.systst;

import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIOpFactory;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Diagnostics;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_SlduStatusNotification;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.ICLTU_AsyncNotify;
import ccsds.sle.api.isrv.icltu.ICLTU_GetParameter;
import ccsds.sle.api.isrv.icltu.ICLTU_Start;
import ccsds.sle.api.isrv.icltu.ICLTU_ThrowEvent;
import ccsds.sle.api.isrv.icltu.ICLTU_TransferData;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationType;
import ccsds.sle.api.isrv.icltu.types.CLTU_ParameterName;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_StartDiagnostic;
import ccsds.sle.api.isrv.icltu.types.CLTU_Status;
import ccsds.sle.api.isrv.icltu.types.CLTU_ThrowEventDiagnostic;
import ccsds.sle.api.isrv.icltu.types.CLTU_TransferDataDiagnostic;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;
import esa.sle.impl.eapi.dcw.IDCW_EventQueue;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_SYSTST_CLTUOpGen extends EE_SYSTST_OpGen
{
    public EE_SYSTST_CLTUOpGen(ISLE_OperationFactory opf, ISLE_SIOpFactory f, boolean playback, UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdCltu, opf, f, playback, utl);
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
            setUpGetParameter(op, this.playback, this.utl);
        }
        else if (opt == SLE_OpType.sleOT_asyncNotify)
        {
            setUpAsyncNotify(op, this.playback, this.utl);
        }
        else if (opt == SLE_OpType.sleOT_throwEvent)
        {
            setUpThrowEvent(op, this.playback, this.utl);
        }
        else if (opt == SLE_OpType.sleOT_transferData)
        {
            setUpTransferData(op, this.playback, this.utl);
        }
        else if (opt == SLE_OpType.sleOT_start)
        {
            setUpStart(op, this.playback, this.utl);
        }
    }

    static void setUpAsyncNotify(ISLE_Operation op, boolean playback, UTL utl)
    {
        ICLTU_AsyncNotify an = (ICLTU_AsyncNotify) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference("");

        EE_Reference<String> arg = new EE_Reference<String>();
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("nt     set-up notification type");
                System.out.println("etid   set-up event thrown id  ");
                System.out.println("idlp   set-up CLTU id - last processed");
                System.out.println("idok   set-up CLTU id - last Ok");
                System.out.println("rst    set-up radiation start time");
                System.out.println("ret    set-up radiation stop  time");
                System.out.println("cs     set-up CLTU status");
                System.out.println("ps     set-up production status");
                System.out.println("us     set-up uplink status");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("nt"))
            {
                System.out.println("Notification type: ");
                System.out.println("0=cltuRadiated, 1=slduExpired, 2=productionInterrupted, 3=productionHalted,");
                System.out.println("4=productionOperational,  5=bufferEmpty, 6=actionListCompleted,");
                System.out.println("7=actionListNotCompleted, 8=eventConditionEvFalse, -1=invalid : ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("ICLTU_AsyncNotify.setNotificationType", CLTU_NotificationType
                        .getNotificationTypeByCode(n).toString());
                an.setNotificationType(CLTU_NotificationType.getNotificationTypeByCode(n));
            }
            else if (what.getReference().equals("etid"))
            {
                System.out.print("Event invocation id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("ICLTU_AsyncNotify.setEventThrownId", Integer.toString(n));
                an.setEventThrownId(n);
            }

            else if (what.getReference().equals("idlp"))
            {
                System.out.print("CLTU Id - last processed: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("ICLTU_AsyncNotify.setCltuLastProcessed", Integer.toString(n));
                an.setCltuLastProcessed(n);
            }

            else if (what.getReference().equals("idok"))
            {
                System.out.print("CLTU Id - last ok: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("ICLTU_AsyncNotify.setCltuLastOk", Integer.toString(n));
                an.setCltuLastOk(n);
            }

            else if (what.getReference().equals("rst"))
            {
                ISLE_Time st_p = utl.readTime("Radiation start time: ", playback);
                UTL.traceIF0("ICLTU_AsyncNotify.setRadiationStartTime");
                an.setRadiationStartTime(st_p);
            }

            else if (what.getReference().equals("ret"))
            {
                ISLE_Time st_p = utl.readTime("Radiation stop time: ", playback);
                UTL.traceIF0("ICLTU_AsyncNotify.setRadiationStopTime");
                an.setRadiationStopTime(st_p);
            }

            else if (what.getReference().equals("cs"))
            {
                System.out.println("CLTU Status: ");
                System.out.println("0=radiated, 1=expired, 2=interrupted, 4=radiationStarted,");
                System.out.println("5=radiationNotStarted, -1=invalid: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("ICLTU_AsyncNotify.setCltuStatus", CLTU_Status.getStatusByCode(n).toString());
                an.setCltuStatus(CLTU_Status.getStatusByCode(n));
            }

            else if (what.getReference().equals("ps"))
            {
                System.out.println("Production Status: ");
                System.out.println("0=operational, 1=configured, 2=interrupted, ");
                System.out.print("3=halted,     -1=invalid : ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("ICLTU_AsyncNotify.setProductionStatus", CLTU_ProductionStatus
                        .getProductionStatusByCode(n).toString());
                an.setProductionStatus(CLTU_ProductionStatus.getProductionStatusByCode(n));
            }

            else if (what.getReference().equals("us"))
            {
                System.out.println("Uplink Status: ");
                System.out.println("0=notAvailable, 1=noRfAvailable, 2=noBitLock,");
                System.out.println("3=nominal,     -1=invalid : ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                UTL.traceIF1("ICLTU_AsyncNotify.setUplinkStatus", CLTU_UplinkStatus.getUplinkStatusByCode(n).toString());
                an.setUplinkStatus(CLTU_UplinkStatus.getUplinkStatusByCode(n));
            }
            else if (!what.getReference().equals("ok"))
            {
                System.out.println("*** unknown command " + what.getReference());
            }

        }
    }

    static void setUpThrowEvent(ISLE_Operation op, boolean playback, UTL utl)
    {

        ICLTU_ThrowEvent te = (ICLTU_ThrowEvent) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");
        EE_Reference<String> arg = new EE_Reference<String>();

        while (!what.getReference().equals("ok"))
        {
            System.out.println("id     set-up event identfier");
            System.out.println("eid    set-up event invocation identfier");
            System.out.println("eq     set-up event qualifier");
            System.out.println("eeid   set-up next expected event invocation identfier (return PDU)");
            System.out.println("sd     set-up diagnostic");
            System.out.println("sp     set-up positive result");
            System.out.println("ok     set-up completed");

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("id"))
            {
                System.out.print("Event Id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
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
                te.setExpectedEventInvocationId(n);
            }
            else if (what.getReference().equals("sd"))
            {
                System.out.println("ThrowEvent Diagnostic: ");
                System.out.print("(0=operation not supported, 1=out of sequence, 2=no such event): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                te.setThrowEventDiagnostic(CLTU_ThrowEventDiagnostic.getThrowEventDiagnosticByCode(n));
            }
            else if (!what.getReference().equals("ok"))
            {
                System.out.println("*** unknown command " + what.getReference());
            }
        }

    }

    static void setUpGetParameter(ISLE_Operation op, boolean playback, UTL utl)
    {
        ICLTU_GetParameter gp = (ICLTU_GetParameter) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference("");

        CLTU_ParameterName pn = CLTU_ParameterName.cltuPN_invalid;
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("Select Parameter Name: ");
                System.out.println("blr      get bit-lock-required");
                System.out.println("dm       get delivery mode ");
                System.out.println("eid      get expected-cltu-id ");
                System.out.println("eeid     get expected-event--invocation-id ");
                System.out.println("ml       get maximum Sldu length ");
                System.out.println("mf       get modulation frequency");
                System.out.println("mi       get modulation index");
                System.out.println("mrc      get minimum reporting cycle");		// new since SLES v5
                System.out.println("plop     get plop-in-effect");
                System.out.println("rc       get reporting-cycle");
                System.out.println("rto      get return-timeout");
                System.out.println("rfa      get rf-available-required");
                System.out.println("scbr     get subcarrier-to-bitrate-ratio");
                System.out.println("ill      illegal parameter");
                System.out.println("ok       set-up completed");
                System.out.println("acqsl    get acquisition sequence length ");
                System.out.println("plidsl   get plop1 idle sequence length ");
                System.out.println("pam      get protocol abort mode ");
                System.out.println("cgv      get clcw global vcid ");
                System.out.println("cpc      get clcw physical channel");
                System.out.println("mdt      get minimum delay time");
                System.out.println("mrc      get minimum reporting cycle");
                System.out.println("nm       get notification mode");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("blr"))
            {
                pn = CLTU_ParameterName.cltuPN_bitLockRequired;
            }
            else if (what.getReference().equals("dm"))
            {
                pn = CLTU_ParameterName.cltuPN_deliveryMode;
            }
            else if (what.getReference().equals("eid"))
            {
                pn = CLTU_ParameterName.cltuPN_expectedSlduIdentification;
            }
            else if (what.getReference().equals("eeid"))
            {
                pn = CLTU_ParameterName.cltuPN_expectedEventInvocationId;
            }
            else if (what.getReference().equals("ml"))
            {
                pn = CLTU_ParameterName.cltuPN_maximumSlduLength;
            }
            else if (what.getReference().equals("mf"))
            {
                pn = CLTU_ParameterName.cltuPN_modulationFrequency;
            }
            else if (what.getReference().equals("mi"))
            {
                pn = CLTU_ParameterName.cltuPN_modulationIndex;
            }
            else if (what.getReference().equals("plop"))
            {
                pn = CLTU_ParameterName.cltuPN_plopInEffect;
            }
            else if (what.getReference().equals("rc"))
            {
                pn = CLTU_ParameterName.cltuPN_reportingCycle;
            }
            else if (what.getReference().equals("rto"))
            {
                pn = CLTU_ParameterName.cltuPN_returnTimeoutPeriod;
            }
            else if (what.getReference().equals("rfa"))
            {
                pn = CLTU_ParameterName.cltuPN_rfAvailableRequired;
            }
            else if (what.getReference().equals("scbr"))
            {
                pn = CLTU_ParameterName.cltuPN_subcarrierToBitRateRatio;
            }
            else if (what.getReference().equals("acqsl"))
            {
                pn = CLTU_ParameterName.cltuPN_acquisitionSequenceLength;
            }
            else if (what.getReference().equals("plidsl"))
            {
                pn = CLTU_ParameterName.cltuPN_plop1IdleSequenceLength;
            }
            else if (what.getReference().equals("pam"))
            {
                pn = CLTU_ParameterName.cltuPN_protocolAbortMode;
            }
            else if (what.getReference().equals("nm"))
            {
                pn = CLTU_ParameterName.cltuPN_notificationMode;
            }
            else if (what.getReference().equals("cgv"))
            {
                pn = CLTU_ParameterName.cltuPN_clcwGlobalVcid;
            }
            else if (what.getReference().equals("cpc"))
            {
                pn = CLTU_ParameterName.cltuPN_clcwPhysicalChannel;
            }
            else if (what.getReference().equals("mdt"))
            {
                pn = CLTU_ParameterName.cltuPN_minimumDelayTime;
            }
            // New with SLES v5
            else if (what.getReference().equals("mrc"))
            {
                pn = CLTU_ParameterName.cltuPN_minimumReportingCycle;
            }
            else if (!what.getReference().equals("ill") && !what.getReference().equals("ok"))
            {
                System.out.println("*** unknown command on setUpGetParameter: " + what.getReference());
            }
        }
        if (what.getReference().equals("ill"))
        {
            pn = CLTU_ParameterName.getParameterNameByCode(40);
        }
        UTL.traceIF1("ICLTU_GetParameter.setRequestedParameter", pn.toString());
        gp.setRequestedParameter(pn);

    }

    static void setUpTransferData(ISLE_Operation op, boolean playback, UTL utl)
    {
        ICLTU_TransferData td = (ICLTU_TransferData) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference("");

        EE_Reference<String> arg = new EE_Reference<String>();
        boolean with_data = false;
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("ert    set-up earliest radiation time (optional)");
                System.out.println("lrt    set-up latest   radiation time (optional)");
                System.out.println("dt     set-up delay time (optional) ");
                System.out.println("rn     set-up radiation notification");
                System.out.println("id     set-up CLTU id for the CLTU to be transferred");
                System.out.println("eid    set-up next expected CLTU id (for return-PDU)");
                System.out.println("sp     set-up positive result");
                System.out.println("sd     set-up diagnostic");
                System.out.println("dl     set-up requ data length");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("rn"))
            {
                System.out.println("Radiation Notification: ");
                System.out.print("(0=produce notif., 1=do not produce notif., -1=invalid): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                td.setRadiationNotification(SLE_SlduStatusNotification.getSlduStatusNotificationByCode(n));
            }

            else if (what.getReference().equals("id"))
            {
                System.out.print("CLTU Id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                td.setCltuId(n);
            }

            else if (what.getReference().equals("dt"))
            {
                System.out.print("Delay Time (millisec): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                td.setDelayTime(n);
            }

            else if (what.getReference().equals("eid"))
            {
                System.out.print("Next Expected CLTU Id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                td.setExpectedCltuId(n);
            }

            else if (what.getReference().equals("ert"))
            {
                ISLE_Time st_p = utl.readTime("Earliest radiation time: ", playback);
                td.setEarliestRadTime(st_p);
            }

            else if (what.getReference().equals("lrt"))
            {
                ISLE_Time st_p = utl.readTime("Latest radiation time: ", playback);
                td.setLatestRadTime(st_p);
            }
            else if (what.getReference().equals("sd"))
            {
                System.out.println("Transfer Data Diagnostic: ");
                System.out.print("(0=unable to process, 1=unable to store, 2=out of sequence");
                System.out.print(" 3=inconsistent time range, 4=invalid time, 5=late sldu");
                System.out.print(" 6=invalidDelayTime, 7=cltuError): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                td.setTransferDataDiagnostic(CLTU_TransferDataDiagnostic.getTransferDataDiagnosticByCode(n));
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
                String tmp = "0123456789ABCDEF";
                int lg1 = tmp.length();
                byte[] data = new byte[lg];
                for (int i = 0; i < lg; i++)
                {
                    data[i] = 'A';
                }
                for (int i = 0; i < lg; i++)
                {
                    data[i] = (byte) tmp.charAt(i % lg1);
                }
                td.setData(data);
                with_data = true;
            }
            else if (!what.getReference().equals("ok"))
            {
                System.out.println("*** unknown command " + what.getReference());
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
            td.setData(data);
        }
    }

    static void setUpStart(ISLE_Operation op, boolean playback, UTL utl)
    {
        ICLTU_Start st = (ICLTU_Start) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");

        EE_Reference<String> arg = new EE_Reference<String>();
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("id     set-up first CLTU id the provider shall accept");
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
                System.out.print("CLTU Id: ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                st.setFirstCltuId(n);
            }
            else if (what.getReference().equals("sn"))
            {
                System.out.println("Diagnostic: ");
                System.out.println("(100=duplicateInvokeId, 127=other): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                st.setDiagnostics(SLE_Diagnostics.getDiagnosticsByCode(n));
            }
            else if (what.getReference().equals("sns"))
            {
                System.out.println("Diagnostic: ");
                System.out.print("(0=outOfService, 1=unableToComply, 2=productionTimeExpired, 3=invalidCltuId): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                st.setStartDiagnostic(CLTU_StartDiagnostic.getStartDiagnosticByCode(n));
            }
            else if (what.getReference().equals("spd") || what.getReference().equals("st")) // ignore
                                                                                            // error
                                                                                            // in
                                                                                            // ctl
                                                                                            // scripts
            {
                ISLE_Time st_p = utl.readTime("Start production time: ", playback);
                st.setStartProductionTime(st_p);
            }

            else if (what.getReference().equals("epd") || what.getReference().equals("et")) // ignore
                                                                                            // error
                                                                                            // in
                                                                                            // ctl
                                                                                            // scripts
            {
                ISLE_Time st_p = utl.readTime("Stop production time: ", playback);
                st.setStopProductionTime(st_p);
            }
            else if (!what.getReference().equals("ok"))
            {
                System.out.println("*** unknown command " + what.getReference());
            }

        }
    }

}
