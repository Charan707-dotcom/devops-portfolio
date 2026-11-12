
package portfolio_auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping
    public String dashboard(Authentication authentication) {
        return "Welcome to your protected portfolio dashboard, "
                + authentication.getName();
    }
}
