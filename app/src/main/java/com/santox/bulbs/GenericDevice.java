package com.santox.bulbs;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.Checksum;

/**
 * Created by s on 15/04/2020.
 */

public class GenericDevice {
    public static String TAG = Bulb.class.getName().toString();

    byte id[] = new byte[4];     //trasformare in int?
    int type;
    String mac;
    String ip;
    AES aes = new AES();
    boolean authenticated = false;
    int count = new Random().nextInt(0xffff);
    DatagramSocket socket;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] bytesFromString(String s){
        byte[] bytes = new byte[s.length()/2];
        for (int i=0;i<s.length(); i+=2){
            String sub = s.substring(i,i+2);
            bytes[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return bytes;
    }

    private byte[] macStringToBytes(){
        byte[] bytes = new byte[6];
        for(int i=0;i<mac.length();i+=2){
            String sub = mac.substring(i,i+2);
            bytes[i / 2] = (byte) ((Character.digit(mac.charAt(i), 16) << 4)
                    + Character.digit(mac.charAt(i+1), 16));

        }
        return bytes;
    }
    public GenericDevice(int type, String ip, String mac){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setType(type);
        setIp(ip);
        setMac(mac);


        try {
            socket = new DatagramSocket();
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            //SocketAddress socketAddress=new InetSocketAddress(Inet4Address.getByName(""), 0);
            //socket.bind(socketAddress);
        } catch (Exception e){
            Log.e(TAG,"Exception " + e.toString());
        }

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


    public boolean auth(){
        if(authenticated){
            Log.w(TAG," === auth ALREADY authenticated === ");

            return authenticated;
        }
        Log.w(TAG," === auth === ");

        byte[] payload = new byte[0x50];
        payload[0x04] = 0x31;
        payload[0x05] = 0x31;
        payload[0x06] = 0x31;
        payload[0x07] = 0x31;
        payload[0x08] = 0x31;
        payload[0x09] = 0x31;
        payload[0x0a] = 0x31;
        payload[0x0b] = 0x31;
        payload[0x0c] = 0x31;
        payload[0x0d] = 0x31;
        payload[0x0e] = 0x31;
        payload[0x0f] = 0x31;
        payload[0x10] = 0x31;
        payload[0x11] = 0x31;
        payload[0x12] = 0x31;
        payload[0x1e] = 0x01;
        payload[0x2d] = 0x01;
        payload[0x30] = 'T';
        payload[0x31] = 'e';
        payload[0x32] = 's';
        payload[0x33] = 't';
        payload[0x34] = ' ';
        payload[0x35] = ' ';
        payload[0x36] = '1';

        //Log.w(TAG,"Auth: Sending " + bytesToHex(payload));

        byte[] response = send_packet((byte)0x65, payload);

        //Log.w(TAG,"Auth: Response " + bytesToHex(response));

        //if any(response[0x22:0x24]):
        //return false

        byte chunk[] = new byte[response.length - 0x38];
        System.arraycopy(response, 0x38, chunk, 0, chunk.length);
        //Log.w(TAG,"Auth: chunk " + bytesToHex(chunk));

        //payload = aes.decrypt(response[0x38:])
        try {
            payload = aes.decrypt(chunk);
            //Log.w(TAG,"Auth: chunk decrypted (payload) " + bytesToHex(payload));

        } catch (Exception e){
            Log.e(TAG,"Auth: exception " + e.toString());
            return false;
        }
        byte key[] = new byte[0x14-0x4];
        System.arraycopy(payload, 0x4, key, 0, key.length);

        //key = payload[0x04:0x14]
        //if len(key) % 16 != 0:
        //return False

        //byte id[] = new byte[0x0-0x4];
        System.arraycopy(payload, 0x0, id, 0, id.length);
        //self.id = payload[0x00:0x04]
        //self.update_aes(key)
        aes.key = key;
        Log.w(TAG,"Auth: key " + bytesToHex(key));
        Log.w(TAG,"Auth: id " + bytesToHex(id));

        return authenticated = true;
    }

    public byte[] send_packet(byte command, byte[] payload) {
        count = (count + 1) & 0xffff;
        byte[] packet = new byte[0x38];
        packet[0x00] = (byte)0x5a;
        packet[0x01] = (byte)0xa5;
        packet[0x02] = (byte)0xaa;
        packet[0x03] = (byte)0x55;
        packet[0x04] = (byte)0x5a;
        packet[0x05] = (byte)0xa5;
        packet[0x06] = (byte)0xaa;
        packet[0x07] = (byte)0x55;
        packet[0x24] = (byte)(type & 0xff);
        packet[0x25] = (byte)(type >> 8);
        packet[0x26] = (byte)command;
        packet[0x28] = (byte)(count & 0xff);
        packet[0x29] = (byte)(count >> 8);
        byte[] macbytes = macStringToBytes();
        packet[0x2a] = macbytes[0];
        packet[0x2b] = macbytes[1];
        packet[0x2c] = macbytes[2];
        packet[0x2d] = macbytes[3];
        packet[0x2e] = macbytes[4];
        packet[0x2f] = macbytes[5];
        packet[0x30] = id[0];
        packet[0x31] = id[1];
        packet[0x32] = id[2];
        packet[0x33] = id[3];

        //pad the payload for AES encryption
        if (payload.length>0) {
            int padding = (16 - payload.length) % 16;
            if (padding > 0) {
                byte[] temp = new byte[payload.length + padding];
                System.arraycopy(payload, 0x0, temp, 0, payload.length);
                payload = temp;
            }
            //payload += bytearray((16 - len(payload)) % 16)
        }

        Adler32 checksum = new Adler32();
        checksum.reset(0xbeaf & 0xffff);
        checksum.update(payload,0,payload.length);

        //int checksum = adler32(payload, 0xbeaf) & 0xffff;
        long chk = checksum.getValue() & 0xffff;
        //Log.i(TAG,"payload checksum " + chk);
        packet[0x34] = (byte)(chk & 0xff);
        packet[0x35] = (byte)((chk >> 8) & 0xff);

        try {
            //Log.i(TAG,"payload " + bytesToHex(payload));
            payload = aes.encrypt(payload);
            //payload = bytesFromString("453452e7f92eda958344930835ef9a6dfb692dc370b90443ac5cd63fbb53adfa08814ca7f8cf417100328e570c3b86c94d05708449a389e29ae1045436a05bdddc02c161af1325e87e19b0f7d1ce068de51b619156876d338cff3b991e40cdb1");
            //Log.i(TAG,"encrypted payload " + bytesToHex(payload));
        } catch (Exception e){
            //exception
            Log.e(TAG,"Exception " + e.toString());
        }

        byte[] tempPacket = new byte[packet.length + payload.length];
        System.arraycopy(packet,0,tempPacket,0,packet.length);
        System.arraycopy(payload,0,tempPacket, packet.length,payload.length);
        packet = tempPacket;
        //for i in range(len(payload)):
        //packet.append(payload[i])

        checksum = new Adler32();
        checksum.reset(0xbeaf & 0xffff);
        checksum.update(packet,0,packet.length);
        chk = (checksum.getValue()) & 0xffff;
        //Log.i(TAG,"packet checksum " + chk);
        //checksum = adler32(packet, 0xbeaf) & 0xffff
        packet[0x20] = (byte)(chk & 0xff);
        packet[0x21] = (byte)((chk >> 8) & 0xff);



        /*
        start_time = time.time()
        with self.lock:
        while True:
        try:
        self.cs.sendto(packet, self.host)
        self.cs.settimeout(1)
        response = self.cs.recvfrom(2048)
        break
                except socket.timeout:
        if (time.time() - start_time) >self.timeout:
        raise
        return bytearray(response[0])
        */
        try {
            int port = 80;
            InetAddress address = InetAddress.getByName(ip);
            DatagramPacket request = new DatagramPacket(packet,packet.length,address,port);

            Log.i(TAG,"sending packet to " + address + " : " + port + "(count " + count + " aes key " + bytesToHex(aes.key) + " id " +  bytesToHex(id) + ")");
            Log.i(TAG, bytesToHex(packet));

            socket.send(request);
            socket.setSoTimeout(10000);

            byte[] buffer = new byte[2048];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            byte[] resp = new byte[response.getLength()];
            System.arraycopy(response.getData(),0,resp,0,resp.length);
            Log.i(TAG,"resp " + bytesToHex(resp));

            return resp;

        } catch (Exception e){
            Log.e(TAG,"Exception " + e.toString());
        }

        return null;
    }
}

