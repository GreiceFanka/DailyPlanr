package dailyPlanr.models;

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
	
	@Transactional
	@Modifying
	@Query(value="UPDATE task SET data = ? , title = ? , description = ? ,task_status = ? , category_id = ? "
			+ "WHERE id = ? ", nativeQuery = true)
	public void updateTask(String data, String title, String description, String taskStatus, int cat_id, int task_id);
	
	public List<Task> findTaskById(int id);
}
