package com.jetam6.ArcheusService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	@Autowired
	private JavaMailSender mailSender;

	public void sendVerificationEmail(String to, String token) {
		String link = "http://localhost:8085/api/users/verify?token=" + token;
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("matejsolt@yahoo.com");
		message.setTo(to);
		message.setSubject("Overenie účtu");
		message.setText("Ahoj, prosím klikni na tento odkaz na overenie účtu:\n\n" + link);

		mailSender.send(message);
	    }
	
}
