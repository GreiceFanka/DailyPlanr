package dailyPlanr.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
			if (task.getData() != null) {
				task.addUser(user);
				taskRepository.save(task);
				redirAttrs.addFlashAttribute("success", "Everything went just fine.");
				return "redirect:/newtask";
			} else {
				redirAttrs.addFlashAttribute("error", "You need insert a date.");
				return "redirect:/newtask";
			}
		} else {
			redirAttrs.addFlashAttribute("error", "Something went wrong, try again.");
			return "redirect:/newtask";
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
				if (task.getData() != null) {
					int latedTasks = task.getData().compareTo(now);
					boolean toDoStatus = task.getTaskStatus().equalsIgnoreCase("To do");
					boolean inProgressStatus = task.getTaskStatus().equalsIgnoreCase("In progress");

					if (latedTasks <= -1 && (toDoStatus || inProgressStatus)) {
						alert = "You have late tasks!";
					}
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

	@PostMapping("/delete/task")
	public String deleteTask(@RequestParam int id) {
		taskRepository.deleteById(id);
		return "redirect:/alltasks";
	}

	@GetMapping("/edit/task/{id}")
	public String editTask(@PathVariable int id, ModelMap model, Status status) {
		boolean session = loggedUser.isLogged();
		if (session) {
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

		} else {
			return "redirect:/login";
		}
	}

	@PostMapping("/update/task")
	public String updateTask(@RequestParam String data, @RequestParam String title, @RequestParam String description,
			@RequestParam String priority, @RequestParam int task_id, RedirectAttributes redirectAttributes) {
		if (data != null && !data.isEmpty()) {
			taskRepository.updateTask(data, title, description, priority, task_id);
		} else {
			redirectAttributes.addAttribute("id", task_id);
			redirectAttributes.addFlashAttribute("error", "Date is required!");
			return "redirect:/edit/task/{id}";
		}
		return "redirect:/alltasks";
	}

	@PostMapping("edit/status")
	public String editTaskStatus(@RequestParam String taskStatus, @RequestParam int id) {
		boolean session = loggedUser.isLogged();
		if (session) {
			LocalDate updatedStatus = LocalDate.now();
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			updatedStatus.format(dateTimeFormatter);

			taskRepository.editStatus(taskStatus, updatedStatus, id);
			return "redirect:/alltasks";
		}
		return "redirect:/login";
	}

	@PostMapping("edit/category")
	public String editTaskCategory(@RequestParam int cat_id, @RequestParam int id) {
		taskRepository.editTaskCategory(cat_id, id);
		return "redirect:/alltasks";
	}

	@PostMapping("/add/user")
	public String addUser(@RequestParam int taskId, @RequestParam int userId, RedirectAttributes redirectAttributes) {
		List<Task> tasks = taskRepository.findTaskById(taskId);
		boolean user = false;

		for (User users : tasks.get(0).getUsers()) {
			if (users.getId() == userId) {
				user = true;
			}
		}

		if (user == false) {

			taskRepository.insertUserTask(taskId, userId);

			redirectAttributes.addFlashAttribute("success", "Everything went just fine.");

		} else {
			redirectAttributes.addFlashAttribute("error", "This user is already signed to this task!");
		}

		return "redirect:/alltasks";
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
	public String tasksInArchive(ModelMap model, @RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "8") int size) {
		boolean session = loggedUser.isLogged();
		int id = loggedUser.getUserId();
		int currentPage = page;
		int pageSize = size;
		Page<Task> allTasks = taskRepository.findTaskByUserAndStatus(id, (PageRequest.of(currentPage - 1, pageSize)));

		int totalPages = allTasks.getTotalPages();

		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}

		List<String> allStatus = Status.getAllStatus();

		if (session) {
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("tasks", allTasks);
			model.addAttribute("status", allStatus);
			model.addAttribute("pageSize", pageSize);
			model.addAttribute("currentPage", currentPage);
			model.addAttribute("totalPages", allTasks.getTotalPages());
			model.addAttribute("totalItems", allTasks.getTotalElements());
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
	public String showTasksByCategory(@Valid Integer category, ModelMap model) {
		boolean session = loggedUser.isLogged();
		int id = loggedUser.getUserId();
		if (session) {
			if (category != null) {
				List<Task> tasks = taskRepository.findTaskByUserAndCategory(id, category);
				List<Category> categories = categoryRepository.findCategoryByUser(id);
				model.addAttribute("name", loggedUser.getName());
				model.addAttribute("tasks", tasks);
				model.addAttribute("categories", categories);
			}
			List<Category> categories = categoryRepository.findCategoryByUser(id);
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("categories", categories);
			return "/tasksbycategory";
		}
		return "redirect:/login";
	}

	@GetMapping("/taskhistory")
	public String getTaskHistory(@Valid LocalDate initialDate, @Valid LocalDate finalDate, ModelMap model) {
		int id = loggedUser.getUserId();
		boolean tasksEmpty = false;

		if (finalDate != null && initialDate != null) {
			Iterable<Task> completedTasks = taskRepository.findCompletedTasks(id, initialDate, finalDate);
			if (completedTasks.iterator().hasNext()) {
				model.addAttribute("completedTasks", completedTasks);
			} else {
				tasksEmpty = true;
				model.addAttribute("tasksEmpty", tasksEmpty);
			}
		}
		model.addAttribute("tasksEmpty", tasksEmpty);
		model.addAttribute("name", loggedUser.getName());
		return "/taskhistory";
	}

	@GetMapping("delete/person/{id}")
	public String deletePerson(@PathVariable int id, ModelMap model) {
		List<Task> tasks = taskRepository.findTaskById(id);
		model.addAttribute("tasks", tasks);
		model.addAttribute("name", loggedUser.getName());
		return "/deleteperson";
	}

	@PostMapping("/delete/person")
	public String removePerson(@RequestParam int taskId, @RequestParam int userId,
			RedirectAttributes redirectAttributes) {
		try {
			taskRepository.deleteUserTask(taskId, userId);
			redirectAttributes.addFlashAttribute("success", "Person deleted from task with success!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Something went wrog, please try later.");
		}
		return "redirect:/alltasks";
	}
}
