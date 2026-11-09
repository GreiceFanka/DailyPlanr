package dailyplanr.models;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long>{
	@Query(value= "select * from Category ct\n"
			+ "inner join categories_user ctu ON ct.id=ctu.category_id\n"
			+ "where user_id = ? ", nativeQuery = true)
	public List<Category> findCategoryByUser(long id);
	
	@Modifying
	@Query(value="UPDATE Category SET categoryName = ? "
			+ "WHERE id = ? ", nativeQuery = true)
	public void updateCategory(String categoryName, long id);
	
	@Modifying
	@Query(value="UPDATE Category SET catId = ?, iv = ?, symmetricKey = ?  WHERE id = ? ", nativeQuery = true)
	public void saveKeys(String categoryEncId, String base64Iv, byte[] cKey, long catId);
	
	public List<Category> findCategoryById(long id);
	
	@Query(value="SELECT id FROM Category WHERE catId = ? ", nativeQuery = true)
	public long findCategory(String catId);
}
