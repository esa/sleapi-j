package ccsds.sle.api.isrv.ifsp.types;

public class FSP_AbsolutePriority
{
    /**
     * 0 to 63
     */
    private int mapOrVc;

    /**
     * 1 (highest) to 64 (lowest)
     */
    private int priority;


    /**
     * Default constructor, the object attributes might be changed during
     * operations.
     */
    public FSP_AbsolutePriority()
    {
        this.mapOrVc = 0;
        this.priority = 64;
    }

    /**
     * Constructor FSP_AbsolutePriority.
     * 
     * @param mapOrVc
     * @param priority
     */
    public FSP_AbsolutePriority(int mapOrVc, int priority)
    {
        if (mapOrVc < 0 && mapOrVc > 63)
        {
            throw new IllegalArgumentException(" mapOrVc not in the interval =" + mapOrVc);
        }
        if (priority < 1 && priority > 64)
        {
            throw new IllegalArgumentException(" priority not in the interval =" + mapOrVc);
        }
        this.mapOrVc = mapOrVc;
        this.priority = priority;
    }

    /**
     * Sets mapOrVc.
     * 
     * @param mapOrVc
     */
    public void setMapOrVc(int mapOrVc)
    {
        if (mapOrVc < 0 && mapOrVc > 63)
        {
            throw new IllegalArgumentException(" mapOrVc not in the interval =" + mapOrVc);
        }
        this.mapOrVc = mapOrVc;
    }

    /**
     * Sets the priority.
     * 
     * @param priority
     */
    public void setPriority(int priority)
    {
        if (priority < 1 && priority > 64)
        {
            throw new IllegalArgumentException(" priority not in the interval =" + this.mapOrVc);
        }
        this.priority = priority;
    }

    /**
     * Gets the mapOrVc.
     * 
     * @return
     */
    public int getMapOrVc()
    {
        return this.mapOrVc;
    }

    /**
     * Gets the priority.
     * 
     * @return
     */
    public int getPriority()
    {
        return this.priority;
    }

}
