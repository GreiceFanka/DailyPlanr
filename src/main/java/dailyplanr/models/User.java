package dailyplanr.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;

@Entity
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(unique = true, nullable = false)
	private String login;
	
	@Column(nullable = false)
	private String password;
	
	@Column(nullable = false)
	private String name;
	
	@Column
	private String company;
	
	@Column
	private String salt;
	
	@Column(nullable = false)
	private int login_attempts;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime time_block;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime temporary_salt;
	
	@Column
	private String token; 
	
	@Lob
	@Column(columnDefinition="longblob")
	private byte[] image;
	
	@Column
	private String hashu;
	
	@Column
	private String iv;
	
	@Column
	private byte[] symmetricKey;
	
	@ManyToMany(mappedBy = "users")
	List<Task> tasks = new ArrayList<>();
	
	@ManyToMany(mappedBy = "users")
	List<Category> categories = new ArrayList<>();
	
	public User() {

	}
	public User(String login, String password, String name, String company, String salt, int login_attempts,LocalDateTime time_block, String token) {
		this.login = login;
		this.password = password;
		this.name = name;
		this.company = company;
		this.salt = salt;
		this.login_attempts = login_attempts;
		this.time_block = time_block;
		this.token = token;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	public List<Category> getCategories() {
		return categories;
	}
	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public int getLogin_attempts() {
		return login_attempts;
	}
	public void setLogin_attempts(int login_attempts) {
		this.login_attempts = login_attempts;
	}
	public LocalDateTime getTime_block() {
		return time_block;
	}
	public void setTime_block(LocalDateTime time_block) {
		this.time_block = time_block;
	}
	public LocalDateTime getTemporary_salt() {
		return temporary_salt;
	}
	public void setTemporary_salt(LocalDateTime temporary_salt) {
		this.temporary_salt = temporary_salt;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getHashu() {
		return hashu;
	}
	public void setHashu(String hashu) {
		this.hashu = hashu;
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
