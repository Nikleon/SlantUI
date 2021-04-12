package com.sprutuswesticus;

import java.io.Serializable;

public class Update implements Serializable {
    private static final long serialVersionUID = 2585894887518553527L;

    public final int r;
    public final int c;
    public final int orientation;

    public Update(int r, int c, int orientation) {
        this.r = r;
        this.c = c;
        this.orientation = orientation;
    }
}
