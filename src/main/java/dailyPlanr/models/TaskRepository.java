package dailyPlanr.models;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface TaskRepository extends CrudRepository<Task, Integer>{
	
	@Query(value= "select * from task tk\n"
			+ "inner join task_user tku ON tk.id=tku.task_id\n"
			+ "where user_id = ? ", nativeQuery = true)
	public Iterable<Task> findTaskByUser(int id); 
	
	@Query(value = "SELECT DISTINCT u.login FROM user u\n"
			+ "LEFT JOIN task_user tu ON u.id=tu.user_id\n"
			+ "LEFT JOIN task t ON t.id=tu.task_id\n"
			+ "WHERE t.data <= ? AND t.task_status IN ('In progress', 'To do');", nativeQuery = true)
	public List<String> findTaskByDate(LocalDateTime date);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE task SET data = ? , title = ? , description = ?  "
			+ "WHERE id = ? ", nativeQuery = true)
	public void updateTask(String data, String title, String description, int task_id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE task SET task_status = ? WHERE id = ? ", nativeQuery = true)
	public void editStatus(String taskStatus, int id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE task SET category_id = ? WHERE id = ? ", nativeQuery = true)
	public void editTaskCategory(int cat_id, int id);
	
	public List<Task> findTaskById(int id);
}
