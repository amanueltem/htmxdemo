package com.aman.htmxdemo.deposit;

import com.aman.htmxdemo.common.EntityStatus;
import com.aman.htmxdemo.expense.Expense;
import com.aman.htmxdemo.handler.OperationNotPermittedException;
import com.aman.htmxdemo.user.User;
import com.aman.htmxdemo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller
@RequestMapping("/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositRepository depositRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String listDeposits(Model model,
                               @AuthenticationPrincipal User currentUser,
                               @RequestHeader(value = "HX-Request", required = false) boolean isHtmx,
                               @PageableDefault(size = 10) Pageable pageable) {
        Page<Deposit> depositPage = depositRepository.findAll(pageable);
        model.addAttribute("deposits", depositPage);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", "deposits");
        return isHtmx ? "deposit/deposit :: deposit-table-container" : "deposit/deposit";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('INPUTTER')")
    public String showCreateForm(Model model) {
        model.addAttribute("deposit", new Deposit());
        return "deposit/deposit-form :: deposit-form";
    }

    @PostMapping
    @PreAuthorize("hasRole('INPUTTER')")
    public String createDeposit(@ModelAttribute Deposit deposit,
                                @AuthenticationPrincipal User currentUser,
                                Model model, @PageableDefault(size = 10) Pageable pageable) {
        deposit.setEntityStatus(EntityStatus.UNAUTHORIZED.name());
        deposit.setInputter(currentUser.getEmail());
        depositRepository.save(deposit);
        return refreshTable(model, currentUser, pageable);
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('INPUTTER')")
    public String showEditForm(@PathVariable UUID id, Model model) {
        Deposit deposit = depositRepository.findById(id).orElseThrow();
        model.addAttribute("deposit", deposit);
        return "deposit/deposit-edit-form :: deposit-edit-form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('INPUTTER')")
    public String updateDeposit(@PathVariable UUID id, @ModelAttribute Deposit depositDetails,
                                @AuthenticationPrincipal User currentUser, Model model, @PageableDefault(size = 10) Pageable pageable) {
        Deposit existing = depositRepository.findById(id).orElseThrow();
        existing.setAmount(depositDetails.getAmount());
        existing.setDate(depositDetails.getDate());
        existing.setEntityStatus(EntityStatus.UNAUTHORIZED.name());
        depositRepository.save(existing);
        return refreshTable(model, currentUser, pageable);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INPUTTER')")
    public String deleteDeposit(@PathVariable UUID id, @AuthenticationPrincipal User currentUser, Model model, @PageableDefault(size = 10) Pageable pageable) {
        depositRepository.deleteById(id);
        return refreshTable(model, currentUser, pageable);
    }

    // Add this missing endpoint to your controller
    @PostMapping("/{id}/authorize")
    @PreAuthorize("hasRole('AUTHORIZER')")
    public String authorizeDeposit(@PathVariable UUID id,
                                   @AuthenticationPrincipal User currentUser,
                                   Model model,
                                   @PageableDefault(size = 10) Pageable pageable) {

        Deposit deposit = depositRepository.findById(id).orElseThrow();

        // Dual-control check
        if (deposit.getInputter().equals(currentUser.getEmail())) {
            throw new RuntimeException("Cannot authorize your own deposit");
        }

        deposit.setEntityStatus(EntityStatus.AUTHORIZED.name());
        deposit.setAuthorizer(currentUser.getEmail());
        depositRepository.save(deposit);

        return refreshTable(model, currentUser, pageable);
    }


    @GetMapping("/{id}/edit-request")
    @PreAuthorize("hasRole('INPUTTER')")
    public String showEditRequestForm(@PathVariable UUID id,
                                      @AuthenticationPrincipal User currentUser,
                                      Model model) {

       Deposit deposit = depositRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!EntityStatus.AUTHORIZED.name().equals(deposit.getEntityStatus())) {
            throw new OperationNotPermittedException("Edit request allowed only for AUTHORIZED records");
        }

        if (!deposit.getInputter().equals(currentUser.getEmail())) {
            throw new OperationNotPermittedException("This is not your record");
        }

        model.addAttribute("deposit", deposit);
        return "deposit/deposit-edit-request :: deposit-edit-request-form";
    }

    @PostMapping("/{id}/edit-request")
    @PreAuthorize("hasRole('INPUTTER')")
    public String submitEditRequest(@PathVariable UUID id,
                                    @RequestParam String editRequestRemark,
                                    @AuthenticationPrincipal User currentUser,
                                    Model model,
                                    @PageableDefault(size = 10) Pageable pageable) {

        if (editRequestRemark == null || editRequestRemark.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Edit request remark is required"
            );
        }

      Deposit deposit = depositRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!EntityStatus.AUTHORIZED.name().equals(deposit.getEntityStatus())) {
            throw new OperationNotPermittedException("Invalid state for edit request");
        }

        if (!deposit.getInputter().equals(currentUser.getEmail())) {
            throw new OperationNotPermittedException("This is not your record");
        }

        deposit.setEditRequestRemark(editRequestRemark);
        deposit.setEntityStatus(EntityStatus.EDIT_REQUEST.name());

       depositRepository.save(deposit);

        return refreshTable(model, currentUser, pageable);
    }
    @PostMapping("/{id}/accept-edit-request")
    @PreAuthorize("hasRole('AUTHORIZER')")
    public String acceptEditRequest(@PathVariable UUID id,
                                    @AuthenticationPrincipal User currentUser,
                                    Model model,
                                    @PageableDefault(size = 10) Pageable pageable) {

       Deposit deposit = depositRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!EntityStatus.EDIT_REQUEST.name().equals(deposit.getEntityStatus())) {
            throw new OperationNotPermittedException("Record is not in EDIT_REQUEST state");
        }

        if (!currentUser.getEmail().equals(deposit.getAuthorizer())) {
            throw new OperationNotPermittedException(
                    "Only the original authorizer can accept this edit request"
            );
        }
        deposit.setEntityStatus(EntityStatus.UNAUTHORIZED.name());
        deposit.setEditRequestRemark(null);

        depositRepository.save(deposit);

        return refreshTable(model, currentUser, pageable);
    }

    private String refreshTable(Model model, User user, Pageable pageable) {
        Page<Deposit> depositPage = depositRepository.findAll(pageable);
        // Ensure deposits is never null to prevent SpEL errors
        model.addAttribute("deposits", depositPage != null ? depositPage : Page.empty());
        model.addAttribute("currentUser", user);
        return "deposit/deposit :: deposit-table-container";
    }
}