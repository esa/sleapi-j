package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.io.InputStream;

import ccsds.sle.api.isle.exception.SleApiException;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_IntegralEncoder;

public class EE_APIPX_PDUMessageFactory implements ITMLMessageFactory
{
    @Override
    public EE_APIPX_TMLMessage createTmlMessage(byte[] initialEightBytes, InputStream is) throws SleApiException,
                                                                                         IOException
    {
        // extract the body length from header
        int length = (int) EE_IntegralEncoder.decodeUnsignedMSBFirst(initialEightBytes, 4, 4);

        // extract the body 
        // EGSINTEG-3861 : Properly reading data from a socket 
        byte[] body = new byte[length];
		
		int dataRead = 0;
		while(dataRead < length) {
			int currentlyRead = is.read(body, dataRead, length - dataRead);
			if(currentlyRead <= 0) {
				return null;
			}
			dataRead += currentlyRead;
		}
		
        EE_GenStrUtil.print("Read from socket: ", body);

        // EE_GenStrUtil.print("Creating the PDU message for: ", body);

        // create the pdu message
        return new EE_APIPX_PDUMessage(body);
    }

    public EE_APIPX_TMLMessage createPDUMessage(byte[] msgInBytes)
    {
        return new EE_APIPX_PDUMessage(msgInBytes);
    }
}
