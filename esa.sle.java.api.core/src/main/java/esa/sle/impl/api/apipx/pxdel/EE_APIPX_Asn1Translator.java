/**
 * @(#) EE_APIPX_Asn1Translator.java
 */

package esa.sle.impl.api.apipx.pxdel;

import isp1.sle.bind.pdus.SleBindInvocationPdu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.beanit.jasn1.ber.BerTag;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.iop.ISLE_PeerAbort;
import ccsds.sle.api.isle.it.SLE_AbortOriginator;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.it.SLE_PeerAbortDiagnostic;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * Asn1 Translator The class provides the means to use the Asn1 compiler to
 * encode and decode the PDU. When encoding and decoding, it selects the
 * corresponding ASN.1 translator based on the service-type. When instantiated,
 * the class initializes the asn1 compiler runtime library. The destructor of
 * the class calls the asnfree operation of the asn1 compiler runtime library to
 * indicate the termination. This class uses the MARBEN ASN.1 compiler. Relies
 * on the platform dependency of the MARBEN ASN.1 compiler.
 */
public class EE_APIPX_Asn1Translator
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_Asn1Translator.class.getName());

    /**
     * Pointer to the operation factory interface.
     */
    private final ISLE_OperationFactory operationFactory;

    /**
     * Pointer to the utility factory interface.
     */
    private final ISLE_UtilFactory utilFactory;

    private int sleVersionNumber;

    public EE_APIPX_PDUTranslator pduTranslator;

    public EE_APIPX_Asn1FspTranslator fspTranslator;

    public EE_APIPX_Asn1RafTranslator rafTranslator;

    public EE_APIPX_Asn1BindTranslator bindTranslator;

    public EE_APIPX_Asn1RcfTranslator rcfTranslator;

    public EE_APIPX_Asn1RocfTranslator rocfTranslator;

    public EE_APIPX_Asn1CltuTranslator cltuTranslator;


    /**
     * Constructor of the class which takes the operation factory as parameter.
     */
    public EE_APIPX_Asn1Translator(ISLE_OperationFactory pOpFactory,
                                   ISLE_UtilFactory pUtilFactory,
                                   EE_APIPX_PDUTranslator pduTranslator)
    {
        this.operationFactory = pOpFactory;
        this.utilFactory = pUtilFactory;
        this.sleVersionNumber = 0;
        this.pduTranslator = pduTranslator;
        this.fspTranslator = null;
        this.rafTranslator = null;
        this.bindTranslator = null;
        this.rcfTranslator = null;
        this.rocfTranslator = null;
        this.cltuTranslator = null;
    }

    /**
     * Decodes the pdu, and instantiates a new operation. S_OK The pdu has been
     * decoded and a new operation instantiated. SLE_E_INVALIDPDU The decoded
     * operation is not the expected one (the pdu number sets by the decode is
     * not the expected one). E_FAIL Unable to decode the pdu.
     * 
     * @throws IOException
     */
    public ISLE_Operation decode(byte[] encodedPdu,
                                 SLE_ApplicationIdentifier serviceType,
                                 EE_Reference<Boolean> isInvoke) throws SleApiException, IOException
    {
        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("Decoding ASN.1 byte array (size " + encodedPdu.length + "): "
                       + EE_GenStrUtil.convAscii(encodedPdu, encodedPdu.length));
        }
        ISLE_Operation retOp = null;

        if (serviceType == SLE_ApplicationIdentifier.sleAI_invalid)
        {
            // the bind translator has to be created
            if (this.bindTranslator == null)
            {
                this.bindTranslator = new EE_APIPX_Asn1BindTranslator(this.operationFactory,
                                                                      this.utilFactory,
                                                                      this.pduTranslator,
                                                                      0);
                InputStream is = new ByteArrayInputStream(encodedPdu);
                BerTag identifier = new BerTag();
                identifier.decode(is);

                // it must be an incoming bind invoke operation
                if (!identifier.equals(SleBindInvocationPdu.tag))
                {
                    throw new SleApiException(HRESULT.SLE_E_INVALIDPDU);
                }
                else
                {
                    SleBindInvocationPdu pdu = new SleBindInvocationPdu();

                    try
                    {
                        pdu.decode(is, false);
                    }
                    catch (IOException e)
                    {
                        LOG.log(Level.FINE, "IOException ", e);
                        throw new SleApiException(HRESULT.E_FAIL, "An I/O Exception has been catched");
                    }

                    retOp = this.bindTranslator.decodeBindOp(pdu, isInvoke);
                }
            }

            return retOp;
        }
        else
        {
            int version = 0;
            if (this.bindTranslator != null)
            {
                // provider side: get from BindTranslator
                version = this.bindTranslator.sleVersionNumber;
            }
            else
            {
                // user side: get from memorized during BIND invocation
                version = this.sleVersionNumber;
            }

            switch (serviceType)
            {
            case sleAI_rtnAllFrames:
            {
                if (this.rafTranslator == null)
                {
                    this.rafTranslator = new EE_APIPX_Asn1RafTranslator(this.operationFactory,
                                                                        this.utilFactory,
                                                                        this.pduTranslator,
                                                                        version);
                }

                try
                {
                    retOp = this.rafTranslator.decodeRafOp(encodedPdu, isInvoke);
                }
                catch (IOException e)
                {
                    LOG.log(Level.FINE, "IOException ", e);
                    throw new SleApiException(HRESULT.E_FAIL, "An I/O Exception has been catched");
                }
                break;
            }
            case sleAI_rtnChFrames:
            {
                if (this.rcfTranslator == null)
                {
                    this.rcfTranslator = new EE_APIPX_Asn1RcfTranslator(this.operationFactory,
                                                                        this.utilFactory,
                                                                        this.pduTranslator,
                                                                        version);
                }

                retOp = this.rcfTranslator.decodeRcfOp(encodedPdu, isInvoke);
                break;
            }
            case sleAI_rtnChOcf:
            {
                if (this.rocfTranslator == null)
                {
                    this.rocfTranslator = new EE_APIPX_Asn1RocfTranslator(this.operationFactory,
                                                                          this.utilFactory,
                                                                          this.pduTranslator,
                                                                          version);
                }

                retOp = this.rocfTranslator.decodeRocfOp(encodedPdu, isInvoke);
                break;
            }
            case sleAI_fwdCltu:
            {
                if (this.cltuTranslator == null)
                {
                    this.cltuTranslator = new EE_APIPX_Asn1CltuTranslator(this.operationFactory,
                                                                          this.utilFactory,
                                                                          this.pduTranslator,
                                                                          version);
                }

                retOp = this.cltuTranslator.decodeCltuOp(encodedPdu, isInvoke);
                break;
            }
            case sleAI_fwdTcSpacePkt:
            {
                if (this.fspTranslator == null)
                {
                    this.fspTranslator = new EE_APIPX_Asn1FspTranslator(this.operationFactory,
                                                                        this.utilFactory,
                                                                        this.pduTranslator,
                                                                        version);
                }

                retOp = this.fspTranslator.decodeFspOp(encodedPdu, isInvoke);
                break;
            }
            default:
            {
                throw new SleApiException(HRESULT.E_FAIL, "Unknown Service type!!");
            }
            }
        }

        return retOp;
    }

    /**
     * Decodes the PEER-ABORT pdu, and instantiates a new operation. S_OK The
     * pdu has been decoded and a new operation instantiated. E_PENDING The
     * pending return for the decoded return operation cannot be founded. E_FAIL
     * Unable to decode the pdu.
     * 
     * @throws SleApiException
     */
    public ISLE_Operation decode(int encodedDiag,
                                 SLE_AbortOriginator peerabortOriginator,
                                 SLE_ApplicationIdentifier serviceType) throws SleApiException
    {
        ISLE_Operation pOp = null;
        ISLE_PeerAbort pPeerAbort = null;

        // for the user use the version memorized from the bind invocation
        int version = this.sleVersionNumber;

        // for the provider use the version received within the bind invocation
        if (this.bindTranslator != null)
        {
            version = this.bindTranslator.sleVersionNumber;
        }

        pPeerAbort = this.operationFactory.createOperation(ISLE_PeerAbort.class,
                                                           SLE_OpType.sleOT_peerAbort,
                                                           serviceType,
                                                           version);

        if (pPeerAbort != null)
        {
            SLE_PeerAbortDiagnostic diag = SLE_PeerAbortDiagnostic.slePAD_invalid;
            switch (serviceType)
            {
            case sleAI_rtnAllFrames:
            {
                if (this.rafTranslator == null)
                {
                    this.rafTranslator = new EE_APIPX_Asn1RafTranslator(this.operationFactory,
                                                                        this.utilFactory,
                                                                        this.pduTranslator,
                                                                        version);
                }

                diag = SLE_PeerAbortDiagnostic.getDiagByCode(encodedDiag);
                break;
            }
            case sleAI_rtnChFrames:
            {
                if (this.rcfTranslator == null)
                {
                    this.rcfTranslator = new EE_APIPX_Asn1RcfTranslator(this.operationFactory,
                                                                        this.utilFactory,
                                                                        this.pduTranslator,
                                                                        version);
                }

                diag = SLE_PeerAbortDiagnostic.getDiagByCode(encodedDiag);
                break;
            }
            case sleAI_rtnChOcf:
            {
                if (this.rocfTranslator == null)
                {
                    this.rocfTranslator = new EE_APIPX_Asn1RocfTranslator(this.operationFactory,
                                                                          this.utilFactory,
                                                                          this.pduTranslator,
                                                                          version);
                }

                diag = SLE_PeerAbortDiagnostic.getDiagByCode(encodedDiag);
                break;
            }
            case sleAI_fwdCltu:
            {
                if (this.cltuTranslator == null)
                {
                    this.cltuTranslator = new EE_APIPX_Asn1CltuTranslator(this.operationFactory,
                                                                          this.utilFactory,
                                                                          this.pduTranslator,
                                                                          version);
                }

                diag = SLE_PeerAbortDiagnostic.getDiagByCode(encodedDiag);
                break;
            }
            case sleAI_fwdTcSpacePkt:
            {
                if (this.fspTranslator == null)
                {
                    this.fspTranslator = new EE_APIPX_Asn1FspTranslator(this.operationFactory,
                                                                        this.utilFactory,
                                                                        this.pduTranslator,
                                                                        version);
                }

                diag = SLE_PeerAbortDiagnostic.getDiagByCode(encodedDiag);
                break;
            }
            default:
            {
                throw new SleApiException(HRESULT.E_FAIL, "Service Type Unknown!");
            }
            }

            pPeerAbort.setPeerAbortDiagnostic(diag);
            pPeerAbort.setAbortOriginator(peerabortOriginator);
            pOp = pPeerAbort.queryInterface(ISLE_Operation.class);

        }

        return pOp;
    }

    /**
     * Encodes the operation. S_OK The operation has been encoded. E_FAIL Unable
     * to encode the operation.
     * 
     * @throws IOException
     * @throws SleApiException
     */
    public byte[] encode(ISLE_Operation pOperation, boolean isInvoke) throws SleApiException, IOException
    {
        byte[] output = null;
        int version = pOperation.getOpVersionNumber();
        // on the user side, the translator has to memorize the bind version,
        // to chose the correct version for decoding the return PDUs.
        // this will also happen on the provider side, but to no effect.

        SLE_ApplicationIdentifier serviceType = pOperation.getOpServiceType();

        if (pOperation.getOperationType() == SLE_OpType.sleOT_bind)
        {
            this.sleVersionNumber = version;
        }

        switch (serviceType)
        {
        default:
        case sleAI_rtnAllFrames:
        {
            if (this.rafTranslator == null)
            {
                this.rafTranslator = new EE_APIPX_Asn1RafTranslator(this.operationFactory,
                                                                    this.utilFactory,
                                                                    this.pduTranslator,
                                                                    version);
            }

            output = this.rafTranslator.encodeRafOp(pOperation, isInvoke);

            break;
        }
        case sleAI_rtnChFrames:
        {
            if (this.rcfTranslator == null)
            {
                this.rcfTranslator = new EE_APIPX_Asn1RcfTranslator(this.operationFactory,
                                                                    this.utilFactory,
                                                                    this.pduTranslator,
                                                                    version);
            }

            output = this.rcfTranslator.encodeRcfOp(pOperation, isInvoke);

            break;
        }
        case sleAI_rtnChOcf:
        {
            if (this.rocfTranslator == null)
            {
                this.rocfTranslator = new EE_APIPX_Asn1RocfTranslator(this.operationFactory,
                                                                      this.utilFactory,
                                                                      this.pduTranslator,
                                                                      version);
            }

            output = this.rocfTranslator.encodeRocfOp(pOperation, isInvoke);

            break;
        }
        case sleAI_fwdCltu:
        {
            if (this.cltuTranslator == null)
            {
                this.cltuTranslator = new EE_APIPX_Asn1CltuTranslator(this.operationFactory,
                                                                      this.utilFactory,
                                                                      this.pduTranslator,
                                                                      version);
            }

            output = this.cltuTranslator.encodeCltuOp(pOperation, isInvoke);

            break;
        }
        case sleAI_fwdTcSpacePkt:
        {
            if (this.fspTranslator == null)
            {
                this.fspTranslator = new EE_APIPX_Asn1FspTranslator(this.operationFactory,
                                                                    this.utilFactory,
                                                                    this.pduTranslator,
                                                                    version);
            }

            output = this.fspTranslator.encodeFspOp(pOperation, isInvoke);

            break;
        }
        }

        return output;
    }

    /**
     * Encodes the PEER-ABORT operation. S_OK The operation has been encoded.
     * E_FAIL Unable to encode the operation.
     * 
     * @throws SleApiException
     */
    public int encode(ISLE_Operation pOperation) throws SleApiException
    {
        int encodedDiag = -1;
        if (pOperation.getOperationType() == SLE_OpType.sleOT_peerAbort)
        {
            ISLE_PeerAbort pPeerAbort = pOperation.queryInterface(ISLE_PeerAbort.class);
            if (pPeerAbort != null)
            {
                switch (pOperation.getOpServiceType())
                {
                case sleAI_rtnAllFrames:
                {
                    if (this.rafTranslator == null)
                    {
                        this.rafTranslator = new EE_APIPX_Asn1RafTranslator(this.operationFactory,
                                                                            this.utilFactory,
                                                                            this.pduTranslator,
                                                                            this.sleVersionNumber);
                    }

                    break;
                }
                case sleAI_rtnChFrames:
                {
                    if (this.rcfTranslator == null)
                    {
                        this.rcfTranslator = new EE_APIPX_Asn1RcfTranslator(this.operationFactory,
                                                                            this.utilFactory,
                                                                            this.pduTranslator,
                                                                            this.sleVersionNumber);
                    }

                    break;
                }
                case sleAI_rtnChOcf:
                {
                    if (this.rocfTranslator == null)
                    {
                        this.rocfTranslator = new EE_APIPX_Asn1RocfTranslator(this.operationFactory,
                                                                              this.utilFactory,
                                                                              this.pduTranslator,
                                                                              this.sleVersionNumber);
                    }

                    break;
                }
                case sleAI_fwdCltu:
                {
                    if (this.cltuTranslator == null)
                    {
                        this.cltuTranslator = new EE_APIPX_Asn1CltuTranslator(this.operationFactory,
                                                                              this.utilFactory,
                                                                              this.pduTranslator,
                                                                              this.sleVersionNumber);
                    }

                    break;
                }
                case sleAI_fwdTcSpacePkt:
                {
                    if (this.fspTranslator == null)
                    {
                        this.fspTranslator = new EE_APIPX_Asn1FspTranslator(this.operationFactory,
                                                                            this.utilFactory,
                                                                            this.pduTranslator,
                                                                            this.sleVersionNumber);
                    }

                    break;
                }
                default:
                {
                    throw new SleApiException(HRESULT.E_FAIL, "Service type Unsupported!");
                }
                }

                encodedDiag = pPeerAbort.getPeerAbortDiagnostic().getCode();
            }
            else
            {
                // query interface failed
                throw new SleApiException(HRESULT.E_FAIL, "No interface!");
            }
        }
        else
        {
            // not a peer abort op!!
            throw new SleApiException(HRESULT.E_FAIL, "Not a peer abort operation!!");
        }

        return encodedDiag;
    }
}
