package dailyPlanr.models;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends CrudRepository<User, Integer>{
	@Query(value="SELECT * FROM User WHERE company = ? ", nativeQuery = true)
	public Iterable<User> findUserWithSameCompany(String company);
	
	public Optional<User> findByLogin(String login);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE User SET password = ? WHERE id = ? ", nativeQuery = true)
	public void updatePassword(String password, int id);
}
