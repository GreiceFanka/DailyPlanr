package dailyplanr.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

public interface TaskRepository extends PagingAndSortingRepository<Task, Integer>, CrudRepository<Task, Integer>{
	
	@Query(value="SELECT user_id FROM task_user WHERE task_id = ? ", nativeQuery = true)
	public List<Integer> findTaskUser(int task_id);
	
	@Query(value="SELECT * FROM Task WHERE encryptId = ? ", nativeQuery = true)
	public Optional<Task> findTaskInf(String encryptId);
	
	@Query(value= "select * from Task tk\n"
			+ "inner join task_user tku ON tk.id=tku.task_id\n"
			+ "where user_id = ? ORDER BY data ASC ", nativeQuery = true)
	public Iterable<Task> findTaskByUser(int id); 
	
	@Query(value= "select * from Task tk\n"
			+ "inner join task_user tku ON tk.id=tku.task_id\n"
			+ "where user_id = ? AND taskStatus IN ('Archive')",nativeQuery = true)
	public Page<Task> findTaskByUserAndStatus(int id, Pageable pageable);
	
	@Query(value = "SELECT DISTINCT u.login FROM User u\n"
			+ "LEFT JOIN task_user tu ON u.id=tu.user_id\n"
			+ "LEFT JOIN Task t ON t.id=tu.task_id\n"
			+ "WHERE t.data <= ? AND t.taskStatus IN ('In progress', 'To do')", nativeQuery = true)
	public List<String> findTaskByDate(LocalDateTime date);
	
	@Query(value="SELECT * FROM Task tk "
			+ "INNER JOIN task_user tku ON tk.id=tku.task_id "
			+ "WHERE tku.user_id = ? AND tk.data < ?  AND tk.taskStatus IN ('In progress', 'To do')", nativeQuery = true)
	public Iterable<Task> findLateTasks(int id, LocalDateTime date);
	
	@Query(value="SELECT * FROM Task tk "
			+ "INNER JOIN task_user tku ON tk.id=tku.task_id "
			+ "WHERE tku.user_id = ? AND tk.updatedStatus BETWEEN ? AND ? AND tk.taskStatus IN ('Done','Archive')", nativeQuery = true)
	public Iterable<Task> findCompletedTasks(int id, LocalDate initialDate, LocalDate finalDate);
	
	@Query(value="select * from Task tk\n "
			+ "inner join task_user tku ON tk.id=tku.task_id\n "
			+ "where user_id = ? AND taskStatus IN ('In progress', 'To do') AND category_id = ? ", nativeQuery = true)
	public List<Task> findTaskByUserAndCategory(int id, Integer category);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE Task SET data = ? , title = ? , description = ? , priority = ? "
			+ "WHERE id = ? ", nativeQuery = true)
	public void updateTask(String data, String title, String description, String priority, int task_id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE Task SET taskStatus = ?, updatedStatus = ? WHERE id = ? ", nativeQuery = true)
	public void editStatus(String taskStatus, LocalDate updatedStatus, int id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE Task SET category_id = ? WHERE id = ? ", nativeQuery = true)
	public void editTaskCategory(int cat_id, int id);
	
	@Transactional
	@Modifying
	@Query(value="INSERT INTO task_user (task_id, user_id) "
			+ "VALUES (?, ?) ", nativeQuery = true)
	public void insertUserTask(int taskId, int userId);
	
	@Transactional
	@Modifying
	@Query(value="DELETE FROM task_user WHERE task_id = ? AND user_id = ? ", nativeQuery = true)
	public void deleteUserTask(int taskId, int userId);
	
	public List<Task> findTaskById(int id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE Task SET encryptId = ?, iv = ?, symmetricKey = ? WHERE id = ? ", nativeQuery = true)
	public void encryptKeyCreation(String encryptId,String taskIv, byte[] taskKey, int id);
	
}
