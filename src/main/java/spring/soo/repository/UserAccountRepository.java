package spring.soo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import spring.soo.domain.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
}
