package dailyPlanr.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@Entity
public class Task {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String description;

	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime data;

	@Column
	private String taskStatus = Status.TODO.getDesc();

	@ManyToMany
	@JoinTable(name = "task_user", joinColumns = { @JoinColumn(name = "task_id") }, inverseJoinColumns = {
			@JoinColumn(name = "user_id") })
	List<User> users = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private Category categories;

	public void addUser(User user) {
		this.users.add(user);
		user.getTasks().add(this);
		categories.getTasks().add(this);
	}

	public Task() {

	}

	public Task(String title, String description, String taskStatus) {
		this.title = title;
		this.description = description;
		this.taskStatus = taskStatus;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getData() {
		return data;
	}

	public void setData(LocalDateTime data) {
		this.data = data;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public Category getCategories() {
		return categories;
	}

	public void setCategories(Category categories) {
		this.categories = categories;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public static void sendEmail(List<String> userList, String passwordMail) throws EmailException {
		try {
			for (String user : userList) {
				SimpleEmail email = new SimpleEmail();

				email.setHostName("smtp.zoho.eu");
				email.setSmtpPort(465);
				email.setFrom("dailyplanr@zohomail.eu", "DailyPlanr");

				email.addTo(user);
				email.setSubject("DailyPlanr task reminder");
				email.setMsg("Dear user,\n" + "\n"
						+ "This is just a reminder mail that you have incompleted tasks that are late or will be late soon. Please login in DailyPlanr app and check your tasks list.\n"
						+ "\n" + "Greetings from DailyPlanr team!");

				email.setSSL(true);
				email.setAuthentication("dailyplanr@zohomail.eu", passwordMail);
				System.out.println("Sending...");
				email.send();
				System.out.println("Email sent!");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
