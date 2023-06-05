package dailyPlanr.controllers;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
	public String tasks(RedirectAttributes redirAttrs) {
		boolean session = loggedUser.isLogged();
		Iterable<Category> category = categoryRepository.findAll();
		if(session) {			
			return "/newtask";
		}
		return "redirect:/login";
	}
	
	@PostMapping("/newtask/create/")
	public String newTask(@Valid Task task,@Valid User user, RedirectAttributes redirAttrs) {
		if(user != null) {
			task.addUser(user);
			taskRepository.save(task);
			return "/newtask";
		}else {
			redirAttrs.addFlashAttribute("error", "Something went wrong.");
			return "/newtask";
		}
	}
	
	@GetMapping("/alltasks")
	public @ResponseBody Iterable<Task> getAllTasks() {
		return taskRepository.findAll();
	}
}
