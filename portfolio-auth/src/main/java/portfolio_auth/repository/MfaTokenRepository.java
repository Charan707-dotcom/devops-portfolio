
package portfolio_auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import portfolio_auth.entity.MfaToken;

public interface MfaTokenRepository
        extends JpaRepository<MfaToken, Long> {

    Optional<MfaToken> findByEmail(String email);
}
