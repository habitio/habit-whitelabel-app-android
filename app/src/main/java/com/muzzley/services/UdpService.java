package com.muzzley.services;

import com.muzzley.model.discovery.Param;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by ruigoncalo on 16/12/14.
 */
public class UdpService {

    @Inject
    public UdpService() {

    }

    public List<String> sendPacket(Param params) {
        List<String> responses = new ArrayList<>();
        DatagramSocket socket = null;

        if (udpParamsAreValid(params)) {

            Timber.d("Will send packet to " + params.getHost() + ":" + params.getPort() + " with payload [" +
                    params.getData() + "] and timeout of " + params.getTtl()+", expectedRespose = "+params.isExpectResponse());

            String host = params.getHost();
            int port = params.getPort();

            try {
                socket = new DatagramSocket(null);
                socket.bind(null);
                socket.setSoTimeout(params.getTtl());

                byte[] data = params.getData().getBytes();
                InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(host), port);
                Timber.d("remote socket address " + socketAddress.toString());
                Timber.d("local socket address " + socket.getLocalSocketAddress().toString());
                socket.send(new DatagramPacket(data, data.length, socketAddress));

                if (params.isExpectResponse()) {
                    receive(socket, responses);
                }

            } catch (IOException e) {
                Timber.d("Exception: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    socket.disconnect();
                    socket.close();
                }
            }
        }

        int counter = 1;
        for (String s : responses) {
            Timber.d("\n" + counter + ":\n" + s);
            counter++;
        }

        return responses;

    }

    private boolean udpParamsAreValid(Param params) {
        return (params.getHost() != null) && (params.getData() != null);
    }

    public void receive(DatagramSocket socket, List<String> responses) {
        boolean waitingPacket = true;
        while (waitingPacket) {
            try {
                byte[] buf = new byte[1536];
                DatagramPacket input = new DatagramPacket(buf, buf.length);
                socket.receive(input);
                byte[] receivedData = new byte[input.getLength()];

                System.arraycopy(input.getData(), 0, receivedData, 0, input.getLength());
                String data = new String(receivedData, "ISO-8859-1");
                responses.add(data);

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
