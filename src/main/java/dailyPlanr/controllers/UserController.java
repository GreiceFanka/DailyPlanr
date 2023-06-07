package dailyPlanr.controllers;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
	public String newUser(@Valid User user, RedirectAttributes redirAttrs ) {
		user.setPassword(encoder.encode(user.getPassword()));
		Optional<User> opUser = userRepository.findByLogin(user.getLogin());
		if(opUser.isEmpty()) {
			userRepository.save(user);
			redirAttrs.addFlashAttribute("success", "Everything went just fine.");
			return "redirect:/login";
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
}
