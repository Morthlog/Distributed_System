package lib.shared;

import java.io.Serializable;

public class RatingChange implements Serializable, StoreNameProvider
{
    String storeName;
    int oldRating;
    int newRating;

    public RatingChange(String storeName, int oldRating, int newRating)
    {
        this.storeName = storeName;
        this.oldRating = oldRating;
        this.newRating = newRating;
    }

    public void setStoreName(String storeName)
    {
        this.storeName = storeName;
    }

    public void setOldRating(int oldRating)
    {
        this.oldRating = oldRating;
    }

    public void setNewRating(int newRating)
    {
        this.newRating = newRating;
    }

    public String getStoreName()
    {
        return storeName;
    }

    public int getOldRating()
    {
        return oldRating;
    }

    public int getNewRating()
    {
        return newRating;
    }
}
