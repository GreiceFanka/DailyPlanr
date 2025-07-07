package dailyplanr.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
	
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private LocalDate updatedStatus = LocalDate.now();
	
	@Column(nullable = false)
	private String priority;
	
	@Column(nullable=false)
	private String encryptId;
	
	@Column(nullable=false)
	private String iv;
	
	@Column(nullable=false)
	private byte[] symmetricKey;

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

	public Task(String title, String description, String taskStatus, String priority) {
		this.title = title;
		this.description = description;
		this.taskStatus = taskStatus;
		this.priority = priority;
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

	public LocalDate getUpdatedStatus() {
		return updatedStatus;
	}

	public void setUpdatedStatus(LocalDate updatedStatus) {
		this.updatedStatus = updatedStatus;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getEncryptId() {
		return encryptId;
	}

	public void setEncryptId(String encryptId) {
		this.encryptId = encryptId;
	}

	public String getIv() {
		return iv;
	}

	public void setIv(String iv) {
		this.iv = iv;
	}

	public byte[] getSymmetricKey() {
		return symmetricKey;
	}

	public void setSymmetricKey(byte[] symmetricKey) {
		this.symmetricKey = symmetricKey;
	}

}
