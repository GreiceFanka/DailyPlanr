package dailyplanr.service;


import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dailyplanr.controllers.LoggedUser;
import dailyplanr.models.Category;
import dailyplanr.models.CategoryRepository;
import dailyplanr.models.User;
import dailyplanr.models.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Inject
	private LoggedUser loggedUser;
	
	@Inject
	private Mail mail;
	
	private final PasswordEncoder encoder;
	
	public UserService(PasswordEncoder encoder) {
		this.encoder = encoder;
	}
	
	public int decryptHash(String hashu) throws Exception {
		
		Optional<User> userInf = userRepository.findUsrInf(hashu);
		
		byte [] ukey = userInf.get().getSymmetricKey();
		SecretKey oKey = new SecretKeySpec(ukey, "AES");
		
		byte[] uIv = Base64.getDecoder().decode(userInf.get().getIv());
		IvParameterSpec uIvSpec = new IvParameterSpec(uIv);
		
		byte[] uCipherText = Base64.getUrlDecoder().decode(hashu);
		String decryptHashu = Security.decrypt(uCipherText, oKey, uIvSpec);
		int decryptUserId = Integer.parseInt(decryptHashu);		
		return decryptUserId;
	}
	
	public byte[] getUserTaskImg(String tempId) {
		Optional<User> userInf = userRepository.findTempId(tempId);
		int id = userInf.get().getId();
		
		return userRepository.findById(id)
	            .map(User::getImage)
	            .filter(image -> image != null && image.length > 0)
	            .orElseGet(this::getUserDefaultTaskImg);
	}
	
	private byte[] getUserDefaultTaskImg() {
		try(InputStream is = getClass().getResourceAsStream("/static/images/user.jpg")){
			return is.readAllBytes();
			
		} catch (IOException e) {
			throw new IllegalStateException("A technical error occurred.", e);
		}
	
	}
	
	public void createKeys(int uId) throws Exception {
		
		String userId = Integer.toString(uId);
		IvParameterSpec iv = Security.iv();
		SecretKey symmetricKey = Security.secretKey();
		byte[] cipherText = Security.encrypt(userId, symmetricKey, iv);
		
		String userEncryptId = Base64.getUrlEncoder().withoutPadding().encodeToString(cipherText);
		byte[] uIv = iv.getIV();
		byte[] uKey = symmetricKey.getEncoded();
		String base64Iv = Base64.getEncoder().encodeToString(uIv);
		userRepository.saveKeys(userEncryptId, base64Iv, uKey, uId);
	}
	
	public void saveImg(MultipartFile file) throws IOException{
		byte[] image = file.getBytes();
		int user_id = loggedUser.getUserId();
		userRepository.saveImageById(image, user_id);
	}
	
	public byte[] getUserImg(){
		int userId = loggedUser.getUserId();

		return userRepository.findById(userId)
		       .map(User::getImage)
		       .filter(image -> image != null && image.length > 0)
		       .orElseGet(this::getDefaultUserImage);
	}

	private byte[] getDefaultUserImage() {
		try (InputStream inputStream = getClass()
		      .getResourceAsStream("/static/images/perfil.png")) {
		    return inputStream.readAllBytes();

		} catch (IOException e) {
		    throw new IllegalStateException("A technical error occurred.", e);
		}
	}
	
	public Matcher createUserPass(User user) {
		String salt = KeyGenerators.string().generateKey();
		user.setSalt(salt);
		String tempId = KeyGenerators.string().generateKey();
		user.setTempId(tempId);
		user.setPassword(encoder.encode(user.getPassword().concat(salt)));
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(user.getLogin());
		
		return matcher;

	}
	
	public Optional<String> verifyUserLogin(User user) {
		return userRepository.findByLogin(user.getLogin()).map(User::getLogin);
	}
	
	public int saveNewUser(User user) {
		user.setTime_block(null);
		user.setLogin_attempts(0);
		userRepository.save(user);
		Optional<User> u = userRepository.findByLogin(user.getLogin());
		int id = u.get().getId();
		return id;
	}
	
	public Category createDefaultCategory(User user){
		Category category = new Category();
		category.setCategoryName("Default");
		category.addUsersCategory(user);
		categoryRepository.save(category);
		return category;
	}
	
	public void setNewSession(User user,int login_attempts,LocalDateTime time_block, int id,HttpSession session, HttpServletRequest request){
		session.invalidate();
		HttpSession newSession = request.getSession(true);
		newSession.setAttribute("user", user.getLogin());
		newSession.setMaxInactiveInterval(30 * 60);
		this.loggedUser.setUserLogged(user);
		userRepository.userTimeBlock(login_attempts, time_block, id);
	}
	
	public boolean validateOldPassword(String oldPass) {
		User user = userRepository.findByLogin(loggedUser.getLoginUser()).orElseThrow();
		String lastPass = oldPass.concat(user.getSalt());
		boolean valid = encoder.matches(lastPass, user.getPassword());		
		return valid;
	}
	
	public void updatePassword(String newPass, int id) {
		String salt = KeyGenerators.string().generateKey();
		String password = encoder.encode(newPass.concat(salt));
		userRepository.updatePassword(password, salt, id);
	}
	
	public void destroyToken(int id) {
		LocalDateTime temporary_salt = null;
		String token = "";
		userRepository.userDestroyToken(token, temporary_salt, id);
	}
	
	public Optional<User> findUserByToken(String token) {
		return userRepository.findByToken(token);
	}
	
	public String sendEmailToResetPassword(User user){
		try {
			String email = user.getLogin();
			String name = user.getName();
			String passwordEmail = mail.getPasswordMail();
			String salt = user.getSalt();
			String token = encoder.encode(email.concat(salt));
			token = token.replaceAll("/", "");
			token = token.replace(".", "");
			int id = user.getId();
			String sender = mail.sendResetPassword(email, name, token, passwordEmail);
			if(sender.equalsIgnoreCase("success")) {
				saveEmailData(token,id);
				return sender;
			}else {
				throw new EmailException();
			}
		} catch (EmailException e) {
			return "A technical error occurred. Please try again later.";
		}
	}
	
	private void saveEmailData(String token, int id) {
		LocalDateTime time = LocalDateTime.now().plusMinutes(5);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
		time.format(dateTimeFormatter);
		userRepository.saveTemporary(time, token, id);
	}
	
	public Optional<User> findLogin(String login){
		return userRepository.findByLogin(login);
	}
	
	public int configMinutesUserBlock(LocalDateTime time_now,LocalDateTime unblockTime) {
		Duration duration = Duration.between(time_now, unblockTime);
		duration = duration.plusMinutes(1);
		int min_block = duration.toMinutesPart();
		return min_block;
	}
	
	public int configLoginAttempts(User user) {
		int login_attempts = user.getLogin_attempts();
		user.setLogin_attempts(login_attempts++);
		return login_attempts;
	}
	
	public void setUserTimeBlock(User user) {
		LocalDateTime newTime = LocalDateTime.now().plusMinutes(10);
		user.setTime_block(newTime);
		LocalDateTime time_block = user.getTime_block(); 
		int login_attempts = 0;
		saveTimeBlock(login_attempts,time_block,user.getId());
	}
	
	public void saveTimeBlock(int login_attempts, LocalDateTime time_block, int id) {
		userRepository.userTimeBlock(login_attempts, time_block, id);
	}
}
