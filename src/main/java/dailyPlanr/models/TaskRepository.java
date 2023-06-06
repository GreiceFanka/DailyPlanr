package dailyPlanr.models;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<Task, Integer>{
	
	@Query(value= "select * from task tk\n"
			+ "inner join task_user tku ON tk.id=tku.task_id\n"
			+ "where user_id = ? ", nativeQuery = true)
	public Iterable<Task> findTaskByUser(int id); 
}
