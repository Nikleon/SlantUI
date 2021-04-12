package com.sprutuswesticus;

import java.io.Serializable;

public class Update implements Serializable {
    private static final long serialVersionUID = 2585894887518553527L;

    public final int x;
    public final int y;
    public final int orientation;

    public Update(int x, int y, int orientation) {
        this.x = x;
        this.y = y;
        this.orientation = orientation;
    }
}
