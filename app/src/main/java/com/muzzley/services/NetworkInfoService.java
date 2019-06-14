package com.muzzley.services;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.muzzley.model.discovery.Init;
import com.muzzley.model.discovery.NetworkInfo;
import com.muzzley.model.discovery.Param;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Enumeration;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by ruigoncalo on 15/12/14.
 */
public class NetworkInfoService {

    @Inject Context context;
    @Inject Gson gson;

    @Inject
    public NetworkInfoService() {
    }

    public String getInfo(Param params) throws UnknownHostException, SocketException {
        String info = null;
        switch (params.getpInterface()) {
            case Init.CONNECTION_TYPE_WIFI:
                info = getWifiInfoUsingMacAddress(params);
                break;
            case Init.CONNECTION_TYPE_CELL:
                //
                break;
        }

        return info;
    }

    private String getWifiInfoUsingIp(Param params) throws UnknownHostException, SocketException {

        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = manager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        // Convert little-endian to big-endianif needed
        Timber.d("Order: " + ByteOrder.nativeOrder().toString());
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] bytes = BigInteger.valueOf(ipAddress).toByteArray();
        InetAddress addr = InetAddress.getByAddress(bytes);
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(addr);

        if (networkInterface != null) {
            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                if (address != null) {
                    Timber.d("IP=" + (address.getAddress() != null ? address.getAddress().toString() : "null"));
                    Timber.d("HostName=" + (address.getAddress() != null ? address.getAddress().getHostName() : "null"));
                    Timber.d("Netmask=" + address.getNetworkPrefixLength());
                    Timber.d("Broadcast=" + (address.getBroadcast() != null ? address.getBroadcast().toString() : "null"));
                }
            }
        }

        return "";
    }

    private String getWifiInfoUsingMacAddress(Param params) throws UnknownHostException, SocketException {

        String result = "";

        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = manager.getConnectionInfo();

        int ipAddress = wifiInfo.getIpAddress();
        Timber.d("Order: " + ByteOrder.nativeOrder().toString());
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] bytes = BigInteger.valueOf(ipAddress).toByteArray();
        InetAddress wifiAddr = InetAddress.getByAddress(bytes);
        Timber.d("wifi Address ip=" + wifiAddr.toString());
        Timber.d("wifi Address hostname=" + wifiAddr.getHostName());
        Timber.d("wifi Address hostaddress=" + wifiAddr.getHostAddress());

        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {

            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {

                Timber.d("interface Adress hostaddress=" + address.getAddress().getHostAddress());

//                if (address != null ) {
                    String ip = null;
                    String prefixLength = null;
                    String broadcast = null;
                    String ipv6 = null;
                    String prefixLengthv6 = null;

                    // filter ipv4
                    if (address.getAddress() instanceof Inet4Address) {

                        if (params.isIp()) {
                            ip = getIp(address);
                        }

                        if (params.isBroadcast()) {
                            broadcast = getBroadcast(address);
                        }

                        if (params.isPrefixLength()) {
                            prefixLength = getPrefixLength(address);
                        }
                    }

                    // filter ipv6
                    if (address.getAddress() instanceof Inet6Address) {
                        ipv6 = getIpv6(address);
                        prefixLengthv6 = getPrefixLength(address);
                    }

                    boolean matched = normalize(wifiAddr.getHostAddress()).equals(normalize(address.getAddress().getHostAddress()));
                    if (matched) {
                        NetworkInfo info = new NetworkInfo(ip, prefixLength, broadcast, ipv6, prefixLengthv6);
                        result = gson.toJson(info);
                        Timber.d("Result matched =" + result);
                        return result;
                    }
//                }
            }
        }
        Timber.d("Result not matched");
        return result;
    }


    String normalize(String addrStr) {
        if (addrStr == null) {
            return "null";
        }
        if (addrStr.startsWith("/")) {
            addrStr = addrStr.substring(1);
        }
        if(addrStr.contains("%")){
            addrStr = addrStr.split("%")[0];
        }
        return addrStr;
    }

    private String getIpv6(InterfaceAddress address){
        String ip = (address.getAddress() != null ? address.getAddress().toString() : "null");
        if (ip.startsWith("/")) {
            ip = ip.substring(1);
        }

        if(ip.contains("%")){
            ip = ip.split("%")[0];
        }

        Timber.d("IPv6=" + ip);
        return ip;
    }

    private String getIp(InterfaceAddress address) {
        String ip = (address.getAddress() != null ? address.getAddress().toString() : "null");
        if (ip.startsWith("/")) {
            ip = ip.substring(1);
        }

        Timber.d("IP=" + ip);
        return ip;
    }

    private String getIp(InetAddress address) {
        String ip = (address != null ? address.toString() : "null");
        if (ip.startsWith("/")) {
            ip = ip.substring(1);
        }
        Timber.d("IP=" + ip);
        return ip;
    }

    private String getBroadcast(InterfaceAddress address) {
        String broadcast = (address.getBroadcast() != null ? address.getBroadcast().toString() : "null");
        if (broadcast.startsWith("/")) {
            broadcast = broadcast.substring(1);
        }

        Timber.d("Broadcast=" + broadcast);
        return broadcast;
    }

    private String getPrefixLength(InterfaceAddress address) {
        String netmask = String.valueOf(address.getNetworkPrefixLength());
        Timber.d("Netmask=" + netmask);
        return String.valueOf(address.getNetworkPrefixLength());
    }

    private byte[] macAddressToByteArray(String macString) {
        String[] mac = macString.split("[:\\s-]");
        byte[] macAddress = new byte[6];
        for (int i = 0; i < mac.length; i++) {
            macAddress[i] = Integer.decode("0x" + mac[i]).byteValue();
        }

        return macAddress;
    }


}
