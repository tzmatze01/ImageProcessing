public enum Border
{
    // potracekernel StartPos (changes during rotation) determines which pixel to choose next
    // therefore the enum values should be in the same order

    // down left right up
    OUTER_DOWN(0, 0),
    OUTER_LEFT(0, 1),
    OUTER_RIGHT(0, 2),
    OUTER_UP(0, 3),

    INNER_DOWN(1, 0),
    INNER_LEFT(1, 1),
    INNER_RIGHT(1, 2),
    INNER_UP(1, 3);

    private int orientation;
    private int borderType;

    private int pixelPos;

    Border()
    {
        this.orientation = 0;
        this.borderType = 0;
    }

    Border(int orientation, int borderType)
    {
        this.orientation = orientation;
        this.borderType = borderType;
    }

    public void setPixelPos(int pos)
    {
        this.pixelPos = pos;
    }

    public void setOrientation(int value)
    {
        this.orientation = value;
    }

    public void setBorderType(int value)
    {
        this.borderType = value;
    }

    public int getPixelPos()
    {
        return this.pixelPos;
    }

    public int getOrientation()
    {
        return this.orientation;
    }

    public int getBorderType()
    {
        return this.borderType;
    }

    public String toString()
    {
        return "PixelPos: "+this.pixelPos+" Orientation: "+orientation+" BorderType: "+borderType;
    }
}