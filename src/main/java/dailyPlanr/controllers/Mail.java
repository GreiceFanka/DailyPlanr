package dailyPlanr.controllers;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Named("Mail")
@Component
public class Mail {
	
	@Value("${app.passwordmail}")
	private String passwordMail;

	public String getPasswordMail() {
		return passwordMail;
	}

}
