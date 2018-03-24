package com.pnf.bt.lib;

public class BluetoothDeviceData {
    String deviceAddress;
    String deviceName;

    public BluetoothDeviceData() {
        this.deviceName = "";
        this.deviceAddress = "";
        this.deviceName = "";
        this.deviceAddress = "";
    }

    public BluetoothDeviceData(String _name, String _address) {
        this.deviceName = "";
        this.deviceAddress = "";
        this.deviceName = _name;
        this.deviceAddress = _address;
    }
}
