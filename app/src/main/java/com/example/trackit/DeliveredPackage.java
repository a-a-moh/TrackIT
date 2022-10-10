package com.example.trackit;

public class DeliveredPackage {
    private String packageID;
    private String date;
    private String driver;

    public DeliveredPackage(String packageID, String date, String driver) {
        this.packageID = packageID;
        this.date = date;
        this.driver = driver;
    }

    public String getPackageID() {
        return packageID;
    }

    public void setPackageID(String packageID) {
        this.packageID = packageID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
