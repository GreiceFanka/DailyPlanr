package dailyPlanr.controllers;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dailyPlanr.models.User;
import dailyPlanr.models.UserRepository;
import jakarta.validation.Valid;

@Controller
public class UserController{

	@Autowired
	private UserRepository userRepository;

	private final PasswordEncoder encoder;
	
	@Inject
	private LoggedUser loggedUser;
	
	public UserController(PasswordEncoder encoder) {
		this.encoder = encoder;
	}
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@GetMapping("/signup")
	public String signup() {
		return "signup";
	}
	
	@GetMapping("/index")
	public String homePage() {
		return "/index";
	}
	
	@PostMapping("/new")
	public String newUser(@Valid User user, RedirectAttributes redirAttrs ){
		user.setPassword(encoder.encode(user.getPassword()));
		boolean isEmail = false;
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		   Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	       Matcher matcher = pattern.matcher(user.getLogin());
	       
	       if(matcher.matches()) {
	    	   isEmail = true;
	       }
		
		Optional<User> opUser = userRepository.findByLogin(user.getLogin());
		
		if(opUser.isEmpty() && isEmail) {
			userRepository.save(user);
			redirAttrs.addFlashAttribute("success", "Everything went just fine.");
			return "redirect:/login";
		}else if (!isEmail) {
			redirAttrs.addFlashAttribute("error", "Invalid email format, please try again.");
		}else {
			redirAttrs.addFlashAttribute("error", "User already registered");
		}
		return "redirect:/signup";
	}
	
	@GetMapping("/allusers")
	public @ResponseBody Iterable<User> getAllUsers() {
		return userRepository.findAll();
	}
	
	@PostMapping("/passwordcheck")
	public String validatePassword (@RequestParam String login, @RequestParam String password, RedirectAttributes redirAttrs) {
		
		Optional<User> opUser = userRepository.findByLogin(login);
		
		if(opUser.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "Login not found.");
			return "redirect:/login";
		}

		User user = opUser.get();
		boolean valid = encoder.matches(password, user.getPassword());
		if(valid) {
			this.loggedUser.setUserLogged(user);		
			return "redirect:/alltasks";
		}else {
			redirAttrs.addFlashAttribute("error", "Incorrect Password!");
			 return "redirect:/login";
		}
	}
	
	@GetMapping("/search/user/{task_id}")
	public String searchUser(@PathVariable int task_id, RedirectAttributes redirAttrs, ModelMap model) {
		int taskId = task_id;
		String company = loggedUser.getCompany();
		
		if(!company.isEmpty()) {
		Iterable<User> usersCompany = userRepository.findUserWithSameCompany(company);
		model.addAttribute("usersCompany", usersCompany);
		model.addAttribute("taskId", taskId);
		return "/adduser";
		}else {
			redirAttrs.addFlashAttribute("error", "You don't have company.");
			return "redirect:/alltasks";
		}
	}
}
