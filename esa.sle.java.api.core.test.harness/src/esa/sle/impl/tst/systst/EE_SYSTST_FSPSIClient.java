package esa.sle.impl.tst.systst;

import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
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
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SlduStatusNotification;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.ICLTU_SIAdmin;
import ccsds.sle.api.isrv.icltu.types.CLTU_ChannelType;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwGvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_ConfType;
import ccsds.sle.api.isrv.icltu.types.CLTU_GvcId;
import ccsds.sle.api.isrv.ifsp.IFSP_AsyncNotify;
import ccsds.sle.api.isrv.ifsp.IFSP_FOPMonitor;
import ccsds.sle.api.isrv.ifsp.IFSP_SIAdmin;
import ccsds.sle.api.isrv.ifsp.IFSP_SIUpdate;
import ccsds.sle.api.isrv.ifsp.IFSP_ThrowEvent;
import ccsds.sle.api.isrv.ifsp.IFSP_TransferData;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_BlockingUsage;
import ccsds.sle.api.isrv.ifsp.types.FSP_ChannelType;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwGvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.ifsp.types.FSP_EventResult;
import ccsds.sle.api.isrv.ifsp.types.FSP_Failure;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopAlert;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopState;
import ccsds.sle.api.isrv.ifsp.types.FSP_GvcId;
import ccsds.sle.api.isrv.ifsp.types.FSP_ConfType;
import ccsds.sle.api.isrv.ifsp.types.FSP_MuxScheme;
import ccsds.sle.api.isrv.ifsp.types.FSP_NotificationType;
import ccsds.sle.api.isrv.ifsp.types.FSP_PacketStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_PermittedTransmissionMode;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_TimeoutType;
import ccsds.sle.api.isrv.ifsp.types.FSP_TransmissionMode;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;
import esa.sle.impl.ifs.gen.EE_CondVar;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.ifs.time.EE_Duration;
import esa.sle.impl.ifs.time.EE_ElapsedTimer;
import esa.sle.impl.ifs.time.EE_TIME_Prec;
import esa.sle.impl.tst.systst.types.EE_SYSTST_T_Component;

public class EE_SYSTST_FSPSIClient extends EE_SYSTST_SIClient implements ISLE_TimeoutProcessor
{

    private static final Logger LOG = Logger.getLogger(EE_SYSTST_FSPSIClient.class.getName());

    private EE_SYSTST_FSPOpGen opGen;

    private long lastRecFspId;

    private long currentFspBufferSize;

    @SuppressWarnings("unused")
    private boolean stop_thread;

    private int lg_radiated;

    private EE_CondVar EE_CondVar_Notification;

    Lock objMutex = new ReentrantLock();

    private int last_fsp;

    private final TreeMap<Integer, EE_ElapsedTimer> mapElapsedTimer = new TreeMap<Integer, EE_ElapsedTimer>();


    public EE_SYSTST_FSPSIClient(SLE_AppRole role, EE_SYSTST_TimeSource timeSource, UTL utl)
    {
        super(SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt, role, timeSource, utl);

        this.opGen = null;
        this.playback = false;

        this.lastRecFspId = 0;
        this.currentFspBufferSize = 0;
        this.stop_thread = false;
        this.lg_radiated = 0;
    }

    private enum T_FSPCmd
    {
        T_FSPCmd_set_max_buffer_size(0, "set_mbs", "set max buffer size"),
        T_FSPCmd_set_max_packet_length(1, "set_mpl", "set max packet length"),
        T_FSPCmd_set_max_frame_length(2, "set_mfl", "set max frame length"),
        T_FSPCmd_set_init_prod_status(3, "set_init_ps", "set initial production status"),
        T_FSPCmd_set_apid_list(4, "set_apidl", "set APID list"),
        T_FSPCmd_set_bit_lock_required(5, "set_blr", "set bit lock required"),
        T_FSPCmd_set_blocking_timeout(6, "set_bto", "set blocking timeout"),
        T_FSPCmd_set_blocking_usage(7, "set_bu", "set blocking usage"),
        T_FSPCmd_set_directive_enabled(8, "set_die", "set directive invoc. enabled"),
        T_FSPCmd_set_init_directive_online(9, "set_init_dio", "set initial directive invoc. online"),
        T_FSPCmd_set_map_list(10, "set_mapl", "set MAP list"),
        T_FSPCmd_set_rf_available_required(11, "set_rfar", "set rf available required"),
        T_FSPCmd_set_segment_header_present(12, "set_shp", "set segment header present"),
        T_FSPCmd_set_vc_mux_scheme(13, "set_vcms", "set VC mux scheme"),
        T_FSPCmd_set_vc_polling_vector(14, "set_vcpv", "set VC polling vector"),
        T_FSPCmd_set_vc_priority_list(15, "set_vcpril", "set VC priority list"),
        T_FSPCmd_set_vc(16, "set_vc", "set VC"),
        T_FSPCmd_set_perm_transmission_mode(17, "set_ptm", "set perm transmission mode"),
        T_FSPCmd_set_clcw_gvcid(52,    		"set_cgv", "set clcw global vcid"),
        T_FSPCmd_set_clcw_phy_chan(53, 		"set_cpc", "set clcw physical channel"),
        T_FSPCmd_set_min_report_cycle(54,	"set_mrc", "set minimum reporting cycle" ),
        T_FSPCmd_set_seq_cntr_frames_rep(55,"set_scfr","set sequence control frames repetition"),
        T_FSPCmd_set_cop_cntr_frames_rep(56,"set_ccfr","set cop control frames repetition"),
        T_FSPCmd_set_throw_event_enabled(57,"set_teoe","set throwing event operation enabled"),
        T_FSPCmd_set_repetition_limit(58, "set_rl","set repetition limit"  ),

        T_FSPCmd_set_sliding_window(18, "set_slw", "Set FOP sliding window"),
        T_FSPCmd_set_timeout_type(19, "set_tot", "Set timeout type"),
        T_FSPCmd_set_timer_init(20, "set_tinit", "Set timer initial"),
        T_FSPCmd_set_transm_limit(21, "set_trl", "Set transmission limit"),
        T_FSPCmd_set_transm_fr_seq_count(22, "set_tfsc", "Set transmitter frame sequence count"),
        T_FSPCmd_set_fops(23, "set_fops", "Set FOP state"),
        T_FSPCmd_set_map_mux_scheme(24, "set_muxs", "Set MAP multiplex scheme"),
        T_FSPCmd_set_map_polling_vector(25, "set_muxpv", "Set MAP polling vector"),
        T_FSPCmd_set_map_priority_list(26, "set_muxpril", "Set MAP priority list"),

        T_FSPCmd_pkt_started(27, "pkt_started", "PacketStarted"),
        T_FSPCmd_pkt_not_started(28, "pkt_ns", "PacketNotStarted"),
        T_FSPCmd_pkt_radiated(29, "pkt_rad", "PacketRadiated"),
        T_FSPCmd_pkt_acknowleged(30, "pkt_ack", "PacketAcknowleged"),
        T_FSPCmd_prod_status_change(31, "set_ps", "ProductionStatusChange"),
        T_FSPCmd_vc_abort(32, "vc_abort", "VCAborted"),
        T_FSPCmd_no_dir_capability(33, "no_dir", "NoDirectiveCapability"),
        T_FSPCmd_buffer_empty(34, "buffer_empty", "BufferEmpty"),
        T_FSPCmd_event_proc_completed(35, "evt_proc_compl", "EventProcCompleted"),
        T_FSPCmd_directive_completed(36, "dir_compl", "DirectiveCompleted"),
        T_FSPCmd_directive_online(37, "dir_online", "DirectiveCapabilityOnline"),
        T_FSPCmd_print_si(38, "print", "print contents of the FSP SI"),
        T_FSPCmd_up(39, "up", "up to service element commanding"),

        
        // OPERATION commands must stay in this order!
        T_FSPCmd_bind(40, "bind", "(u)  send FSP-BIND operation"),
        T_FSPCmd_unbind(41, "unbind", "(u)  send FSP-UNBIND operation"),
        T_FSPCmd_start(42, "start", "(u)  send FSP-START operation"),
        T_FSPCmd_stop(43, "stop", "(u)  send FSP-STOP operation"),
        T_FSPCmd_transfer_data(44, "td", "(u)  send FSP-TRANSFER-DATA operation"),
        T_FSPCmd_async_notify(45, "an", "(p)  send FSP-ASYNC-NOTIFY operation"),
        T_FSPCmd_ssr(46, "ssr", "(u)  send FSP-SCHEDULE-STATUS-REPORT operation"),
        T_FSPCmd_get_prm(47, "gp", "(u)  send FSP-GET-PARAMETER operation"),
        T_FSPCmd_throw_event(48, "te", "(u)  send FSP-THROW-EVENT operation "),
        T_FSPCmd_invoke_dir(49, "dir", "(u)  send FSP-INVOKE-DIRECTIVE operation "),
        T_FSPCmd_peer_abort(50, "peer_abort", "(u/p) send FSP-PEER-ABORT operation"),

