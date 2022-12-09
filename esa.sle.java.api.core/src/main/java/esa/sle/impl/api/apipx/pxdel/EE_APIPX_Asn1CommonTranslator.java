/**
 * @(#) EE_APIPX_Asn1CommonTranslator.java
 */

package esa.sle.impl.api.apipx.pxdel;

import isp1.credentials.ISP1Credentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

//import com.beanit.jasn1.ber.BerByteArrayOutputStream;
import com.beanit.jasn1.ber.ReverseByteArrayOutputStream;
import com.beanit.jasn1.ber.types.BerInteger;
import com.beanit.jasn1.ber.types.BerNull;
import com.beanit.jasn1.ber.types.BerOctetString;

import ccsds.sle.api.isle.exception.HRESULT;
import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iop.ISLE_OperationFactory;
import ccsds.sle.api.isle.it.SLE_ApplicationIdentifier;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import ccsds.sle.transfer.service.common.types.ConditionalTime;
import ccsds.sle.transfer.service.common.types.Credentials;
import ccsds.sle.transfer.service.common.types.Time;
import ccsds.sle.transfer.service.common.types.TimeCCSDS;
import ccsds.sle.transfer.service.common.types.TimeCCSDSpico;

/**
 * The class contains methods that can be used to encode and decode PDU of
 * several types. Two global context attributes are needed to handle correctly
 * the multi-threading.
 */
public class EE_APIPX_Asn1CommonTranslator
{
    private static final Logger LOG = Logger.getLogger(EE_APIPX_Asn1CommonTranslator.class.getName());

    protected ISLE_OperationFactory operationFactory;

    protected ISLE_UtilFactory utilFactory;

    protected SLE_ApplicationIdentifier serviceType;

    /**
     * Length of the time CCSDS format.
     */
    protected static int C_LengthFormatCCSDS = 8;

    /**
     * Maximum length of the credentials for ASN1.
     */
    protected static int C_MaxLengthCredentials = 256;

    /**
     * Maximum length of the ObjectID for ASN1.
     */
    protected static int C_MaxLengthObjectID = 8;

    /**
     * Maximum length of the Authority Identifier for ASN1.
     */
    protected static int C_MaxLengthAuthorityIdentifier = 16;

    /**
     * Maximum length of the Logical Port Name for ASN1.
     */
    protected static int C_MaxLengthLogicalPortName = 128;

    /**
     * Maximum length of the RDN Value for ASN1.
     */
    protected static int C_MaxLengthRDNValue = 256;

    /**
     * Maximum number of RDN for ASN1.
     */
    protected static int C_MaxNumberRDN = 32;

    /**
     * Maximum length of the local antenna form for ASN1.
     */
    protected static int C_MaxLengthAntennaLocalForm = 16;

    /**
     * Maximum length of the private annotation for ASN1.
     */
    protected static int C_MaxLengthPrivateAnnotation = 128;

    /**
     * Maximum length of the VC list for ASN1. This value is also used in a
     * bitset in EE_TI_PXDEL.h.
     */
    protected static int C_MaxLengthVCList = 64;

    /**
     * Maximum length of the protected of the credentials for ASN1 (SHA-1).
     */
    protected static int C_MaxLengthProtectedToV4 = 20;
     
    /**
     * Maximum length of the protected of the credentials for ASN1, due to SHA-256.
     */
    protected static int C_MaxLengthProtectedSinceV5 = 32;

    protected int sleVersionNumber;

    /**
     * Length of the time CCSDS format.
     */
    protected static int C_LengthFormatPicoCCSDS = 10;

    private volatile ISLE_Time time;
    /**
     * Default constructor.
     */
    public EE_APIPX_Asn1CommonTranslator()
    {
        this.operationFactory = null;
        this.utilFactory = null;
        this.sleVersionNumber = 0;
        this.serviceType = SLE_ApplicationIdentifier.sleAI_invalid;
    }

    /**
     * Constructor of the class which takes the operation and utility factory as
     * parameter.
     */
    public EE_APIPX_Asn1CommonTranslator(ISLE_OperationFactory pOpFactory,
                                         ISLE_UtilFactory pUtilFactory,
                                         int sleVersionNumber)
    {
        this.operationFactory = pOpFactory;
        this.utilFactory = pUtilFactory;
        this.sleVersionNumber = sleVersionNumber;
        this.serviceType = SLE_ApplicationIdentifier.sleAI_invalid;
    }

