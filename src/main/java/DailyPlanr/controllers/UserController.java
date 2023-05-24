package DailyPlanr.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import DailyPlanr.models.User;
import DailyPlanr.models.UserRepository;
import jakarta.validation.Valid;

@Controller
public class UserController {
	@Autowired
	private UserRepository userRepository;
	
	private final PasswordEncoder encoder;
	
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
	
	@PostMapping("/new")
	public String newUser(@Valid User user) {
		user.setPassword(encoder.encode(user.getPassword()));
		userRepository.save(user);
		return "login";
	}
	@GetMapping("/all")
	public Iterable<User> getAllUsers() {
		return userRepository.findAll();
	}
	
	@PostMapping("/passwordcheck")
	public ResponseEntity<Boolean> validatePassword (@RequestParam String login, @RequestParam String password) {
		
		Optional<User> opUser = userRepository.findByLogin(login);
		
		if(opUser.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);	
		}

		User user = opUser.get();
		boolean valid = encoder.matches(password, user.getPassword());
		HttpStatus status = (valid) ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
		return ResponseEntity.status(status).body(valid);
	}
}
