package dailyplanr.controllers;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
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
import dailyplanr.models.User;
import dailyplanr.models.UserRepository;
import jakarta.validation.Valid;

@Controller
public class TaskController {
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;

	@Inject
	private LoggedUser loggedUser;
	
	private IvParameterSpec iv;
	
	private SecretKey symmetricKey;
	
	private byte[] cipherText;

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
	public String newTask(@Valid Task task, @Valid User user, RedirectAttributes redirAttrs) throws Exception {
		if (user != null) {
			if (task.getData() != null) {
				task.setEncryptId("XXXX");
				iv = Security.iv();
				symmetricKey = Security.secretKey();
				byte[] taskIv = iv.getIV();
				byte[] taskKey = symmetricKey.getEncoded();
				String base64Iv = Base64.getEncoder().encodeToString(taskIv);
				task.setIv(base64Iv);
				task.setSymmetricKey(taskKey);
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
	public String getAllTasks(ModelMap model) throws Exception {
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
			
			
			for (Task task : allTasks) {
				if(!task.getTaskStatus().equalsIgnoreCase("Archive")) {
					String taskId = Integer.toString(task.getId());
					int tid = task.getId();
					iv = Security.iv();
					symmetricKey = Security.secretKey();
					cipherText = Security.encrypt(taskId, symmetricKey, iv);
					String taskEncryptId = Base64.getUrlEncoder().withoutPadding().encodeToString(cipherText);
					task.setEncryptId(taskEncryptId);
					byte[] taskIv = iv.getIV();
					byte[] taskKey = symmetricKey.getEncoded();
					String base64Iv = Base64.getEncoder().encodeToString(taskIv);
					taskRepository.encryptKeyCreation(taskEncryptId, base64Iv, taskKey, tid);
				}
				
			}

			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("user", loggedUser.getUserId());
			model.addAttribute("tasks", allTasks);
			model.addAttribute("alert", alert);
			return "alltasks";
		}
		return "redirect:/login";
	}

	@PostMapping("/delete/task")
	public String deleteTask(@RequestParam String encryptId) throws Exception {
		boolean session = loggedUser.isLogged();
		if (session) {
			Optional<Task> taskInf = taskRepository.findTaskInf(encryptId);
			Task task = taskInf.get();
			
			byte [] key = task.getSymmetricKey();
			SecretKey originalKey = new SecretKeySpec(key, "AES");
			
			byte[] decIv = Base64.getDecoder().decode(task.getIv());
			IvParameterSpec ivSpec = new IvParameterSpec(decIv);
			
			cipherText = Base64.getUrlDecoder().decode(encryptId);
			
			String decryptedText = Security.decrypt(cipherText, originalKey, ivSpec);
			
			int idDecrypt = Integer.parseInt(decryptedText);
			taskRepository.deleteById(idDecrypt);
			return "redirect:/alltasks";
		}
		return "redirect:/login";
	}

	@GetMapping("/edit/task/{encryptId}")
	public String editTask(@PathVariable String encryptId, ModelMap model, Status status) throws Exception {
		boolean session = loggedUser.isLogged();
		Optional<Task> taskInf = taskRepository.findTaskInf(encryptId);
		Task task = taskInf.get();
		
		byte [] key = task.getSymmetricKey();
		SecretKey originalKey = new SecretKeySpec(key, "AES");
		
		byte[] decIv = Base64.getDecoder().decode(task.getIv());
		IvParameterSpec ivSpec = new IvParameterSpec(decIv);
		
		cipherText = Base64.getUrlDecoder().decode(encryptId);
		
		String decryptedText = Security.decrypt(cipherText, originalKey, ivSpec);
		
		int idDecrypt = Integer.parseInt(decryptedText);
		
		if (session) {
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
		
		}
		return "redirect:/login";
	}

	@PostMapping("/update/task")
	public String updateTask(@RequestParam String data, @RequestParam String title, @RequestParam String description,
			@RequestParam String priority, @RequestParam String task_id, RedirectAttributes redirectAttributes) throws Exception {
		boolean session = loggedUser.isLogged();
		if (session) {
			if (data != null && !data.isEmpty()) {
				Optional<Task> taskInf = taskRepository.findTaskInf(task_id);
				Task task = taskInf.get();
				
				byte [] key = task.getSymmetricKey();
				SecretKey originalKey = new SecretKeySpec(key, "AES");
				
				byte[] decIv = Base64.getDecoder().decode(task.getIv());
				IvParameterSpec ivSpec = new IvParameterSpec(decIv);
				
				cipherText = Base64.getUrlDecoder().decode(task_id);
				
				String decryptedText = Security.decrypt(cipherText, originalKey, ivSpec);
				
				int idDecrypt = Integer.parseInt(decryptedText);
				
				taskRepository.updateTask(data, title, description, priority, idDecrypt);
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
	public String editTaskStatus(@RequestParam String taskStatus, @RequestParam String task_id) throws Exception {
		boolean session = loggedUser.isLogged();
		if (session) {
			Optional<Task> taskInf = taskRepository.findTaskInf(task_id);
			Task task = taskInf.get();
			
			byte [] key = task.getSymmetricKey();
			SecretKey originalKey = new SecretKeySpec(key, "AES");
			
			byte[] decIv = Base64.getDecoder().decode(task.getIv());
			IvParameterSpec ivSpec = new IvParameterSpec(decIv);
			
			cipherText = Base64.getUrlDecoder().decode(task_id);
			
			String decryptedText = Security.decrypt(cipherText, originalKey, ivSpec);
			
			int idDecrypt = Integer.parseInt(decryptedText);
			
			LocalDate updatedStatus = LocalDate.now();
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			updatedStatus.format(dateTimeFormatter);

			taskRepository.editStatus(taskStatus, updatedStatus, idDecrypt);
			return "redirect:/alltasks";
		}
		return "redirect:/login";
	}

	@PostMapping("edit/category")
	public String editTaskCategory(@RequestParam int cat_id, @RequestParam String task_id) throws Exception {
		boolean session = loggedUser.isLogged();
		if (session) {
			Optional<Task> taskInf = taskRepository.findTaskInf(task_id);
			Task task = taskInf.get();
			
			byte [] key = task.getSymmetricKey();
			SecretKey originalKey = new SecretKeySpec(key, "AES");
			
			byte[] decIv = Base64.getDecoder().decode(task.getIv());
			IvParameterSpec ivSpec = new IvParameterSpec(decIv);
			
			cipherText = Base64.getUrlDecoder().decode(task_id);
			
			String decryptedText = Security.decrypt(cipherText, originalKey, ivSpec);
			
			int idDecrypt = Integer.parseInt(decryptedText);
			
			taskRepository.editTaskCategory(cat_id, idDecrypt);
			return "redirect:/alltasks";
		}
		return "redirect:/login";
	}

	@PostMapping("/add/user")
	public String addUser(@RequestParam String taskId, @RequestParam String hashu, RedirectAttributes redirectAttributes)throws Exception {
				
		Optional<Task> taskInf = taskRepository.findTaskInf(taskId);
		Task task = taskInf.get();
		
		byte [] key = task.getSymmetricKey();
		SecretKey originalKey = new SecretKeySpec(key, "AES");
		
		byte[] decIv = Base64.getDecoder().decode(task.getIv());
		IvParameterSpec ivSpec = new IvParameterSpec(decIv);
		
		cipherText = Base64.getUrlDecoder().decode(taskId);
		
		String decryptedText = Security.decrypt(cipherText, originalKey, ivSpec);
		
		int idDecrypt = Integer.parseInt(decryptedText);
		
		List<Task> tasks = taskRepository.findTaskById(idDecrypt);
		
		Optional<User> userInf = userRepository.findUsrInf(hashu);
		
		byte [] ukey = userInf.get().getSymmetricKey();
		SecretKey oKey = new SecretKeySpec(ukey, "AES");
		
		byte[] uIv = Base64.getDecoder().decode(userInf.get().getIv());
		IvParameterSpec uIvSpec = new IvParameterSpec(uIv);
		
		byte[] uCipherText = Base64.getUrlDecoder().decode(hashu);
		String decryptHashu = Security.decrypt(uCipherText, oKey, uIvSpec);
		int decryptUserId = Integer.parseInt(decryptHashu);		
		
		boolean user = false;
		boolean session = loggedUser.isLogged();
		if (session) {
			for (User users : tasks.get(0).getUsers()) {
				if (users.getId() == decryptUserId) {
					user = true;
				}
			}

			if (user == false) {

				taskRepository.insertUserTask(idDecrypt, decryptUserId);

				redirectAttributes.addFlashAttribute("success", "Everything went just fine.");

			} else {
				redirectAttributes.addFlashAttribute("error", "This user is already signed to this task!");
			}

			return "redirect:/alltasks";
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
	public String changeStatus(@PathVariable String id, ModelMap model) throws Exception {
		boolean session = loggedUser.isLogged();
		if (session) {
			Optional<Task> taskInf = taskRepository.findTaskInf(id);
			Task task = taskInf.get();
			
			byte [] key = task.getSymmetricKey();
			SecretKey originalKey = new SecretKeySpec(key, "AES");
			
			byte[] decIv = Base64.getDecoder().decode(task.getIv());
			IvParameterSpec ivSpec = new IvParameterSpec(decIv);
			
			cipherText = Base64.getUrlDecoder().decode(id);
			
			String decryptedText = Security.decrypt(cipherText, originalKey, ivSpec);
			
			int idDecrypt = Integer.parseInt(decryptedText);
			List<Task> tasks = taskRepository.findTaskById(idDecrypt);
			List<String> allStatus = Status.getAllStatus();
			model.addAttribute("name", loggedUser.getName());
			model.addAttribute("status", allStatus);
			model.addAttribute("tasks", tasks);
			return "changestatus";
		}
		return "redirect:/login";
	}

	@GetMapping("/tasksbycategory")
	public String showTasksByCategory(@Valid Integer category, ModelMap model) {
		boolean session = loggedUser.isLogged();
		if (session) {
			int id = loggedUser.getUserId();
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
			return "tasksbycategory";
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
	public String deletePerson(@PathVariable String id, ModelMap model) throws Exception {
		boolean session = loggedUser.isLogged();
		if (session) {
			Optional<Task> taskInf = taskRepository.findTaskInf(id);
			Task task = taskInf.get();
			
			byte [] key = task.getSymmetricKey();
			SecretKey originalKey = new SecretKeySpec(key, "AES");
			
			byte[] decIv = Base64.getDecoder().decode(task.getIv());
			IvParameterSpec ivSpec = new IvParameterSpec(decIv);
			
			cipherText = Base64.getUrlDecoder().decode(id);
			
			String decryptedText = Security.decrypt(cipherText, originalKey, ivSpec);
			
			int idDecrypt = Integer.parseInt(decryptedText);
			List<Task> tasks = taskRepository.findTaskById(idDecrypt);
			model.addAttribute("tasks", tasks);
			model.addAttribute("name", loggedUser.getName());
			return "deleteperson";
		}
		return "redirect:/login";
	}

	@PostMapping("/delete/person")
	public String removePerson(@RequestParam String taskId, @RequestParam String hashu,
			RedirectAttributes redirectAttributes) {
		boolean session = loggedUser.isLogged();
		if (session) {
			try {
				Optional<Task> taskInf = taskRepository.findTaskInf(taskId);
				Task task = taskInf.get();
				
				byte [] key = task.getSymmetricKey();
				SecretKey originalKey = new SecretKeySpec(key, "AES");
				
				byte[] decIv = Base64.getDecoder().decode(task.getIv());
				IvParameterSpec ivSpec = new IvParameterSpec(decIv);
				
				cipherText = Base64.getUrlDecoder().decode(taskId);
				
				String decryptedText = Security.decrypt(cipherText, originalKey, ivSpec);
				
				int idDecrypt = Integer.parseInt(decryptedText);
				
				Optional<User> userInf = userRepository.findUsrInf(hashu);
				
				byte [] ukey = userInf.get().getSymmetricKey();
				SecretKey oKey = new SecretKeySpec(ukey, "AES");
				
				byte[] uIv = Base64.getDecoder().decode(userInf.get().getIv());
				IvParameterSpec uIvSpec = new IvParameterSpec(uIv);
				
				byte[] uCipherText = Base64.getUrlDecoder().decode(hashu);
				String decryptHashu = Security.decrypt(uCipherText, oKey, uIvSpec);
				int decryptUserId = Integer.parseInt(decryptHashu);	
				
				taskRepository.deleteUserTask(idDecrypt, decryptUserId);
				
				redirectAttributes.addFlashAttribute("success", "Person deleted from task with success!");
			} catch (Exception e) {
				redirectAttributes.addFlashAttribute("error", "Something went wrog, please try later.");
			}
			return "redirect:/alltasks";
		}
		return "redirect:/login";
	}
	
	@GetMapping("/img/{hashu}")
	@ResponseBody
	public byte[] getUserImage(@PathVariable String hashu) throws Exception {
		Optional<User> userInf = userRepository.findUsrInf(hashu);
		
		byte [] ukey = userInf.get().getSymmetricKey();
		SecretKey oKey = new SecretKeySpec(ukey, "AES");
		
		byte[] uIv = Base64.getDecoder().decode(userInf.get().getIv());
		IvParameterSpec uIvSpec = new IvParameterSpec(uIv);
		
		byte[] uCipherText = Base64.getUrlDecoder().decode(hashu);
		String decryptHashu = Security.decrypt(uCipherText, oKey, uIvSpec);
		int decryptUserId = Integer.parseInt(decryptHashu);	
		
		byte[] photo = null;
		Optional<User> users = userRepository.findById(decryptUserId);
		byte[]images = users.get().getImage();
			
			if (images != null) {
				return images;
			} else {
				try (InputStream is = getClass().getResourceAsStream("/static/images/user.jpg")) {
				    if (is != null) {
				        BufferedImage rd = ImageIO.read(is);
				        ByteArrayOutputStream wr = new ByteArrayOutputStream();
				        ImageIO.write(rd, "jpg", wr);
				        photo = wr.toByteArray();
				    } else {
				        System.out.println("Imagem default não encontrada!");
				    }
				} catch (Exception e) {
				    e.printStackTrace();
				}
				return photo;
			}
	}

}
