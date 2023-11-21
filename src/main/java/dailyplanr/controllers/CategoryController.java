package dailyplanr.controllers;

import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
	public String getAllCategories(ModelMap model) {
		boolean session = loggedUser.isLogged();
		int id = loggedUser.getUserId();
		List<Category> categories = categoryRepository.findCategoryByUser(id);
		if (session) {
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("user", loggedUser.getUserId());
			model.addAttribute("categories", categories);
			return "/allcategories";
		}
		return "redirect:/login";
	}

	@PostMapping("/create/categories")
	public String createCategory(@Valid Category category, @Valid User user) {
		boolean session = loggedUser.isLogged();
		if (session) {
			category.addUsersCategory(user);
			categoryRepository.save(category);
			return "redirect:/allcategories";
		}
		return "redirect:/login";
	}

	@GetMapping("/edit/category/{id}")
	public String editCategory(@PathVariable int id, ModelMap model) {
		boolean session = loggedUser.isLogged();
		if (session) {
			List<Category> categories = categoryRepository.findCategoryById(id);
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("user", loggedUser.getUserId());
			model.addAttribute("categories", categories);
			return "/editcategory";
		}
		return "redirect:/login";
	}

	@PostMapping("/update/category")
	public String updateCategory(@RequestParam String categoryName, @RequestParam int id, ModelMap model) {
		boolean session = loggedUser.isLogged();
		if (session) {
			categoryRepository.updateCategory(categoryName, id);
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("user", loggedUser.getUserId());
			return "redirect:/allcategories";
		}
		return "redirect:/login";
	}

	@GetMapping("/newcategory")
	public String newCategory(ModelMap model) {
		boolean session = loggedUser.isLogged();
		if (session) {
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("user", loggedUser.getUserId());
			return "/newcategory";
		}
		return "redirect:/login";
	}
}
