package esa.sle.impl.tst.systst;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIOpFactory;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Diagnostics;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.types.FSP_ParameterName;
import ccsds.sle.api.isrv.iraf.IRAF_GetParameter;
import ccsds.sle.api.isrv.iraf.IRAF_Start;
import ccsds.sle.api.isrv.iraf.IRAF_SyncNotify;
import ccsds.sle.api.isrv.iraf.IRAF_TransferData;
import ccsds.sle.api.isrv.iraf.types.RAF_FrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_ParameterName;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_RequestedFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_StartDiagnostic;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.eapi.dcw.IDCW_EventQueue;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_SYSTST_RAFOpGen extends EE_SYSTST_OpGen
{
    private static final Logger LOG = Logger.getLogger(EE_SYSTST_RAFOpGen.class.getName());


    public EE_SYSTST_RAFOpGen(ISLE_OperationFactory opf, ISLE_SIOpFactory f, boolean playback, UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnAllFrames, opf, f, playback, utl);
    }

    @Override
    public ISLE_Operation createOp(SLE_OpType opt, IDCW_EventQueue eventQueue, IUnknown si)
    {
        ISLE_Operation op = super.createOp(opt, eventQueue, si);

        if (op != null)
        {
            System.out.println("------------------------------------");
            String op_s = op.print(512);
            System.out.print(op_s);

            EE_Reference<String> yn = new EE_Reference<String>();
            System.out.println("\n Set-up operation object (y/n): ");
            utl.read(yn, this.playback);
            if (yn.getReference().equals("y"))
            {
                System.out.println();
                setUpOperation(op, eventQueue, si);
            }
            System.out.println("------------------------------------");
            op_s = op.print(512);
            System.out.print(op_s);
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
        else if (opt == SLE_OpType.sleOT_syncNotify)
        {
            setUpSyncNotify(op, this.playback, this.utl);
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

    public static void setUpGetParameter(ISLE_Operation op, boolean playback, UTL utl)
    {
        IRAF_GetParameter gp = (IRAF_GetParameter) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");

        RAF_ParameterName pn = RAF_ParameterName.rafPN_invalid;
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("Select Parameter Name: ");
                System.out.println("bs     get buffer size ");
                System.out.println("dm     get delivery mode ");
                System.out.println("ll     get latency limit ");
                System.out.println("mrc    get minimum reporting cycle");		// new since SLES v5
                System.out.println("rc     get reporting cycle ");
                System.out.println("rfq    get requested frame quality");
                System.out.println("rto    get return timeout period ");
                System.out.println("pfq    get permitted frame quality ");		// new since SLES v5
                System.out.println("inv    invalid parameter name");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("bs"))
            {
                pn = RAF_ParameterName.rafPN_bufferSize;
            }
            else if (what.getReference().equals("dm"))
            {
                pn = RAF_ParameterName.rafPN_deliveryMode;
            }
            else if (what.getReference().equals("ll"))
            {
                pn = RAF_ParameterName.rafPN_latencyLimit;
            }
            else if (what.getReference().equals("rc"))
            {
                pn = RAF_ParameterName.rafPN_reportingCycle;
            }
            else if (what.getReference().equals("rfq"))
            {
                pn = RAF_ParameterName.rafPN_requestFrameQuality;
            }
            else if (what.getReference().equals("rto"))
            {
                pn = RAF_ParameterName.rafPN_returnTimeoutPeriod;
            }
            // New with SLES v5
            else if (what.getReference().equals("mrc"))
            {
                pn = RAF_ParameterName.rafPN_minReportingCycle;
            }
            else if (what.getReference().equals("pfq"))
            {
                pn = RAF_ParameterName.rafPN_permittedFrameQuality;
            }
            else if (!what.getReference().equals("ok"))
            {
                System.out.println("*** unknown command");
            }

        }

        gp.setRequestedParameter(pn);
    }

    public static void setUpSyncNotify(ISLE_Operation op, boolean playback, UTL utl)
    {

        IRAF_SyncNotify sn = (IRAF_SyncNotify) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");

        EE_Reference<String> arg = new EE_Reference<String>();
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("ps     set production status change notification");
                System.out.println("eod    set end of data notification");
                System.out.println("lfs    set loss of frame sync notification");
                System.out.println("dd     set data discarded");
                System.out.println("ok     set-up completed");
            }

            System.out.println("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("ps"))
            {
                System.out.println("Production Status: ");
                System.out.print("(0=running, 1=interrupted, 2=halted, -1=invalid): ");
                utl.read(arg, playback);
                int ps = Integer.parseInt(arg.getReference());
                sn.setProductionStatusChange(RAF_ProductionStatus.getProductionStatusByCode(ps));
            }
            else if (what.getReference().equals("eod"))
            {
                sn.setEndOfData();
                System.out.println("End of data is set");
            }
            else if (what.getReference().equals("dd"))
            {
                sn.setDataDiscarded();
                System.out.println("Data discarded is set");
            }
            else if (what.getReference().equals("lfs"))
            {

                ISLE_Time time = null;
                HRESULT res = HRESULT.S_OK;
                try
                {
                    time = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
                }
                catch (SleApiException e)
                {
                    res = e.getHResult();
                }
                if (res == HRESULT.S_OK)
                {
                    time.update();
                    System.out.println("Lock Status: (0=inLock, 1=outOfLock, 2=notInUse, 3=unknown) ");
                    utl.read(arg, playback);
                    int ls = Integer.parseInt(arg.getReference());
                    RAF_LockStatus rls = RAF_LockStatus.getLockStatusByCode(ls);
                    sn.setLossOfFrameSync(time, rls, rls, rls);
                }

            }
            else if (!what.getReference().equals("ok"))
            {
                System.out.println("*** unknown command");
            }
        }
    }

    public static void setUpTransferData(ISLE_Operation op, boolean playback, UTL utl)
    {
        IRAF_TransferData td = (IRAF_TransferData) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference("");

        EE_Reference<String> arg = new EE_Reference<String>();
        boolean firstTime = true;

        td.setDataLinkContinuity(25); // default set-up for our tests

        boolean picoEnabled = false;
        boolean antennaIdGlobal = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("fq     set-up requ frame quality");
                System.out.println("dl     set-up requ data length");
                System.out.println("dlc    set-up data-link-continuity");
                System.out.println("panno  set-up private annotation");
                System.out.println("pico   set-up picosecond resolution"); // Not
                                                                           // mandatory
                                                                           // to
                                                                           // set:
                                                                           // if
                                                                           // not
                                                                           // set,
                                                                           // microsecond
                                                                           // resolution
                                                                           // will
                                                                           // be
                                                                           // used
                System.out.println("aidl   set antenna id to a local format");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("fq"))
            {
                System.out.print("Frame quality: (0=good, 1=erred, 2=undetermined, -1=invalid): ");
                utl.read(arg, playback);
                int quality = Integer.parseInt(arg.getReference());
                td.setFrameQuality(RAF_FrameQuality.getFrameQualityByCode(quality));
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
                    data[i] = 'A';
                }
                for (int i = 0; i < lg; i++)
                {
                    data[i] = (byte) tmp.charAt(i % lg1);
                }
                td.setData(data);
            }
            else if (what.getReference().equals("panno"))
            {
                System.out.print("Private Annotation Length : ");
                utl.read(arg, playback);
                int lg = Integer.parseInt(arg.getReference());
                final String tmp = "0123456789ABCDEF";
                int lg1 = tmp.length();
                byte[] anno = new byte[lg];
                for (int i = 0; i < lg; i++)
                {
                    anno[i] = 'A';
                }

                for (int i = 0; i < lg; i++)
                {
                    anno[i] = (byte) tmp.charAt(i % lg1);
                }
                td.setPrivateAnnotation(anno);
            }
            else if (what.getReference().equals("dlc"))
            {
                System.out.print("Data Link Continuity ( > -2 ): ");
                utl.read(arg, playback);
                int dataLinkCont = Integer.parseInt(arg.getReference());
                td.setDataLinkContinuity(dataLinkCont);
            }
            else if (what.getReference().equals("pico"))
            {
                picoEnabled = true;
            }
            else if (what.getReference().equals("aidl"))
            {
            	antennaIdGlobal = false;
            }
            else if (!what.getReference().equals("ok"))
            {
                System.out.println("*** unknown command ");
            }
        }

        ISLE_Time time = null;
        try
        {
            time = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
        }
        catch (SleApiException e1)
        {
            LOG.log(Level.FINE, "SleApiException ", e1);
        }
        time.update();

        if (picoEnabled)
        {
            byte[] cds_time = time.getCDSToPicosecondsRes();
            try
            {
                time.setCDSToPicosecondsRes(cds_time);
                // add a picosecond value for the picosecond configuration
                String timeWithPicoSec = time.getTime(SLE_TimeFmt.sleTF_dayOfYear, SLE_TimeRes.sleTR_seconds);
                timeWithPicoSec = timeWithPicoSec.substring(0, 8) + ".123456789012";
                time.setTime(timeWithPicoSec);
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
            }
        }
        td.setEarthReceiveTime(time);
        if(antennaIdGlobal == true)
        {
        	td.setAntennaIdGFString("1.2.3");
        }
        else
        {
        	byte[] aidl = {32, 35, 35};
        	td.setAntennaIdLF(aidl);	
        }
    }

    public static void setUpStart(ISLE_Operation op, boolean playback, UTL utl)
    {
        IRAF_Start st = (IRAF_Start) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");

        EE_Reference<String> arg = new EE_Reference<String>();
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("st     set-up Start Time");
                System.out.println("et     set-up End Time");
                System.out.println("fq     set-up requ frame quality");
                System.out.println("sn     set-up negative result with common diag");
                System.out.println("sns    set-up negative result with start diag");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("st"))
            {
                ISLE_Time st_p = utl.readTime("Start Time: ", playback);
                st.setStartTime(st_p);

            }
            else if (what.getReference().equals("et"))
            {
                ISLE_Time st_p = utl.readTime("Stop Time: ", playback);
                st.setStopTime(st_p);
            }
            else if (what.getReference().equals("fq"))
            {
                System.out.print("Requested Frame quality: (0=good, 1=erred, 2=all, -1=invalid): ");
                utl.read(arg, playback);
                int quality = Integer.parseInt(arg.getReference());
                st.setRequestedFrameQuality(RAF_RequestedFrameQuality.getRequestedFrameQualityByCode(quality));
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
                System.out
                        .print("(0=outOfService, 1=unableToComply, 2=invalidStartTime, 3=invalidStopTime, 4=missingTimeValue): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                st.setStartDiagnostic(RAF_StartDiagnostic.getStartDiagnosticByCode(n));
            }
            else if (!what.getReference().equals("ok"))
            {
                System.out.println("*** unknown command");
            }
        }

    }
}
