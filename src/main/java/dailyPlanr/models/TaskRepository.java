package dailyPlanr.models;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface TaskRepository extends CrudRepository<Task, Integer>{
	
	@Query(value= "select * from Task tk\n"
			+ "inner join task_user tku ON tk.id=tku.task_id\n"
			+ "where user_id = ? ", nativeQuery = true)
	public Iterable<Task> findTaskByUser(int id); 
	
	@Query(value = "SELECT DISTINCT u.login FROM User u\n"
			+ "LEFT JOIN task_user tu ON u.id=tu.user_id\n"
			+ "LEFT JOIN Task t ON t.id=tu.task_id\n"
			+ "WHERE t.data <= ? AND t.taskStatus IN ('In progress', 'To do')", nativeQuery = true)
	public List<String> findTaskByDate(LocalDateTime date);
	
	@Query(value="SELECT * FROM Task tk "
			+ "INNER JOIN task_user tku ON tk.id=tku.task_id "
			+ "WHERE tku.user_id = ? AND tk.data < ?  AND tk.taskStatus IN ('In progress', 'To do')", nativeQuery = true)
	public Iterable<Task> findLateTasks(int id, LocalDateTime date);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE Task SET data = ? , title = ? , description = ? , priority = ? "
			+ "WHERE id = ? ", nativeQuery = true)
	public void updateTask(String data, String title, String description, String priority, int task_id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE Task SET taskStatus = ? WHERE id = ? ", nativeQuery = true)
	public void editStatus(String taskStatus, int id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE Task SET category_id = ? WHERE id = ? ", nativeQuery = true)
	public void editTaskCategory(int cat_id, int id);
	
	@Transactional
	@Modifying
	@Query(value="INSERT INTO task_user (task_id, user_id) "
			+ "VALUES (?, ?) ", nativeQuery = true)
	public void insertUserTask(int taskId, int userId);
	
	public List<Task> findTaskById(int id);
}