    /**
     * Fills the Credentials of the Asn1 object from the Credentials of the
     * operation. S_OK The Credentials have been encoded. E_FAIL Unable to
     * encode the Credentials.
     * 
     * @throws SleApiException
     */
    protected Credentials encodeCredentials(ISLE_Credentials pCredentials) throws SleApiException
    {
        Credentials eea_o = new Credentials();

        if (pCredentials == null)
        {
            //eea_o.unused = new BerNull();
        	eea_o.setUnused(new BerNull());
        }
        else
        {
            // ISP1 Credentials
            ISP1Credentials isp1Credentials = new ISP1Credentials();

            // fill the time
            ISLE_Time time = pCredentials.getTimeRef();
            if (time != null)
            {
                //isp1Credentials.time = new BerOctetString(time.getCDS());
            	isp1Credentials.setTime(new BerOctetString(time.getCDS()));
            }
            else
            {
                //isp1Credentials.time = new BerOctetString();
            	isp1Credentials.setTime(new BerOctetString());
            }

            // fill the protected
            byte[] theProtected = pCredentials.getProtected();
            if (theProtected != null)
            {
            	if(sleVersionNumber <= 4)
            	{
            		if (theProtected.length > C_MaxLengthProtectedToV4)
            		{
            			//isp1Credentials.theProtected = new BerOctetString();
            			isp1Credentials.setTheProtected(new BerOctetString());
            		}
            		else
            		{
            			//isp1Credentials.theProtected = new BerOctetString(theProtected);
            			isp1Credentials.setTheProtected(new BerOctetString(theProtected));
            		}
                }
            	else
            	{
            		if (theProtected.length > C_MaxLengthProtectedSinceV5)
            		{
            			//isp1Credentials.theProtected = new BerOctetString();
            			isp1Credentials.setTheProtected(new BerOctetString());
            		}
            		else
            		{
            			//isp1Credentials.theProtected = new BerOctetString(theProtected);
            			isp1Credentials.setTheProtected(new BerOctetString(theProtected));
            		}
            	}
            }

            // fill the random number
            //isp1Credentials.randomNumber = new BerInteger(pCredentials.getRandomNumber());
            isp1Credentials.setRandomNumber(new BerInteger(pCredentials.getRandomNumber()));

            //BerByteArrayOutputStream encoding = new BerByteArrayOutputStream(60, true);
            ReverseByteArrayOutputStream encoding = new ReverseByteArrayOutputStream(72, true);
            try
            {
                isp1Credentials.encode(encoding, true);
                if (encoding.buffer.length > C_MaxLengthCredentials)
                {
                    //eea_o.used = new BerOctetString();
                	eea_o.setUsed(new BerOctetString());                	
                }
                else
                {
                    //eea_o.used = new BerOctetString(encoding.getArray());
                    eea_o.setUsed(new BerOctetString(encoding.getArray())); 
                }
            }
            catch (IOException e)
            {
                LOG.log(Level.FINE, "IOException ", e);
                throw new SleApiException(HRESULT.E_FAIL, "An I/O Exception has been catched");
            }
        }

        return eea_o;
    }

    /**
     * Fills the Credentials of the operation from the Asn1 object. S_OK A new
     * Credentials object has been instantiated. E_FAIL Unable to instantiate
     * new Credentials.
     * 
     * @throws SleApiException
     */
    protected ISLE_Credentials decodeCredentials(Credentials eea_o) throws SleApiException
    {
        ISLE_Credentials pCredentials = null;

        //if (eea_o.unused != null)
        if (eea_o.getUnused() != null)
        {
            pCredentials = null;
        }
        else
        {
            //if (eea_o.used.value.length <= C_MaxLengthCredentials)
        	if (eea_o.getUsed().value.length <= C_MaxLengthCredentials)
            {
                pCredentials = this.utilFactory.createCredentials(ISLE_Credentials.class);

                if (pCredentials != null)
                {
                    //InputStream is = new ByteArrayInputStream(eea_o.used.value);
                	InputStream is = new ByteArrayInputStream(eea_o.getUsed().value);
                    ISP1Credentials isp1c = new ISP1Credentials();

                    // decode ISP1 credentials
                    try
                    {
                        isp1c.decode(is, true);
                    }
                    catch (IOException e)
                    {
                        LOG.log(Level.FINE, "IOException ", e);
                        return null;
                    }

                    // set the time
                    ISLE_Time pTime = this.utilFactory.createTime(ISLE_Time.class);

                    if (pTime != null)
                    {
                        //if (isp1c.time.value.length <= C_LengthFormatCCSDS)
                    	if (isp1c.getTime().value.length <= C_LengthFormatCCSDS)
                        {
                            //pTime.setCDS(isp1c.time.value);
                    		pTime.setCDS(isp1c.getTime().value);
                            pCredentials.setTimeRef(pTime);
                        }
                    }

                    // set the protected
                    //pCredentials.setProtected(isp1c.theProtected.value);
                    pCredentials.setProtected(isp1c.getTheProtected().value);

                    // set the random number
                    //pCredentials.setRandomNumber(isp1c.randomNumber.value);
                    pCredentials.setRandomNumber(isp1c.getRandomNumber().longValue());
                }
            }
            else
            {
                // size of string is too big
                throw new SleApiException(HRESULT.E_FAIL, "The Credentials size is too big!!");
            }
        }

        return pCredentials;
    }

