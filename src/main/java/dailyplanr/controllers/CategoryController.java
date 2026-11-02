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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dailyplanr.models.Category;
import dailyplanr.models.CategoryRepository;
import dailyplanr.models.User;
import dailyplanr.service.CategoryService;
import jakarta.validation.Valid;

@Controller
public class CategoryController {

	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private CategoryService categoryService;

	@Inject
	private LoggedUser loggedUser;
	
	@GetMapping("/allcategories")
	public String getAllCategories(ModelMap model){
		boolean session = loggedUser.isLogged();
		if (session) {
			List<Category> categories = categoryService.findCategoryByUser();			
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("categories", categories);
			return "allcategories";
		}
			return "redirect:/login";
	}

	@PostMapping("/create/categories")
	public String createCategory(@Valid Category category, RedirectAttributes redirAttrs){
		boolean session = loggedUser.isLogged();
		if (session) {
			boolean exists = categoryService.categoryExists(category);
				if (exists) {
					redirAttrs.addFlashAttribute("error", "This category already exists.");
					return "redirect:/newcategory";
				}else if(!category.getCategoryName().isEmpty()){
						User u = new User();
						u.setId(loggedUser.getUserId());
						category.addUsersCategory(u);
						categoryRepository.save(category);
						
						try {
							categoryService.createKey(u, category);
						} catch (Exception e) {
							e.printStackTrace();
							redirAttrs.addFlashAttribute("error", "A technical error ocurred. Please try again later.");
							return "redirect:/newcategory";
						}
					return "redirect:/allcategories";
				}else {
					redirAttrs.addFlashAttribute("error", "Category name must not be empty.");
					return "redirect:/newcategory";
				}
		}
		return "redirect:/login";
	}

	@GetMapping("/edit/category/{id}")
	public String editCategory(@PathVariable String id, ModelMap model) {
		boolean session = loggedUser.isLogged();
		if (session) {
			List<Category> categories = categoryService.findCategory(id);
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("categories", categories);
			return "editcategory";
		}
		return "redirect:/login";
	}

	@PostMapping("/update/category")
	public String updateCategory(@RequestParam String categoryName, String id, ModelMap model,RedirectAttributes redirAttrs) {
		boolean session = loggedUser.isLogged();
		if (session) {
			if(!categoryName.isEmpty()) {
			categoryService.updateCategory(categoryName, id);
			model.addAttribute("name", loggedUser.getName());
			return "redirect:/allcategories";
			}else {
				redirAttrs.addFlashAttribute("error", "Category name must not be empty.");
				return "redirect:/edit/category/"+ id;
			}
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
