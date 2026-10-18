package com.lastmile.delivery;

import com.lastmile.security.AppUserDetails;
import com.lastmile.web.BusinessRuleException;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/deliveries")
public class DeliveryController {

    private static final String[] EDITABLE_FIELDS = {
            "recipientName", "recipientPhone", "deliveryAddress", "deliveryCity", "referenceNumber", "assignedAgentId"};

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /** Defence in depth: only whitelisted fields can ever be bound from the request. */
    @InitBinder("deliveryForm")
    void restrictBinding(WebDataBinder binder) {
        binder.setAllowedFields(EDITABLE_FIELDS);
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("deliveryForm", new DeliveryForm());
        model.addAttribute("agents", deliveryService.assignableAgents());
        return "deliveries/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("deliveryForm") DeliveryForm form, BindingResult binding,
                         @AuthenticationPrincipal AppUserDetails principal, Model model,
                         RedirectAttributes redirect) {
        if (!binding.hasErrors()) {
            try {
                DeliveryView created = deliveryService.create(form, principal);
                redirect.addFlashAttribute("successMessage", "Delivery " + created.deliveryCode() + " created.");
                return "redirect:/deliveries/" + created.id();
            } catch (BusinessRuleException ex) {
                binding.rejectValue(ex.getField(), "business", ex.getMessage());
            }
        }
        model.addAttribute("agents", deliveryService.assignableAgents());
        return "deliveries/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id, @AuthenticationPrincipal AppUserDetails principal, Model model) {
        model.addAttribute("delivery", deliveryService.get(id, principal));
        return "deliveries/detail";
    }
}
