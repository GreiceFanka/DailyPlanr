package dailyplanr.service;

import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dailyplanr.models.Category;
import dailyplanr.models.CategoryRepository;
import dailyplanr.models.User;

@Service
public class CategoryKeys {
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	public void createKey(User user,Category category) throws Exception {
		int u = user.getId();
		List<Category> categories = categoryRepository.findCategoryByUser(u);
		for (Category cat : categories) {
				int catId = cat.getId();
				String cId = Integer.toString(catId);
				IvParameterSpec iv = Security.iv();
				SecretKey symmetricKey = Security.secretKey();
				byte[] cipherText = Security.encrypt(cId, symmetricKey, iv);
				
				String categoryEncId = Base64.getUrlEncoder().withoutPadding().encodeToString(cipherText);
				category.setCatId(categoryEncId);
				byte[] cIv = iv.getIV();
				byte[] cKey = symmetricKey.getEncoded();
				String base64Iv = Base64.getEncoder().encodeToString(cIv);
				categoryRepository.saveKeys(categoryEncId, base64Iv, cKey, catId);
			}
	}

}
