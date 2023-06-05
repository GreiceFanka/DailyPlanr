package dailyPlanr.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import dailyPlanr.models.Category;
import dailyPlanr.models.CategoryRepository;
import jakarta.validation.Valid;
@RestController
public class CategoryController {
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@GetMapping("/allcategories")
	public @ResponseBody Iterable<Category> getAllCategories(){
		return categoryRepository.findAll();
	}
	
	@PostMapping("/create/categories")
		public String createCategory(@Valid Category category) {
			categoryRepository.save(category);
			return "salvo";
	}
}
