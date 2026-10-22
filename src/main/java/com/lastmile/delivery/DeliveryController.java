package com.lastmile.delivery;

import com.lastmile.security.AppUserDetails;
import com.lastmile.user.Role;
import com.lastmile.web.BusinessRuleException;
import com.lastmile.web.ConflictException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) DeliveryStatus status,
                       @RequestParam(required = false) Long agentId,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "" + DeliverySearch.DEFAULT_PAGE_SIZE) int size,
                       @AuthenticationPrincipal AppUserDetails principal, Model model) {
        DeliverySearch search = new DeliverySearch(q, status, principal.getRole() == Role.AGENT ? null : agentId);
        Page<DeliverySummary> results = deliveryService.search(search, page, size, principal);
        model.addAttribute("results", results);
        model.addAttribute("search", search);
        model.addAttribute("statuses", DeliveryStatus.values());
        if (principal.getRole().isStaff()) {
            var agents = deliveryService.assignableAgents();
            model.addAttribute("agents", agents);
            agents.stream().filter(a -> search.agentId() != null && a.id() == search.agentId()).findFirst()
                    .ifPresent(a -> model.addAttribute("agentName", a.name()));
        }
        return "deliveries/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("deliveryForm", new DeliveryForm());
        return showForm(model, null);
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
        return showForm(model, null);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id, @AuthenticationPrincipal AppUserDetails principal, Model model) {
        DeliveryView view = deliveryService.get(id, principal);
        model.addAttribute("delivery", view);
        model.addAttribute("actions", deliveryService.actionsFor(view, principal));
        if (principal.getRole().isStaff()) {
            model.addAttribute("agents", deliveryService.assignableAgents());
        }
        return "deliveries/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable long id, @AuthenticationPrincipal AppUserDetails principal, Model model) {
        DeliveryView view = deliveryService.get(id, principal);
        if (!DeliveryPolicy.canEdit(principal.getRole(), view.status())) {
            throw new ConflictException("This delivery can no longer be edited.");
        }
        model.addAttribute("deliveryForm", DeliveryForm.from(view));
        return showForm(model, view);
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable long id, @Valid @ModelAttribute("deliveryForm") DeliveryForm form,
                       BindingResult binding, @RequestParam(required = false) Long version,
                       @AuthenticationPrincipal AppUserDetails principal, Model model, RedirectAttributes redirect) {
        if (!binding.hasErrors()) {
            try {
                DeliveryView updated = deliveryService.update(id, form, version, principal);
                redirect.addFlashAttribute("successMessage", "Delivery " + updated.deliveryCode() + " updated.");
                return "redirect:/deliveries/" + id;
            } catch (ConflictException ex) {
                redirect.addFlashAttribute("errorMessage", ex.getMessage());
                return "redirect:/deliveries/" + id;
            }
        }
        return showForm(model, deliveryService.get(id, principal));
    }

    @PostMapping("/{id}/assignment")
    public String assign(@PathVariable long id, @RequestParam(required = false) Long agentId,
                         @RequestParam(required = false) Long version,
                         @AuthenticationPrincipal AppUserDetails principal, RedirectAttributes redirect) {
        try {
            DeliveryView view = deliveryService.assign(id, agentId, version, principal);
            redirect.addFlashAttribute("successMessage", "Assigned to " + view.assignedAgentName() + ".");
        } catch (BusinessRuleException | ConflictException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/deliveries/" + id;
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable long id, @RequestParam(required = false) DeliveryStatus newStatus,
                               @RequestParam(required = false) String reason,
                               @RequestParam(required = false) Long version,
                               @AuthenticationPrincipal AppUserDetails principal, RedirectAttributes redirect) {
        try {
            DeliveryView view = deliveryService.changeStatus(id, newStatus, reason, version, principal);
            redirect.addFlashAttribute("successMessage", "Status changed to " + view.status().label() + ".");
        } catch (BusinessRuleException | ConflictException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/deliveries/" + id;
    }

    private String showForm(Model model, DeliveryView editing) {
        model.addAttribute("editing", editing);
        if (editing == null) {
            model.addAttribute("agents", deliveryService.assignableAgents());
        }
        return "deliveries/form";
    }
}
