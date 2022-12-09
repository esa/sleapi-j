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
import ccsds.sle.api.isrv.ircf.IRCF_SIAdmin;
import ccsds.sle.api.isrv.irocf.IROCF_SIAdmin;
import ccsds.sle.api.isrv.irocf.IROCF_SIUpdate;
import ccsds.sle.api.isrv.irocf.IROCF_SyncNotify;
import ccsds.sle.api.isrv.irocf.IROCF_TransferData;
import ccsds.sle.api.isrv.irocf.types.ROCF_ChannelType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ControlWordType;
import ccsds.sle.api.isrv.irocf.types.ROCF_DeliveryMode;
import ccsds.sle.api.isrv.irocf.types.ROCF_Gvcid;
import ccsds.sle.api.isrv.irocf.types.ROCF_LockStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_NotificationType;
import ccsds.sle.api.isrv.irocf.types.ROCF_ProductionStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_UpdateMode;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.tst.systst.types.EE_SYSTST_T_Component;

public class EE_SYSTST_ROCFSIClient extends EE_SYSTST_SIClient
{
    private static final Logger LOG = Logger.getLogger(EE_SYSTST_ROCFSIClient.class.getName());

    private EE_SYSTST_ROCFOpGen opGen;


    private enum T_ROCFCmd
    {
        T_ROCFCmd_set_delivery_mode(0, "set_dm", "set delivery mode"),
        T_ROCFCmd_set_latency_limit(1, "set_ll", "set latency limit"),
        T_ROCFCmd_set_buffer_size(2, "set_buffer_size", "set transfer buffer size"),
        T_ROCFCmd_set_init_prod_status(3, "set_init_ps", "set initial production status"),
        T_ROCFCmd_set_init_fs_lock(4, "set_init_fsl", "set initial frame sync lock"),
        T_ROCFCmd_set_init_cdm_lock(5, "set_init_cdml", "set initial carrier demod lock"),
        T_ROCFCmd_set_init_scd_lock(6, "set_init_scdl", "set initial sub carrier demod lock"),
        T_ROCFCmd_set_init_ss_lock(7, "set_init_ssl", "set initial symbol sync lock"),
        T_ROCFCmd_set_perm_gvcIds(8, "set_pgvcid", "set permitted GVCID list"),
        T_ROCFCmd_set_perm_cwts(9, "set_pcwts", "set permitted control word type list"),
        T_ROCFCmd_set_perm_tcvcIds(10, "set_ptcvcid", "set permitted TC VcId list"),
        T_ROCFCmd_set_perm_ums(11, "set_pums", "set permitted update mode list"),
        T_ROCFCmd_set_min_rep_cycle(30, "set_mrc", "set minimum reporting cycle"),

        // SI_update
        T_ROCFCmd_set_prod_status(12, "set_ps", "set production status"),
        T_ROCFCmd_set_fs_lock(13, "set_fsl", "set frame sync lock"),
        T_ROCFCmd_set_cdm_lock(14, "set_cdml", "set carrier demod lock"),
        T_ROCFCmd_set_scd_lock(15, "set_scdl", "set subcarrier demod lock"),
        T_ROCFCmd_set_ss_lock(16, "set_ssl", "set symbol sync lock"),
        T_ROCFCmd_set_nframes_proc(17, "set_nfp", "set number of frames processed"), // CHANGED-v2:
                                                                                     // inserted
        T_ROCFCmd_print_si(18, "print", "prints the values of the SIAdmin and SIUpdate parameters"),
        T_ROCFCmd_up(19, "up", "back to the service element"),

