package dailyplanr.service;


import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dailyplanr.controllers.LoggedUser;
import dailyplanr.models.User;
import dailyplanr.models.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	
	@Inject
	private LoggedUser loggedUser;
	
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
	
}
