/**
 * @(#) IEE_SLE_ConfirmedOperation.java
 */

package esa.sle.impl.api.apiop.sleop;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Reporter;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_DiagnosticType;
import ccsds.sle.api.isle.it.SLE_Diagnostics;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_Result;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;

/**
 * /////////////////////////////////////////////////////// The class
 * EE_SLE_ConfirmedOperation implements the interface ISLE_ConfirmedOperation
 * exported by the component class 'Confirmed Operation' defined in reference
 * [SLE-API]. It provides functionality common to all confirmed operation
 * objects.@EndResponsibility Usage for derived classes: The most derived class
 * implements the Copy() function, which performs a deep copy of the operation
 * object. The implementation uses the protected copy-constructor of the
 * base-class. The copy-constructor is not seen on the class diagram, because
 * they are generated automatically. Furthermore the derived class has to
 * implement the method VerifyReturnArguments(). The Print() function shall be
 * implemented by derived classes such that they first call the protected
 * printOn() function of IEE_SLE_ConfirmedOperation. The VerifyReturnArguments()
 * has to be implemented such that the base-class is called first. Note that the
 * derived class has to implement only the methods inherited by the interface it
 * implements directly. The declaration and implementation of the base-interface
 * methods is supported via the macros defined in EE_M_SLEOP.h to save
 * repetitive work. ///////////////////////////////////////////////////////
 */
public abstract class IEE_SLE_ConfirmedOperation extends IEE_SLE_Operation implements ISLE_ConfirmedOperation
{
    /**
     * The result of the operation.
     */
    private SLE_Result operationResult;

    /**
     * The type of diagnostic (general, specific, none).
     */
    private SLE_DiagnosticType diagnosticType;

    /**
     * The common diagnostics.
     */
    private SLE_Diagnostics commonDiagnostics;

    /**
     * The invocation identifier of the operation.
     */
    private int invokeId = 0;

    /**
     * The pointer to the performer credentials. If no performer credentials are
     * used, the pointer equals 0.
     */
    private ISLE_Credentials performerCredentials;


    /**
     * This protected constructor initialises the object according to the
     * delivered argument(s).
     * 
     * @param srvType
     * @param opType
     * @param version
     * @param preporter
     */
    protected IEE_SLE_ConfirmedOperation(SLE_ApplicationIdentifier srvType,
                                         SLE_OpType opType,
                                         int version,
                                         ISLE_Reporter preporter)
    {
        super(srvType, opType, version, true, preporter);

        this.operationResult = SLE_Result.sleRES_invalid;
        this.diagnosticType = SLE_DiagnosticType.sleDT_noDiagnostics;
        this.commonDiagnostics = SLE_Diagnostics.sleD_invalid;
        this.invokeId = 0;
        this.performerCredentials = null;
    }

    /**
     * Copy Constructor
     * 
     * @param right
     */
    protected IEE_SLE_ConfirmedOperation(IEE_SLE_ConfirmedOperation right)
    {
        super(right);

        this.operationResult = right.operationResult;
        this.diagnosticType = right.diagnosticType;
        this.commonDiagnostics = right.commonDiagnostics;
        this.invokeId = right.invokeId;

        if (right.performerCredentials != null)
        {
            this.performerCredentials = right.performerCredentials.copy();
        }
    }

    /**
	 * 
	 */
    @Override
    public synchronized SLE_Result getResult()
    {
        return this.operationResult;
    }

    /**
	 * 
	 */
    @Override
    public synchronized SLE_DiagnosticType getDiagnosticType()
    {
        return this.diagnosticType;
    }

    /**
	 * 
	 */
    @Override
    public synchronized SLE_Diagnostics getDiagnostics()
    {
        return this.commonDiagnostics;
    }

    /**
	 * 
	 */
    @Override
    public synchronized int getInvokeId()
    {
        return this.invokeId;
    }

    /**
	 * 
	 */
    @Override
    public synchronized ISLE_Credentials getPerformerCredentials()
    {
        return this.performerCredentials;
    }

    /**
	 * 
	 */
    @Override
    public synchronized void setPositiveResult()
    {
        this.operationResult = SLE_Result.sleRES_positive;
        this.diagnosticType = SLE_DiagnosticType.sleDT_noDiagnostics;
    }

