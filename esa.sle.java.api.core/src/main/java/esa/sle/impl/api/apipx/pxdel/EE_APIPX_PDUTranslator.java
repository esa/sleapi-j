/**
 * @(#) EE_APIPX_PDUTranslator.java
 */

package esa.sle.impl.api.apipx.pxdel;

import java.io.IOException;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iapl.ISLE_Trace;
import ccsds.sle.api.isle.icc.ISLE_TraceControl;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_ConfirmedOperation;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_Unbind;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_Component;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_TraceLevel;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import ccsds.sle.transfer.service.common.types.InvokeId;
import esa.sle.impl.ifs.gen.EE_MessageRepository;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * The class provides the means to encode and decode PDU's, and manages the list
 * of pending return used to check the invoke-return operations. The class
 * delegates the ASN.1 encoding and decoding to the Asn1Translator. The class
 * has a map of pending return for all the confirmed operations, except for the
 * bind and unbind operations. This is because the bind and unbind operations
 * don't have any invoke id. The mutex prevents concurrent access to the map of
 * pending return.
 */
public class EE_APIPX_PDUTranslator implements ISLE_TraceControl
{
    // private static final Logger LOG =
    // Logger.getLogger(EE_APIPX_PDUTranslator.class.getName());

    /**
     * Pointer to the ISLE_Trace interface.
     */
    private ISLE_Trace trace;

    /**
     * Indicates if the traces had been started for the association.
     */
    private boolean traceStarted;

    /**
     * Trace level.
     */
    @SuppressWarnings("unused")
    private SLE_TraceLevel traceLevel;

    private ISLE_Unbind pendingUnbind;

    public EE_APIPX_Asn1Translator asn1Translator;

    private final TreeMap<Integer, ISLE_ConfirmedOperation> pendingReturn;

    private ISLE_Bind pendingBind;

    private final ReentrantLock accessPendingReturn;


    /**
     * Constructor of the class which takes the operation and utility factory as
     * parameter.
     */
    public EE_APIPX_PDUTranslator(ISLE_OperationFactory popFactory, ISLE_UtilFactory pUtilFactory)
    {
        this.asn1Translator = new EE_APIPX_Asn1Translator(popFactory, pUtilFactory, this);
        this.pendingBind = null;
        this.pendingUnbind = null;
        this.trace = null;
        this.traceStarted = false;
        this.traceLevel = SLE_TraceLevel.sleTL_low;
        this.pendingReturn = new TreeMap<Integer, ISLE_ConfirmedOperation>();
        this.accessPendingReturn = new ReentrantLock();
    }

    /**
     * Encodes the operation. If it is a confirmed invoke operation, records the
     * operation with its invoke identifier in the list of pending return. The
     * allocated encoded pdu will be deleted when it is sent on the IP network
     * (initiating association) or when it is sent over the IPC link (responding
     * association). S_OK The operation has been encoded. E_FAIL Unable to
     * encode the operation.
     * 
     * @throws IOException
     * @throws SleApiException
     */
    public byte[] encode(ISLE_Operation pOperation, boolean isInvoke) throws SleApiException, IOException
    {
        byte[] encodedPdu = null;

        encodedPdu = this.asn1Translator.encode(pOperation, isInvoke);

        if (encodedPdu != null && pOperation != null && pOperation.isConfirmed() && isInvoke)
        {
            try
            {
                // insert the pending return
                insertPendingReturn(pOperation);
            }
            catch (SleApiException e)
            {
                if (this.traceStarted && this.trace != null)
                {
                    // trace
                    String p1 = "Cannot encode the pdu";
                    String p2 = "Cannot insert the pending return";
                    String mess = EE_MessageRepository.getMessage(1004, p1, p2);
                    this.trace.traceRecord(SLE_TraceLevel.sleTL_low, SLE_Component.sleCP_proxy, null, mess);
                }

                throw new SleApiException(e.getHResult());
            }
        }

        return encodedPdu;
    }

    /**
     * Encodes the PEER-ABORT operation thanks to the Asn1Translator. S_OK The
     * operation has been encoded. E_FAIL Unable to encode the operation.
     * 
     * @throws SleApiException
     */
    public int encode(ISLE_Operation pOperation) throws SleApiException
    {
        return this.asn1Translator.encode(pOperation);
    }

