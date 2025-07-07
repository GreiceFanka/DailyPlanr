package dailyplanr.controllers;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
import dailyplanr.models.User;
import dailyplanr.models.UserRepository;

@EnableScheduling
@Component
public class Mail {
	
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Value("${app.passwordmail}")
	private String passwordMail;

	public String getPasswordMail() {
		return passwordMail;
	}
	
	@Scheduled(cron="0 0 0 * * *")
	public void destroyToken() {
		String time = LocalTime.now().plusMinutes(5).format(DateTimeFormatter.ofPattern("HH:mm"));
		time = time.replace(":", "");
		int hourMinute = Integer.parseInt(time);
		
		Iterable<User> users = userRepository.findUsersWithToken();
		
		for (User user : users) {
			if(user.getTemporary_salt() > hourMinute) {
				int id = user.getId();
				userRepository.userDestroyToken("", 0, id);
			}
		}
		
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
				email.setFrom("greicefcardoso@zohomail.eu","DailyPlanr");

				email.addTo(user);
				email.setSubject("DailyPlanr task reminder");
				email.setMsg("Dear user,\n" + "\n"
						+ "This is just a reminder mail that you have incompleted tasks that are late or will be late soon. Please login in DailyPlanr app and check your tasks list.\n"
						+ "\n" + "Greetings from DailyPlanr team!");

				email.setSSLOnConnect(true);
				email.setAuthentication("greicefcardoso@zohomail.eu", passwordMail);
				System.out.println("Sending...");
				email.send();
				System.out.println("Email sent!");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public String sendContactEmail(String userEmail, String subject ,String message, String passwordEmail) throws EmailException {
		try {
			
			SimpleEmail contactEmail = new SimpleEmail();
			contactEmail.setHostName("smtp.zoho.eu");
			contactEmail.setSmtpPort(465);
			
			contactEmail.setFrom("greicefcardoso@zohomail.eu", userEmail);
			contactEmail.addTo("greicefcardoso@gmail.com");
			contactEmail.setSubject(subject);
			contactEmail.setMsg(message);
			
			contactEmail.setSSLOnConnect(true);
			contactEmail.setAuthentication("greicefcardoso@zohomail.eu", passwordEmail);
			System.out.println("Sending...");
			contactEmail.send();
			return "success";
		}catch (Exception e) {
			String error = e.getMessage(); 
			return error;
		}
	}
	
	
	public String sendResetPassword(String userEmail,String name, String token, String passwordEmail) throws EmailException {
		String subject = "DailyPlanr reset password";
		String urlToken = "http://localhost:8080/userpass/"+token;
	
		String message = "Hello "+name+", \n"
				
				+ "	This is a request to create a password to access your DailyPlanr dashboard.\n"
				+ "	Please access the URL below, enter your registered email, set a new password, and you're all set! You will then be able to log in to the platform.\n"
				+ "	Click now to create your password: " +urlToken+ "\n"
				+ "	If you're unable to click the link above, copy the full link and paste it into your browser's address bar.\n"
				+ ""  +urlToken+ "\n"             			
				+ "If you did not request any changes to your account, we recommend updating the password for both your email and your DailyPlanr account.\n"
				+ "\n"
				+ "DailyPlanr\n";
		
		try {
			
			SimpleEmail contactEmail = new SimpleEmail();
			contactEmail.setHostName("smtp.zoho.eu");
			contactEmail.setSmtpPort(465);
			
			contactEmail.setFrom("greicefcardoso@zohomail.eu");
			contactEmail.addTo(userEmail);
			contactEmail.setSubject(subject);
			contactEmail.setMsg(message);
			contactEmail.setSSLOnConnect(true);
			contactEmail.setAuthentication("greicefcardoso@zohomail.eu", passwordEmail);
			System.out.println("Sending...");
			contactEmail.send();
			return "success";
		}catch (Exception e) {
			String error = e.getMessage(); 
			return error;
		}
	}
}
