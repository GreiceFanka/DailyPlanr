package dailyplanr.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dailyplanr.models.User;
import dailyplanr.models.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	
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
	
	public byte[] userImg(String tempId) {
		Optional<User> userInf = userRepository.findTempId(tempId);
		int id = userInf.get().getId();
		byte[] photo = null;
		Optional<User> users = userRepository.findById(id);
		byte[]images = users.get().getImage();
			
		try {
			if (images != null) {
				return images;
			} else {
				InputStream is = getClass().getResourceAsStream("/static/images/user.jpg");
						if (is != null) {
					        BufferedImage rd = ImageIO.read(is);
					        ByteArrayOutputStream wr = new ByteArrayOutputStream();
					        ImageIO.write(rd, "jpg", wr);
					        photo = wr.toByteArray();
					    } else {
					        System.out.println("Imagem default não encontrada!");
					    }
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
			return photo;
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
	
}
