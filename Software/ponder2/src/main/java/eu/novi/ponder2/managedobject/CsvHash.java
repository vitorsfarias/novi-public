/**
 * Copyright 2007 Kevin Twidle, Imperial College, London, England.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 *
 * Contact: Kevin Twidle <kpt@doc.ic.ac.uk>
 *
 * Created on Sep 4, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.managedobject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Vector;


import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.Util;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Block;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.objects.P2String;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class CsvHash extends P2Array implements ManagedObject {

	private char comma = ',';
	P2String[] headers;

	@Ponder2op("create:")
	CsvHash(String url) {
		try {
			URI uri = new URI(url);
			InputStream is = Util.getInputStream(uri);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String header = in.readLine();
			if (header == null)
				return;
			headers = split(0, header);
			String line;
			while ((line = in.readLine()) != null) {
				P2String[] data = split(headers.length, line);
				this.add(P2Object.create(data));
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * answers an array containing the titles of the columns
	 * 
	 * @return an array with the titles
	 */
	@Ponder2op("titles")
	P2Object getTitles() {
		return P2Object.create(headers);
	}

	@Ponder2op("do:")
	void op_do(P2Object source, P2Block aBlock) throws Ponder2Exception {
		P2Object oldValue[] = new P2Object[headers.length];
		for (Iterator<P2Object> iter = getValues().iterator(); iter.hasNext();) {
			P2Array line = iter.next().asP2Array();
			P2Hash hash = new P2Hash();
			for (int i = 0; i < headers.length; i++) {
				P2Object value = line.at(i);
				if (oldValue[i] == null)
					oldValue[i] = value;
				if (value.asString().length() == 0)
					value = oldValue[i];
				else
					oldValue[i] = value;
				hash.put(headers[i].asString(), value);
			}
			aBlock.operation(source, "value:", hash);
		}
	}

	/**
	 * @param line
	 * @return
	 */
	private P2String[] split(int size, String line) {
		Vector<P2Object> vec = new Vector<P2Object>();
		int findex = 0;
		int tindex = 0;
		while ((tindex = line.indexOf(comma, findex)) >= 0l) {
			vec.add(P2Object.create(line.substring(findex, tindex)));
			findex = tindex + 1;
		}
		vec.add(P2Object.create(line.substring(findex)));
		// If necessary bring the array up to size,
		// trailing delimiters may have been lost
		if (size > 0)
			while (vec.size() < headers.length)
				vec.add(P2Object.create(""));
		return vec.toArray(new P2String[vec.size()]);
	}
}
