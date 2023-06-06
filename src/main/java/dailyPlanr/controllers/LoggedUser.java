package dailyPlanr.controllers;

import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import dailyPlanr.models.User;

@Named("loggedUser")
@SessionScope
@Component
public class LoggedUser {

	private User user;

	public User getUser() {
		return user;
	}
	
	public void setUserLogged(User user) {
		this.user = user;
	}
	
	public void logOff(){
		this.user = null;
	}
	
	public boolean isLogged() {
		return this.user != null;
	}
	
	public String getLoginUser() {
		return this.user.getLogin();
	}
	
	public Integer getUserId() {
		return this.user.getId();
	}
	
}
