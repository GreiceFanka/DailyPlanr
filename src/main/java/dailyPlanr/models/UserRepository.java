package dailyPlanr.models;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer>{
	public Optional<User> findByLogin(String login);
}
