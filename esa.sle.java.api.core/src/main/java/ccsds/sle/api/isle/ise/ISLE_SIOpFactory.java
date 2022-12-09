package ccsds.sle.api.isle.ise;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_OpType;

/**
 * The interface defines a specialized operation object factory provided by
 * service instances in the API Service Element. The interface is able to create
 * operation objects for a given service type and service role. Operation
 * objects created via this interface are "pre-configured" using the data
 * specified for the service instance (see the description of the operation
 * object interfaces for details). Operation objects for common association
 * management (see section 6.5.4) are created by all service instances.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_SIOpFactory extends IUnknown
{
    /**
     * Creates Operation
     * 
     * @param iid
     * @param optype
     * @return
     * @throws SleApiException
     */
    <T extends ISLE_Operation> T createOperation(Class<T> iid, SLE_OpType optype) throws SleApiException;
}
