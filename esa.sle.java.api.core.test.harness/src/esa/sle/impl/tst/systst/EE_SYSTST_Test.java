package esa.sle.impl.tst.systst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_TimeSource;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIAdmin;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_BindRole;
import ccsds.sle.api.isle.it.SLE_TimeFmt;
import ccsds.sle.api.isle.it.SLE_TimeRes;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.api.apiop.sleop.EE_SLE_OpFactory;
import esa.sle.impl.api.apiut.EE_SLE_LibraryInstance;
import esa.sle.impl.eapi.bld.ESLE_Builder;
import esa.sle.impl.eapi.dcw.IDCW_EventQueue;
import esa.sle.impl.eapi.dcw.IDCW_SIFactory;
import esa.sle.impl.eapi.dcw.type.DCW_Event_Type;
import esa.sle.impl.ifs.gen.EE_Reference;
import esa.sle.impl.ifs.gen.EE_StubReporter;
import esa.sle.impl.ifs.gen.EE_StubTrace;
import esa.sle.impl.tst.systst.types.T_Cmd;

public class EE_SYSTST_Test implements ITST_Application, IUnknown
{
    private static final Logger LOG = Logger.getLogger(EE_SYSTST_Test.class.getName());

    private ISLE_UtilFactory utilFactory;

    private ISLE_OperationFactory opFactory;

    private IDCW_SIFactory siFactory;

    private IDCW_EventQueue eventQueue;

    private ESLE_Builder builder;

    private ISLE_Reporter reporter;

    private ISLE_Trace trace;

    private SLE_TraceLevel traceLevel;

    private boolean traceStarted;

    private EE_SYSTST_SEUIF userIf;

    private EE_SYSTST_TimeSource timeSource;

    private LinkedList<EE_SYSTST_SITestTool> siList = new LinkedList<EE_SYSTST_SITestTool>();

    private String seConfigFile;

    private String proxyConfigFile;

    private SLE_AppRole role;

    private SLE_BindRole bindRole;

    private boolean playbackStarted;

    private String baseSiiRtn;

    private String baseSiiFwd;

    private final String instanceId;

    public EE_SYSTST_Test(String instanceKey)
    {
    	this.instanceId = instanceKey;
        this.utilFactory = null;
        this.opFactory = null;
        this.reporter = null;
        this.trace = null;
        this.userIf = null;
        this.timeSource = null;
        this.seConfigFile = null;
        this.proxyConfigFile = null;
        this.role = SLE_AppRole.sleAR_user;
        this.playbackStarted = false;
        this.baseSiiRtn = "sagr=SAGR.spack=SPACK.rsl-fg=RSL-FG";
        this.baseSiiFwd = "sagr=SAGR.spack=SPACK.fsl-fg=FSL-FG";
        this.siList = new LinkedList<EE_SYSTST_SITestTool>();
        this.eventQueue = null;
        this.siFactory = null;
    }

    /**
     * Constructor
     */
    public EE_SYSTST_Test()
    {
    	this.instanceId = EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY;
        this.utilFactory = null;
        this.opFactory = null;
        this.reporter = null;
        this.trace = null;
        this.userIf = null;
        this.timeSource = null;
        this.seConfigFile = null;
        this.proxyConfigFile = null;
        this.role = SLE_AppRole.sleAR_user;
        this.playbackStarted = false;
        this.baseSiiRtn = "sagr=SAGR.spack=SPACK.rsl-fg=RSL-FG";
        this.baseSiiFwd = "sagr=SAGR.spack=SPACK.fsl-fg=FSL-FG";
        this.siList = new LinkedList<EE_SYSTST_SITestTool>();
        this.eventQueue = null;
        this.siFactory = null;
    }

    @Override
    public void bind(ISLE_Bind pbind, ITST_Assoc assoc, ITST_Responder pprsp) throws SleApiException
    {
        ISLE_SII aSii = pbind.getServiceInstanceId();
        HRESULT rc = HRESULT.S_OK;

        ListIterator<EE_SYSTST_SITestTool> listIter = this.siList.listIterator();
        while (listIter.hasNext())
        {
            EE_SYSTST_SITestTool siTool = listIter.next();
            ISLE_SII theSii = siTool.getSII();
            if (aSii == theSii)
            {
                // link the assoc and the AssocClient:
                rc = siTool.bind(assoc, pprsp);
                throw new SleApiException(rc);
            }
        }
        throw new SleApiException(HRESULT.S_OK);
    }

