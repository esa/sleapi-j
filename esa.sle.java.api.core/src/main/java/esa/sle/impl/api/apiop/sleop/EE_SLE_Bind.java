/**
 * @(#) EE_SLE_Bind.java
 */

package esa.sle.impl.api.apiop.sleop;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_BindDiagnostic;
import ccsds.sle.api.isle.it.SLE_DiagnosticType;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.iutl.ISLE_SII;

/**
 * The class implements the BIND operation.
 */
public class EE_SLE_Bind extends IEE_SLE_ConfirmedOperation implements ISLE_Bind
{
    /**
     * The identifier for initiating applications
     */
    private String initiatorId;

    /**
     * The identifier for responding applications
     */
    private String responderId;

    /**
     * The responder port identifier
     */
    private String responderPortId;

    /**
     * The service instance identifier
     */
    private ISLE_SII sii;

    /**
     * The service type
     */
    private SLE_ApplicationIdentifier serviceType;

    /**
     * The version number
     */
    private int version;

    /**
     * The bind diagnostics
     */
    private SLE_BindDiagnostic diagnostic;


    /**
     * This constructor initializes the object according to the delivered
     * argument(s) and passes the argument(s) to the constructor of the
     * base-class.
     */
    public EE_SLE_Bind(SLE_ApplicationIdentifier opSrvType, int version, ISLE_Reporter preporter)
    {
        super(opSrvType, SLE_OpType.sleOT_bind, version, preporter);
        this.initiatorId = null;
        this.responderId = null;
        this.responderPortId = null;
        this.sii = null;
        this.serviceType = SLE_ApplicationIdentifier.sleAI_invalid;
        this.version = version;
        this.diagnostic = SLE_BindDiagnostic.sleBD_invalid;
    }

