/**
 * @(#) EE_APIPX_Asn1BindTranslator.java
 */

package esa.sle.impl.api.apipx.pxdel;

import isp1.sle.bind.pdus.SleBindInvocationPdu;
import isp1.sle.bind.pdus.SleBindReturnPdu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.beanit.jasn1.ber.types.BerObjectIdentifier;
import com.beanit.jasn1.ber.types.string.BerVisibleString;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_Bind;
import ccsds.sle.api.isle.iop.ISLE_Operation;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.it.SLE_BindDiagnostic;
import ccsds.sle.api.isle.it.SLE_GlobalRDN;
import ccsds.sle.api.isle.it.SLE_OpType;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;
import ccsds.sle.api.isle.iutl.ISLE_SII;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import ccsds.sle.transfer.service.bind.types.ApplicationIdentifier;
import ccsds.sle.transfer.service.bind.types.AttributeTypeAndValue;
import ccsds.sle.transfer.service.bind.types.AuthorityIdentifier;
import ccsds.sle.transfer.service.bind.types.BindDiagnostic;
import ccsds.sle.transfer.service.bind.types.PortId;
import ccsds.sle.transfer.service.bind.types.ServiceInstanceIdentifier;
import ccsds.sle.transfer.service.bind.types.ServiceInstanceIdentifier.SETOF;
import ccsds.sle.transfer.service.bind.types.SleBindReturn;
import ccsds.sle.transfer.service.bind.types.VersionNumber;
import esa.sle.impl.ifs.gen.EE_Reference;

/**
 * ASN.1 Bind Translator The class encodes and decodes BIND PDU's. When
 * decoding, the decoded BIND operation is instantiated. The class contains
 * several private methods used to encode and decode some parts of the bind
 * operation.
 */
