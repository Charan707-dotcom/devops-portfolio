
package portfolio_auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import portfolio_auth.service.MfaService;
import portfolio_auth.service.MfaService.MfaSetupResponse;

@RestController
@RequestMapping("/api/mfa")
public class MfaController {

    private final MfaService mfaService;

    public MfaController(MfaService mfaService) {
        this.mfaService = mfaService;
    }

    private String getEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        }

        return authentication.getName();
    }

    @GetMapping("/setup")
    public MfaSetupResponse setup(Authentication authentication) {
        String email = getEmail(authentication);
        return mfaService.setup(email);
    }

    @PostMapping("/enable")
    public String enable(
            Authentication authentication,
            @RequestBody MfaRequest request) {

        String email = getEmail(authentication);

        mfaService.enable(email, request.code());

        return "MFA enabled successfully";
    }

    public record MfaRequest(int code) {
    }
}
