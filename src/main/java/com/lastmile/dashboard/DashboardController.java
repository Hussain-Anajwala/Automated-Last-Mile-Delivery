package com.lastmile.dashboard;

import com.lastmile.delivery.DeliveryStatus;
import com.lastmile.security.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal AppUserDetails principal, Model model) {
        model.addAttribute("summary", dashboardService.summary(principal));
        model.addAttribute("statuses", DeliveryStatus.values());
        return "dashboard";
    }
}
