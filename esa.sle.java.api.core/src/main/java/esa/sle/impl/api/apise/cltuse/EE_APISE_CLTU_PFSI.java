package esa.sle.impl.api.apise.cltuse;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_Stop;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.ise.ISLE_SIOpFactory;
import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_DeliveryMode;
import ccsds.sle.api.isle.it.SLE_LogMessageType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.it.SLE_SIState;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isrv.icltu.ICLTU_AsyncNotify;
import ccsds.sle.api.isrv.icltu.ICLTU_GetParameter;
import ccsds.sle.api.isrv.icltu.ICLTU_SIAdmin;
import ccsds.sle.api.isrv.icltu.ICLTU_SIUpdate;
import ccsds.sle.api.isrv.icltu.ICLTU_Start;
import ccsds.sle.api.isrv.icltu.ICLTU_StatusReport;
import ccsds.sle.api.isrv.icltu.ICLTU_ThrowEvent;
import ccsds.sle.api.isrv.icltu.ICLTU_TransferData;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwGvcId;
import ccsds.sle.api.isrv.icltu.types.CLTU_ClcwPhysicalChannel;
import ccsds.sle.api.isrv.icltu.types.CLTU_EventResult;
import ccsds.sle.api.isrv.icltu.types.CLTU_Failure;
import ccsds.sle.api.isrv.icltu.types.CLTU_GetParameterDiagnostic;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_NotificationType;
import ccsds.sle.api.isrv.icltu.types.CLTU_ParameterName;
import ccsds.sle.api.isrv.icltu.types.CLTU_PlopInEffect;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProtocolAbortMode;
import ccsds.sle.api.isrv.icltu.types.CLTU_Status;
import ccsds.sle.api.isrv.icltu.types.CLTU_TransferDataDiagnostic;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;
import esa.sle.impl.api.apise.slese.EE_APISE_PConfiguration;
import esa.sle.impl.api.apise.slese.EE_APISE_PFSI;
import esa.sle.impl.api.apise.slese.types.EE_TI_SLESE_Event;
import esa.sle.impl.ifs.gen.EE_LogMsg;

/**
 * Provider Forward Service Instance This class provides the functionality that
 * is specific to CLTU forward service instances for provider applications. It
 * is responsible for the creation of CLTU service specific operation objects
 * for a provider application, and for checking of the compatibility of
 * invocation and return PDUs with the CLTU provider role. Furthermore the class
 * collects statistical information to be compiled in the status report, which
 * is set-up and passed to the proxy by this class. The class implements the
 * interfaces ICLTU_SIAdmin and ICLTU_SIUpdate. The class implements the
 * update-mechanism and automatic sending of notifications as specified in the
 * CLTU supplement, section 3.1.3. For a GET-PARAMETER invocation the class is
 * responsible to set the value of the desired parameter If the class is
 * requested to perform a status report ( doStatusReport() ), it creates and
 * initialises a CLTU-STATUS-REPORT operation using the data collected for the
 * status information. For PDUs received from the application, the class looks
 * at TRANSFER-DATA and THROW-EVENT return PDU's in order to update the status
 * information parameters from the PDUs received. If a CLTU-GET-PARAMETER is
 * received from the proxy, the class sets the desired parameter-value (function
 * setUpGetParameter()) and passes the return-PDU back to the proxy. Note that
 * the class applies the following approach for CLTU-TRANSFER-DATA and
 * CLTU-THROW-EVENT invocations: if the check of the invocation fails, the
 * diagnostic code is set and the PDU is passed to the application (which checks
 * the result and initiates the return-PDU). See CLTU-supplement, sections
 * 3.1.4.1 and 3.1.4.2 for more information
 */
public class EE_APISE_CLTU_PFSI extends EE_APISE_PFSI implements ICLTU_SIAdmin, ICLTU_SIUpdate, ISLE_SIOpFactory
{
    private static final Logger LOG = Logger.getLogger(EE_APISE_CLTU_PFSI.class.getName());

    private final EE_APISE_CLTU_LastProcessed lastProcessed = new EE_APISE_CLTU_LastProcessed();

    private final EE_APISE_CLTU_Configuration config = new EE_APISE_CLTU_Configuration();

    private final EE_APISE_CLTU_LastOK lastOK = new EE_APISE_CLTU_LastOK();

    private final EE_APISE_CLTU_StatusInformation statusInfo = new EE_APISE_CLTU_StatusInformation();

    private final ReentrantLock obj = new ReentrantLock();


