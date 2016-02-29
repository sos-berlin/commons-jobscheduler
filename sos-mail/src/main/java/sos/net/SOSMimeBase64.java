package sos.net;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;

/** @author ap */
public class SOSMimeBase64 {

    private static final Logger LOGGER = Logger.getLogger(SOSMimeBase64.class);
    static String BaseTable[] = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
            "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+", "/" };

    public static void encode(String filename, BufferedWriter out) {

        try {
            File f = new File(filename);
            FileInputStream fin = new FileInputStream(filename);
            // read the entire file into the byte array
            byte bytes[] = new byte[(int) (f.length())];
            int n = fin.read(bytes);
            if (n < 1) {
                return;
            }
            byte buf[] = new byte[4];
            int n3byt = n / 3;
            int nrest = n % 3;
            int k = n3byt * 3;
            int linelength = 0;
            int i = 0;
            // do the 3-bytes groups ...
            while (i < k) {
                buf[0] = (byte) ((bytes[i] & 0xFC) >> 2);
                buf[1] = (byte) (((bytes[i] & 0x03) << 4) | ((bytes[i + 1] & 0xF0) >> 4));
                buf[2] = (byte) (((bytes[i + 1] & 0x0F) << 2) | ((bytes[i + 2] & 0xC0) >> 6));
                buf[3] = (byte) (bytes[i + 2] & 0x3F);
                send(out, BaseTable[buf[0]]);
                send(out, BaseTable[buf[1]]);
                send(out, BaseTable[buf[2]]);
                send(out, BaseTable[buf[3]]);
                if ((linelength += 4) >= 76) {
                    send(out, "\r\n");
                    linelength = 0;
                }
                i += 3;
            }
            if (nrest == 2) {
                // 2 bytes left
                buf[0] = (byte) ((bytes[k] & 0xFC) >> 2);
                buf[1] = (byte) (((bytes[k] & 0x03) << 4) | ((bytes[k + 1] & 0xF0) >> 4));
                buf[2] = (byte) ((bytes[k + 1] & 0x0F) << 2);
            } else if (nrest == 1) {
                // 1 byte left
                buf[0] = (byte) ((bytes[k] & 0xFC) >> 2);
                buf[1] = (byte) ((bytes[k] & 0x03) << 4);
            }
            if (nrest > 0) {
                // send the padding
                if ((linelength += 4) >= 76) {
                    send(out, "\r\n");
                }
                send(out, BaseTable[buf[0]]);
                send(out, BaseTable[buf[1]]);
                // Thanks to R. Claerman for the bug fix here!
                if (nrest == 2) {
                    send(out, BaseTable[buf[2]]);
                } else {
                    send(out, "=");
                }
                send(out, "=");
            }
            out.flush();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void send(BufferedWriter out, String s) {
        try {
            out.write(s);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
