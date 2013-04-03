/**
*
* Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
* Copyright according to BSD License
* For full text of the license see: ./novi/Software/Monitoring/MonitoringTool/PacketTracking/license.txt
*
* @author <a href="mailto:ramon.masek@fokus.fraunhofer.de">Ramon Masek</a>, Fraunhofer FOKUS
* @author <a href="mailto:c.henke@tu-berlin.de">Christian Henke</a>, Technical University Berlin
* @author <a href="mailto:carsten.schmoll@fokus.fraunhofer.de">Carsten Schmoll</a>, Fraunhofer FOKUS
* @author <a href="mailto:Julian.Vetter@campus.tu-berlin.de">Julian Vetter</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Jens Krenzin</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Michael Gehring</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Tacio Grespan Santos</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Fabian Wolff</a>, Fraunhofer FOKUS
*
*/

package de.fhg.fokus.net.ipfix.debug;

import java.nio.ByteBuffer;

public class HexDump {
	public static String toHexString(byte in[]  ) {
		return toHexString(in, 8);
	}
	public static String toHexString( ByteBuffer bbuf ){
		byte [] bytes = new byte[bbuf.capacity()];
		bbuf.get(bytes);
		return toHexString(bytes);
	}
	
	/**
	 * 
	 * Convert a byte[] array to readable string format. This makes the "hex"
	 * readable!
	 * (from http://www.devx.com/tips/Tip/13540)
	 * 
	 * @return result String buffer in String format
	 * 
	 * @param in   byte[] buffer to convert to string format
	 */

	public static String toHexString(byte in[], int bytesPerLine ) {

		byte ch = 0x00;

		int i = 0;

		if (in == null || in.length <= 0)

			return null;

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };

		StringBuffer out = new StringBuffer(in.length * 2);

		while (i < in.length) {

			ch = (byte) (in[i] & 0xF0); // Strip off high nibble

			ch = (byte) (ch >>> 4);
			// shift the bits down

			ch = (byte) (ch & 0x0F);
			// must do this is high order bit is on!

			out.append(pseudo[(int) ch]); // convert the nibble to a String
											// Character

			ch = (byte) (in[i] & 0x0F); // Strip offlow nibble

			out.append(pseudo[(int) ch]); // convert the nibble to a String
											// Character
			out.append(" ");
			i++;
			if( i % bytesPerLine ==0 ){
				out.append( "\n");
			}


		}

		String rslt = new String(out);

		return rslt;

	}
	public static void main(String[] args) {
		ByteBuffer bbuf = ByteBuffer.allocate(8);
		bbuf.put((byte)1);
		bbuf.put((byte)255);
		bbuf.put((byte)3);
		bbuf.put((byte)4);
		System.out.println(toHexString(bbuf));
		
	}
}
