/**
 * @(#) EE_SLE_SecAttributes.java
 */

package esa.sle.impl.api.apiut;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.iscm.IUnknown;
import ccsds.sle.api.isle.iutl.ISLE_Credentials;
import ccsds.sle.api.isle.iutl.ISLE_SecAttributes;
import ccsds.sle.api.isle.iutl.ISLE_Time;
import ccsds.sle.api.isle.iutl.ISLE_UtilFactory;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;

/**
 * ////////////////////////////////////////////////// The class provides an
 * implementation of the interface ISLE_SecAttributes as specified in reference
 * [SLE-API] for the component class 'SLE Security Attributes'.
 * //////////////////////////////////////////////////
 */
public class EE_SLE_SecAttributes implements ISLE_SecAttributes
{
    private static final Logger LOG = Logger.getLogger(EE_SLE_SecAttributes.class.getName());

    /**
     * The user name.
     */
    private String username;

    /**
     * The password.
     */
    private byte[] password;

    /**
     * The util factory instance to which this object refers to.
     */
    private EE_SLE_UtilityFactory utilFactory;
    
    
    /**
     * Constructor with no arguments
     */
    public EE_SLE_SecAttributes(EE_SLE_UtilityFactory utilFactory)
    {
    	this.utilFactory = utilFactory;
        this.username = "";
        this.password = null;
    }

    /**
     * Copy constructor
     */
    private EE_SLE_SecAttributes(EE_SLE_SecAttributes right)
    {
        if ((right.password != null) && (right.password.length != 0))
        {
            this.password = Arrays.copyOf(right.password, right.password.length);
        }
        else
        {
            this.password = null;
        }

        if (!right.username.isEmpty())
        {
            this.username = right.username;
        }
        
        this.utilFactory = right.utilFactory;
    }

    /**
     * @param iid
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IUnknown> T queryInterface(Class<T> iid)
    {
        if (iid == IUnknown.class)
        {
            return (T) this;
        }
        else if (iid == ISLE_SecAttributes.class)
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }

    /**
     * @param pwd
     */
    @Override
    public void setPassword(byte[] pwd)
    {
        if ((pwd != null) && (pwd.length != 0))
        {
            this.password = Arrays.copyOf(pwd, pwd.length);
        }
    }

