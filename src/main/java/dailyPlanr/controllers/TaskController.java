package dailyPlanr.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import dailyPlanr.models.Task;
import dailyPlanr.models.TaskRepository;
import dailyPlanr.models.User;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class TaskController {
	@Autowired
	private TaskRepository taskRepository;
	
	@GetMapping("/newtask")
	public String tasks() {
		return "/newtask";
	}
	
	@PostMapping("/newtask")
	public String newTask(@Valid Task task,User user) {
		task.addUser(user);
		taskRepository.save(task);
		return "/newtask";
	}
}