    /**
     * Decodes the PDU, and instantiates a new operation. If it is a confirmed
     * operation, checks if a pending return exist, deletes it, and sets the
     * isInvoke parameter. The encoded pdu given as parameter is not deleted
     * (this is the responsibility of the caller). S_OK The pdu has been decoded
     * and a new operation instantiated. E_PENDING The pending return for the
     * decoded return operation cannot be founded. SLE_E_INVALIDPDU The decoded
     * operation is not the expected one (the pdu number sets by the decode is
     * not the expected one). E_FAIL Unable to decode the pdu.
     * 
     * @throws SleApiException
     * @throws IOException
     */
    public ISLE_Operation decode(byte[] encodedPdu,
                                 SLE_ApplicationIdentifier serviceType,
                                 EE_Reference<Boolean> isInvoked) throws SleApiException
    {
    	try
    	{
    		return this.asn1Translator.decode(encodedPdu, serviceType, isInvoked); // SLEAPIJ-67 IOException happens for decoding error. Translate to SleApiException
    	}
    	catch(IOException ioe)
    	{
    		throw new SleApiException(HRESULT.SLE_E_INVALIDPDU);
    	}
    }

    /**
     * Decodes the PEER-ABORT pdu and instantiates a new operation thanks to the
     * Asn1Translator. S_OK The pdu has been decoded and a new operation
     * Instantiated. E_PENDING The pending return for the decoded return
     * operation cannot be founded. E_FAIL Unable to decode the pdu.
     * 
     * @throws SleApiException
     */
    public ISLE_Operation decode(int encodedDiag,
                                 SLE_AbortOriginator peerabortOriginator,
                                 SLE_ApplicationIdentifier serviceType) throws SleApiException
    {
        return this.asn1Translator.decode(encodedDiag, peerabortOriginator, serviceType);
    }

