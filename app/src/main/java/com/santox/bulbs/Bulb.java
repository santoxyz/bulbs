package com.santox.bulbs;

import org.json.JSONObject;

import java.util.zip.Adler32;
import java.util.zip.Checksum;

/**
 * Created by s on 15/04/2020.
 */

public class Bulb extends GenericDevice {
    public static int SET_COMMAND = (byte)2;
    public static int GET_COMMAND = (byte)1;

    byte[] state;

    public Bulb(int type, String ip, String mac){
        super(type,ip,mac);
    }


    public void send_command(byte[] command, byte type) {
        byte packet[] = new byte[16 + ((int)(command.length / 16) + 1)*16];
         //packet = bytearray(16 + (int(command.length / 16) + 1)*16)

        packet[0x02] = (byte)0xa5;
        packet[0x03] = (byte)0xa5;
        packet[0x04] = (byte)0x5a;
        packet[0x05] = (byte)0x5a;
        packet[0x08] = type;
        packet[0x09] = (byte)0x0b;
        packet[0x0a] = (byte)command.length;
        System.arraycopy(command,0,packet,0x0e,command.length);
        //packet[0x0e:] =map(ord, command)

        Checksum checksum = new Adler32();
        checksum.update(packet,0,packet.length);
        long chk = checksum.getValue();
        //checksum = adler32(packet, 0xbeaf) & 0xffff

        packet[0x00] = (byte)((0x0c + command.length) & 0xff);
        packet[0x06] = (byte)(chk & 0xff); //  #Checksum 1 position
        packet[0x07] = (byte)(chk >> 8);  //#Checksum 2 position

        byte[] response = send_packet((byte)0x6a, packet);

        int err = response[0x36] | (response[0x37] << 8);
        if (err != 0) {
            System.out.println("error");
            return;
        }

        byte[] encrypted_payload = new byte[response.length - 0x38];
        System.arraycopy(response,(byte)0x38,encrypted_payload,0,encrypted_payload.length);
        try {
            byte[] payload = aes.decrypt(encrypted_payload);
            int responseLength = ((int)(payload[0x0a]) | (int)(payload[0x0b]) << 8);
            if (responseLength > 0) {
                state = new byte[responseLength];
                System.arraycopy(payload,0x0e,state,0,responseLength);

                //self.state_dict = json.loads(payload[0x0e:0x0e + responseLength])
                System.out.println("response received");

            }
        } catch (Exception e){
            System.out.println("exception");
        }


    }

    void set_state(boolean on) {
        try {
            JSONObject cmd = new JSONObject();
            cmd.put("pwr", new Boolean(on));
            //String cmd = '{"pwr":%d}' % (1 if state == "ON" or state ==1 else 0)
            send_command(cmd.toString().getBytes("UTF-8"),(byte)SET_COMMAND);
        } catch (Exception e){
            System.out.println("exception");
        }
    }

    public String get_state() {
        try {
            JSONObject cmd = new JSONObject();
            //cmd = "{}"
            send_command(cmd.toString().getBytes("UTF-8"),(byte)SET_COMMAND);
            return new String(state);
        } catch (Exception e){
            System.out.println("exception");
        }
        return "";
    }
}
