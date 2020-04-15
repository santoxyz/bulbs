package com.santox.bulbs;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

/**
 * Created by s on 15/04/2020.
 */

public class GenericDevice {
    byte id[] = new byte[4];     //trasformare in int?
    int type;
    String mac;
    String ip;
    AES aes = new AES();

    int count = 0;

    private byte[] macStringToBytes(){
        byte[] bytes = new byte[6];
        for(int i=0;i<mac.length();i+=2){
            String sub = mac.substring(i,i+2);
            bytes[i/2] = Byte.valueOf(mac.substring(i,i+2),16);
        }
        return bytes;
    }
    public GenericDevice(int type, String ip, String mac){
        setType(type);
        setIp(ip);
        setMac(mac);
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

        byte[] response = send_packet((byte)0x65, payload);


        //if any(response[0x22:0x24]):
        //return false

        byte chunk[] = new byte[response.length - 0x38];
        System.arraycopy(response, 0x38, chunk, 0, chunk.length);

        //payload = aes.decrypt(response[0x38:])
        try {
            payload = aes.decrypt(chunk);
        } catch (Exception e){
            //do something
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

        return true;
    }

    public byte[] send_packet(byte command, byte[] payload) {
        count = (count + 1) & 0xffff;
        byte[] packet = new byte[0x38 + payload.length];

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

        Checksum checksum = new Adler32();
        checksum.update(payload,0,payload.length);

        //int checksum = adler32(payload, 0xbeaf) & 0xffff;
        long chk = checksum.getValue();
        packet[0x34] = (byte)(chk & 0xff);
        packet[0x35] = (byte)(chk >> 8);

        try {
            payload = aes.encrypt(payload);
        } catch (Exception e){
            //exception
            System.out.println("exception");

        }

        for (int i=0;i< payload.length;i++){
            packet[0x36 + i] = payload[i];
        }
        //for i in range(len(payload)):
        //packet.append(payload[i])

        checksum = new Adler32();
        checksum.update(packet,0,packet.length);
        chk = checksum.getValue();
        //checksum = adler32(packet, 0xbeaf) & 0xffff
        packet[0x20] = (byte)(chk & 0xff);
        packet[0x21] = (byte)(chk >> 8);

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
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket request = new DatagramPacket(packet,packet.length,address,port);
            socket.setSoTimeout(1000);
            socket.send(request);

            byte[] buffer = new byte[2048];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            System.out.println(response);

            return buffer;

        } catch (Exception e){
            System.out.println("exception");
        }

        return null;
    }
}

