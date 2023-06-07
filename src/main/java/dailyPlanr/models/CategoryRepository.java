package dailyPlanr.models;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Integer>{
	@Query(value= "select * from category ct\n"
			+ "inner join categories_user ctu ON ct.id=ctu.category_id\n"
			+ "where user_id = ? ", nativeQuery = true)
	public List<Category> findCategoryByUser(int id);
}
