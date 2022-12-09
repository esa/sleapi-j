package esa.sle.impl.tst.systst;

import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.icc.ISLE_TimeoutProcessor;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIAdmin;
import ccsds.sle.api.isle.ise.ISLE_SIOpFactory;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_SlduStatusNotification;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.ICLTU_AsyncNotify;
import ccsds.sle.api.isrv.icltu.ICLTU_SIAdmin;
import ccsds.sle.api.isrv.icltu.ICLTU_SIUpdate;
import ccsds.sle.api.isrv.icltu.ICLTU_ThrowEvent;
import ccsds.sle.api.isrv.icltu.ICLTU_TransferData;
import ccsds.sle.api.isrv.icltu.types.CLTU_ChannelType;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwGvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.icltu.types.CLTU_GvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_EventResult;
import ccsds.sle.api.isrv.icltu.types.CLTU_Failure;
import ccsds.sle.api.isrv.icltu.types.CLTU_ConfType;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationType;
import ccsds.sle.api.isrv.icltu.types.CLTU_PlopInEffect;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProtocolAbortMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_Status;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;
import esa.sle.impl.ifs.gen.EE_CondVar;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.ifs.time.EE_Duration;
import esa.sle.impl.ifs.time.EE_ElapsedTimer;
import esa.sle.impl.ifs.time.EE_TIME_Prec;
import esa.sle.impl.tst.systst.types.EE_SYSTST_T_Component;
import esa.sle.impl.tst.systst.types.T_CLTUCmd;

public class EE_SYSTST_CLTUSIClient extends EE_SYSTST_SIClient implements ISLE_TimeoutProcessor
{

    private static final Logger LOG = Logger.getLogger(EE_SYSTST_CLTUSIClient.class.getName());

    private int lgRadiated;

    private boolean stopThread;

    private int lastCltu;

    private EE_SYSTST_CLTUOpGen opGen; // the CLTU operations generator

    private long lastRecCltuId;

    private long currentCltuBufferSize;

    private ICLTU_SIUpdate cltusiupd;

    private EE_CondVar eeCondVarNotification;

    ReentrantLock objMutex = new ReentrantLock();

    private final TreeMap<Integer, EE_ElapsedTimer> mapElapsedTimer = new TreeMap<>();

    private final String[] helpCommand[] = {
                                            { "set_blr          ICLTU_SIAdmin: set bit lock required" },
                                            { "set_maxl         ICLTU_SIAdmin: set max Sldu length" },
                                            { "set_mf           ICLTU_SIAdmin: set modulation frequency" },
                                            { "set_mi           ICLTU_SIAdmin: set modulation index" },
                                            { "set_plop         ICLTU_SIAdmin: set plop in effect" },
                                            { "set_rfr          ICLTU_SIAdmin: set RF lock required" },
                                            { "set_scbrr        ICLTU_SIAdmin: set subcarr. to bitrate ratio" },
                                            { "set_mbs          ICLTU_SIAdmin: set max buffer size" },
                                            { "set_init_ps      ICLTU_SIAdmin: set initial production status" },
                                            { "set_init_uls     ICLTU_SIAdmin: set initial uplink status" },
                                            { "                                                         " },
                                            { "cltu_started      ICLTU_SIUpdate: CltuStarted" },
                                            { "cltu_ns           ICLTU_SIUpdate: CltuNotStarted" },
                                            { "cltu_rad          ICLTU_SIUpdate: CltuRadiated" },
                                            { "cltu_aborted      ICLTU_SIUpdate: CltuAborted" },
                                            { "set_ps            ICLTU_SIUpdate: ProductionStatusChange" },
                                            { "set_uls           ICLTU_SIUpdate: set uplink status" },
                                            { "print             print contetnts of the CLTU SI" },
                                            { "up                up to service element commanding" },
                                            { "                                                 " },
                                            { "bind        (u)   send CLTU-BIND operation" },
                                            { "unbind      (u)   send CLTU-UNBIND operation" },
                                            { "start       (u)   send CLTU-START operation" },
                                            { "stop        (u)   send CLTU-STOP operation" },
                                            { "td          (u)   send CLTU-TRANSFER-DATA operation" },
                                            { "an          (p)   send CLTU-ASYNC-NOTIFY operation" },
                                            { "ssr         (u)   send CLTU-SCHEDULE-STATUS-REPORT operation" },
                                            { "gp          (u)   send CLTU-GET-PARAMETER operation" },
                                            { "te          (u)   send CLTU-THROW-EVENT operation " },
                                            { "peer_abort (u/p)  send CLTU-PEER-ABORT operation" },
                                            { "auto_gen_td (u)   send CLTU-TRANSFER-DATA operations automatically" },
                                            { "buffer_empty      ICLTU_SIUpdate: BufferEmpty" }, // CHANGED-v2:
                                                                                                // new
                                            { "evt_proc_compl    ICLTU_SIUpdate: EventProcCompleted" }, // CHANGED-v2:
                                                                                                       // new
                                            { "set_nm            ICLTU_SIAdmin: Set_NotificationMode" }, // CHANGED-v2:
                                                                                                        // new
                                            { "set_acqsl         ICLTU_SIAdmin: set acquisition sequence length" },
                                            { "set_plop1_idle_sl ICLTU_SIAdmin: set plop1 idle sequence length" },
                                            { "set_pam           ICLTU_SIAdmin: set protocol abort mode" },
                                            { "set_cgv           ICLTU_SIAdmin: set clcw global vcid" },
                                            { "set_cpc           ICLTU_SIAdmin: set clcw physical channel" },
                                            { "set_mdt           ICLTU_SIAdmin: set minimum delay time" },
                                            { "set_mrc           ICLTU_SIAdmin: set minimum reporting cycle" },};


