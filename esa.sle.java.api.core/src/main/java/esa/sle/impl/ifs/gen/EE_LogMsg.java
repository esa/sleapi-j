package esa.sle.impl.ifs.gen;

public enum EE_LogMsg
{

    PXDBLISTELEMENTFOUND_1(2000),
    PXDBVALUENOTSET_1(2001),
    PXDBVALUESET_1(2002),
    PXDBUNKNOWNKEYWORD_1(2003),
    PXDBDUPLICATE_2(2004),

    EE_SE_LMBase(1500),
    EE_OP_LMBase(4000),
    EE_SE_LM_NoSuchFile(EE_SE_LMBase.getCode() + 1),
    EE_SE_LM_OpenDbFailed(EE_SE_LMBase.getCode() + 2),
    EE_SE_LM_ParsingError(EE_SE_LMBase.getCode() + 3),
    EE_SE_LM_ConfigError(EE_SE_LMBase.getCode() + 4),
    EE_SE_LM_AddPxyRejected(EE_SE_LMBase.getCode() + 5),
    EE_SE_LM_ProtIdNotSupported(EE_SE_LMBase.getCode() + 6),
    EE_SE_LM_DuplicateProtId(EE_SE_LMBase.getCode() + 7),
    EE_SE_LM_ProxyNotRegistered(EE_SE_LMBase.getCode() + 8),
    EE_SE_LM_ProxyNotStarted(EE_SE_LMBase.getCode() + 9),
    EE_SE_LM_NoProxyStarted(EE_SE_LMBase.getCode() + 10),
    EE_SE_LM_AccessViolation(EE_SE_LMBase.getCode() + 11),
    EE_SE_LM_StateChange(EE_SE_LMBase.getCode() + 12),
    EE_SE_LM_PxyProtocolError(EE_SE_LMBase.getCode() + 13),
    EE_SE_LM_ProtocolError(EE_SE_LMBase.getCode() + 14),
    EE_SE_LM_ReturnTimerExpired(EE_SE_LMBase.getCode() + 15),
    EE_SE_LM_PpEnds(EE_SE_LMBase.getCode() + 16),
    EE_SE_LM_PpEndsOnRequest(EE_SE_LMBase.getCode() + 17),
    EE_SE_LM_BufferQueued(EE_SE_LMBase.getCode() + 18),
    EE_SE_LM_BufferXmitted(EE_SE_LMBase.getCode() + 19),
    EE_SE_LM_BufferDiscarded(EE_SE_LMBase.getCode() + 20),
    EE_SE_LM_LatencyTimerExpired(EE_SE_LMBase.getCode() + 21),
    EE_SE_LM_SSRRequested(EE_SE_LMBase.getCode() + 22),
    EE_SE_LM_SendingPeriodicReport(EE_SE_LMBase.getCode() + 23),
    EE_SE_LM_UnexpectedTbPdu(EE_SE_LMBase.getCode() + 24),
    EE_SE_LM_TimerAborted(EE_SE_LMBase.getCode() + 25),
    EE_SE_LM_EmptyBufferRec(EE_SE_LMBase.getCode() + 26),
    EE_SE_LM_InconsistentInvArgs(EE_SE_LMBase.getCode() + 27),
    EE_SE_LM_InconsistentRtnArgs(EE_SE_LMBase.getCode() + 28),
    EE_SE_LM_IncompatibleInvPDU(EE_SE_LMBase.getCode() + 29),
    EE_SE_LM_IncompatibleRtnPDU(EE_SE_LMBase.getCode() + 30),
    EE_SE_LM_BindVersionMismatch(EE_SE_LMBase.getCode() + 31),
    EE_SE_LM_UnsolicitedPdu(EE_SE_LMBase.getCode() + 32),

