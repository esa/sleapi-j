package ccsds.sle.api.isle.it;

public enum SLE_TimeRes
{
    sleTR_minutes(0),
    sleTR_seconds(1),
    sleTR_hundredMilliSec(2),
    sleTR_tenMilliSec(3),
    sleTR_milliSec(4),
    sleTR_hundredMicroSec(5),
    sleTR_tenMicroSec(6),
    sleTR_microSec(7),
    sleTR_hundredNanoSec(8),
    sleTR_tenNanoSec(9),
    sleTR_nanoSec(10),
    sleTR_hundredPicoSec(11),
    sleTR_tenPicoSec(12),
    sleTR_picoSec(13);

    private int code;


    /**
     * Constructor SLE_TimeRes.
     * 
     * @param code
     */
    private SLE_TimeRes(int code)
    {
        this.code = code;
    }

    /**
     * Gets the code.
     * 
     * @return
     */
    public int getCode()
    {
        return this.code;
    }
}
