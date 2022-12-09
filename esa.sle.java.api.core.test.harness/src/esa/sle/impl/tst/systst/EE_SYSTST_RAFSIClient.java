package esa.sle.impl.tst.systst;

import java.util.ArrayList;
import java.util.List;
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
import ccsds.sle.api.isrv.iraf.IRAF_SITransferBufferControl;
import ccsds.sle.api.isrv.iraf.IRAF_SIUpdate;
import ccsds.sle.api.isrv.iraf.IRAF_SyncNotify;
import ccsds.sle.api.isrv.iraf.IRAF_TransferData;
import ccsds.sle.api.isrv.iraf.types.RAF_DeliveryMode;
import ccsds.sle.api.isrv.iraf.types.RAF_FrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_NotificationType;
import ccsds.sle.api.isrv.iraf.types.RAF_ParFrameQuality;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_RequestedFrameQuality;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.tst.systst.types.EE_SYSTST_T_Component;
import esa.sle.impl.tst.systst.types.T_RAFCmd;

public class EE_SYSTST_RAFSIClient extends EE_SYSTST_SIClient
{
    private static final Logger LOG = Logger.getLogger(EE_SYSTST_RAFSIClient.class.getName());

    private EE_SYSTST_RAFOpGen opGen;

    private final String[] helpCommand[] = {
                                            { "set_dm             IRAF_SIAdmin: set delivery mode" },
                                            { "set_ll             IRAF_SIAdmin: set latency limit" },
                                            { "set_mrc            IRAF_SIAdmin: set minimum reporting cycle" },  //Since SLES V5
                                            { "set_buffer_size    IRAF_SIAdmin: set transfer buffer size" },
                                            { "set_init_ps        IRAF_SIAdmin: set initial production status" },
                                            { "set_init_fsl       IRAF_SIAdmin: set initial frame sync lock" },
                                            { "set_init_cdml      IRAF_SIAdmin: set initial carrier demod lock" },
                                            { "set_init_scdl      IRAF_SIAdmin: set initial sub carrier demod lock" },
                                            { "set_init_ssl       IRAF_SIAdmin: set initial symbol sync lock" },
                                            { "set_pfq            IRAF_SIAdmin: set permitted frame quality"},
                                            { "set_ps             IRAF_SIUpdate: set production status" },
                                            { "set_fsl            IRAF_SIUpdate: set frame sync lock" },
                                            { "set_cdml           IRAF_SIUpdate: set carrier demod lock" },
                                            { "set_scdl           IRAF_SIUpdate: set subcarrier demod lock" },
                                            { "set_ssl            IRAF_SIUpdate: set symbol sync lock" },
                                            { "print              prints the values of the SIAdmin and SIUpdate parameters" },
                                            { "up                 back to the service element" },
                                            { "                                             " },
                                            { "bind         (u)   send RAF-BIND operation" },
                                            { "unbind       (u)   send RAF-UNBIND operation" },
                                            { "start        (u)   send RAF-START operation" },
                                            { "stop         (u)   send RAF-STOP operation" },
                                            { "td           (p)   send RAF-TRANSFER-DATA operation" },
                                            { "sb           (p)   force sending of Transfer Buffer" },
                                            { "sn           (p)   send RAF-SYNC-NOTIFY" },
                                            { "ssr          (u)   send RAF-SCHEDULE-STATUS-REPORT operation" },
                                            { "gp           (u)   send RAF-GET-PARAMETER operation" },
                                            { "peer_abort   (u/p) send RAF-PEER-ABORT operation" },
                                            { "auto_send_td (p)   send RAF-TRANSFER-DATA operations automatically" },
                                            { "auto_recv_td (u)   receive RAF-TRANSFER-DATA operations automatically" } };


