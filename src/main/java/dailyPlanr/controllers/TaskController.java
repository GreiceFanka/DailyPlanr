package dailyPlanr.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
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
import dailyPlanr.models.Priority;
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

	@Inject
	private Mail mail;

	@GetMapping("/newtask")
	public String tasks(ModelMap model) {
		boolean session = loggedUser.isLogged();

		if (session) {
			int id = loggedUser.getUserId();
			List<Category> listCategories = categoryRepository.findCategoryByUser(id);
			List<String> allPriorities = Priority.getAllPriorities();

			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("user", loggedUser.getUserId());
			model.addAttribute("categories", listCategories);
			model.addAttribute("priorities", allPriorities);
			return "/newtask";
		}
		return "redirect:/login";
	}

	@PostMapping("/newtask/create/")
	public String newTask(@Valid Task task, @Valid User user, RedirectAttributes redirAttrs) {
		if (user != null) {
			task.addUser(user);
			taskRepository.save(task);
			redirAttrs.addFlashAttribute("success", "Everything went just fine.");
			return "redirect:/newtask";
		} else {
			redirAttrs.addFlashAttribute("error", "Something went wrong, try again.");
			return "/newtask";
		}
	}

	@GetMapping("/alltasks")
	public String getAllTasks(ModelMap model) {
		String alert = "null";
		boolean session = loggedUser.isLogged();
		if (session) {
			int id = loggedUser.getUserId();
			Iterable<Task> allTasks = taskRepository.findTaskByUser(id);
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
			now.format(dateTimeFormatter);
	
			for (Task task : allTasks) {
				int latedTasks = task.getData().compareTo(now);
				boolean toDoStatus = task.getTaskStatus().equalsIgnoreCase("To do");
				boolean inProgressStatus = task.getTaskStatus().equalsIgnoreCase("In progress");
	
				if (latedTasks <= -1 && (toDoStatus || inProgressStatus)) {
					alert = "You have late tasks!";
				}
			}
	
				model.addAttribute("name", loggedUser.getName());
				model.addAttribute("user", loggedUser.getUserId());
				model.addAttribute("tasks", allTasks);
				model.addAttribute("alert", alert);
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
		List<String> allPriorities = Priority.getAllPriorities();
 
		model.addAttribute("name", loggedUser.getName());
		model.addAttribute("user", loggedUser.getUserId());
		model.addAttribute("tasks", tasks);
		model.addAttribute("categories", listCategories);
		model.addAttribute("status", allStatus);
		model.addAttribute("priorities", allPriorities);
	
		return "/updatetask";
	}

	@PostMapping("/update/task")
	public String updateTask(@RequestParam String data, @RequestParam String title, @RequestParam String description, @RequestParam String priority,
			@RequestParam int task_id) {
		taskRepository.updateTask(data, title, description, priority, task_id);
		return "redirect:/alltasks";
	}

	@PostMapping("edit/status")
	public String editTaskStatus(@RequestParam String taskStatus, @RequestParam int id) {
		LocalDate updatedStatus = LocalDate.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		updatedStatus.format(dateTimeFormatter);
		
		taskRepository.editStatus(taskStatus,updatedStatus, id);
		return "redirect:/alltasks";
	}

	@PostMapping("edit/category")
	public String editTaskCategory(@RequestParam int cat_id, @RequestParam int id) {
		taskRepository.editTaskCategory(cat_id, id);
		return "redirect:/alltasks";
	}

	@PostMapping("/add/user")
	public String addUser(@RequestParam int taskId, @RequestParam int userId, RedirectAttributes redirectAttributes) {
		taskRepository.insertUserTask(taskId, userId);
		redirectAttributes.addFlashAttribute("success", "Everything went just fine.");
		return "redirect:/alltasks";
	}

	@GetMapping("/sendReminder")
	public void sendReminder() throws EmailException {
		LocalDateTime date = LocalDateTime.now().plusDays(1);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
		date.format(dateTimeFormatter);

		List<String> userList = taskRepository.findTaskByDate(date);
		String passwordMail = mail.getPasswordMail();
		Task.sendEmail(userList, passwordMail);
	}

	@GetMapping("/late/tasks")
	public String lateTasks(ModelMap model) {
		int id = loggedUser.getUserId();
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
		date.format(dateTimeFormatter);

		Iterable<Task> lateTasks = taskRepository.findLateTasks(id, date);
		model.addAttribute("lateTasks", lateTasks);
		model.addAttribute("name", loggedUser.getName());
		return "/lateTask";
	}

	@GetMapping("/archive")
	public String tasksInArchive(ModelMap model) {
		boolean session = loggedUser.isLogged();
		int id = loggedUser.getUserId();
		Iterable<Task> allTasks = taskRepository.findTaskByUser(id);
		List<String> allStatus = Status.getAllStatus();
		if (session) {
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("tasks", allTasks);
			model.addAttribute("status", allStatus);
			return "/archive";
		}
		return "redirect:/login";
	}

	@GetMapping("archive/{id}")
	public String changeStatus(@PathVariable int id, ModelMap model) {
		List<Task> tasks = taskRepository.findTaskById(id);
		List<String> allStatus = Status.getAllStatus();
		model.addAttribute("name", loggedUser.getName());
		model.addAttribute("status", allStatus);
		model.addAttribute("tasks", tasks);
		return "/changestatus";
	}
	
	@GetMapping("/tasksbycategory")
	public String showTasksByCategory(ModelMap model) {
		boolean session = loggedUser.isLogged();
		int id = loggedUser.getUserId();
		Iterable<Task> allTasks = taskRepository.findTaskByUser(id);
		List<Category> categories = categoryRepository.findCategoryByUser(id);
		if(session) {
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("tasks", allTasks);
			model.addAttribute("categories", categories);
			return "/tasksbycategory";
		}
		return "redirect:/login";
	}
	
	@GetMapping("/taskhistory")
	public String taskHistory(ModelMap model) {
		boolean haveTasks = true;
		model.addAttribute("name", loggedUser.getName());
		model.addAttribute("haveTasks", haveTasks);
		return "/taskhistory";
	}
	
	@PostMapping("/taskhistory")
	public String getTaskHistory(LocalDate initialDate, LocalDate finalDate, ModelMap model) {
		int id = loggedUser.getUserId();
		boolean haveTasks = false;
		
		if(finalDate != null && initialDate != null) {
			Iterable<Task> completedTasks =	taskRepository.findCompletedTasks(id, initialDate, finalDate);
			if(completedTasks.iterator().hasNext()) {
				haveTasks = true;
				model.addAttribute("completedTasks", completedTasks);
				model.addAttribute("haveTasks", haveTasks);
			}
		}else {
			model.addAttribute("haveTasks", haveTasks);
		}
		model.addAttribute("name", loggedUser.getName());
		return"/taskhistory";
	}
}
