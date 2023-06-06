package dailyPlanr.controllers;

import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dailyPlanr.models.Category;
import dailyPlanr.models.CategoryRepository;
import dailyPlanr.models.Task;
import dailyPlanr.models.TaskRepository;
import dailyPlanr.models.User;
import jakarta.validation.Valid;

@Controller
public class TaskController {
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Inject
	private LoggedUser loggedUser;
	
	@GetMapping("/newtask")
	public String tasks(ModelMap model) {
		boolean session = loggedUser.isLogged();
		if(session) {	
			List<Category> listCategories = categoryRepository.findAll();
			model.addAttribute("login", loggedUser.getLoginUser());
			model.addAttribute("user", loggedUser.getUserId());
			model.addAttribute("categories", listCategories);
			return "/newtask";
		}
		return "redirect:/login";
	}
	
	@PostMapping("/newtask/create/")
	public String newTask(@Valid Task task,@Valid User user, RedirectAttributes redirAttrs) {
		if(user != null) {
			task.addUser(user);
			taskRepository.save(task);
			redirAttrs.addFlashAttribute("success","Everything went just fine.");
			return "redirect:/newtask";
		}else {
			redirAttrs.addFlashAttribute("error", "Something went wrong, try again.");
			return "/newtask";
		}
	}
	
	@GetMapping("/alltasks")
	public String getAllTasks(ModelMap model) {
		boolean session = loggedUser.isLogged();
		int id = loggedUser.getUserId();
		Iterable<Task> allTasks = taskRepository.findTaskByUser(id);
		if(session) {
			model.addAttribute("login", loggedUser.getLoginUser());
			model.addAttribute("user", loggedUser.getUserId());
			model.addAttribute("tasks", allTasks);
		}
		return "/alltasks";
	}
	
	@GetMapping("/task")
	public String bringMeTasks() {
		return "/task";
	}
}