    public EE_APISE_CLTU_PFSI(String instanceKey, ISLE_ServiceInform clientIf)
    {
        super(instanceKey, SLE_ApplicationIdentifier.sleAI_fwdCltu, clientIf);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        T ppv = super.queryInterface(iid);
        if (ppv != null)
        {
            return ppv;
        }
        else
        {
            if (iid == ICLTU_SIAdmin.class)
            {
                return (T) this;
            }
            else if (iid == ICLTU_SIUpdate.class)
            {
                return (T) this;
            }
            else if (iid == ISLE_SIOpFactory.class)
            {
                return (T) this;
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * See specification of ICLTU_SIAdmin
     */
    @Override
    public void setBitLockRequired(SLE_YesNo yesno)
    {
        this.obj.lock();
        this.config.setBitLockRequired(yesno);
        this.obj.unlock();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public void setMaximumSlduLength(long length)
    {
        this.obj.lock();
        this.config.setMaxCltuLength(length);
        this.obj.unlock();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public void setModulationFrequency(long frequency)
    {
        this.obj.lock();
        this.config.setModulationFrequency(frequency);
        this.obj.unlock();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public void setModulationIndex(int index)
    {
        this.obj.lock();
        this.config.setModulationIndex(index);
        this.obj.unlock();
    }

    /**
     * @FunctionSee specification of ICLTU_SIAdmin.@EndFunction
     */
    @Override
    public void setPlopInEffect(CLTU_PlopInEffect plop)
    {
        this.obj.lock();
        this.config.setPlopInEffect(plop);
        this.obj.unlock();
    }

    /**
     * See specification of ICLTU_SIAdmin
     */
    @Override
    public void setRfAvailableRequired(SLE_YesNo yesno)
    {
        this.obj.lock();
        this.config.setRfAvailRequired(yesno);
        this.obj.unlock();
    }

    /**
     * See specification of ICLTU_SIAdmin
     */
    @Override
    public void setSubcarrierToBitRateRatio(int divisor)
    {
        this.obj.lock();
        this.config.setScToBitrateRatio(divisor);
        this.obj.unlock();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public void setMaximumBufferSize(long size)
    {
        this.config.setMaxBufferSize(size);
        this.statusInfo.setMaxBufferSize(size);

        // allow adjusting the CLTU buffer to the correct value at SI loading time after ConfigCompleted()
        if(this.statusInfo.getCltuBufferAvailable() > size) {
            this.statusInfo.setCltuBufferAvailable(size);
        }
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public void setInitialProductionStatus(CLTU_ProductionStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.lock();
        this.statusInfo.setProductionStatus(status);
        this.statusInfo.unlock();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public void setInitialUplinkStatus(CLTU_UplinkStatus status)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.statusInfo.lock();
        this.statusInfo.setUplinkStatus(status);
        this.statusInfo.unlock();

    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public void setNotificationMode(CLTU_NotificationMode mode)
    {
        if (isConfigured() == true)
        {
            return;
        }
        this.obj.lock();
        this.config.setNotificationMode(mode);
        this.obj.unlock();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public SLE_YesNo getBitLockRequired()
    {
        return this.config.getBitLockRequired();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public long getMaximumSlduLength()
    {
        return this.config.getMaxCltuLength();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public long getModulationFrequency()
    {
        return this.config.getModulationFrequency();
    }

    /**
     * See specification of ICLTU_SIAdmin
     */
    @Override
    public int getModulationIndex()
    {
        return this.config.getModulationIndex();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public CLTU_PlopInEffect getPlopInEffect()
    {
        return this.config.getPlopInEffect();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public SLE_YesNo getRfAvailableRequired()
    {
        return this.config.getRfAvailRequired();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public int getSubcarrierToBitRateRatio()
    {
        return this.config.getScToBitrateRatio();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public long getMaximumBufferSize()
    {
        return this.config.getMaxBufferSize();
    }

    /**
     * See specification of ICLTU_SIAdmin.
     */
    @Override
    public CLTU_NotificationMode getNotificationMode()
    {
        return this.config.getNotificationMode();
    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public void cltuStarted(long id, ISLE_Time radiationStartTime, long bufferAvailable)
    {
        this.statusInfo.lock();
        this.lastProcessed.lock();

        this.statusInfo.incrNumProcessed();
        this.lastProcessed.setCltuId(id);
        this.lastProcessed.setRadiationStartTime(radiationStartTime);

        this.lastProcessed.setCltuStatus(CLTU_Status.cltuST_radiationStarted);
        this.statusInfo.setCltuBufferAvailable(bufferAvailable);

        this.lastProcessed.unlock();
        this.statusInfo.unlock();
    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public void cltuRadiated(ISLE_Time radiationStopTime, ISLE_Time radiationStartTime, boolean notify)
    {
        this.obj.lock();

        this.statusInfo.lock();
        this.lastProcessed.lock();
        this.lastOK.lock();

        this.statusInfo.incrNumRadiated();
        this.lastProcessed.setCltuStatus(CLTU_Status.cltuST_radiated);

        this.lastOK.setCltuId(this.lastProcessed.getCltuId());
        this.lastOK.setRadiationStopTime(radiationStopTime);

        if (radiationStartTime != null)
        {
            this.lastProcessed.setRadiationStartTime(radiationStartTime);
        }

        this.statusInfo.unlock();
        this.lastProcessed.unlock();
        this.lastOK.unlock();

        this.obj.unlock();

        if (notify == true)
        {
            this.obj.lock();
            sendNotification(CLTU_NotificationType.cltuNT_cltuRadiated);
            this.obj.unlock();
        }
    }

    /**
     * See specification of ICLTU_SIUpdate.
     * 
     * @throws SleApiException
     */
    @Override
    public void cltuNotStarted(long id, CLTU_Failure reason, long bufferAvailable, boolean notify) throws SleApiException
    {
        this.obj.lock();
        CLTU_ProductionStatus ps = this.statusInfo.getProductionStatus();
        CLTU_NotificationMode nm = this.config.getNotificationMode();
        if (reason == CLTU_Failure.cltuF_interrupted && ps != CLTU_ProductionStatus.cltuPS_interrupted)
        {
            this.obj.unlock();
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT, "SLE_E_INCONSISTENT");
        }
        if (nm == CLTU_NotificationMode.cltuNM_immediate && ps == CLTU_ProductionStatus.cltuPS_interrupted)
        {
            this.obj.unlock();
            throw new SleApiException(HRESULT.SLE_E_INCONSISTENT);
        }

        HRESULT rc = HRESULT.S_OK;
        if (this.lastProcessed.getCltuStatus() == CLTU_Status.cltuST_radiationStarted)
        {
            rc = HRESULT.SLE_E_SEQUENCE;
        }
        if (getSIState() == SLE_SIState.sleSIS_unbound)
        {
            rc = HRESULT.SLE_E_STATE;
        }

        this.statusInfo.lock();
        this.lastProcessed.lock();

        this.statusInfo.incrNumProcessed();
        this.lastProcessed.setCltuId(id);
        this.lastProcessed.setRadiationStartTime(null);

        CLTU_Status status = CLTU_Status.cltuST_invalid;
        CLTU_NotificationType nt = CLTU_NotificationType.cltuNT_invalid;

        if (reason == CLTU_Failure.cltuF_expired)
        {
            status = CLTU_Status.cltuST_expired;
            nt = CLTU_NotificationType.cltuNT_slduExpired;
        }
        else if (reason == CLTU_Failure.cltuF_interrupted)
        {
            status = CLTU_Status.cltuST_radiationNotStarted;
            nt = CLTU_NotificationType.cltuNT_productionInterrupted;
        }

        this.lastProcessed.setCltuStatus(status);
        this.statusInfo.setCltuBufferAvailable(bufferAvailable);

        this.statusInfo.unlock();
        this.lastProcessed.unlock();

        if (notify == true && nt != CLTU_NotificationType.cltuNT_invalid)
        {
            sendNotification(nt);
        }

        this.obj.unlock();
        throw new SleApiException(rc);
    }

    /**
     * See specification of ICLTU_SIUpdate.
     * 
     * @throws SleApiException
     */
    @Override
    public void productionStatusChange(CLTU_ProductionStatus newStatus, long bufferAvailable, boolean notify) throws SleApiException
    {
        this.obj.lock();

        CLTU_Status lps = this.lastProcessed.getCltuStatus();

        CLTU_ProductionStatus oldStatus = this.statusInfo.getProductionStatus();
        // ---------------- first perform consistency checks
        // ------------------------
        if (oldStatus == newStatus)
        {
            this.obj.unlock();
            throw new SleApiException(HRESULT.SLE_S_IGNORED, "SLE_S_IGNORED");
        }

        this.statusInfo.lock();

        this.statusInfo.setProductionStatus(newStatus);
        this.statusInfo.setCltuBufferAvailable(bufferAvailable);

        this.statusInfo.unlock();

        if (newStatus == CLTU_ProductionStatus.cltuPS_interrupted || newStatus == CLTU_ProductionStatus.cltuPS_halted)
        {
            if (this.lastProcessed.getCltuStatus() == CLTU_Status.cltuST_radiationStarted)
            {
                this.lastProcessed.setCltuStatus(CLTU_Status.cltuST_interrupted);
            }
        }

        CLTU_NotificationMode nm = this.config.getNotificationMode();

        if (notify == true)
        {
            switch (newStatus)
            {
            case cltuPS_operational:
                sendNotification(CLTU_NotificationType.cltuNT_productionOperational);
                break;
            case cltuPS_halted:
                sendNotification(CLTU_NotificationType.cltuNT_productionHalted);
                break;
            case cltuPS_interrupted:
                if (nm == CLTU_NotificationMode.cltuNM_immediate)
                {
                    sendNotification(CLTU_NotificationType.cltuNT_productionInterrupted);
                }
                if (nm == CLTU_NotificationMode.cltuNM_deferred && lps == CLTU_Status.cltuST_radiationStarted)
                {
                    sendNotification(CLTU_NotificationType.cltuNT_productionInterrupted);
                }
                break;
            default:
                break;
            }
        }
        this.obj.unlock();
    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public void bufferEmpty(boolean notify)
    {

        this.statusInfo.lock();
        this.statusInfo.setCltuBufferAvailable(this.config.getMaxBufferSize());
        this.statusInfo.unlock();

        if (notify)
        {
            this.obj.lock();
            sendNotification(CLTU_NotificationType.cltuNT_bufferEmpty);
            this.obj.unlock();
        }
    }

    /**
     * See specification of ICLTU_SIUpdate
     * 
     * @throws SleApiException
     */
    @Override
    public void eventProcCompleted(long id, CLTU_EventResult result, boolean notify)
    {
        HRESULT rc = HRESULT.S_OK;
        this.obj.lock();

        if (getSIState() == SLE_SIState.sleSIS_unbound)
        {
            rc = HRESULT.SLE_E_STATE;
        }

        if (notify == true && rc != HRESULT.SLE_E_STATE)
        {
            if (result == CLTU_EventResult.cltuER_completed)
            {
                sendNotification(CLTU_NotificationType.cltuNT_actionListCompleted, id, result);
            }
            else if (result == CLTU_EventResult.cltuER_notCompleted)
            {
                sendNotification(CLTU_NotificationType.cltuNT_actionListNotCompleted, id, result);
            }
            else if (result == CLTU_EventResult.cltuER_conditionFalse)
            {
                sendNotification(CLTU_NotificationType.cltuNT_eventConditionEvFalse, id, result);
            }
        }

        this.obj.unlock();
    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public void setUplinkStatus(CLTU_UplinkStatus status)
    {

        this.statusInfo.lock();
        this.statusInfo.setUplinkStatus(status);
        this.statusInfo.unlock();
    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public CLTU_ProductionStatus getProductionStatus()
    {
        this.statusInfo.lock();
        CLTU_ProductionStatus si = this.statusInfo.getProductionStatus();
        this.statusInfo.unlock();
        return si;

    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public long getCltuBufferAvailable()
    {
        this.statusInfo.lock();
        long bs = this.statusInfo.getCltuBufferAvailable();
        this.statusInfo.unlock();
        return bs;
    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public long getNumberOfCltusReceived()
    {
        this.statusInfo.lock();
        long num = this.statusInfo.getNumCltusReceived();
        this.statusInfo.unlock();
        return num;
    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public long getNumberOfCltusProcessed()
    {
        this.statusInfo.lock();
        long num = this.statusInfo.getNumCltusProcessed();
        this.statusInfo.unlock();
        return num;

    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public long getNumberOfCltusRadiated()
    {
        this.statusInfo.lock();
        long num = this.statusInfo.getNumCltusRadiated();
        this.statusInfo.unlock();
        return num;
    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public long getCltuLastProcessed()
    {
        this.lastProcessed.lock();
        long id = this.lastProcessed.getCltuId();
        this.lastProcessed.unlock();
        return id;
    }

    /**
     * See specification of ICLTU_SIUpdate
     */
    @Override
    public ISLE_Time getRadiationStartTime()
    {

        this.statusInfo.lock();
        this.lastProcessed.lock();

        ISLE_Time rst = null;

        if (this.statusInfo.getNumCltusProcessed() != 0)
        {
            rst = this.lastProcessed.getRadiationStartTime();
        }

        this.statusInfo.unlock();
        this.lastProcessed.unlock();
        return rst;

    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public CLTU_Status getCltuStatus()
    {

        this.statusInfo.lock();
        this.lastProcessed.lock();

        CLTU_Status st = CLTU_Status.cltuST_radiationNotStarted;

        if (this.statusInfo.getNumCltusProcessed() != 0)
        {
            st = this.lastProcessed.getCltuStatus();
        }

        this.statusInfo.unlock();
        this.lastProcessed.unlock();
        return st;
    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public long getCltuLastOk()
    {

        this.lastOK.lock();
        long id = this.lastOK.getCltuId();
        this.lastOK.unlock();
        return id;

    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public ISLE_Time getRadiationStopTime()
    {

        this.statusInfo.lock();
        this.lastOK.lock();

        ISLE_Time rst = null;

        if (this.statusInfo.getNumCltusRadiated() != 0)
        {
            rst = this.lastOK.getRadiationStopTime(); // performs AddRef()
        }

        this.statusInfo.unlock();
        this.lastOK.unlock();

        return rst;

    }

    /**
     * See specification of ICLTU_SIUpdate
     */
    @Override
    public CLTU_UplinkStatus getUplinkStatus()
    {

        this.statusInfo.lock();
        CLTU_UplinkStatus us = this.statusInfo.getUplinkStatus();
        this.statusInfo.unlock();
        return us;
    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public long getExpectedCltuId()
    {
        return this.statusInfo.getExpectedCltuId();
    }

    /**
     * See specification of ICLTU_SIUpdate.
     */
    @Override
    public long getExpectedEventInvocationId()
    {
        this.statusInfo.lock();
        long id = this.statusInfo.getExpectedEventInvId();
        this.statusInfo.unlock();
        return id;
    }

    @Override
    public <T extends ISLE_Operation> T createOperation(Class<T> iid, SLE_OpType optype) throws SleApiException
    {
        if (isConfigured() == false)
        {
            throw new SleApiException(HRESULT.SLE_E_STATE);
        }

        if (optype != SLE_OpType.sleOT_asyncNotify && optype != SLE_OpType.sleOT_peerAbort)
        {
            throw new SleApiException(HRESULT.SLE_E_TYPE);
        }

        ISLE_OperationFactory opf = getOpFactory();
        T ppv = null;
        if(optype == SLE_OpType.sleOT_scheduleStatusReport)
        {
        	ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_fwdCltu, getVersion(), getMinimumReportingCycle());
        }
        else
        {
        	ppv = opf.createOperation(iid, optype, SLE_ApplicationIdentifier.sleAI_fwdCltu, getVersion());
        }

        if (optype == SLE_OpType.sleOT_asyncNotify)
        {
            this.statusInfo.lock(); // SPR-1388 order of locks harmonized with
            // CltuStarted()
            this.lastProcessed.lock();
            this.lastOK.lock();

            ICLTU_AsyncNotify an = (ICLTU_AsyncNotify) (ppv);

            an.setNotificationType(CLTU_NotificationType.cltuNT_invalid);
            an.setEventThrownId(0);
            if (this.statusInfo.getNumCltusProcessed() > 0)
            {
                an.setCltuLastProcessed(this.lastProcessed.getCltuId());
            }
            ISLE_Time t = this.lastProcessed.getRadiationStartTime();
            if (t != null)
            {
                an.setRadiationStartTime(t);
            }
            an.setCltuStatus(this.lastProcessed.getCltuStatus());

            if (this.statusInfo.getNumCltusRadiated() > 0)
            {
                an.setCltuLastOk(this.lastOK.getCltuId());
            }
            t = this.lastOK.getRadiationStopTime();
            if (t != null)
            {
                an.setRadiationStopTime(t);
            }
            an.setProductionStatus(this.statusInfo.getProductionStatus());
            an.setUplinkStatus(this.statusInfo.getUplinkStatus());

            this.lastOK.unlock();
            this.lastProcessed.unlock();
            this.statusInfo.unlock();
        }

        // PEER-ABORT-specific set-up supported by base-class:
        setUpOperation(optype, ppv);
        return ppv;

    }

    /**
     * Returns a pointer to the configuration object
     */
    @Override
    protected EE_APISE_PConfiguration getConfiguration()
    {
        return this.config;
    }

    @Override
    protected HRESULT doConfigCompleted()
    {

        HRESULT rc = super.doConfigCompleted();
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        // check configuration parameters
        if (this.config.getBitLockRequired() == SLE_YesNo.sleYN_invalid)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid Bit Lock Required");
            rc = HRESULT.SLE_E_CONFIG;
        }
        if	(this.config.getMaxCltuLength() == 0)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid Max CLTU Length");
        	rc = HRESULT.SLE_E_CONFIG;            
        }
        if ( this.config.getModulationFrequency() == 0)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid Modulation Frequency");
            rc = HRESULT.SLE_E_CONFIG;        	
        }
        if ( this.config.getModulationIndex() == 0)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid Modulation Index");
            rc = HRESULT.SLE_E_CONFIG;        	
        }
        if ( this.config.getPlopInEffect() == CLTU_PlopInEffect.cltuPIE_invalid)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid Plop In Effect");
            rc = HRESULT.SLE_E_CONFIG;        	
        }
        if ( this.config.getRfAvailRequired() == SLE_YesNo.sleYN_invalid)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid Rf Availability Required");
            rc = HRESULT.SLE_E_CONFIG;        	
        }
        if ( this.config.getScToBitrateRatio() == 0)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid Sc To Bit Rate Ratio");
            rc = HRESULT.SLE_E_CONFIG;
        }
        if ( this.config.getMaxBufferSize() == 0)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid Max Buffer Size");
            rc = HRESULT.SLE_E_CONFIG;
        }
        if ( this.config.getNotificationMode() == CLTU_NotificationMode.cltuNM_invalid)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid CLTU Notification Mode");
            rc = HRESULT.SLE_E_CONFIG;
        }
        if ( this.statusInfo.getProductionStatus() == CLTU_ProductionStatus.cltuPS_invalid) // #SPR-1189
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid CLTU Production Status");
            rc = HRESULT.SLE_E_CONFIG;
        }
        if ( this.statusInfo.getUplinkStatus() == CLTU_UplinkStatus.cltuUS_invalid)
        {
            logRecord(SLE_LogMessageType.sleLM_alarm,
                    EE_LogMsg.EE_SE_LM_ConfigError.getCode(),
                    "Invalid CLTU Uplink Status");
            rc = HRESULT.SLE_E_CONFIG;
        }

        if (rc != HRESULT.S_OK)
        {
            return HRESULT.SLE_E_CONFIG;
        }
        // long bufferSize = config.getMaxBufferSize();
        // set-up according to section 3.1.3.1 of the CLTU supplement
        this.config.setDeliveryMode(SLE_DeliveryMode.sleDM_fwdOnline); // the
                                                                       // only
        // possible
        // value for
        // CLTU
        this.statusInfo.setCltuBufferAvailable(this.config.getMaxBufferSize());
        this.lastProcessed.setCltuId(0);
        this.lastProcessed.setRadiationStartTime(null);
        this.lastProcessed.setCltuStatus(CLTU_Status.cltuST_invalid);

        this.lastOK.setCltuId(0L);
        this.lastOK.setRadiationStopTime(null);

        this.statusInfo.setNumCltusReceived(0);
        this.statusInfo.setNumCltusProcessed(0);
        this.statusInfo.setNumCltusRadiated(0);

        this.statusInfo.setExpectedCltuId(0);
        this.statusInfo.setExpectedEventInvId(0);

        return HRESULT.S_OK;
    }

    @Override
    protected HRESULT doStatusReport()
    {

        ISLE_OperationFactory opf = getOpFactory();
        ICLTU_StatusReport sr;
        try
        {
            sr = opf.createOperation(ICLTU_StatusReport.class,
                                     SLE_OpType.sleOT_statusReport,
                                     SLE_ApplicationIdentifier.sleAI_fwdCltu,
                                     getVersion());
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return e.getHResult();
        }

        this.statusInfo.lock();
        this.lastProcessed.lock();
        this.lastOK.lock();

        this.lastProcessed.setUpReport(sr);
        this.lastOK.setUpReport(sr);
        this.statusInfo.setUpReport(sr);

        this.statusInfo.unlock();
        this.lastProcessed.unlock();
        this.lastOK.unlock();

        return initiatePxyOpInv(sr, false);

    }

    /**
     * Performs setting of the required parameter to the supplied GetParameter
     * operation.. When the value has been set, it passes the operation to the
     * interface ISLE_SrvProxyInitiate for transmission to the user.
     */
    @Override
    protected HRESULT doGetParameter(ISLE_Operation poperation)
    {

        ICLTU_GetParameter gp = (ICLTU_GetParameter) poperation;

        CLTU_ParameterName pname = gp.getRequestedParameter();

        HRESULT rc = HRESULT.SLE_E_UNKNOWN;

        if (pname == CLTU_ParameterName.cltuPN_reportingCycle)
        {
            gp.setReportingCycle(getReportingCycle());
            rc = HRESULT.S_OK;
        }
        else if (pname == CLTU_ParameterName.cltuPN_returnTimeoutPeriod)
        {
            gp.setReturnTimeoutPeriod(getReturnTimeout());
            rc = HRESULT.S_OK;
        }
        else
        {
            rc = this.config.setUpGetParameter(gp);
        }

        if (rc != HRESULT.S_OK)
        {
            this.statusInfo.lock();
            rc = this.statusInfo.setUpGetParameter(gp);
            this.statusInfo.unlock();
        }

        if (rc == HRESULT.S_OK)
        {
            gp.setPositiveResult();
        }
        else
        {
            gp.setGetParameterDiagnostic(CLTU_GetParameterDiagnostic.cltuGP_unknownParameter);
        }
        return initiatePxyOpRtn(gp, false);

    }

    /**
     * @FunctionResets the status information parameters to the initial values.@EndFunction
     *                 Implementation: The base-class is called first.
     */
    @Override
    protected void cleanup()
    {
        super.cleanup();
        this.statusInfo.lock();
        this.statusInfo.setCltuBufferAvailable(this.config.getMaxBufferSize());
        this.statusInfo.unlock();
    }

    /**
     * Starts processing of the operation invocation received from the
     * application
     */
    @Override
    protected HRESULT doInitiateOpInvoke(ISLE_Operation poperation)
    {

        HRESULT rc = super.doInitiateOpInvoke(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        SLE_OpType opType = poperation.getOperationType();

        switch (opType)
        {
        case sleOT_asyncNotify:
            return asyncNotifyInv(poperation);
        case sleOT_peerAbort:
            return peerAbortInv(poperation, SLE_AbortOriginator.sleAO_application);

        default:
            return HRESULT.SLE_E_ROLE;
        }

    }

    /**
     * Starts processing of the return-operation received from the application.
     */
    @Override
    protected HRESULT doInitiateOpReturn(ISLE_ConfirmedOperation poperation)
    {

        HRESULT rc = super.doInitiateOpReturn(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        SLE_OpType opType = poperation.getOperationType();
        try
        {
            switch (opType)
            {
            case sleOT_bind:
                return bindRtn(poperation);
            case sleOT_unbind:
                return unbindRtn(poperation);
            case sleOT_start:
                return startRtn(poperation);
            case sleOT_stop:
                return stopRtn(poperation);
            case sleOT_transferData:
                return transferDataRtn(poperation);
            case sleOT_throwEvent:
                return throwEventRtn(poperation);

            default:
                return HRESULT.SLE_E_ROLE;
            }
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        return HRESULT.SLE_E_ROLE;
    }

    /**
     * Starts processing of the operation invocation received from the proxy
     */
    @Override
    protected HRESULT doInformOpInvoke(ISLE_Operation poperation)
    {

        SLE_OpType opType = poperation.getOperationType();
        HRESULT rc = HRESULT.S_OK;

        // do not make checks for TRANSFER-DATA and THROW-EVENT,
        // these PDUs have special error-handling, see
        // transferDataInv() and throwEventInv()

        if (opType != SLE_OpType.sleOT_transferData && opType != SLE_OpType.sleOT_throwEvent)
        {
            rc = super.doInformOpInvoke(poperation);
            if (rc != HRESULT.S_OK)
            {
                return rc;
            }
        }

        switch (opType)
        {
        case sleOT_bind:
            return bindInv(poperation);
        case sleOT_unbind:
            return unbindInv(poperation);
        case sleOT_start:
            return startInv(poperation);
        case sleOT_stop:
            return stopInv(poperation);
        case sleOT_transferData:
            return transferDataInv(poperation);
        case sleOT_throwEvent:
            return throwEventInv(poperation);
        case sleOT_scheduleStatusReport:
            return scheduleStatusReportInv(poperation);
        case sleOT_getParameter:
            return getParameterInv(poperation);
        case sleOT_peerAbort:
            return peerAbortInv(poperation, SLE_AbortOriginator.sleAO_proxy);

        default:
            return HRESULT.SLE_E_ROLE;
        }

    }

    /**
     * Starts processing of the return-operation received from the proxy
     */
    @Override
    protected HRESULT doInformOpReturn(ISLE_ConfirmedOperation poperation)
    {

        HRESULT rc = super.doInformOpReturn(poperation);
        if (rc != HRESULT.S_OK)
        {
            return rc;
        }

        return HRESULT.SLE_E_ROLE;
    }

    /**
     * Performs all checks on the CLTU-TRANSFER-DATA operation supplied by the
     * Proxy. When the checks are completed successfully, state-processing is
     * initiated.
     */
    private HRESULT transferDataInv(ISLE_Operation poperation)
    {

        HRESULT rc = HRESULT.S_OK;
        rc = checkInformOpInvoke(poperation, false); // do not send return on
                                                     // failure

        ICLTU_TransferData td = (ICLTU_TransferData) poperation;

        final ISLE_Time ert = td.getEarliestRadTime();
        final ISLE_Time lrt = td.getLatestRadTime();

        if ((rc == HRESULT.S_OK) && (ert != null && lrt != null))
        {
            if (!(ert.compareTo(lrt) < 0))
            {
                td.setTransferDataDiagnostic(CLTU_TransferDataDiagnostic.cltuXFD_inconsistenceTimeRange);
                rc = HRESULT.E_FAIL;
            }
        }

        if (rc == HRESULT.S_OK)
        {
            long length = 0;
            final byte[] data = td.getData();
            length = data.length;
            if (length > this.config.getMaxCltuLength())
            {
                td.setTransferDataDiagnostic(CLTU_TransferDataDiagnostic.cltuXFD_cltuError);
                rc = HRESULT.E_FAIL;
            }
        }

        if (rc == HRESULT.S_OK)
        {
            td.setPositiveResult(); // according to SE-8.3
        }

        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_TransferDataInv, poperation);
    }

    /**
     * Performs all checks on the CLTU-TRANSFER-DATA return-PDU supplied by the
     * Application. When the checks are completed successfully, state-processing
     * is initiated. The function also obtains status-parameters from the
     * supplied operation object and copies them to the internal status
     * parameters
     */
    private HRESULT transferDataRtn(ISLE_ConfirmedOperation poperation)
    {

        ICLTU_TransferData td = (ICLTU_TransferData) poperation;
        this.statusInfo.lock();
        this.statusInfo.setExpectedCltuId(td.getExpectedCltuId());
        this.statusInfo.setCltuBufferAvailable(td.getCltuBufferAvailable());

        if (td.getResult() == SLE_Result.sleRES_positive)
        {
            this.statusInfo.incrNumReceived();
        }
        this.statusInfo.unlock();

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_TransferDataRtn, poperation);

    }

    /**
     * Performs all checks on the CLTU-THROW-EVENT operation supplied by the
     * Proxy. When the checks are completed successfully, state-processing is
     * initiated. If any error is encountered, the result is set to 'negative',
     * the appropriate diagnostic code is set and the PDU is forwarded to the
     * application after state-processing.
     */
    private HRESULT throwEventInv(ISLE_Operation poperation)
    {

        HRESULT rc = HRESULT.S_OK;
        rc = checkInformOpInvoke(poperation, false); // do not send return on
                                                     // failure

        ICLTU_ThrowEvent te = (ICLTU_ThrowEvent) poperation;

        if (rc == HRESULT.S_OK)
        {
            te.setPositiveResult(); // according to SE-8.3
        }

        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_ThrowEventInv, poperation);

    }

    /**
     * Performs all checks on the CLTU-THROW-EVENT return-PDU supplied by the
     * Application. When the checks are completed successfully, state-processing
     * is initiated. The function also obtains status-parameters from the
     * supplied operation object and copies them to the internal status
     * parameters
     */
    private HRESULT throwEventRtn(ISLE_ConfirmedOperation poperation)
    {

        ICLTU_ThrowEvent te = (ICLTU_ThrowEvent) poperation;

        this.statusInfo.lock();
        this.statusInfo.setExpectedEventInvId(te.getExpectedEventInvocationId());
        this.statusInfo.unlock();

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_ThrowEventRtn, poperation);
    }

    /**
     * @FunctionPerforms all checks on the CLTU-GET-PARAMETER operation supplied
     *                   by the Proxy. When the checks are completed
     *                   successfully, state-processing is initiated.@EndFunction
     */
    private HRESULT getParameterInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_GetPrmInv, poperation);
    }

    /**
     * @FunctionPerforms all checks on the CLTU-START operation supplied by the
     *                   Proxy. When the checks are completed successfully,
     *                   state-processing is initiated.@EndFunction
     */
    private HRESULT startInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StartInv, poperation);
    }

    /**
     * @throws SleApiException
     * @FunctionPerforms all checks on the CLTU-START return supplied by the
     *                   Appliaction. When the checks are completed
     *                   successfully, state-processing is initiated. Status
     *                   parameters are also updated by reading them from the
     *                   return-PDU.@EndFunction
     */
    private HRESULT startRtn(ISLE_ConfirmedOperation poperation) throws SleApiException
    {
        ICLTU_Start s = (ICLTU_Start) poperation;
        if (s.getResult() == SLE_Result.sleRES_positive)
        {
            if (s.getFirstCltuIdUsed() == true)
            {
                this.statusInfo.lock();
                this.statusInfo.setExpectedCltuId(s.getFirstCltuId());
                this.statusInfo.unlock();
            }
        }

        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StartRtn, poperation);

    }

    /**
     * Performs all checks on the CLTU-ASYNC-NOTIFY invocation supplied by the
     * application. When the checks are completed successfully, state-processing
     * is initiated.
     */
    private HRESULT asyncNotifyInv(ISLE_Operation poperation)
    {

        ICLTU_AsyncNotify an = (ICLTU_AsyncNotify) poperation;

        if (an.getNotificationType() == CLTU_NotificationType.cltuNT_bufferEmpty)
        {
            this.statusInfo.lock();
            this.statusInfo.setCltuBufferAvailable(this.config.getMaxBufferSize());
            this.statusInfo.unlock();
        }
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_AsyncNotifyInv, poperation);
    }

    /**
     * @FunctionPerforms all checks on the STOP operation supplied by the Proxy.
     *                   When the checks are completed successfully,
     *                   state-processing is initiated.@EndFunction
     */
    private HRESULT stopInv(ISLE_Operation poperation)
    {
        return doStateProcessing(SLE_Component.sleCP_proxy, EE_TI_SLESE_Event.eeSLESE_StopInv, poperation);

    }

    /**
     * Performs all checks on the STOP return supplied by the Appliaction. When
     * the checks are completed successfully, state-processing is initiated. The
     * function also updates the status-parameter 'cltu-buffer-available' from
     * the configuration parameter 'maximum-cltu-buffer'.
     */
    private HRESULT stopRtn(ISLE_ConfirmedOperation poperation)
    {

        ISLE_Stop s = (ISLE_Stop) poperation;

        if (s.getResult() == SLE_Result.sleRES_positive)
        {
            this.statusInfo.lock();
            this.statusInfo.setCltuBufferAvailable(this.config.getMaxBufferSize());
            this.statusInfo.unlock();
        }
        return doStateProcessing(SLE_Component.sleCP_application, EE_TI_SLESE_Event.eeSLESE_StopRtn, poperation);
    }

    /**
     * Creates and passes a notification with the supplied type to the proxy.
     */
    private void sendNotification(CLTU_NotificationType type)
    {

        SLE_SIState state = getSIState();
        if (state == SLE_SIState.sleSIS_unbound || state == SLE_SIState.sleSIS_bindPending)
        {
            return;
        }

        ICLTU_AsyncNotify an = null;
        try
        {
            an = createOperation(ICLTU_AsyncNotify.class, SLE_OpType.sleOT_asyncNotify);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        an.setNotificationType(type);
        
        try
        {
        	this.objMutex.lock();// SLEAPIJ-86
        	initiatePxyOpInv(an, false);
        }
        finally
        {
        	this.objMutex.unlock();// SLEAPIJ-86 Part II
        }
    }

    private void sendNotification(CLTU_NotificationType type, long eventInvId, CLTU_EventResult result)
    {
        if (type != CLTU_NotificationType.cltuNT_actionListCompleted
            && type != CLTU_NotificationType.cltuNT_actionListNotCompleted
            && type != CLTU_NotificationType.cltuNT_eventConditionEvFalse)
        {
            return;
        }

        SLE_SIState state = getSIState();
        if (state == SLE_SIState.sleSIS_unbound || state == SLE_SIState.sleSIS_bindPending)
        {
            return;
        }

        ICLTU_AsyncNotify an = null;
        try
        {
            an = createOperation(ICLTU_AsyncNotify.class, SLE_OpType.sleOT_asyncNotify);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }
        an.setNotificationType(type);
        an.setEventThrownId(eventInvId);
        try
        {
        	this.objMutex.lock();// SLEAPIJ-86        
        	initiatePxyOpInv(an, false);
        }
        finally
        {
        	this.objMutex.unlock();// SLEAPIJ-86 Part II
        }
    }

    @Override
    public void setAcquisitionSequenceLength(int length)
    {
        this.obj.lock();
        this.config.setAcquisitionSequenceLength(length);
        this.obj.unlock();
    }

    @Override
    public int getAcquisitionSequenceLength()
    {
        return this.config.getAcquisitionSequenceLength();
    }

    @Override
    public void setPlop1IdleSequenceLength(int length)
    {
        this.obj.lock();
        this.config.setPlop1IdleSequenceLength(length);
        this.obj.unlock();
    }

    @Override
    public int getPlop1IdleSequenceLength()
    {
        return this.config.getPlop1IdleSequenceLength();
    }

    @Override
    public void setProtocolAbortMode(CLTU_ProtocolAbortMode pam)
    {
        this.obj.lock();
        this.config.setProtocolAbortMode(pam);
        this.obj.unlock();
    }

    @Override
    public CLTU_ProtocolAbortMode getProtocolAbortMode()
    {
        return this.config.getProtocolAbortMode();
    }

    @Override
    public void setClcwGlobalVcid(CLTU_ClcwGvcId cgv)
    {
        this.obj.lock();
        this.config.setClcwGlobalVcid(cgv);
        this.obj.unlock();
    }

    @Override
    public CLTU_ClcwGvcId getClcwGlobalVcid()
    {
        return this.config.getClcwGlobalVcid();
    }

    @Override
    public void setClcwPhysicalChannel(CLTU_ClcwPhysicalChannel cgv)
    {
        this.config.setClcwPhysicalChannel(cgv);
    }

    @Override
    public CLTU_ClcwPhysicalChannel getClcwPhysicalChannel()
    {
        return this.config.getClcwPhysicalChannel();
    }

    @Override
    public void setMinimumDelayTime(long mdt)
    {
        this.obj.lock();
        this.config.setMinimumDelayTime(mdt);
        this.obj.unlock();
    }

    @Override
    public long getMinimumDelayTime()
    {
        return this.config.getMinimumDelayTime();
    }
    
    /**
     * Sets the CLTU config parameter minimum-reporting-cycle in seconds
     * SLES parameter ID 301
     * @since SLES V5
     */
    @Override
    public void setMinimumReportingCycle(long mrc)
    {
    	this.config.setMinimumReportingCycle(mrc);
    }
    
    /**
     * Gets the CLTU config parameter minimum-reporting-cycle in seconds
     * SLES parameter ID 301
     * @since SLES V5
     * @return minimum-reporting-cycle in seconds as type long
     */
    @Override
    public long getMinimumReportingCycle()
    {
    	return this.config.getMinimumReportingCycle();
    }
}
