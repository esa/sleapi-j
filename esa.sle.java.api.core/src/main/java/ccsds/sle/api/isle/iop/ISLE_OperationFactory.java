package ccsds.sle.api.isle.iop;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;

/**
 * The interface defines the factory method to create operations.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_OperationFactory extends IUnknown
{
    /**
     * Creates an operation
     * 
     * @param iid the interface identifier
     * @param opType the operation type
     * @param srvType the service type
     * @param version the version
     * @return the operation
     * @throws SleApiException
     */
    <T extends ISLE_Operation> T createOperation(Class<T> iid,
                                                 SLE_OpType opType,
                                                 SLE_ApplicationIdentifier srvType,
                                                 int version) throws SleApiException;
    
    /**
     * Creates an Schedule-Status-Report-Operation
     * 
     * This interface method has been extended to pass minRepCycle.
     * 
     * @param iid the interface identifier
     * @param opType the operation type
     * @param srvType the service type
     * @param version the version
     * @param minRepCycle the minimum reporting cycle new with SLE V5
     * @return the operation
     * @throws SleApiException
     */
    <T extends ISLE_Operation> T createOperation(Class<T> iid,
                                                 SLE_OpType opType,
                                                 SLE_ApplicationIdentifier srvType,
                                                 int version,
                                                 long minRepCycle) throws SleApiException;
}
