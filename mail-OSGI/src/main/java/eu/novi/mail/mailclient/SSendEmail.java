package eu.novi.mail.mailclient;
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
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSendEmail implements InterfaceForMail{
	private static final transient Logger log = 
			LoggerFactory.getLogger(SSendEmail.class);
public int SendEmail(String receiver,String information ) {
// Collect the necessary information to send a simple message
// Make sure to replace the values for host, to, and from with
// valid information.
// host - must be a valid smtp server that you currently have
// access to.
// to - whoever is going to get your email
// from - whoever you want to be. Just remember that many smtp
// servers will validate the domain of the from address
// before allowing the mail to be sent.
	System.out.println("I am in");
	log.info("I am in");
	final String username = "NOVIfeedback@gmail.com";
	final String password = "netmodenovi";
	log.info("Setting properties");
	Properties props = new Properties();
	props.put("mail.smtp.auth", "true");
	props.put("mail.smtp.starttls.enable", "true");
	props.put("mail.smtp.host", "smtp.gmail.com");
	props.put("mail.smtp.port", "587");
	log.info("Properties are set");
	Session session = Session.getInstance(props,
	  new javax.mail.Authenticator() {
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password);
		}
	  });
	
boolean sessionDebug = true;
// Create some properties and get the default Session.

try {
	System.out.println("trying");
	log.info("trying to sent email to: "+ receiver);
	Message message = new MimeMessage(session);
	message.setFrom(new InternetAddress("NOVIfeedback@gmail.com"));
	message.setRecipients(Message.RecipientType.TO,
		InternetAddress.parse(receiver));
	message.setSubject("About your Virtual Resources");
	message.setText(information);
	System.out.println("Sending ");
	log.info("Sending "+ information);
	//ClassLoader bakCL = Thread.currentThread().getContextClassLoader();
    //Thread.currentThread().setContextClassLoader(null);
    try {
        Transport.send(message);
    } finally {
      // Thread.currentThread().setContextClassLoader(bakCL);
    }
	//Transport.send(message);
	System.out.println("Done");
	log.info("done: email sent");

} catch (MessagingException e) {
	//throw new RuntimeException(e);
	log.info(e.getMessage().toString());
	return 0;
}
return 1;




// Set debug on the Session so we can see what is going on
// Passing false will not echo debug info, and passing true
// will.
/*session.setDebug(sessionDebug);
try {
// Instantiate a new MimeMessage and fill it with the
// required information.
Message msg = new MimeMessage(session);
msg.setFrom(new InternetAddress(from));
InternetAddress[] address = {new InternetAddress(to)};
msg.setRecipients(Message.RecipientType.TO, address);
msg.setSubject(subject);
msg.setSentDate(new Date());
msg.setText(messageText);
// Hand the message to the default transport service
// for delivery.
Transport.send(msg);
}
catch (MessagingException mex) {
mex.printStackTrace();
}*/
}
}