package com.muzzley.services;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.muzzley.model.discovery.Param;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by ruigoncalo on 04/07/14.
 */
public class UPnPService {

    private final static String IP = "239.255.255.250";
    private final static String PORT = "1900";
    private static final String DEFAULT_SEARCH_TYPE = "urn:schemas-upnp-org:device:InternetGatewayDevice:1";
    private static final String DEFAULT_MX_TYPE = "3";

    @Inject Context context;

    private WifiManager.MulticastLock lock;
    private int mx;
    private String st;

    @Inject
    public UPnPService() {
    }

    private void getParams(Param params){
        String st = params.getSt();
        String mx = params.getMx();
        Timber.d("Values to UPnP: st=" + st + " mx=" + mx);

        this.mx = Integer.valueOf(mx == null ? DEFAULT_MX_TYPE : mx);
        this.st = st == null ? DEFAULT_SEARCH_TYPE : st;
    }

    private String getMessage(){
        return "M-SEARCH * HTTP/1.1" + "\r\n"
                        + "HOST: " + IP + ":" + PORT + "\r\n"
                        + "ST: "+ this.st + "\r\n"
                        + "MAN: \"ssdp:discover\"" + "\r\n"
                        + "MX: " + this.mx + "\r\n" + "\r\n";
    }

    public List<String> discover(Param params) {
        List<String> gateways = new ArrayList<>();
        MulticastSocket socket = null;

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifi != null){
            lock = wifi.createMulticastLock("Log_Tag");
            lock.acquire();
        }

        getParams(params);

        try {
            socket = new MulticastSocket(null);
            socket.setBroadcast(true);
            socket.bind(null);
            socket.setSoTimeout(mx * 1000);

            String packet = getMessage();
            Timber.d("Packet: " + packet + " timeout=" + socket.getSoTimeout());

            byte[] data = packet.getBytes();
            InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(IP), Integer.valueOf(PORT));
            socket.send(new DatagramPacket(data, data.length, socketAddress));

            receive(socket, gateways);

        } catch (IOException e) {
            Timber.d("Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if(socket != null) {
                socket.disconnect();
                socket.close();

                if(lock != null){
                    lock.release();
                }
            }
        }

        int counter = 1;
        for(String s : gateways) {
            Timber.d("\n" + counter + ":\n" + s);
            counter++;
        }

        return gateways;
    }

    public void receive(MulticastSocket socket, List<String> gateways) {
        boolean waitingPacket = true;
        while (waitingPacket) {
            try {
                byte[] buf = new byte[1536];
                DatagramPacket input = new DatagramPacket(buf, buf.length);
                socket.receive(input);
                byte[] receivedData = new byte[input.getLength()];

                System.arraycopy(input.getData(), 0, receivedData, 0, input.getLength());

                String data = new String(receivedData);
                gateways.add(data);

            } catch (SocketTimeoutException ste) {
                Timber.d("timeout");
                waitingPacket = false;
            } catch (IOException e) {
                Timber.d("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