    /**
     * Fills the ConditionalTime of the Asn1 object.
     */
    protected ConditionalTime encodeConditionalTime(ISLE_Time pTime)
    {
        ConditionalTime eea_o = new ConditionalTime();
        if (pTime == null)
        {
            //eea_o.undefined = new BerNull();
        	eea_o.setUndefined(new BerNull());
            //eea_o.known = null;
        	eea_o.setKnown(null);
        }
        else
        {
            //eea_o.undefined = null;
        	eea_o.setUndefined(null);
            //eea_o.known = new Time(new TimeCCSDS(pTime.getCDS()), null);
        	TimeCCSDS ccsdsTime = new TimeCCSDS(pTime.getCDS());
        	Time time = new Time();
        	time.setCcsdsFormat(ccsdsTime);
        	eea_o.setKnown(time);
        }

        return eea_o;
    }

    /**
     * If necessary, instantiates and fills the Time from the Asn1 object
     * 
     * @param eea_o
     * @return
     * @throws SleApiException
     */
    protected ISLE_Time decodeConditionalTime(ConditionalTime eea_o) throws SleApiException
    {
        ISLE_Time pTime = null;
        if (eea_o.getUndefined() != null)
        {
            return null;
        }
        else if (eea_o.getKnown() != null)
        {
            pTime = this.utilFactory.createTime(ISLE_Time.class);
            if (pTime != null)
            {
                if (eea_o.getKnown().getCcsdsFormat().value.length == C_LengthFormatCCSDS)
                {
                    pTime.setCDS(eea_o.getKnown().getCcsdsFormat().value);
                }
                else
                {
                    throw new SleApiException(HRESULT.E_FAIL, "The length is not ok!");
                }
            }
        }

        return pTime;
    }

    /**
     * Fills the Time of the Asn1 object. Supports picosecond resolution
     * 
     * @throws SleApiException
     */
    protected Time encodeEarthReceiveTime(ISLE_Time pTime) throws SleApiException
    {
        Time eea_o = new Time();

        if (pTime == null)
        {
            return eea_o;
        }

        // check for picoseconds resolution
        if (pTime.getPicosecondsResUsed())
        {
            // check if the service version is capable to handle picoseconds
            boolean canHandle = false;
            switch (this.serviceType)
            {
            case sleAI_rtnAllFrames:
            case sleAI_rtnChFrames:
            case sleAI_fwdCltu:
            {
                canHandle = (this.sleVersionNumber >= 3) ? true : false;
                break;
            }
            case sleAI_rtnChOcf:
            case sleAI_fwdTcSpacePkt:
            {
                canHandle = (this.sleVersionNumber >= 2) ? true : false;
                break;
            }
            default:
            {
                canHandle = false;
                break;
            }
            }

            if (!canHandle)
            {
                // No handle possible, encode with the old function
                if (LOG.isLoggable(Level.INFO))
                {
                    LOG.info("[EE_APIPX_Asn1CommonTranslator::encodeEarthReceiveTime] The picosecond precision isn't handle for this version ("
                             + this.sleVersionNumber + ") of the SI.");
                    LOG.info("[EE_APIPX_Asn1CommonTranslator::encodeEarthReceiveTime] The pico second precision is handled since version 3 for RAF, RCF, and version 2 for ROCF.");
                }

                eea_o = encodeTime(pTime);
            }
            else
            {
                // version and service are supported, encode with picosecond
                // resolution
                TimeCCSDSpico eeaCcsdsPico = new TimeCCSDSpico();
                byte[] timeCds = pTime.getCDSToPicosecondsRes();
                if (timeCds != null)
                {
                    eeaCcsdsPico.value = timeCds;
                    eea_o.setCcsdsPicoFormat (eeaCcsdsPico);
                }
                else
                {
                    throw new SleApiException(HRESULT.E_FAIL);
                }
            }
        }
        else
        {
            // no picoseconds used, encode with the old function
            eea_o = encodeTime(pTime);
        }

        return eea_o;
    }