    /**
     * Copy constructor
     */
    protected EE_SLE_Bind(EE_SLE_Bind right)
    {
        super(right);

        if (right.initiatorId != null)
        {
            this.initiatorId = right.initiatorId;
        }

        if (right.responderId != null)
        {
            this.responderId = right.responderId;
        }

        if (right.responderPortId != null)
        {
            this.responderPortId = right.responderPortId;
        }

        if (right.sii != null)
        {
            this.sii = right.sii.copy();
        }

        this.serviceType = right.serviceType;
        this.version = right.version;
        this.diagnostic = right.diagnostic;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Operation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_ConfirmedOperation.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_Bind.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public synchronized String getInitiatorIdentifier()
    {
        return this.initiatorId;
    }

    @Override
    public synchronized String getResponderIdentifier()
    {
        return this.responderId;
    }

    @Override
    public synchronized String getResponderPortIdentifier()
    {
        return this.responderPortId;
    }

    @Override
    public synchronized ISLE_SII getServiceInstanceId()
    {
        return this.sii;
    }

    @Override
    public synchronized void setInitiatorIdentifier(String id)
    {
        this.initiatorId = id;
    }

    @Override
    public synchronized void setResponderIdentifier(String id)
    {
        this.responderId = id;
    }

    @Override
    public synchronized void setResponderPortIdentifier(String port)
    {
        this.responderPortId = port;
    }

    @Override
    public synchronized void setServiceInstanceId(ISLE_SII siid)
    {
        this.sii = siid.copy();
    }

    @Override
    public synchronized void putServiceInstanceId(ISLE_SII psiid)
    {
        this.sii = psiid;
    }

    @Override
    public synchronized SLE_ApplicationIdentifier getServiceType()
    {
        return this.serviceType;
    }

    @Override
    public synchronized int getVersionNumber()
    {
        return this.version;
    }

    @Override
    public synchronized void setServiceType(SLE_ApplicationIdentifier serviceType)
    {
        this.serviceType = serviceType;
    }

    @Override
    public synchronized void setVersionNumber(int version)
    {
        this.version = version;
    }

    @Override
    public synchronized SLE_BindDiagnostic getBindDiagnostic()
    {
        return this.diagnostic;
    }

    @Override
    public synchronized void setBindDiagnostic(SLE_BindDiagnostic diagnostic)
    {
        setSpecificDiagnostics();
        this.diagnostic = diagnostic;
    }

    /**
     * @throws SleApiException
     */
    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        super.verifyInvocationArguments();
        if (this.responderId == null || this.responderPortId == null || this.sii == null || this.version == 0
            || this.serviceType == SLE_ApplicationIdentifier.sleAI_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG, "Invalid Bind invocation arguments");
        }
    }

    /**
     * @throws SleApiException
     */
    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        super.verifyReturnArguments();

        if (this.diagnostic == SLE_BindDiagnostic.sleBD_invalid)
        {
            if (getResult() == SLE_Result.sleRES_negative
                && getDiagnosticType() == SLE_DiagnosticType.sleDT_specificDiagnostics)
            {
                throw new SleApiException(HRESULT.SLE_E_MISSINGARG, "Invalid Bind return arguments");
            }
        }
    }

    /**
	 * 
	 */
    @Override
    public synchronized ISLE_Operation copy()
    {
        return new EE_SLE_Bind(this);
    }

    /**
	 * 
	 */
    @Override
    public synchronized String print(int maxDumpLength)
    {
        StringBuilder os = new StringBuilder();
        printOn(os, maxDumpLength);

        os.append("Initiator Identifier   : " + this.initiatorId + "\n");
        os.append("Responder Identifier   : " + this.responderId + "\n");
        os.append("Rsp Port Identifier    : " + this.responderPortId + "\n");
        os.append("Service Instance Id    : ");

        if (this.sii != null)
        {
            String sii_c = this.sii.getAsciiForm();
            os.append(sii_c);
        }

        os.append("\n");
        os.append("Service Type           : " + this.serviceType.toString() + "\n");
        os.append("Version                : " + this.version + "\n");
        os.append("Bind Diagnostic        : " + this.diagnostic.toString() + "\n");

        return os.toString();
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.diagnostic == null) ? 0 : this.diagnostic.hashCode());
        result = prime * result + ((this.initiatorId == null) ? 0 : this.initiatorId.hashCode());
        result = prime * result + ((this.responderId == null) ? 0 : this.responderId.hashCode());
        result = prime * result + ((this.responderPortId == null) ? 0 : this.responderPortId.hashCode());
        result = prime * result + ((this.serviceType == null) ? 0 : this.serviceType.hashCode());
        result = prime * result + ((this.sii == null) ? 0 : this.sii.hashCode());
        result = prime * result + this.version;
        return result;
    }

    @Override
    public synchronized boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        EE_SLE_Bind other = (EE_SLE_Bind) obj;
        if (this.diagnostic != other.diagnostic)
        {
            return false;
        }
        if (this.initiatorId == null)
        {
            if (other.initiatorId != null)
            {
                return false;
            }
        }
        else if (!this.initiatorId.equals(other.initiatorId))
        {
            return false;
        }
        if (this.responderId == null)
        {
            if (other.responderId != null)
            {
                return false;
            }
        }
        else if (!this.responderId.equals(other.responderId))
        {
            return false;
        }
        if (this.responderPortId == null)
        {
            if (other.responderPortId != null)
            {
                return false;
            }
        }
        else if (!this.responderPortId.equals(other.responderPortId))
        {
            return false;
        }
        if (this.serviceType != other.serviceType)
        {
            return false;
        }
        if (this.sii == null)
        {
            if (other.sii != null)
            {
                return false;
            }
        }
        else if (!this.sii.equals(other.sii))
        {
            return false;
        }
        if (this.version != other.version)
        {
            return false;
        }
        return true;
    }

    @Override
    public synchronized String toString()
    {
        return "EE_SLE_Bind [initiatorId=" + ((this.initiatorId != null) ? this.initiatorId : "") + ", responderId="
               + ((this.responderId != null) ? this.responderId : "") + ", responderPortId="
               + ((this.responderPortId != null) ? this.responderPortId : "") + ", sii="
               + ((this.sii != null) ? this.sii : "") + ", serviceType=" + this.serviceType + ", version="
               + this.version + ", diagnostic=" + this.diagnostic + "]";
    }

}