    /**
     * @param credentials
     * @param acceptableDelay
     * @param sleVersion @since SLE V5 to support different encryption (SHA-1 & SHA-256)
     * @return
     */
    @Override
    public boolean authenticate(ISLE_Credentials credentials, int acceptableDelay, int sleVersion)
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Authenticating credentials " + credentials);
            LOG.fine("Username=" + this.username + ", password=" + Arrays.toString(this.password));
        }

        ISLE_UtilFactory pUtilFactory = null;
        ISLE_Time pTime = null;

        pUtilFactory = this.utilFactory;
        try
        {
            pTime = pUtilFactory.createTime(ISLE_Time.class);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
            return false;
        }

        // check diff between the current time and the time of the credentials
        ISLE_Time pTimeCredentials = credentials.getTimeRef();
        pTime.update();

        if (LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("pTime = " + pTime + ", pTimeCredentials = " + pTimeCredentials);
        }

        double diff = 0.0;
        diff = pTime.subtract(pTimeCredentials);
        if (diff < 0)
        {
            diff = -diff;
        }

        if (diff > acceptableDelay)
        {
            if (LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("Time difference = " + diff + " > Acceptable delay = " + acceptableDelay);
            }
            return false;
        }

        // encode and check
        byte[] hashBuffer = buildProtected(credentials, sleVersion);

        // compare the buffer with the one of the credentials
        byte[] pProtected = credentials.getProtected();

        EE_GenStrUtil.print("hashBuffer=", hashBuffer);
        EE_GenStrUtil.print("pProtected=", pProtected);

        return Arrays.equals(hashBuffer, pProtected);
    }

    /**
     * @param name
     */
    @Override
    public void setUserName(String name)
    {
        this.username = name;
    }

    /**
     * @param pwd
     */
    @Override
    public void setHexPassword(String pwd)
    {
        byte[] password = EE_GenStrUtil.hexToBin(pwd);
        if ((password != null) && (password.length != 0))
        {
            this.password = password;
        }
    }

    /**
     * @return
     */
    @Override
    public ISLE_Credentials generateCredentials(int sleVersion)
    {
        ISLE_Credentials pCredentials = null;
        ISLE_UtilFactory pUtilFactory = this.utilFactory;

        try
        {
            pCredentials = pUtilFactory.createCredentials(ISLE_Credentials.class);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }

        // Fill the time of the credentials
        ISLE_Time pTime = null;
        try
        {
            pTime = pUtilFactory.createTime(ISLE_Time.class);
        }
        catch (SleApiException e)
        {
            LOG.log(Level.FINE, "SleApiException ", e);
        }

        pTime.update();
        pCredentials.setTimeRef(pTime);

        // Fill the random of the credentials
        Random rand = new Random();
        long max = 2147483647;
        long randomNumber = (Math.abs(rand.nextLong())) % max;
        pCredentials.setRandomNumber(randomNumber);

        // Set the protected of the credentials
        byte[] hash_buffer = buildProtected(pCredentials, sleVersion);
        pCredentials.setProtected(hash_buffer);

        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Generating credentials with result " + pCredentials);
            LOG.fine("Username=" + this.username + ", password=" + Arrays.toString(this.password));
        }

        return pCredentials;
    }

    /**
     * @return
     */
    @Override
    public ISLE_SecAttributes copy()
    {
        return new EE_SLE_SecAttributes(this);
    }

    /**
     * @param credentials
     * @return
     */
    private byte[] encodeSecAttributes(ISLE_Credentials credentials)
    {
        byte[] buffer = null;
        long random = 0;
        int i = 0, l = 0;
        int maxSecAttrSize = 2500;

        buffer = new byte[maxSecAttrSize];

        // encode SEQUENCE
        buffer[l++] = 0x30;
        buffer[l++] = 0; // fix length field later

        // encode TimeCCSDS (OCTET STRING)
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.log(Level.FINE, "Credentials attributes to be encoded: " + credentials);
        }
        ISLE_Time pTime = credentials.getTimeRef();

        if (LOG.isLoggable(Level.FINE))
        {
            LOG.log(Level.FINE, "Time to use to extract CDS time: " + pTime);
        }
        byte[] time_cds = pTime.getCDS();

        if (LOG.isLoggable(Level.FINE))
        {
            LOG.log(Level.FINE, "Credentials attributes to be encoded: time_cds=" + Arrays.toString(time_cds)
                                + ", random=" + credentials.getRandomNumber());
            LOG.fine("Username=" + this.username + ", password=" + Arrays.toString(this.password));
        }
        buffer[l++] = 0x04;
        buffer[l++] = 0x08;

        System.arraycopy(time_cds, 0, buffer, l++, 8);
        l += 7;

        // encode random number (INTEGER)
        buffer[l++] = 0x02;
        random = credentials.getRandomNumber();
        i = 4;
        while (((random & 0xff800000L) == 0) || (random & 0xff800000L) == 0xff800000L)
        {
            if (i == 1)
            {
                break;
            }
            random <<= 8;
            i--;
        }

        buffer[l++] = (byte) i;

        for (; i > 0; i--)
        {
            buffer[l++] = (byte) (random >> 24);
            random <<= 8;
        }

        // encode username (VisibleString)
        buffer[l++] = 0x1A;
        buffer[l++] = (byte) this.username.length();
        for (i = 0; i < this.username.length(); i++)
        {
            buffer[l++] = (byte) this.username.charAt(i);
        }

        // encode password (OCTET STRING)
        buffer[l++] = 0x04;
        buffer[l++] = (byte) this.password.length;
        System.arraycopy(this.password, 0, buffer, l++, this.password.length);

        l += this.password.length - 1;

        // now fill the length field for the whole sequence
        buffer[1] = (byte) (l - 2);

        byte[] bufferToRtn = new byte[l];
        System.arraycopy(buffer, 0, bufferToRtn, 0, l);
        return bufferToRtn;
    }

    /**
     * @param credentials
     * @param sleVersion @since SLE V5 support encryption SHA-256.
     * @return
     */
    private byte[] buildProtected(ISLE_Credentials credentials, int sleVersion)
    {
        // encode the security attribute
        byte[] buffer = encodeSecAttributes(credentials);
        // call the hashCode function
        byte[] hashBuffer = null;

        MessageDigest md;
        try
        {
        	if(sleVersion <= 4){
        		md = MessageDigest.getInstance("SHA-1");
        	}
        	else{
        		md = MessageDigest.getInstance("SHA-256");
        	}
            md.reset();
            // md.update(buffer, 0, buffer.length);
            md.update(buffer);
            hashBuffer = md.digest();
        }
        catch (NoSuchAlgorithmException e)
        {
            LOG.log(Level.FINE, "NoSuchAlgorithmException ", e);
        }

        return hashBuffer;
    }

    @SuppressWarnings("unused")
    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    /**
     * @return
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.password);
        result = prime * result + ((this.username == null) ? 0 : this.username.hashCode());
        return result;
    }

    /**
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        EE_SLE_SecAttributes other = (EE_SLE_SecAttributes) obj;
        if (!Arrays.equals(this.password, other.password))
        {
            return false;
        }
        if (this.username == null)
        {
            if (other.username != null)
            {
                return false;
            }
        }
        else if (!this.username.equals(other.username))
        {
            return false;
        }
        return true;
    }

}