    /**
	 * 
	 */
    @Override
    public synchronized void setDiagnostics(SLE_Diagnostics diagnostic)
    {
        this.commonDiagnostics = diagnostic;
        this.diagnosticType = SLE_DiagnosticType.sleDT_commonDiagnostics;
        this.operationResult = SLE_Result.sleRES_negative;
    }

    /**
	 * 
	 */
    @Override
    public synchronized void setPerformerCredentials(ISLE_Credentials credentials)
    {
        this.performerCredentials = credentials.copy();
    }

    /**
	 * 
	 */
    @Override
    public synchronized void setInvokeId(int id)
    {
        this.invokeId = id;
    }

    /**
	 * 
	 */
    @Override
    public synchronized void putPerformerCredentials(ISLE_Credentials pcredentials)
    {
        this.performerCredentials = pcredentials;
    }

    /**
     * @throws SleApiException
     */
    @Override
    public synchronized void verifyInvocationArguments() throws SleApiException
    {
        // no specific verifications are performed
        super.verifyInvocationArguments();
    }

    /**
     * @throws SleApiException
     */
    @Override
    public synchronized void verifyReturnArguments() throws SleApiException
    {
        if (this.operationResult == SLE_Result.sleRES_invalid)
        {
            throw new SleApiException(HRESULT.SLE_E_MISSINGARG);
        }
        else if (this.operationResult == SLE_Result.sleRES_negative)
        {
            if (this.diagnosticType == SLE_DiagnosticType.sleDT_noDiagnostics)
            {
                throw new SleApiException(HRESULT.SLE_E_DIAGNOSTIC);
            }
        }

        if (this.diagnosticType == SLE_DiagnosticType.sleDT_commonDiagnostics)
        {
            if (this.commonDiagnostics == SLE_Diagnostics.sleD_invalid)
            {
                throw new SleApiException(HRESULT.SLE_E_DIAGNOSTIC);
            }
        }
    }

    /**
     * @param os
     * @param maxDumpLength
     */
    @Override
    protected synchronized void printOn(StringBuilder os, int maxDumpLength)
    {
        super.printOn(os, maxDumpLength);

        os.append("Operation Result       : " + this.operationResult.toString() + "\n");
        os.append("Diagnostic Type        : " + this.diagnosticType.toString() + "\n");
        os.append("Common Diagnostics     : " + this.commonDiagnostics.toString() + "\n");
        os.append("Invocation Identifier  : " + this.invokeId + "\n");
        os.append("Performer Credentials  : ");

        if (this.performerCredentials != null)
        {
            os.append(this.performerCredentials.dump() + "\n");
        }

        os.append("\n");
    }

    protected synchronized void setSpecificDiagnostics()
    {
        this.diagnosticType = SLE_DiagnosticType.sleDT_specificDiagnostics;
        this.operationResult = SLE_Result.sleRES_negative;
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.commonDiagnostics == null) ? 0 : this.commonDiagnostics.hashCode());
        result = prime * result + ((this.diagnosticType == null) ? 0 : this.diagnosticType.hashCode());
        result = prime * result + this.invokeId;
        result = prime * result + ((this.operationResult == null) ? 0 : this.operationResult.hashCode());
        result = prime * result + ((this.performerCredentials == null) ? 0 : this.performerCredentials.hashCode());
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
        IEE_SLE_ConfirmedOperation other = (IEE_SLE_ConfirmedOperation) obj;
        if (this.commonDiagnostics != other.commonDiagnostics)
        {
            return false;
        }
        if (this.diagnosticType != other.diagnosticType)
        {
            return false;
        }
        if (this.invokeId != other.invokeId)
        {
            return false;
        }
        if (this.operationResult != other.operationResult)
        {
            return false;
        }
        if (this.performerCredentials == null)
        {
            if (other.performerCredentials != null)
            {
                return false;
            }
        }
        else if (!this.performerCredentials.equals(other.performerCredentials))
        {
            return false;
        }
        return true;
    }

    @Override
    public synchronized String toString()
    {
        return "IEE_SLE_ConfirmedOperation [operationResult=" + this.operationResult + ", diagnosticType="
               + this.diagnosticType + ", commonDiagnostics=" + this.commonDiagnostics + ", invokeId=" + this.invokeId
               + ", performerCredentials=" + ((this.performerCredentials != null) ? this.performerCredentials : "")
               + "]";
    }

}
