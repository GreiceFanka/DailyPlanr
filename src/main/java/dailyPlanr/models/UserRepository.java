package dailyPlanr.models;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer>{
	@Query(value="SELECT * FROM User WHERE company = ? ", nativeQuery = true)
	public Iterable<User> findUserWithSameCompany(String company);
	
	public Optional<User> findByLogin(String login);
}
