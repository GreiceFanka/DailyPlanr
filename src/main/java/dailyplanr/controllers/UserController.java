package dailyplanr.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import dailyplanr.models.Category;
import dailyplanr.models.User;
import dailyplanr.models.UserRepository;
import dailyplanr.service.CategoryService;
import dailyplanr.service.Mail;
import dailyplanr.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class UserController {

	@Autowired
	private UserRepository userRepository;

	private final PasswordEncoder encoder;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private UserService userService;

	@Inject
	private LoggedUser loggedUser;

	@Inject
	private Mail mail;

	public UserController(PasswordEncoder encoder) {
		this.encoder = encoder;
	}
	
	@GetMapping("/forgotpass")
	public String forgotPass() {
		return "forgotpass";
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
		return "index";
	}
	
	@GetMapping("/userpass/{token}")
	public String userpass(@PathVariable String token) {
		Optional<User> user = userService.findUserByToken(token);
		LocalDateTime dateTime = LocalDateTime.now();
		if(user.isEmpty()) {
			return "redirect:/index";
		}else if(user.get().getTemporary_salt().isBefore(dateTime)) {
			int id = user.get().getId();
			userService.destroyToken(id);
			return "redirect:/index";
		}else if(user.isPresent() && token.length() >= 16) {
			return "userpass";
		}
		return "redirect:/index";
	}
	
	@PostMapping("/tokenpasschange")
	public ResponseEntity<String> tokenPassChange(String newPassword, String token){
		Optional<User> user = userService.findUserByToken(token);
		int id = user.get().getId();
		if(user.isEmpty()){
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
		}else {
			LocalDateTime time = LocalDateTime.now();
			if(user.get().getTemporary_salt().isAfter(time)) {
				userService.updatePassword(newPassword, id);
				userService.destroyToken(id);
				return ResponseEntity.status(HttpStatus.OK).body("Password changed successfully!");
					
			}else {
				userService.destroyToken(id);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Expired token. Please request the reset password again.");
		}
			
	}
}
	
	@PostMapping("/resetpass")
	public String resetPass(@RequestParam String email, RedirectAttributes redirAttrs){
		Optional<User> findUser = userRepository.findByLogin(email);
		
		if(findUser.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "User not found!");
			return "redirect:/forgotpass";
		}else {
			User user = findUser.get();
			String name = user.getName();
			String passwordEmail = mail.getPasswordMail();
			String salt = user.getSalt();
			String token = encoder.encode(email.concat(salt));
			token = token.replaceAll("/", "");
			token = token.replace(".", "");
			int id = user.getId();
			try {
				String sender = mail.sendResetPassword(email, name, token, passwordEmail);
				if(sender.equalsIgnoreCase("success")) {
					LocalDateTime time = LocalDateTime.now().plusMinutes(5);
					DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
					time.format(dateTimeFormatter);
					userRepository.saveTemporary(time, token, id);
					redirAttrs.addFlashAttribute("success", "An email was sent, you have 5 minutes to change your password.");
					return "redirect:/forgotpass";
				}else {
					redirAttrs.addFlashAttribute("error", sender);
					return "redirect:/forgotpass";
				}
			} catch (Exception e) {
				String error = e.getMessage();
				redirAttrs.addFlashAttribute("error", error);
				return "redirect:/forgotpass";
			}
		}
	}

	@PostMapping("/new")
	public ResponseEntity<String> newUser(@Valid User user, RedirectAttributes redirAttrs){
		boolean isEmail = false;
		Matcher matcher = userService.createUserPass(user);
		if (matcher.matches()) {
			isEmail = true;
		}
		Optional<String> opUser = userService.verifyUserLogin(user);
		if (opUser.isEmpty() && isEmail) {
			int id = userService.saveNewUser(user);
			Category category = userService.createDefaultCategory(user);
			try {
				userService.createKeys(id);
				categoryService.createKey(user, category);
			} catch (Exception e) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("A technical error occurred. Please try again later.");
			}
			return ResponseEntity.status(HttpStatus.OK).body("Account created successfully!");
		} else if (!isEmail) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error!Try again later!");
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error!Try again later!");
		}
		
	}

	@PostMapping("/passwordcheck")
	public ResponseEntity<String> validatePassword(@RequestParam String login, @RequestParam String password, HttpSession session, HttpServletRequest request) {
		LocalDateTime time_block = null;
		int login_attempts = 0;
		int min_block = 0;
		
		Optional<User> opUser = userRepository.findByLogin(login);

		if (opUser.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
		}
		
		User user = opUser.get();
		int id = user.getId();
		LocalDateTime time_now = LocalDateTime.now();
		LocalDateTime unblockTime = user.getTime_block();
		
		if(unblockTime != null && time_now.isBefore(unblockTime)) {
			Duration duration = Duration.between(time_now, unblockTime);
			duration = duration.plusMinutes(1);
			min_block = duration.toMinutesPart();
			return ResponseEntity.status(HttpStatus.LOCKED).body("Blocked for " + min_block + " minutes due to multiple tentatives!Try again later.");
			
		}else {
			if(user.getSalt() != null) {
				String encodedPass = password.concat(user.getSalt());				
				boolean valid = encoder.matches(encodedPass, user.getPassword());
				
				if (valid) {
					userService.setNewSession(user, login_attempts, time_block, id, session, request);
					return ResponseEntity.status(HttpStatus.OK).body("Success");
						
				}else {
					login_attempts = user.getLogin_attempts();
					user.setLogin_attempts(login_attempts++);
					if(login_attempts >= 5) {
						LocalDateTime newTime = LocalDateTime.now().plusMinutes(10);
						user.setTime_block(newTime);
						time_block = user.getTime_block();
						login_attempts = 0;
					}
			
				}
				
			}
			
			userRepository.userTimeBlock(login_attempts, time_block, id);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
			}
		}

	@GetMapping("/changepassword")
	public String changePassword(ModelMap model) {
		boolean session = loggedUser.isLogged();
		if (session) {
			model.addAttribute("name", loggedUser.getName());
			return "changepassword";
		}
		return "redirect:/login";
	}

	@PostMapping("/changepassword")
	public ResponseEntity<String> updatePassword(@RequestParam String oldPass, @RequestParam String newPass) {
		boolean logged = loggedUser.isLogged();

		if (logged) {
			boolean valid = userService.validateOldPassword(oldPass);

			if (valid) {
				int id = loggedUser.getUserId();
				userService.updatePassword(newPass, id);
				return ResponseEntity.status(HttpStatus.OK).body("Password changed successfully!");
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Your current password doesn´t match.");
			}
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("An error occurred!Please try again later.");
		}
	}

	@GetMapping("/search/user/{task_id}")
	public String searchUser(@PathVariable String task_id, RedirectAttributes redirAttrs, ModelMap model){
		boolean session = loggedUser.isLogged();
		if (session) {
			String company = loggedUser.getCompany();
			
			if (!company.isEmpty()) {
				Iterable<User> usersCompany = userRepository.findUserWithSameCompany(company);
								
				model.addAttribute("usersCompany", usersCompany);
				model.addAttribute("taskId", task_id);
				model.addAttribute("name", loggedUser.getName());
				return "adduser";
			} else {
				redirAttrs.addFlashAttribute("error", "You don't have company.");
				return "redirect:/alltasks";
			}
		}
		return "redirect:/login";
	}

	@GetMapping("/exit")
	public String logout(HttpSession session) {
		session.invalidate();
		loggedUser.logOff();
		return "redirect:/login";
	}

	@PostMapping("/sendcontact")
	public String sendContact(@RequestParam String userEmail, @RequestParam String subject,
			@RequestParam String message, RedirectAttributes redirAttrs){
		String passwordEmail = mail.getPasswordMail();
		try {
			Mail mm = new Mail();
			String sender = mm.sendContactEmail(userEmail, subject, message, passwordEmail);
			if(sender.equalsIgnoreCase("success")) {
				redirAttrs.addFlashAttribute("success", "Email sent with success!");
			}else {
				redirAttrs.addFlashAttribute("error", sender);
			}
			
		} catch (Exception e) {
			String error = e.getMessage();
			redirAttrs.addFlashAttribute("error", error);
		}
		return "redirect:/index";
	}

	@GetMapping("/uploadimage")
	public String uploadImage(ModelMap model) {
		boolean logged = loggedUser.isLogged();
		if (logged) {
			model.addAttribute("name", loggedUser.getName());
			return "uploadimage";
		} else {
			return "redirect:/login";
		}
	}

	@PostMapping("/save/image")
	public String saveUserImage(@RequestParam("image") MultipartFile file, RedirectAttributes redirAttrs){
		boolean session = loggedUser.isLogged();
		String images = file.getContentType();
	
		if (session) {
			if(images.endsWith("jpeg") || images.endsWith("png")) {
			try {
				userService.saveImg(file);
				redirAttrs.addFlashAttribute("success", "Image saved with success!");
			} catch (IOException e) {
				redirAttrs.addFlashAttribute("error", "A technical error occurred. Please try again later.");
			}
				return "redirect:/uploadimage";
			}else {
				redirAttrs.addFlashAttribute("error", "Extension not allowed!");
				return "redirect:/uploadimage";
			}		
		}
			return "redirect:/login";
	}

	@GetMapping("/getimage")
	@ResponseBody
	public byte[] getUserImage(RedirectAttributes redirAttrs){
		byte[] photo = userService.getUserImg();
			return photo;
		}
}