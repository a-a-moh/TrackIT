package com.example.trackit;

public class DriverRoute {
    private String packageID;
    private String customerName;
    private String customerAddress;

    public DriverRoute(String packageID, String customerName, String customerAddress) {
        this.packageID = packageID;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
    }

    public String getPackageID() {
        return packageID;
    }

    public void setPackageID(String packageID) {
        this.packageID = packageID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }
}
