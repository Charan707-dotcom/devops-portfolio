
package portfolio_auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import portfolio_auth.service.MfaService;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private static final String PENDING_AUTH = "MFA_PENDING_AUTH";
    private static final String PENDING_TIME = "MFA_PENDING_TIME";

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final MfaService mfaService;

    public LoginController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            MfaService mfaService) {

        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.mfaService = mfaService;
    }

    public record LoginRequest(String email, String password) {}

    public record MfaLoginRequest(int code) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        try {
            SecurityContextHolder.clearContext();

            HttpSession oldSession = httpRequest.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            String email = request.email().trim().toLowerCase();

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    request.password()
                            )
                    );

            // Require MFA if enabled for this account
            if (mfaService.isEnabled(email)) {

                HttpSession session = httpRequest.getSession(true);

                session.setAttribute(PENDING_AUTH, authentication);
                session.setAttribute(
                        PENDING_TIME,
                        System.currentTimeMillis()
                );

                return ResponseEntity.accepted()
                        .body("MFA_REQUIRED");
            }

            // Normal login when MFA is disabled
            createAuthenticatedSession(
                    authentication,
                    httpRequest,
                    httpResponse
            );

            return ResponseEntity.ok("Login successful");

        } catch (Exception exception) {
            SecurityContextHolder.clearContext();

            return ResponseEntity.status(401)
                    .body("Invalid email or password");
        }
    }

    @PostMapping("/login/mfa")
    public ResponseEntity<?> verifyMfa(
            @RequestBody MfaLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            return ResponseEntity.status(401)
                    .body("Please login again");
        }

        Authentication pendingAuthentication =
                (Authentication) session.getAttribute(PENDING_AUTH);

        Long pendingTime =
                (Long) session.getAttribute(PENDING_TIME);

        if (pendingAuthentication == null || pendingTime == null
                || System.currentTimeMillis() - pendingTime > 300000) {

            session.invalidate();

            return ResponseEntity.status(401)
                    .body("MFA session expired. Please login again");
        }

        boolean validCode;

        try {
            validCode = mfaService.verify(
                    pendingAuthentication.getName(),
                    request.code()
            );
        } catch (Exception exception) {
            validCode = false;
        }

        if (!validCode) {
            return ResponseEntity.status(401)
                    .body("Invalid MFA code");
        }

        session.removeAttribute(PENDING_AUTH);
        session.removeAttribute(PENDING_TIME);

        // Rotate session ID after successful MFA
        httpRequest.changeSessionId();

        createAuthenticatedSession(
                pendingAuthentication,
                httpRequest,
                httpResponse
        );

        return ResponseEntity.ok(
                "Login successful. MFA verified"
        );
    }

    private void createAuthenticatedSession(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        request.getSession(true);

        securityContextRepository.saveContext(
                context,
                request,
                response
        );
    }
}
