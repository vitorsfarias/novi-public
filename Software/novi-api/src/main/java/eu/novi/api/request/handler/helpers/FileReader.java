package eu.novi.api.request.handler.helpers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import eu.novi.api.request.handler.impl.RequestHandlerImpl;
/**
 * 
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *******************************************************************************************
 * 
 * @author <a href="mailto:a.wibisono@uva.nl">Adianto Wibisono</a>
 *
 ********************************************************************************************/
public class FileReader {
	
	public static String readFile(String file) throws IOException, NoClassDefFoundError{
		Bundle bundle = FrameworkUtil.getBundle(RequestHandlerImpl.class);
		InputStream resourceInputStream;
		
		if (bundle != null) {
			resourceInputStream = bundle.getEntry(file).openStream();
		} else {
			resourceInputStream = new FileInputStream(RequestHandlerImpl.class.getResource("/" + file).getPath());
		}
		return readInputStreamAsString(resourceInputStream);
	}
	
	private static String readInputStreamAsString(InputStream is)
	throws java.io.IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder fileData = new StringBuilder();
		String line = "";
		while((line = reader.readLine()) != null){
			fileData.append(line +"\n");
		}
		reader.close();
		return fileData.toString();
	}
}
