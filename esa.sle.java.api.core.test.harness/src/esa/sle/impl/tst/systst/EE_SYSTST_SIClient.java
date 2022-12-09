package esa.sle.impl.tst.systst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIAdmin;
import ccsds.sle.api.isle.ise.ISLE_ServiceInitiate;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_LocalRDN;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_SIState;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apiut.EE_SLE_UtilityFactory;
import esa.sle.impl.eapi.dcw.IDCW_EventQueue;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.ifs.time.EE_Duration;
import esa.sle.impl.ifs.time.EE_TIME_Fmt;
import esa.sle.impl.ifs.time.EE_Time;
import esa.sle.impl.tst.systst.types.EE_SYSTST_T_Component;
import esa.sle.impl.tst.systst.types.T_ClientCmd;

public abstract class EE_SYSTST_SIClient implements ISLE_ServiceInform
{

    private static final String VOID = "void";

	private static final Logger LOG = Logger.getLogger(EE_SYSTST_SIClient.class.getName());

    private static String helpCommand[] = {
                                           "wait              Wait for n seconds",
                                           "wait_event        Wait for the next event from DCW",
                                           "wait_selected_op  Wait for a given operation from DCW",
                                           "start_loop_sequence Start recording the loop sequence",
                                           "stop_loop_sequence  Stop the recording of the loop sequence",
                                           "play_loop_sequence Play the recorded loop sequence",
                                           "test_td           Test Transfer Data and Resume Data Transfer",
                                           "                  ",
                                           "set_siid          ISLE_SIAdmin: set service instance identifier",
                                           "set_peer_id       ISLE_AIAdmin: set peer identifier",
                                           "set_pp            ISLE_SIAdmin: set provision period",
                                           "set_bind_ini      ISLE_SIAdmin: set bind initiative",
                                           "set_rsp_port_id   ISLE_SIAdmin: set responder port identifier",
                                           "set_rtn_to        ISLE_SIAdmin: set return timeout",
                                           "config_completed  ISLE_SIAdmin: config completed",
                                           "send_a_return     Send a return operation from the list",
                                           "send_all_return   Send all the stored return operation",
                                           "timeoffset        Set offset for Timesource",
                                           "help              this help text",
                                           "suspend           suspends the reception of operations other than PEER-ABORT",
                                           "resume            resumes the reception of operations",
                                           "playback_cmd      playback of commands" };

    protected ISLE_UtilFactory utilFactory;

    protected ISLE_OperationFactory opFactory;

    protected IDCW_EventQueue eventQueue;

    protected ISLE_SIAdmin siAdmin;

    protected ISLE_ServiceInitiate srvInit;

    protected EE_SYSTST_TimeSource timeSource;

    protected ISLE_Time time;

    protected long seqCounter;

    protected boolean playback;

    protected LinkedList<ISLE_ConfirmedOperation> listCop;

    protected SLE_AppRole role;

    protected int version;

    private final SLE_ApplicationIdentifier srvType;

    @SuppressWarnings("unused")
    private ISLE_Time startTime;

    @SuppressWarnings("unused")
    private ISLE_Time stopTime;

    private String loopFileName;

    private boolean configCompleted;

    protected UTL utl;

