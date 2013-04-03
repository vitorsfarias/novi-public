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
 * Contact: Yiannos Kryftis <ykryftis@netmode.ece.ntua.gr>
 */
package eu.novi.policylistener.ponder2comms;

import java.net.*;
import java.io.*;


public class TelnetClient
{
	Socket soc; 
	DataInputStream din;
	DataOutputStream dout;

	//@SuppressWarnings("deprecation")
	public String TelnetPonder2(String arg) 
  {
    init();
	 String resultb="true";
      try {
		System.out.println("< Telnet Prompt > "+ din.readLine()+" "+ din.readLine());
    	dout.flush();
	    dout.writeBytes(arg);
	    String result = din.readLine();
	       if (result.equals("/ $ false"))
	    	   resultb = "false";
	       else
	    	   if (result.equals("/ $ true"))
	    	      resultb = "true";
	        	   else  
	        	     resultb = result;
	      soc.close();  //close port  
	      din.close();  //close input stream      
	      dout.close(); //close output stream      
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
     // System.out.println(din.readLine());
      //System.out.println(din.readLine());
      
	return resultb;
       
  }
	
	public String TelnetPonder1(String arg) 
	  {
	    init();
		 String resultb1="true";
	      try {
			System.out.println("< Telnet Prompt > "+ din.readLine()+" "+ din.readLine());
	    	dout.flush();
		    dout.writeBytes(arg);
		      soc.close();  //close port  
		      din.close();  //close input stream      
		      dout.close(); //close output stream      
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultb1;
	       
	  }
	
	public String TelnetPonder3(String arg) 
	  {
	    init();
		 String res="";
		 String text;
	      try {
			System.out.println("< Telnet Prompt > "+ din.readLine()+" "+ din.readLine());
			dout.flush();
			dout.writeBytes(arg);
		  // while (din.readLine()!=null)
		   //{ res+=din.readLine();
		   //}
	    	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	int available=din.available();
	    	System.out.println(din.available());
	    	while (din.available()>0)
	    	{
	    		int inp= din.read();
	    		//System.out.println(inp);
	 //   		System.out.print(Character.valueOf((char) inp));
	    		res+=Character.valueOf((char) inp);
	    		//System.out.println(din.available());
	    		
	    	}
	    	/*while (din.available()>4)
	    	{
	    	text=din.readLine();
	    	res+=text+"\n";
	    	available-=text.length();
	    	System.out.println(available);
	    	System.out.println("System says: "+din.available());
	    	}*/
	    	  soc.close();  //close port  
		      din.close();  //close input stream      
		      dout.close(); //close output stream      
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	System.out.println(res);
	    return res;
	       
	  }
	
	public void init()
	{
		 try {
			 //Create object of Socket "150.254.160.28"
			 soc  = new Socket("localhost",13570);
			//Create object of Input Stream to read from socket
			din = new DataInputStream(soc.getInputStream()); 
			//Create object of Output Stream  to write on socket 
			dout=new DataOutputStream(soc.getOutputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
	}
/*  public void setSocket(Socket socc)
  {
	  soc=socc;
  }
	public void setDataInputStream(DataInputStream mockDin)
  {
	   din=mockDin;
  }
  public void setDataOutputStream(DataOutputStream doutt)
  {
	   dout=doutt;
  }*/

}