        // operations
        T_ROCFCmd_bind(20, "bind", "ROCF-OPERATIONS"),
        T_ROCFCmd_unbind(21, "unbind", "(u)  send ROCF-BIND operation"),
        T_ROCFCmd_start(22, "start", "(u)  send ROCF-START operation"),
        T_ROCFCmd_stop(23, "stop", "(u)  send ROCF-STOP operation"),
        T_ROCFCmd_transfer_data(24, "td", "(u)  send ROCF-TRANSFER-DATA operation"),
        T_ROCFCmd_sync_notify(25, "sn", "(p)  send ROCF-SYNC-NOTIFY operation"),
        T_ROCFCmd_ssr(26, "ssr", "(u)  send ROCF-SCHEDULE-STATUS-REPORT operation"),
        T_ROCFCmd_get_prm(27, "gp", "(u)  send ROCF-GET-PARAMETER operation"),
        T_ROCFCmd_peer_abort(28, "peer_abort", "(u/p) send ROCF-PEER-ABORT operation"),
        T_ROCFCmd_auto_gen_td(29, "auto_gen_td", "(u)  send ROCF-TRANSFER-DATA operations automatically"), // auto
                                                                                                           // generate
                                                                                                           // TD
                                                                                                           // operations
        T_ROCFCmd_dummy(31, "----", "dummy"),
        T_ROCFCmd_Max(32, "", "Max"); // can be used for invalid and/or max num
                                      // of
        // commands

        private int code;

        private String command;

        private String help;


        private T_ROCFCmd(int code, String command, String help)
        {
            this.code = code;
            this.command = command;
            this.help = help;
        }

        public int getCode()
        {
            return this.code;
        }

        public String getHelp()
        {
            return this.help;
        }

        public String getCommand()
        {
            return this.command;
        }

        @Override
        public String toString()
        {
            return getCommand();
        }

        public static T_ROCFCmd getT_ROCFCmdByCode(int code)
        {
            for (T_ROCFCmd e : values())
            {
                if (e.code == code)
                {
                    return e;
                }
            }

            return null;
        }
    };