    /**
     * Removes all the pending returns.
     */
    public void removeAllPendingReturns()
    {
        if (this.pendingBind != null)
        {
            this.pendingBind = null;
        }

        if (this.pendingUnbind != null)
        {
            this.pendingUnbind = null;
        }

        // remove all pending return from map
        this.accessPendingReturn.lock();
        this.pendingReturn.clear();
        this.accessPendingReturn.unlock();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == ISLE_TraceControl.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void startTrace(ISLE_Trace trace, SLE_TraceLevel level, boolean forward)
    {
        this.traceStarted = true;
        this.traceLevel = level;
        this.trace = trace;
    }

    @Override
    public void stopTrace() throws SleApiException
    {
        this.traceStarted = false;
    }

    /**
     * Checks if a pending return exists for the invoke id given as parameter.
     * If the pending return exists, it is removed, and the return operation is
     * returned. S_OK There was a pending return for the invoke id, and it has
     * been deleted. E_PENDING The pending return for the invoke id cannot be
     * found.
     * 
     * @throws SleApiException
     */
    public ISLE_Operation getReturnOp(InvokeId invokeid, SLE_OpType opType) throws SleApiException
    {
        ISLE_ConfirmedOperation pConfirmedOperation = null;
        ISLE_Operation pOperation = null;

        Integer index = new Integer(invokeid.value.intValue());

        this.accessPendingReturn.lock();
        ISLE_ConfirmedOperation mi = this.pendingReturn.get(index);
        if (mi == null)
        {
            // no pending return
        	this.accessPendingReturn.unlock(); // SLEAPIJ-28
            throw new SleApiException(HRESULT.E_PENDING, "No pending return");
        }
        else
        {
            // take the operation in the map
            pConfirmedOperation = mi;

            // remove the pending return
            this.pendingReturn.remove(index);

            if (pConfirmedOperation.getOperationType() == opType)
            {
                pOperation = pConfirmedOperation.queryInterface(ISLE_Operation.class);
            }
            else
            {
                // the invoke and return operation are not of the same type!!
            	this.accessPendingReturn.unlock(); // SLEAPIJ-28
                throw new SleApiException(HRESULT.E_PENDING, "The invoke and return operation are not of the same type");
            }
        }
        this.accessPendingReturn.unlock();
        return pOperation;
    }

    /**
     * Checks if a pending bind exists. If the pending return exists, it is
     * removed, and the bind operation is returned. S_OK There was a pending
     * bind, and it has been deleted. E_PENDING The pending bind cannot be
     * found.
     * 
     * @throws SleApiException
     */
    public ISLE_Bind getBindReturnOp() throws SleApiException
    {
        ISLE_Bind pBindOperation = null;

        if (this.pendingBind != null)
        {
            pBindOperation = this.pendingBind;
            this.pendingBind = null;
        }
        else
        {
            // no pending return
            throw new SleApiException(HRESULT.E_PENDING, "No pending return");
        }

        return pBindOperation;
    }

    /**
     * Checks if a pending unbind exists. If the pending unbind exists, it is
     * removed, and the unbind operation is returned. S_OK There was a pending
     * unbind, and it has been deleted. E_PENDING The pending unbind cannot be
     * found.
     * 
     * @throws SleApiException
     */
    public ISLE_Unbind getUnbindReturnOp() throws SleApiException
    {
        ISLE_Unbind pUnbindOperation = null;

        if (this.pendingUnbind != null)
        {
            pUnbindOperation = this.pendingUnbind;
            this.pendingUnbind = null;
        }
        else
        {
            // no pending return
            throw new SleApiException(HRESULT.E_PENDING, "No pending return");
        }
        return pUnbindOperation;
    }

    /**
     * Inserts a confirmed operation in the list of pending return. S_OK The
     * confirmed operation has been inserted. E_FAIL Unable to insert the
     * confirmed operation.
     * 
     * @throws SleApiException
     */
    private void insertPendingReturn(ISLE_Operation pOperation) throws SleApiException
    {
        switch (pOperation.getOperationType())
        {
        case sleOT_bind:
        {
            if (this.pendingBind != null)
            {
                // already a pending return for a bind op
                throw new SleApiException(HRESULT.E_FAIL, "Already a pending return for a bind operation");
            }
            else
            {
                ISLE_Bind pBind = null;
                pBind = pOperation.queryInterface(ISLE_Bind.class);
                if (pBind != null)
                {
                    this.pendingBind = pBind;
                }
                else
                {
                    // unable to get the bind interface
                    throw new SleApiException(HRESULT.E_FAIL, "Unable to get the bind interface");
                }
            }

            break;
        }
        case sleOT_unbind:
        {
            if (this.pendingUnbind != null)
            {
                // already a pending return for an unbind op
                throw new SleApiException(HRESULT.E_FAIL, "Already a pending return for an unbind operation");
            }
            else
            {
                ISLE_Unbind pUnbind = null;
                pUnbind = pOperation.queryInterface(ISLE_Unbind.class);
                if (pUnbind != null)
                {
                    this.pendingUnbind = pUnbind;
                }
                else
                {
                    // unable to get the unbind interface
                    throw new SleApiException(HRESULT.E_FAIL, "Unable to get the unbind interface");
                }
            }

            break;
        }
        default:
        {
            ISLE_ConfirmedOperation pConfOp = null;
            pConfOp = pOperation.queryInterface(ISLE_ConfirmedOperation.class);
            if (pConfOp != null)
            {
                int invokeId = pConfOp.getInvokeId();
                this.accessPendingReturn.lock();
                // check if the invoke id is already in the map
                if (this.pendingReturn.get(new Integer(invokeId)) != null)
                {
                    // already a pending return with this invoke id
                	this.accessPendingReturn.unlock(); // SLEAPIJ-28
                    throw new SleApiException(HRESULT.E_FAIL, "Already a pending return with this invoke id "
                                                              + invokeId);
                }
                else
                {
                    this.pendingReturn.put(new Integer(invokeId), pConfOp);
                }
                this.accessPendingReturn.unlock();
            }
            else
            {
                // unable to get the conf operation interface
                throw new SleApiException(HRESULT.E_FAIL, "Unable to get the conf operation interface");
            }
            break;
        }
        }
    }
}
