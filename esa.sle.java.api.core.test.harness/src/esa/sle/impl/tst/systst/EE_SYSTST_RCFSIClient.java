package esa.sle.impl.tst.systst;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIOpFactory;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.iraf.IRAF_SIAdmin;
import ccsds.sle.api.isrv.iraf.types.RAF_DeliveryMode;
import ccsds.sle.api.isrv.ircf.IRCF_SIAdmin;
import ccsds.sle.api.isrv.ircf.IRCF_SIUpdate;
import ccsds.sle.api.isrv.ircf.IRCF_SyncNotify;
import ccsds.sle.api.isrv.ircf.IRCF_TransferData;
import ccsds.sle.api.isrv.ircf.types.RCF_ChannelType;
import ccsds.sle.api.isrv.ircf.types.RCF_DeliveryMode;
import ccsds.sle.api.isrv.ircf.types.RCF_Gvcid;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_NotificationType;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.tst.systst.types.EE_SYSTST_T_Component;

public class EE_SYSTST_RCFSIClient extends EE_SYSTST_SIClient
{
    private static final Logger LOG = Logger.getLogger(EE_SYSTST_RCFSIClient.class.getName());

    private EE_SYSTST_RCFOpGen opGen;


    private enum T_RCFCmd
    {
        T_RCFCmd_set_delivery_mode(0, "set_dm"),
        T_RCFCmd_set_latency_limit(1, "set_ll"),
        T_RCFCmd_set_buffer_size(2, "set_buffer_size"),
        T_RCFCmd_set_init_prod_status(3, "set_init_ps"),
        T_RCFCmd_set_init_fs_lock(4, "set_init_fsl"),
        T_RCFCmd_set_init_cdm_lock(5, "set_init_cdml"),
        T_RCFCmd_set_init_scd_lock(6, "set_init_scdl"),
        T_RCFCmd_set_init_ss_lock(7, "set_init_ssl"),
        T_RCFCmd_set_perm_gvcIds(8, "set_pgvcid"),
        T_RCFCmd_set_min_rep_cycle(27, "set_mrc"),
        // SI_update
        T_RCFCmd_set_prod_status(9, "set_ps"),
        T_RCFCmd_set_fs_lock(10, "set_fsl"),
        T_RCFCmd_set_cdm_lock(11, "set_cdml"),
        T_RCFCmd_set_scd_lock(12, "set_scdl"),
        T_RCFCmd_set_ss_lock(13, "set_ssl"),
        T_RCFCmd_print_si(14, "print"),
        T_RCFCmd_up(15, "up"),

        T_RCFCmd_dummy(16, "      "),

        T_RCFCmd_bind(17, "bind"),
        T_RCFCmd_unbind(18, "unbind"),
        T_RCFCmd_start(19, "start"),
        T_RCFCmd_stop(20, "stop"),
        T_RCFCmd_transfer_data(21, "td"),
        T_RCFCmd_sync_notify(22, "sn"),
        T_RCFCmd_ssr(23, "ssr"),
        T_RCFCmd_get_prm(24, "gp"),
        T_RCFCmd_peer_abort(25, "peer_abort"),
        T_RCFCmd_auto_gen_td(26, "auto_gen_td"), // auto generate TD operations
        T_RCFCmd_Max(28, "Max"); // can be used for invalid and/or max num of
                                 // commands

        private int code;

        private String msg;


        private T_RCFCmd(int code, String msg)
        {
            this.code = code;
            this.msg = msg;
        }

        public int getCode()
        {
            return this.code;
        }

        @Override
        public String toString()
        {
            return this.msg;
        }

        public static T_RCFCmd getT_RCFCmd(int code)
        {
            for (T_RCFCmd e : values())
            {
                if (e.code == code)
                {
                    return e;
                }
            }

            return null;
        }
    }


