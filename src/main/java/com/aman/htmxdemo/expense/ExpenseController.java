package com.aman.htmxdemo.expense;

import com.aman.htmxdemo.common.EntityStatus;
import com.aman.htmxdemo.handler.OperationNotPermittedException;
import com.aman.htmxdemo.user.User;
import com.aman.htmxdemo.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String listExpenses(Model model,
                               @AuthenticationPrincipal User currentUser,
                               @RequestHeader(value = "HX-Request", required = false) boolean isHtmx,
                               @PageableDefault(size = 10) Pageable pageable) {

        Page<Expense> expensePage = expenseRepository.findAll(pageable);
        model.addAttribute("expenses", expensePage);
        model.addAttribute("currentUser", currentUser);

        if (isHtmx) {
            return "expense/expense :: expense-table-container";
        }
        return "expense/expense";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('INPUTTER')")
    public String showCreateForm(Model model) {
        model.addAttribute("expense", new Expense());
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

        if (EntityStatus.AUTHORIZED.name().equals(expense.getEntityStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot edit authorized record");
        }

        model.addAttribute("expense", expense);
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

    // Helper to avoid code duplication
    private String refreshTableFragment(Model model, User currentUser, Pageable pageable) {
        Page<Expense> expensePage = expenseRepository.findAll(pageable);
        model.addAttribute("expenses", expensePage);
        model.addAttribute("currentUser", currentUser);
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