    /**
     * Fills the Time from the Asn1 object. Supports picosecond resolution.
     * 
     * @param eea
     * @return
     * @throws SleApiException
     */
    protected ISLE_Time decodeEarthReceiveTime(Time eea) throws SleApiException
    {
        ISLE_Time pTime = null;

        if (eea.getCcsdsPicoFormat() != null)
        {
//            pTime = this.utilFactory.createTime(ISLE_Time.class);
//            if (pTime != null)
//            {
                if (eea.getCcsdsPicoFormat().value.length == C_LengthFormatPicoCCSDS)
                {
                    try
                    {
//                        pTime.setCDSToPicosecondsRes(eea.ccsdsPicoFormat.value);
                    	pTime = this.utilFactory.createTime(ISLE_Time.class, eea.getCcsdsPicoFormat().value);
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                        LOG.info("[EE_APIPX_Asn1CommonTranslator::decodeEarthReceiveTime] Set_CDS_ToPicosecondsRes fails");
                        pTime = null;
                    }

                    // check if the service and the version handle picosecond
                    // resolution
                    boolean canHandle = false;
                    switch (this.serviceType)
                    {
                    case sleAI_rtnAllFrames:
                    case sleAI_rtnChFrames:
                    case sleAI_fwdCltu:
                    {
                        canHandle = (this.sleVersionNumber >= 3) ? true : false;
                        break;
                    }
                    case sleAI_rtnChOcf:
                    case sleAI_fwdTcSpacePkt:
                    {
                        canHandle = (this.sleVersionNumber >= 2) ? true : false;
                        break;
                    }
                    default:
                    {
                        canHandle = false;
                        break;
                    }
                    }

                    if (!canHandle)
                    {
                        // revert to microsecond resolution
                        LOG.info("[EE_APIPX_Asn1CommonTranslator::decodeEarthReceiveTime] The picosecond precision isn't handle for this version ("
                                 + this.sleVersionNumber + ") of the SI.");
                        LOG.info("[EE_APIPX_Asn1CommonTranslator::decodeEarthReceiveTime] The pico second precision is handled since version 3 for RAF, RCF, and version 2 for ROCF.");
                        byte[] utimeCds = pTime.getCDS();
                        pTime.setCDS(utimeCds);
                    }
                }
                else
                {
                    // the length is not ok
                    pTime = null;
                }
//            }
        }
        else
        {
            // decode with the old function
            pTime = decodeTime(eea);
        }

        return pTime;
    }

    /**
     * Fills the Time of the Asn1 object.
     * 
     * @param pTime
     * @return
     * @throws SleApiException
     */
    protected Time encodeTime(ISLE_Time pTime) throws SleApiException
    {
        TimeCCSDS eeaCcsds = new TimeCCSDS();
        byte[] timeCds = pTime.getCDS();
        if (timeCds != null)
        {
            eeaCcsds.value = timeCds;
        }
        else
        {
            throw new SleApiException(HRESULT.E_FAIL);
        }
        
        //Time eea = new Time(eeaCcsds.value);
        Time eea = new Time();      
        eea.setCcsdsFormat(eeaCcsds); // JC added for correction
  
        return eea;
    }

    /**
     * Fills the Time from the Asn1 object.
     * 
     * @param eea
     * @return
     * @throws SleApiException
     */
    protected ISLE_Time decodeTime(Time eea) throws SleApiException
    {
    	if(this.time == null)
    	{
    		this.time = this.utilFactory.createTime(ISLE_Time.class);
    	}
    	
        ISLE_Time pTime = null;

        if (eea.getCcsdsFormat() != null)
        {
            //pTime = this.utilFactory.createTime(ISLE_Time.class);
        	pTime = this.time.copy(); // cheaper than the this.utilFactory.createTime which initialises with current time
            if (pTime != null)
            {
                if (eea.getCcsdsFormat().value.length == C_LengthFormatCCSDS)
                {
                    try
                    {
                        pTime.setCDS(eea.getCcsdsFormat().value);
                    }
                    catch (SleApiException e)
                    {
                        LOG.log(Level.FINE, "SleApiException ", e);
                        pTime = null;
                    }
                }
                else
                {
                    pTime = null;
                }
            }
        }

        return pTime;
    }

}