    static private String[] helpCommand = {
                                           "set_dm            IRCF_SIAdmin: set delivery mode",
                                           "set_ll            IRCF_SIAdmin: set latency limit",
                                           "set_buffer_size   IRCF_SIAdmin: set transfer buffer size",
                                           "set_init_ps       IRCF_SIAdmin: set initial production status",
                                           "set_init_fsl      IRCF_SIAdmin: set initial frame sync lock",
                                           "set_init_cdml     IRCF_SIAdmin: set initial carrier demod lock",
                                           "set_init_scdl     IRCF_SIAdmin: set initial sub carrier demod lock",
                                           "set_init_ssl      IRCF_SIAdmin: set initial symbol sync lock",
                                           "set_pgvcid        IRCF_SIAdmin: set permitted GVCID list",
                                           "set_mrc           IRCF_SIAdmin: set minimum reporting cycle",  //Since SLES V5
                                           "set_ps            IRCF_SIUpdate: set production status",
                                           "set_fsl           IRCF_SIUpdate: set frame sync lock",
                                           "set_cdml          IRCF_SIUpdate: set carrier demod lock",
                                           "set_scdl          IRCF_SIUpdate: set subcarrier demod lock",
                                           "set_ssl           IRCF_SIUpdate: set symbol sync lock",
                                           "print             prints the values of the SIAdmin and SIUpdate parameters",
                                           "up                back to the service element",
                                           "                                             ",
                                           "bind        (u)   send RCF-BIND operation",
                                           "unbind      (u)   send RCF-UNBIND operation",
                                           "start       (u)   send RCF-START operation",
                                           "stop        (u)   send RCF-STOP operation",
                                           "td          (p)   send RCF-TRANSFER-DATA operation",
                                           "sn          (p)   send RCF-SYNC-NOTIFY",
                                           "ssr         (u)   send RCF-SCHEDULE-STATUS-REPORT operation",
                                           "gp          (u)   send RCF-GET-PARAMETER operation",
                                           "peer_abort  (u/p) send RCF-PEER-ABORT operation",
                                           "auto_gen_td (p)   send RCF-TRANSFER-DATA operations automatically" };


