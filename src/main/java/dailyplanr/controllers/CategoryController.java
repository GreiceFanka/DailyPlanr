package dailyplanr.controllers;

import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dailyplanr.models.Category;
import dailyplanr.models.CategoryRepository;
import dailyplanr.models.User;
import jakarta.validation.Valid;

@Controller
public class CategoryController {

	@Autowired
	private CategoryRepository categoryRepository;

	@Inject
	private LoggedUser loggedUser;

	@GetMapping("/allcategories")
	public String getAllCategories(ModelMap model)throws Exception {
		boolean session = loggedUser.isLogged();
		if (session) {
			int id = loggedUser.getUserId();
			List<Category> categories = categoryRepository.findCategoryByUser(id);
			for (Category category : categories) {
				int catId = category.getId();
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
			
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("categories", categories);
			return "allcategories";
		}
			return "redirect:/login";
	}

	@PostMapping("/create/categories")
	public String createCategory(@Valid Category category, RedirectAttributes redirAttrs) {
		boolean session = loggedUser.isLogged();
		int user = loggedUser.getUserId();
		boolean exists = false;
		String catName = category.getCategoryName();
		
		if (session) {
			List<Category> catExists = categoryRepository.findCategoryByUser(user);
			for (Category cat : catExists) {
				String userCategoryName = cat.getCategoryName();
				if(userCategoryName.equalsIgnoreCase(catName)) {
					exists = true;
				}
			}
				if (exists == true) {
					redirAttrs.addFlashAttribute("error", "This category already exists.");
					return "redirect:/newcategory";
				}else {
						User u = new User();
						u.setId(user);
						category.addUsersCategory(u);
						categoryRepository.save(category);
						return "redirect:/allcategories";
				}
		}
		return "redirect:/login";
	}

	@GetMapping("/edit/category/{id}")
	public String editCategory(@PathVariable String id, ModelMap model) {
		boolean session = loggedUser.isLogged();
		int categoryId = categoryRepository.findCategory(id);
		if (session) {
			List<Category> categories = categoryRepository.findCategoryById(categoryId);
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("categories", categories);
			return "editcategory";
		}
		return "redirect:/login";
	}

	@PostMapping("/update/category")
	public String updateCategory(@RequestParam String categoryName, String id, ModelMap model) {
		boolean session = loggedUser.isLogged();
		int categoryId = categoryRepository.findCategory(id);
		if (session) {
			categoryRepository.updateCategory(categoryName, categoryId);
			model.addAttribute("name", loggedUser.getName());
			return "redirect:/allcategories";
		}
		return "redirect:/login";
	}

	@GetMapping("/newcategory")
	public String newCategory(ModelMap model) {
		boolean session = loggedUser.isLogged();
		if (session) {
			model.addAttribute("name", loggedUser.getName());
			return "newcategory";
		}
		return "redirect:/login";
	}
}
