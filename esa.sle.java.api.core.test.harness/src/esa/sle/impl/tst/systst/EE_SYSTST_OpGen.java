package esa.sle.impl.tst.systst;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_PeerAbort;
import ccsds.sle.api.isle.iop.ISLE_ScheduleStatusReport;
import ccsds.sle.api.isle.iop.ISLE_Stop;
import ccsds.sle.api.isle.iop.ISLE_Unbind;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIOpFactory;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_BindDiagnostic;
import ccsds.sle.api.isle.it.SLE_Diagnostics;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.it.SLE_ReportRequestType;
import ccsds.sle.api.isle.it.SLE_UnbindReason;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import esa.sle.impl.eapi.dcw.IDCW_EventQueue;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_SYSTST_OpGen
{
    private final SLE_ApplicationIdentifier srvType;

    private int version;

    protected ISLE_OperationFactory opFactory;

    protected boolean playback;

    public ISLE_SIOpFactory siOPF;

    protected UTL utl;

    public EE_SYSTST_OpGen(SLE_ApplicationIdentifier srvType,
                           ISLE_OperationFactory opf,
                           ISLE_SIOpFactory f,
                           boolean playback,
                           UTL utl)
    {
        this.srvType = srvType;
        this.opFactory = opf;
        this.playback = playback;
        this.siOPF = f;
        this.utl = utl;
    }

    public ISLE_Operation createOp(SLE_OpType opt, IDCW_EventQueue eventQueue, IUnknown si)
    {
        ISLE_Operation op = null;
        HRESULT rc = HRESULT.S_OK;

        if (this.siOPF != null)
        {
            try
            {
                op = this.siOPF.createOperation(ISLE_Operation.class, opt);
            }
            catch (SleApiException e)
            {
                rc = e.getHResult();
            }
            if (rc == HRESULT.SLE_E_TYPE)
            {
                System.out.println("WARNING: Operation creation not supported by SIOpFactory");
                System.out.println("         Creating operation via OperationFactory");
            }
        }

        if (rc != HRESULT.S_OK)
        {            
            int vNo = 2;
            if (this.version > 0)
            {
                vNo = this.version;
            }
            try
            {
                this.opFactory.createOperation(ISLE_Operation.class, opt, this.srvType, vNo);
            }
            catch (SleApiException e)
            {
                rc = e.getHResult();
            }
        }

        // create the operation object via the OperationFactory if
        // SIOpFactory is not existing or if the service-type specific
        // SIOpFactory does not support the creation of the operation
        if (rc != HRESULT.S_OK)
        {
            System.out.println("*** EE_SYSTST_OpGen::createOp failed: " + rc);
        }
        return op;
    }

    public void setUpOperation(ISLE_Operation op, IDCW_EventQueue eventQueue, IUnknown si)
    {
        SLE_OpType opt = op.getOperationType();
        if (opt == SLE_OpType.sleOT_bind)
        {
            setUpBind(op, this.playback, this.utl);
        }
        else if (opt == SLE_OpType.sleOT_unbind)
        {
            setUpUnbind(op, this.playback, this.utl);
        }
        else if (opt == SLE_OpType.sleOT_stop)
        {
            setUpStop(op, this.playback, this.utl);
        }
        else if (opt == SLE_OpType.sleOT_scheduleStatusReport)
        {
            setUpSSR(op, this.playback, this.utl);
        }
        else if (opt == SLE_OpType.sleOT_peerAbort)
        {
            setUpPeerAbort(op, this.playback, this.utl);
            // must be done after sending peer abort
            // if (eventQueue) {
            // HRESULT rc = eventQueue->FlushQueue(si);
            // cout << "FlushQueue the DCW Event Queue return " <<
            // EE_GenStrUtil::resultText(rc) << endl;
            // }
        }
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public static void setUpBind(ISLE_Operation op, boolean playback, UTL utl)
    {
        ISLE_Bind b = (ISLE_Bind) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");

        EE_Reference<String> arg = new EE_Reference<String>();
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("sii     set-up SIId");
                System.out.println("vn      set-up version number");
                System.out.println("rsp     set-op responder id");
                System.out.println("rspp    set-up responder port id");
                System.out.println("srv     set-up service type");
                System.out.println("bd      set-up bind diagnostic");
                System.out.println("ok      set-up completed");
            }

            // prompt();
            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("sii"))
            {
                ISLE_SII sii_p = utl.readSII(playback);
                b.setServiceInstanceId(sii_p);
            }
            // CHANGED-v2: added bind version number
            else if (what.getReference().equals("vn"))
            {
                System.out.println("Version Number: ");
                utl.read(arg, playback);
                int vn = Integer.parseInt(arg.getReference());
                b.setVersionNumber(vn);
            }
            else if (what.getReference().equals("bd"))
            {
                System.out.println("Bind Diagnostic: ");
                System.out
                        .println("(0=accessDenied, 1=serviceTypeNotSupported, 2=versionNotSupported, 3=noSuchServiceInstance");
                System.out
                        .println(" 4=alreadyBound, 5=siNotAccessibleToThisInitiator, 6=inconsistentServiceType, 7=invalidTime");
                System.out.print(" 8=outOfService, 127=other, -1=invalid 128=positive result): ");
                utl.read(arg, playback);
                int ur = Integer.parseInt(arg.getReference());
                if (ur == 128)
                {
                    b.setPositiveResult();
                }
                else
                {
                    b.setBindDiagnostic(SLE_BindDiagnostic.getBindDiagnosticByCode(ur));
                }
            }
            else if (what.getReference().equals("rsp"))
            {
                System.out.print("Responder Id: ");
                utl.read(arg, playback);
                b.setResponderIdentifier(arg.getReference());
            }
            else if (what.getReference().equals("rspp"))
            {
                System.out.print("Responder Port Id: ");
                utl.read(arg, playback);
                b.setResponderPortIdentifier(arg.getReference());
            }
            else if (what.getReference().equals("srv"))
            {
                System.out.println("Service Type: (RAF/RCF/ROCF/FSP/CLTU/FTCF): ");
                utl.read(arg, playback);
                SLE_ApplicationIdentifier ai;
                final String c = arg.getReference();

                if (c.toLowerCase().equals("raf"))
                {
                    ai = SLE_ApplicationIdentifier.sleAI_rtnAllFrames;
                }
                else if (c.toLowerCase().equals("rcf"))
                {
                    ai = SLE_ApplicationIdentifier.sleAI_rtnChFrames;
                }
                else if (c.toLowerCase().equals("cltu"))
                {
                    ai = SLE_ApplicationIdentifier.sleAI_fwdCltu;
                }
                else
                {
                    ai = SLE_ApplicationIdentifier.sleAI_fwdTcFrame;
                }
                b.setServiceType(ai);
            }
        }
    }

    public static void setUpUnbind(ISLE_Operation op, boolean playback, UTL utl)
    {
        ISLE_Unbind ub = (ISLE_Unbind) op;

        EE_Reference<String> what = new EE_Reference<>();
        what.setReference(" ");
        EE_Reference<String> arg = new EE_Reference<>();
        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("ur     set-up unbind reason");
                System.out.println("ok     set-up completed");
            }

            // prompt();
            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("ur"))
            {
                System.out.println("Unbind Reason: ");
                System.out.print("(0=end, 1=suspend, 2=versionNotSupp, 127=other, -1=invalid): ");
                utl.read(arg, playback);
                int ur = Integer.parseInt(arg.getReference());
                ub.setUnbindReason(SLE_UnbindReason.getUnbindReasonByCode(ur));
            }
        }
    }

    public static void setUpStop(ISLE_Operation op, boolean playback, UTL utl)
    {
        EE_Reference<String> what = new EE_Reference<>();
        what.setReference(" ");
        EE_Reference<String> arg = new EE_Reference<>();
        boolean firstTime = true;
        ISLE_Stop stop = (ISLE_Stop) op;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("di     set-up diagnostic");
                System.out.println("ok     set-up completed");
            }

            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("di"))
            {
                System.out.println("Diagnostic: ");
                System.out.print("(100=duplicateInvokeId,   127=other :");
                utl.read(arg, playback);
                int n = Integer.parseInt(arg.getReference());
                stop.setDiagnostics(SLE_Diagnostics.getDiagnosticsByCode(n));
            }
        }
    }

    public static void setUpSSR(ISLE_Operation op, boolean playback, UTL utl)
    {

        ISLE_ScheduleStatusReport sr = (ISLE_ScheduleStatusReport) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");
        EE_Reference<String> arg = new EE_Reference<String>();

        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("rrt     set-up report request type");
                System.out.println("rc      set-up reporting cycle");
                System.out.println("ok      set-up completed");
            }

            // prompt();
            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("rrt"))
            {
                System.out.println("Report Req Type: ");
                System.out.print("(0=immediately, 1=periodic, 2=stop, -1=invalid): ");
                utl.read(arg, playback);
                int rrt = Integer.parseInt(arg.getReference());
                sr.setReportRequestType(SLE_ReportRequestType.getReportRequestTypeByCode(rrt));
            }
            if (what.getReference().equals("rc"))
            {
                System.out.println("Reporting cycle: ");
                utl.read(arg, playback);
                int rc = Integer.parseInt(arg.getReference());
                sr.setReportingCycle(rc);
            }
        }
    }

    public static void setUpPeerAbort(ISLE_Operation op, boolean playback, UTL utl)
    {

        ISLE_PeerAbort pa = (ISLE_PeerAbort) op;
        EE_Reference<String> what = new EE_Reference<String>();
        what.setReference(" ");
        EE_Reference<String> arg = new EE_Reference<String>();

        boolean firstTime = true;

        while (!what.getReference().equals("ok"))
        {
            if (firstTime)
            {
                firstTime = false;
                System.out.println("pad     set-up peer-abort diagnostic");
                System.out.println("ok      set-up completed");
            }

            // prompt();
            System.out.print("Selection: ");
            utl.read(what, playback);

            if (what.getReference().equals("pad"))
            {
                System.out.println("Peer Abort diagnostic: ");
                System.out.println("(0=accessDenied,   1=unexpectedResponderId,  2=operationalRequirement,");
                System.out.println(" 3=protocolError,  4=communicationsFailure,  5=encodingError,");
                System.out.println(" 6=returnTimeout,  7=endOfServiceProvPeriod, 8=unsolicitedInvokeId");
                System.out.print("127=other,        -1=invalid): ");
                utl.read(arg, playback);
                int pad = Integer.parseInt(arg.getReference());
                pa.setPeerAbortDiagnostic(SLE_PeerAbortDiagnostic.getDiagByCode(pad));
            }
        }

    }
}
