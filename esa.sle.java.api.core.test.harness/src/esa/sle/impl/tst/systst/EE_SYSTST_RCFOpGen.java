package esa.sle.impl.tst.systst;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_TransferBuffer;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIOpFactory;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Diagnostics;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.ifsp.types.FSP_ParameterName;
import ccsds.sle.api.isrv.ircf.IRCF_GetParameter;
import ccsds.sle.api.isrv.ircf.IRCF_Start;
import ccsds.sle.api.isrv.ircf.IRCF_SyncNotify;
import ccsds.sle.api.isrv.ircf.IRCF_TransferData;
import ccsds.sle.api.isrv.ircf.types.RCF_ChannelType;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_ParameterName;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_StartDiagnostic;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.eapi.dcw.IDCW_EventQueue;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_SYSTST_RCFOpGen extends EE_SYSTST_OpGen
{
    private static final Logger LOG = Logger.getLogger(EE_SYSTST_RCFOpGen.class.getName());


    public EE_SYSTST_RCFOpGen(ISLE_OperationFactory opf, ISLE_SIOpFactory f, boolean playback, UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChFrames, opf, f, playback, utl);
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
        else if (opt == SLE_OpType.sleOT_syncNotify)
        {
            setUpSyncNotify(op, this.playback, this.utl);
        }
        else if (opt == SLE_OpType.sleOT_transferBuffer)
        {
            setUpTransferBuffer(op, this.playback, this.utl);
        }
        else if (opt == SLE_OpType.sleOT_transferData)
        {
            setUpTransferData(op, this.playback, this.utl);
        }
        else if (opt == SLE_OpType.sleOT_start)
        {
            setUpStart(op, this.playback, this.utl);
        }

        // sonst hamma nix
    }

    public static void setUpGetParameter(ISLE_Operation op, boolean playback, UTL utl)
    {
        IRCF_GetParameter gp = (IRCF_GetParameter) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference("");

        RCF_ParameterName pn = RCF_ParameterName.rcfPN_invalid;
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
                System.out.println("pgvc   get permitted GVCID list");
                System.out.println("rgvc   get requested GVCID");
                System.out.println("rto    get return timeout period ");
                System.out.println("inv    invalid parameter name");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("bs"))
            {
                pn = RCF_ParameterName.rcfPN_bufferSize;
            }
            else if (what.getReference().equals("dm"))
            {
                pn = RCF_ParameterName.rcfPN_deliveryMode;
            }
            else if (what.getReference().equals("ll"))
            {
                pn = RCF_ParameterName.rcfPN_latencyLimit;
            }
            else if (what.getReference().equals("rc"))
            {
                pn = RCF_ParameterName.rcfPN_reportingCycle;
            }
            else if (what.getReference().equals("pgvc"))
            {
                pn = RCF_ParameterName.rcfPN_permittedGvcidSet;
            }
            else if (what.getReference().equals("rgvc"))
            {
                pn = RCF_ParameterName.rcfPN_requestedGvcid;
            }
            else if (what.getReference().equals("rto"))
            {
                pn = RCF_ParameterName.rcfPN_returnTimeoutPeriod;
            }
            // New with SLES V5
            else if (what.getReference().equals("mrc"))
            {
                pn = RCF_ParameterName.rcfPN_minReportingCycle;
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
        IRCF_SyncNotify sn = (IRCF_SyncNotify) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference("");

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

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("ps"))
            {
                System.out.println("Production Status: ");
                System.out.print("(0=running, 1=interrupted, 2=halted, -1=invalid): ");
                utl.read(arg, playback);
                int ps = Integer.parseInt(arg.getReference());
                sn.setProductionStatusChange(RCF_ProductionStatus.getProductionStatusByCode(ps));
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
                try
                {
                    time = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                }
                if (time != null)
                {
                    time.update();
                    System.out.print("Lock Status: (0=inLock, 1=outOfLock, 2=notInUse, 3=unknown) ");
                    utl.read(arg, playback);
                    int ls = Integer.parseInt(arg.getReference());
                    RCF_LockStatus rls = RCF_LockStatus.getLockStatusByCode(ls);
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
        IRCF_TransferData td = (IRCF_TransferData) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference("");

        EE_Reference<String> arg = new EE_Reference<String>();
        boolean firstTime = true;

        td.setDataLinkContinuity(25); // default set-up for tests

        boolean picoEnabled = false;
        boolean antennaIdGlobal = true;
        
        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("dl     set-up requ data length");
                System.out.println("dlc    set-up data-link-continuity");
                System.out.println("pico   set-up picosecond resolution");
                System.out.println("aidl   set antenna id to a local format");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("dl"))
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
            else if (what.getReference().equals("dlc"))
            {
                System.out.println("Data Link Continuity ( > -2 ): ");
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
                System.out.println("*** unknown command");
            }
        }

        ISLE_Time time = null;
        try
        {
            time = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
            if (time != null)
            {
                time.update();
                if (picoEnabled)
                {
                    byte[] cds_time = time.getCDSToPicosecondsRes();
                    time.setCDSToPicosecondsRes(cds_time);
                    // add a picosecond value for the picosecond configuration
                    String timeWithPicoSec = time.getTime(SLE_TimeFmt.sleTF_dayOfYear, SLE_TimeRes.sleTR_seconds);
                    timeWithPicoSec = timeWithPicoSec.substring(0, 8) + ".123456789012";
                    time.setTime(timeWithPicoSec);
                }
                td.setEarthReceiveTime(time);

                if(antennaIdGlobal)
                {
                	td.setAntennaIdGFString("1.2.3");
                }
                else
                {
                	byte[] aid = {32, 35, 35};
                	td.setAntennaIdLF(aid);
                }
            }
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
    }

    public static void setUpStart(ISLE_Operation op, boolean playback, UTL utl)
    {
        IRCF_Start st = (IRCF_Start) op;
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
                System.out.println("gvc    set-up GvcId");
                System.out.println("sn     set-up negative result with common diag");
                System.out.println("sns    set-up negative result with start diag");
                System.out.println("ok      set-up completed");
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
            else if (what.getReference().equals("gvc"))
            {
                RCF_Gvcid gvcId = new RCF_Gvcid();
                System.out.println("Enter GCVID:");
                System.out.print("Type (0=Master,1=Virtual): ");
                utl.read(arg, playback);
                int type_i = Integer.parseInt(arg.getReference());
                gvcId.setType(RCF_ChannelType.getChannelTypeByCode(type_i));

                System.out.print("SCID (0-1023): ");
                utl.read(arg, playback);
                int sc_i = Integer.parseInt(arg.getReference());
                gvcId.setScid(sc_i);

                System.out.print("Version (0-1): ");
                utl.read(arg, playback);
                int ver_i = Integer.parseInt(arg.getReference());

                gvcId.setVersion(ver_i);

                System.out.print("VC ID  (0-63): ");
                utl.read(arg, playback);
                int vc_i = Integer.parseInt(arg.getReference());
                gvcId.setVcid(vc_i);

                st.setGvcid(gvcId);
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
                        .print("(0=outOfService, 1=unableToComply, 2=invalidStartTime, 3=invalidStopTime, 4=missingTimeValue, 5: invalidGvcId): ");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                st.setStartDiagnostic(RCF_StartDiagnostic.getStartDiagnosticByCode(n));
            }
            else if (!what.getReference().equals("ok"))
            {
                System.out.println("*** unknown command");
            }
        }
    }

    public static void setUpTransferBuffer(ISLE_Operation op, boolean playback, UTL utl)
    {

        ISLE_TransferBuffer st = (ISLE_TransferBuffer) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");

        EE_Reference<String> arg = new EE_Reference<String>();
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("bs     set-up Max Buffer Size");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("bs"))
            {
                System.out.print("Max Buffer Size: ");
                utl.read(arg, playback);
                int val = Integer.parseInt(arg.getReference());
                HRESULT res = HRESULT.S_OK;
                try
                {
                    st.setMaximumSize(val);
                }
                catch (SleApiException e)
                {
                    res = e.getHResult();

                    if (res != HRESULT.S_OK)
                    {
                        System.out.println(e.getMessage());
                    }

                }
            }
        }

    }
}
