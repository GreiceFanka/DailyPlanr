package dailyplanr.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.keygen.KeyGenerators;
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
import dailyplanr.models.CategoryRepository;
import dailyplanr.models.User;
import dailyplanr.models.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	private final PasswordEncoder encoder;

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
		Optional<User> validToken = userRepository.findToken(token);
		
		if(validToken.isEmpty()) {
			return "redirect:/index";
		}else if(validToken.isPresent() && token.length() >= 16) {
			return "userpass";
		}
		return "redirect:/index";
	}
	
	@PostMapping("/tokenpasschange")
	public ResponseEntity<String> tokenPassChange(String email, String currentPassword, String newPassword, String token){
		Optional<User> findUser = userRepository.findByLogin(email);
		User user = findUser.get();
		String userToken = user.getToken();
		LocalDateTime time = LocalDateTime.now();
	
		
		if(findUser.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
		}else {
			if(userToken.equals(token) && user.getTemporary_salt().isAfter(time)) {
				String currentPass = currentPassword.concat(user.getSalt());
				boolean valid = encoder.matches(currentPass, user.getPassword());
				
				if(valid) {
					String salt = KeyGenerators.string().generateKey();
					user.setSalt(salt);
					String password = (encoder.encode(newPassword.concat(salt)));
					user.setTemporary_salt(null);
					user.setToken("");
					int id = user.getId();
					userRepository.updatePassword(password, id);
					return ResponseEntity.status(HttpStatus.OK).body("Password changed successfully!");
					
				}else {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Your current password doesn´t match.");
				}
		}else {
			int id = user.getId();
			userRepository.userDestroyToken("", null, id);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid data.");
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
					redirAttrs.addFlashAttribute("success", "An email was sent, you have five minutes to change your password.");
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
	public ResponseEntity<String> newUser(@Valid User user, RedirectAttributes redirAttrs) {
		String salt = KeyGenerators.string().generateKey();
		user.setSalt(salt);
		user.setPassword(encoder.encode(user.getPassword().concat(salt)));
		boolean isEmail = false;
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(user.getLogin());

		if (matcher.matches()) {
			isEmail = true;
		}

		Optional<User> opUser = userRepository.findByLogin(user.getLogin());

		if (opUser.isEmpty() && isEmail) {
			user.setTime_block(null);
			user.setLogin_attempts(0);
			userRepository.save(user);
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
			time_block = user.getTime_block();
			Duration duration = Duration.between(time_now, unblockTime);
			duration = duration.plusMinutes(1);
			min_block = duration.toMinutesPart();
			return ResponseEntity.status(HttpStatus.LOCKED).body("Blocked for " + min_block + " minutes due to multiple tentatives!Try again later.");
			
		}else {
			if(user.getSalt() != null) {
				String encodedPass = password.concat(user.getSalt());
				
				boolean valid = encoder.matches(encodedPass, user.getPassword());
				
				if (valid) {
					session.invalidate();
					HttpSession newSession = request.getSession(true);
					newSession.setAttribute("user", user.getLogin());
					newSession.setMaxInactiveInterval(30 * 60);
					this.loggedUser.setUserLogged(user);
				
					List<Category> listCat = categoryRepository.findCategoryByUser(loggedUser.getUserId());
					if (listCat.isEmpty()) {
						Category category = new Category();
						category.setCategoryName("Default");
						category.addUsersCategory(user);
						categoryRepository.save(category);
					}
					userRepository.userTimeBlock(login_attempts, time_block, id);
					return ResponseEntity.status(HttpStatus.OK).body("Success");
				} else {
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
			String login = loggedUser.getLoginUser();
			Optional<User> opUser = userRepository.findByLogin(login);
			User user = opUser.get();
			String lastPass = oldPass.concat(user.getSalt());
			boolean valid = encoder.matches(lastPass, user.getPassword());		

			if (valid) {
				int id = loggedUser.getUserId();
				String salt = KeyGenerators.string().generateKey();
				user.setSalt(salt);
				String password = (encoder.encode(newPass.concat(salt)));
				userRepository.updatePassword(password, id);
				return ResponseEntity.status(HttpStatus.OK).body("Password changed successfully!");
				
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Your current password doesn´t match.");
				
			}
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("An error occurred!Please try again later.");
		}
	}

	@GetMapping("/search/user/{task_id}")
	public String searchUser(@PathVariable String task_id, RedirectAttributes redirAttrs, ModelMap model) throws Exception {
		boolean session = loggedUser.isLogged();
		if (session) {
			String company = loggedUser.getCompany();
			
			if (!company.isEmpty()) {
				Iterable<User> usersCompany = userRepository.findUserWithSameCompany(company);
				for (User user : usersCompany) {
					int uid = user.getId();
					String userId = Integer.toString(uid);
					IvParameterSpec iv = Security.iv();
					SecretKey symmetricKey = Security.secretKey();
					byte[] cipherText = Security.encrypt(userId, symmetricKey, iv);
					
					String userEncryptId = Base64.getUrlEncoder().withoutPadding().encodeToString(cipherText);
					user.setHashu(userEncryptId);
					byte[] uIv = iv.getIV();
					byte[] uKey = symmetricKey.getEncoded();
					String base64Iv = Base64.getEncoder().encodeToString(uIv);
					userRepository.saveKeys(userEncryptId, base64Iv, uKey, uid);
				}
				
				
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
			@RequestParam String message, RedirectAttributes redirAttrs) throws EmailException {
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
	public String saveUserImage(@RequestParam("image") MultipartFile file, RedirectAttributes redirAttrs)
			throws IOException {
		boolean session = loggedUser.isLogged();
		String images = file.getContentType();
	
		if (session) {
			if(images.endsWith("jpeg") || images.endsWith("png")) {
			try {
				byte[] image = file.getBytes();
				int user_id = loggedUser.getUserId();
				userRepository.saveImageById(image, user_id);
				redirAttrs.addFlashAttribute("success", "Image saved with success!");
			} catch (Exception e) {
				String error = e.getMessage();
				redirAttrs.addFlashAttribute("error", error);
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
	public byte[] getUserImage() throws IOException {
		int id = loggedUser.getUserId();
		Optional<User> user = userRepository.findById(id);
		byte[] photo = null;
		byte[] image = user.get().getImage();

		if (image != null) {
			return image;
		} else {
			try (InputStream is = getClass().getResourceAsStream("/static/images/perfil.png")) {
				BufferedImage rd = ImageIO.read(is);
				ByteArrayOutputStream wr = new ByteArrayOutputStream();
				ImageIO.write(rd, "png", wr);
				photo = wr.toByteArray();

			} catch (Exception e) {
				e.getMessage();
			}
			return photo;
		}
	}
}