package dailyplanr.models;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends CrudRepository<User, Integer>{
	@Query(value="SELECT * FROM User WHERE company = ? ", nativeQuery = true)
	public Iterable<User> findUserWithSameCompany(String company);
	
	@Query(value="SELECT * FROM User WHERE hashu = ? ", nativeQuery = true)
	public Optional<User> findUsrInf(String hashu);
	
	public Optional<User> findByLogin(String login);
	
	@Query(value="SELECT * FROM User WHERE token = ? ", nativeQuery = true)
	public Optional<User> findToken(String token);
	
	@Query(value="SELECT * FROM User WHERE token <> '' AND temporary_salt > 0", nativeQuery = true)
	public Iterable<User> findUsersWithToken();
	
	@Transactional
	@Modifying
	@Query(value="UPDATE User SET image = ? "
			+ "WHERE id = ? ",nativeQuery = true)
	public void saveImageById(byte[] image, int user_id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE User SET password = ? WHERE id = ? ", nativeQuery = true)
	public void updatePassword(String password, int id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE User SET token = ?, temporary_salt = ? WHERE id = ? ", nativeQuery = true)
	public void userDestroyToken(String token, LocalDateTime temporary_salt, int id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE User SET login_attempts = ?, time_block = ? WHERE id = ? ", nativeQuery = true)
	public void userTimeBlock (int login_attempts, LocalDateTime time_block, int id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE User SET temporary_salt = ?, token = ? WHERE id = ? ", nativeQuery = true)
	public void saveTemporary(LocalDateTime temporary_salt, String token, int id);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE User SET hashu = ?, iv = ?, symmetricKey = ?  WHERE id = ? ", nativeQuery = true)
	public void saveKeys(String hashu, String iv, byte[] uKey, int uid);
}
