
package portfolio_auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import portfolio_auth.entity.MfaToken;
import portfolio_auth.repository.MfaTokenRepository;

@Service
public class MfaService {

    private final MfaTokenRepository repository;

    private final GoogleAuthenticator authenticator = new GoogleAuthenticator();

    public MfaService(MfaTokenRepository repository) {
        this.repository = repository;
    }

    public MfaSetupResponse setup(String email) {

        GoogleAuthenticatorKey key = authenticator.createCredentials();

        MfaToken token = repository.findByEmail(email)
                .orElse(new MfaToken());

        token.setEmail(email);
        token.setSecret(key.getKey());
        token.setEnabled(false);

        repository.save(token);

        String label = URLEncoder.encode(
                "Portfolio:" + email,
                StandardCharsets.UTF_8);

        String issuer = URLEncoder.encode(
                "Portfolio",
                StandardCharsets.UTF_8);

        String otpUrl = "otpauth://totp/" + label
                + "?secret=" + key.getKey()
                + "&issuer=" + issuer;

        return new MfaSetupResponse(key.getKey(), otpUrl);
    }

    public boolean verify(String email, int code) {

        MfaToken token = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("MFA setup not found"));

        return authenticator.authorize(
                token.getSecret(), code);
    }

    public void enable(String email, int code) {

        MfaToken token = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("MFA setup not found"));

        if (!authenticator.authorize(token.getSecret(), code)) {
            throw new RuntimeException("Invalid MFA code");
        }

        token.setEnabled(true);
        repository.save(token);
    }

    public record MfaSetupResponse(
            String secret,
            String otpAuthUrl) {
    }
    public boolean isEnabled(String email) {
    return repository.findByEmail(email)
            .map(MfaToken::isEnabled)
            .orElse(false);
}
}