        T_FSPCmd_auto_gen_td(51, "auto_gen_td", "(u)  send FSP-TRANSFER-DATA operations automatically"),
        
        T_FSPCmd_dummy(59, "----", "FSP"),
        T_FSPCmd_Max(60, "", ""); // can be used for invalid and/or max num of
                                  // commands

        private int code;

        private String command;

        private String help;


        private T_FSPCmd(int code, String command, String help)
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

        public static T_FSPCmd getT_ROCFCmdByCode(int code)
        {
            for (T_FSPCmd e : values())
            {
                if (e.code == code)
                {
                    return e;
                }
            }

            return null;
        }
    }


    @Override
    public void printSI()
    {
        super.printSI();

        SLE_YesNo yn;

        IFSP_SIAdmin adm = getFSPSIAdmin();

        int i;
        if (adm != null)
        {
            long bs = adm.getMaximumBufferSize();
            System.out.println("Max buffer size       : " + bs);
            long ml = adm.getMaximumPacketLength();
            System.out.println("Max Packet length     : " + ml);
            ml = adm.getMaximumFrameLength();
            System.out.println("Max Frame length      : " + ml);
            ml = adm.getBlockingTimeout();
            System.out.println("Blocking Timeout      : " + ml);
            FSP_BlockingUsage bu = adm.getBlockingUsage();
            System.out.println("Blocking Usage        : " + bu);
            yn = adm.getDirectiveInvocationEnabled();
            System.out.println("Dir. Inv. Enabled     : " + yn);
            yn = adm.getSegmentHeaderPresent();
            System.out.println("Segm. Hdr present     : " + yn);
            yn = adm.getBitLockRequired();
            System.out.println("Bit Lock Required     : " + yn);
            yn = adm.getRfAvailableRequired();
            System.out.println("Rf Available Required : " + yn);
            FSP_MuxScheme ms = adm.getVcMuxScheme();
            System.out.println("VC Mux scheme         : " + ms);
            ml = adm.getVirtualChannel();
            System.out.println("VC                    : " + ml);
            FSP_PermittedTransmissionMode ptm = adm.getPermittedTransmissionMode();
            System.out.println("Perm Transmiss Mode   : " + ptm);

            long[] pApId = adm.getApIdList();
            int vSize = pApId.length;
            System.out.print("APID List             : ");
            if (pApId != null)
            {
                for (i = 0; i < vSize; i++)
                {
                    if (i > 0)
                    {
                        System.out.print(",");
                    }
                    System.out.print(pApId[i]);
                }
            }
            System.out.println();

            long[] pMapId = adm.getMapList();
            System.out.print("MAP List              : ");
            if (pMapId != null)
            {
                vSize = pMapId.length;
                for (i = 0; i < vSize; i++)
                {
                    if (i > 0)
                    {
                        System.out.print(",");
                    }
                    System.out.print(pMapId[i]);
                }
            }
            System.out.println();

            long[] pVcId = adm.getVcPollingVector();
            System.out.print("VC Polling Vector     : ");
            if (pVcId != null)
            {
                vSize = pVcId.length;
                for (i = 0; i < vSize; i++)
                {
                    if (i > 0)
                    {
                        System.out.print(",");
                    }
                    System.out.print(pVcId[i]);
                }
            }
            System.out.println();

            FSP_AbsolutePriority[] pPrioList = adm.getVcPriorityList();
            System.out.println("VC Priority List      : ");
            if (pPrioList != null)
            {
                vSize = pPrioList.length;
                for (i = 0; i < vSize; i++)
                {
                    if (i > 0)
                    {
                        System.out.println(":");
                    }
                    System.out.print(pPrioList[i].getMapOrVc() + "," + pPrioList[i].getPriority());
                }
            }
            System.out.println();
            FSP_ClcwGvcId cgv = adm.getClcwGvcId();
            if (cgv != null && cgv.getConfigType() == FSP_ConfType.fspCT_configured)
            {
                System.out.println("Clcw Global VCID: ");
                System.out.println(" type = " + cgv.getGvcId().getType().toString() + ", ");
                System.out.println(" scId = " + cgv.getGvcId().getScid() + ", ");
                System.out.println(" version = " + cgv.getGvcId().getVersion() + ", ");
                System.out.println(" vcId = " + cgv.getGvcId().getVcid());
            }
            else
            {
            	System.out.println("Clcw Global VCID       : NotConfigured");
            }


            System.out.println("Clcw Physical Channel  : " + adm.getClcwPhysicalChannel());
            System.out.println("Min. Reporting Cycle   : " + adm.getMinimumReportingCycle());
            System.out.println("Cop Ctrl Frames Rep.   : " + adm.getCopCntrFramesRepetition());
            System.out.println("Seq Ctrl Frames Rep.   : " + adm.getSeqCntrFramesRepetition());
            System.out.println("Throw Event Op. enabled: " + adm.getThrowEventOperation());
            System.out.println("Repetition limit       : " + adm.getRepetitionLimit());
        }

        IFSP_FOPMonitor fopm = getFSPFOPMonitor();
        if (fopm != null)
        {
            long ul = fopm.getFopSlidingWindowWidth();
            System.out.println("Sliding Wdw Width     : " + ul);
            FSP_FopState fops = fopm.getFopState();
            System.out.println("FOP state             : " + fops);
            FSP_MuxScheme ms = fopm.getMapMuxScheme();
            System.out.println("MAP Mux scheme        : " + ms);

            long[] pMapId = fopm.getMapPollingVector();
            System.out.print("MAP Polling Vector    : ");

            if (pMapId != null)
            {
                int vSize = pMapId.length;
                for (i = 0; i < vSize; i++)
                {
                    if (i > 0)
                    {
                        System.out.print(",");
                    }
                    System.out.println(pMapId[i]);
                }
            }
            System.out.println();

            FSP_AbsolutePriority[] pPrioList = fopm.getMapPriorityList();
            System.out.print("MAP Priority List     : ");
            if (pPrioList != null)
            {
                int vSize = pPrioList.length;
                for (i = 0; i < vSize; i++)
                {
                    if (i > 0)
                    {
                        System.out.print(":");
                    }
                    System.out.print(pPrioList[i].getMapOrVc() + "," + pPrioList[i].getPriority());
                }
            }
            System.out.println();

            FSP_TimeoutType tt = fopm.getTimeoutType();
            System.out.println("Timeout Type          : " + tt);
            ul = fopm.getTimerInitial();
            System.out.println("Timer Initial         : " + ul);
            ul = fopm.getTransmissionLimit();
            System.out.println("Transmission Limit    : " + ul);
            ul = fopm.getTransmitterFrameSequenceNumber();
            System.out.println("Transm. Frm Seq No    : " + ul);

            System.out.println();
        }

        IFSP_SIUpdate upd = getFSPSIUpdate();
        if (upd != null)
        {

            long bs = upd.getPacketBufferAvailable();
            System.out.println("Avail buffer size     : " + bs);

            long npkt;

            npkt = upd.getNumberOfADPacketsReceived();
            System.out.println("AD Packets received   : " + npkt);
            npkt = upd.getNumberOfBDPacketsReceived();
            System.out.println("BD Packets received   : " + npkt);

            npkt = upd.getNumberOfADPacketsProcessed();
            System.out.println("AD Packets processed  : " + npkt);
            npkt = upd.getNumberOfBDPacketsProcessed();
            System.out.println("BD Packets processed  : " + npkt);

            npkt = upd.getNumberOfADPacketsRadiated();
            System.out.println("AD Packets radiated   : " + npkt);
            npkt = upd.getNumberOfBDPacketsRadiated();
            System.out.println("BD Packets radiated   : " + npkt);

            npkt = upd.getNumberOfPacketsAcknowledged();
            System.out.println("Packets acknowledged  : " + npkt);

            long id = upd.getPacketLastProcessed();
            System.out.println("Last processed Id     : " + id);
            id = upd.getPacketLastOk();
            System.out.println("Last OK Id            : " + id);
            FSP_PacketStatus pst = upd.getPacketStatus();
            System.out.println("Packet Status         : " + pst);

            FSP_ProductionStatus ps = upd.getProductionStatus();
            System.out.println("Production status     : " + ps);
            ISLE_Time t = upd.getProductionStartTime();
            String theTime_c = null;
            if (t != null)
            {
                theTime_c = t.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth);
            }
            System.out.println("Production started    : ");
            if (t != null)
            {
                System.out.print(theTime_c);
            }
            System.out.println();
            t = upd.getProductionStopTime();
            theTime_c = null;
            if (t != null)
            {
                theTime_c = t.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth);
            }
            System.out.print("Production ended      : ");
            if (t != null)
            {
                System.out.print(theTime_c);
            }
            System.out.println();

