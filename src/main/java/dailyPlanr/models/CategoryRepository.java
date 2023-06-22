package dailyPlanr.models;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Integer>{
	@Query(value= "select * from Category ct\n"
			+ "inner join categories_user ctu ON ct.id=ctu.category_id\n"
			+ "where user_id = ? ", nativeQuery = true)
	public List<Category> findCategoryByUser(int id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE Category SET categoryName = ? "
			+ "WHERE id = ? ", nativeQuery = true)
	public void updateCategory(String categoryName, int id);
	
	public List<Category> findCategoryById(int id);
}
