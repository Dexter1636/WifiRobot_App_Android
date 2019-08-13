package org.blackant.wifirobotappandroid.models.jsonBean;

public class ResponseStatusBean {
    /**
     * status :
     */

    private String status;

    public ResponseStatusBean(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
