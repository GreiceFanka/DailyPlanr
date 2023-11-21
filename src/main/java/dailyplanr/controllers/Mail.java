package dailyplanr.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dailyplanr.models.TaskRepository;

@EnableScheduling
@Component
public class Mail {
	
	@Autowired
	private TaskRepository taskRepository;
	
	@Value("${app.passwordmail}")
	private String passwordMail;

	public String getPasswordMail() {
		return passwordMail;
	}
	
	@Scheduled(cron="0 0 23 * * *")
	public void sendReminder() throws EmailException {
		LocalDateTime date = LocalDateTime.now().plusDays(1);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
		date.format(dateTimeFormatter);

		List<String> userList = taskRepository.findTaskByDate(date);
		String passwordMail = getPasswordMail();
		sendEmail(userList, passwordMail);
	}
	
	public static void sendEmail(List<String> userList, String passwordMail) throws EmailException {
		try {
			for (String user : userList) {
				SimpleEmail email = new SimpleEmail();

				email.setHostName("smtp.zoho.eu");
				email.setSmtpPort(465);
				email.setFrom("dailyplanr@zohomail.eu", "DailyPlanr");

				email.addTo(user);
				email.setSubject("DailyPlanr task reminder");
				email.setMsg("Dear user,\n" + "\n"
						+ "This is just a reminder mail that you have incompleted tasks that are late or will be late soon. Please login in DailyPlanr app and check your tasks list.\n"
						+ "\n" + "Greetings from DailyPlanr team!");

				email.setSSL(true);
				email.setAuthentication("dailyplanr@zohomail.eu", passwordMail);
				System.out.println("Sending...");
				email.send();
				System.out.println("Email sent!");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void sendContactEmail(String userEmail, String subject ,String message, String passwordEmail) throws EmailException {
		try {
			
			SimpleEmail contactEmail = new SimpleEmail();
			contactEmail.setHostName("smtp.zoho.eu");
			contactEmail.setSmtpPort(465);
			
			contactEmail.setFrom("dailyplanr@zohomail.eu", userEmail);
			contactEmail.addTo("greicefcardoso@gmail.com");
			contactEmail.setSubject(subject);
			contactEmail.setMsg(message);
			
			contactEmail.setSSL(true);
			contactEmail.setAuthentication("dailyplanr@zohomail.eu", passwordEmail);
			System.out.println("Sending...");
			contactEmail.send();
			System.out.println("Email sent!");
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
