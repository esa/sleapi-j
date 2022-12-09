package esa.sle.impl.api.apipx.pxtml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ccsds.sle.api.isle.exception.SleApiException;
import esa.sle.impl.api.apipx.pxtml.types.EE_APIPX_TMLErrors;
import esa.sle.impl.ifs.gen.EE_GenStrUtil;
import esa.sle.impl.ifs.gen.EE_Reference;

public class EE_APIPX_TMLMessageFactory {
	private static final Logger LOG = Logger
			.getLogger(EE_APIPX_TMLMessageFactory.class.getName());

	public Map<Integer, ITMLMessageFactory> factories = new TreeMap<Integer, ITMLMessageFactory>();

	private final byte[] buf = new byte[8];

	private final boolean logFine;

	public EE_APIPX_TMLMessageFactory() {
		this.factories.put(0x03, new EE_APIPX_HBMessageFactory());
		this.factories.put(0x01, new EE_APIPX_PDUMessageFactory());
		this.factories.put(0x02, new EE_APIPX_CtxMessageFactory());
		this.logFine = LOG.isLoggable(Level.FINE);
	}

	public EE_APIPX_TMLMessage decodeFrom(InputStream is,
			EE_Reference<EE_APIPX_TMLErrors> error) throws IOException,
			SleApiException {
		int dataRead = 0;
		try {

			// int dataRead = 0;

			// read = is.read(this.buf, 0, this.buf.length);
			// EGSINTEG-3861 : Properly reading data from a socket
			// if (read < 0)
			// {
			// error.setReference(EE_APIPX_TMLErrors.eeAPIPXtml_unexpectedClose);
			// throw new IOException("Received <0 bytes from socket");
			// }
			final int length = this.buf.length;
			if(this.logFine == true) {
				LOG.log(Level.FINE, "Thread: " + Thread.currentThread().getId()
						+ " initial length " + length);
			}
			while (dataRead < length) {
				if(this.logFine == true) {
					LOG.log(Level.FINE, "Thread: " + Thread.currentThread().getId()
							+ " begin of while dataRead " + dataRead);
				}
				
				int left = (length - dataRead);
				
				if(this.logFine == true) {
					LOG.log(Level.FINE, "Thread: " + Thread.currentThread().getId()
							+ " left " + left);
				}
				
				int currentlyRead = is.read(this.buf, dataRead, left);
				
				if(this.logFine == true) {
					LOG.log(Level.FINE, "Thread: " + Thread.currentThread().getId()
							+ " currentlyRead " + currentlyRead);
				}
				
				// SLEAPIJ-33 Peer Abort not read from network
				if(dataRead == 1 && currentlyRead <=0) {
					break; // PEER-ABORT
				}
				
				if (currentlyRead <= 0) {
					return null;
				}
				
				dataRead += currentlyRead;
				
				if(this.logFine == true) {
					LOG.log(Level.FINE, "Thread: " + Thread.currentThread().getId()
							+ " end of while dataRead " + dataRead);
				}
			}
			if (dataRead < 0) {
				error.setReference(EE_APIPX_TMLErrors.eeAPIPXtml_unexpectedClose);
				throw new IOException("Received <0 bytes from socket");
			}

		} catch (IOException e) {
			error.setReference(EE_APIPX_TMLErrors.eeAPIPXtml_unexpectedClose);
			throw e;
		}

		if (LOG.isLoggable(Level.FINEST))
        {
			EE_GenStrUtil.print("Read from socket: ", this.buf);
        }
		
		if (dataRead == 8) {
			if (this.logFine) {
				LOG.finest("8 bytes read from socket");
			}

			ITMLMessageFactory selectedFactory = this.factories
					.get((int) this.buf[0]);
			if (selectedFactory != null) {
				return selectedFactory.createTmlMessage(this.buf, is);
			} else {
				// the 8 bytes read from the input stream do not represent a
				// valid TML message
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.finest("the 8 bytes read from the input stream do not represent a valid TML message. Factory selector: " + (int) this.buf[0]);
				}
				error.setReference(EE_APIPX_TMLErrors.eeAPIPXtml_badTMLMsg);
				return null;
			}
		} else {
			if (dataRead == 1) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.finest("1 byte read from the socket - assume a PEER ABORT received with diagnostic "
							+ this.buf[0]);
				}
				// assume buf[0] peer abort diagnostic
				return new EE_APIPX_UrgentByteMessage(this.buf[0]);

			} else {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.finest(">1 and < 8 byte read from the socket - assume a PEER ABORT received with diagnostic "
							+ this.buf[dataRead - 1]);
				}
				// assume buf[read - 1] peer abort diagnostic
				return new EE_APIPX_UrgentByteMessage(this.buf[dataRead - 1]);
			}
		}
	}

	public EE_APIPX_TMLMessage createPDUMsg(byte[] buffer) {
		EE_APIPX_PDUMessageFactory pduMsgFactory = (EE_APIPX_PDUMessageFactory) this.factories
				.get(0x01);
		return pduMsgFactory.createPDUMessage(buffer);
	}

	public EE_APIPX_TMLMessage createHBMsg() {
		EE_APIPX_HBMessageFactory hbMsgFactory = (EE_APIPX_HBMessageFactory) this.factories
				.get(0x03);
		return hbMsgFactory.createTmlMessage();
	}

	public EE_APIPX_TMLMessage createCtxMsg(int hbt, int deadFactor) {
		EE_APIPX_CtxMessageFactory cxtMessageFactory = (EE_APIPX_CtxMessageFactory) this.factories
				.get(0x02);
		return cxtMessageFactory.createTmlMessage(hbt, deadFactor);
	}
}
