package sos.net;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;


/**
 * @author ap
 */ 
public class SOSMimeBase64 {

	/*
	  Base64 uses a 65 character subset of US-ASCII,
	  allowing 6 bits for each character so the character
	  "m" with a Base64 value of 38, when represented
	  in binary form, is 100110.

	  With a text string, let's say "men" is encoded this
	  is what happens :

	  The text string is converted into its US-ASCII value.

		 The character "m" has the decimal value of 109
		 The character "e" has the decimal value of 101
		 The character "n" has the decimal value of 110

	  When converted to binary the string looks like this :

		  m   01101101
		  e   01100101
		  n   01101110

	  These three "8-bits" are concatenated to make a
	  24 bit stream
					 011011010110010101101110

	  This 24 bit stream is then split up into 4 6-bit
	  sections
					 011011 010110 010101 101110

	  We now have 4 values. These binary values are
	  converted to decimal form
					   27     22     21     46

	  And the corresponding Base64 character are :
					   b      W       V     u

	  The encoding is always on a three characters basis
	  (to have a set of 4 Base64 characters). To encode one
	  or two then, we use the special character "=" to pad
	  until 4 base64 characters is reached.

	  ex. encode "me"

		  01101101  01100101
		  0110110101100101
		  011011 010110 0101
						111111    (AND to fill the missing bits)
		  011011 010110 010100
			 b     W      U
			 b     W      U     =  ("=" is the padding character)

		  so "bWU="  is the base64 equivalent.

		  encode "m"

		  01101101
		  011011 01
				 111111         (AND to fill the missing bits)
		  011011 010000
			  b     Q     =  =   (two paddings are added)

		  Finally, MIME specifies that lines are 76 characters wide maximum.

	*/

   static String BaseTable[] = {
	  "A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P",
	  "Q","R","S","T","U","V","W","X","Y","Z","a","b","c","d","e","f",
	  "g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v",
	  "w","x","y","z","0","1","2","3","4","5","6","7","8","9","+","/"
	  };


	public static void encode(String filename, BufferedWriter out) {

	  try {
		File f              = new File(filename);
		FileInputStream fin = new FileInputStream(filename);

		// read the entire file into the byte array
		byte bytes[]        = new byte[(int)(f.length())];
		int n               = fin.read(bytes);

		if (n < 1) return;          // no bytes to encode!?!

		byte buf[] = new byte[4];   // array of base64 characters

		int n3byt      = n / 3;     // how 3 bytes groups?
		int nrest      = n % 3;     // the remaining bytes from the grouping
		int k          = n3byt * 3; // we are doing 3 bytes at a time
		int linelength = 0;         // current linelength
		int i          = 0;         // index

	   // do the 3-bytes groups ...
	   while ( i < k ) {
		 buf[0] = (byte)(( bytes[i]   & 0xFC) >> 2);
		 buf[1] = (byte)(((bytes[i]   & 0x03) << 4) |
					((bytes[i+1] & 0xF0) >> 4));
		 buf[2] = (byte)(((bytes[i+1] & 0x0F) << 2) |
					((bytes[i+2] & 0xC0) >> 6));
		 buf[3] = (byte)(  bytes[i+2] & 0x3F);
		 send(out, BaseTable[buf[0]]);
		 send(out, BaseTable[buf[1]]);
		 send(out, BaseTable[buf[2]]);
		 send(out, BaseTable[buf[3]]);
		 /*
			The above code can be written in more "optimized"
			way. Harder to understand but more compact.
			Thanks to J. Tordera for the tip!
			 buf[0]= (byte)(b[i] >> 2);
			 buf[1]= (byte)(((b[i] & 0x03)  << 4)|(b[i+1]>> 4));
			 buf[2]= (byte)(((b[i+1] & 0x0F)<< 2)|(b[i+2]>> 6));
			 buf[3]= (byte)(b[i+2] & 0x3F);
			 send(out,BaseTable[buf[0]]+BaseTable[buf[1]]+
					  BaseTable[buf[2]]+BaseTable[buf[3]]);
		 */

		 if ((linelength += 4) >= 76) {
			send(out, "\r\n");
			linelength = 0;
			}
		 i += 3;
		 }

	   // deals with with the padding ...
	   if (nrest==2) {
		  // 2 bytes left
		  buf[0] = (byte)(( bytes[k] & 0xFC)   >> 2);
		  buf[1] = (byte)(((bytes[k] & 0x03)   << 4) |
					 ((bytes[k+1] & 0xF0) >> 4));
		  buf[2] = (byte)(( bytes[k+1] & 0x0F) << 2);
		  }
	   else if (nrest==1) {
		  // 1 byte left
		  buf[0] = (byte)((bytes[k] & 0xFC) >> 2);
		  buf[1] = (byte)((bytes[k] & 0x03) << 4);
		  }
	   if (nrest > 0) {
		  // send the padding
		  if ((linelength += 4) >= 76) send(out, "\r\n");
		  send(out, BaseTable[buf[0]]);
		  send(out, BaseTable[buf[1]]);
		  // Thanks to R. Claerman for the bug fix here!
		  if (nrest==2) {
			 send(out, BaseTable[buf[2]]);
			 }
		  else {
			send(out, "=");
			}
		  send(out, "=");
		  }
		out.flush();
		}
	   catch (Exception e) {
		 e.printStackTrace();
		 }
	 }


   public static void send(BufferedWriter out, String s) {
	  try {
		out.write(s);
		}
	  catch (Exception e) {
		e.printStackTrace();
		}
	  }

}