            long evId = upd.getExpectedEventInvocationId();
            System.out.println("Exp ev invoc. Id      : " + evId);
            long dirId = upd.getExpectedDirectiveInvocationId();
            System.out.println("Exp dir invoc. Id     : " + dirId);
            long epId = upd.getExpectedPacketId();
            System.out.println("Exp packet Id         : " + epId);

            yn = upd.getDirectiveInvocationOnline();
            System.out.println("Dir Inv Online        : " + yn);

            System.out.println();
        }

    }

    private IFSP_FOPMonitor getFSPFOPMonitor()
    {
        IFSP_FOPMonitor fopmonitor = null;

        ISLE_SIAdmin admsi = getSiAdmin();
        if (admsi != null)
        {
            fopmonitor = admsi.queryInterface(IFSP_FOPMonitor.class);
        }
        if (fopmonitor == null)
        {
            System.out.println("Interface IFSP_FOPMonitor not available");
        }
        return fopmonitor;
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

    @Override
    public void help()
    {
        super.help();

        for (int i = 0; i < T_FSPCmd.T_FSPCmd_Max.getCode(); i++)
        {
            String cmd = T_FSPCmd.getT_ROCFCmdByCode(i).getCommand();
            int lenght = cmd.length();
            if (lenght < 15)
            {
                for (int j = 0; j < 15 - lenght; j++)
                {
                    cmd += ' ';
                }
            }
            else
            {
                cmd = cmd.substring(0, 15);
            }

            System.out.println("   " + cmd + "  " + T_FSPCmd.getT_ROCFCmdByCode(i).getHelp());
        }
        System.out.println();
    }

    @Override
    public void informOpInvoke(ISLE_Operation poperation, long seqCount) throws SleApiException
    {
        setupLastProcessed(poperation);
        super.informOpInvoke(poperation, seqCount);
    }

    @SuppressWarnings({ "unused" })
    @Override
    public EE_SYSTST_T_Component startUIF(boolean playback)
    {
        HRESULT rc = HRESULT.S_OK;
        T_FSPCmd nextCommand = T_FSPCmd.T_FSPCmd_Max;

        this.playback = playback;

        EE_Reference<String> arg1 = new EE_Reference<String>();
        EE_Reference<String> arg2 = new EE_Reference<String>();
        EE_Reference<String> arg3 = new EE_Reference<String>();

        IFSP_SIAdmin adm = null;
        IFSP_SIUpdate upd = null;
        IFSP_FOPMonitor fopm = null;
        if (this.role != SLE_AppRole.sleAR_user)
        {
            adm = getFSPSIAdmin();
            upd = getFSPSIUpdate();
            fopm = get_FSP_FOPMonitor();
            if (upd == null || adm == null || fopm == null)
            {
                // severe error
                System.err.println("cannot get interfaces in EE_SYSTST_FSPSIClient::startUIF!!!");

                System.exit(1);
            }
        }

        // Modification for backward compatibility - SLE API Version 3.4 - FSP
        // Version 2
        if (adm != null)
        {
            adm.setBitLockRequired(SLE_YesNo.sleYN_No);
            adm.setRfAvailableRequired(SLE_YesNo.sleYN_No);
            if(version < 5)
            {
            	// Set the following config parameters to a valid value for
            	// version earlier than 5, since lated the doConfigCompleted() expects
            	// valid configuration and in case ealier versions would be 
            	// exectued the doConfigCompleted() would fail.
            	adm.setClcwGvcId(new FSP_ClcwGvcId(null, FSP_ConfType.fspCT_notConfigured));
            	adm.setClcwPhysicalChannel(new FSP_ClcwPhysicalChannel(null)); // set to "not configured"
            	adm.setCopCntrFramesRepetition(1);
            	adm.setSeqCntrFramesRepetition(1);
            	adm.setThrowEventOperation(SLE_YesNo.sleYN_No);
            	adm.setRepetitionLimit(1);
            }
        }
        // End modification - SLE API Version 3.4 - FSP Version 2

        while (nextCommand != T_FSPCmd.T_FSPCmd_up)
        {
            nextCommand = getNextCommand();

            // ///////////////////////////////////////////////////
            // SIAdmin commands:
            // ///////////////////////////////////////////////////
            // ----------------------------------------------------
            if (nextCommand == T_FSPCmd.T_FSPCmd_set_max_buffer_size)
            {
                System.out.print("Maximum buffer size: ");
                utl.read(arg1, playback);
                int l = Integer.parseInt(arg1.getReference());
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setMaximumBufferSize", Integer.toString(l));
                    adm.setMaximumBufferSize(l);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_max_packet_length)
            {
                System.out.print("Max Packet length: ");
                utl.read(arg1, playback);
                int l = Integer.parseInt(arg1.getReference());
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setMaximumPacketLength", Integer.toString(l));
                    adm.setMaximumPacketLength(l);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_max_frame_length)
            {
                System.out.print("Max Frame length: ");
                utl.read(arg1, playback);
                int l = Integer.parseInt(arg1.getReference());
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setMaximumFrameLength", Integer.toString(l));
                    adm.setMaximumFrameLength(l);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_init_prod_status)
            {
                String psPrompt = "Init Production status: (0=configured, 1=operationalBD,\n"
                                  + "                         2=operationalADandBD, 3=operationalADsuspended,\n"
                                  + "                         4=interrupted, 5=halted): ";

                FSP_ProductionStatus ps = FSP_ProductionStatus.getProductionStatusByCode(utl
                        .readInt(psPrompt, playback));
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin::setInitialProductionStatus", ps.toString());
                    adm.setInitialProductionStatus(ps);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_apid_list)
            {
                long[] pApidList = utl.readIntList("APID list (comma-sep, no spaces): ", playback);
                if (pApidList.length < 1 || pApidList == null)
                {
                    System.err.println("Illegal APID list input");
                }
                else if(pApidList.length == 1 && pApidList[0] == -1)
                {
                	 adm.setApIdList(pApidList); // set any for invalid input to allow testing any
                }
                else
                {
                    if (adm != null)
                    {
                        UTL.traceIF1("IFSP_SIAdmin.setApIdList", Integer.toString(pApidList.length));
                        adm.setApIdList(pApidList);
                    }
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_bit_lock_required)
            {
                SLE_YesNo yn = SLE_YesNo.getYesNoByBool(utl.readYn("Bit Lock Required (y/n): ", playback));
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setBitLockRequired", yn.toString());
                    adm.setBitLockRequired(yn);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_blocking_timeout)
            {
                int to = utl.readInt("Blocking Timeout (microsec): ", playback);
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setBlockingTimeout", Integer.toString(to));
                    adm.setBlockingTimeout(to);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_blocking_usage)
            {
                FSP_BlockingUsage bu = FSP_BlockingUsage.getFSP_BlockingUsageByCode(utl
                        .readInt("Blocking Usage (0:permitted, 1:notPermitted): ", playback));
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setBlockingUsage", bu.toString());
                    adm.setBlockingUsage(bu);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_directive_enabled)
            {
                SLE_YesNo yn = SLE_YesNo.getYesNoByBool(utl.readYn("Directive Enabled (y/n): ", playback));
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setDirectiveInvocationEnabled", yn.toString());
                    adm.setDirectiveInvocationEnabled(yn);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_init_directive_online)
            {
                SLE_YesNo yn = SLE_YesNo.getYesNoByBool(utl.readYn("Initial Directive Online (y/n): ", playback));
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setInitialDirectiveInvocationOnline", yn.toString());
                    adm.setInitialDirectiveInvocationOnline(yn);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_map_list)
            {
                long[] pMapIdList = utl.readIntList("MAP list (comma-sep, no spaces): ", playback);

                if (pMapIdList.length == 1 && pMapIdList[0] < 1)
                {
                    System.err.println("Illegal MAP list input");
                }
                else
                {
                    if (adm != null)
                    {
                        UTL.traceIF1("IFSP_SIAdmin.setMapList", Integer.toString(pMapIdList.length));
                        adm.setMapList(pMapIdList);
                    }

                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_rf_available_required)
            {
                SLE_YesNo yn = SLE_YesNo.getYesNoByBool(utl.readYn("Rf Available Required (y/n): ", playback));
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setRfAvailableRequired", yn.toString());
                    adm.setRfAvailableRequired(yn);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_segment_header_present)
            {
                boolean isYes = utl.readYn("Segment Header Present (y/n): ", playback);
                SLE_YesNo yn = SLE_YesNo.getYesNoByBool(isYes);
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setSegmentHeaderPresent", yn.toString());
                    adm.setSegmentHeaderPresent(yn);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_vc_mux_scheme)
            {
                FSP_MuxScheme ms = FSP_MuxScheme.getFSP_MuxSchemeByCode(utl
                        .readInt("Mux Scheme (0:fifo, 1:absolutePriority, 2:pollingVector): ", playback));
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setVcMuxScheme", ms.toString());
                    adm.setVcMuxScheme(ms);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_vc_polling_vector)
            {
                long[] pPollVector = utl.readIntList("Polling vector (comma-sep, no spaces): ", playback);
                if (pPollVector.length == 1 && pPollVector[0] < 1)
                {
                    System.out.println("Illegal Polling Vector input");
                }
                else
                {
                    if (adm != null)
                    {
                        UTL.traceIF1("IFSP_SIAdmin.setVcPollingVector", Integer.toString(pPollVector.length));
                        adm.setVcPollingVector(pPollVector);
                    }
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_vc_priority_list)
            {
                FSP_AbsolutePriority[] pPrioList = utl
                        .readPriorityList("Priority List (vc1,pr1:vc2,pr2:vc3,pr3:... no spaces!): ", playback);

                if (pPrioList == null)
                {
                    System.out.println("Illegal Priority List input");
                }
                else
                {
                    if (adm != null)
                    {
                        UTL.traceIF1("IFSP_SIAdmin.setVcPriorityList", Integer.toString(pPrioList.length));
                        adm.setVcPriorityList(pPrioList);
                    }

                }

            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_vc)
            {
                long id = utl.readInt("VC ID: ", playback);
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setVirtualChannel", Long.toString(id));
                    adm.setVirtualChannel(id);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_perm_transmission_mode)
            {
                FSP_PermittedTransmissionMode mode = FSP_PermittedTransmissionMode
                        .getFSPPermittedTransmissionModeByCode(utl.readInt("Perm. Transmission Mode: ", playback));
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setPermittedTransmissionMode", mode.toString());
                    adm.setPermittedTransmissionMode(mode);
                }
            }
            
            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_clcw_gvcid) // New with SLES V5
            {
                FSP_GvcId id = new FSP_GvcId();
                if (adm != null)
                {
                    EE_Reference<String> arg = new EE_Reference<String>();
                    System.out.println("Type (0=Master,1=Virtual): ");
                    utl.read(arg, this.playback);
                    int type_i = Integer.parseInt(arg.getReference());
                    id.setType((FSP_ChannelType.getChannelTypeByCode(type_i)));

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
                FSP_ClcwGvcId gvcid = new FSP_ClcwGvcId(id);

                adm.setClcwGvcId(gvcid);
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_clcw_phy_chan) // New with SLES V5
            {
            	System.out.print("clcw physical channel : ");
            	utl.read(arg1, playback);
                if (adm != null)
                {
                    adm.setClcwPhysicalChannel(new FSP_ClcwPhysicalChannel(arg1.getReference()));
                }
            }
            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_min_report_cycle) // New with SLES V5
            {
                long mrc = utl.readInt("Min. reporting cycle: ", playback);
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setMinimumReportingCycle", Long.toString(mrc));
                    adm.setMinimumReportingCycle(mrc);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_cop_cntr_frames_rep) // New with SLES V5
            {
                int ccfr = utl.readInt("Cop ctrl frames repetition: ", playback);
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setCopCntrFramesRepetition", Integer.toString(ccfr));
                    adm.setCopCntrFramesRepetition(ccfr);
                }
            }
            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_seq_cntr_frames_rep) // New with SLES V5
            {
                int scfr = utl.readInt("Sequence ctrl frames repetition: ", playback);
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setCopCntrFramesRepetition", Integer.toString(scfr));
                    adm.setSeqCntrFramesRepetition(scfr);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_throw_event_enabled) // New with SLES V5
            {
            	SLE_YesNo yn = SLE_YesNo.getYesNoByBool(utl.readYn("Throw event operation enabled (Y/N): ", playback));
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setThrowEventOperation", yn.toString());
                    adm.setThrowEventOperation(yn);
                }
            }
            
            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_repetition_limit) // New with SLES V5
            {
                int rl = utl.readInt("Repetition limit: ", playback);
                if (adm != null)
                {
                    UTL.traceIF1("IFSP_SIAdmin.setRepetitionLimit", Integer.toString(rl));
                    adm.setRepetitionLimit(rl);
                }
            }

            // ///////////////////////////////////////////////////
            // FOPMonitor commands:
            // ///////////////////////////////////////////////////

            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_sliding_window)
            {
                long w = utl.readInt("Window Width: ", playback);
                if (fopm != null)
                {
                    UTL.traceIF1("IFSP_FOPMonitor.setFopSlidingWindow", Long.toString(w));
                    fopm.setFopSlidingWindow(w);
                }
            }
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_timeout_type)
            {
                FSP_TimeoutType tt = FSP_TimeoutType.getFSPTimeoutTypeByCode(utl
                        .readInt("Timeout Type (0:generateAlert, 1:suspendAD): ", playback));
                if (fopm != null)
                {
                    UTL.traceIF1("IFSP_FOPMonitor.setTimeoutType", tt.toString());
                    fopm.setTimeoutType(tt);
                }
            }
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_timer_init)
            {
                long timer = utl.readInt("Timer Initial: ", playback);
                if (fopm != null)
                {
                    UTL.traceIF1("IFSP_FOPMonitor.setTimerInitial", Long.toString(timer));
                    fopm.setTimerInitial(timer);
                }
            }
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_transm_limit)
            {
                long limit = utl.readInt("Transmission Limit: ", playback);
                if (fopm != null)
                {
                    UTL.traceIF1("IFSP_FOPMonitor.setTransmissionLimit", Long.toString(limit));
                    fopm.setTransmissionLimit(limit);
                }
            }
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_transm_fr_seq_count)
            {
                long num = utl.readInt("Transm. Frame Sequence Number: ", playback);
                if (fopm != null)
                {
                    UTL.traceIF1("IFSP_FOPMonitor.setTransmitterFrameSequenceNumber", Long.toString(num));
                    fopm.setTransmitterFrameSequenceNumber(num);
                }
            }
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_fops)
            {
                final String fsPrompt = "FOP state (0:active, 1:retransmitWithoutWait, 2:retransmitWithWait\n"
                                        + "           3:initWithoutBCFrame, 4:initWithBCFrame, 5:initial): ";
                FSP_FopState fs = FSP_FopState.getFSPFopStateByCode(utl.readInt(fsPrompt, playback));
                if (fopm != null)
                {
                    UTL.traceIF1("IFSP_FOPMonitor.setFopState", fs.toString());
                    fopm.setFopState(fs);
                }
            }
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_map_polling_vector)
            {
                long[] pPollVector = utl.readIntList("Polling vector (comma-sep, no spaces): ", playback);
                if (pPollVector.length == 1 && pPollVector[0] < 1)
                {
                    System.out.println("Illegal Polling Vector input");
                }
                else
                {
                    if (fopm != null)
                    {
                        UTL.traceIF1("IFSP_FOPMonitor.setMapPollingVector", Integer.toString(pPollVector.length));
                        fopm.setMapPollingVector(pPollVector);
                    }
                }
            }
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_map_priority_list)
            {
                FSP_AbsolutePriority[] pPrioList = utl
                        .readPriorityList("Priority List (vc1,pr1:vc2,pr2:vc3,pr3:... no spaces!): ", playback);
                if (pPrioList == null)
                {
                    System.out.println("Illegal Priority List input");
                }
                else
                {
                    if (fopm != null)
                    {
                        UTL.traceIF0("IFSP_FOPMonitor.setMapPriorityList");
                        fopm.setMapPriorityList(pPrioList);
                    }
                }
            }
            else if (nextCommand == T_FSPCmd.T_FSPCmd_set_map_mux_scheme)
            {
                FSP_MuxScheme ms = FSP_MuxScheme.getFSP_MuxSchemeByCode(utl
                        .readInt("Mux Scheme (0:fifo, 1:absolutePriority, 2:pollingVector): ", playback));
                if (fopm != null)
                {
                    UTL.traceIF1("IFSP_FOPMonitor.setMapMuxScheme", ms.toString());
                    fopm.setMapMuxScheme(ms);
                }
            }
            // ///////////////////////////////////////////////////
            // SIUpdate commands:
            // ///////////////////////////////////////////////////

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_pkt_started)
            {
                long id = utl.readInt("Packet ID: ", playback);
                FSP_TransmissionMode tm = FSP_TransmissionMode
                        .getTransmissionModeByCode(utl
                                .readInt("Transmission Mode (0:sequenceControlled, 1:expedited, 2:sequenceControlledUnblock): ",
                                         playback));
                ISLE_Time startTime = utl.readTime("Start Time: ", playback);
                long bs = utl.readInt("Buffer Size: ", playback);
                boolean yn = utl.readYn("Notify (y/n): ", playback);

                if (upd != null)
                {
                    UTL.traceIF4("IFSP_SIUpdate.packetStarted",
                                 Long.toString(id),
                                 tm.toString(),
                                 Long.toString(bs),
                                 Boolean.toString(yn));
                    upd.packetStarted(id, tm, startTime, bs, yn);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_pkt_not_started)
            {
                long id = utl.readInt("Packet ID: ", playback);
                FSP_TransmissionMode tm = FSP_TransmissionMode
                        .getTransmissionModeByCode(utl
                                .readInt("Transmission Mode (0:sequenceControlled, 1:expedited, 2:sequenceControlledUnblock): ",
                                         playback));
                ISLE_Time startTime = utl.readTime("Start Time: ", playback);
                FSP_Failure reason = FSP_Failure.getFSP_FailureByCode(utl
                        .readInt("Failure reason: (0=expired, 1=interrupted, 2:modeMismatch): ", playback));
                long bs = utl.readInt("Buffer Size: ", playback);
                boolean yn = utl.readYn("Notify (y/n): ", playback);

                long[] affPktPtr = utl.readIntList("Affected Pkt IDs (comma-sep, no spaces): ", playback);
                if (affPktPtr.length == 1 && affPktPtr[0] < 1)
                {
                    System.out.println("Illegal PacketId list input");
                }
                else
                {
                    if (upd != null)
                    {
                        try
                        {
                            UTL.traceIF5("IFSP_SIUpdate.packetNotStarted",
                                         Long.toString(id),
                                         tm.toString(),
                                         Long.toString(bs),
                                         Boolean.toString(yn),
                                         Integer.toString(affPktPtr.length));
                            upd.packetNotStarted(id, tm, startTime, reason, bs, yn, affPktPtr);
                        }
                        catch (SleApiException e)
                        {
                            LOG.log(Level.FINE, "SleApiException ", e);
                        }
                    }
                }

            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_pkt_radiated)
            {
                long id = utl.readInt("Packet ID: ", playback);
                FSP_TransmissionMode tm = FSP_TransmissionMode
                        .getTransmissionModeByCode(utl
                                .readInt("Transmission Mode (0:sequenceControlled, 1:expedited, 2:sequenceControlledUnblock): ",
                                         playback));
                ISLE_Time radTime = utl.readTime("Radiation Time: ", playback);
                boolean yn = utl.readYn("Notify (y/n): ", playback);

                if (upd != null)
                {
                    UTL.traceIF3("IFSP_SIUpdate.packetRadiated", Long.toString(id), tm.toString(), Boolean.toString(yn));
                    upd.packetRadiated(id, tm, radTime, yn);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_pkt_acknowleged)
            {
                long id = utl.readInt("Packet ID: ", playback);
                ISLE_Time ackTime = utl.readTime("Acknowlege Time: ", playback);
                boolean yn = utl.readYn("Notify (y/n): ", playback);

                if (upd != null)
                {
                    UTL.traceIF1("IFSP_SIUpdate.packetAcknowledged", Boolean.toString(yn));
                    upd.packetAcknowledged(id, ackTime, yn);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_prod_status_change)
            {
                String psPrompt = "Update Production status: (0=configured, 1=operationalBD,\n"
                                  + "                            2=operationalADandBD, 3=operationalADsuspended,\n"
                                  + "                            4=interrupted, 5=halted): ";
                FSP_ProductionStatus ps = FSP_ProductionStatus.getProductionStatusByCode(utl
                        .readInt(psPrompt, playback));
                String fopaPrompt = "FOP Alert: (0=noAlert, 1=limit, 2=lockOutDetected, 3=synch, 4=invalidNR): \n"
                                    + "           (5=Clcw, 6=lowerLayerOutOfSync, 7=terminateAD): ";
                FSP_FopAlert fopa = FSP_FopAlert.getFopAlertByCode(utl.readInt(fopaPrompt, playback));
                long bs = utl.readInt("Buffer Size: ", playback);
                boolean yn = utl.readYn("Notify (y/n): ", playback);

                long[] affPktPtr = utl.readIntList("Affected Pkt IDs (comma-sep, no spaces, 'NULL' for empty list): ",
                                                   playback);
                
                try
                {
                    UTL.traceIF5("IFSP_SIUpdate.productionStatusChange",
                                 ps.toString(),
                                 Integer.toString(affPktPtr.length),
                                 fopa.toString(),
                                 Long.toString(bs),
                                 Boolean.toString(yn));

                    // SLE API expects affPktPtr to be null if no packets are affected
                    if(affPktPtr != null && affPktPtr.length == 1 && affPktPtr[0] == -1)
                    {
                    	affPktPtr = null;
                    }

                    upd.productionStatusChange(ps, affPktPtr, fopa, bs, yn);
                }
                catch (SleApiException e)
                {
                    // Nothing to do
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_vc_abort)
            {
                long bs = utl.readInt("Buffer Size: ", playback);
                boolean yn = utl.readYn("Notify (y/n): ", playback);

                long[] affPktPtr = utl.readIntList("Affected Pkt IDs (comma-sep, no spaces, 'NULL' for empty list): ",
                                                   playback);
                if (upd != null)
                {
                    try
                    {
                        UTL.traceIF3("IFSP_SIUpdate.vcAborted",
                                     Integer.toString(affPktPtr.length),
                                     Long.toString(bs),
                                     Boolean.toString(yn));
                        upd.vcAborted(affPktPtr, bs, yn);
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                    }
                }

            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_no_dir_capability)
            {
                boolean yn = utl.readYn("Notify (y/n): ", playback);
                if (upd != null)
                {
                    try
                    {
                        UTL.traceIF1("IFSP_SIUpdate.noDirectiveCapability", Boolean.toString(yn));
                        upd.noDirectiveCapability(yn);
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                    }
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_buffer_empty)
            {
                boolean yn = utl.readYn("Notify (y/n): ", playback);
                if (upd != null)
                {
                    UTL.traceIF1("IFSP_SIUpdate.bufferEmpty", Boolean.toString(yn));
                    upd.bufferEmpty(yn);
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_event_proc_completed)
            {
                long id = utl.readInt("Invocation ID: ", playback);
                FSP_EventResult eres = FSP_EventResult.getFSP_EventResultByCode(utl
                        .readInt("Event Result: (0=completed, 1=notCompleted, 2=conditionFalse): ", playback));
                boolean yn = utl.readYn("Notify (y/n): ", playback);

                if (upd != null)
                {
                    try
                    {
                        UTL.traceIF3("IFSP_SIUpdate.eventProcCompleted",
                                     Long.toString(id),
                                     eres.toString(),
                                     Boolean.toString(yn));
                        upd.eventProcCompleted(id, eres, yn);
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                    }
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_directive_completed)
            {
                long id = utl.readInt("Directive ID: ", playback);
                // SLE_Result dres =
                // (SLE_Result)UTL::readInt("Directive Result: (0=completed, 1=notCompleted, 2=conditionFalse): ",
                // _playback);
                SLE_Result dres = utl.readResult("Directive Result",
                                                 "completed",
                                                 "notCompleted",
                                                 "conditionFalse",
                                                 playback);
                String fopaPrompt = "FOP Alert: (0=noAlert, 1=limit, 2=lockOutDetected, 3=synch, 4=invalidNR): \n"
                                    + "           (5=Clcw, 6=lowerLayerOutOfSync, 7=terminateAD): ";
                FSP_FopAlert fopa = FSP_FopAlert.getFopAlertByCode(utl.readInt(fopaPrompt, playback));
                boolean yn = utl.readYn("Notify (y/n): ", playback);

                if (upd != null)
                {
                    try
                    {
                        UTL.traceIF4("IFSP_SIUpdate.directiveCompleted",
                                     Long.toString(id),
                                     dres.toString(),
                                     fopa.toString(),
                                     Boolean.toString(yn));
                        upd.directiveCompleted(id, dres, fopa, yn);
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                    }
                }
            }

            // ----------------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_directive_online)
            {
                boolean yn = utl.readYn("Notify (y/n): ", playback);
                if (upd != null)
                {
                    try
                    {
                        UTL.traceIF1("IFSP_SIUpdate.directiveCapabilityOnline", Boolean.toString(yn));
                        upd.directiveCapabilityOnline(yn);
                    }
                    catch (SleApiException e)
                    {
                        // Nothing to do
                    }
                }
            }

            // -----------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_print_si)
            {
                printSI();
            }

            // -----------------------------------------------
            else if (nextCommand.getCode() >= T_FSPCmd.T_FSPCmd_bind.getCode()
                     && nextCommand.getCode() <= T_FSPCmd.T_FSPCmd_peer_abort.getCode())
            {
                handleOperCmd(nextCommand);
            }

            // -----------------------------------------------
            else if (nextCommand == T_FSPCmd.T_FSPCmd_auto_gen_td)
            {
                rc = autoSendTD();
                displayResult(rc);
            }

        } // end while nextCommand != T_FSPCmd_up

        return EE_SYSTST_T_Component.eeEE_SYSTST_TestSE;
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

            this.opGen = new EE_SYSTST_FSPOpGen(this.opFactory, f, this.playback, this.utl);

            // #hd# shift setting the version to this place
            // SLE_VersionNumber vNum = _siAdmin->Get_Version();
            // _opGen->setVersion(vNum);
            this.opGen.setVersion(this.version);
        }

        return this.opGen;

    }

    @SuppressWarnings("unused")
    private void waitForSendNotification(EE_Duration duration)
    {

        // wait for data
        this.EE_CondVar_Notification.lock();
        try
        {
            this.EE_CondVar_Notification.wait();
        }
        catch (InterruptedException e1)
        {
            LOG.log(Level.FINE, "InterruptedException ", e1);
        }
        this.EE_CondVar_Notification.unlock();

        // cout << "Start the timer" << endl;
        EE_ElapsedTimer pElapsedTimer = new EE_ElapsedTimer();

        this.objMutex.lock();
        HRESULT res = HRESULT.S_OK;
        try
        {
            pElapsedTimer.start(duration, this, this.last_fsp);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        this.mapElapsedTimer.put(this.last_fsp, pElapsedTimer);
        this.objMutex.unlock();
        if (res != HRESULT.S_OK)
        {
            System.out.println("Start Timer failed. res=" + res);
        }

    }

    @SuppressWarnings("unused")
    private void signalCondVarNotification()
    {
        this.EE_CondVar_Notification.lock();
        this.EE_CondVar_Notification.notify();
        this.EE_CondVar_Notification.lock();
    }

    @Override
    public void handlerAbort(Object timer)
    {
        System.out.println("EE_SYSTST_FSPSIClient::HandlerAbort");
    }

    @Override
    public void processTimeout(Object timer, int invocationId)
    {
        this.objMutex.lock();
        this.mapElapsedTimer.remove(invocationId);
        this.objMutex.unlock();

        this.time.update();
        this.currentFspBufferSize += this.lg_radiated;

        IFSP_SIUpdate fspsiupd = getFSPSIUpdate();
        FSP_TransmissionMode tm = FSP_TransmissionMode.getTransmissionModeByCode(0);
        long id = invocationId;
        UTL.traceIF3("IFSP_SIUpdate.packetRadiated", Long.toString(id), tm.toString(), Boolean.toString(true));
        fspsiupd.packetRadiated(id, tm, this.time, true);
    }

    private IFSP_SIAdmin getFSPSIAdmin()
    {
        IFSP_SIAdmin ia = this.siAdmin.queryInterface(IFSP_SIAdmin.class);
        if (ia == null)
        {
            System.out.println("Interface IFSP_SIAdmin not available");
        }
        return ia;

    }

    private IFSP_SIUpdate getFSPSIUpdate()
    {
        IFSP_SIUpdate fspsiupd = null;

        ISLE_SIAdmin admsi = getSiAdmin();
        if (admsi != null)
        {
            fspsiupd = admsi.queryInterface(IFSP_SIUpdate.class);
        }
        if (fspsiupd == null)
        {
            System.out.println("Interface IFSP_SIUpdate not available");
        }

        return fspsiupd;
    }

    private IFSP_FOPMonitor get_FSP_FOPMonitor()
    {
        IFSP_FOPMonitor fopmonitor = null;

        ISLE_SIAdmin admsi = getSiAdmin();
        if (admsi != null)
        {
            fopmonitor = admsi.queryInterface(IFSP_FOPMonitor.class);
        }
        if (fopmonitor == null)
        {
            System.out.println("Interface IFSP_FOPMonitor not available");
        }

        return fopmonitor;
    }

    private T_FSPCmd getNextCommand()
    {
        T_FSPCmd nextCommand = T_FSPCmd.T_FSPCmd_Max;
        EE_Reference<String> cmd = new EE_Reference<>();
        cmd.setReference("");
        while (nextCommand == T_FSPCmd.T_FSPCmd_Max || nextCommand == T_FSPCmd.T_FSPCmd_dummy)
        {

            prompt();

            if (utl.read(cmd, this.playback) == false)
            {
                nextCommand = T_FSPCmd.T_FSPCmd_up;
                break;
            }

            for (int i = 0; i < T_FSPCmd.T_FSPCmd_Max.getCode(); i++)
            {
                if (T_FSPCmd.getT_ROCFCmdByCode(i).getCommand().toString().equals(cmd.getReference()))
                {
                    nextCommand = T_FSPCmd.getT_ROCFCmdByCode(i);
                    break;
                }
            }

            if (nextCommand == T_FSPCmd.T_FSPCmd_Max)
            {
                // command not found, go to base-class           	
                processSLECommand(cmd.getReference());
                cmd.setReference("");
            }
        } // end while

        return nextCommand;
    }

    void setupLastProcessed(ISLE_Operation poperation)
    {
        SLE_OpType ot = poperation.getOperationType();
        if (ot == SLE_OpType.sleOT_start)
        {
            this.lastRecFspId = 0;

            IFSP_SIAdmin adm = getFSPSIAdmin();
            if (adm != null)
            {
                this.currentFspBufferSize = adm.getMaximumBufferSize();
            }
            else
            {
                this.currentFspBufferSize = 1024;
            }

        }
        else if (ot == SLE_OpType.sleOT_transferData)
        {
            IFSP_TransferData td = (IFSP_TransferData) poperation;

            byte[] data = td.getData();
            int length = data.length;
            if (this.currentFspBufferSize >= length)
            {
                this.currentFspBufferSize = this.currentFspBufferSize - length;
            }
            else
            {
                this.currentFspBufferSize = 0;
            }

            this.lastRecFspId = td.getPacketId();

            System.out.println("Setup FSP last processed. ExpectedFspId " + (this.lastRecFspId + 1)
                               + " BufferAvailable " + this.currentFspBufferSize);
            UTL.traceIF1("IFSP_TransferData.setExpectedPacketId", Long.toString(this.lastRecFspId + 1));
            td.setExpectedPacketId(this.lastRecFspId + 1);
            UTL.traceIF1("IFSP_TransferData.setPacketBufferAvailable", Long.toString(this.currentFspBufferSize));
            td.setPacketBufferAvailable(this.currentFspBufferSize);

        }
        else if (ot == SLE_OpType.sleOT_throwEvent)
        {
            IFSP_ThrowEvent te = (IFSP_ThrowEvent) poperation;
            // set expected event invocation id
            long evId = te.getEventInvocationId();
            te.setExpectedEventInvocationId(evId + 1);

            System.out.println("Setup FSP last processed. ExpectedFspId " + (evId + 1));
        }
    }

    private SLE_OpType getOpType(T_FSPCmd cmd)
    {
        if (cmd == T_FSPCmd.T_FSPCmd_bind)
        {
            return SLE_OpType.sleOT_bind;
        }
        else if (cmd == T_FSPCmd.T_FSPCmd_unbind)
        {
            return SLE_OpType.sleOT_unbind;
        }
        else if (cmd == T_FSPCmd.T_FSPCmd_start)
        {
            return SLE_OpType.sleOT_start;
        }
        else if (cmd == T_FSPCmd.T_FSPCmd_stop)
        {
            return SLE_OpType.sleOT_stop;
        }
        else if (cmd == T_FSPCmd.T_FSPCmd_transfer_data)
        {
            return SLE_OpType.sleOT_transferData;
        }
        else if (cmd == T_FSPCmd.T_FSPCmd_async_notify)
        {
            return SLE_OpType.sleOT_asyncNotify;
        }
        else if (cmd == T_FSPCmd.T_FSPCmd_ssr)
        {
            return SLE_OpType.sleOT_scheduleStatusReport;
        }
        else if (cmd == T_FSPCmd.T_FSPCmd_get_prm)
        {
            return SLE_OpType.sleOT_getParameter;
        }
        else if (cmd == T_FSPCmd.T_FSPCmd_throw_event)
        {
            return SLE_OpType.sleOT_throwEvent;
        }
        else if (cmd == T_FSPCmd.T_FSPCmd_invoke_dir)
        {
            return SLE_OpType.sleOT_invokeDirective;
        }
        else if (cmd == T_FSPCmd.T_FSPCmd_peer_abort)
        {
            return SLE_OpType.sleOT_peerAbort;
        }
        else
        {
            return SLE_OpType.sleOT_bind; // no better idea
        }

    }

    private void handleOperCmd(T_FSPCmd nextCommand)
    {
        HRESULT rc = HRESULT.S_OK;

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
                if (nextCommand == T_FSPCmd.T_FSPCmd_bind)
                {
                    System.out.println("i=Invocation  r=Return  is=Invocation without reset sequence counter");
                    System.out.println("rs=Return with reset sequence counter");
                    while (!(what.getReference().equals("i") || what.getReference().equals("r")
                             || what.getReference().equals("is") || what.getReference().equals("rs")))
                    {
                        utl.read(what, this.playback);
                        if ((what.getReference().equals("i")) || (what.getReference().equals("rs")))
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
                        utl.read(what, this.playback);
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

                    if (nextCommand == T_FSPCmd.T_FSPCmd_peer_abort)
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

    }

    private HRESULT autoSendTD()
    {
        HRESULT rc = HRESULT.S_OK;

        IUnknown piuk = this.siAdmin.queryInterface(IUnknown.class);
        ISLE_Operation op = getOpGen().createOp(SLE_OpType.sleOT_transferData, this.eventQueue, piuk);

        if (op != null)
        {
            IFSP_TransferData td = (IFSP_TransferData) op;

            System.out.print("How many TD operations ? ");
            EE_Reference<String> n = new EE_Reference<String>();
            utl.read(n, this.playback);
            int i = Integer.parseInt(n.getReference());

            long theId = td.getPacketId();

            for (int j = 0; j < i; j++)
            {
                IFSP_TransferData tds = (IFSP_TransferData) td.copy();
                tds.setPacketId(theId);
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

    @SuppressWarnings("unused")
    @Override
    public void testTdSend(long lg, int nbtime, long delay_td, byte[] td_data, int delay)
    {

        EE_Reference<String> produce_notif_all = new EE_Reference<>();
        System.out.print("Produce Noficiation for All cltu ? (y/n) : ");
        utl.read(produce_notif_all, this.playback);

        IUnknown piuk = this.siAdmin.queryInterface(IUnknown.class);
        ISLE_Operation op = null;
        IFSP_TransferData optdfsp;
        IFSP_AsyncNotify opasnfsp;

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

        byte[] ptmp = time1.getCDS();
        String ascii = EE_GenStrUtil.convAscii(ptmp, 8);
        System.out.println("time=" + ascii);

        EE_SYSTST_OpGen pOpGen = getOpGen();
        int count = 0, n;
        double lgfsp_tot = 0, rate;
        boolean do_print = true;
        int fspid = 1;
        if (nbtime > 0)
        {
            nbtime++;
        }

        this.currentFspBufferSize = 1024;

        while (true)
        {
            count = 0;

            // send transfer data
            while (true)
            {

                // if (_currentFspBufferSize < lg) {
                // cout << "Not enought buffer available. " <<
                // _currentFspBufferSize << " " << lg << endl;
                // break;
                // }

                if (nbtime >= 0)
                {

                    // create the transfer data op
                    try
                    {
                        op = pOpGen.siOPF.createOperation(IFSP_TransferData.class, SLE_OpType.sleOT_transferData);
                    }
                    catch (SleApiException e1)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e1);
                    }
                    IFSP_TransferData td = (IFSP_TransferData) op;
                    if (produce_notif_all.getReference().equals("y"))
                    {
                        td.setRadiatedNotification(SLE_SlduStatusNotification.sleSN_produceNotification);
                    }
                    else
                    {
                        if (nbtime > 2)
                        {
                            td.setRadiatedNotification(SLE_SlduStatusNotification.sleSN_doNotProduceNotification);
                        }
                        else
                        {
                            td.setRadiatedNotification(SLE_SlduStatusNotification.sleSN_produceNotification);
                        }
                    }
                    UTL.traceIF1("IFSP_TransferData.setDelayTime", Long.toString(delay_td));
                    td.setDelayTime(delay_td);
                    td.setPacketId(fspid);
                    UTL.traceIF1("IFSP_TransferData.setData", Long.toString(lg));
                    td.setData(td_data);
                    lgfsp_tot += td_data.length;
                    // if ((_seqCounter % 10) == 0)
                    do_print = true;
                    // else
                    // do_print = false;

                    if (do_print)
                    {
                        System.out.print("Send TD Op. Seq " + this.seqCounter + " FspId " + fspid + ". lg "
                                         + td_data.length);
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
                        fspid++;
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
                        fspid++;
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
                        System.out.println("Last TD op sent with fspid=" + (fspid - 1) + " after " + diff_time
                                           + " seconds");
                        if (diff_time > 10)
                        {

                            rate = ((lgfsp_tot * 8) / 1024);
                            System.out.println(rate + " K bits sent in " + diff_time + " s" + " --> "
                                               + (rate / diff_time) + " K bits / s");
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

                            optdfsp = pop.getReference().queryInterface(IFSP_TransferData.class);
                            byte[] data1;

                            data1 = optdfsp.getData();
                            this.currentFspBufferSize = optdfsp.getPacketBufferAvailable();

                            if (do_print)
                            {
                                System.out.println("Rcv TD Rtn Op. FspId " + optdfsp.getPacketId() + " Buff "
                                                   + this.currentFspBufferSize);
                            }

                            if ((nbtime == -2) && (optdfsp.getPacketId() == fspid - 1))
                            {
                                time.update();
                                double diff_time = time.subtract(time1);
                                System.out.println("-----------------------------------------------");
                                System.out.println("TD Rtn Op rcv. fspid " + optdfsp.getPacketId() + " after "
                                                   + diff_time + " seconds");
                                if (diff_time > 10)
                                {

                                    rate = ((lgfsp_tot * 8) / 1024);
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
                            opasnfsp = pop.getReference().queryInterface(IFSP_AsyncNotify.class);

                            if (opasnfsp.getNotificationType() == FSP_NotificationType.fspNT_bufferEmpty)
                            {
                                System.out.println("Rcv Async Notify Op. Fsp " + opasnfsp.getPacketLastOk() + " "
                                                   + opasnfsp.getNotificationType());
                                // _currentFspBufferSize = 1024;

                            }
                            else if (opasnfsp.getNotificationType() == FSP_NotificationType.fspNT_packetRadiated)
                            {

                                if ((opasnfsp.getPacketLastOk() == fspid - 1) && (nbtime == -2))
                                {
                                    time.update();
                                    double diff_time = time.subtract(time1);
                                    System.out.println("-----------------------------------------------");
                                    System.out.println("Fsp Radiated rcv. fsp_last_ok=" + opasnfsp.getPacketLastOk()
                                                       + " after " + diff_time + " seconds");
                                    if (diff_time > 10)
                                    {

                                        rate = ((lgfsp_tot * 8) / 1024);
                                        System.out.println(rate + " K bits sent, confirmed and radiated in "
                                                           + diff_time + " s" + " --> " + (rate / diff_time)
                                                           + " K bits/s");
                                        System.out.println("-----------------------------------------------");
                                    }
                                    return;
                                }
                                else
                                {
                                    System.out.println("Rcv Async Notify Op. Fsp " + opasnfsp.getPacketLastOk() + " "
                                                       + opasnfsp.getNotificationType());
                                }
                            }
                            else
                            {
                                System.out.println("Rcv Async Notify Op. Fsp " + opasnfsp.getPacketLastOk() + " "
                                                   + opasnfsp.getNotificationType());
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

            }
        }

    }

    @SuppressWarnings({ "unused" })
    @Override
    public void testTdReceive(long lg, int nbtime)
    {

        IUnknown piuk = this.siAdmin.queryInterface(IUnknown.class);
        ISLE_Operation op;
        IFSP_TransferData optdfsp;

        EE_Reference<DCW_Event_Type> et = new EE_Reference<>();
        EE_Reference<IUnknown> psi1 = new EE_Reference<>();
        EE_Reference<ISLE_Operation> pop = new EE_Reference<>();

        int to = 10;

        EE_SYSTST_OpGen pOpGen = getOpGen();
        int count = 0, n;
        int fspid = 1;
        double rate, rate_prec = 0;
        boolean do_print = true;

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

        if (nbtime > 0)
        {
            nbtime++;
        }

        IFSP_SIAdmin adm = getFSPSIAdmin();
        if (adm != null)
        {
            this.currentFspBufferSize = adm.getMaximumBufferSize();
        }
        else
        {
            this.currentFspBufferSize = 1024;
        }

        count = 0;

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
                    count = 0;
                    System.out.println("Rcv " + pop.getReference().getOperationType() + " Rtn Op");
                }
                else if (et.getReference() == DCW_Event_Type.dcwEVT_informOpInvoke)
                {
                    count = 0;
                    if (pop.getReference().getOperationType() == SLE_OpType.sleOT_transferData)
                    {

                        optdfsp = pop.getReference().queryInterface(IFSP_TransferData.class);
                        byte[] data1;

                        data1 = optdfsp.removeData();

                        if ((this.seqCounter % 100) == 0)
                        {
                            do_print = true;
                        }
                        else
                        {
                            do_print = false;
                        }

                        if (do_print)
                        {
                            System.out.println("Receive TD Inv Op. FspId " + optdfsp.getPacketId() + ". lg "
                                               + data1.length);
                        }

                        // if (lg != lg1) {
                        // cout << "length is not correct. " << lg << " " << lg1
                        // << endl;
                        // return;
                        // }

                        if (fspid != optdfsp.getPacketId())
                        {
                            System.out.println("FSP Id not the expected one !!");
                        }

                        if (this.currentFspBufferSize >= data1.length)
                        {
                            this.currentFspBufferSize = this.currentFspBufferSize - data1.length;
                        }
                        else
                        {
                            this.currentFspBufferSize = 0;
                        }

                        fspid++;
                        UTL.traceIF1("IFSP_TransferData.setExpectedPacketId", Integer.toString(fspid));
                        optdfsp.setExpectedPacketId(fspid);
                        UTL.traceIF1("IFSP_TransferData.setPacketBufferAvailable",
                                     Long.toString(this.currentFspBufferSize));
                        optdfsp.setPacketBufferAvailable(this.currentFspBufferSize);

                        if (do_print)
                        {
                            System.out.println("Send Rtn Op. Seq " + this.seqCounter + ". lg " + data1.length);
                        }

                        HRESULT rc = HRESULT.S_OK;
                        try
                        {
                            this.srvInit.initiateOpReturn(optdfsp, this.seqCounter++);
                        }
                        catch (SleApiException e)
                        {
                            rc = e.getHResult();
                        }

                        if (do_print)
                        {
                            System.out.println(" : " + rc);
                        }

                        if (optdfsp.getRadiatedNotification() == SLE_SlduStatusNotification.sleSN_produceNotification)
                        {
                            // send a fsp_radiated
                            time.update();
                            ISLE_SIAdmin admsi = getSiAdmin();
                            if (admsi != null)
                            {
                                IFSP_SIUpdate fspsiupd = admsi.queryInterface(IFSP_SIUpdate.class);

                                admsi.queryInterface(IFSP_SIUpdate.class);

                                if (fspsiupd != null)
                                {

                                    IFSP_SIAdmin admfspsi = (IFSP_SIAdmin) admsi;
                                    fspsiupd.packetStarted(optdfsp.getPacketId(),
                                                           optdfsp.getTransmissionMode(),
                                                           time,
                                                           this.currentFspBufferSize,
                                                           false);
                                    // done in separate thread
                                    // time->Update();
                                    // _currentFspBufferSize += lg1;
                                    // fspsiupd->FspRadiated(*time, true);
                                    this.lg_radiated = data1.length;
                                    this.last_fsp = (int) optdfsp.getPacketId();

                                    // signalCondVarNotification();

                                    // cout << "Start the timer" << endl;
                                    EE_ElapsedTimer pElapsedTimer = new EE_ElapsedTimer();

                                    this.objMutex.lock();
                                    EE_Duration duration = new EE_Duration(0, 1500, EE_TIME_Prec.eeTIME_PrecMILLISEC); // 1500
                                                                                                                       // ms
                                    res = HRESULT.S_OK;
                                    try
                                    {
                                        pElapsedTimer.start(duration, this, this.last_fsp);
                                    }
                                    catch (SleApiException e)
                                    {
                                        res = e.getHResult();
                                    }
                                    this.mapElapsedTimer.put(this.last_fsp, pElapsedTimer);
                                    this.objMutex.unlock();
                                    if (res != HRESULT.S_OK)
                                    {
                                        System.out.println("Start Timer failed. res=" + res);
                                    }

                                }

                            }
                        }
                        else
                        {
                            this.currentFspBufferSize += data1.length;
                        }

                    }
                    else
                    {
                        System.out.println("Rcv " + pop.getReference().getOperationType() + " Inv Op");
                        if (pop.getReference().getOperationType() == SLE_OpType.sleOT_peerAbort)
                        {
                            this.stop_thread = true;
                            return;
                        }
                    }

                    if (nbtime > 0)
                    {
                        nbtime--;
                        if (nbtime == 1)
                        {
                            this.stop_thread = true;
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
                this.stop_thread = true;
                return;
            }

        }

        this.stop_thread = true;
    }

}