    @Override
    public void releaseAssoc(ITST_Assoc assoc) throws SleApiException
    {
        throw new SleApiException(HRESULT.S_OK);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ITST_Application.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    public HRESULT configure(final String configFilePathSE,
                             final String configFilePathProxy,
                             SLE_AppRole appRole,
                             SLE_TraceLevel tracelevelP,
                             boolean traceStartedP)
    {

        HRESULT res;
        this.seConfigFile = configFilePathSE;
        this.proxyConfigFile = configFilePathProxy;
        this.role = appRole;
        this.traceLevel = tracelevelP;
        this.traceStarted = traceStartedP;
        this.trace = null;
        this.reporter = null;

        if (this.role == SLE_AppRole.sleAR_user)
        {
            this.bindRole = SLE_BindRole.sleBR_initiator;
        }
        else if (this.role == SLE_AppRole.sleAR_provider)
        {
            this.bindRole = SLE_BindRole.sleBR_responder;
        }

        // create report and trace interfaces
        EE_StubReporter sr = new EE_StubReporter();
        this.reporter = sr.queryInterface(ISLE_Reporter.class);

        EE_StubTrace st = new EE_StubTrace();
        this.trace = st.queryInterface(ISLE_Trace.class);
        // create a time source for testing
        // a time offset can be used with this time source
        if (this.timeSource == null)
        {
            this.timeSource = new EE_SYSTST_TimeSource();
        }

        ISLE_TimeSource ts = this.timeSource.queryInterface(ISLE_TimeSource.class);

        // create the builder
        this.builder = ESLE_Builder.getESLE_Builder(this.instanceId);

        res = this.builder.initialise(configFilePathSE, configFilePathProxy, this.reporter, this.bindRole, ts);

        if (res == HRESULT.S_OK)
        {
            this.siFactory = this.builder.getSIFactory();
            this.eventQueue = this.builder.getEventQueue();
            this.utilFactory = this.builder.getUtilFactory();
            res = HRESULT.S_OK;
            ISLE_Time pTime = null;
            try
            {
                pTime = this.utilFactory.createTime(ISLE_Time.class);
            }
            catch (SleApiException e)
            {
                res = e.getHResult();
            }
            if (res == HRESULT.S_OK)
            {
                String tmp = pTime.getTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_minutes);
                System.out.print("######" + tmp + "\n");
                tmp = pTime.getDateAndTime(SLE_TimeFmt.sleTF_dayOfMonth, SLE_TimeRes.sleTR_minutes);
                System.out.print("######" + tmp + "\n");
            }
            if (this.traceStarted)
            {
                // set the traces
                ISLE_TraceControl pIsleTcc = null;
                pIsleTcc = this.siFactory.queryInterface(ISLE_TraceControl.class);
                res = HRESULT.S_OK;
                try
                {
                    pIsleTcc.startTrace(this.trace, this.traceLevel, true);
                }
                catch (SleApiException e)
                {
                    res = e.getHResult();
                }
            }
            // create the Operation Factory
            EE_SLE_OpFactory.initialiseInstance(this.instanceId, this.reporter);
            this.opFactory = EE_SLE_OpFactory.getInstance(this.instanceId);
        }

        if (res != HRESULT.S_OK)
        {
            System.out.println();
            System.out.println("*** Configuration of API failed: " + res);
        }

        return res;
    }

