package dailyplanr.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

@Entity
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column
	private String categoryName;
	
	@Column
	private String catId;
	
	@Column
	private String iv;
	
	@Column
	private byte[] symmetricKey;
	
	@OneToMany(mappedBy = "categories")
	private List<Task> tasks;
	
	@ManyToMany
	@JoinTable(name="categories_user", joinColumns = {@JoinColumn(name = "category_id")}, inverseJoinColumns = {@JoinColumn(name="user_id")})
	List<User> users = new ArrayList<>();
	
	public void addUsersCategory(User user) {
		this.users.add(user);
		user.getCategories().add(this);
	}
	
	public Category() {
		
	}

	public Category(String categoryName) {
		this.categoryName = categoryName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getCatId() {
		return catId;
	}

	public void setCatId(String catId) {
		this.catId = catId;
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

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
	
}
