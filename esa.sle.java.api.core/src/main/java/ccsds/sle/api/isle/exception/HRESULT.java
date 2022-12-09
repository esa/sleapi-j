package ccsds.sle.api.isle.exception;

public enum HRESULT
{
    S_OK(0, "S_OK"),
    NOERROR(0, "NOERROR"),
    NO_ERROR(0, "NO_ERROR"),
    S_FALSE(1, "S_FALSE"),
    E_UNEXPECTED(0x8000FFFF, "E_UNEXPECTED"), /* Catastrophic failure */
    E_NOTIMPL(0x80000001, "E_NOTIMPL"), /* Not implemented */
    E_OUTOFMEMORY(0x80000002, "E_OUTOFMEMORY"), /* Ran out of memory */
    E_INVALIDARG(0x80000003, "E_INVALIDARG"), /*
                                               * One or more arguments are
                                               * invalid
                                               */
    E_NOINTERFACE(0x80000004, "E_NOINTERFACE"), /* No such interface supported */
    E_POINTER(0x80000005, "E_POINTER"), /* Invalid pointer */
    E_ABORT(0x80000007, "E_ABORT"), /* Operation aborted */
    E_FAIL(0x80000008, "E_FAIL"), /* Unspecified error */
    E_ACCESSDENIED(0x80000009, "E_ACCESSDENIED"), /* General access denied error */
    E_PENDING(0x8000000A, "E_PENDING"), /*
                                         * The data necessary to complete this
                                         * operation
                                         */
    /* is not yet available. */

    /*
     * ----------------------------------------------------------------- SLE API
     * specific codes
     * -----------------------------------------------------------------
     */

    // success code
    SLE_S_TRANSMITTED(0x00040200, "SLE_S_TRANSMITTED"),
    SLE_S_QUEUED(0x00040201, "SLE_S_QUEUED"),
    SLE_S_SUSPEND(0x00040202, "SLE_S_SUSPEND"),
    SLE_S_DISCARDED(0x00040203, "SLE_S_DISCARDED"),
    SLE_S_NOTDISCARDED(0x00040204, "SLE_S_NOTDISCARDED"),
    SLE_S_EOD(0x00040205, "SLE_S_EOD"),
    SLE_S_NULL(0x00040206, "SLE_S_NULL"),
    SLE_S_LOCKED(0x00040207, "SLE_S_LOCKED"),
    SLE_S_DEGRADED(0x00040208, "SLE_S_DEGRADED"),
    SLE_S_IGNORED(0x00040209, "SLE_S_IGNORED"),

    // error codes
    SLE_E_STATE(0x80040200, "SLE_E_STATE"),
    SLE_E_PROTOCOL(0x80040201, "SLE_E_PROTOCOL"),
    SLE_E_UNBINDING(0x80040202, "SLE_E_UNBINDING"),
    SLE_E_STOPPING(0x80040203, "SLE_E_STOPPING"),
    SLE_E_ABORTED(0x80040204, "SLE_E_ABORTED"),
    SLE_E_UNKNOWN(0x80040205, "SLE_E_UNKNOWN"),
    SLE_E_INVALIDPDU(0x80040206, "SLE_E_INVALIDPDU"),
    SLE_E_INVALIDID(0x80040207, "SLE_E_INVALIDID"),
    SLE_E_BADVALUE(0x80040208, "SLE_E_BADVALUE"),
    SLE_E_MISSINGARG(0x80040209, "SLE_E_MISSINGARG"),
    SLE_E_INCONSISTENT(0x8004020a, "SLE_E_INCONSISTENT"),
    SLE_E_RANGE(0x8004020b, "SLE_E_RANGE"),
    SLE_E_CONFIG(0x8004020c, "SLE_E_CONFIG"),
    SLE_E_OVERFLOW(0x8004020d, "SLE_E_OVERFLOW"),
    SLE_E_SUSPENDED(0x8004020e, "SLE_E_SUSPENDED"),
    SLE_E_DUPLICATE(0x8004020f, "SLE_E_DUPLICATE"),
    SLE_E_NOFILE(0x80040210, "SLE_E_NOFILE"),
    SLE_E_COMMS(0x80040211, "SLE_E_COMMS"),
    SLE_E_TYPE(0x80040212, "SLE_E_TYPE"),
    SLE_E_PORT(0x80040213, "SLE_E_PORT"),
    SLE_E_TIME(0x80040214, "SLE_E_TIME"),
    SLE_E_SEQUENCE(0x80040215, "SLE_E_SEQUENCE"),
    SLE_E_UNSOLICITED(0x80040216, "SLE_E_UNSOLICITED"),
    SLE_E_ROLE(0x80040217, "SLE_E_ROLE"),
    SLE_E_TIMERANGE(0x80040218, "SLE_E_TIMERANGE"),
    SLE_E_DIAGNOSTIC(0x80040219, "SLE_E_DIAGNOSTIC"),

    EE_S_SOWOULDBLOCK(0x40040401, "EE_S_SOWOULDBLOCK"),
    EE_S_OKPENDING(0x40040402, "EE_S_OKPENDING"),
    EE_S_SEQUENCEWAIT(0x40040403, "EE_S_SEQUENCEWAIT"),
    EE_E_SOFAILDEADLOCK(0x80040402, "EE_E_SOFAILDEADLOCK"),
    EE_E_SIFAILCANTREINIT(0x80040403, "EE_E_SIFAILCANTREINIT"),
    EE_E_SIALREADY(0x80040404, "EE_E_SIALREADY"),
    EE_E_SIDOESNOTEXIST(0x80040405, "EE_E_SIDOESNOTEXIST"),
    EE_E_PARTIALFAIL(0x80040406, "EE_E_PARTIALFAIL"),

    EE_S_CVTIME(0x40040410, "EE_S_CVTIME"),
    EE_E_CVINTR(0x80040410, "EE_E_CVINTR"),
    EE_E_REJECTED(0x80040411, "EE_E_REJECTED"),
    EE_E_NOSUCHEVENT(0x80040412, "EE_E_NOSUCHEVENT"),
    EE_E_TCPHARDERROR(0x80040413, "EE_E_TCPHARDERROR"),

    EE_E_NOPORT(0x80040414, "EE_E_NOPORT"),
    EE_E_CONTEXTMSG(0x80040415, "EE_E_CONTEXTMSG"),
    EE_E_NOTTML(0x80040416, "EE_E_NOTTML"),
    EE_E_NOTCTX(0x80040417, "EE_E_NOTCTX"),
    EE_E_VERSION(0x80040418, "EE_E_VERSION");

    private int code;

    private String msg;


    private HRESULT(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    public int getCode()
    {
        return this.code;
    }

    @Override
    public String toString()
    {
        return this.msg;
    }

    public static HRESULT getResultByCode(int code)
    {
        for (HRESULT e : values())
        {
            if (e.code == code)
            {
                return e;
            }
        }

        return null;
    }
}