    public EE_SYSTST_SIClient(SLE_ApplicationIdentifier srvType, SLE_AppRole role, EE_SYSTST_TimeSource timeSource, UTL utl)
    {
    	this.utl = utl;
        this.srvType = srvType;
        this.role = role;
        this.version = 0;
        this.siAdmin = null;
        this.srvInit = null;
        this.utilFactory = null;
        this.eventQueue = null;
        this.opFactory = null;
        this.startTime = null;
        this.stopTime = null;
        this.configCompleted = false;
        this.seqCounter = 1;
        this.listCop = new LinkedList<ISLE_ConfirmedOperation>();

        String lnum = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        if (this.role == SLE_AppRole.sleAR_user)
        {
            this.loopFileName = "THLoopUser_" + lnum + ".ctl";
        }
        else
        {
            this.loopFileName = "THLoopProvider_" + lnum + ".ctl";
        }

        if (timeSource != null)
        {
            this.timeSource = timeSource;
        }

        try
        {
            this.time = EE_SLE_UtilityFactory.getInstance(utl.getInstanceId()).createTime(ISLE_Time.class);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
    }

    public void init(ISLE_UtilFactory uf, ISLE_OperationFactory opf)
    {
        this.utilFactory = uf;
        this.opFactory = opf;
        this.seqCounter = 1;
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
    public void informOpInvoke(ISLE_Operation poperation, long seqCount) throws SleApiException
    {
        System.out.println();
        System.out.println("SI-Client: operation invocation received: ");
        SLE_OpType ot = poperation.getOperationType();
        System.out.println("Operation Type: " + ot.toString());

        if (poperation.isConfirmed())
        {
            EE_Reference<String> send = new EE_Reference<String>();
            System.out.println("Send positive return ? (y/n): ");
            utl.read(send, this.playback);

            if (send.getReference().equals("y"))
            {
                ISLE_ConfirmedOperation cop = (ISLE_ConfirmedOperation) poperation;
                System.out.println("Send Return Operation. Seq " + seqCount);
                try
                {
                    this.srvInit.initiateOpReturn(cop, seqCount);
                }
                catch (SleApiException e)
                {
                    System.out.println("Send Return Operation failed: " + e.getHResult());
                    throw new SleApiException(e.getHResult());
                }
            }
        }

    }

    @Override
    public void informOpReturn(ISLE_ConfirmedOperation poperation, long seqCount) throws SleApiException
    {
        System.out.println();
        System.out.println("SI-Client: operation return received: ");
        SLE_OpType ot = poperation.getOperationType();
        System.out.println("Operation Type: " + ot.toString());
    }

    @Override
    public void resumeDataTransfer()
    {
        System.out.println("\nSI-Client: Resume Data Transfer received: ");
    }

    @Override
    public void provisionPeriodEnds()
    {
        System.out.println("\nSI-Client: End of Provision Period Report received: ");
    }

    @Override
    public void protocolAbort(byte[] diagnostic) throws SleApiException
    {
        System.out.println("\nSI-Client: ProtocolAbort received: ");
        this.seqCounter = 0;
    }

    public void setSIAdmin(ISLE_SIAdmin siAdmin, IDCW_EventQueue eventQueue)
    {
        this.siAdmin = siAdmin;
        this.eventQueue = eventQueue;
        this.srvInit = this.siAdmin.queryInterface(ISLE_ServiceInitiate.class);
    }

    public ISLE_SIAdmin getSiAdmin()
    {
        return this.siAdmin;
    }

    public SLE_SIState getSIState()
    {
        return this.srvInit.getSIState();
    }

    public abstract EE_SYSTST_T_Component startUIF(boolean playback);

    public void printSI()
    {
        ISLE_SII sii = this.siAdmin.getServiceInstanceIdentifier();        
        sii.reset();
        System.out.print("\nSrv Instance Id: ");
        if (sii != null)
        {
            String id = sii.getAsciiForm();
            System.out.println(id);

            SLE_LocalRDN lrdn = null;
            try
            {
                lrdn = sii.nextLocalRDN();
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
            }

            if (lrdn != null)
            {
                System.out.print("Id = ");
                System.out.print(lrdn.getOid() + "");
                System.out.println("  Value = " + lrdn.getValue());
            }
        }

        int vn = this.siAdmin.getVersion();
        System.out.println("SI Version     : " + vn);
        SLE_ApplicationIdentifier type = this.siAdmin.getServiceType();
        System.out.println("Service Type   : " + type);
        SLE_SIState st = this.srvInit.getSIState();
        System.out.println("SI State       : " + st);
        SLE_AppRole r = this.siAdmin.getRole();
        System.out.println("Role           : " + r);
        String tmpPeer = this.siAdmin.getPeerIdentifier();
        System.out.println("Peer Identifier: " + tmpPeer);
        System.out.println("Return Timeout : " + this.siAdmin.getPeerIdentifier());
        ISLE_Time startTime = this.siAdmin.getProvisionPeriodStart();
        System.out.print("Start Time     : ");
        if (startTime != null)
        {
            String tmp = startTime.getDate(SLE_TimeFmt.sleTF_dayOfMonth);
            System.out.println(tmp);
        }
        else
        {
            System.out.println("NULL");
        }
        ISLE_Time stopTime = this.siAdmin.getProvisionPeriodStop();
        System.out.print("Stop Time      : ");
        if (stopTime != null)
        {
            String tmp = stopTime.getDate(SLE_TimeFmt.sleTF_dayOfMonth);
            System.out.println(tmp);
        }
        else
        {
            System.out.println("NULL");
        }

        System.out.println("Config completed: " + String.valueOf(this.configCompleted));
    }

    /**
     * This function is needed for a provider set-up in order to invoke SI
     * Location on the proxy.
     * 
     * @return
     */
    public ISLE_Bind makeBindOp()
    {
        ISLE_Bind b = null;
        int vNo = utl.readInt("Version Number: ", this.playback);
        HRESULT res = HRESULT.S_OK;
        try
        {
            b = this.opFactory.createOperation(ISLE_Bind.class, SLE_OpType.sleOT_bind, this.srvType, vNo);
        }
        catch (SleApiException e)
        {
            res = e.getHResult();
        }
        if (b == null || res != HRESULT.S_OK)
        {
            System.out.println("Creation of BIND operation failed");
            return null;
        }

        EE_Reference<String> peerId = new EE_Reference<String>();
        EE_Reference<String> rspPortId = new EE_Reference<String>();
        EE_Reference<String> initiatorId = new EE_Reference<String>();

        System.out.println("Responder Id     : ");
        utl.read(peerId, this.playback);
        System.out.println("Responder Port Id: ");
        utl.read(rspPortId, this.playback);
        System.out.println("Initiator Id     : ");
        utl.read(initiatorId, this.playback);

        ISLE_SII sii = this.siAdmin.getServiceInstanceIdentifier();
        b.setResponderIdentifier(peerId.getReference());
        b.setResponderPortIdentifier(rspPortId.getReference());
        b.setInitiatorIdentifier(initiatorId.getReference());
        b.setServiceInstanceId(sii);
        b.setServiceType(this.srvType);

        System.out.println();
        System.out.println("------------------------------------");
        String bstr = b.print(512);
        System.out.println(bstr);

        EE_Reference<String> yn = new EE_Reference<String>();
        System.out.println("Further BIND operation set-up (y/n): ");
        utl.read(yn, this.playback);

        if (yn.getReference().equals("y"))
        {
            getOpGen().setUpOperation(b, null, null);
        }

        return b;
    }

    public abstract EE_SYSTST_OpGen getOpGen();

    public void setVersion(int version)
    {
        this.version = version;
    }

    public void rcvProtocolAbort()
    {
        prompt();
        System.out.println("Receive Protocol Abort");
        this.seqCounter = 1;
    }

    public void rcvResumeDataTransfer()
    {
        prompt();
        System.out.println("Receive Resume Data Transfer");
    }

    public void rcvProvisionPeriodEnds()
    {
        prompt();
        System.out.println("Receive Provision Period Ends");
    }

    public void rcvOpReturn(ISLE_Operation pop)
    {
        prompt();
        System.out.println("Receive " + pop.getOperationType().toString() + " Return Operation");
        System.out.println(pop.print(512));
    }

    public void rcvOpInvoke(ISLE_Operation pop, boolean fromSe)
    {
        EE_Reference<String> send = new EE_Reference<String>();
        prompt();
        System.out.println("Receive " + pop.getOperationType().toString() + " Invoke Operation");
        String tmp = pop.print(512);
        System.out.println(tmp);
        // set automatically the cltu id and the buffer size
        if (pop.getOpServiceType() == SLE_ApplicationIdentifier.sleAI_fwdCltu)
        {
            EE_SYSTST_CLTUSIClient cltusi = (EE_SYSTST_CLTUSIClient) this;
            cltusi.setUpLastprocessed(pop);
        }

        if (pop.isConfirmed())
        {
            if (!fromSe)
            {
                prompt();
                System.out.println("y : Send positive return Operation now.");
                System.out.println("r : Setup and Send return Operation now.");
                System.out.println("n : Don't send a return Operation now.");
                System.out.println("Selection :");

                utl.read(send, fromSe);
            }
            else
            {
                // when called from service element -> automatic response
                send.setReference("y");
            }

            if (send.getReference() != null && send.getReference().equals("y"))
            {
                ISLE_ConfirmedOperation cop = setReturnOp(pop, true, false);
                System.out.println("Send Return Operation. Seq " + this.seqCounter);
                try
                {
                    this.srvInit.initiateOpReturn(cop, this.seqCounter++);
                }
                catch (SleApiException e)
                {
                    prompt();
                    System.out.println("Send Return Operation failed: " + e.getHResult().toString());
                    if (cop != null)
                    {
                        System.out.println(cop.print(512));
                    }
                }
            }
            else if (send.getReference() != null && send.getReference().equals("r"))
            {
                ISLE_ConfirmedOperation cop = setReturnOp(pop, false, true);
                System.out.println("Send Return Operation. Seq " + this.seqCounter);
                try
                {
                    this.srvInit.initiateOpReturn(cop, this.seqCounter++);
                }
                catch (SleApiException e)
                {
                    prompt();
                    System.out.println("Send Return Operation failed: " + e.getHResult().toString());
                    if (cop != null)
                    {
                        System.out.println(cop.print(512));
                    }
                }
            }
            else if (send.getReference() != null && send.getReference().equals("n"))
            {
                EE_Reference<String> store = new EE_Reference<String>();
                prompt();
                System.out.println("y : Store positive return Operation in list.");
                System.out.println("r : Store and Setup return Operation in list.");
                System.out.println("n : Don't store return Operation in list.");
                System.out.println("Selection :");
                utl.read(store, this.playback);
                if (store.getReference().equals("y"))
                {
                    prompt();
                    System.out.println("Store positive return operation in the list");
                    ISLE_ConfirmedOperation cop = setReturnOp(pop, true, false);
                    if (cop != null)
                    {
                        // insert the op in the list for future return
                        // addref done by previous query interface
                        this.listCop.addLast(cop);
                    }
                }
                else if (store.getReference().equals("r"))
                {
                    prompt();
                    System.out.println("Store and Setup return operation in the list");
                    ISLE_ConfirmedOperation cop = setReturnOp(pop, false, true);
                    if (cop != null)
                    {
                        // insert the op in the list for future return
                        // addref done by previous query interface
                        this.listCop.addLast(cop);
                    }
                }
            }
            else
            {
                throw new RuntimeException("Control file malformed: expected one of y, n, r, but got "
                                           + send.getReference());
            }
        }
    }

    protected void displayResult(HRESULT rc)
    {
        prompt();
        System.out.println("Result: " + rc + " ######");
    }

    protected void prompt()
    {
        SLE_AppRole aRole = this.siAdmin.getRole();
        ISLE_SII sii = this.siAdmin.getServiceInstanceIdentifier();
        String name = sii.getLastRDN();
        if (aRole == SLE_AppRole.sleAR_user)
        {
            System.out.println("######U-SI " + name);
        }
        else
        {
            System.out.println("######P-SI " + name);
        }
    }

    protected void help()
    {
        System.out.println("\nAvailable commands: ");
        for (String i : helpCommand)
        {
            System.out.println("    " + i);
        }
        System.out.println();
    }

    protected void processSLECommand(final String cmd)
    {
        T_ClientCmd nextCommand = T_ClientCmd.T_Cmd_Max;

        String theCmd = cmd;

        if (cmd.equals("?"))
        {
            theCmd = T_ClientCmd.T_Cmd_help.toString();
        }

        for (int i = 0; i < T_ClientCmd.T_Cmd_Max.getCode(); i++)
        {
            if (T_ClientCmd.getDiagByCode(i).toString().equals(theCmd))
            {
                nextCommand = T_ClientCmd.getDiagByCode(i);
                break;
            }
        }

        // ---------------------------------------------------

        if (nextCommand == T_ClientCmd.T_Cmd_Max)
        {
            System.out.println("*** unknown command ");
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_help)
        {
            help();
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_config_completed)
        {

            HRESULT res = HRESULT.S_OK;
            try
            {
                this.siAdmin.configCompleted();
            }
            catch (SleApiException e)
            {
                res = e.getHResult();
            }
            if (res == HRESULT.S_OK)
            {
                this.configCompleted = true;
            }
            displayResult(res);
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_suspend)
        {
            System.out.println("------------------SUSPEND-----------------------");
            try
            {
                this.eventQueue.suspend();
            }
            catch (SleApiException e)
            {

                LOG.log(Level.FINE, "SleApiException ", e);
            }
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_resume)
        {
            System.out.println("------------------RESUME-----------------------");
            try
            {
                this.eventQueue.resume();
            }
            catch (SleApiException e)
            {
                LOG.log(Level.FINE, "SleApiException ", e);
            }
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_set_siid)
        {
            setUpSii();
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_set_peer_id)
        {
            EE_Reference<String> val = new EE_Reference<String>();
            System.out.print("Peer Identifier: ");
            utl.read(val, this.playback);
            this.siAdmin.setPeerIdentifier(val.getReference());
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_set_pp)
        {
            EE_Reference<String> startTime = new EE_Reference<String>();
            EE_Reference<String> stopTime = new EE_Reference<String>();
            System.out.println("Start time:  ");
            utl.read(startTime, this.playback);
            System.out.println("Stop time:  ");
            utl.read(stopTime, this.playback);
            this.startTime = null;
            this.stopTime = null;
            ISLE_Time time1 = null;
            ISLE_Time time2 = null;
            if (!startTime.getReference().isEmpty() && startTime.getReference() != null)
            {
                time1 = getTime(startTime.getReference());
                this.startTime = time1.copy();
                String tmp = time1.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth);
                System.out.println("Start time :  " + tmp);
            }
            if (!stopTime.getReference().isEmpty() && stopTime.getReference() != null)
            {
                time2 = getTime(stopTime.getReference());
                this.stopTime = time2.copy();
                String tmp = time2.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth);
                System.out.println("Stop time :  " + tmp);
            }

            this.siAdmin.setProvisionPeriod(time1, time2);
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_set_bind_ini)
        {
            EE_Reference<String> value = new EE_Reference<String>();
            System.out.println("Bind initiative (user=u/provider=p): ");
            utl.read(value, this.playback);
            SLE_AppRole r = SLE_AppRole.sleAR_user;
            if (value.getReference().equals("p"))
            {
                r = SLE_AppRole.sleAR_provider;
            }
            this.siAdmin.setBindInitiative(r);
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_set_rsp_port_id)
        {
            EE_Reference<String> value = new EE_Reference<String>();
            System.out.println("Responder port identifier: ");
            utl.read(value, this.playback);
            this.siAdmin.setResponderPortIdentifier(value.getReference());
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_set_rtn_to)
        {
            int to = 0;
            System.out.println("Return timeout (sec): ");
            EE_Reference<String> toS = new EE_Reference<String>();
            utl.read(toS, this.playback);
            to = Integer.parseInt(toS.getReference());
            this.siAdmin.setReturnTimeout(to);
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_send_a_return)
        {
            int ind = 1;
            System.out.println("List of possible return operation");
            for (ISLE_ConfirmedOperation li : this.listCop)
            {
                System.out.println("[" + ind++ + "]  " + li.getOperationType() + " InvokeId " + li.getInvokeId());
            }
            System.out.println("Ind of the return operation to send: ");
            EE_Reference<String> toS = new EE_Reference<String>();
            utl.read(toS, this.playback);
            int indRtnOp = Integer.parseInt(toS.getReference());
            ind = 1;
            for (ISLE_ConfirmedOperation li : this.listCop)
            {
                if (ind++ == indRtnOp)
                {

                    System.out.println("Send Return Operation. Seq " + this.seqCounter);
                    HRESULT res = HRESULT.S_OK;
                    try
                    {
                        this.srvInit.initiateOpReturn(li, this.seqCounter++);
                    }
                    catch (SleApiException e)
                    {
                        res = e.getHResult();
                    }
                    if (res != HRESULT.S_OK)
                    {
                        System.out.println("Send Return Operation failed: " + res);
                    }
                    this.listCop.remove(li);
                    break;
                }
            }
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_send_all_return)
        {
            ISLE_ConfirmedOperation cop = null;
            System.out.println("Send All Return");
            while (!this.listCop.isEmpty())
            {
                cop = this.listCop.removeFirst();
                System.out.println("Send Return Operation. Seq " + this.seqCounter);
                HRESULT res = HRESULT.S_OK;
                try
                {
                    this.srvInit.initiateOpReturn(cop, this.seqCounter++);
                }
                catch (SleApiException e)
                {
                    res = e.getHResult();
                }
                if (res != HRESULT.S_OK)
                {
                    System.out.println("Send Return Operation failed: " + res);
                }
            }
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_start_loop_seq)
        {
            EE_Reference<String> cmdStr = new EE_Reference<String>();
            // store all the next command in a file
            BufferedWriter loopOs = null;
            try
            {
                loopOs = new BufferedWriter(new FileWriter(this.loopFileName));

                while (true)
                {
                    if (!utl.read(cmdStr, this.playback))
                    {
                        break;
                    }
                    for (int i = 0; i < T_ClientCmd.T_Cmd_Max.getCode(); i++)
                    {
                        if (T_ClientCmd.getDiagByCode(i).toString().equals(cmdStr.getReference()))
                        {
                            nextCommand = T_ClientCmd.getDiagByCode(i);
                            break;
                        }
                    }
                    if (nextCommand == T_ClientCmd.T_Cmd_stop_loop_seq)
                    {
                        break;
                    }

                    loopOs.write(cmdStr.getReference() + "\n");
                }
            }
            catch (IOException e)
            {
                LOG.log(Level.FINE, "IOException ", e);
            }
            finally
            {
                try
                {
                    loopOs.close();
                }
                catch (IOException e)
                {
                    LOG.log(Level.FINE, "IOException ", e);
                }
            }
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_stop_loop_seq)
        {
            // Nothing to do
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_play_loop)
        {
            int nbLoop = 0;
            System.out.print("How many times:");
            EE_Reference<String> toS = new EE_Reference<String>();
            utl.read(toS, this.playback);
            nbLoop = Integer.parseInt(toS.getReference());
            if (nbLoop > 0)
            {
                BufferedReader oldIs = this.utl.getInputReader();
                int i = 0;
                while (i < nbLoop)
                {
                    prompt();
                    System.out.println("LOOP " + (i + 1) + " ############### ");
                    FileInputStream loopIs = null;
                    try
                    {
                        loopIs = new FileInputStream(this.loopFileName);
                        this.utl.setInputReader(new BufferedReader(new InputStreamReader(loopIs)));
                        startUIF(this.playback);
                    }
                    catch (FileNotFoundException e)
                    {
                        LOG.log(Level.FINE, "FileNotFoundException ", e);
                    }
                    finally
                    {
                        try
                        {
                            loopIs.close();
                        }
                        catch (IOException e)
                        {
                            LOG.log(Level.FINE, "IOException ", e);
                        }
                    }
                    i++;
                }
                this.utl.setInputReader(oldIs);
            }
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_wait)
        {
            int to = 0;
            System.out.println("Waiting time (sec): ");
            EE_Reference<String> toS = new EE_Reference<String>();
            utl.read(toS, this.playback);
            to = Integer.parseInt(toS.getReference());
            try
            {
                Thread.sleep(to*1000);
            }
            catch (InterruptedException e)
            {
                LOG.log(Level.FINE, "InterruptedException ", e);
            }
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_time_offset)
        {
            long offset = 0;
            EE_Reference<String> toS = new EE_Reference<String>();
            EE_Reference<String> toS1 = new EE_Reference<String>();
            System.out.println("Time Offset : positive(+) or negative(-):");
            utl.read(toS, this.playback);
            System.out.println("Offset:");
            utl.read(toS1, this.playback);
            offset = Integer.parseInt(toS1.getReference());
            if (this.timeSource != null)
            {
                if (toS.getReference().equals("+"))
                {
                    this.timeSource.setOffset(true, offset);
                }
                else if (toS.getReference().equals("-"))
                {
                    this.timeSource.setOffset(false, offset);
                }
            }
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_wait_selected_op)
        {

            EE_Reference<DCW_Event_Type> et = new EE_Reference<DCW_Event_Type>();
            et.setReference(DCW_Event_Type.dcwEVT_noEvent);

            EE_Reference<IUnknown> psi1 = new EE_Reference<IUnknown>();
            psi1.setReference(null);

            EE_Reference<ISLE_Operation> pop = new EE_Reference<ISLE_Operation>();
            pop.setReference(null);

            int timeoutSec = 0;
            int timeoutMilliSec = 0;
            System.out.print("Max waiting time for operation (sec): ");
            EE_Reference<String> to_s = new EE_Reference<String>();
            utl.read(to_s, this.playback);
            timeoutSec = Integer.parseInt(to_s.getReference());

            int op = 0;
            System.out.println("Operation to wait for: ");
            System.out.println("(0=bind, 1=unbind, 2=peer abort, 3=start,");
            System.out.println(" 4=stop, 5=transfer data, 6=transfer buffer,");
            System.out.println(" 7=sync notify, 8=async notify,");
            System.out.println(" 9=schedule status report, 10=status report,");
            System.out.print(" 11=get parameter, 12=throw event, 13=invoke directive): ");
            utl.read(to_s, this.playback);
            op = Integer.parseInt(to_s.getReference());

            while (true)
            {

                HRESULT res = HRESULT.S_OK;
                try
                {
                    this.eventQueue.nextEvent(et, psi1, pop, timeoutSec, timeoutMilliSec);
                    System.out.println(" eventQueue " + pop.getReference().getOperationType());

                }
                catch (SleApiException e)
                {
                    res = e.getHResult();
                }
                if (res == HRESULT.S_OK)
                {

                    if (et.getReference() == DCW_Event_Type.dcwEVT_informOpReturn)
                    {
                        prompt();
                        System.out.println("Receive " + pop.getReference().getOperationType() + " Return Operation");
                        if (pop.getReference().getOperationType() == SLE_OpType.getOpTypeByCode(op))
                        {
                            break;
                        }
                    }
                    else if (et.getReference() == DCW_Event_Type.dcwEVT_informOpInvoke)
                    {
                        prompt();
                        System.out.println("Receive " + pop.getReference().getOperationType() + " Invoke Operation");
                        if (pop.getReference().getOperationType() == SLE_OpType.getOpTypeByCode(op))
                        {
                            break;
                        }
                    }
                    else
                    {
                        if (pop.getReference() != null)
                        {
                            System.out.println("Receive " + pop.getReference().getOperationType() + " Operation");
                        }
                    }

                }
                else
                {
                    prompt();
                    System.out.println("No Event");
                    break;
                }
            }
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_test_td)
        {
            long delayTd = 0;
            int delay = 0;
            byte[] tdData;
            EE_Reference<String> arg = new EE_Reference<String>();
            System.out.println("How many times : ");
            utl.read(arg, this.playback);
            int nbTime = Integer.parseInt(arg.getReference());
            System.out.println("Data Length in bytes : ");
            utl.read(arg, this.playback);
            int lg = Integer.parseInt(arg.getReference());
            System.out.println("User defined data ? (y/n) : ");
            utl.read(arg, this.playback);
            if (arg.getReference().equals("y"))
            {
                System.out.println("Data (in Hex) : ");
                utl.read(arg, this.playback);
                tdData = EE_GenStrUtil.hexToBin(arg.getReference());
            }
            else
            {
                tdData = new byte[lg];
                byte[] tmp = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D,
                              0x0E, 0x0F };
                int lg1 = tmp.length;
                System.arraycopy(tmp, 0, tdData, 0, lg1);
            }
            if (this.srvType == SLE_ApplicationIdentifier.sleAI_fwdCltu
                || this.srvType == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)
            {
                if (this.role == SLE_AppRole.sleAR_user)
                {
                    System.out.println("Delay Time between two CLTU or FSP (in microseconds) : ");
                    utl.read(arg, this.playback);
                    delayTd = Integer.parseInt(arg.getReference());
                    System.out.println("Delay for sending TD (in ms) : ");
                    utl.read(arg, this.playback);
                    delay = Integer.parseInt(arg.getReference());
                    testTdSend(lg, nbTime, delayTd, tdData, delay);
                }
                else
                {
                    testTdReceive(lg, nbTime);
                }
            }
            else if (this.srvType == SLE_ApplicationIdentifier.sleAI_rtnAllFrames
                     || this.srvType == SLE_ApplicationIdentifier.sleAI_rtnChFrames
                     || this.srvType == SLE_ApplicationIdentifier.sleAI_rtnChOcf)
            {
                if (this.role == SLE_AppRole.sleAR_user)
                {
                    testTdReceive(lg, nbTime);
                }
                else
                {
                    testTdSend(lg, nbTime, delayTd, tdData, delay);
                }
            }
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_playback_cmd)
        {
            System.out.println("Playback of commands (0=off, 1=on): ");
            EE_Reference<String> toS = new EE_Reference<String>();
            utl.read(toS, this.playback);
            int to = Integer.parseInt(toS.getReference());
            this.playback = (to == 1);
        }
        else if (nextCommand == T_ClientCmd.T_Cmd_wait_event)
        {

            EE_Reference<DCW_Event_Type> et = new EE_Reference<DCW_Event_Type>();
            et.setReference(DCW_Event_Type.dcwEVT_noEvent);

            EE_Reference<IUnknown> psi1 = new EE_Reference<IUnknown>();
            psi1.setReference(null);

            EE_Reference<ISLE_Operation> pop = new EE_Reference<ISLE_Operation>();

            int timeoutSec = 0;
            int timeoutMilliSec = 0;

            EE_Reference<String> secEvent = new EE_Reference<String>();
            System.out.println("Max waiting time for event (sec): ");
            utl.read(secEvent, this.playback);
            timeoutSec = Integer.parseInt(secEvent.getReference());

            EE_Reference<String> nEvent = new EE_Reference<String>();
            System.out.println("Nb of Event to wait for : ");
            utl.read(nEvent, this.playback);

            int nbEvents = Integer.parseInt(nEvent.getReference());
            while (nbEvents > 0)
            {
                HRESULT res = HRESULT.S_OK;
                try
                {
                    this.eventQueue.nextEvent(et, psi1, pop, timeoutSec, timeoutMilliSec);
                }
                catch (SleApiException e)
                {
                    LOG.log(Level.FINE, "SleApiException ", e);
                    res = e.getHResult();
                    break;
                }
                if (res == HRESULT.S_OK)
                {
                    if (psi1.getReference() != null)
                    {
                        ISLE_SIAdmin pSIAdmin = psi1.getReference().queryInterface(ISLE_SIAdmin.class);
                        final ISLE_SII psii1 = pSIAdmin.getServiceInstanceIdentifier();

                        // check if the service instance is the correct one !
                        final ISLE_SII psii2 = this.siAdmin.getServiceInstanceIdentifier();

                        if ((psii1 != null) && (psii2 != null))
                        {
                            if (!psii1.equals(psii2))
                            {
                                prompt();
                                System.out.println("The operation is not received on the correct Service Instance");
                                et.setReference(DCW_Event_Type.dcwEVT_noEvent);
                                if (pop.getReference() != null)
                                {
                                    String tmp = pop.getReference().print(512);
                                    System.out.println(tmp);
                                }
                            }
                        }

                    }
                    switch (et.getReference())
                    {
                    case dcwEVT_protocolAbort:
                        rcvProtocolAbort();
                        break;
                    case dcwEVT_resumeDataTransfer:
                        rcvResumeDataTransfer();
                        break;
                    case dcwEVT_provisionPeriodEnds:
                        rcvProvisionPeriodEnds();
                        break;
                    case dcwEVT_informOpReturn:
                        rcvOpReturn(pop.getReference());
                        break;
                    case dcwEVT_informOpInvoke:
                        rcvOpInvoke(pop.getReference(), false);
                        break;
                    default:
                        break;
                    }

                }
                else
                {
                    prompt();
                    System.out.println("No Event");
                }
                nbEvents--;
            }
        }
    }

    protected ISLE_Time getTime(String tm)
    {
        ISLE_Time time = null;
        try
        {
            time = this.utilFactory.createTime(ISLE_Time.class);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return null;
        }

        if(tm.equals(VOID) == false)
        {
	        HRESULT res = HRESULT.S_OK;
	        try
	        {
	            time.setDateAndTime(tm);
	        }
	        catch (SleApiException e)
	        {
	            res = e.getHResult();
	        }
	
	        if (res != HRESULT.S_OK)
	        {
	            System.out.println("invalid time format: " + tm);
	        }
        }
        else
        {
        	if(this.startTime == null)
        	{
        		time.update();
        	}
        	else if(this.startTime != null)
        	{
        		EE_Time t = new EE_Time();
        		t.update();
        		t = t.add(new EE_Duration(3600*24*7)); // one week
        		try {
					time.setDateAndTime(t.getDateAndTimeCCSDS(EE_TIME_Fmt.eeTIME_FmtB));
				} catch (SleApiException e) {
					e.printStackTrace();
				}
        	}
        }

        return time;
    }

    private void setUpSii()
    {
        EE_Reference<String> what = new EE_Reference<String>();
        ISLE_SII sii = null;
        try
        {
            sii = this.utilFactory.createSII(ISLE_SII.class);
        }
        catch (SleApiException e1)
        {
            LOG.log(Level.FINE, "SleApiException ", e1);
            return;
        }

        while (!what.getReference().equals("ok"))
        {
            System.out.println("rdn    append a single RDN component");
            System.out.println("ascii  set complete id in ascii form");
            System.out.println("initf  set initialFormat (V1)");
            System.out.println("set    assign SII");
            System.out.println("ok     set-up completed");

            System.out.println("Selection: ");
            utl.read(what, this.playback);

            if (what.getReference().equals("rdn"))
            {
                int objId = 0;
                EE_Reference<String> val = new EE_Reference<String>();
                System.out.print("RDN Obj Id: ");
                utl.read(val, this.playback);
                objId = Integer.parseInt(val.getReference());
                val.setReference("");
                System.out.print("RDN value: ");
                utl.read(val, this.playback);
                try
                {
                    sii.addLocalRDN(objId, val.getReference());
                    displayResult(HRESULT.S_OK);
                }
                catch (SleApiException e)
                {
                    displayResult(e.getHResult());
                }
            }
            else if (what.getReference().equals("ascii"))
            {
                EE_Reference<String> val = new EE_Reference<String>();
                System.out.print("SIID ascii form: ");
                utl.read(val, this.playback);
                try
                {
                    sii.setAsciiForm(val.getReference());
                }
                catch (SleApiException e)
                {
                    displayResult(e.getHResult());
                }
            }
            else if (what.getReference().equals("initf"))
            {
                sii.setInitialFormat();
            }
            else if (what.getReference().equals("set"))
            {
                if (!sii.isNull())
                {
                    this.siAdmin.setServiceInstanceId(sii);
                }
                else
                {
                    System.out.println("NULL SIID");
                }
            }
        }
    }

    private ISLE_ConfirmedOperation setReturnOp(ISLE_Operation pop, boolean positiveResult, boolean doConfig)
    {
        ISLE_ConfirmedOperation cop = null;

        if (pop.getOperationType() == SLE_OpType.sleOT_bind)
        {
            this.seqCounter = 1;
        }

        // start return operations must always be setup
        if (!positiveResult
            || doConfig
            || (pop.getOperationType() == SLE_OpType.sleOT_start && (pop.getOpServiceType() == SLE_ApplicationIdentifier.sleAI_fwdCltu || pop
                    .getOpServiceType() == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)))
        {
            switch (pop.getOperationType())
            {
            // operation setups not dependent on service-type:
            case sleOT_stop:
            {
                EE_SYSTST_OpGen.setUpStop(pop, this.playback, this.utl);
                break;
            }
            case sleOT_bind:
            {
                EE_SYSTST_OpGen.setUpBind(pop, this.playback, this.utl);
                break;
            }
            case sleOT_unbind:
            {
                EE_SYSTST_OpGen.setUpUnbind(pop, this.playback, this.utl);
                break;
            }
            case sleOT_scheduleStatusReport:
            {
                EE_SYSTST_OpGen.setUpSSR(pop, this.playback, this.utl);
                break;
            }
            case sleOT_peerAbort:
            {
                EE_SYSTST_OpGen.setUpPeerAbort(pop, this.playback, this.utl);
                break;
            }
            // service-type dependent operation setups:
            case sleOT_start:
            {
                switch (pop.getOpServiceType())
                {
                case sleAI_rtnAllFrames:
                {
                    EE_SYSTST_RAFOpGen.setUpStart(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_rtnChFrames:
                {
                    EE_SYSTST_RCFOpGen.setUpStart(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_rtnChOcf:
                {
                    EE_SYSTST_ROCFOpGen.setUpStart(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_fwdCltu:
                {
                    EE_SYSTST_CLTUOpGen.setUpStart(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_fwdTcSpacePkt:
                {
                    EE_SYSTST_FSPOpGen.setUpStart(pop, this.playback, this.utl);
                    break;
                }
                default:
                    break;
                }
                break;
            }
            case sleOT_asyncNotify:
            {
                switch (pop.getOpServiceType())
                {
                case sleAI_fwdCltu:
                {
                    EE_SYSTST_CLTUOpGen.setUpAsyncNotify(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_fwdTcSpacePkt:
                {
                    EE_SYSTST_FSPOpGen.setUpAsyncNotify(pop, this.playback, this.utl);
                    break;
                }
                default:
                    break;
                }
                break;
            }
            case sleOT_throwEvent:
            {
                switch (pop.getOpServiceType())
                {
                case sleAI_fwdCltu:
                {
                    EE_SYSTST_CLTUOpGen.setUpThrowEvent(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_fwdTcSpacePkt:
                {
                    EE_SYSTST_FSPOpGen.setUpThrowEvent(pop, this.playback, this.utl);
                    break;
                }
                default:
                    break;
                }
                break;
            }
            case sleOT_getParameter:
            {
                switch (pop.getOpServiceType())
                {
                case sleAI_rtnAllFrames:
                {
                    EE_SYSTST_RAFOpGen.setUpGetParameter(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_rtnChFrames:
                {
                    EE_SYSTST_RCFOpGen.setUpGetParameter(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_rtnChOcf:
                {
                    EE_SYSTST_ROCFOpGen.setUpGetParameter(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_fwdCltu:
                {
                    EE_SYSTST_CLTUOpGen.setUpGetParameter(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_fwdTcSpacePkt:
                {
                    EE_SYSTST_FSPOpGen.setUpGetParameter(pop, this.playback, this.utl);
                    break;
                }
                default:
                    break;
                }
                break;
            }
            case sleOT_transferData:
            {
                switch (pop.getOpServiceType())
                {
                case sleAI_rtnAllFrames:
                {
                    EE_SYSTST_RAFOpGen.setUpTransferData(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_rtnChFrames:
                {
                    EE_SYSTST_RCFOpGen.setUpTransferData(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_rtnChOcf:
                {
                    EE_SYSTST_ROCFOpGen.setUpTransferData(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_fwdCltu:
                {
                    EE_SYSTST_CLTUOpGen.setUpTransferData(pop, this.playback, this.utl);
                    break;
                }
                case sleAI_fwdTcSpacePkt:
                {
                    EE_SYSTST_FSPOpGen.setUpTransferData(pop, this.playback, this.utl);
                    break;
                }
                default:
                    break;
                }
                break;
            }
            case sleOT_invokeDirective:
            {
                switch (pop.getOpServiceType())
                {
                case sleAI_fwdTcSpacePkt:
                {
                    EE_SYSTST_FSPOpGen.setUpInvokeDirective(pop, this.playback, this.utl);
                    break;
                }
                default:
                    break;
                }
                break;
            }
            default:
                break;
            }
        }

        cop = pop.queryInterface(ISLE_ConfirmedOperation.class);
        if (positiveResult)
        {
            cop.setPositiveResult();
        }

        return cop;
    }

    public abstract void testTdSend(long lg, int nbtime, long delay_td, byte[] tdData, int delay);

    public abstract void testTdReceive(long lg, int nbtime);

    @SuppressWarnings("unused")
    private boolean checkData(long lg, long lg1, String data2)
    {
        String tmp = "0123456789ABCDEF";
        int lgtmp = tmp.length();
        if (lg1 == lg)
        {
            // check the content of the pdu
            for (int i = 0; i + lgtmp < lg; i += lgtmp)
            {
                if (!strncmp(data2.substring(i), tmp, lgtmp))
                {
                    System.out.println("Data is not correct!!:");
                    System.out.println(data2);
                    return false;
                }
            }
        }
        else
        {
            System.out.println("Data Length is not correct. lg " + lg + " lg1 " + lg1);
            return false;
        }
        return true;
    }

    private boolean strncmp(String str1, String str2, int n)
    {
        str1 = str1.substring(0, n);
        str2 = str2.substring(0, n);
        return str1.equals(str2);
    }

}