    public EE_SYSTST_RAFSIClient(SLE_AppRole role, EE_SYSTST_TimeSource timeSource, UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_rtnAllFrames, role, timeSource, utl);
        this.opGen = null;
        this.playback = false;
    }

    @Override
    public EE_SYSTST_T_Component startUIF(boolean playback)
    {
        HRESULT rc = HRESULT.S_OK;
        T_RAFCmd nextCommand = T_RAFCmd.T_RAFCmd_Max;

        this.playback = playback;

        EE_Reference<String> arg1 = new EE_Reference<String>();
        EE_Reference<String> arg2 = new EE_Reference<String>();
        EE_Reference<String> arg3 = new EE_Reference<String>();

        while (true)
        {
            nextCommand = getNextCommand(arg1, arg2, arg3);

            // -----------------------------------------------
            if (nextCommand == T_RAFCmd.T_RAFCmd_up)
            {
                break;
            }

            // -----------------------------------------------

            else if (nextCommand == T_RAFCmd.T_RAFCmd_set_delivery_mode)
            {
                int dm = Integer.parseInt(arg1.getReference());
                RAF_DeliveryMode rafDm = RAF_DeliveryMode.getRAFDelModeByCode(dm);

                IRAF_SIAdmin adm = getRAFSIAdmin();
                if (adm != null)
                {
                    adm.setDeliveryMode(rafDm);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_RAFCmd.T_RAFCmd_set_latency_limit)
            {
                int ll = Integer.parseInt(arg1.getReference());
                IRAF_SIAdmin adm = getRAFSIAdmin();
                if (adm != null)
                {
                    adm.setLatencyLimit(ll);
                }
            }
            
            // -----------------------------------------------
            else if (nextCommand == T_RAFCmd.T_RAFCmd_set_min_rep_cycle) // New with SLES V5
            {
                long mrc = Integer.parseInt(arg1.getReference());
                IRAF_SIAdmin adm = getRAFSIAdmin();
                if (adm != null)
                {
                    adm.setMinimumReportCycle(mrc);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_RAFCmd.T_RAFCmd_set_buffer_size)
            {
                long bs = Long.parseLong(arg1.getReference());
                IRAF_SIAdmin adm = getRAFSIAdmin();
                if (adm != null)
                {
                    adm.setTransferBufferSize(bs);
                }

            }

            // -----------------------------------------------
            else if (nextCommand == T_RAFCmd.T_RAFCmd_set_init_prod_status)
            {
                int ps = Integer.parseInt(arg1.getReference());
                RAF_ProductionStatus rafPs = RAF_ProductionStatus.getProductionStatusByCode(ps);
                IRAF_SIAdmin adm = getRAFSIAdmin();
                if (adm != null)
                {
                    adm.setInitialProductionStatus(rafPs);
                }
            }
            // -----------------------------------------------
            else if (nextCommand == T_RAFCmd.T_RAFCmd_set_perm_frames_quality)
            {
            	// The input details for permitted frame quality are 
            	// gained from getNextCommand and are passed by arg1
            	// Playback specifies whether the input shall be printed to console.
            	String[] qualities = arg1.getReference().split(",");
            	RAF_ParFrameQuality[] pfqSet = new RAF_ParFrameQuality[qualities.length];
            	for(int i = 0; i < qualities.length ; i++)
            	{
            		int quality = Integer.parseInt(qualities[i]);
            		pfqSet[i]= RAF_ParFrameQuality.getRAFParFrameQualByCode(quality);
            	}


                IRAF_SIAdmin adm = getRAFSIAdmin();
                adm.setPermittedFrameQuality(pfqSet);
                System.out.println("");
            }
            // -----------------------------------------------
            else if (nextCommand == T_RAFCmd.T_RAFCmd_set_init_fs_lock
                     || nextCommand == T_RAFCmd.T_RAFCmd_set_init_cdm_lock
                     || nextCommand == T_RAFCmd.T_RAFCmd_set_init_scd_lock
                     || nextCommand == T_RAFCmd.T_RAFCmd_set_init_ss_lock)
            {
                int lsi = Integer.parseInt(arg1.getReference());
                RAF_LockStatus ls = RAF_LockStatus.getLockStatusByCode(lsi);
                IRAF_SIAdmin adm = getRAFSIAdmin();
                if (adm != null)
                {
                    if (nextCommand == T_RAFCmd.T_RAFCmd_set_init_fs_lock)
                    {
                        adm.setInitialFrameSyncLock(ls);
                    }
                    else if (nextCommand == T_RAFCmd.T_RAFCmd_set_init_cdm_lock)
                    {
                        adm.setInitialCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_RAFCmd.T_RAFCmd_set_init_scd_lock)
                    {
                        adm.setInitialSubCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_RAFCmd.T_RAFCmd_set_init_ss_lock)
                    {
                        adm.setInitialSymbolSyncLock(ls);
                    }
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_RAFCmd.T_RAFCmd_set_prod_status)
            {
                int ps = Integer.parseInt(arg1.getReference());
                RAF_ProductionStatus rafPs = RAF_ProductionStatus.getProductionStatusByCode(ps);
                IRAF_SIUpdate upd = getRAFSIUpdate();
                if (upd != null)
                {
                    upd.setProductionStatus(rafPs);
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_RAFCmd.T_RAFCmd_set_fs_lock || nextCommand == T_RAFCmd.T_RAFCmd_set_cdm_lock
                     || nextCommand == T_RAFCmd.T_RAFCmd_set_scd_lock || nextCommand == T_RAFCmd.T_RAFCmd_set_ss_lock)
            {
                int lsi = Integer.parseInt(arg1.getReference());
                RAF_LockStatus ls = RAF_LockStatus.getLockStatusByCode(lsi);
                IRAF_SIUpdate upd = getRAFSIUpdate();
                if (upd != null)
                {
                    if (nextCommand == T_RAFCmd.T_RAFCmd_set_fs_lock)
                    {
                        upd.setFrameSyncLock(ls);
                    }
                    else if (nextCommand == T_RAFCmd.T_RAFCmd_set_cdm_lock)
                    {
                        upd.setCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_RAFCmd.T_RAFCmd_set_scd_lock)
                    {
                        upd.setSubCarrierDemodLock(ls);
                    }
                    else if (nextCommand == T_RAFCmd.T_RAFCmd_set_ss_lock)
                    {
                        upd.setSymbolSyncLock(ls);
                    }
                }
            }
            // -----------------------------------------------
            else if (nextCommand == T_RAFCmd.T_RAFCmd_print_si)
            {
                printSI();
            }

            // -----------------------------------------------
            else if (nextCommand == T_RAFCmd.T_RAFCmd_dummy)
            {
                // do nothing
            }

            // -----------------------------------------------
            else if (nextCommand == T_RAFCmd.T_RAFCmd_bind || nextCommand == T_RAFCmd.T_RAFCmd_unbind
                     || nextCommand == T_RAFCmd.T_RAFCmd_start || nextCommand == T_RAFCmd.T_RAFCmd_stop
                     || nextCommand == T_RAFCmd.T_RAFCmd_transfer_data || nextCommand == T_RAFCmd.T_RAFCmd_sync_notify
                     || nextCommand == T_RAFCmd.T_RAFCmd_ssr || nextCommand == T_RAFCmd.T_RAFCmd_get_prm
                     || nextCommand == T_RAFCmd.T_RAFCmd_peer_abort)
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
                            if (nextCommand == T_RAFCmd.T_RAFCmd_bind)
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
                            if (nextCommand == T_RAFCmd.T_RAFCmd_transfer_data
                                || nextCommand == T_RAFCmd.T_RAFCmd_sync_notify)
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

                            if (nextCommand == T_RAFCmd.T_RAFCmd_peer_abort)
                            {
                                if (this.eventQueue != null)
                                {
                                    rc = HRESULT.S_OK;
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
                            rc = HRESULT.S_OK;
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
            else if (nextCommand == T_RAFCmd.T_RAFCmd_auto_gen_td)
            {
                rc = autoSendTD();
                displayResult(rc);
            }
            else if (nextCommand == T_RAFCmd.T_RAFCmd_auto_recv_td)
            {
                rc = autoRecvTD();
                displayResult(rc);
            }
            else if (nextCommand == T_RAFCmd.T_RAFCmd_send_buffer)
            {
                IRAF_SITransferBufferControl tbc = getRAFSITransferBufferControl();
                if (tbc != null)
                {
                    boolean withNotification = false;

                    boolean isOk = false;
                    EE_Reference<String> arg = new EE_Reference<String>();
                    while (isOk == false)
                    {
                        System.out.print("With Notification (0 = no, 1 = yes) ");
                        utl.read(arg, this.playback);

                        int smallBoolean = Integer.parseInt(arg.getReference());
                        if (smallBoolean != 0 && smallBoolean != 1)
                        {
                            System.out.println("Invalid With Notification value");
                        }
                        else
                        {
                            isOk = true;
                        }
                        withNotification = (smallBoolean == 0) ? false : true;

                    }
                    tbc.sendBufferTransfer(withNotification);
                }
            }

        } // end while nextCommand != T_RAFCmd_up
        return EE_SYSTST_T_Component.eeEE_SYSTST_TestSE;

    }

    @Override
    public void testTdReceive(long lg, int nbtime)
    {
        EE_Reference<DCW_Event_Type> et = new EE_Reference<>();
        EE_Reference<ISLE_Operation> pop = new EE_Reference<>();
        EE_Reference<IUnknown> psi1 = new EE_Reference<IUnknown>();

        int to = 10;

        double lgraf_tot = 0, rate, rate_prec = 0;

        ISLE_Time time = null;
        ISLE_Time time1 = null;
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
            int count_raf = 0;

            while (true)
            {

                // wait for data transfer op
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
                    System.out.println("Some event: " + et.getReference());
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
                            IRAF_TransferData optdraf = pop.getReference().queryInterface(IRAF_TransferData.class);
                            byte[] data1 = optdraf.getData();
                            lgraf_tot += data1.length;

                            if ((count_raf % 100) == 0)
                            {
                                time.update();
                                double diff_time = time.subtract(time1);

                                if (diff_time > 10)
                                {
                                    rate = ((lgraf_tot * 8) / (1024 * 1024));
                                    System.out.print("Received " + rate + " M bits in " + diff_time + " s" + " --> "
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
                            IRAF_SyncNotify opsn = pop.getReference().queryInterface(IRAF_SyncNotify.class);
                            System.out.println("Rcv Sync Notify Inv Op. " + opsn.getNotificationType());

                            if (opsn.getNotificationType() == RAF_NotificationType.rafNT_endOfData)
                            {
                                time.update();
                                double diff_time = time.subtract(time1);

                                if (diff_time > 10)
                                {
                                    rate = ((lgraf_tot * 8) / (1024 * 1024));
                                    System.out.print("Received " + rate + " M bits in " + diff_time + " s" + " --> "
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
                else
                {
                    System.out.println("No next event.");
                }

              

            }
        }
    }

    @Override
    protected void help()
    {
        super.help();

        for (int i = 0; i < T_RAFCmd.T_RAFCmd_Max.getCode(); i++)
        {
            System.out.println("   " + this.helpCommand[i]);
        }
        System.out.println();
    }

    /**
     * Prints out the configuration of the SI
     * on user input 'print'.
     */
    @Override
    public void printSI()
    {
        super.printSI();

        IRAF_SIAdmin adm = getRAFSIAdmin();
        if (adm != null)
        {
            RAF_DeliveryMode dm = adm.getDeliveryMode();
            System.out.println("Delivery Mode    : " + dm);

            System.out.println("Latency Limit    : " + adm.getLatencyLimit());
            System.out.println("Buffer Size      : " + adm.getTransferBufferSize());
            System.out.println("Min report cycle : " + adm.getMinimumReportCycle());
            System.out.print  ("Perm frame qual  : ");
            if(adm.getPermittedFrameQuality() != null)
            {
            	for( RAF_ParFrameQuality permFrameQual : adm.getPermittedFrameQuality())
            	{
            		if(permFrameQual != null)
            		{
            			System.out.print(permFrameQual.toString() + " ");
            		}

            	}
            }
            System.out.println();

        }

        IRAF_SIUpdate upd = getRAFSIUpdate();
        if (upd != null)
        {
            RAF_ProductionStatus ps = upd.getProductionStatus();
            System.out.println("Production status  : " + ps);

            RAF_LockStatus ls = upd.getFrameSyncLock();
            System.out.println("Frame sync lock    : " + ls);

            ls = upd.getCarrierDemodLock();
            System.out.println("Carrier demod lck  : " + ls);

            ls = upd.getSubCarrierDemodLock();
            System.out.println("Sub Carr dem lock  : " + ls);

            ls = upd.getSymbolSyncLock();
            System.out.println("Symbol sync lock   : " + ls);

            System.out.println("Error free frames  : " + upd.getNumErrorFreeFrames());
            System.out.println("Frames delivered   : " + upd.getNumFrames());

            RAF_ParFrameQuality fq = upd.getRequestedFrameQuality();
            System.out.println("Req Frame quality  : " + fq);     
            
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
            this.opGen = new EE_SYSTST_RAFOpGen(this.opFactory, f, this.playback, this.utl);

            // #hd# shift setting the version to this place
            int vNum = this.siAdmin.getVersion();
            this.opGen.setVersion(vNum);
        }
        return this.opGen;
    }

    @Override
    public void testTdSend(long lg, int nbtime, long delay_td, byte[] td_data, int delay)
    {

        ISLE_Operation op = null;
        IRAF_TransferData optdraf;

        EE_Reference<DCW_Event_Type> et = new EE_Reference<>();
        EE_Reference<IUnknown> psi1 = new EE_Reference<>();
        EE_Reference<ISLE_Operation> pop = new EE_Reference<>();

        int to = 1;

        ISLE_Time time = null, time1 = null;
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
                    res = HRESULT.S_OK;
                    IRAF_TransferData td = null;
                    try
                    {
                        op = pOpGen.siOPF.createOperation(ISLE_Operation.class, SLE_OpType.sleOT_transferData);
                    }
                    catch (SleApiException e)
                    {
                        res = e.getHResult();
                    }
                    if (res == HRESULT.S_OK)
                    {
                        td = (IRAF_TransferData) op;
                    }
                    td.setDataLinkContinuity(25);
                    td.setFrameQuality(RAF_FrameQuality.rafFQ_good);
                    td.setData(td_data);
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
                        System.out.print("Send TD Op. Seq " + this.seqCounter + ". lg " + lg);
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
                        // cout << "Rcv Resume DT" << endl;
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
                            optdraf = pop.getReference().queryInterface(IRAF_TransferData.class);
                            byte[] data1 = optdraf.getData();
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
        }

    }

    private T_RAFCmd getNextCommand(EE_Reference<String> arg1, EE_Reference<String> arg2, EE_Reference<String> arg3)
    {

        T_RAFCmd nextCommand = T_RAFCmd.T_RAFCmd_Max;
        EE_Reference<String> cmd = new EE_Reference<String>();
        cmd.setReference("");

        while (nextCommand == T_RAFCmd.T_RAFCmd_Max)
        {
            prompt();
            if (utl.read(cmd, this.playback) == false)
            {
                nextCommand = T_RAFCmd.T_RAFCmd_up;
                break;
            }

            for (int i = 0; i < T_RAFCmd.T_RAFCmd_Max.getCode(); i++)
            {
                if (T_RAFCmd.getRAFCmdByCode(i).toString().equals(cmd.getReference()))
                {
                    nextCommand = T_RAFCmd.getRAFCmdByCode(i);
                    break;
                }
            }

            if (nextCommand == T_RAFCmd.T_RAFCmd_Max)
            {
                // command not found, go to base-class
                processSLECommand(cmd.getReference());
                cmd.setReference("");
            }
        } // end while

        // ----------------------------------------------------
        if (nextCommand == T_RAFCmd.T_RAFCmd_set_delivery_mode)
        {
            arg1.setReference(readDeliveryMode());
        }

        // ----------------------------------------------------
        else if (nextCommand == T_RAFCmd.T_RAFCmd_set_latency_limit)
        {
            System.out.print("Latency Limit (sec): ");
            utl.read(arg1, this.playback);
        }
        // -----------------------------------------------
        else if (nextCommand == T_RAFCmd.T_RAFCmd_set_buffer_size)
        {
            System.out.print("Buffer size (number of operation objects): ");
            utl.read(arg1, this.playback);

        }
        // -----------------------------------------------
        else if (nextCommand == T_RAFCmd.T_RAFCmd_set_init_prod_status)
        {
            System.out.println("Initial Production status:");
            arg1.setReference(readProductionStatus());
        }
     // ----------------------------------------------------
        else if (nextCommand == T_RAFCmd.T_RAFCmd_set_min_rep_cycle)
        {
            System.out.print("Minimum repetition cycle: ");
            utl.read(arg1, this.playback);
        }
        // -----------------------------------------------
        else if (nextCommand == T_RAFCmd.T_RAFCmd_set_perm_frames_quality)
        {
        	// The input prompt is done in readPermittedFrameQulaity()
            arg1.setReference(readPermittedFrameQuality());
        }
        // -----------------------------------------------
        else if (nextCommand == T_RAFCmd.T_RAFCmd_set_init_fs_lock
                 || nextCommand == T_RAFCmd.T_RAFCmd_set_init_cdm_lock
                 || nextCommand == T_RAFCmd.T_RAFCmd_set_init_scd_lock
                 || nextCommand == T_RAFCmd.T_RAFCmd_set_init_ss_lock)
        {
            arg1.setReference(readLockStatus());
        }

        // -----------------------------------------------
        else if (nextCommand == T_RAFCmd.T_RAFCmd_set_prod_status)
        {
            arg1.setReference(readProductionStatus());
        }

        // -----------------------------------------------
        else if (nextCommand == T_RAFCmd.T_RAFCmd_set_fs_lock || nextCommand == T_RAFCmd.T_RAFCmd_set_cdm_lock
                 || nextCommand == T_RAFCmd.T_RAFCmd_set_scd_lock || nextCommand == T_RAFCmd.T_RAFCmd_set_ss_lock)
        {
            arg1.setReference(readLockStatus());
        }

        return nextCommand;
    }

    private SLE_OpType getOpType(T_RAFCmd cmd)
    {
        if (cmd == T_RAFCmd.T_RAFCmd_bind)
        {
            return SLE_OpType.sleOT_bind;
        }
        else if (cmd == T_RAFCmd.T_RAFCmd_unbind)
        {
            return SLE_OpType.sleOT_unbind;
        }
        else if (cmd == T_RAFCmd.T_RAFCmd_start)
        {
            return SLE_OpType.sleOT_start;
        }
        else if (cmd == T_RAFCmd.T_RAFCmd_stop)
        {
            return SLE_OpType.sleOT_stop;
        }
        else if (cmd == T_RAFCmd.T_RAFCmd_transfer_data)
        {
            return SLE_OpType.sleOT_transferData;
        }
        else if (cmd == T_RAFCmd.T_RAFCmd_sync_notify)
        {
            return SLE_OpType.sleOT_syncNotify;
        }
        else if (cmd == T_RAFCmd.T_RAFCmd_ssr)
        {
            return SLE_OpType.sleOT_scheduleStatusReport;
        }
        else if (cmd == T_RAFCmd.T_RAFCmd_get_prm)
        {
            return SLE_OpType.sleOT_getParameter;
        }
        else if (cmd == T_RAFCmd.T_RAFCmd_peer_abort)
        {
            return SLE_OpType.sleOT_peerAbort;
        }
        else
        {
            return SLE_OpType.sleOT_bind; // no better idea
        }
    }

    private IRAF_SIAdmin getRAFSIAdmin()
    {
        IRAF_SIAdmin ia = this.siAdmin.queryInterface(IRAF_SIAdmin.class);
        if (ia == null)
        {
            System.out.println("Interface IRAF_SIAdmin not available");
        }
        return ia;
    }

    private IRAF_SIUpdate getRAFSIUpdate()
    {
        IRAF_SIUpdate iu = this.siAdmin.queryInterface(IRAF_SIUpdate.class);
        if (iu == null)
        {
            System.out.println("Interface IRAF_SIUpdate not available");
        }
        return iu;
    }

    private IRAF_SITransferBufferControl getRAFSITransferBufferControl()
    {
        IRAF_SIAdmin rafSiAdm = getRAFSIAdmin();

        if (rafSiAdm != null)
        {
            IRAF_SITransferBufferControl itbc = rafSiAdm.queryInterface(IRAF_SITransferBufferControl.class);
            if (itbc == null)
            {
                System.out.println("Interface IRAF_SITransferBufferControl not available");
            }
            return itbc;
        }
        return null;
    }

    private String readLockStatus()
    {
        EE_Reference<String> arg = new EE_Reference<String>();
        boolean isOk = false;

        while (isOk == false)
        {
            System.out.print("Lock Status: (0=inLock, 1=outOfLock, 2=notInUse, 3=unknown) ");
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
    
    /**
     * Prints out the prompt for Permitted Frame Quality input details
     * and checks the input.
     * @return
     */
    private String readPermittedFrameQuality()
    {
    	EE_Reference<String> arg = new EE_Reference<String>();
        boolean isOk = false;

        while (isOk == false)
        {
        	System.out.print("Permitted Frame Quality (comma-sep, no spaces): (0=good, 1=error, 2=all):");
        	utl.read(arg, this.playback);

        	String[] qualities = arg.getReference().split(",");
            if (qualities.length < 1 || qualities.length > 3)
            {
            	System.out.println("Invalid Permitted Frame Quality Set size. Size 1 .. 3 ");
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

    private HRESULT autoSendTD()
    {
        HRESULT rc = HRESULT.S_OK;

        IUnknown piuk = this.siAdmin.queryInterface(IUnknown.class);
        ISLE_Operation op = getOpGen().createOp(SLE_OpType.sleOT_transferData, this.eventQueue, piuk);
        ISLE_Time time = null;

        int nSuspendResume = 0;

        if (op != null)
        {
            IRAF_TransferData td = (IRAF_TransferData) op;

            try
            {
                time = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
            }
            catch (SleApiException e1)
            {
                LOG.log(Level.FINE, "time is null ", e1);
            }
            EE_SYSTST_OpGen pOpGen = getOpGen();

            byte[] data = td.getData();

            EE_Reference<String> n = new EE_Reference<String>();
            System.out.print("Maximum waiting time for tranfer resume: ");
            utl.read(n, this.playback);
            int wtime = Integer.parseInt(n.getReference());

            System.out.print("Number of TD operations: ");
            utl.read(n, this.playback);
            int i = Integer.parseInt(n.getReference());

            System.out.print("Status output every n operations (0 = off): ");
            utl.read(n, this.playback);
            int stat_out = Integer.parseInt(n.getReference());

            EE_Reference<ISLE_Operation> pop = new EE_Reference<ISLE_Operation>();
            EE_Reference<DCW_Event_Type> et = new EE_Reference<DCW_Event_Type>();
            EE_Reference<IUnknown> psi1 = new EE_Reference<IUnknown>();

            rc = HRESULT.S_OK;
            for (int j = 0; j < i && rc == HRESULT.S_OK; j++)
            {
                try
                {
                    op = pOpGen.siOPF.createOperation(ISLE_Operation.class, SLE_OpType.sleOT_transferData);
                }
                catch (SleApiException e1)
                {
                    LOG.log(Level.FINE, "op si null ", e1);
                }

                IRAF_TransferData tds = (IRAF_TransferData) op;
                tds.setDataLinkContinuity(td.getDataLinkContinuity());
                tds.setFrameQuality(td.getFrameQuality());

                tds.setData(data);
                String antennaId = td.getAntennaIdGFString();
                tds.setAntennaIdGFString(antennaId);

                time.update();
                tds.setEarthReceiveTime(time);

                if ((stat_out > 0) && (((j + 1) % stat_out) == 0))
                {
                    System.out.println("Transfer Data Operation sent " + (j + 1));
                }

                rc = HRESULT.S_OK;
                try
                {
                    this.srvInit.initiateOpInvoke(tds, this.seqCounter++);
                }
                catch (SleApiException e)
                {
                    rc = e.getHResult();
                }
                switch (rc)
                {
                case S_OK:
                    System.out.println("  OK");
                    break;
                case SLE_S_SUSPEND:
                    System.out.println("  Suspended ");

                    rc = HRESULT.S_OK;
                    try
                    {
                        this.eventQueue.nextEvent(et, psi1, pop, wtime, 0);
                    }
                    catch (SleApiException e)
                    {
                        rc = e.getHResult();
                    }
                    if (rc == HRESULT.S_OK)
                    {
                        switch (et.getReference())
                        {
                        case dcwEVT_resumeDataTransfer:
                            // cout << " Resume" << endl;
                            nSuspendResume++;
                            rc = HRESULT.S_OK;
                            break;
                        default:
                            System.out.println("\nUnexpected event received: " + et.getReference());
                            break;
                        }
                    }
                    else
                    {
                        System.out.println("\nCould not retrieve Event!");
                    }

                default:
                    System.out.println("  Error!");
                }
            }
        }
        else
        {
            rc = HRESULT.E_FAIL;
        }

        System.out.println("\n\n**********************************************************");
        System.out.println(nSuspendResume + " Transfer suspend / resume events");
        System.out.println("**********************************************************\n\n");
        return rc;
    }

    private HRESULT autoRecvTD()
    {
        HRESULT rc = HRESULT.S_OK;

        EE_Reference<String> n = new EE_Reference<String>();
        System.out.print("Maximum waiting time: ");
        utl.read(n, this.playback);
        int wtime = Integer.parseInt(n.getReference());

        System.out.print("Number of TD operations: ");
        utl.read(n, this.playback);
        int nb_event = Integer.parseInt(n.getReference());

        System.out.println("Status output every n operations (0 = off): ");
        utl.read(n, this.playback);
        int stat_out = Integer.parseInt(n.getReference());

        EE_Reference<IUnknown> psi1 = new EE_Reference<IUnknown>();
        EE_Reference<DCW_Event_Type> et = new EE_Reference<DCW_Event_Type>();
        EE_Reference<ISLE_Operation> pop = new EE_Reference<ISLE_Operation>();

        SLE_OpType ot;
        int i = 0;

        long data_sum = 0;

        long start_time = 0;
        long stop_time = 0;

        while (nb_event > 0 && rc == HRESULT.S_OK)
        {
            pop.setReference(null);
            HRESULT res = HRESULT.S_OK;
            try
            {
                this.eventQueue.nextEvent(et, psi1, pop, wtime, 0);
            }
            catch (SleApiException e)
            {
                res = e.getHResult();
            }
            if ((res == HRESULT.S_OK) && pop.getReference() != null)
            {

                switch (et.getReference())
                {
                case dcwEVT_informOpInvoke:
                {
                    ot = pop.getReference().getOperationType();
                    if (ot != SLE_OpType.sleOT_transferData)
                    {
                        System.out.println("Wrong operation received " + ot);
                        rc = HRESULT.E_FAIL;
                    }
                    else
                    {

                        if (i == 0)
                        {
                            start_time = System.currentTimeMillis() - THApiexe.stime;
                        }

                        i++;
                        IRAF_TransferData tds = (IRAF_TransferData) pop;
                        byte[] data = tds.getData();
                        data_sum += data.length;
                        if ((stat_out > 0) && ((i % stat_out) == 0))
                        {
                            System.out.println("Transfer Data received " + i);
                        }
                    }
                    break;
                }
                default:
                    System.out.println("Wrong event received " + et);
                    rc = HRESULT.E_FAIL;
                    break;
                }
            }

        }

        stop_time = System.currentTimeMillis() - THApiexe.stime;
        double difftime = (stop_time - start_time) / 1000.0;

        System.out.print("\n\n**********************************************************");
        System.out.println(i + " Transfer Data Operations received");
        System.out.println(data_sum + " Octets received");
        if (i > 0)
        {
            System.out.println(difftime + " Seconds required");
            double drate = data_sum / difftime;
            System.out.println(drate + " Octets/sec");

            System.out.println(drate / 131072.0 + " mbit/sec");
        }
        System.out.println("**********************************************************\n\n");
        return rc;
    }

}
