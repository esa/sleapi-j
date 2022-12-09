package ccsds.sle.api.isle.iutl;

import ccsds.sle.api.isle.iscm.IUnknown;

/**
 * Objects implementing this interface hold the user name and the password
 * required for generating and authenticating credentials. For the password the
 * object does not make any assumptions on the format, size, or encoding. It
 * simply stores the sequence of bytes passed to it. The interface provides
 * methods to generate credentials and to authenticate credentials. The
 * procedure applied for both methods is specified in chapter 4. Because the
 * object stores sensitive information is does not provides methods for read
 * access and does not support printing of the contents.
 * 
 * @version: 1.0, October 2015
 */
public interface ISLE_SecAttributes extends IUnknown
{
    /**
     * Sets the user name.
     * 
     * @param name
     */
    void setUserName(String name);

    /**
     * Sets the password.
     * 
     * @param pwd
     */
    void setPassword(byte[] pwd);

    /**
     * Sets Hex password.
     * 
     * @param pwd
     */
    void setHexPassword(String pwd);

    /**
     * Generate credentials.
     * @param sleVersion @since SLE V5 to support encryption SHA-256
     * @return
     */
    ISLE_Credentials generateCredentials(int sleVersion);

    /**
     * Authenticate.
     * 
     * @param credentials
     * @param acceptableDelay
     * @param sleVersion @since SLE V5 to support encryption SHA-256
     * @return
     */
    boolean authenticate(ISLE_Credentials credentials, int acceptableDelay, int sleVersion);

    /**
     * Copies the object.
     * 
     * @return
     */
    ISLE_SecAttributes copy();

    /**
     * @param o
     * @return
     */
    @Override
    boolean equals(Object o);

    /**
     * @return
     */
    @Override
    int hashCode();
}
