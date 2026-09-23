package dailyplanr.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dailyplanr.controllers.LoggedUser;
import dailyplanr.models.Category;
import dailyplanr.models.CategoryRepository;
import dailyplanr.models.Task;
import dailyplanr.models.TaskRepository;
import dailyplanr.models.User;

@Service
public class TaskService {
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private TaskRepository taskRepository;
	
	@Inject
	private LoggedUser loggedUser;
	
	private IvParameterSpec iv;
	
	private SecretKey symmetricKey;
	
	private byte[] cipherText;
	
	
	public void createTask(String title,LocalDateTime data,String description,String categories,String priority) throws Exception {
		User user = new User();
	    user.setId(loggedUser.getUserId());

	    int categoryId = categoryRepository.findCategory(categories);
	    Category category = categoryRepository.findById(categoryId)
	        .orElseThrow(() -> new IllegalArgumentException("Category not found"));

	    Task task = new Task();
	    task.setTitle(title);
	    task.setData(data);
	    task.setDescription(description);
	    task.setCategories(category);
	    task.setPriority(priority);
	    task.setEncryptId("XXXX");

	    iv = Security.iv();
	    symmetricKey = Security.secretKey();

	    task.setIv(Base64.getEncoder().encodeToString(iv.getIV()));
	    task.setSymmetricKey(symmetricKey.getEncoded());
	    task.addUser(user);

	    taskRepository.save(task);
	    
	    createKey();
	}
	
	public String findLateTasks() {
		String alert = "null";
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
						return alert = "You have late tasks!";
					}
				}
			}
			return alert;
	}
	
	public void createKey() throws Exception {
		int id = loggedUser.getUserId();
		Iterable<Task> allTasks = taskRepository.findTaskByUser(id);
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
	}

}