    /**
     * Starts processing for the test tool. It waits for commands entered by the
     * tester and starts processing according to the supplied command
     * 
     * @param playback
     * @return
     */
    public HRESULT start(UTL utl, boolean playback)
    {
        if (this.userIf == null)
        {
            this.userIf = new EE_SYSTST_SEUIF(this.role, utl);
        }
        
        EE_Reference<String> arg1, arg2, arg3, arg4;
        HRESULT rc = HRESULT.S_OK;
        T_Cmd cmd = T_Cmd.T_Cmd_Max;

        System.out.println();
        System.out.println("API Test Harness");
        System.out.println();
        while (cmd != T_Cmd.T_Cmd_exit)
        {
            arg1 = new EE_Reference<String>();
            arg2 = new EE_Reference<String>();
            arg3 = new EE_Reference<String>();
            arg4 = new EE_Reference<String>();

            cmd = this.userIf.getNextCommand(arg1, arg2, arg3, arg4, playback);

            if (cmd == T_Cmd.T_Cmd_start)
            {
                rc = this.builder.start();
                displayResult(rc);
            }
            else if (cmd == T_Cmd.T_Cmd_terminate)
            {
                rc = this.builder.terminate();
                displayResult(rc);
            }
            else if (cmd == T_Cmd.T_Cmd_shutdown)
            {
                // stop the traces
                ISLE_TraceControl pIsleTcc = null;
                if (this.siFactory != null)
                {

                    pIsleTcc = this.siFactory.queryInterface(ISLE_TraceControl.class);
                    try
                    {
                        pIsleTcc.stopTrace();
                    }
                    catch (SleApiException e)
                    {
                        // if (this.LOG.isLoggable(Level.FINEST))
                        // {
                        // this.LOG.finest("Expected : " + e.getHResult() +
                        // " from stopTrace()");
                        // }
                    }
                }
                rc = this.builder.shutdown();
                displayResult(rc);
                if (rc == HRESULT.S_OK)
                {
                    if (this.utilFactory != null)
                    {
                        this.utilFactory = null;
                    }
                    if (this.opFactory != null)
                    {
                        this.opFactory = null;
                    }
                    if (this.siFactory != null)
                    {
                        this.siFactory = null;
                    }
                    if (this.eventQueue != null)
                    {
                        this.eventQueue = null;
                    }
                    return HRESULT.S_OK;
                }
            }
            else if (cmd == T_Cmd.T_Cmd_initialise)
            {
                System.out.println("Initialisation of the Builder");
                if ((rc = this.builder.initialise(this.seConfigFile,
                                                  this.proxyConfigFile,
                                                  this.reporter,
                                                  this.bindRole,
                                                  this.timeSource)) == HRESULT.S_OK)
                {
                    this.siFactory = this.builder.getSIFactory();
                    this.eventQueue = this.builder.getEventQueue();
                    this.utilFactory = this.builder.getUtilFactory();

                    // Create the operation factory
                    EE_SLE_OpFactory.initialiseInstance(this.instanceId, this.reporter);
                    this.opFactory = EE_SLE_OpFactory.getInstance(this.instanceId);
                }
                displayResult(rc);
            }
            else if (cmd == T_Cmd.T_Cmd_create_si)
            {
                rc = createSI(arg1.getReference(), arg2.getReference(), arg3.getReference(), arg4.getReference(), utl);
                displayResult(rc);
            }
            else if (cmd == T_Cmd.T_Cmd_use_si)
            {
                rc = useSI(arg1.getReference(), arg2.getReference(), arg3.getReference(), arg4.getReference(), utl);
                displayResult(rc);
            }
            else if (cmd == T_Cmd.T_Cmd_destroy_si)
            {
                rc = destroySI(arg1.getReference(), utl);
                displayResult(rc);
            }
            else if (cmd == T_Cmd.T_Cmd_list_si)
            {
                int j = 0;
                for (EE_SYSTST_SITestTool siTool : this.siList)
                {
                    j++;
                    ISLE_SII theSii = siTool.getSII();
                    System.out
                            .println(j + ": " + theSii.getAsciiForm() + ", state = " + siTool.getSIState().toString());
                }
            }
            else if (cmd == T_Cmd.T_Cmd_base_sii_rtn)
            {
                this.baseSiiRtn = arg1.getReference();
            }
            else if (cmd == T_Cmd.T_Cmd_base_sii_fwd)
            {
                this.baseSiiFwd = arg1.getReference();
            }
            else if (cmd == T_Cmd.T_Cmd_wait_event_all_si)
            {

                EE_Reference<DCW_Event_Type> et = new EE_Reference<>();
                EE_Reference<IUnknown> psi1 = new EE_Reference<>();
                EE_Reference<ISLE_Operation> pop = new EE_Reference<>();

                int to = Integer.parseInt(arg1.getReference());
                int nb_event = Integer.parseInt(arg2.getReference());

                while (nb_event > 0)
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
                        EE_SYSTST_SIClient siClient = null;

                        if (psi1.getReference() != null)
                        {
                            ISLE_SIAdmin pSIAdmin = psi1.getReference().queryInterface(ISLE_SIAdmin.class);
                            ISLE_SII psii1 = pSIAdmin.getServiceInstanceIdentifier();
                            EE_SYSTST_SITestTool siTool = null;

                            // try to find the correct service instance
                            ListIterator<EE_SYSTST_SITestTool> listIterator = this.siList.listIterator();
                            while (listIterator.hasNext())
                            {
                                siTool = listIterator.next();
                                ISLE_SII theSii = siTool.getSII();
                                if (psii1.equals(theSii))
                                {
                                    break;
                                }
                                else
                                {
                                    siTool = null;
                                }
                            }

                            if (siTool != null)
                            {
                                siClient = siTool.siClient;
                            }
                        }
                        switch (et.getReference())
                        {
                        case dcwEVT_protocolAbort:
                            if (siClient != null)
                            {
                                siClient.rcvProtocolAbort();
                            }
                            break;
                        case dcwEVT_resumeDataTransfer:
                            if (siClient != null)
                            {
                                siClient.rcvResumeDataTransfer();
                            }
                            break;
                        case dcwEVT_provisionPeriodEnds:
                            if (siClient != null)
                            {
                                siClient.rcvProvisionPeriodEnds();
                            }
                            break;
                        case dcwEVT_informOpReturn:
                            if (siClient != null)
                            {
                                siClient.rcvOpReturn(pop.getReference());
                            }
                            break;
                        case dcwEVT_informOpInvoke:
                            if (siClient != null)
                            {
                                siClient.rcvOpInvoke(pop.getReference(), true);
                            }
                            break;
                        default:
                            break;
                        }
                    }

                }

            }
            else if (cmd == T_Cmd.T_Cmd_down)
            {
                boolean found = false;
                for (EE_SYSTST_SITestTool siTool : this.siList)
                {
                    ISLE_SII theSii = siTool.getSII();
                    String lastRdn = theSii.getLastRDN();
                    if (lastRdn.compareToIgnoreCase(arg1.getReference()) == 0)
                    {
                        found = true;
                        siTool.startUIF(playback);
                    }
                }
                if (!found)
                {
                    System.out.println("No such service instance");
                }
            }
            else if (cmd == T_Cmd.T_Cmd_start_rec)
            {
                if (utl.getRecordingStream() != null)
                {
                    System.out.println("Recording already active ");
                }
                else
                {
                    EE_Reference<String> recordingFile = new EE_Reference<String>();
                    System.out.println("Recording file: ");
                    utl.read(recordingFile, playback);
                    try
                    {
                        utl.setRecordingStream(new BufferedWriter(new FileWriter(recordingFile.getReference())));
                    }
                    catch (IOException e)
                    {
                        LOG.log(Level.FINE, "IOException ", e);
                    }
                    System.out.println("Recording started");
                }
            }
            else if (cmd == T_Cmd.T_Cmd_stop_rec)
            {
                if (this.playbackStarted)
                {
                    // the end of the playback
                    System.out.println();
                    return HRESULT.S_OK;
                }
                if (utl.getRecordingStream() == null)
                {
                    System.out.println("Recording not active ");
                }
                else
                {

                    try
                    {
                    	utl.getRecordingStream().close();
                    }
                    catch (IOException e)
                    {
                        System.out.println(e.getMessage());
                    }
                    utl.setRecordingStream(null);
                    System.out.println("Recording stopped");
                }
            }
            else if (cmd == T_Cmd.T_Cmd_playback)
            {
                EE_Reference<String> playbackFile = new EE_Reference<String>();
                System.out.println("Playback file: ");
                utl.read(playbackFile, playback);
                BufferedReader oldIs = utl.getInputReader();
                BufferedReader newis = null;
                try
                {
                    newis = new BufferedReader(new FileReader(playbackFile.getReference()));
                }
                catch (FileNotFoundException e1)
                {
                    LOG.log(Level.FINE, "FileNotFoundException ", e1);
                }
                utl.setInputReader(newis);
                this.playbackStarted = true;
                start(utl, true);
                this.playbackStarted = false;
                System.out.println("Playback from " + playbackFile + " completed");
                utl.setInputReader(oldIs);;
                try
                {
                    newis.close();
                }
                catch (IOException e)
                {
                    LOG.log(Level.FINE, "IOException ", e);
                }
            }
        }

        return HRESULT.S_OK;
    }

    private void displayResult(HRESULT rc)
    {
        System.out.println("######> Result:" + rc + " ######");
    }

    /**
     * Creates a SI on the Service Element, creates an SIClient, an AssocClient
     * and links the interfaces
     * 
     * @param srvType
     * @param localRDN
     * @param aRole
     * @param aVersion
     * @return
     */
    private HRESULT createSI(String srvType, String localRDN, String aRole, String aVersion, UTL utl)
    {
        SLE_ApplicationIdentifier aid = SLE_ApplicationIdentifier.sleAI_invalid;
        int objId = 0;
        String pBaseSii = null; // ready for initial format
        boolean useInitialFormat = false;

        int version = Integer.parseInt(aVersion);

        if (srvType.equals("RAF"))
        {
            objId = 22;
            aid = SLE_ApplicationIdentifier.sleAI_rtnAllFrames;
            if (version == 1)
            {
                useInitialFormat = true;
            }
            else
            {
                pBaseSii = this.baseSiiRtn;
            }
        }
        else if (srvType.equals("RCF"))
        {
            objId = 46;
            aid = SLE_ApplicationIdentifier.sleAI_rtnChFrames;
            if (version == 1)
            {
                useInitialFormat = true;
            }
            else
            {
                pBaseSii = this.baseSiiRtn;
            }
        }
        else if (srvType.equals("CLTU"))
        {
            objId = 7;
            aid = SLE_ApplicationIdentifier.sleAI_fwdCltu;
            if (version == 1)
            {
                useInitialFormat = true;
            }
            else
            {
                pBaseSii = this.baseSiiFwd;
            }
        }
        else if (srvType.equals("ROCF"))
        {
            objId = 49;
            aid = SLE_ApplicationIdentifier.sleAI_rtnChOcf;
            pBaseSii = this.baseSiiRtn;
        }
        else if (srvType.equals("FSP"))
        {
            objId = 10;
            aid = SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt;
            pBaseSii = this.baseSiiFwd;
        }
        else if (srvType.equals("FTCF"))
        {
            objId = 12;
            aid = SLE_ApplicationIdentifier.sleAI_fwdTcFrame;
            pBaseSii = this.baseSiiFwd;
        }

        switch (aid)
        {
        case sleAI_rtnAllFrames:
        case sleAI_rtnChFrames:
        case sleAI_fwdCltu:
        {
            if (version != 1 && version != 2 && version != 3 && version != 4 && version != 5)
            {
                System.out.println("WARNING: Illegal version for service " + srvType + ": " + version);
            }
            break;
        }
        case sleAI_rtnChOcf:
        case sleAI_fwdTcSpacePkt:
        {
            if (version != 1 && version != 2 && version != 4 && version != 5)
            {
                System.out.println("WARNING: Illegal version for service " + srvType + ": " + version);
            }
            break;
        }
        default:
            break;
        }

        SLE_AppRole theRole = SLE_AppRole.sleAR_user;
        if (aRole.equals("p"))
        {
            theRole = SLE_AppRole.sleAR_provider;
        }

        ISLE_SII sii = null;

        try
        {
            sii = this.utilFactory.createSII(ISLE_SII.class);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return e.getHResult();
        }

        String error = "";

        try
        {
            if (useInitialFormat)
            {
                sii.setInitialFormat();
                sii.addLocalRDN(objId, localRDN);
            }
            else
            {
                String asciiSii = pBaseSii + ".";
                asciiSii += localRDN;
                sii.setAsciiForm(asciiSii);
            }
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            error = e.getHResult().toString();
        }

        if (sii.isNull())
        {
            System.out.println("Invalid Service Instance Id: " + localRDN + ", error = " + error);
            return HRESULT.E_FAIL;
        }

        if (aid == SLE_ApplicationIdentifier.sleAI_invalid)
        {
            return HRESULT.EE_E_REJECTED;
        }

        // create the test-responder (ISLE_ServiceInform)
        EE_SYSTST_SIClient sic = null;
        EE_SYSTST_AssocClient ac = null;
        if (aid == SLE_ApplicationIdentifier.sleAI_rtnAllFrames)
        {
            sic = new EE_SYSTST_RAFSIClient(this.role, this.timeSource, utl);
            ac = new EE_SYSTST_RAFAssocClient(utl);
        }
        else if (aid == SLE_ApplicationIdentifier.sleAI_rtnChFrames)
        {
            sic = new EE_SYSTST_RCFSIClient(this.role, this.timeSource, utl);
            ac = new EE_SYSTST_RCFAssocClient(utl);
        }
        else if (aid == SLE_ApplicationIdentifier.sleAI_fwdCltu)
        {
            sic = new EE_SYSTST_CLTUSIClient(this.role, this.timeSource, utl);
            ac = new EE_SYSTST_CLTUAssocClient(utl);
        }
        else if (aid == SLE_ApplicationIdentifier.sleAI_rtnChOcf)
        {
            sic = new EE_SYSTST_ROCFSIClient(this.role, this.timeSource, utl);
            ac = new EE_SYSTST_ROCFAssocClient(utl);
        }
        else if (aid == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)
        {
            sic = new EE_SYSTST_FSPSIClient(this.role, this.timeSource, utl);
            ac = new EE_SYSTST_FSPAssocClient(utl);
        }
        else
        {
            sic = new EE_SYSTST_FTCFSIClient(this.role, this.timeSource, utl);
            /** for error test-cases */
            ac = new EE_SYSTST_FTCFAssocClient(utl);
        }

        sic.setVersion(version);
        sic.init(this.utilFactory, this.opFactory);

        ac.init(this.utilFactory, this.opFactory);

        ISLE_SIAdmin siAdmin = null;

        try
        {
            siAdmin = this.siFactory.createServiceInstance(ISLE_SIAdmin.class, aid, version, theRole, 4);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return e.getHResult();
        }

        if (siAdmin != null)
        {
            siAdmin.setServiceInstanceId(sii);
            sic.setSIAdmin(siAdmin, this.eventQueue);
            EE_SYSTST_SITestTool siTT = new EE_SYSTST_SITestTool(sic, ac, sii, this.role);
            this.siList.add(siTT);
        }

        return HRESULT.S_OK;
    }

    /**
     * Creates a SI from a full SI identifier on the Service Element, creates an
     * SIClient, an AssocClient and links the interfaces
     * 
     * @param srvType
     * @param localRDN
     * @param aRole
     * @param aVersion
     * @return
     */
    private HRESULT useSI(String srvType, String siid, String aRole, String aVersion, UTL utl)
    {
        SLE_ApplicationIdentifier aid = SLE_ApplicationIdentifier.sleAI_invalid;
        @SuppressWarnings("unused")
        int objId = 0;
        boolean useInitialFormat = false; // SII in initial format?? (only for
                                          // RAF,RCF,CLTU)

        int version = Integer.parseInt(aVersion);

        if (srvType.equals("RAF"))
        {
            objId = 22;
            aid = SLE_ApplicationIdentifier.sleAI_rtnAllFrames;
            if (version == 1)
            {
                useInitialFormat = true;
            }
        }
        else if (srvType.equals("RCF"))
        {
            objId = 46;
            aid = SLE_ApplicationIdentifier.sleAI_rtnChFrames;
            if (version == 1)
            {
                useInitialFormat = true;
            }
        }
        else if (srvType.equals("CLTU"))
        {
            objId = 7;
            aid = SLE_ApplicationIdentifier.sleAI_fwdCltu;
            if (version == 1)
            {
                useInitialFormat = true;
            }
        }
        else if (srvType.equals("ROCF"))
        {
            objId = 49;
            aid = SLE_ApplicationIdentifier.sleAI_rtnChOcf;
        }
        else if (srvType.equals("FSP"))
        {
            objId = 10;
            aid = SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt;
        }
        else if (srvType.equals("FTCF"))
        {
            objId = 12;
            aid = SLE_ApplicationIdentifier.sleAI_fwdTcFrame;
        }

        switch (aid)
        {
        case sleAI_rtnAllFrames:
        case sleAI_rtnChFrames:
        case sleAI_fwdCltu:
        {
            if (version != 1 && version != 2 && version != 3 && version != 4 && version != 5)
            {
                System.out.println("WARNING: Illegal version for service " + srvType + ": " + version);
            }
            break;
        }
        case sleAI_rtnChOcf:
        case sleAI_fwdTcSpacePkt:
        {
            if (version != 1 && version != 2 && version != 4 && version != 5)
            {
                System.out.println("WARNING: Illegal version for service " + srvType + ": " + version);
            }
            break;
        }
        default:
            break;
        }

        SLE_AppRole theRole = SLE_AppRole.sleAR_user;
        if (aRole.equals("p"))
        {
            theRole = SLE_AppRole.sleAR_provider;
        }

        ISLE_SII sii = null;

        try
        {
            sii = this.utilFactory.createSII(ISLE_SII.class);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return e.getHResult();
        }

        String error = "";

        try
        {
            if (useInitialFormat)
            {
                sii.setInitialFormat();
            }
            sii.setAsciiForm(siid);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            error = e.getHResult().toString();
        }

        if (sii.isNull())
        {
            System.out.println("Invalid Service Instance Id: " + siid + ", error = " + error);
            return HRESULT.E_FAIL;
        }

        if (aid == SLE_ApplicationIdentifier.sleAI_invalid)
        {
            return HRESULT.EE_E_REJECTED;
        }

        // create the test-responder (ISLE_ServiceInform)
        EE_SYSTST_SIClient sic = null;
        EE_SYSTST_AssocClient ac = null;
        if (aid == SLE_ApplicationIdentifier.sleAI_rtnAllFrames)
        {
            sic = new EE_SYSTST_RAFSIClient(this.role, this.timeSource, utl);
            ac = new EE_SYSTST_RAFAssocClient(utl);
        }
        else if (aid == SLE_ApplicationIdentifier.sleAI_rtnChFrames)
        {
            sic = new EE_SYSTST_RCFSIClient(this.role, this.timeSource, utl);
            ac = new EE_SYSTST_RCFAssocClient(utl);
        }
        else if (aid == SLE_ApplicationIdentifier.sleAI_fwdCltu)
        {
            sic = new EE_SYSTST_CLTUSIClient(this.role, this.timeSource, utl);
            ac = new EE_SYSTST_CLTUAssocClient(utl);
        }
        else if (aid == SLE_ApplicationIdentifier.sleAI_rtnChOcf)
        {
            sic = new EE_SYSTST_ROCFSIClient(this.role, this.timeSource, utl);
            ac = new EE_SYSTST_ROCFAssocClient(utl);
        }
        else if (aid == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)
        {
            sic = new EE_SYSTST_FSPSIClient(this.role, this.timeSource, utl);
            ac = new EE_SYSTST_FSPAssocClient(utl);
        }
        else
        {
            sic = new EE_SYSTST_FTCFSIClient(this.role, this.timeSource, utl);
            /** for error test-cases */
            ac = new EE_SYSTST_FTCFAssocClient(utl);
        }

        sic.setVersion(version);
        sic.init(this.utilFactory, this.opFactory);

        ac.init(this.utilFactory, this.opFactory);

        ISLE_SIAdmin siAdmin = null;

        try
        {
            siAdmin = this.siFactory.createServiceInstance(ISLE_SIAdmin.class, aid, version, theRole, 4);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return e.getHResult();
        }

        if (siAdmin != null)
        {
            siAdmin.setServiceInstanceId(sii);
            sic.setSIAdmin(siAdmin, this.eventQueue);
            EE_SYSTST_SITestTool siTT = new EE_SYSTST_SITestTool(sic, ac, sii, this.role);
            this.siList.add(siTT);
        }

        return HRESULT.S_OK;
    }

    private HRESULT destroySI(String localRDN, UTL utl)
    {
        HRESULT rc = HRESULT.SLE_E_UNKNOWN;
        ListIterator<EE_SYSTST_SITestTool> listIter = this.siList.listIterator();
        while (listIter.hasNext())
        {
            EE_SYSTST_SITestTool tt = listIter.next();

            ISLE_SII siid = tt.getSII();
            String tmp = siid.getLastRDN();
            if (tmp.equals(localRDN))
            {
                IUnknown iu = tt.getSIIF();

                // stop the traces

                ISLE_TraceControl p_isle_tcc = null;
                if (iu != null)
                {
                    p_isle_tcc = iu.queryInterface(ISLE_TraceControl.class);
                    try
                    {
                        p_isle_tcc.stopTrace();
                    }
                    catch (SleApiException e)
                    {

                        // if (this.LOG.isLoggable(Level.FINEST))
                        // {
                        // this.LOG.finest("Expected : " + e.getHResult() +
                        // " from stopTrace()");
                        // }
                    }

                }

                // destroy the service instance
                rc = HRESULT.S_OK;
                try
                {
                    this.siFactory.destroyServiceInstance(iu);
                }
                catch (SleApiException e)
                {
                    rc = e.getHResult();
                }

                // destroy the test tool
                if (rc == HRESULT.S_OK)
                {
                    this.siList.remove(tt);
                }
                return rc;
            }

        }
        return rc;

    }
}
