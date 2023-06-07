package dailyPlanr.controllers;

import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import dailyPlanr.models.Category;
import dailyPlanr.models.CategoryRepository;
import dailyPlanr.models.User;
import jakarta.validation.Valid;
@RestController
public class CategoryController {
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Inject
	private LoggedUser loggedUser;
	
	@GetMapping("/allcategories")
	public String getAllCategories(ModelMap model){
		boolean session = loggedUser.isLogged();
		int id = loggedUser.getUserId();
		List<Category> categories = categoryRepository.findCategoryByUser(id);
		if(session) {
			model.addAttribute("login", loggedUser.getLoginUser());
			model.addAttribute("user", loggedUser.getUserId());
			model.addAttribute("categories", categories);
			return "/allcategories";
		}
		return "redirect:/login";
	}
	
	@PostMapping("/create/categories")
		public String createCategory(@Valid Category category, @Valid User user) {
			category.addUsersCategory(user);
			categoryRepository.save(category);
			return "salvo";
	}
}