public class EE_APIPX_Asn1BindTranslator extends EE_APIPX_Asn1CommonTranslator
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_Asn1BindTranslator.class.getName());

    protected EE_APIPX_PDUTranslator pduTranslator;


    /**
     * Constructor of the class which takes the operation and utility factory as
     * parameter.
     */
    public EE_APIPX_Asn1BindTranslator(ISLE_OperationFactory pOpFactory,
                                       ISLE_UtilFactory pUtilFactory,
                                       EE_APIPX_PDUTranslator pdutranslator,
                                       int sleVersionNumber)
    {
        super(pOpFactory, pUtilFactory, sleVersionNumber);
        this.pduTranslator = pdutranslator;
    }

    /**
     * Default Constructor
     */
    public EE_APIPX_Asn1BindTranslator()
    {
        super();
        this.pduTranslator = null;
    }

    /**
     * Instantiates a new SLE BIND operation from the object given as parameter.
     * This method is used when a bind pdu is received from the network, and the
     * service type is not yet known. S_OK A new BIND operation has been
     * instantiated. E_FAIL Unable to instantiate a BIND operation.
     * 
     * @throws SleApiException
     */
    public ISLE_Operation decodeBindOp(SleBindInvocationPdu eea_bind_o, EE_Reference<Boolean> isInvoke) throws SleApiException
    {
        ISLE_Operation retOp = null;
        ISLE_Bind pOp = null;

        SLE_OpType opType = SLE_OpType.sleOT_bind;
        SLE_ApplicationIdentifier srvType = SLE_ApplicationIdentifier
                .getApplIdByCode( eea_bind_o.getServiceType().intValue());
        this.sleVersionNumber = eea_bind_o.getVersionNumber().intValue();

        try
        {
            pOp = this.operationFactory.createOperation(ISLE_Bind.class, opType, srvType, this.sleVersionNumber);
        }
        catch (SleApiException e)
        {

            LOG.log(Level.FINE, "SleApiException ", e);
            if (e.getHResult() == HRESULT.SLE_E_INCONSISTENT)
            {
                // service type not supported
                throw new SleApiException(HRESULT.SLE_E_UNKNOWN, "Service Type not supported!");
            }
        }

        decodeBindInvokeOp(eea_bind_o, pOp);
        isInvoke.setReference(new Boolean(true));
        retOp = pOp.queryInterface(ISLE_Operation.class);

        return retOp;
    }

    /**
     * Fills the object used for the encoding of Bind invoke operation. S_OK The
     * Bind operation has been encoded. E_FAIL Unable to encode the Bind
     * operation.
     * 
     * @throws SleApiException
     * @throws IOException 
     */
    protected void encodeBindInvokeOp(ISLE_Operation pOperation, SleBindInvocationPdu eea_bind_o) throws SleApiException, IOException
    {
        ISLE_Bind pBindOp = pOperation.queryInterface(ISLE_Bind.class);
        if (pBindOp != null)
        {
            // the invoker credentials
            eea_bind_o.setInvokerCredentials(encodeCredentials(pBindOp.getInvokerCredentials()));

            // the initiator identifier
            eea_bind_o.setInitiatorIdentifier(encodeAuthorityIdentifier(pBindOp.getInitiatorIdentifier()));

            // the responder port identifier
            eea_bind_o.setResponderPortIdentifier(encodePortId(pBindOp.getResponderPortIdentifier()));

            // the service type
            eea_bind_o.setServiceType (new ApplicationIdentifier(pBindOp.getServiceType().getCode()));

            // the version number
            eea_bind_o.setVersionNumber(new VersionNumber(pBindOp.getVersionNumber()));

            // the service instance identifier
            eea_bind_o.setServiceInstanceIdentifier(encodeSII(pBindOp.getServiceInstanceId()));
        }
    }

    /**
     * Fills the BIND invoke operation from the object. S_OK The Bind operation
     * has been decoded. E_FAIL Unable to decode the Bind operation.
     */
    protected void decodeBindInvokeOp(SleBindInvocationPdu eea_bind_o, ISLE_Bind pBindOperation) throws SleApiException
    {
        // the invoker credentials
        ISLE_Credentials pCredentials = decodeCredentials(eea_bind_o.getInvokerCredentials());

        if (pCredentials != null)
        {
            pBindOperation.putInvokerCredentials(pCredentials);
        }

        // the initiator identifier
        String initiatorId = decodeAuthorityIdentifier(eea_bind_o.getInitiatorIdentifier());
        if (initiatorId != null)
        {
            pBindOperation.setInitiatorIdentifier(initiatorId);
        }

        // the responder port identifier
        String rspportId = decodePortId(eea_bind_o.getResponderPortIdentifier());
        if (rspportId != null)
        {
            pBindOperation.setResponderPortIdentifier(rspportId);
        }

        // the service type
        SLE_ApplicationIdentifier srvType = SLE_ApplicationIdentifier
                .getApplIdByCode(eea_bind_o.getServiceType().intValue());
        pBindOperation.setServiceType(srvType);

        // the version number
        pBindOperation.setVersionNumber(eea_bind_o.getVersionNumber().intValue());

        // the service instance identifier
        boolean initialFormat = false;
        switch (srvType)
        {
        // services implemented in v1 can use the initial sii format
        case sleAI_rtnAllFrames:
        case sleAI_rtnChFrames:
        case sleAI_fwdCltu:
            initialFormat = eea_bind_o.getVersionNumber().intValue() == 1 ? true : false;
            break;
        // services implemented in v2 use the new sii format only
        case sleAI_rtnChOcf:
        case sleAI_fwdTcSpacePkt:
            initialFormat = false;
            break;
        // services implemented in future releases can use the new sii format
        // only
        default:
            initialFormat = false;
            break;
        }

        ISLE_SII psii = decodeSII(eea_bind_o.getServiceInstanceIdentifier(), initialFormat);
        if (psii != null)
        {
            pBindOperation.putServiceInstanceId(psii);
        }
    }

    /**
     * Fills the object used for the encoding of Bind return operation. S_OK The
     * Bind operation has been encoded. E_FAIL Unable to encode the Bind
     * operation.
     * 
     * @throws SleApiException
     */
    protected void encodeBindReturnOp(ISLE_Operation pOperation, SleBindReturnPdu eea_bind_o) throws SleApiException
    {
        ISLE_Bind pBindOperation = null;
        pBindOperation = pOperation.queryInterface(ISLE_Bind.class);
        if (pBindOperation != null)
        {
            // the performer credentials
            ISLE_Credentials pCredentials = null;
            pCredentials = pBindOperation.getPerformerCredentials();
            eea_bind_o.setPerformerCredentials(encodeCredentials(pCredentials));

            // the responder identifier
            eea_bind_o.setResponderIdentifier(encodeAuthorityIdentifier(pBindOperation.getResponderIdentifier()));

            switch (pBindOperation.getResult())
            {
            case sleRES_positive:
            {
                //eea_bind_o.result = new Result(new VersionNumber(pBindOperation.getVersionNumber()), null);
            	SleBindReturn.Result res = new SleBindReturn.Result();
            	res.setPositive(new VersionNumber(pBindOperation.getVersionNumber()));
            	eea_bind_o.setResult(res);
                break;
            }
            case sleRES_negative:
            {
                //eea_bind_o.result = new Result(null, new BindDiagnostic(pBindOperation.getBindDiagnostic()
                //        .getCode()));
            	SleBindReturn.Result res = new SleBindReturn.Result();
            	res.setNegative(new BindDiagnostic(pBindOperation.getBindDiagnostic().getCode()));
            	eea_bind_o.setResult(res);
                break;
            }
            default:
            {
                // the asn1 encoder will fail
                break;
            }
            }
        }
    }

    /**
     * Fills the BIND return operation from the object. S_OK The Bind operation
     * has been decoded. E_FAIL Unable to decode the Bind operation.
     * 
     * @throws SleApiException
     */
    protected ISLE_Bind decodeBindReturnOp(SleBindReturnPdu eea_bind_o) throws SleApiException
    {
        ISLE_Bind pBindOperation = this.pduTranslator.getBindReturnOp();
        if (pBindOperation != null)
        {
            // the performer credentials
            ISLE_Credentials pCredentials = decodeCredentials(eea_bind_o.getPerformerCredentials());
            if (pCredentials != null)
            {
                pBindOperation.putPerformerCredentials(pCredentials);
            }

            // the responder identifier
            String responderId = decodeAuthorityIdentifier(eea_bind_o.getResponderIdentifier());
            if (responderId != null)
            {
                pBindOperation.setResponderIdentifier(responderId);
            }

            if (eea_bind_o.getResult().getPositive() != null)
            {
                pBindOperation.setPositiveResult();
                pBindOperation.setVersionNumber(eea_bind_o.getResult().getPositive().intValue());
            }
            else if (eea_bind_o.getResult().getNegative() != null)
            {
                SLE_BindDiagnostic diag = SLE_BindDiagnostic
                        .getBindDiagnosticByCode(eea_bind_o.getResult().getNegative().intValue());
                pBindOperation.setBindDiagnostic(diag);
            }
            else
            {
                throw new SleApiException(HRESULT.E_FAIL);
            }
        }

        return pBindOperation;
    }

    /**
     * Fill the AuthorityIdentifier of the Asn1 object from the
     * AuthorityIdentifier.
     */
    private AuthorityIdentifier encodeAuthorityIdentifier(String pAuthorityIdentifier)
    {
        AuthorityIdentifier eea_bind_o = null;
        if (pAuthorityIdentifier == null || pAuthorityIdentifier.length() > C_MaxLengthAuthorityIdentifier)
        {
            eea_bind_o = new AuthorityIdentifier();
        }
        else
        {
            eea_bind_o = new AuthorityIdentifier(pAuthorityIdentifier.getBytes());
        }

        return eea_bind_o;
    }

    /**
     * Fills the AuthorityIdentifier from the Asn1 object.
     */
    private String decodeAuthorityIdentifier(AuthorityIdentifier eea_bind_o)
    {
        String pAuthorityIdentifier = null;
        if (eea_bind_o.value.length <= C_MaxLengthAuthorityIdentifier)
        {
            pAuthorityIdentifier = new String(eea_bind_o.value);
        }

        return pAuthorityIdentifier;
    }

    /**
     * Fill the PortId of the Asn1 object from the PortId.
     */
    private PortId encodePortId(String pPortId)
    {
        PortId eea_bind_o = null;
        if (pPortId == null || pPortId.length() > C_MaxLengthLogicalPortName)
        {
            eea_bind_o = new PortId();
        }
        else
        {
            eea_bind_o = new PortId(pPortId.getBytes());
        }

        return eea_bind_o;
    }

    /**
     * Fills the PortId from the Asn1 object.
     */
    private String decodePortId(PortId eea_bind_o)
    {
        String pPortId = null;
        if (eea_bind_o.value.length <= C_MaxLengthLogicalPortName)
        {
            pPortId = new String(eea_bind_o.value);
        }

        return pPortId;
    }

    /**
     * Fill the ServiceInstanceIdentifier of the Asn1 object from the Service
     * Instance Identifier.
     * 
     * @throws SleApiException
     * @throws IOException 
     */
    private ServiceInstanceIdentifier encodeSII(ISLE_SII pSii) throws SleApiException
    {
        List<SETOF> ssoList = new ArrayList<ServiceInstanceIdentifier.SETOF>(0);
        pSii.reset();
      
        while (pSii.moreData())
        {
            SLE_GlobalRDN gRDN = pSii.nextGlobalRDN();
            // encode the object identifier and the attribute value
            BerObjectIdentifier objId = new BerObjectIdentifier(gRDN.getOid());
            BerVisibleString attributeValue = null;

            if (gRDN.getValue().length() > C_MaxLengthRDNValue)
            {
                attributeValue = new BerVisibleString();
            }
            else
            {
                attributeValue = new BerVisibleString(gRDN.getValue());
            }

            //AttributeTypeAndValue atv = new AttributeTypeAndValue(objId, attributeValue);
            AttributeTypeAndValue atv = new AttributeTypeAndValue();
            atv.setAttributeID(objId);
            atv.setAttributeValue(attributeValue);

            List<AttributeTypeAndValue> atvList = new ArrayList<AttributeTypeAndValue>();
            atvList.add(atv);

            SETOF sso = new SETOF();
            sso.getAttributeTypeAndValue().addAll(atvList);
            ssoList.add(sso);

            // no more than 32 rdn in the c-structure !!
            if (ssoList.size() == C_MaxNumberRDN)
            {
                break;
            }
        }

        //ServiceInstanceIdentifier eea_ssi_o = new ServiceInstanceIdentifier(ssoList);
        ServiceInstanceIdentifier eea_ssi_o = new ServiceInstanceIdentifier();
        eea_ssi_o.getSETOF().addAll(ssoList);

        return eea_ssi_o;
    }

    /**
     * Fills the Service Instance Identifier from the Asn1 object.
     * 
     * @throws SleApiException
     */
    private ISLE_SII decodeSII(ServiceInstanceIdentifier eea_sii_o, boolean useInitialFormat) throws SleApiException
    {
        ISLE_SII psii = null;
        psii = this.utilFactory.createSII(ISLE_SII.class);
        if (psii != null)
        {
            psii.setToNull();
        }

        if (useInitialFormat)
        {
            psii.setInitialFormat();
        }

        //for (SETOF sso : eea_sii_o.seqOf)
        for (SETOF sso : eea_sii_o.getSETOF())
        {
            //for (AttributeTypeAndValue atv : sso.seqOf)
        	for (AttributeTypeAndValue atv : sso.getAttributeTypeAndValue())
            {
                //int[] objId = atv.attributeID.value;
        		int[] objId = atv.getAttributeID().value;
                if (atv.getAttributeID().value.length <= C_MaxLengthRDNValue)
                {
                    //String value = new String(atv.attributeValue.value);
                	String value = new String(atv.getAttributeValue().value);
                    psii.addGlobalRDN(objId, value);
                }
                else
                {
                    throw new SleApiException(HRESULT.E_FAIL, "The attribute value length is too big!!");
                }
            }
        }

        psii.reset();
        return psii;
    }
}
