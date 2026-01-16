package com.aman.htmxdemo.expense;

import com.aman.htmxdemo.common.EntityStatus;
import com.aman.htmxdemo.handler.OperationNotPermittedException;
import com.aman.htmxdemo.user.User;
import com.aman.htmxdemo.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
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
import java.util.stream.Stream;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
@RegisterReflectionForBinding({ExpenseDisplay.class})
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String listExpenses(Model model,
                               @AuthenticationPrincipal User currentUser,
                               @RequestHeader(value = "HX-Request", required = false) boolean isHtmx,
                               @PageableDefault(size = 10) Pageable pageable) {

        // 1. Set the basic UI attributes
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", "expenses");

        // 2. Use the refreshTableFragment logic to populate the list and pagination variables
        refreshTableFragment(model, currentUser, pageable);

        // 3. Return the full page or just the fragment
        return isHtmx ? "expense/expense :: expense-table-container" : "expense/expense";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('INPUTTER')")
    public String showCreateForm(Model model) {
        model.addAttribute("expense", new Expense());
        // Convert Enums to Strings here to avoid reflection in the template
        model.addAttribute("timeSpans", Stream.of(TimeSpan.values()).map(Enum::name).toList());
        return "expense/expense-form :: expense-form";
    }

    @PostMapping
    @PreAuthorize("hasRole('INPUTTER')")
    public String createExpense(@ModelAttribute Expense expense,
                                @AuthenticationPrincipal User currentUser,
                                Model model,
                                @PageableDefault(size = 10) Pageable pageable) {

        expense.setEntityStatus(EntityStatus.UNAUTHORIZED.name());
        expense.setInputter(currentUser.getEmail());
        expenseRepository.save(expense);

        return refreshTableFragment(model, currentUser, pageable);
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('INPUTTER')")
    public String showEditForm(@PathVariable UUID id, Model model) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));

        // Check status using String comparison for safety
        if ("AUTHORIZED".equals(expense.getEntityStatus().toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot edit authorized record");
        }

        model.addAttribute("expense", expense);
        model.addAttribute("timeSpans", Stream.of(TimeSpan.values()).map(Enum::name).toList());
        return "expense/expense-edit-form :: expense-edit-form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('INPUTTER')")
    public String updateExpense(@PathVariable UUID id,
                                @ModelAttribute Expense expenseDetails,
                                @AuthenticationPrincipal User currentUser,
                                Model model,
                                @PageableDefault(size = 10) Pageable pageable) {

        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));

        if (EntityStatus.AUTHORIZED.name().equals(existingExpense.getEntityStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update authorized record");
        }
        if(!currentUser.getEmail().equals(existingExpense.getInputter())){
            throw  new OperationNotPermittedException("This is not your record.");
        }

        existingExpense.setAmount(expenseDetails.getAmount());
        existingExpense.setDate(expenseDetails.getDate());
        existingExpense.setTimeSpan(expenseDetails.getTimeSpan());
        existingExpense.setEntityStatus(EntityStatus.UNAUTHORIZED.name());
        expenseRepository.save(existingExpense);

        return refreshTableFragment(model, currentUser, pageable);
    }

    // --- CHECKER FLOW (AUTHORIZER) ---

    @PostMapping("/{id}/authorize")
    @PreAuthorize("hasRole('AUTHORIZER')")
    public String authorizeExpense(@PathVariable UUID id,
                                   @AuthenticationPrincipal User currentUser,
                                   Model model,
                                   @PageableDefault(size = 10) Pageable pageable) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (expense.getInputter().equals(currentUser.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot authorize your own entry");
        }

        User creator = userRepository.findByEmail(expense.getInputter())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!creator.getGroupMember().getId().equals(currentUser.getGroupMember().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not in same group");
        }

        expense.setEntityStatus(EntityStatus.AUTHORIZED.name());
        expense.setAuthorizer(currentUser.getEmail());
        expenseRepository.save(expense);

        return refreshTableFragment(model, currentUser, pageable);
    }


    @GetMapping("/{id}/edit-request")
    @PreAuthorize("hasRole('INPUTTER')")
    public String showEditRequestForm(@PathVariable UUID id,
                                      @AuthenticationPrincipal User currentUser,
                                      Model model) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!EntityStatus.AUTHORIZED.name().equals(expense.getEntityStatus())) {
            throw new OperationNotPermittedException("Edit request allowed only for AUTHORIZED records");
        }

        if (!expense.getInputter().equals(currentUser.getEmail())) {
            throw new OperationNotPermittedException("This is not your record");
        }

        model.addAttribute("expense", expense);
        return "expense/expense-edit-request :: expense-edit-request-form";
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

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!EntityStatus.AUTHORIZED.name().equals(expense.getEntityStatus())) {
            throw new OperationNotPermittedException("Invalid state for edit request");
        }

        if (!expense.getInputter().equals(currentUser.getEmail())) {
            throw new OperationNotPermittedException("This is not your record");
        }

        expense.setEditRequestRemark(editRequestRemark);
        expense.setEntityStatus(EntityStatus.EDIT_REQUEST.name());

        expenseRepository.save(expense);

        return refreshTableFragment(model, currentUser, pageable);
    }
    @PostMapping("/{id}/accept-edit-request")
    @PreAuthorize("hasRole('AUTHORIZER')")
    public String acceptEditRequest(@PathVariable UUID id,
                                    @AuthenticationPrincipal User currentUser,
                                    Model model,
                                    @PageableDefault(size = 10) Pageable pageable) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!EntityStatus.EDIT_REQUEST.name().equals(expense.getEntityStatus())) {
            throw new OperationNotPermittedException("Record is not in EDIT_REQUEST state");
        }

        if (!currentUser.getEmail().equals(expense.getAuthorizer())) {
            throw new OperationNotPermittedException(
                    "Only the original authorizer can accept this edit request"
            );
        }
        expense.setEntityStatus(EntityStatus.UNAUTHORIZED.name());
        expense.setEditRequestRemark(null);

        expenseRepository.save(expense);

        return refreshTableFragment(model, currentUser, pageable);
    }


    // Add this helper method to your ExpenseController
    private ExpenseDisplay mapToDisplay(Expense e, User currentUser) {
        String currentEmail = currentUser.getEmail();
        boolean isInputter = e.getInputter().equals(currentEmail);
        boolean isAuthorizer = currentEmail.equals(e.getAuthorizer());

        // Check if authorizer is in same group but not the same person
        boolean canAuthLogic = !isInputter && e.getEntityStatus().equals("UNAUTHORIZED");

        return new ExpenseDisplay(
                e.getId(),
                e.getDate().toString(),
                e.getTimeSpan(),
                String.format("%.2f", e.getAmount()),
                e.getEntityStatus(),
                e.getInputter(),
                "AUTHORIZED".equals(e.getEntityStatus()),
                // canEdit
                "UNAUTHORIZED".equals(e.getEntityStatus()) && isInputter,
                // canDelete
                "UNAUTHORIZED".equals(e.getEntityStatus()) && isInputter,
                // canAuthorize
                canAuthLogic,
                // canRequestEdit
                "AUTHORIZED".equals(e.getEntityStatus()) && isInputter,
                // canAcceptEditRequest
                "EDIT_REQUEST".equals(e.getEntityStatus()) && isAuthorizer
        );
    }

    private String refreshTableFragment(Model model, User currentUser, Pageable pageable) {
        Page<Expense> expensePage = expenseRepository.findAll(pageable);

        // 1. Map to your Reflection-Free Record
        Page<ExpenseDisplay> displayPage = expensePage.map(e -> mapToDisplay(e, currentUser));

        // 2. The Nuclear Option: Flatten the Page object into simple variables
        model.addAttribute("expenseList", displayPage.getContent());
        model.addAttribute("currentPage", displayPage.getNumber());
        model.addAttribute("totalPages", displayPage.getTotalPages());
        model.addAttribute("hasNext", displayPage.hasNext());
        model.addAttribute("hasPrev", displayPage.hasPrevious());

        // Keep this for any other logic, but we won't call methods on it in HTML
        model.addAttribute("expenses", displayPage);

        return "expense/expense :: expense-table-container";
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INPUTTER')")
    public String deleteExpense(@PathVariable UUID id,
                                @AuthenticationPrincipal User currentUser,
                                Model model,
                                @PageableDefault(size = 10) Pageable pageable) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!EntityStatus.UNAUTHORIZED.name().equals(expense.getEntityStatus())) {
            throw new OperationNotPermittedException("you can  delete  only unauthorized data");
        }
        if (!expense.getInputter().equals(currentUser.getEmail())) {
            throw new OperationNotPermittedException("This is not your record.");
        }

        expenseRepository.delete(expense);

        // Refresh and return the table fragment
        return refreshTableFragment(model, currentUser, pageable);
    }




}