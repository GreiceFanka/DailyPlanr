package dailyplanr.models;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Long>{
	@Query(value="SELECT * FROM User WHERE company = ? ", nativeQuery = true)
	public Iterable<User> findUserWithSameCompany(String company);
	
	@Query(value="SELECT * FROM User WHERE tempId = ? ", nativeQuery = true)
	public Optional<User> findTempId(String tempId);
	
	@Query(value="SELECT * FROM User WHERE hashu = ? ", nativeQuery = true)
	public Optional<User> findUsrInf(String hashu);
	
	public Optional<User> findByLogin(String login);
	
	public Optional<User> findByToken(String token);
	
	@Query(value="SELECT * FROM User WHERE token <> '' AND temporary_salt > 0", nativeQuery = true)
	public Iterable<User> findUsersWithToken();
	
	@Modifying
	@Query(value="UPDATE User SET image = ? "
			+ "WHERE id = ? ",nativeQuery = true)
	public void saveImageById(byte[] image, long user_id);
	
	@Modifying
	@Query(value="UPDATE User SET password = ?, salt = ? WHERE id = ? ", nativeQuery = true)
	public void updatePassword(String password, String salt, long id);
	
	@Modifying
	@Query(value=" UPDATE User SET token = '',temporary_salt = null WHERE temporary_salt < :now AND token IS NOT NULL", nativeQuery = true)
	public void userDestroyExpiredToken(@Param("now") LocalDateTime now);
	
	@Modifying
	@Query(value=" UPDATE User SET token = '', temporary_salt = null WHERE id = ?",nativeQuery = true)
	public void userDestroyToken(long id);
	
	@Modifying
	@Query(value="UPDATE User SET login_attempts = ?, time_block = ? WHERE id = ? ", nativeQuery = true)
	public void userTimeBlock (int login_attempts, LocalDateTime time_block, long id);
	
	@Modifying
	@Query(value="UPDATE User SET temporary_salt = ?, token = ? WHERE id = ? ", nativeQuery = true)
	public void saveTemporary(LocalDateTime temporary_salt, String token, long id);
	
	@Modifying
	@Query(value="UPDATE User SET hashu = ?, iv = ?, symmetricKey = ?  WHERE id = ? ", nativeQuery = true)
	public void saveKeys(String hashu, String iv, byte[] uKey, long uid);
}
