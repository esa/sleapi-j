package esa.sle.impl.api.apiut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import ccsds.sle.api.isle.exception.SleApiException;
import ccsds.sle.api.isle.it.SLE_GlobalRDN;
import ccsds.sle.api.isle.it.SLE_LocalRDN;
import ccsds.sle.api.isle.iutl.ISLE_SII;

public class EE_SLE_SIITest
{

    static ISLE_SII theSii;

    // static String siiStr =
    // "sagr=198.spack=dss16-PASS0000.fsl-fg=1.cltu=cltu1";
    static String siiStr2 = "sagr=198.spack=dss16-PASS0000.rsl-fg=1.rcf=onlc1";

    private static int sagrId = 52;

    private static int spackId = 53;

    private static int fslFgId = 14;

    private static int cltuId = 7;


    @BeforeClass
    public static void beforeClass() throws SleApiException
    {
        EE_SLE_UtilityFactory.initialiseInstance(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY, new EE_SLE_TimeSource());
        EE_SLE_UtilityFactory theFactory = EE_SLE_UtilityFactory.getInstance(EE_SLE_LibraryInstance.LIBRARY_INSTANCE_KEY);
        theSii = theFactory.createSII(ISLE_SII.class);
    }

    @Test(expected = SleApiException.class)
    public void testSet_AsciiFormEmptyString() throws SleApiException
    {
        String siiStr = "";
        theSii.setAsciiForm(siiStr);
    }

    @Test
    public void testSet_AsciiForm() throws SleApiException
    {
        theSii.setAsciiForm(siiStr2);
    }

    @Test
    public void testAddLocalRDNSLE_LocalRDN() throws SleApiException
    {
        SLE_LocalRDN lRDN = new SLE_LocalRDN("198", sagrId);
        theSii.addLocalRDN(lRDN);

        lRDN = new SLE_LocalRDN("dss16-PASS0000", spackId);
        theSii.addLocalRDN(lRDN);

        lRDN = new SLE_LocalRDN("1", fslFgId);
        theSii.addLocalRDN(lRDN);

        lRDN = new SLE_LocalRDN("cltu1", cltuId);
        theSii.addLocalRDN(lRDN);

        String siiStr = theSii.getAsciiForm();
        assertEquals(siiStr, siiStr);
    }

    @Test
    public void testNextGlobalRDN() throws SleApiException
    {
        theSii.reset();
        int[] globalOid = { 1, 3, 112, 4, 3, 1, 2, 0 };
        globalOid[7] = sagrId;

        ArrayList<SLE_GlobalRDN> lGDN = new ArrayList<SLE_GlobalRDN>();

        SLE_GlobalRDN lGDN1 = new SLE_GlobalRDN("198", globalOid);
        lGDN.add(lGDN1);
        theSii.addGlobalRDN(lGDN1);

        globalOid[7] = spackId;
        SLE_GlobalRDN lGDN2 = new SLE_GlobalRDN("dss16-PASS0000", globalOid);
        lGDN.add(lGDN2);
        theSii.addGlobalRDN(lGDN2);

        globalOid[7] = fslFgId;
        SLE_GlobalRDN lGDN3 = new SLE_GlobalRDN("1", globalOid);
        lGDN.add(lGDN3);
        theSii.addGlobalRDN(lGDN3);

        globalOid[7] = cltuId;
        SLE_GlobalRDN lGDN4 = new SLE_GlobalRDN("cltu1", globalOid);
        lGDN.add(lGDN4);
        theSii.addGlobalRDN(lGDN4);

        for (int i = 0; i < 4; i++)
        {
            SLE_GlobalRDN gRDNNext = theSii.nextGlobalRDN();
            assertEquals(gRDNNext, lGDN.get(i));
        }
    }

    @Test
    public void testGet_AsciiForm() throws SleApiException
    {
        // theSii.setInitialFormat();
        theSii.setAsciiForm(siiStr2);
        String siiStr = theSii.getAsciiForm();
        assertTrue(siiStr.equals(siiStr));
    }

}