    public EE_SYSTST_RCFSIClient(SLE_AppRole role, EE_SYSTST_TimeSource timeSource, UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnChFrames, role, timeSource, utl);
        this.opGen = null;
        this.playback = false;
    }

    @Override
    public EE_SYSTST_T_Component startUIF(boolean playback)
    {
        HRESULT rc = HRESULT.S_OK;
        T_RCFCmd nextCommand = T_RCFCmd.T_RCFCmd_Max;

        this.playback = playback;

        EE_Reference<String> arg1 = new EE_Reference<String>();
        EE_Reference<String> arg2 = new EE_Reference<String>();
        EE_Reference<String> arg3 = new EE_Reference<String>();

        while (nextCommand != T_RCFCmd.T_RCFCmd_up)
        {
            nextCommand = getNextCommand(arg1, arg2, arg3);

            // -----------------------------------------------
            if (nextCommand == T_RCFCmd.T_RCFCmd_set_delivery_mode)
            {
                int dm = Integer.parseInt(arg1.getReference());
                RCF_DeliveryMode rcfDm = RCF_DeliveryMode.getRCFDelModeByCode(dm);

                IRCF_SIAdmin adm = getRCFSIAdmin();
                if (adm != null)
                {
                    adm.setDeliveryMode(rcfDm);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_set_latency_limit)
            {
                int ll = Integer.parseInt(arg1.getReference());
                IRCF_SIAdmin adm = getRCFSIAdmin();
                if (adm != null)
                {
                    adm.setLatencyLimit(ll);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_set_buffer_size)
            {
                long bs = Long.parseLong(arg1.getReference());
                IRCF_SIAdmin adm = getRCFSIAdmin();
                if (adm != null)
                {
                    adm.setTransferBufferSize(bs);
                }

            }

            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_set_init_prod_status)
            {
                int ps = Integer.parseInt(arg1.getReference());
                RCF_ProductionStatus rcfPs = RCF_ProductionStatus.getProductionStatusByCode(ps);
                IRCF_SIAdmin adm = getRCFSIAdmin();
                if (adm != null)
                {
                    adm.setInitialProductionStatus(rcfPs);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_set_init_fs_lock
                     || nextCommand == T_RCFCmd.T_RCFCmd_set_init_cdm_lock
                     || nextCommand == T_RCFCmd.T_RCFCmd_set_init_scd_lock
                     || nextCommand == T_RCFCmd.T_RCFCmd_set_init_ss_lock)
            {
                int lsi = Integer.parseInt(arg1.getReference());
                RCF_LockStatus ls = RCF_LockStatus.getLockStatusByCode(lsi);
                IRCF_SIAdmin adm = getRCFSIAdmin();
                if (adm != null)
                {
                    if (nextCommand == T_RCFCmd.T_RCFCmd_set_init_fs_lock)
                    {
                        adm.setInitialFrameSyncLock(ls);
                    }
                    else if (nextCommand == T_RCFCmd.T_RCFCmd_set_init_cdm_lock)
                    {
                        adm.setInitialCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_RCFCmd.T_RCFCmd_set_init_scd_lock)
                    {
                        adm.setInitialSubCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_RCFCmd.T_RCFCmd_set_init_ss_lock)
                    {
                        adm.setInitialSymbolSyncLock(ls);
                    }
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_set_perm_gvcIds)
            {
                setPermGvcIdList();
            }
            
            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_set_min_rep_cycle) // New with SLES V5
            {
            	IRCF_SIAdmin adm = getRCFSIAdmin();
            	long mrc = Long.parseLong(arg1.getReference());
                if (adm != null)
                {
                    adm.setMinimumReportCycle(mrc);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_set_prod_status)
            {
                int ps = Integer.parseInt(arg1.getReference());
                RCF_ProductionStatus rcfPs = RCF_ProductionStatus.getProductionStatusByCode(ps);
                IRCF_SIUpdate upd = getRCFSIUpdate();
                if (upd != null)
                {
                    upd.setProductionStatus(rcfPs);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_set_fs_lock || nextCommand == T_RCFCmd.T_RCFCmd_set_cdm_lock
                     || nextCommand == T_RCFCmd.T_RCFCmd_set_scd_lock || nextCommand == T_RCFCmd.T_RCFCmd_set_ss_lock)
            {
                int lsi = Integer.parseInt(arg1.getReference());
                RCF_LockStatus ls = RCF_LockStatus.getLockStatusByCode(lsi);
                IRCF_SIUpdate upd = getRCFSIUpdate();
                if (upd != null)
                {
                    if (nextCommand == T_RCFCmd.T_RCFCmd_set_fs_lock)
                    {
                        upd.setFrameSyncLock(ls);
                    }
                    else if (nextCommand == T_RCFCmd.T_RCFCmd_set_cdm_lock)
                    {
                        upd.setCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_RCFCmd.T_RCFCmd_set_scd_lock)
                    {
                        upd.setSubCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_RCFCmd.T_RCFCmd_set_ss_lock)
                    {
                        upd.setSymbolSyncLock(ls);
                    }
                }
            }
            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_print_si)
            {
                printSI();
            }

            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_dummy)
            {
                // do nothing
            }
            
            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_bind || nextCommand == T_RCFCmd.T_RCFCmd_unbind
                     || nextCommand == T_RCFCmd.T_RCFCmd_start || nextCommand == T_RCFCmd.T_RCFCmd_stop
                     || nextCommand == T_RCFCmd.T_RCFCmd_transfer_data || nextCommand == T_RCFCmd.T_RCFCmd_sync_notify
                     || nextCommand == T_RCFCmd.T_RCFCmd_ssr || nextCommand == T_RCFCmd.T_RCFCmd_get_prm
                     || nextCommand == T_RCFCmd.T_RCFCmd_peer_abort)
            {

                IUnknown piuk = this.siAdmin.queryInterface(IUnknown.class);
                ISLE_Operation op = getOpGen().createOp(getOpType(nextCommand), this.eventQueue, piuk);
                if (op != null)
                {
                    String ir = "i";
                    if (op.isConfirmed() == true)
                    {
                        EE_Reference<String> what = new EE_Reference<String>();
                        what.setReference("");
                        while (!(what.getReference().equals("i") || what.getReference().equals("r")))
                        {
                            System.out.print("Invocation or Return (i/r)? ");
                            utl.read(what, playback);
                        }
                        ir = what.getReference();
                    }

                    if (this.srvInit != null)
                    {
                        if (ir.equals("i"))
                        {
                            if (nextCommand == T_RCFCmd.T_RCFCmd_bind)
                            {
                                this.seqCounter = 1;
                            }
                            System.out.println("Send Invoke Operation. Seq " + this.seqCounter);
                            rc = HRESULT.S_OK;
                            try
                            {
                                this.srvInit.initiateOpInvoke(op, this.seqCounter++);
                            }
                            catch (SleApiException e)
                            {
                                rc = e.getHResult();
                            }

                            // //////////////////////////////////////////////
                            // TRY TO HANDLE RESUME TRANSFER (very limited)
                            if (nextCommand == T_RCFCmd.T_RCFCmd_transfer_data
                                || nextCommand == T_RCFCmd.T_RCFCmd_sync_notify)
                            {
                                while (rc == HRESULT.SLE_E_SUSPENDED)
                                {
                                    System.out.println("*** Waiting for Resume Data Transfer");

                                    try
                                    {
                                        Thread.sleep(1);
                                    }
                                    catch (InterruptedException e1)
                                    {
                                        LOG.log(Level.FINE, "InterruptedException ", e1);
                                    }

                                    // try again until S_OK
                                    // (assumes Resume Data Transfer event
                                    // received)
                                    try
                                    {
                                        this.srvInit.initiateOpInvoke(op, this.seqCounter++);
                                    }
                                    catch (SleApiException e)
                                    {
                                        rc = e.getHResult();
                                    }
                                }
                            }
                            // END HANDLE RESUME DATA TRANSFER
                            // //////////////////////////////////////////////

                            if (nextCommand == T_RCFCmd.T_RCFCmd_peer_abort)
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
                            ISLE_ConfirmedOperation cop = (ISLE_ConfirmedOperation) op;
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

            } // end if (next Comman == send operation)

            // -----------------------------------------------
            else if (nextCommand == T_RCFCmd.T_RCFCmd_auto_gen_td)
            {
                rc = autoSendTD();
                displayResult(rc);
            }

        } // end while nextCommand != T_RCFCmd_up

        return EE_SYSTST_T_Component.eeEE_SYSTST_TestSE;
    }

    private HRESULT autoSendTD()
    {
        HRESULT rc = HRESULT.S_OK;
        IUnknown piuk = this.siAdmin.queryInterface(IUnknown.class);
        ISLE_Operation op = getOpGen().createOp(SLE_OpType.sleOT_transferData, this.eventQueue, piuk);

        if (op != null)
        {
            IRCF_TransferData td = (IRCF_TransferData) op;

            System.out.print("How many TD operations ? ");
            EE_Reference<String> n = new EE_Reference<String>();
            utl.read(n, this.playback);
            int i = Integer.parseInt(n.getReference());

            for (int j = 0; j < i; j++)
            {
                IRCF_TransferData tds = (IRCF_TransferData) td.copy();
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

            }
        }
        else
        {
            return HRESULT.E_FAIL;
        }

        return HRESULT.S_OK;
    }

    private IRCF_SIUpdate getRCFSIUpdate()
    {

        IRCF_SIUpdate iu = this.siAdmin.queryInterface(IRCF_SIUpdate.class);
        if (iu == null)
        {
            System.out.println("Interface IRCF_SIUpdate not available");
        }
        return iu;
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
            this.opGen = new EE_SYSTST_RCFOpGen(this.opFactory, f, this.playback, this.utl);

            // #hd# shift setting the version to this place
            int vNum = this.siAdmin.getVersion();
            this.opGen.setVersion(vNum);
        }
        return this.opGen;
    }

    @Override
    public void testTdSend(long lg, int nbtime, long delay_td, byte[] tdData, int delay)
    {

        ISLE_Operation op = null;
        IRCF_TransferData optd;

        EE_Reference<DCW_Event_Type> et = new EE_Reference<>();
        EE_Reference<IUnknown> psi1 = new EE_Reference<>();
        EE_Reference<ISLE_Operation> pop = new EE_Reference<>();

        int to = 1;

        ISLE_Time time = null, time1 = null;

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

        EE_SYSTST_OpGen pOpGen = getOpGen();

        boolean do_print = true;
        if (nbtime > 0)
        {
            nbtime++;
        }

        while (true)
        {

            // send transfer data till suspend
            while (true)
            {

                if (nbtime >= 0)
                {
                    // create the transfer data op
                    try
                    {
                        op = pOpGen.siOPF.createOperation(ISLE_Operation.class, SLE_OpType.sleOT_transferData);
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                    }
                    IRCF_TransferData td = (IRCF_TransferData) op;
                    td.setDataLinkContinuity(25);
                    td.setData(tdData);
                    td.setAntennaIdGFString("1.2.3");
                    time.update();
                    td.setEarthReceiveTime(time);
                    if ((this.seqCounter % 1000) == 0)
                    {
                        do_print = true;
                    }
                    else
                    {
                        do_print = false;
                    }

                    if (do_print)
                    {
                        System.out.println("Send TD Op. Seq " + this.seqCounter + ". lg " + lg);
                    }

                    HRESULT rc = HRESULT.S_OK;
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
                        if (do_print)
                        {
                            System.out.println(" : S_SUS");
                        }
                        if (nbtime > 0)
                        {
                            nbtime--;
                            if (nbtime == 1)
                            {
                                break;
                            }
                        }

                    }
                    else if (rc == HRESULT.S_OK)
                    {
                        if (do_print)
                        {
                            System.out.println(" : S_OK");
                        }
                        if (nbtime > 0)
                        {
                            nbtime--;
                            if (nbtime == 1)
                            {
                                break;
                            }
                        }

                    }
                    else
                    {
                        System.out.println(" : " + rc);
                        break;
                    }

                }
                else
                {
                    break;
                }
            }

            while (true)
            {
                HRESULT res = HRESULT.S_OK;
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
                        System.out.println("Rcv Resume DT");
                        break;
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
                            optd = pop.getReference().queryInterface(IRCF_TransferData.class);
                            @SuppressWarnings("unused")
                            byte[] data1;
                            data1 = optd.getData();
                            System.out.println("Rcv TD Rtn Op.");
                        }
                        else
                        {
                            System.out.println("Rcv " + pop.getReference().getOperationType() + " Rtn Op");
                        }
                    }
                    else if (et.getReference() == DCW_Event_Type.dcwEVT_informOpInvoke)
                    {
                        if (pop.getReference().getOperationType() == SLE_OpType.sleOT_peerAbort)
                        {
                            String tmp = pop.getReference().print(512);
                            System.out.println(tmp);
                        }
                        else
                        {
                            System.out.println("Rcv " + pop.getReference().getOperationType() + " Inv Op");
                        }
                    }
                }
                else
                {
                    System.out.println("Next Event fail");
                    return;
                }

            }
        }

    }

    @Override
    public void testTdReceive(long lg, int nbtime)
    {
        EE_Reference<DCW_Event_Type> et = new EE_Reference<DCW_Event_Type>();
        EE_Reference<IUnknown> psi1 = new EE_Reference<IUnknown>();
        EE_Reference<ISLE_Operation> pop = new EE_Reference<>();

        int to = 10;

        int count_raf;
        double lgraf_tot = 0, rate, rate_prec = 0;

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

        if (nbtime > 0)
        {
            nbtime++;
        }

        while (true)
        {

            count_raf = 0;

            // wait for data transfer op
            while (true)
            {
                HRESULT res = HRESULT.S_OK;
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

                        switch (pop.getReference().getOperationType())
                        {
                        case sleOT_transferData:
                        {
                            IRCF_TransferData optd = pop.getReference().queryInterface(IRCF_TransferData.class);
                            byte[] data1;

                            data1 = optd.getData();
                            lgraf_tot += data1.length;

                            if ((count_raf % 100) == 0)
                            {
                                time.update();
                                double diff_time = time.subtract(time1);

                                if (diff_time > 10)
                                {
                                    rate = ((lgraf_tot * 8) / (1024 * 1024));
                                    System.out.println("Received " + rate + " M bits in " + diff_time + " s" + " --> "
                                                       + (rate / diff_time) + " M bits / s");
                                    if (rate_prec != 0)
                                    {
                                        double dd = (rate / diff_time) - rate_prec;
                                        System.out.println(".   (" + dd + ")");
                                    }
                                    else
                                    {
                                        System.out.println();
                                    }
                                    rate_prec = (rate / diff_time);
                                }
                            }
                            count_raf++;
                            break;
                        }
                        case sleOT_syncNotify:
                        {

                            IRCF_SyncNotify opsn = pop.getReference().queryInterface(IRCF_SyncNotify.class);
                            System.out.println("Rcv Sync Notify Inv Op. " + opsn.getNotificationType());

                            if (opsn.getNotificationType() == RCF_NotificationType.rcfNT_endOfData)
                            {
                                time.update();
                                double diff_time = time.subtract(time1);

                                if (diff_time > 10)
                                {
                                    rate = ((lgraf_tot * 8) / (1024 * 1024));
                                    System.out.println("Received " + rate + " M bits in " + diff_time + " s" + " --> "
                                                       + (rate / diff_time) + " M bits / s");
                                    if (rate_prec != 0)
                                    {
                                        double dd = (rate / diff_time) - rate_prec;
                                        System.out.println(".   (" + dd + ")");
                                    }
                                    else
                                    {
                                        System.out.println();
                                    }
                                    rate_prec = (rate / diff_time);
                                }
                            }
                            break;
                        }

                        default:
                        {
                            System.out.println("Rcv " + pop.getReference().getOperationType() + " Inv Op");
                            if (pop.getReference().getOperationType() == SLE_OpType.sleOT_peerAbort)
                            {
                                return;
                            }
                            break;
                        }
                        }

                        if (nbtime > 0)
                        {
                            nbtime--;
                            if (nbtime == 1)
                            {
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
                    return;
                }

            }
        }

    }

    private T_RCFCmd getNextCommand(EE_Reference<String> arg1, EE_Reference<String> arg2, EE_Reference<String> arg3)
    {

        T_RCFCmd nextCommand = T_RCFCmd.T_RCFCmd_Max;
        EE_Reference<String> cmd = new EE_Reference<String>();
        cmd.setReference("");

        while (nextCommand == T_RCFCmd.T_RCFCmd_Max)
        {

            prompt();

            if (utl.read(cmd, this.playback) == false)
            {
                nextCommand = T_RCFCmd.T_RCFCmd_up;
                break;
            }

            for (int i = 0; i < T_RCFCmd.T_RCFCmd_Max.getCode(); i++)
            {

                if ((T_RCFCmd.getT_RCFCmd(i).toString()).equals(cmd.getReference()))
                {
                    nextCommand = T_RCFCmd.getT_RCFCmd(i);
                    break;
                }
            }

            if (nextCommand == T_RCFCmd.T_RCFCmd_Max)
            {
                // command not found, go to base-class
                processSLECommand(cmd.getReference());
                cmd.setReference("");
            }
        } // end while

        // ----------------------------------------------------
        if (nextCommand == T_RCFCmd.T_RCFCmd_set_delivery_mode)
        {
            arg1.setReference(readDeliveryMode());
        }

        // ----------------------------------------------------
        else if (nextCommand == T_RCFCmd.T_RCFCmd_set_latency_limit)
        {
            System.out.println("Latency Limit (sec): ");
            utl.read(arg1, this.playback);
        }
        // -----------------------------------------------
        else if (nextCommand == T_RCFCmd.T_RCFCmd_set_buffer_size)
        {
            System.out.println("Buffer size (number of operation objects): ");
            utl.read(arg1, this.playback);

        }
        else if (nextCommand == T_RCFCmd.T_RCFCmd_set_min_rep_cycle)
        {
        	System.out.println("Min. reporting cycle: ");
        	utl.read(arg1, playback);
        }
        // -----------------------------------------------
        else if (nextCommand == T_RCFCmd.T_RCFCmd_set_init_prod_status)
        {
            System.out.println("Initial Production status:");
            arg1.setReference(readProductionStatus());
        }

        // -----------------------------------------------
        else if (nextCommand == T_RCFCmd.T_RCFCmd_set_init_fs_lock
                 || nextCommand == T_RCFCmd.T_RCFCmd_set_init_cdm_lock
                 || nextCommand == T_RCFCmd.T_RCFCmd_set_init_scd_lock
                 || nextCommand == T_RCFCmd.T_RCFCmd_set_init_ss_lock)
        {
            arg1.setReference(readLockStatus());
        }

        // -----------------------------------------------
        else if (nextCommand == T_RCFCmd.T_RCFCmd_set_prod_status)
        {
            arg1.setReference(readProductionStatus());
        }

        // -----------------------------------------------
        else if (nextCommand == T_RCFCmd.T_RCFCmd_set_fs_lock || nextCommand == T_RCFCmd.T_RCFCmd_set_cdm_lock
                 || nextCommand == T_RCFCmd.T_RCFCmd_set_scd_lock || nextCommand == T_RCFCmd.T_RCFCmd_set_ss_lock)
        {
            arg1.setReference(readLockStatus());
        }

        return nextCommand;
    }

    private String readLockStatus()
    {
        EE_Reference<String> arg = new EE_Reference<String>();
        boolean isOk = false;

        while (isOk == false)
        {
            System.out.println("Lock Status: (0=inLock, 1=outOfLock, 2=notInUse, 3=unknown) ");
            utl.read(arg, this.playback);

            int ls = Integer.parseInt(arg.getReference());
            if (ls < 0 || ls > 3)
            {
                System.out.println("Invalid Lock Status value");
            }
            else
            {
                isOk = true;
            }
        }
        return arg.getReference();
    }

    private String readProductionStatus()
    {
        EE_Reference<String> arg = new EE_Reference<String>();
        boolean isOk = false;

        while (isOk == false)
        {
            System.out.print("Production status: (0=running, 1=interrupted, 2=halted) ");
            utl.read(arg, this.playback);

            int ls = Integer.parseInt(arg.getReference());
            if (ls < 0 || ls > 2)
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

    private String readDeliveryMode()
    {
        EE_Reference<String> arg = new EE_Reference<String>();
        boolean isOk = false;

        while (isOk == false)
        {
            System.out.println("Delivery Mode (0 = rtnOnlineTimely, 1 = rtnOnlineComplete,");
            System.out.println("               2 = rtnOffline) ");
            utl.read(arg, this.playback);

            int ls = Integer.parseInt(arg.getReference());
            if (ls < 0 || ls > 2)
            {
                System.out.println("Invalid Delivery Mode value");
            }
            else
            {
                isOk = true;
            }
        }

        return arg.getReference();
    }

    private SLE_OpType getOpType(T_RCFCmd cmd)
    {
        if (cmd == T_RCFCmd.T_RCFCmd_bind)
        {
            return SLE_OpType.sleOT_bind;
        }
        else if (cmd == T_RCFCmd.T_RCFCmd_unbind)
        {
            return SLE_OpType.sleOT_unbind;
        }
        else if (cmd == T_RCFCmd.T_RCFCmd_start)
        {
            return SLE_OpType.sleOT_start;
        }
        else if (cmd == T_RCFCmd.T_RCFCmd_stop)
        {
            return SLE_OpType.sleOT_stop;
        }
        else if (cmd == T_RCFCmd.T_RCFCmd_transfer_data)
        {
            return SLE_OpType.sleOT_transferData;
        }
        else if (cmd == T_RCFCmd.T_RCFCmd_sync_notify)
        {
            return SLE_OpType.sleOT_syncNotify;
        }
        else if (cmd == T_RCFCmd.T_RCFCmd_ssr)
        {
            return SLE_OpType.sleOT_scheduleStatusReport;
        }
        else if (cmd == T_RCFCmd.T_RCFCmd_get_prm)
        {
            return SLE_OpType.sleOT_getParameter;
        }
        else if (cmd == T_RCFCmd.T_RCFCmd_peer_abort)
        {
            return SLE_OpType.sleOT_peerAbort;
        }
        else
        {
            return SLE_OpType.sleOT_bind; // no better idea
        }
    }

    /**
     * Gets the permitted GVCId from the istream and sets it on the IRCF_SIAdmin
     * interface
     */
    private void setPermGvcIdList()
    {
        IRCF_SIAdmin adm = getRCFSIAdmin();
        if (adm != null)
        {
            EE_Reference<String> arg = new EE_Reference<String>();
            System.out.println("How many GVCIds ? ");
            utl.read(arg, this.playback);
            int num = Integer.parseInt(arg.getReference());

            RCF_Gvcid[] l = new RCF_Gvcid[num];
            for (int i = 0; i < num; i++)
            {
                RCF_Gvcid id = new RCF_Gvcid();
                System.out.println("Enter GCVID " + i);
                System.out.print("Type (0=Master,1=Virtual): ");
                utl.read(arg, this.playback);
                int type_i = Integer.parseInt(arg.getReference());
                id.setType(RCF_ChannelType.getChannelTypeByCode(type_i));

                System.out.println("SCID (0-1023): ");
                utl.read(arg, this.playback);
                int sc_i = Integer.parseInt(arg.getReference());
                id.setScid(sc_i);

                System.out.println("Version (0-1): ");
                utl.read(arg, this.playback);
                int ver_i = Integer.parseInt(arg.getReference());
                id.setVersion(ver_i);

                System.out.print("VC ID  (0-63): ");
                utl.read(arg, this.playback);
                int vc_i = Integer.parseInt(arg.getReference());
                id.setVcid(vc_i);

                l[i] = id;
            }

            adm.setPermittedGvcidSet(l);
        }
        else
        {
            System.out.println("Could not get IRCF_SIAdmin interface ");
        }

    }
    
    /**
     * Prints out the configuration of the SI
     * on user input 'print'.
     */
    @Override
    public void printSI()
    {
        super.printSI();

        IRCF_SIAdmin adm = getRCFSIAdmin();
        if (adm != null)
        {
            RCF_DeliveryMode dm = adm.getDeliveryMode();
            System.out.println("Delivery Mode    : " + dm);
            System.out.println("Min report cycle : " + adm.getMinimumReportCycle());
            System.out.println();
        }
    }
        

    private IRCF_SIAdmin getRCFSIAdmin()
    {
        IRCF_SIAdmin ia = this.siAdmin.queryInterface(IRCF_SIAdmin.class);
        if (ia == null)
        {
            System.out.println("Interface IRCF_SIAdmin not available");
        }
        return ia;
    }

    @Override
    public void help()
    {
        super.help();

        for (int i = 0; i < T_RCFCmd.T_RCFCmd_Max.getCode(); i++)
        {
            System.out.println("   " + helpCommand[i]);
        }
        System.out.println();
    }

}