    public EE_SYSTST_CLTUSIClient(SLE_AppRole role, EE_SYSTST_TimeSource timeSource, UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdCltu, role, timeSource, utl);
        this.opGen = null;
        this.playback = false;
        this.lastRecCltuId = 0;
        this.currentCltuBufferSize = 0;
        this.stopThread = false;
        this.lgRadiated = 0;
        this.cltusiupd = null;
        ISLE_SIAdmin admsi = getSiAdmin();
        if (admsi != null)
        {
            this.cltusiupd = admsi.queryInterface(ICLTU_SIUpdate.class);
        }
    }

    @Override
    public EE_SYSTST_T_Component startUIF(boolean playback)
    {
        HRESULT rc = HRESULT.S_OK;
        T_CLTUCmd nextCommand = T_CLTUCmd.T_CLTUCmd_Max;

        this.playback = playback;

        EE_Reference<String> arg1 = new EE_Reference<String>();
        EE_Reference<String> arg2 = new EE_Reference<String>();
        EE_Reference<String> arg3 = new EE_Reference<String>();

        while (nextCommand != T_CLTUCmd.T_CLTUCmd_up)
        {
            nextCommand = getNextCommand(arg1, arg2, arg3);

            // -----------------------------------------------
            if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_bit_lock_req)
            {
                SLE_YesNo yn = SLE_YesNo.sleYN_No;
                if (arg1.getReference().equals("y"))
                {
                    yn = SLE_YesNo.sleYN_Yes;
                }

                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setBitLockRequired(yn);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_max_sldu_length)
            {
                int l = Integer.parseInt(arg1.getReference());
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setMaximumSlduLength(l);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_modulation_frequ)
            {
                int l = Integer.parseInt(arg1.getReference());
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setModulationFrequency(l);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_modulation_index)
            {
                int l = Integer.parseInt(arg1.getReference());
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setModulationIndex(l);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_plop_in_effect)
            {
                CLTU_PlopInEffect plop = CLTU_PlopInEffect.cltuPIE_invalid;
                if (arg1.getReference().equals("1"))
                {
                    plop = CLTU_PlopInEffect.cltuPIE_plop1;
                }
                else if (arg1.getReference().equals("2"))
                {
                    plop = CLTU_PlopInEffect.cltuPIE_plop2;
                }
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setPlopInEffect(plop);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_rf_avail_requ)
            {
                SLE_YesNo yn = SLE_YesNo.sleYN_No;
                if (arg1.getReference().equals("y"))
                {
                    yn = SLE_YesNo.sleYN_Yes;
                }
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setRfAvailableRequired(yn);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_sc_to_bitr_rat)
            {
                int divisor = Integer.parseInt(arg1.getReference());
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setSubcarrierToBitRateRatio(divisor);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_max_buffer_size)
            {
                int num = Integer.parseInt(arg1.getReference());
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setMaximumBufferSize(num);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_notification_mode)
            {
                // CHANGED-v2: added set_notification_mode
                CLTU_NotificationMode nm = CLTU_NotificationMode.getNotificationModeByCode(Integer.parseInt(arg1
                        .getReference()));
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setNotificationMode(nm);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_init_prod_status)
            {
                int ips = Integer.parseInt(arg1.getReference());
                CLTU_ProductionStatus ps = CLTU_ProductionStatus.getProductionStatusByCode(ips);
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setInitialProductionStatus(ps);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_init_ul_status)
            {
                int iuls = Integer.parseInt(arg1.getReference());
                CLTU_UplinkStatus uls = CLTU_UplinkStatus.getUplinkStatusByCode(iuls);
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setInitialUplinkStatus(uls);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_cltu_started)
            {
                HRESULT local = HRESULT.S_OK;
                ISLE_Time time = null;
                try
                {
                    time = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
                }
                catch (SleApiException e)
                {
                    local = e.getHResult();
                }
                if (time != null && local == HRESULT.S_OK)
                {
                    time.update();
                    ICLTU_SIUpdate upd = getCLTUSIUpdate();
                    if (upd != null)
                    {
                        upd.cltuStarted(this.lastRecCltuId, time, this.currentCltuBufferSize);
                    }
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_cltu_radiated)
            {
                boolean notify = false;
                if (arg1.getReference().equals("y"))
                {
                    notify = true;
                }
                HRESULT local = HRESULT.S_OK;
                ISLE_Time time = null;
                try
                {
                    time = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
                }
                catch (SleApiException e)
                {
                    local = e.getHResult();
                }
                if (time != null && local == HRESULT.S_OK)
                {
                    time.update();
                    ICLTU_SIUpdate upd = getCLTUSIUpdate();
                    if (upd != null)
                    {
                        upd.cltuRadiated(time, null, notify);
                    }
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_cltu_not_started)
            {
                int reason_i = Integer.parseInt(arg1.getReference());
                CLTU_Failure f = CLTU_Failure.getFailureByCode(reason_i);
                boolean notify = false;
                if (arg2.getReference().equals("y"))
                {
                    notify = true;
                }

                HRESULT res = HRESULT.S_OK;
                ICLTU_SIUpdate upd = getCLTUSIUpdate();
                if (upd != null)
                {
                    try
                    {
                        upd.cltuNotStarted(this.lastRecCltuId, f, this.currentCltuBufferSize, notify);
                    }
                    catch (SleApiException e)
                    {
                        res = e.getHResult();
                    }
                }
                displayResult(res);
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_buffer_empty)
            {
                // CHANGED-v2: added cltu_buffer_empty
                boolean notify = (arg1.getReference().equals("y") ? true : false);
                ICLTU_SIUpdate upd = getCLTUSIUpdate();
                if (upd != null)
                {
                    upd.bufferEmpty(notify);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_cltu_aborted)
            {               
                System.out.println("WARNING: cltu_aborted no longer supported");
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_event_proc_completed)
            {
                // CHANGED-v2: added event_proc_completed
                long invId = Long.parseLong(arg1.getReference());
                CLTU_EventResult evtRes = CLTU_EventResult.getEventResultByCode(Integer.parseInt(arg2.getReference()));
                boolean notify = arg3.getReference().equals("y") ? true : false;
                ICLTU_SIUpdate upd = getCLTUSIUpdate();
                if (upd != null)
                {
                    upd.eventProcCompleted(invId, evtRes, notify);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_prod_status_change)
            {
                int ps_i = Integer.parseInt(arg1.getReference());
                CLTU_ProductionStatus ps = CLTU_ProductionStatus.getProductionStatusByCode(ps_i);
                boolean notify = false;
                if (arg2.getReference().equals("y"))
                {
                    notify = true;
                }

                HRESULT res = HRESULT.S_OK;
                ICLTU_SIUpdate upd = getCLTUSIUpdate();
                if (upd != null)
                {
                    try
                    {
                        upd.productionStatusChange(ps, this.currentCltuBufferSize, notify);
                    }
                    catch (SleApiException e)
                    {
                        res = e.getHResult();
                    }
                }
                displayResult(res);
            }

            // ----------------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_uplink_status)
            {
                int uls_i = Integer.parseInt(arg1.getReference());
                CLTU_UplinkStatus uls = CLTU_UplinkStatus.getUplinkStatusByCode(uls_i);
                ICLTU_SIUpdate upd = getCLTUSIUpdate();
                if (upd != null)
                {
                    upd.setUplinkStatus(uls);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_print_si)
            {
                printSI();
            }

            // -----------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_dummy1 || nextCommand == T_CLTUCmd.T_CLTUCmd_dummy2)
            {
                // do nothing
            }

            // -----------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_bind || nextCommand == T_CLTUCmd.T_CLTUCmd_unbind
                     || nextCommand == T_CLTUCmd.T_CLTUCmd_start || nextCommand == T_CLTUCmd.T_CLTUCmd_stop
                     || nextCommand == T_CLTUCmd.T_CLTUCmd_transfer_data
                     || nextCommand == T_CLTUCmd.T_CLTUCmd_async_notify || nextCommand == T_CLTUCmd.T_CLTUCmd_ssr
                     || nextCommand == T_CLTUCmd.T_CLTUCmd_get_prm || nextCommand == T_CLTUCmd.T_CLTUCmd_throw_event
                     || nextCommand == T_CLTUCmd.T_CLTUCmd_peer_abort)
            {
                IUnknown piuk = this.siAdmin.queryInterface(IUnknown.class);
                ISLE_Operation op = getOpGen().createOp(getOpType(nextCommand), this.eventQueue, piuk);

                if (op != null)
                {
                    boolean reset_seq_counter = false;

                    String ir = "i";
                    if (op.isConfirmed() == true)
                    {
                        EE_Reference<String> what = new EE_Reference<String>();
                        what.setReference("");
                        System.out.println("Role: ");
                        if (nextCommand == T_CLTUCmd.T_CLTUCmd_bind)
                        {
                            System.out.println("i=Invocation  r=Return  is=Invocation without reset sequence counter");
                            System.out.println("rs=Return with reset sequence counter");
                            while (!(what.getReference().equals("i") || what.getReference().equals("r")
                                     || what.getReference().equals("is") || what.getReference().equals("rs")))
                            {
                                utl.read(what, playback);
                                if (what.getReference().equals("i") || what.getReference().equals("rs"))
                                {
                                    reset_seq_counter = true;
                                }
                                else if (what.getReference().equals("is"))
                                {
                                    what.setReference("i");
                                }
                            }
                        }
                        else
                        {
                            System.out.println("i=Invocation  r=Return");
                            while (!(what.getReference().equals("i") || what.getReference().equals("r")))
                            {
                                utl.read(what, playback);
                            }
                        }
                        ir = what.getReference();
                    }

                    if (this.srvInit != null)
                    {
                        if (ir.equals("i"))
                        {
                            if (reset_seq_counter == true)
                            {
                                this.seqCounter = 1;
                            }
                            System.out.println("Send Invoke Operation. Seq " + this.seqCounter);

                            try
                            {
                                this.srvInit.initiateOpInvoke(op, this.seqCounter++);
                            }
                            catch (SleApiException e)
                            {
                                rc = e.getHResult();
                            }

                            if (nextCommand == T_CLTUCmd.T_CLTUCmd_peer_abort)
                            {
                                if (this.eventQueue != null)
                                {
                                    try
                                    {
                                        this.eventQueue.flushQueue(piuk);
                                    }
                                    catch (SleApiException e)
                                    {
                                        rc = e.getHResult();
                                    }
                                    System.out.println("FlushQueue the DCW Event Queue return " + rc);
                                }
                            }
                        }
                        else
                        {
                            if (reset_seq_counter == true)
                            {
                                this.seqCounter = 1;
                            }
                            ISLE_ConfirmedOperation cop = (ISLE_ConfirmedOperation) (op);
                            System.out.println("Send Return Operation. Seq " + this.seqCounter);
                            try
                            {
                                this.srvInit.initiateOpReturn(cop, this.seqCounter++);
                            }
                            catch (SleApiException e)
                            {
                                rc = e.getHResult();
                            }
                        }
                        displayResult(rc);
                        if (rc != HRESULT.S_OK)
                        {
                            String tmp = op.print(512);
                            System.out.println(tmp);
                        }
                    }
                    else
                    {
                        System.out.println("No ServiceInitiate interface yet available");
                    }
                }
                else
                {
                    System.out.println("Could not get operation object");
                }

            } // end if (next Comman == send operation, ...)

            // -----------------------------------------------
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_auto_gen_td)
            {
                rc = autoSendTD();
                displayResult(rc);
            }
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_acquisition_seq_length)
            {
                int l = Integer.parseInt(arg1.getReference());
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setAcquisitionSequenceLength(l);
                }
            }
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_plop1_idle_seq_length)
            {
                int l = Integer.parseInt(arg1.getReference());
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setPlop1IdleSequenceLength(l);
                }
            }
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_protocol_abort_mode)
            {
                CLTU_ProtocolAbortMode pam = CLTU_ProtocolAbortMode.cltuPAM_abort;
                if (arg1.equals("continue"))
                {
                    pam = CLTU_ProtocolAbortMode.cltuPAM_continue;
                }
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setProtocolAbortMode(pam);
                }
            }
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_clcw_global_vcid)
            {
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                CLTU_GvcId id = new CLTU_GvcId();
                if (adm != null)
                {
                    EE_Reference<String> arg = new EE_Reference<String>();
                    System.out.println("Type (0=Master,1=Virtual): ");
                    utl.read(arg, this.playback);
                    int type_i = Integer.parseInt(arg.getReference());
                    id.setType((CLTU_ChannelType.getChannelTypeByCode(type_i)));

                    System.out.println("SCID (0-1023): ");
                    utl.read(arg, this.playback);
                    int sc_i = Integer.parseInt(arg.getReference());
                    id.setScid(sc_i);

                    System.out.println("Version (0-1): ");
                    utl.read(arg, this.playback);
                    int ver_i = Integer.parseInt(arg.getReference());
                    id.setVersion(ver_i);

                    System.out.println("VC ID  (0-63): ");
                    utl.read(arg, this.playback);
                    int vc_i = Integer.parseInt(arg.getReference());
                    id.setVcid(vc_i);
                }
                CLTU_ClcwGvcId cgvcid = new CLTU_ClcwGvcId(id);

                adm.setClcwGlobalVcid(cgvcid);
            }
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_clcw_physical_channel)
            {
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setClcwPhysicalChannel(new CLTU_ClcwPhysicalChannel(arg1.getReference()));
                }
            }
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_minimum_delay_time)
            {
                int l = Integer.parseInt(arg1.getReference());
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setMinimumDelayTime(l);
                }
            }
            else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_minimum_reporting_cycle)
            {
                int l = Integer.parseInt(arg1.getReference());
                ICLTU_SIAdmin adm = getCLTUSIAdmin();
                if (adm != null)
                {
                    adm.setMinimumReportingCycle(l);
                }
            }
        } // end while nextCommand != T_CLTUCmd_up

        return EE_SYSTST_T_Component.eeEE_SYSTST_TestSE;

    }

    @Override
    public void informOpInvoke(ISLE_Operation poperation, long seqCount) throws SleApiException
    {
        setUpLastprocessed(poperation);
        super.informOpInvoke(poperation, seqCount);
    }

    public void setUpLastprocessed(ISLE_Operation poperation)
    {

        SLE_OpType ot = poperation.getOperationType();
        if (ot == SLE_OpType.sleOT_start)
        {
            this.lastRecCltuId = 0;

            ICLTU_SIAdmin adm = getCLTUSIAdmin();
            if (adm != null)
            {
                this.currentCltuBufferSize = adm.getMaximumBufferSize();
            }
            else
            {
                this.currentCltuBufferSize = 1024;
            }

        }
        else if (ot == SLE_OpType.sleOT_transferData)
        {
            ICLTU_TransferData td = (ICLTU_TransferData) poperation;
            final byte[] data = td.getData();
            int length = data.length;
            if (this.currentCltuBufferSize >= length)
            {
                this.currentCltuBufferSize = this.currentCltuBufferSize - length;
            }
            else
            {
                this.currentCltuBufferSize = 0;
            }

            this.lastRecCltuId = td.getCltuId();

            System.out.println("Setup CLTU last processed. ExpectedCltuId " + (this.lastRecCltuId + 1)
                               + " BufferAvailable " + this.currentCltuBufferSize);

            td.setExpectedCltuId(this.lastRecCltuId + 1);
            td.setCltuBufferAvailable(this.currentCltuBufferSize);

        }
        else if (ot == SLE_OpType.sleOT_throwEvent)
        {
            ICLTU_ThrowEvent te = (ICLTU_ThrowEvent) poperation;
            // set expected event invocation id
            long evId = te.getEventInvocationId();
            te.setExpectedEventInvocationId(evId + 1);

            System.out.println("Setup CLTU last processed. ExpectedCltuId " + (evId + 1));
        }
    }

    @Override
    public EE_SYSTST_OpGen getOpGen()
    {
        if (this.opGen != null)
        {
            return this.opGen;
        }
        else
        {
            ISLE_SIOpFactory f = this.siAdmin.queryInterface(ISLE_SIOpFactory.class);
            this.opGen = new EE_SYSTST_CLTUOpGen(this.opFactory, f, this.playback, this.utl);

            // #hd# shift setting the version to this place
            int vNum = this.siAdmin.getVersion();
            this.opGen.setVersion(vNum);
        }
        return this.opGen;
    }

    @Override
    public void testTdSend(long lg, int nbtime, long delay_td, byte[] tdData, int delay)
    {
        EE_Reference<String> produce_notif_all = new EE_Reference<String>();
        System.out.print("Produce Noficiation for All cltu ? (y/n) : ");
        utl.read(produce_notif_all, this.playback);

        ISLE_Operation op = null;
        ICLTU_TransferData optdcltu = null;
        ICLTU_AsyncNotify opasncltu = null;

        EE_Reference<DCW_Event_Type> et = new EE_Reference<DCW_Event_Type>();
        EE_Reference<IUnknown> psi1 = new EE_Reference<IUnknown>();
        EE_Reference<ISLE_Operation> pop = new EE_Reference<ISLE_Operation>();

        ISLE_Time time = null;
        ISLE_Time time1 = null;
        try
        {
            time = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
            time1 = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }

        time1.update();

        byte[] ptmp = time1.getCDS();
        String ascii = EE_GenStrUtil.convAscii(ptmp, 8);
        System.out.println("time=" + ascii);

        EE_SYSTST_OpGen pOpGen = getOpGen();
        int count = 0;

        double lgcltu_tot = 0;
        double rate;
        boolean do_print = true;
        int cltuid = 1;
        if (nbtime > 0)
        {
            nbtime++;
        }

        this.currentCltuBufferSize = 1024;

        while (true)
        {
            count = 0;

            // send transfer data
            while (true)
            {

                if (this.currentCltuBufferSize < lg)
                {
                    System.out.println("Not enought buffer available. " + this.currentCltuBufferSize + " " + lg);
                    break;
                }

                if (nbtime >= 0)
                {
                    HRESULT rc = HRESULT.S_OK;

                    // create the transfer data op
                    try
                    {
                        op = pOpGen.siOPF.createOperation(ISLE_Operation.class, SLE_OpType.sleOT_transferData);
                    }
                    catch (SleApiException e)
                    {
                        rc = e.getHResult();
                    }
                    if (rc == HRESULT.S_OK)
                    {
                        ICLTU_TransferData td = (ICLTU_TransferData) op;

                        if (produce_notif_all.equals("y"))
                        {
                            td.setRadiationNotification(SLE_SlduStatusNotification.sleSN_produceNotification);
                        }
                        else
                        {
                            if (nbtime > 2)
                            {
                                td.setRadiationNotification(SLE_SlduStatusNotification.sleSN_doNotProduceNotification);
                            }
                            else
                            {
                                td.setRadiationNotification(SLE_SlduStatusNotification.sleSN_produceNotification);
                            }
                        }
                        td.setDelayTime(delay_td);
                        td.setCltuId(cltuid);
                        td.setData(tdData);
                    }
                    lgcltu_tot += lg;
                    // if ((_seqCounter % 10) == 0)
                    do_print = true;
                    // else
                    // do_print = false;

                    if (do_print)
                    {
                        System.out.print("Send TD Op. Seq " + this.seqCounter + " CltuId " + cltuid + ". lg " + lg);
                    }

                    rc = HRESULT.S_OK;
                    try
                    {
                        this.srvInit.initiateOpInvoke(op, this.seqCounter++);
                    }
                    catch (SleApiException e)
                    {
                        rc = e.getHResult();
                    }

                    if (rc == HRESULT.SLE_E_SUSPENDED)
                    {
                        if (do_print)
                        {
                            System.out.println(" : E_SUS");
                        }
                        break;

                    }
                    else if (rc == HRESULT.SLE_S_SUSPEND)
                    {
                        cltuid++;
                        if (do_print)
                        {
                            System.out.println(" : S_SUS");
                        }
                        if (nbtime > 0)
                        {
                            nbtime--;
                            if (nbtime == 1)
                            {
                                nbtime = -1;
                            }
                        }
                        break;

                    }
                    else if (rc == HRESULT.S_OK)
                    {
                        if (do_print)
                        {
                            System.out.println(" : S_OK");
                        }
                        cltuid++;
                        if (nbtime > 0)
                        {
                            nbtime--;
                            if (nbtime == 1)
                            {
                                nbtime = -1;
                            }
                        }
                        break;

                    }
                    else
                    {
                        System.out.println(" : " + rc);
                        break;
                    }

                }
                else
                {
                    if (nbtime == -1)
                    {
                        time.update();
                        double diff_time = time.subtract(time1);
                        System.out.println("-----------------------------------------------");
                        System.out.println("Last TD op sent with cltuid=" + (cltuid - 1) + " after " + diff_time
                                           + " seconds");
                        if (diff_time > 10)
                        {

                            rate = (lgcltu_tot * 8) / 1024;
                            System.out.println(rate + " K bits sent in " + diff_time + " s -->" + (rate / diff_time)
                                               + " K bits / s");
                        }
                        nbtime = -2;
                    }
                    break;
                }
            }
            count = 0;

            // wait
            while (true)
            {
                HRESULT res = HRESULT.S_OK;
                try
                {
                    this.eventQueue.nextEvent(et, psi1, pop, 0, delay);
                }
                catch (SleApiException e)
                {
                    res = e.getHResult();
                }
                if (res == HRESULT.S_OK)
                {
                    if (et.getReference() == DCW_Event_Type.dcwEVT_protocolAbort)
                    {
                        rcvProtocolAbort();
                        break;
                    }
                    else if (et.getReference() == DCW_Event_Type.dcwEVT_resumeDataTransfer)
                    {
                        // cout << "Rcv Resume DT" << endl;
                        // break;
                    }
                    else if (et.getReference() == DCW_Event_Type.dcwEVT_provisionPeriodEnds)
                    {
                        rcvProvisionPeriodEnds();
                        break;
                    }
                    else if (et.getReference() == DCW_Event_Type.dcwEVT_informOpReturn)
                    {

                        if (pop.getReference().getOperationType() == SLE_OpType.sleOT_transferData)
                        {

                            optdcltu = pop.getReference().queryInterface(ICLTU_TransferData.class);
                            @SuppressWarnings("unused")
                            byte[] data1 = optdcltu.getData();
                            this.currentCltuBufferSize = optdcltu.getCltuBufferAvailable();

                            if (do_print)
                            {
                                System.out.println("Rcv TD Rtn Op. CltuId " + optdcltu.getCltuId() + " Buff "
                                                   + this.currentCltuBufferSize);
                            }
                            if ((nbtime == -2) && (optdcltu.getCltuId() == cltuid - 1))
                            {
                                time.update();
                                double diff_time = time.subtract(time1);
                                System.out.println("-----------------------------------------------");
                                System.out.println("TD Rtn Op rcv. cltuid " + optdcltu.getCltuId() + " after "
                                                   + diff_time + " seconds");
                                if (diff_time > 10)
                                {
                                    rate = ((lgcltu_tot * 8) / 1024);
                                    System.out.println(rate + " K bits sent and confirmed in " + diff_time + " s"
                                                       + " --> " + (rate / diff_time) + " K bits/s");
                                }
                            }

                        }
                        else
                        {
                            System.out.println("Rcv " + pop.getReference().getOperationType() + " Rtn Op");
                        }

                    }
                    else if (et.getReference() == DCW_Event_Type.dcwEVT_informOpInvoke)
                    {
                        switch (pop.getReference().getOperationType())
                        {
                        case sleOT_asyncNotify:
                        {
                            opasncltu = pop.getReference().queryInterface(ICLTU_AsyncNotify.class);

                            if (opasncltu.getNotificationType() == CLTU_NotificationType.cltuNT_bufferEmpty)
                            {
                                System.out.println("Rcv Async Notify Op. Cltu " + opasncltu.getCltuLastOk() + " "
                                                   + opasncltu.getNotificationType());

                            }
                            else if (opasncltu.getNotificationType() == CLTU_NotificationType.cltuNT_cltuRadiated)
                            {

                                if ((opasncltu.getCltuLastOk() == cltuid - 1) && (nbtime == -2))
                                {
                                    time.update();
                                    double diff_time = time.subtract(time1);
                                    System.out.println("-----------------------------------------------");
                                    System.out.println("Cltu Radiated rcv. cltu_last_ok=" + opasncltu.getCltuLastOk()
                                                       + " after " + diff_time + " seconds");
                                    if (diff_time > 10)
                                    {
                                        rate = ((lgcltu_tot * 8) / 1024);
                                        System.out.println(rate + " K bits sent, confirmed and radiated in "
                                                           + diff_time + " s" + " --> " + (rate / diff_time)
                                                           + " K bits/s");
                                        System.out.println("-----------------------------------------------");
                                    }
                                    return;
                                }
                                else
                                {
                                    System.out.println("Rcv Async Notify Op. Cltu " + opasncltu.getCltuLastOk() + " "
                                                       + opasncltu.getNotificationType());
                                }
                            }
                            else
                            {
                                System.out.println("Rcv Async Notify Op. Cltu " + opasncltu.getCltuLastOk() + " "
                                                   + opasncltu.getNotificationType());
                            }

                            break;
                        }
                        case sleOT_peerAbort:
                        {
                            System.out.println("Rcv " + pop.getReference().getOperationType() + " Inv Op");
                            String tmp = pop.getReference().print(512);
                            System.out.println(tmp);
                            break;
                        }
                        default:
                        {
                            System.out.println("Rcv " + pop.getReference().getOperationType() + " Inv Op");
                            break;
                        }
                        }
                    }
                }
                else
                {
                    System.out.println("Next Event fail");
                    return;
                }

                count++;
                if (count == 10)
                {
                    return;
                }
            }
        }
    }

    @Override
    public void testTdReceive(long lg, int nbtime)
    {

        ICLTU_TransferData optdcltu = null;

        EE_Reference<DCW_Event_Type> et = new EE_Reference<DCW_Event_Type>();
        EE_Reference<IUnknown> psi1 = new EE_Reference<IUnknown>();
        EE_Reference<ISLE_Operation> pop = new EE_Reference<ISLE_Operation>();

        int to = 10;

        int cltuid = 1;

        boolean do_print = true;

        ISLE_Time time = null;
        ISLE_Time time1 = null;
        HRESULT res = HRESULT.S_OK;
        try
        {
            time = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
            time1 = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (res == HRESULT.S_OK)
        {
            time1.update();
        }

        if (nbtime > 0)
        {
            nbtime++;
        }

        ICLTU_SIAdmin adm = getCLTUSIAdmin();
        if (adm != null)
        {
            this.currentCltuBufferSize = adm.getMaximumBufferSize();
        }
        else
        {
            this.currentCltuBufferSize = 1024;
        }

        // wait for data transfer op
        while (true)
        {
            res = HRESULT.S_OK;
            try
            {
                this.eventQueue.nextEvent(et, psi1, pop, to, 0);
            }
            catch (SleApiException e)
            {
                res = e.getHResult();
            }
            if (res == HRESULT.S_OK)
            {

                if (et.getReference() == DCW_Event_Type.dcwEVT_protocolAbort)
                {
                    rcvProtocolAbort();
                    break;
                }
                else if (et.getReference() == DCW_Event_Type.dcwEVT_resumeDataTransfer)
                {
                    rcvResumeDataTransfer();
                    break;
                }
                else if (et.getReference() == DCW_Event_Type.dcwEVT_provisionPeriodEnds)
                {
                    rcvProvisionPeriodEnds();
                    break;
                }
                else if (et.getReference() == DCW_Event_Type.dcwEVT_informOpReturn)
                {
                    System.out.println("Rcv " + pop.getReference().getOperationType() + " Rtn Op");
                }
                else if (et.getReference() == DCW_Event_Type.dcwEVT_informOpInvoke)
                {
                    if (pop.getReference().getOperationType() == SLE_OpType.sleOT_transferData)
                    {

                        optdcltu = pop.getReference().queryInterface(ICLTU_TransferData.class);
                        byte[] data1;
                        data1 = optdcltu.removeData();

                        if ((this.seqCounter % 100) == 0)
                        {
                            do_print = true;
                        }
                        else
                        {
                            do_print = false;
                        }

                        int lg1 = data1.length;
                        if (do_print)
                        {
                            System.out.println("Receive TD Inv Op. CltuId " + optdcltu.getCltuId() + ". lg " + lg1);
                        }

                        if (lg != lg1)
                        {
                            System.out.println("length is not correct. " + lg + " " + lg1);
                            return;
                        }

                        if (cltuid != optdcltu.getCltuId())
                        {
                            System.out.println("CLTU Id not the expected one !!");
                        }

                        if (this.currentCltuBufferSize >= lg1)
                        {
                            this.currentCltuBufferSize = this.currentCltuBufferSize - lg1;
                        }
                        else
                        {
                            this.currentCltuBufferSize = 0;
                        }

                        cltuid++;
                        optdcltu.setExpectedCltuId(cltuid);
                        optdcltu.setCltuBufferAvailable(this.currentCltuBufferSize);

                        if (do_print)
                        {
                            System.out.print("Send Rtn Op. Seq " + this.seqCounter + ". lg " + lg1);
                        }

                        HRESULT rc = HRESULT.S_OK;
                        try
                        {
                            this.srvInit.initiateOpReturn(optdcltu, this.seqCounter++);
                        }
                        catch (SleApiException e)
                        {
                            rc = e.getHResult();
                        }

                        if (do_print)
                        {
                            System.out.println(" : " + rc);
                        }

                        if (optdcltu.getRadiationNotification() == SLE_SlduStatusNotification.sleSN_produceNotification)
                        {
                            // send a cltu_radiated
                            time.update();
                            ISLE_SIAdmin admsi = getSiAdmin();
                            if (admsi != null)
                            {
                                ICLTU_SIUpdate cltusiupd = admsi.queryInterface(ICLTU_SIUpdate.class);
                                if (cltusiupd != null)
                                {
                                    @SuppressWarnings("unused")
                                    ICLTU_SIAdmin admcltusi = (ICLTU_SIAdmin) admsi;
                                    cltusiupd.cltuStarted(optdcltu.getCltuId(), time, this.currentCltuBufferSize);
                                    // done in separate thread
                                    // time->Update();
                                    // _currentCltuBufferSize += lg1;
                                    // cltusiupd->CltuRadiated(*time, true);
                                    this.lgRadiated = lg1;
                                    this.lastCltu = (int) optdcltu.getCltuId();
                                    signalCondVarNotification();

                                }
                            }
                        }
                        else
                        {
                            this.currentCltuBufferSize += lg1;
                        }

                    }
                    else
                    {
                        System.out.println("Rcv " + pop.getReference().getOperationType() + " Inv Op");
                        if (pop.getReference().getOperationType() == SLE_OpType.sleOT_peerAbort)
                        {
                            this.stopThread = true;
                            return;
                        }
                    }

                    if (nbtime > 0)
                    {
                        nbtime--;
                        if (nbtime == 1)
                        {
                            this.stopThread = true;
                            return;
                        }
                    }

                }
                else
                {
                    System.out.println("Rcv unknown event from DCW !!");
                }
            }
            else
            {
                System.out.println("Next Event fail");
                this.stopThread = true;
                return;
            }
        }
        this.stopThread = true;
    }

    private void signalCondVarNotification()
    {
        this.eeCondVarNotification.lock();
        ;
        this.eeCondVarNotification.signalAll();
        this.eeCondVarNotification.unlock();
    }

    private T_CLTUCmd getNextCommand(EE_Reference<String> arg1, EE_Reference<String> arg2, EE_Reference<String> arg3)
    {
        T_CLTUCmd nextCommand = T_CLTUCmd.T_CLTUCmd_Max;
        EE_Reference<String> cmd = new EE_Reference<String>();
        cmd.setReference("");
        while (nextCommand == T_CLTUCmd.T_CLTUCmd_Max)
        {
            prompt();
            if (utl.read(cmd, this.playback) == false)
            {
                nextCommand = T_CLTUCmd.T_CLTUCmd_up;
                break;
            }
            for (int i = 0; i < T_CLTUCmd.T_CLTUCmd_Max.getCode(); i++)
            {
                if (T_CLTUCmd.getCLTUCmdByCode(i).toString().equals(cmd.getReference()))
                {
                    nextCommand = T_CLTUCmd.getCLTUCmdByCode(i);
                    break;
                }
            }
            if (nextCommand == T_CLTUCmd.T_CLTUCmd_Max)
            {
                // command not found, go to base-class
                processSLECommand(cmd.getReference());
                cmd.setReference("");
            }
        } // end while
          // ----------------------------------------------------

        if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_bit_lock_req)
        {
            System.out.print("Bit lock required (y/n): ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_max_sldu_length)
        {
            System.out.print("Max Sldu length: ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_modulation_frequ)
        {
            System.out.print("Modulation freq: ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_modulation_index)
        {
            System.out.print("Modulation index: ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_notification_mode)
        {
            System.out.print("Notification mode (0: deferred, 1:immediate, -1:invalid): ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_plop_in_effect)
        {
            System.out.print("Plop in effect (1/2/invalid): ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_rf_avail_requ)
        {
            System.out.print("Rf avail required (y/n): ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_sc_to_bitr_rat)
        {
            System.out.print("Subcarrier to bitrate ratio: ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_max_buffer_size)
        {
            System.out.print("Maximum buffer size: ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_init_prod_status)
        {
            System.out.println("Initial Production status:");
            arg1.setReference(readProductionStatus());
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_init_ul_status)
        {
            System.out.println("Initial Uplink status:");
            arg1.setReference(readUplinkStatus());
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_cltu_radiated)
        {
            System.out.print("Notify (y/n): ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_cltu_not_started)
        {
            System.out.print("Failure reason: (0=expired, 1=interrupted): ");
            utl.read(arg1, this.playback);
            System.out.print("Notify (y/n): ");
            utl.read(arg2, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_cltu_aborted)
        {
            // CHANGED-v2: cltu_aborted removed
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_buffer_empty)
        {
            // CHANGED-v2: added buffer_empty
            System.out.print("Notify (y/n): ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_event_proc_completed)
        {
            // CHANGED-v2: added event_proc_completed
            System.out.print("Invocation Id: ");
            utl.read(arg1, this.playback);
            System.out.print("Event Result: (0=completed, 1=notCompleted, 2=conditionFalse): ");
            utl.read(arg2, this.playback);
            System.out.print("Notify (y/n): ");
            utl.read(arg3, this.playback);
        }

        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_prod_status_change)
        {
            arg1.setReference(readProductionStatus());
            System.out.print("Notify (y/n): ");
            utl.read(arg2, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_uplink_status)
        {
            arg1.setReference(readUplinkStatus());
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_acquisition_seq_length)
        {
            System.out.print("acquisition sequence length : ");
            utl.read(arg1, this.playback);
        }

        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_plop1_idle_seq_length)
        {
            System.out.print("plop1 idle sequence length : ");
            utl.read(arg1, this.playback);
        }

        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_protocol_abort_mode)
        {
            System.out.print("protocol abort mode : ");
            utl.read(arg1, this.playback);
        }

        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_notification_mode)
        {
            System.out.print("notification mode : ");
            utl.read(arg1, this.playback);
        }

        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_clcw_global_vcid)
        {
            System.out.print("clcw global vcid : ");
            // UTL.read( arg1 , this.playback);
        }

        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_clcw_physical_channel)
        {
            System.out.print("clcw physical channel : ");
            utl.read(arg1, this.playback);
        }

        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_minimum_delay_time)
        {
            System.out.print("Minimum delay Time : ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_CLTUCmd.T_CLTUCmd_set_minimum_reporting_cycle)
        {
            System.out.print("Minimum reporting cycle : ");
            utl.read(arg1, this.playback);
        }
        return nextCommand;
    }

    private String readProductionStatus()
    {
        EE_Reference<String> arg = new EE_Reference<String>();
        boolean isOk = false;

        while (isOk == false)
        {
            System.out.println("Production status: (0=operational, 1=configured, ");
            System.out.print("                    2=interrupted, 3=halted) ");
            utl.read(arg, this.playback);

            int ls = Integer.parseInt(arg.getReference());
            if (ls < 0 || ls > 3)
            {
                System.out.println("Invalid Production Status value");
            }
            else
            {
                isOk = true;
            }
        }

        return arg.getReference();
    }

    private String readUplinkStatus()
    {
        EE_Reference<String> arg = new EE_Reference<String>();
        boolean isOk = false;

        while (isOk == false)
        {
            System.out.println("Uplink status: (0=not-available, 1=no-Rf-available, ");
            System.out.print("                2=no-bit-lock,   3=nominal) ");
            utl.read(arg, this.playback);

            int ls = Integer.parseInt(arg.getReference());
            if (ls < 0 || ls > 3)
            {
                System.out.println("Invalid Uplink Status value");
            }
            else
            {
                isOk = true;
            }
        }

        return arg.getReference();
    }

    private ICLTU_SIAdmin getCLTUSIAdmin()
    {
        ICLTU_SIAdmin ia = this.siAdmin.queryInterface(ICLTU_SIAdmin.class);
        if (ia == null)
        {
            System.out.println("Interface ICLTU_SIAdmin not available");
        }
        return ia;

    }

    private ICLTU_SIUpdate getCLTUSIUpdate()
    {
        if (this.cltusiupd == null)
        {
            ISLE_SIAdmin admsi = getSiAdmin();
            if (admsi != null)
            {
                this.cltusiupd = admsi.queryInterface(ICLTU_SIUpdate.class);
            }
            if (this.cltusiupd == null)
            {
                System.out.println("Interface ICLTU_SIUpdate not available");
            }
        }
        return this.cltusiupd;
    }

    private SLE_OpType getOpType(T_CLTUCmd cmd)
    {
        if (cmd == T_CLTUCmd.T_CLTUCmd_bind)
        {
            return SLE_OpType.sleOT_bind;
        }
        else if (cmd == T_CLTUCmd.T_CLTUCmd_unbind)
        {
            return SLE_OpType.sleOT_unbind;
        }
        else if (cmd == T_CLTUCmd.T_CLTUCmd_start)
        {
            return SLE_OpType.sleOT_start;
        }
        else if (cmd == T_CLTUCmd.T_CLTUCmd_stop)
        {
            return SLE_OpType.sleOT_stop;
        }
        else if (cmd == T_CLTUCmd.T_CLTUCmd_transfer_data)
        {
            return SLE_OpType.sleOT_transferData;
        }
        else if (cmd == T_CLTUCmd.T_CLTUCmd_async_notify)
        {
            return SLE_OpType.sleOT_asyncNotify;
        }
        else if (cmd == T_CLTUCmd.T_CLTUCmd_ssr)
        {
            return SLE_OpType.sleOT_scheduleStatusReport;
        }
        else if (cmd == T_CLTUCmd.T_CLTUCmd_get_prm)
        {
            return SLE_OpType.sleOT_getParameter;
        }
        else if (cmd == T_CLTUCmd.T_CLTUCmd_throw_event)
        {
            return SLE_OpType.sleOT_throwEvent;
        }
        else if (cmd == T_CLTUCmd.T_CLTUCmd_peer_abort)
        {
            return SLE_OpType.sleOT_peerAbort;
        }
        else
        {
            return SLE_OpType.sleOT_bind; // no better idea
        }

    }

    private HRESULT autoSendTD()
    {
        HRESULT rc = HRESULT.S_OK;
        IUnknown piuk = this.siAdmin.queryInterface(IUnknown.class);
        ISLE_Operation op = getOpGen().createOp(SLE_OpType.sleOT_transferData, this.eventQueue, piuk);
        if (op != null)
        {
            ICLTU_TransferData td = (ICLTU_TransferData) op;

            System.out.print("How many TD operations ? ");
            EE_Reference<String> n = new EE_Reference<String>();
            utl.read(n, this.playback);
            int i = Integer.parseInt(n.getReference());

            long theId = td.getCltuId();

            for (int j = 0; j < i; j++)
            {
                ICLTU_TransferData tds = (ICLTU_TransferData) td.copy();
                tds.setCltuId(theId);
                System.out.println("Send Invoke Operation. Seq " + this.seqCounter);
                try
                {
                    this.srvInit.initiateOpInvoke(tds, this.seqCounter++);
                }
                catch (SleApiException e)
                {
                    rc = e.getHResult();
                }
                if (rc != HRESULT.S_OK)
                {
                    return rc;
                }
                theId++;
            }
        }
        else
        {
            return HRESULT.E_FAIL;
        }

        return HRESULT.S_OK;
    }

    @Override
    public void help()
    {
        super.help();

        for (int i = 0; i < T_CLTUCmd.T_CLTUCmd_Max.getCode(); i++)
        {
            System.out.println("   " + this.helpCommand[i]);
        }
        System.out.println();
    }

    @Override
    public void printSI()
    {
        super.printSI();

        ICLTU_SIAdmin adm = getCLTUSIAdmin();
        if (adm != null)
        {
            SLE_YesNo yn = adm.getBitLockRequired();
            System.out.println("Bit Lock required: " + yn);

            long ml = adm.getMaximumSlduLength();
            System.out.println("Max Sldu length  : " + ml);
            
            System.out.println("Min report cycle : " + adm.getMinimumReportingCycle());

            long mf = adm.getModulationFrequency();
            System.out.println("Modulation Frequ : " + mf);

            int mi = adm.getModulationIndex();
            System.out.println("Modulation Index : " + mi);

            CLTU_NotificationMode nm = adm.getNotificationMode();
            System.out.println("Notification Mode: " + nm);

            CLTU_PlopInEffect pl = adm.getPlopInEffect();
            System.out.println("Plop in effect   : " + pl);

            yn = adm.getRfAvailableRequired();
            System.out.println("Rf avail required: " + yn);

            int dv = adm.getSubcarrierToBitRateRatio();
            System.out.println("SC to BR Ratio   : " + dv);

            long bs = adm.getMaximumBufferSize();
            System.out.println("Max buffer size  : " + bs);

            int asl = adm.getAcquisitionSequenceLength();
            System.out.println("Acq. Seq. Length : " + asl);

            int pisl = adm.getPlop1IdleSequenceLength();
            System.out.println("Plop1 Idle Seq. Length : " + pisl);

            CLTU_ProtocolAbortMode pam = adm.getProtocolAbortMode();
            System.out.println("Protocol Abort Mode : " + pam.toString());

            CLTU_ClcwGvcId cgv = adm.getClcwGlobalVcid();
            if (cgv == null || cgv.getConfigType() == CLTU_ConfType.cltuCT_notConfigured)
            {
                System.out.println("Clcw Global VCID: Not initialised");
            }
            else
            {
            	CLTU_GvcId id = cgv.getCltuGvcId();
                System.out.println("Clcw Global VCID: ");
                System.out.println(" type = " + id.getType().toString() + ", ");
                System.out.println(" scId = " + id.getScid() + ", ");
                System.out.println(" version = " + id.getVersion() + ", ");
                System.out.println(" vcId = " + id.getVcid());
            }
            
            CLTU_ClcwPhysicalChannel cltuPC = adm.getClcwPhysicalChannel();
            if (cltuPC == null)
            {
            	System.out.println("Clcw Physical Channel   : Not initialised");
            }
            else
            {
            	String cpc = cltuPC.getCltuPhyChannel();
            	if (cpc == null || cpc.isEmpty())
            	{
            		System.out.println("Clcw Physical Channel   : Not initialised");
            	}
            	else
            	{
            		System.out.println("Clcw Physical Channel   : " + cpc);
            	}
            }

            long mdt = adm.getMinimumDelayTime();
            System.out.println("Minimum Delay Time   : " + mdt);

            System.out.println();
        }

        ICLTU_SIUpdate upd = getCLTUSIUpdate();
        if (upd != null)
        {
            CLTU_ProductionStatus ps = upd.getProductionStatus();
            System.out.println("Production status: " + ps);

            long bs = upd.getCltuBufferAvailable();
            System.out.println("Avail buffer size: " + bs);

            long nrec = upd.getNumberOfCltusReceived();
            System.out.println("CLTUs received   : " + nrec);

            long nproc = upd.getNumberOfCltusProcessed();
            System.out.println("CLTUs processed  : " + nproc);

            long nrad = upd.getNumberOfCltusRadiated();
            System.out.println("CLTUs radiated   : " + nrad);

            long id = upd.getCltuLastProcessed();
            System.out.println("Last processed Id: " + id);

            ISLE_Time t = upd.getRadiationStartTime();
            String theTime_c = "";
            if (t != null)
            {
                theTime_c = t.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth);
            }
            System.out.print("Radiation started: ");
            if (t != null)
            {
                System.out.print(theTime_c);
            }
            System.out.println();

            CLTU_Status st = upd.getCltuStatus();
            System.out.println("CLTU status      : " + st);

            id = upd.getCltuLastOk();
            System.out.println("Last Ok Id       : " + id);

            t = upd.getRadiationStopTime();
            theTime_c = null;
            if (t != null)
            {
                theTime_c = t.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth);
            }
            System.out.print("Radiation stopped: ");
            if (t != null)
            {
                System.out.print(theTime_c);
            }
            System.out.println();

            CLTU_UplinkStatus uls = upd.getUplinkStatus();
            System.out.println("Uplink status    : " + uls);

            id = upd.getExpectedCltuId();
            System.out.println("Next expected Id : " + id);

            long evId = upd.getExpectedEventInvocationId();
            System.out.println("Exp ev invoc. Id : " + evId);

            System.out.println();

        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ServiceInform.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    // virtual in c++, so it should be extended in the in some class.
    @SuppressWarnings("unused")
    private void threadMain()
    {
        EE_Duration duration = new EE_Duration(0, 1500, EE_TIME_Prec.eeTIME_PrecMILLISEC); // 1500
                                                                                           // ms
        // EE_Duration duration(1); //1 second
        while (true)
        {
            waitForSendNotification(duration);
            if (this.stopThread == true)
            {
                break;
            }
        }
        System.out.println("End of thread");
    }

    private void waitForSendNotification(EE_Duration duration)
    {

        // wait for data
        this.eeCondVarNotification.lock();
        try
        {
            this.eeCondVarNotification.wait();
        }
        catch (InterruptedException e1)
        {
            LOG.log(Level.FINE, "SleApiException ", e1);
        }
        this.eeCondVarNotification.unlock();

        // cout << "Start the timer" << endl;
        EE_ElapsedTimer pElapsedTimer = new EE_ElapsedTimer();
        this.objMutex.lock();
        HRESULT res = HRESULT.S_OK;
        try
        {
            pElapsedTimer.start(duration, this, this.lastCltu);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        this.mapElapsedTimer.put(this.lastCltu, pElapsedTimer);
        this.objMutex.unlock();
        if (res != HRESULT.S_OK)
        {
            System.out.println("Start Timer failed. res=" + res);
        }

    }

    @Override
    public void processTimeout(Object timer, int invocationId)
    {
        this.objMutex.lock();
        this.mapElapsedTimer.remove(invocationId);
        this.objMutex.unlock();

        this.time.update();
        this.currentCltuBufferSize += this.lgRadiated;

        ICLTU_SIUpdate cltusiupd = getCLTUSIUpdate();
        cltusiupd.cltuRadiated(this.time, null, true);

    }

    @Override
    public void handlerAbort(Object timer)
    {
        System.out.println("EE_SYSTST_CLTUSIClient::HandlerAbort");

    }

}
