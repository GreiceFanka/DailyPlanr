package dailyPlanr.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
import org.springframework.beans.factory.annotation.Autowired;
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

import dailyPlanr.models.User;
import dailyPlanr.models.UserRepository;
import jakarta.validation.Valid;

@Controller
public class UserController {

	@Autowired
	private UserRepository userRepository;

	private final PasswordEncoder encoder;

	@Inject
	private LoggedUser loggedUser;

	@Inject
	private Mail mail;

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
	public String newUser(@Valid User user, RedirectAttributes redirAttrs) {
		user.setPassword(encoder.encode(user.getPassword()));
		boolean isEmail = false;
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(user.getLogin());

		if (matcher.matches()) {
			isEmail = true;
		}

		Optional<User> opUser = userRepository.findByLogin(user.getLogin());

		if (opUser.isEmpty() && isEmail) {
			userRepository.save(user);
			redirAttrs.addFlashAttribute("success", "Everything went just fine.");
			return "redirect:/login";
		} else if (!isEmail) {
			redirAttrs.addFlashAttribute("error", "Invalid email format, please try again.");
		} else {
			redirAttrs.addFlashAttribute("error", "User already registered");
		}
		return "redirect:/signup";
	}

	@GetMapping("/allusers")
	public @ResponseBody Iterable<User> getAllUsers() {
		return userRepository.findAll();
	}

	@PostMapping("/passwordcheck")
	public String validatePassword(@RequestParam String login,@RequestParam String password,
			RedirectAttributes redirAttrs) {

		Optional<User> opUser = userRepository.findByLogin(login);

		if (opUser.isEmpty()) {
			redirAttrs.addFlashAttribute("error", "Login not found.");
			return "redirect:/login";
		}

		User user = opUser.get();
		boolean valid = encoder.matches(password, user.getPassword());
		if (valid) {
			this.loggedUser.setUserLogged(user);
			return "redirect:/alltasks";
		} else {
			redirAttrs.addFlashAttribute("error", "Incorrect Password!");
			return "redirect:/login";
		}
	}

	@GetMapping("/changepassword")
	public String changePassword(ModelMap model) {
		boolean session = loggedUser.isLogged();
		if (session) {
			model.addAttribute("name", loggedUser.getName());
			return "/changepassword";
		}
		return "redirect:/login";
	}

	@PostMapping("/changepassword")
	public String updatePassword(@RequestParam String oldPass, @RequestParam String newPass,
			RedirectAttributes redirAttrs) {
		boolean logged = loggedUser.isLogged();

		if (logged) {
			String login = loggedUser.getLoginUser();
			Optional<User> opUser = userRepository.findByLogin(login);
			User user = opUser.get();
			boolean valid = encoder.matches(oldPass, user.getPassword());

			if (valid) {
				int id = loggedUser.getUserId();
				String password = (encoder.encode(newPass));
				userRepository.updatePassword(password, id);
				redirAttrs.addFlashAttribute("success", "New password save with success.");
				return "redirect:/changepassword";
			} else {
				redirAttrs.addFlashAttribute("error", "Your password does not match.");
				return "redirect:/changepassword";
			}
		} else {
			return "redirect:/login";
		}
	}

	@GetMapping("/search/user/{task_id}")
	public String searchUser(@PathVariable int task_id, RedirectAttributes redirAttrs, ModelMap model) {
		int taskId = task_id;
		String company = loggedUser.getCompany();

		if (!company.isEmpty()) {
			Iterable<User> usersCompany = userRepository.findUserWithSameCompany(company);
			model.addAttribute("usersCompany", usersCompany);
			model.addAttribute("taskId", taskId);
			model.addAttribute("name", loggedUser.getName());
			return "/adduser";
		} else {
			redirAttrs.addFlashAttribute("error", "You don't have company.");
			return "redirect:/alltasks";
		}
	}

	@GetMapping("/exit")
	public String logout() {
		loggedUser.logOff();
		return "redirect:/login";
	}

	@PostMapping("/sendcontact")
	public String sendContact(@RequestParam String userEmail, @RequestParam String subject,
			@RequestParam String message, RedirectAttributes redirAttrs) throws EmailException {
		String passwordEmail = mail.getPasswordMail();
		try {
			Mail.sendContactEmail(userEmail, subject, message, passwordEmail);
			redirAttrs.addFlashAttribute("success", "Email sent with success!");
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
			return "/uploadimage";
		} else {
			return "redirect:/login";
		}
	}

	@PostMapping("/save/image")
	public String saveUserImage(@RequestParam("image") MultipartFile file, RedirectAttributes redirAttrs)
			throws IOException {
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
	}

	@GetMapping("/getimage")
	@ResponseBody
	public byte[] getUserImage() throws IOException {
		int id = loggedUser.getUserId();
		Optional<User> user = userRepository.findById(id);
		byte [] photo = null;
		byte [] image = user.get().getImage();
		
		if(image != null) {
			return image;
		}else {
			try {
				BufferedImage rd = ImageIO.read(new File("/home/greice-dev/eclipse-workspace/dailyplanr/src/main/resources/static/images/perfil.png"));
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