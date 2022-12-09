package esa.sle.service.loader.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iapl.ISLE_ServiceInform;
import ccsds.sle.api.isle.ise.ISLE_SIAdmin;
import ccsds.sle.api.isle.it.SLE_Alarm;
import ccsds.sle.api.isle.it.SLE_AppRole;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_YesNo;
import ccsds.sle.api.isrv.icltu.ICLTU_SIAdmin;
import ccsds.sle.api.isrv.icltu.types.CLTU_ProductionStatus;
import ccsds.sle.api.isrv.icltu.types.CLTU_UplinkStatus;
import ccsds.sle.api.isrv.ifsp.IFSP_FOPMonitor;
import ccsds.sle.api.isrv.ifsp.IFSP_SIAdmin;
import ccsds.sle.api.isrv.ifsp.types.FSP_AbsolutePriority;
import ccsds.sle.api.isrv.ifsp.types.FSP_FopState;
import ccsds.sle.api.isrv.ifsp.types.FSP_MuxScheme;
import ccsds.sle.api.isrv.ifsp.types.FSP_ProductionStatus;
import ccsds.sle.api.isrv.ifsp.types.FSP_TimeoutType;
import ccsds.sle.api.isrv.iraf.IRAF_SIAdmin;
import ccsds.sle.api.isrv.iraf.types.RAF_LockStatus;
import ccsds.sle.api.isrv.iraf.types.RAF_ProductionStatus;
import ccsds.sle.api.isrv.ircf.IRCF_SIAdmin;
import ccsds.sle.api.isrv.ircf.types.RCF_LockStatus;
import ccsds.sle.api.isrv.ircf.types.RCF_ProductionStatus;
import ccsds.sle.api.isrv.irocf.IROCF_SIAdmin;
import ccsds.sle.api.isrv.irocf.types.ROCF_LockStatus;
import ccsds.sle.api.isrv.irocf.types.ROCF_ProductionStatus;
import esa.sle.impl.api.apise.fspse.EE_APISE_FSP_Limits;
import esa.sle.service.loader.ISLE_LoadedServiceInstance;
import esa.sle.service.loader.ISLE_ServiceLoader;
import esa.sle.sicf.si.descriptors.CltuSIDescriptor;
import esa.sle.sicf.si.descriptors.FspSIDescriptor;
import esa.sle.sicf.si.descriptors.RafSIDescriptor;
import esa.sle.sicf.si.descriptors.RcfSIDescriptor;
import esa.sle.sicf.si.descriptors.RocfSIDescriptor;
import esa.sle.sicf.si.descriptors.SIDescriptor;

public class EE_SLE_LoadedServiceInstance implements ISLE_LoadedServiceInstance
{
    private static final Logger LOG = Logger.getLogger(EE_SLE_LoadedServiceInstance.class.getName());

	private static final long MAX_CLTU_BUFFER_SIZE = 4294967295L; //SLEAPIJ-102 sets the value to 2^32-1

    private final ISLE_ServiceLoader serviceLoader;

    private ISLE_ServiceInform inform;

    private SIDescriptor siDescriptor;

    private ISLE_SIAdmin serviceInstance;

    private boolean isBounded;

    private int version;

    private SLE_AppRole applRole;


    public EE_SLE_LoadedServiceInstance(SIDescriptor siDescriptor, ISLE_ServiceLoader serviceLoader)
    {
        this.siDescriptor = siDescriptor;
        this.serviceLoader = serviceLoader;
        this.inform = null;
        this.serviceInstance = null;
        this.isBounded = false;
        this.version = -1;
    }

    @Override
    public synchronized String getServiceInstanceIdentifier()
    {
        return this.siDescriptor.getServiceInstanceId().getAsciiForm();
    }

    @Override
    public synchronized ISLE_SIAdmin getServiceInstance()
    {
        return this.serviceInstance;
    }

    @Override
    public synchronized ISLE_ServiceLoader getServiceLoader()
    {
        return this.serviceLoader;
    }

