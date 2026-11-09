package dailyplanr.service;

import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dailyplanr.controllers.LoggedUser;
import dailyplanr.models.Category;
import dailyplanr.models.CategoryRepository;
import dailyplanr.models.User;

@Service
public class CategoryService {
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Inject
	private LoggedUser loggedUser;
	
	@Transactional
	public void createKey(User user,Category category) throws Exception {
		long u = user.getId();
		List<Category> categories = categoryRepository.findCategoryByUser(u);
		for (Category cat : categories) {
				long catId = cat.getId();
				String cId = Long.toString(catId);
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
	
	@Transactional
	public void updateCategory (String categoryName,String id) {
		long categoryId = categoryRepository.findCategory(id);
		categoryRepository.updateCategory(categoryName, categoryId);
	}
	
	public boolean categoryExists(Category category) {
		String catName = category.getCategoryName();
		List<Category> categories = findCategoryByUser();
		boolean exists = categories.stream()
		        .anyMatch(cat -> cat.getCategoryName().equalsIgnoreCase(catName));
		return exists;
	}
	
	public List<Category> findCategoryByUser(){
		long user = loggedUser.getUserId();
		List<Category> categories = categoryRepository.findCategoryByUser(user);
		return categories;
	}
	
	public List<Category> findCategory(String id) {
		long categoryId = categoryRepository.findCategory(id);
		List<Category> categories = categoryRepository.findCategoryById(categoryId);
		return categories;
	}

}
