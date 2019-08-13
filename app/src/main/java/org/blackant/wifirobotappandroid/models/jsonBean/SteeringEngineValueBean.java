package org.blackant.wifirobotappandroid.models.jsonBean;

public class SteeringEngineValueBean {

    /**
     * x_axis :
     * y_axis :
     */

    private int x_axis;
    private int y_axis;

    public SteeringEngineValueBean(int x_axis, int y_axis) {
        this.x_axis = x_axis;
        this.y_axis = y_axis;
    }

    public int getX_axis() {
        return x_axis;
    }

    public void setX_axis(int x_axis) {
        this.x_axis = x_axis;
    }

    public int getY_axis() {
        return y_axis;
    }

    public void setY_axis(int y_axis) {
        this.y_axis = y_axis;
    }
}
