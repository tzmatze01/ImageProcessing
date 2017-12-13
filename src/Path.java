public class Path {

    // Border is always between white pixel and black pixel
    //BORDER(0, 0, 0);

    private int blackPixel;
    private int whitePixel;
    private int direction;


    private boolean outerBorder = true;
    private String id;

    Path(int blackPixel, int whitePixel, int direction)
    {
        this.blackPixel = blackPixel;
        this.whitePixel = whitePixel;
        this.direction = direction;
    }

    private void setID()
    {
        this.id = ""+blackPixel+""+whitePixel;
    }

    public String getID()
    {
        setID();
        return this.id;
    }



    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setBlackPixel(int blackPixel) {
        this.blackPixel = blackPixel;
    }

    public void setWhitePixel(int whitePixel) {
        this.whitePixel = whitePixel;
    }

    public void setBorder(boolean isOuterBorder)
    {
        this.outerBorder = isOuterBorder;
    }

    public boolean isOuterBorder()
    {
        return this.outerBorder;
    }

    public int getWhitePixel()
    {
        return this.whitePixel;
    }

    public int getBlackPixel()
    {
        return this.blackPixel;
    }

    public String toString()
    {
        return "blackPos: "+blackPixel+" whitePos: "+whitePixel+" kernel: "+direction;
    }

    public Path getObject()
    {
        return this;
    }

}
