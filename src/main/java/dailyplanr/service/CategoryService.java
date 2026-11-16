package dailyplanr.service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

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
import dailyplanr.models.UserRepository;

@Service
public class CategoryService {
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private UserRepository userRepository;
	
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
	
	@Transactional
	public void changeTaskCategory(String catId) throws Exception {
	    long categoryId = categoryRepository.findCategory(catId);
		Category defaultCategory = getOrCreateDefaultCategory();

	    categoryRepository.changeTaskCategory(
	        defaultCategory.getId(),
	        categoryId
	    );
	    deleteCategory(categoryId);
	}
	
	private Category getOrCreateDefaultCategory() throws Exception {
		List<Category> categories = findCategoryByUser();
		Optional<Category> defaultCategory = findDefaultCategory(categories);
		  if (defaultCategory.isPresent()) {
		        return defaultCategory.get();
		  }

		User user = findUserById();
		Category category = createDefaultCategory(user);
		createKey(user, category);
		return category;
	}
	
	public Optional<Category> findDefaultCategory(List<Category> categories){
	    Optional<Category> defaultCategory = categories.stream()
	            .filter(cat -> cat.getCategoryName().equalsIgnoreCase("Default"))
	            .findFirst();
	    return defaultCategory;
	}
	
	private User findUserById() {
		long userId = loggedUser.getUserId();
		User user = userRepository.findById(userId)
	        .orElseThrow(() -> new RuntimeException("User not found"));
		return user;
	}
	
	@Transactional
	private void deleteCategory(long categoryId) {
		long user_id = loggedUser.getUserId();
		categoryRepository.deleteCategory(categoryId, user_id);
	}
	
	@Transactional
	public Category createDefaultCategory(User user){
		Category category = new Category();
		category.setCategoryName("Default");
		category.addUsersCategory(user);
		categoryRepository.save(category);
		return category;
	}

}