    @Override
    public synchronized void register(ISLE_ServiceInform servInform, int version)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Registering a ISLE_ServiceInform to service instance "
                     + this.siDescriptor.getServiceInstanceId().getAsciiForm() + " with version " + version);
        }

        this.inform = servInform;
        this.version = version;

        try
        {
            // create the Service Instance
            createServiceInstance();
            this.isBounded = true;

            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info(Thread.currentThread().getId() + " Service Instance " + getServiceInstanceIdentifier()
                         + " created");
            }
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            if (LOG.isLoggable(Level.SEVERE))
            {
                LOG.log(Level.SEVERE, Thread.currentThread().getId() + " Error while creating/configuring the Service Instance "
                         + getServiceInstanceIdentifier() + ", " + e.getHResult() + " / " + e.getMessage(), e);
            }

            try
            {
				deregister(); // SLEAPIJ-59
			}
            catch (SleApiException ed)
            {
				LOG.log(Level.SEVERE, "Failed to destroy SI failing config completed", ed);			
			}
            
            this.serviceInstance = null;
            this.isBounded = false;
            this.inform = null;
            this.version = -1;
        }
    }

    @Override
    public synchronized void deregister() throws SleApiException
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Deregistering a ISLE_ServiceInform");
        }

        try
        {
            // destroy the associate Service Instance
            this.serviceLoader.getLibraryInstance().getSIFactory().destroyServiceInstance(this.serviceInstance);
            this.isBounded = false;
            this.serviceInstance = null;
            this.inform = null;
            this.version = -1;

            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info(Thread.currentThread().getId() + " Service Instance " + getServiceInstanceIdentifier()
                         + " destroyed");
            }
        }
        catch (SleApiException e)
        {
            LOG.log(Level.SEVERE, "SleApiException ", e);
            if (LOG.isLoggable(Level.SEVERE))
            {
                LOG.severe(Thread.currentThread().getId() + " Error while destroying Service Instance"
                         + getServiceInstanceIdentifier() + ", " + e.getHResult());
            }
            
            throw e;
        }
    }

    @Override
    public synchronized void updateDescriptor(SIDescriptor siDescriptor)
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info("Updating the descriptor");
        }

        if (this.isBounded)
        {
            // Error
            ISLE_Reporter reporter = this.serviceLoader.getLibraryInstance().getReporter();
            reporter.notify(SLE_Alarm.sleAL_invalid,
                            SLE_Component.sleCP_application,
                            this.siDescriptor.getServiceInstanceId(),
                            0,
                            "Tryng to update the descriptor of an already bounded Service Instance");
            return;
        }
        else
        {
            // set the new descriptor
            this.siDescriptor = siDescriptor;
        }
    }

    private void createServiceInstance() throws SleApiException
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Creating the Service Instance");
        }

        this.applRole = this.serviceLoader.getLibraryInstance().getApplRole();

        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Application Role " + this.applRole);
        }

        // set the application identifier
        SLE_ApplicationIdentifier aid = this.siDescriptor.getApplicationIdentifier();

        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Getting the SI Factory "
                     + this.serviceLoader.getLibraryInstance().getSIFactory());
        }

        // create the Service Instance
        this.serviceInstance = this.serviceLoader.getLibraryInstance().getSIFactory()
                .createServiceInstance(ISLE_SIAdmin.class, aid, this.version, this.applRole, this.inform);
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Service instance created " + this.serviceInstance);
        }

        this.serviceInstance.setServiceInstanceId(this.siDescriptor.getServiceInstanceId());

        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info(Thread.currentThread().getId() + " Service instance ID set " + this.serviceInstance);
        }

        // configure the new Service Instance
        configureServiceInstance();
    }

    private void configureServiceInstance() throws SleApiException
    {
        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info("Configuring the Service Instance " + this);
        }

        // set peer id
        if (this.applRole.equals(SLE_AppRole.sleAR_user))
        {
            this.serviceInstance.setPeerIdentifier(this.siDescriptor.getResponderId());
        }
        else
        {
            this.serviceInstance.setPeerIdentifier(this.siDescriptor.getInitiatorId());
        }

        // set provision period
        this.serviceInstance.setProvisionPeriod(this.siDescriptor.getProvisionPeriodStart(),
                                                this.siDescriptor.getProvisionPeriodStop());

        // set responder port identifier
        this.serviceInstance.setResponderPortIdentifier(this.siDescriptor.getResponderPortIdentifier());

        // set return timeout
        this.serviceInstance.setReturnTimeout((int) this.siDescriptor.getReturnTimeoutPeriod());

        // set specific service properties
        // RAF
        if (this.siDescriptor.getApplicationIdentifier() == SLE_ApplicationIdentifier.sleAI_rtnAllFrames)
        {
            if (this.applRole.equals(SLE_AppRole.sleAR_provider))
            {
                IRAF_SIAdmin rafSI = this.serviceInstance.queryInterface(IRAF_SIAdmin.class);
                RafSIDescriptor descriptor = (RafSIDescriptor) this.siDescriptor;
                rafSI.setDeliveryMode(descriptor.getDeliveryMode());
                rafSI.setLatencyLimit(descriptor.getLatencyLimit());
                rafSI.setTransferBufferSize(descriptor.getTransferBufferSize());
                rafSI.setPermittedFrameQuality(descriptor.getFrameQuality());
                rafSI.setInitialProductionStatus(RAF_ProductionStatus.getProductionStatusByCode(0));
                rafSI.setInitialFrameSyncLock(RAF_LockStatus.getLockStatusByCode(0));
                rafSI.setInitialCarrierDemodLock(RAF_LockStatus.getLockStatusByCode(0));
                rafSI.setInitialSubCarrierDemodLock(RAF_LockStatus.getLockStatusByCode(0));
                rafSI.setInitialSymbolSyncLock(RAF_LockStatus.getLockStatusByCode(0));
            }
        }

        // CLTU
        if (this.siDescriptor.getApplicationIdentifier() == SLE_ApplicationIdentifier.sleAI_fwdCltu)
        {
            if (this.applRole.equals(SLE_AppRole.sleAR_provider))
            {
                ICLTU_SIAdmin cltuSI = this.serviceInstance.queryInterface(ICLTU_SIAdmin.class);
                CltuSIDescriptor descriptor = (CltuSIDescriptor) this.siDescriptor;
                
                cltuSI.setBitLockRequired(descriptor.getBitLockRequired());                
                cltuSI.setMaximumSlduLength(descriptor.getMaximumClduLength());
                cltuSI.setModulationFrequency(descriptor.getModulationFrequency());
                cltuSI.setModulationIndex(descriptor.getModulationIndex());
                cltuSI.setPlopInEffect(descriptor.getPlopInEffect());
                cltuSI.setRfAvailableRequired(descriptor.getRfAvailableRequired());
                cltuSI.setSubcarrierToBitRateRatio(descriptor.getSubcarrierToBitRateRatio());
                cltuSI.setMaximumBufferSize(MAX_CLTU_BUFFER_SIZE); //SLEAPIJ-42 does not come from SICF, but we need a meaningful value to configure the SI
                cltuSI.setInitialProductionStatus(CLTU_ProductionStatus.getProductionStatusByCode(0));
                cltuSI.setInitialUplinkStatus(CLTU_UplinkStatus.getUplinkStatusByCode(0));
                cltuSI.setNotificationMode(descriptor.getNotificationMode()); // SLEAPI-J-41
                cltuSI.setBitLockRequired(descriptor.getBitLockRequired());  // SLEAPI-J-41
                cltuSI.setProtocolAbortMode(descriptor.getProtocolAbortMode()); // SLEAPI-J-41
                cltuSI.setMinimumDelayTime(descriptor.getMinimumCltuDelay());
                
            }
        }

        // RCF
        if (this.siDescriptor.getApplicationIdentifier() == SLE_ApplicationIdentifier.sleAI_rtnChFrames)
        {
            if (this.applRole.equals(SLE_AppRole.sleAR_provider))
            {
                IRCF_SIAdmin rcfSI = this.serviceInstance.queryInterface(IRCF_SIAdmin.class);
                RcfSIDescriptor descriptor = (RcfSIDescriptor) this.siDescriptor;
                rcfSI.setDeliveryMode(descriptor.getDeliveryMode());
                rcfSI.setInitialCarrierDemodLock(RCF_LockStatus.getLockStatusByCode(0));
                rcfSI.setInitialFrameSyncLock(RCF_LockStatus.getLockStatusByCode(0));
                rcfSI.setInitialProductionStatus(RCF_ProductionStatus.getProductionStatusByCode(0));
                rcfSI.setInitialSubCarrierDemodLock(RCF_LockStatus.getLockStatusByCode(0));
                rcfSI.setInitialSymbolSyncLock(RCF_LockStatus.getLockStatusByCode(0));
                rcfSI.setLatencyLimit(descriptor.getLatencyLimit());
                rcfSI.setPermittedGvcidSet(descriptor.getPermittedGvcidSet());
                rcfSI.setTransferBufferSize(descriptor.getTransferBufferSize());
            }
        }

        // FSP
        if (this.siDescriptor.getApplicationIdentifier() == SLE_ApplicationIdentifier.sleAI_fwdTcSpacePkt)
        {
            if (this.applRole.equals(SLE_AppRole.sleAR_provider))
            {
                IFSP_SIAdmin fspSI = this.serviceInstance.queryInterface(IFSP_SIAdmin.class);
                FspSIDescriptor descriptor = (FspSIDescriptor) this.siDescriptor;
                fspSI.setApIdList(descriptor.getPermittedApis());
                fspSI.setBitLockRequired(descriptor.getBitLockRequired());
                fspSI.setBlockingTimeout(descriptor.getBlockingTimeoutPeriod());
                fspSI.setBlockingUsage(descriptor.getBlockingUsage());
                fspSI.setDirectiveInvocationEnabled(descriptor.getDirectiveInvocationEnabled());
                fspSI.setInitialDirectiveInvocationOnline(SLE_YesNo.sleYN_invalid);
                fspSI.setInitialProductionStatus(FSP_ProductionStatus.getProductionStatusByCode(0));
                fspSI.setMapList(descriptor.getPermittedMaps());
                fspSI.setMaximumBufferSize(Long.MAX_VALUE); // SLEAPIJ-77
                fspSI.setMaximumFrameLength(EE_APISE_FSP_Limits.getMaxFrameLength()); // SLEAPIJ-77 use a meaningful default - this value is not part of the SICF
                fspSI.setMaximumPacketLength(EE_APISE_FSP_Limits.getMaxPacketLength()); // SLEAPIJ-77 use a meaningful default - this value is not part of the SICF
                fspSI.setPermittedTransmissionMode(descriptor.getPermittedTransmissionMode());
                fspSI.setRfAvailableRequired(descriptor.getRfAvailableRequired());
                fspSI.setSegmentHeaderPresent(descriptor.getSegmentHeader());
                fspSI.setVcMuxScheme(descriptor.getMapMultiplexingScheme());
                
                // SLEAPIJ-77 - The complete dummy initialisation of the FOP monitor happens here tpo pass configCompleted
                // However, a 
                IFSP_FOPMonitor fopMonitor = this.serviceInstance.queryInterface(IFSP_FOPMonitor.class);
                fopMonitor.setFopSlidingWindow(EE_APISE_FSP_Limits.getMaxFopSlidingWindow()); // SLEAPIJ-77 use a meaningful default - this value is not part of the SICF
                fopMonitor.setTimeoutType(FSP_TimeoutType.fspTT_generateAlert);
                fopMonitor.setTimerInitial(Long.MAX_VALUE);
                fopMonitor.setTransmissionLimit(EE_APISE_FSP_Limits.getMaxTransmissionLimit());
                fopMonitor.setTransmitterFrameSequenceNumber(EE_APISE_FSP_Limits.getMinTransmissionLimit());
                fopMonitor.setFopState(FSP_FopState.fspFS_initial);
                fopMonitor.setMapMuxScheme(FSP_MuxScheme.fspMS_absolutePriority);
                
                FSP_AbsolutePriority[] mapPriorityList = {new FSP_AbsolutePriority(0, 1)};
                fopMonitor.setMapPriorityList(mapPriorityList);
                // fspSI.setVcPollingVector(); TBC
                // fspSI.setVcPriorityList(); TBC
            }
        }

        // ROCF
        if (this.siDescriptor.getApplicationIdentifier() == SLE_ApplicationIdentifier.sleAI_rtnChOcf)
        {
            if (this.applRole.equals(SLE_AppRole.sleAR_provider))
            {
                IROCF_SIAdmin rocfSI = this.serviceInstance.queryInterface(IROCF_SIAdmin.class);
                RocfSIDescriptor descriptor = (RocfSIDescriptor) this.siDescriptor;
                rocfSI.setDeliveryMode(descriptor.getDeliveryMode());
                rocfSI.setInitialCarrierDemodLock(ROCF_LockStatus.getLockStatusByCode(0));
                rocfSI.setInitialFrameSyncLock(ROCF_LockStatus.getLockStatusByCode(0));
                rocfSI.setInitialProductionStatus(ROCF_ProductionStatus.getProductionStatusByCode(0));
                rocfSI.setInitialSymbolSyncLock(ROCF_LockStatus.getLockStatusByCode(0));
                rocfSI.setLatencyLimit(descriptor.getLatencyLimit());
                rocfSI.setPermittedControlWordTypeSet(descriptor.getPermittedControlWordTypeSet());
                rocfSI.setPermittedGvcidSet(descriptor.getPermittedGvcidSet());
                rocfSI.setPermittedTcVcidSet(descriptor.getPermittedTcVcidSet());
                rocfSI.setPermittedUpdateModeSet(descriptor.getPermittedUpdateModeSet());
                rocfSI.setTransferBufferSize(descriptor.getTransferBufferSize());
                rocfSI.setInitialSubCarrierDemodLock(ROCF_LockStatus.getLockStatusByCode(0)); // SLEAPIJ-76
            }
        }

        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info("Configuration of the Service Instance " + this + " about to be completed");
        }

        this.serviceInstance.configCompleted();

        if (LOG.isLoggable(Level.INFO))
        {
            LOG.info("Configuration of the Service Instance " + this + " completed");
        }
    }
}
