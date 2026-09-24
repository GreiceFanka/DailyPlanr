package dailyplanr.controllers;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dailyplanr.models.Category;
import dailyplanr.models.CategoryRepository;
import dailyplanr.models.Priority;
import dailyplanr.models.Status;
import dailyplanr.models.Task;
import dailyplanr.models.TaskRepository;
import dailyplanr.service.TaskService;
import dailyplanr.service.UserService;
import jakarta.validation.Valid;

@Controller
public class TaskController {
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private TaskService taskService;

	@Autowired
	private UserService userService;
	
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
			return "newtask";
		}
		return "redirect:/login";
	}

	@PostMapping("/newtask/create/")
	public String newTask(@RequestParam String title, @Valid LocalDateTime data,@RequestParam String description,@RequestParam String categories,@RequestParam String priority, RedirectAttributes redirAttrs){
		if (!loggedUser.isLogged()) {
	        redirAttrs.addFlashAttribute("error", "Something went wrong, try again.");
	        return "redirect:/newtask";
	    }

	    if (data == null) {
	        redirAttrs.addFlashAttribute("error", "You need to insert a date.");
	        return "redirect:/newtask";
	    }

	    try {
	    	taskService.createTask(title, data, description, categories, priority);
	        redirAttrs.addFlashAttribute("success", "Everything went just fine.");
	        
	    } catch (Exception e) {
	        redirAttrs.addFlashAttribute("error", "Failed to create task. Try again later." );
	    }

	    return "redirect:/newtask";
	}

	@GetMapping("/alltasks")
	public String getAllTasks(ModelMap model){
		String alert = "null";
		boolean session = loggedUser.isLogged();
		
		if (session) {
			Iterable<Task> allTasks = taskRepository.findTaskByUser(loggedUser.getUserId());
			alert = taskService.findLateTasks();
			
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("user", loggedUser.getUserId());
			model.addAttribute("tasks", allTasks);
			model.addAttribute("alert", alert);
			return "alltasks";
		}	
		return "redirect:/login";
	}

	@PostMapping("/delete/task")
	public String deleteTask(@RequestParam String encryptId,RedirectAttributes redirectAttributes){
		boolean session = loggedUser.isLogged();
		if (session) {
			try {
				int idDecrypt = taskService.decryptId(encryptId);
				taskRepository.deleteById(idDecrypt);
				return "redirect:/alltasks";
			} catch (Exception e) {
				redirectAttributes.addFlashAttribute("error", "Failed to delete task.Please try again later.");
				return "redirect:/alltasks";
			}
		}
		return "redirect:/login";
	}

	@GetMapping("/edit/task/{encryptId}")
	public String editTask(@PathVariable String encryptId, ModelMap model, Status status,RedirectAttributes redirectAttributes){
		boolean session = loggedUser.isLogged();
		
		if (session) {
			try {
				int idDecrypt = taskService.decryptId(encryptId);
				int user_id = loggedUser.getUserId();
				List<Integer> taskUser = taskRepository.findTaskUser(idDecrypt);
				for (Integer id : taskUser) {
					if(user_id == id) {
						List<Task> tasks = taskRepository.findTaskById(idDecrypt);
						List<Category> listCategories = categoryRepository.findCategoryByUser(user_id);
						List<String> allStatus = Status.getAllStatus();
						List<String> allPriorities = Priority.getAllPriorities();
						
						model.addAttribute("name", loggedUser.getName());
						model.addAttribute("user", loggedUser.getUserId());
						model.addAttribute("tasks", tasks);
						model.addAttribute("categories", listCategories);
						model.addAttribute("status", allStatus);
						model.addAttribute("priorities", allPriorities);
						return "updatetask";
						
					}else {
						return"redirect:/alltasks";
					}
				} 
			}catch (Exception e) {
				redirectAttributes.addFlashAttribute("error", "Failed to load the page.Please try again later.");
				return "redirect:/alltasks";
			}
		
		}
		return "redirect:/login";
	}

	@PostMapping("/update/task")
	public String updateTask(@RequestParam String data, @RequestParam String title, @RequestParam String description,
			@RequestParam String priority, @RequestParam String task_id, RedirectAttributes redirectAttributes){
		boolean session = loggedUser.isLogged();
		if (session) {
			if (data != null && !data.isEmpty()) {
				try {
					int idDecrypt = taskService.decryptId(task_id);
					taskRepository.updateTask(data, title, description, priority, idDecrypt);
				} catch (Exception e) {
					redirectAttributes.addFlashAttribute("error", "Failed to edit task.Please try again later.");
					return "redirect:/edit/task/{id}";
				}
				
			} else {
				redirectAttributes.addAttribute("id", task_id);
				redirectAttributes.addFlashAttribute("error", "Date is required!");
				return "redirect:/edit/task/{id}";
			}
			return "redirect:/alltasks";
		} else {
			return "redirect:/login";
		}
	}

	@PostMapping("edit/status")
	public String editTaskStatus(@RequestParam String taskStatus, @RequestParam String task_id,RedirectAttributes redirectAttributes){
		boolean session = loggedUser.isLogged();
		if (session) {
			try {
				int idDecrypt = taskService.decryptId(task_id);
				LocalDate updatedStatus = LocalDate.now();
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				updatedStatus.format(dateTimeFormatter);

				taskRepository.editStatus(taskStatus, updatedStatus, idDecrypt);
				return "redirect:/alltasks";
			} catch (Exception e) {
				redirectAttributes.addFlashAttribute("error", "Failed to edit status.Please try again later.");
				return "redirect:/alltasks";
			}
		}
		return "redirect:/login";
	}

	@PostMapping("edit/category")
	public String editTaskCategory(@RequestParam String cat_id, @RequestParam String task_id,RedirectAttributes redirectAttributes){
		boolean session = loggedUser.isLogged();
		if (session) {
			try {
				int cId = categoryRepository.findCategory(cat_id);
				int idDecrypt = taskService.decryptId(task_id);
				taskRepository.editTaskCategory(cId, idDecrypt);
				return "redirect:/alltasks";
				
			} catch (Exception e) {
				redirectAttributes.addFlashAttribute("error", "Failed to edit category.Please try again later.");
				return "redirect:/alltasks";
			}
		}
		return "redirect:/login";
	}

	@PostMapping("/add/user")
	public String addUser(@RequestParam String taskId, @RequestParam String hashu, RedirectAttributes redirectAttributes){
		boolean session = loggedUser.isLogged();	
		if (session) {
			try {
				int idDecrypt = taskService.decryptId(taskId);
				int decryptUserId = userService.decryptHash(hashu);	
				boolean user = taskService.userInTask(idDecrypt, decryptUserId);
				if (!user) {
					taskRepository.insertUserTask(idDecrypt, decryptUserId);
					redirectAttributes.addFlashAttribute("success", "Everything went just fine.");
				} else {
					redirectAttributes.addFlashAttribute("error", "This user is already signed to this task!");
				}
				return "redirect:/alltasks";
			} catch (Exception e) {
				redirectAttributes.addFlashAttribute("error", "Failed to add the user.Please try again later.");
				return "redirect:/alltasks";
			}
		
		}
	
		return "redirect:/login";
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
		return "lateTask";
	}

	@GetMapping("/archive")
	public String tasksInArchive(ModelMap model, @RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "8") int size) {
		boolean session = loggedUser.isLogged();
		if (session) {
			int id = loggedUser.getUserId();
			int currentPage = page;
			int pageSize = size;
			Page<Task> allTasks = taskRepository.findTaskByUserAndStatus(id,
					(PageRequest.of(currentPage - 1, pageSize)));

			int totalPages = allTasks.getTotalPages();

			if (totalPages > 0) {
				List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
				model.addAttribute("pageNumbers", pageNumbers);
			}

			List<String> allStatus = Status.getAllStatus();

			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("tasks", allTasks);
			model.addAttribute("status", allStatus);
			model.addAttribute("pageSize", pageSize);
			model.addAttribute("currentPage", currentPage);
			model.addAttribute("totalPages", allTasks.getTotalPages());
			model.addAttribute("totalItems", allTasks.getTotalElements());
			return "archive";
		}
		return "redirect:/login";
	}

	@GetMapping("archive/{id}")
	public String changeStatus(@PathVariable String id, ModelMap model,RedirectAttributes redirectAttributes){
		boolean session = loggedUser.isLogged();
		if (session) {
			try {
				int idDecrypt = taskService.decryptId(id);
				List<Task> tasks = taskRepository.findTaskById(idDecrypt);
				List<String> allStatus = Status.getAllStatus();
				model.addAttribute("name", loggedUser.getName());
				model.addAttribute("status", allStatus);
				model.addAttribute("tasks", tasks);
				return "changestatus";
			} catch (Exception e) {
				redirectAttributes.addFlashAttribute("error", "Failed to load the page.Please try again later.");
				return "redirect:/alltasks";
			}
			
		}
		return "redirect:/login";
	}

	@GetMapping("/tasksbycategory")
	public String showTasksByCategory(ModelMap model) {
		boolean session = loggedUser.isLogged();
		if (session) {
			int id = loggedUser.getUserId();
			List<Category> categories = categoryRepository.findCategoryByUser(id);
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("categories", categories);
			return "tasksbycategory";
		}
		return "redirect:/login";
	}
	
	@GetMapping("/tasksCategory")
	public String showTasksCategory(@RequestParam String category, ModelMap model) {
		boolean session = loggedUser.isLogged();
		if (session) {
			int id = loggedUser.getUserId();
			try {
				int catId = categoryRepository.findCategory(category);
				if (catId > 0) {
					List<Task> tasks = taskRepository.findTaskByUserAndCategory(id, catId);
					List<Category> categories = categoryRepository.findCategoryByUser(id);
					model.addAttribute("name", loggedUser.getName());
					model.addAttribute("tasks", tasks);
					model.addAttribute("categories", categories);
					return "tasksbycategory";
				}
				
			} catch (Exception e) {
				return "redirect:/login";
			}
		}
		return "redirect:/login";
	}

	@GetMapping("/taskhistory")
	public String getTaskHistory(@Valid LocalDate initialDate, @Valid LocalDate finalDate, ModelMap model) {
		boolean tasksEmpty = false;
		boolean session = loggedUser.isLogged();
		
		if (session) {
			if (finalDate != null && initialDate != null) {
				int id = loggedUser.getUserId();
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
			return "taskhistory";
		}
		return "redirect:/login";
	}

	@GetMapping("delete/person/{id}")
	public String deletePerson(@PathVariable String id, ModelMap model,RedirectAttributes redirectAttributes){
		boolean session = loggedUser.isLogged();
		if (session) {
			try {
				int idDecrypt = taskService.decryptId(id);
				List<Task> tasks = taskRepository.findTaskById(idDecrypt);
				model.addAttribute("tasks", tasks);
				model.addAttribute("name", loggedUser.getName());
				return "deleteperson";
			} catch (Exception e) {
				redirectAttributes.addFlashAttribute("error", "Something went wrog, please try later.");
				return "redirect:/alltasks";
			}
		}
		return "redirect:/login";
	}

	@PostMapping("/delete/person")
	public String removePerson(@RequestParam String taskId, @RequestParam String hashu,
			RedirectAttributes redirectAttributes) {
		boolean session = loggedUser.isLogged();
		if (session) {
			try {
				int idDecrypt = taskService.decryptId(taskId);
				int decryptUserId = userService.decryptHash(hashu);				
				taskRepository.deleteUserTask(idDecrypt, decryptUserId);
				redirectAttributes.addFlashAttribute("success", "Person deleted from task with success!");
			} catch (Exception e) {
				redirectAttributes.addFlashAttribute("error", "Something went wrog, please try later.");
			}
			return "redirect:/alltasks";
		}
		return "redirect:/login";
	}
	
	@GetMapping("/img/{tempId}")
	@ResponseBody
	public byte[] getUserImage(@PathVariable String tempId){
		byte[] photo = userService.userImg(tempId);
		return photo;
	}

}