    TMLLISTENUNK(3000),
    TMLLISTENFAIL(3001),
    TMLCONNECTFAILALL(3002),
    TMLCONNECTFAIL(3003),
    TMLWRITECTXFAIL(3004),
    TMLREADCTXFAIL0(3005),
    TMLREADCTXFAIL1(3006),
    TMLREADCTXFAIL2(3007),
    TMLESTTIMEOUT(3008),
    TMLCONNECTEDTIMEOUT(3009),

    TMLCLOSETIMEOUT(3010),
    TMLCLOSINGDATA(3011),
    TMLCLOSINGURG(3012),
    TMLCLOSINGHARD(3013),

    TMLCONNECTEDCLOSE(3014),
    TMLCONNECTEDHARD(3015),
    TMLCONNECTEDBADHDR(3016),

    TMLABORTRCVTIMEOUT(3017),
    TMLABORTRCVCLOSED(3018),
    TMLABORTRCVHARD(3019),
    TMLABORTRCV(3022),

    TMLCROSSEDHARD(3023),
    TMLCROSSEDCLOSED(3024),
    TMLCROSSEDTIMEOUT(3025),
    TMLCROSSEDURG(3026),
    TMLABORTFAIL1(3027),
    TMLABORTFAIL2(3028),
    TMLSENDFAIL(3029),
    TMLABORTRCVTOOLATE(3030),

    TMLABORTHARD(3020),
    TMLABORTTIMEOUT(3021),
    TMLCLOSELISTENFAIL(3031),
    TMLACCEPTFAIL(3032),
    TMLLISTENABORTED(3033),
    TMLBADINVOCATION(3034),
    TMLESTRESPONDERHARD(3035),
    TMLESTRESPONDERCLOSED(3036),

    TMLTR_STARTLISTEN(3050),
    TMLTR_STOPLISTEN(3051),
    TMLTR_NEWCONN(3052),
    TMLTR_SENDCONNECT(3053),
    TMLTR_ONCONNECTED(3054),
    TMLTR_ONPDUTRANSMITTED(3055),
    TMLTR_ONHBTTRANSMITTED(3056),
    TMLTR_ONDATAWRITTEN(3057),
    TMLTR_TIMEOUTHBT(3058),
    TMLTR_SENDPDU(3059),
    TMLTR_CANREAD(3060),
    TMLTR_HBTREAD(3061),
    TMLTR_PDUREAD(3062),
    TMLTR_READYTORECEIVEPDU(3063),
    TMLTR_NOTREADYTORECEIVE(3064),
    TMLTR_SENDABORT(3065),
    TMLTR_SENDDISCONNECT(3066),
    TMLTR_SENDRESET(3067),
    TMLTR_CANREADABORTING(3068),
    TMLTR_LASTPDU(3069),
    TMLTR_LASTPDUWRITTEN(3070),
    TMLTR_CONNCLOSEDNOMINAL(3071),
    TMLTR_BADCONTEXT(3073),
    TMLTR_BADHDRREAD(3074),
    TMLTR_ESTABLISHCLOSED(3075),
    TMLTR_ESTABLISHTIMEOUT(3076),
    TMLTR_ESTABLISHHARDERROR(3077),
    TMLTR_HDRREAD(3078),
    TMLTR_TIMEOUT(3079),
    TMLTR_CONTEXTRCVD(3080),
    TMLTR_IOEVENT(3081),
    TMLTR_REQIOEVENT(3082),
    TMLTR_CANCELIOEVENT(3083),
    TMLTR_TRACEON(3084),
    TMLTR_TRACEOFF(3085),

    EE_OP_LM_MissingArg(EE_OP_LMBase.getCode() + 1),
    EE_OP_LM_Inconsistent(EE_OP_LMBase.getCode() + 2),
    EE_OP_LM_TimeRange(EE_OP_LMBase.getCode() + 3),
    EE_OP_LM_Range(EE_OP_LMBase.getCode() + 4),
    EE_OP_LM_InvalidID(EE_OP_LMBase.getCode() + 5),
    EE_OP_LM_InvalidMode(EE_OP_LMBase.getCode() + 6);

    private int code;


    private EE_LogMsg(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return this.code;
    }

}