    public EE_SYSTST_ROCFSIClient(SLE_AppRole role, EE_SYSTST_TimeSource timeSource, UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnAllFrames, role, timeSource, utl);
        this.opGen = null;
        this.playback = false;
    }

    @Override
    public EE_SYSTST_T_Component startUIF(boolean playback)
    {
        HRESULT rc = HRESULT.S_OK;
        T_ROCFCmd nextCommand = T_ROCFCmd.T_ROCFCmd_Max;

        this.playback = playback;

        EE_Reference<String> arg1 = new EE_Reference<String>();
        EE_Reference<String> arg2 = new EE_Reference<String>();
        EE_Reference<String> arg3 = new EE_Reference<String>();

        while (true)
        {
            nextCommand = getNextCommand(arg1, arg2, arg3);

            // -----------------------------------------------
            if (nextCommand == T_ROCFCmd.T_ROCFCmd_up)
            {
                break;
            }

            // -----------------------------------------------

            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_delivery_mode)
            {
                int dm = Integer.parseInt(arg1.getReference());
                ROCF_DeliveryMode rocfDm = ROCF_DeliveryMode.getROCFDeliveryMode(dm);

                IROCF_SIAdmin adm = getROCFSIAdmin();
                if (adm != null)
                {
                    adm.setDeliveryMode(rocfDm);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_latency_limit)
            {
                int ll = Integer.parseInt(arg1.getReference());
                IROCF_SIAdmin adm = getROCFSIAdmin();
                if (adm != null)
                {
                    adm.setLatencyLimit(ll);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_buffer_size)
            {
                Long bs = Long.parseLong(arg1.getReference());
                IROCF_SIAdmin adm = getROCFSIAdmin();
                if (adm != null)
                {
                    adm.setTransferBufferSize(bs);
                }

            }
            
            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_min_rep_cycle) // New with SLES V5
            {
            	IROCF_SIAdmin adm = getROCFSIAdmin();
            	long mrc = Long.parseLong(arg1.getReference());
                if (adm != null)
                {
                    adm.setMinimumReportCycle(mrc);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_prod_status)
            {
                int ps = Integer.parseInt(arg1.getReference());
                ROCF_ProductionStatus rocfPs = ROCF_ProductionStatus.getProductionStatusByCode(ps);
                IROCF_SIAdmin adm = getROCFSIAdmin();
                if (adm != null)
                {
                    adm.setInitialProductionStatus(rocfPs);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_fs_lock
                     || nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_cdm_lock
                     || nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_scd_lock
                     || nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_ss_lock)
            {
                int lsi = Integer.parseInt(arg1.getReference());
                ROCF_LockStatus ls = ROCF_LockStatus.getLockStatusByCode(lsi);
                IROCF_SIAdmin adm = getROCFSIAdmin();
                if (adm != null)
                {
                    if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_fs_lock)
                    {
                        adm.setInitialFrameSyncLock(ls);
                    }
                    else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_cdm_lock)
                    {
                        adm.setInitialCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_scd_lock)
                    {
                        adm.setInitialSubCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_ss_lock)
                    {
                        adm.setInitialSymbolSyncLock(ls);
                    }

                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_perm_gvcIds)
            {
                setPermGvcIdList();
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_perm_tcvcIds)
            {
                long[] pIdList = utl.readIntList("Permitted TC VcId list (comma-sep, no spaces!): ", playback);
                if (pIdList.length == 1 && pIdList[0] < 1)
                {
                    System.out.println("Illegal VcId list input");
                }
                else
                {
                    IROCF_SIAdmin adm = getROCFSIAdmin();
                    if (adm != null)
                    {
                        UTL.traceIF1("IROCF_SIAdmin.setPermittedTcVcidSet", Integer.toString(pIdList.length));
                        adm.setPermittedTcVcidSet(pIdList);
                    }
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_perm_ums)
            {
                long[] pUmList = utl
                        .readIntList("Permitted update mode list (0:continuous, 1:changeBased)\n(comma-sep, no spaces!): ",
                                     playback);
                if (pUmList.length == 0)
                {
                    System.out.println("Illegal update mode list input");
                }
                else
                {
                    IROCF_SIAdmin adm = getROCFSIAdmin();
                    if (adm != null)
                    {

                        int k = 0;
                        ROCF_UpdateMode[] enums = new ROCF_UpdateMode[pUmList.length];
                        for (Long el : pUmList)
                        {
                            enums[k++] = ROCF_UpdateMode.getROCFUpdateModeByCode(el);
                        }

                        UTL.traceIF1("IROCF_SIAdmin.setPermittedUpdateModeSet", Integer.toString(pUmList.length));
                        adm.setPermittedUpdateModeSet(enums);
                    }

                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_perm_cwts)
            {
                long[] pCwtList = utl
                        .readIntList("Permitted CW type list (0:allControlWords, 1:clcw, 2:notClcw)\n(comma-sep, no spaces!): ",
                                     playback);
                if (pCwtList.length == 0)
                {
                    System.out.println("Illegal CW type list input");
                }
                else
                {
                    IROCF_SIAdmin adm = getROCFSIAdmin();
                    if (adm != null)
                    {
                        int k = 0;
                        ROCF_ControlWordType[] enums = new ROCF_ControlWordType[pCwtList.length];
                        for (Long el : pCwtList)
                        {
                            enums[k++] = ROCF_ControlWordType.getControlWordTypeByCode(el);
                        }
                        UTL.traceIF1("IROCF_SIAdmin.setPermittedControlWordTypeSet", Integer.toString(pCwtList.length));
                        adm.setPermittedControlWordTypeSet(enums);
                    }
                }
            }
            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_prod_status)
            {
                int ps = Integer.parseInt(arg1.getReference());
                ROCF_ProductionStatus rocfPs = ROCF_ProductionStatus.getProductionStatusByCode(ps);
                IROCF_SIUpdate upd = getROCFSIUpdate();
                if (upd != null)
                {
                    upd.setProductionStatus(rocfPs);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_nframes_proc)
            {
                long nf = Long.parseLong(arg1.getReference());
                IROCF_SIUpdate upd = getROCFSIUpdate();
                if (upd != null)
                {
                    upd.setNumFramesProcessed(nf);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_fs_lock || nextCommand == T_ROCFCmd.T_ROCFCmd_set_cdm_lock
                     || nextCommand == T_ROCFCmd.T_ROCFCmd_set_scd_lock
                     || nextCommand == T_ROCFCmd.T_ROCFCmd_set_ss_lock)
            {
                int lsi = Integer.parseInt(arg1.getReference());
                ROCF_LockStatus ls = ROCF_LockStatus.getLockStatusByCode(lsi);
                IROCF_SIUpdate upd = getROCFSIUpdate();
                if (upd != null)
                {
                    if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_fs_lock)
                    {
                        upd.setFrameSyncLock(ls);
                    }
                    else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_cdm_lock)
                    {
                        upd.setCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_scd_lock)
                    {
                        upd.setSubCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_ss_lock)
                    {
                        upd.setSymbolSyncLock(ls);
                    }
                }
            }
            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_print_si)
            {
                printSI();
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_dummy)
            {
                // do nothing
            }

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_bind || nextCommand == T_ROCFCmd.T_ROCFCmd_unbind
                     || nextCommand == T_ROCFCmd.T_ROCFCmd_start || nextCommand == T_ROCFCmd.T_ROCFCmd_stop
                     || nextCommand == T_ROCFCmd.T_ROCFCmd_transfer_data
                     || nextCommand == T_ROCFCmd.T_ROCFCmd_sync_notify || nextCommand == T_ROCFCmd.T_ROCFCmd_ssr
                     || nextCommand == T_ROCFCmd.T_ROCFCmd_get_prm || nextCommand == T_ROCFCmd.T_ROCFCmd_peer_abort)
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
                            System.out.println("Invocation or Return (i/r)? ");
                            utl.read(what, playback);
                        }
                        ir = what.getReference();
                    }

                    if (this.srvInit != null)
                    {
                        if (ir.equals("i"))
                        {
                            if (nextCommand == T_ROCFCmd.T_ROCFCmd_bind)
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
                            if (nextCommand == T_ROCFCmd.T_ROCFCmd_transfer_data
                                || nextCommand == T_ROCFCmd.T_ROCFCmd_sync_notify)
                            {
                                while (rc == HRESULT.SLE_E_SUSPENDED)
                                {
                                    System.out.println("*** Waiting for Resume Data Transfer");

                                    try
                                    {
                                        Thread.sleep(1000);
                                    }
                                    catch (InterruptedException e1)
                                    {
                                        LOG.log(Level.FINE, "InterruptedException ", e1);
                                    }
                                    // try again until S_OK
                                    // (assumes Resume Data Transfer event
                                    // received)
                                    rc = HRESULT.S_OK;
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

                            if (nextCommand == T_ROCFCmd.T_ROCFCmd_peer_abort)
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

            } // end if (next Command == send operation)

            // -----------------------------------------------
            else if (nextCommand == T_ROCFCmd.T_ROCFCmd_auto_gen_td)
            {
                rc = autoSendTD();
                displayResult(rc);
            }

        } // end while nextCommand != T_ROCFCmd_up
        return EE_SYSTST_T_Component.eeEE_SYSTST_TestSE;

    }

    @Override
    public void help()
    {
        super.help();

        for (int i = 0; i < T_ROCFCmd.T_ROCFCmd_Max.getCode(); i++)
        {
            String cmd = T_ROCFCmd.getT_ROCFCmdByCode(i).getCommand();
            cmd = cmd.substring(0, 15);
            System.out.println("   " + cmd + " " + T_ROCFCmd.getT_ROCFCmdByCode(i).getHelp());
        }
        System.out.println();
    }

    @Override
    public void printSI()
    {
        super.printSI();

        IROCF_SIAdmin adm = getROCFSIAdmin();
        if (adm != null)
        {
            ROCF_DeliveryMode dm = adm.getDeliveryMode();
            System.out.println("Delivery Mode  : " + dm);

            System.out.println("Latency Limit  : " + adm.getLatencyLimit());
            System.out.println("Buffer Size    : " + adm.getTransferBufferSize());

            System.out.println("Permitted GVCID list: ");

            ROCF_Gvcid[] l = adm.getPermittedGvcidSet();
            int sz = l.length;
            {
                for (int i = 0; i < sz; i++)
                {
                    System.out.println("  GVCID(" + i + "): type=" + l[i].getType() + ", ");
                    System.out.println("scId=" + l[i].getScid() + ", ");
                    System.out.println("version=" + l[i].getVersion() + ", ");
                    System.out.println("vcId=" + l[i].getVcid());
                }
            }
            if (sz == 0)
            {
                System.out.println("No permitted GVCID list set");
            }

            long[] pTcVcId = adm.getPermittedTcVcidSet();
            sz = pTcVcId.length;
            System.out.println("Perm TC VcId List   : ");
            int i;
            for (i = 0; i < sz; i++)
            {
                if (i > 0)
                {// separator only between elements
                    System.out.print(",");
                }
                System.out.print(pTcVcId[i]);
            }
            if (sz == 0)
            {
                System.out.print("NOT SET");
            }
            System.out.println();

            ROCF_ControlWordType[] pCwt = adm.getPermittedControlWordTypeSet();
            sz = pCwt.length;
            System.out.print("Perm CWT List       : ");
            for (i = 0; i < sz; i++)
            {
                if (i > 0)
                { // separator only between elements
                    System.out.print(",");
                }
                System.out.print(pCwt[i]);
            }
            if (sz == 0)
            {
                System.out.print("NOT SET");
            }
            System.out.println();

            ROCF_UpdateMode[] pUm = adm.getPermittedUpdateModeSet();
            sz = pUm.length;
            System.out.print("Perm UpdMode List   : ");
            for (i = 0; i < sz; i++)
            {
                if (i > 0)
                {// separator only between elements
                    System.out.print(",");
                }
                System.out.print(pUm[i]);
            }
            if (sz == 0)
            {
                System.out.print("NOT SET");
            }
            System.out.println();
            System.out.println("Min Reporting Cycle : "+adm.getMinimumReportCycle());
            System.out.println();
        }

        IROCF_SIUpdate upd = getROCFSIUpdate();
        if (upd != null)
        {
            ROCF_ProductionStatus ps = upd.getProductionStatus();
            System.out.println("Production status: " + ps);

            ROCF_LockStatus ls = upd.getFrameSyncLock();
            System.out.println("Frame sync lock  : " + ls);

            ls = upd.getCarrierDemodLock();
            System.out.println("Carrier demod lck: " + ls);

            ls = upd.getSubCarrierDemodLock();
            System.out.println("Sub Carr dem lock: " + ls);

            ls = upd.getSymbolSyncLock();
            System.out.println("Symbol sync lock : " + ls);

            System.out.println("Frames processed : " + upd.getNumFramesProcessed());
            System.out.println("OCF delivered    : " + upd.getNumOcfDelivered());
            ROCF_UpdateMode um;
            um = upd.getRequestedUpdateMode();
            System.out.println("Req Update Mode  : " + um);
            ROCF_ControlWordType cwt;
            cwt = upd.getRequestedControlWordType();
            System.out.println("Req CW Type      : " + cwt);

            boolean tcvcidUsed = upd.getTcVcidUsed();
            String res = tcvcidUsed ? "yes" : "no";
            System.out.println("TC VcId Used     : " + res);

            // Get Requested GvcId
            ROCF_Gvcid gvcid = upd.getRequestedGvcid();
            if (gvcid != null)
            {
                System.out.println("Req GVCID type   : " + gvcid.getType());
                System.out.println("Req GVCID scId   : " + gvcid.getScid());
                System.out.println("Req GVCID version: " + gvcid.getVersion());
                System.out.println("Req GVCID vcId   : " + gvcid.getVcid());
            }
            else
            {
                System.out.println("Requested GVCID is not set ");
            }

            System.out.println("Req TcVCID vcId   : " + upd.getRequestedTcVcid());

            System.out.println();
        }

        System.out.println();

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
            this.opGen = new EE_SYSTST_ROCFOpGen(this.opFactory, f, this.playback, this.utl);

            // #hd# shift setting the version to this place
            // SLE_VersionNumber vNum = _siAdmin->Get_Version();
            // _opGen->setVersion(vNum);
            this.opGen.setVersion(this.version);

        }

        return this.opGen;
    }

    @Override
    public void testTdReceive(long lg, int nbtime)
    {

        EE_Reference<DCW_Event_Type> et = new EE_Reference<>();
        EE_Reference<IUnknown> psi1 = new EE_Reference<>();
        EE_Reference<ISLE_Operation> pop = new EE_Reference<>();

        int to = 10;

        int count = 0, count_rocf;
        double lgrocf_tot = 0, rate, rate_prec = 0;

        ISLE_Time time = null, time1 = null;
        try
        {
            time = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
            time1 = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
        }
        catch (SleApiException e1)
        {
            LOG.log(Level.FINE, "SleApiException ", e1);
        }
        time1.update();

        if (nbtime > 0)
        {
            nbtime++;
        }

        while (true)
        {
            count = 0;
            count_rocf = 0;

            // wait for data transfer op
            while (true)
            {
                HRESULT rc = HRESULT.S_OK;
                try
                {
                    this.eventQueue.nextEvent(et, psi1, pop, to, 0);
                }
                catch (SleApiException e)
                {
                    rc = e.getHResult();
                }
                if (rc == HRESULT.S_OK)
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
                        count = 0;
                        System.out.println("Rcv " + pop.getReference().getOperationType() + " Rtn Op");
                    }
                    else if (et.getReference() == DCW_Event_Type.dcwEVT_informOpInvoke)
                    {
                        count = 0;
                        switch (pop.getReference().getOperationType())
                        {
                        case sleOT_transferData:
                        {

                            IROCF_TransferData optdrocf = pop.getReference().queryInterface(IROCF_TransferData.class);
                            byte[] data1 = new byte[4];
                            data1 = optdrocf.getData(); // length is always 4
                            lgrocf_tot += data1.length;

                          
                            if ((count_rocf % 100) == 0)
                            {
                                time.update();
                                double diff_time = time.subtract(time1);

                                if (diff_time > 10)
                                {

                                    rate = ((lgrocf_tot * 8) / (1024 * 1024));
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
                            count_rocf++;
                            break;
                        }
                        case sleOT_syncNotify:
                        {

                            IROCF_SyncNotify opsn = pop.getReference().queryInterface(IROCF_SyncNotify.class);
                            System.out.println("Rcv Sync Notify Inv Op. " + opsn.getNotificationType());

                            if (opsn.getNotificationType() == ROCF_NotificationType.rocfNT_endOfData)
                            {
                                time.update();
                                double diff_time = time.subtract(time1);

                                if (diff_time > 10)
                                {

                                    rate = ((lgrocf_tot * 8) / (1024 * 1024));
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
                count++;
                if (count == 10)
                {
                    return;
                }
            }

        }
    }

    @Override
    public void testTdSend(long lg, int nbtime, long delay_td, byte[] td_data, int delay)
    {

        ISLE_Operation op = null;
        IROCF_TransferData optdrocf;

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
        catch (SleApiException e2)
        {
            LOG.log(Level.FINE, "SleApiException ", e2);
        }
        time1.update();

        EE_SYSTST_OpGen pOpGen = getOpGen();
        int count = 0;

        boolean do_print = true;

        if (nbtime > 0)
        {
            nbtime++;
        }

        while (true)
        {
            count = 0;

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
                    catch (SleApiException e1)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e1);

                    }
                    IROCF_TransferData td = (IROCF_TransferData) op;
                    td.setDataLinkContinuity(25);
                    // td->Set_FrameQuality(rafFQ_good);
                    td.setData(td_data); // length is always 4
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
            count = 0;

            // wait
            while (true)
            {
                HRESULT rc = HRESULT.S_OK;
                try
                {
                    this.eventQueue.nextEvent(et, psi1, pop, to, 0);
                }
                catch (SleApiException e)
                {
                    rc = e.getHResult();
                }

                if (rc == HRESULT.S_OK)
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
                            optdrocf = pop.getReference().queryInterface(IROCF_TransferData.class);
                            byte[] data1 = optdrocf.getData(); // length is
                                                               // always 4
                            System.out.println("Rcv TD Rtn Op." + data1.length);
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
            count++;
            if (count == 20)
            {
                return;
            }
        }
    }

    private SLE_OpType getOpType(T_ROCFCmd cmd)
    {
        if (cmd == T_ROCFCmd.T_ROCFCmd_bind)
        {
            return SLE_OpType.sleOT_bind;
        }
        else if (cmd == T_ROCFCmd.T_ROCFCmd_unbind)
        {
            return SLE_OpType.sleOT_unbind;
        }
        else if (cmd == T_ROCFCmd.T_ROCFCmd_start)
        {
            return SLE_OpType.sleOT_start;
        }
        else if (cmd == T_ROCFCmd.T_ROCFCmd_stop)
        {
            return SLE_OpType.sleOT_stop;
        }
        else if (cmd == T_ROCFCmd.T_ROCFCmd_transfer_data)
        {
            return SLE_OpType.sleOT_transferData;
        }
        else if (cmd == T_ROCFCmd.T_ROCFCmd_sync_notify)
        {
            return SLE_OpType.sleOT_syncNotify;
        }
        else if (cmd == T_ROCFCmd.T_ROCFCmd_ssr)
        {
            return SLE_OpType.sleOT_scheduleStatusReport;
        }
        else if (cmd == T_ROCFCmd.T_ROCFCmd_get_prm)
        {
            return SLE_OpType.sleOT_getParameter;
        }
        else if (cmd == T_ROCFCmd.T_ROCFCmd_peer_abort)
        {
            return SLE_OpType.sleOT_peerAbort;
        }
        else
        {
            return SLE_OpType.sleOT_bind; // no better idea
        }
    }

    private IROCF_SIAdmin getROCFSIAdmin()
    {
        IROCF_SIAdmin ia = this.siAdmin.queryInterface(IROCF_SIAdmin.class);
        if (ia == null)
        {
            System.out.println("Interface IROCF_SIAdmin not available");
        }
        return ia;

    }

    private IROCF_SIUpdate getROCFSIUpdate()
    {
        IROCF_SIUpdate iu = this.siAdmin.queryInterface(IROCF_SIUpdate.class);
        if (iu == null)
        {
            System.out.println("Interface IROCF_SIUpdate not available");
        }
        return iu;
    }

    private String readLockStatus()
    {
        EE_Reference<String> arg = new EE_Reference<>();
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
        EE_Reference<String> arg = new EE_Reference<>();
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
        EE_Reference<String> arg = new EE_Reference<>();
        boolean isOk = false;

        while (isOk == false)
        {
            System.out.println("Delivery Mode (0 = rtnOnlineTimely, 1 = rtnOnlineComplete,");
            System.out.print("               2 = rtnOffline) ");
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

    private void setPermGvcIdList()
    {
        IROCF_SIAdmin adm = getROCFSIAdmin();
        if (adm != null)
        {
            EE_Reference<String> arg = new EE_Reference<>();
            System.out.println("How many GVCIds ? ");
            utl.read(arg, this.playback);
            int num = Integer.parseInt(arg.getReference());

            ROCF_Gvcid[] l = new ROCF_Gvcid[num];
            for (int i = 0; i < num; i++)
            {
                l[i] = new ROCF_Gvcid();
                ROCF_Gvcid id = new ROCF_Gvcid();
                System.out.println("Enter GCVID " + i);
                System.out.print("Type (0=Master,1=Virtual): ");
                utl.read(arg, this.playback);
                int type_i = Integer.parseInt(arg.getReference());
                id.setType(ROCF_ChannelType.getChannelTypeByCode(type_i));

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

                l[i].setType(id.getType());
                l[i].setScid(id.getScid());
                l[i].setVersion(id.getVersion());
                l[i].setVcid(id.getVcid());

            }
            UTL.traceIF1("IROCF_SIAdmin.setPermittedGvcidSet", Integer.toString(num));
            adm.setPermittedGvcidSet(l);
        }
        else
        {
            System.out.println("Could not get IROCF_SIAdmin interface ");
        }

    }

    private HRESULT autoSendTD()
    {
        HRESULT rc = HRESULT.S_OK;
        IUnknown piuk = this.siAdmin.queryInterface(IUnknown.class);
        ISLE_Operation op = getOpGen().createOp(SLE_OpType.sleOT_transferData, this.eventQueue, piuk);

        if (op != null)
        {
            IROCF_TransferData td = (IROCF_TransferData) op;

            System.out.println("How many TD operations ? ");
            EE_Reference<String> n = new EE_Reference<>();
            utl.read(n, this.playback);
            int i = Integer.parseInt(n.getReference());

            for (int j = 0; j < i; j++)
            {
                IROCF_TransferData tds = (IROCF_TransferData) td.copy();
                System.out.println("Send Invoke Operation. Seq " + this.seqCounter);
                rc = HRESULT.S_OK;
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

    private T_ROCFCmd getNextCommand(EE_Reference<String> arg1, EE_Reference<String> arg2, EE_Reference<String> arg3)
    {

        T_ROCFCmd nextCommand = T_ROCFCmd.T_ROCFCmd_Max;
        EE_Reference<String> cmd = new EE_Reference<String>();
        cmd.setReference("");

        System.out.println("EE_SYSTST_ROCFSIClient::getNextCommand");

        while (nextCommand == T_ROCFCmd.T_ROCFCmd_Max)
        {
            prompt();

            if (utl.read(cmd, this.playback) == false)
            {
                nextCommand = T_ROCFCmd.T_ROCFCmd_up;
                break;
            }

            for (int i = 0; i < T_ROCFCmd.T_ROCFCmd_Max.getCode(); i++)
            {
                if (cmd.getReference().equals(T_ROCFCmd.getT_ROCFCmdByCode(i).getCommand()))
                {
                    nextCommand = T_ROCFCmd.getT_ROCFCmdByCode(i);
                    break;
                }
            }
            if (nextCommand == T_ROCFCmd.T_ROCFCmd_Max)
            {
                // command not found, go to base-class
                super.processSLECommand(cmd.getReference());
                cmd.setReference("");
            }
        } // end while

        // ----------------------------------------------------
        if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_delivery_mode)
        {
            arg1.setReference(readDeliveryMode());
        }

        // ----------------------------------------------------
        else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_latency_limit)
        {
            System.out.print("Latency Limit (sec): ");
            utl.read(arg1, this.playback);
        }
        // -----------------------------------------------
        else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_buffer_size)
        {
            System.out.print("Buffer size (number of operation objects): ");
            utl.read(arg1, this.playback);

        }
        // -----------------------------------------------
        else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_prod_status)
        {
            System.out.print("Initial Production status: ");
            arg1.setReference(readProductionStatus());
        }

        // -----------------------------------------------
        else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_fs_lock
                 || nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_cdm_lock
                 || nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_scd_lock
                 || nextCommand == T_ROCFCmd.T_ROCFCmd_set_init_ss_lock)
        {
            arg1.setReference(readLockStatus());
        }

        // -----------------------------------------------
        else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_prod_status)
        {
            arg1.setReference(readProductionStatus());
        }

        // -----------------------------------------------
        else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_fs_lock || nextCommand == T_ROCFCmd.T_ROCFCmd_set_cdm_lock
                 || nextCommand == T_ROCFCmd.T_ROCFCmd_set_scd_lock || nextCommand == T_ROCFCmd.T_ROCFCmd_set_ss_lock)
        {
            arg1.setReference(readLockStatus());
        }
        // -----------------------------------------------
        else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_nframes_proc)
        {
            System.out.print("Number of frames processed: ");
            utl.read(arg1, this.playback);
        }
        // ----------------------------------------------------
        else if (nextCommand == T_ROCFCmd.T_ROCFCmd_set_min_rep_cycle)
        {
            System.out.print("Minimum reporting cycle (sec): ");
            utl.read(arg1, this.playback);
        }
        return nextCommand;
    }

}
