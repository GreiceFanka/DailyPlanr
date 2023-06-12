package dailyPlanr.controllers;

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

import dailyPlanr.models.Category;
import dailyPlanr.models.CategoryRepository;
import dailyPlanr.models.Status;
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
		int id = loggedUser.getUserId();
		if(session) {	
			List<Category> listCategories = categoryRepository.findCategoryByUser(id);
			
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
			return "/alltasks";
		}
		return "redirect:/login";
	}
	
	@GetMapping("/delete/task/{id}")
	public String deleteTask(@PathVariable int id) {
		taskRepository.deleteById(id);
		return "redirect:/alltasks";
	}
	
	@GetMapping("/edit/task/{id}")
	public String editTask(@PathVariable int id, ModelMap model, Status status) {
		int user_id = loggedUser.getUserId();
		
		List<Task> tasks = taskRepository.findTaskById(id);
		List<Category> listCategories = categoryRepository.findCategoryByUser(user_id);
		
		List<String> allStatus = Status.getAllStatus();
		
		model.addAttribute("login", loggedUser.getLoginUser());
		model.addAttribute("user", loggedUser.getUserId());
		model.addAttribute("tasks", tasks);
		model.addAttribute("categories", listCategories);
		model.addAttribute("status",allStatus);
		return "/updatetask";
	}
	
	@PostMapping("/update/task")
	public String updateTask(@RequestParam String data,@RequestParam String title,@RequestParam String description,@RequestParam String taskStatus,@RequestParam int cat_id,@RequestParam int task_id) {
		taskRepository.updateTask(data, title, description, taskStatus, cat_id, task_id);
		return "redirect:/alltasks";
	}
}
