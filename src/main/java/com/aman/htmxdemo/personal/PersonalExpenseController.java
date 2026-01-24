package com.aman.htmxdemo.personal;
import com.aman.htmxdemo.expense.Expense;
import com.aman.htmxdemo.expense.ExpenseDisplay;
import com.aman.htmxdemo.handler.OperationNotPermittedException;
import com.aman.htmxdemo.user.User;
import com.aman.htmxdemo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/personal-expenses")
@RequiredArgsConstructor
@RegisterReflectionForBinding({ExpenseDisplay.class})
public class PersonalExpenseController {

    private final PersonalExpenseRepository repo;
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
    public String createExpense(@ModelAttribute PersonalExpense expense,
                                @AuthenticationPrincipal User currentUser,
                                Model model,
                                @PageableDefault(size = 10) Pageable pageable) {
        expense.setInputter(currentUser.getEmail());
        repo.save(expense);

        return refreshTableFragment(model, currentUser, pageable);
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('INPUTTER')")
    public String showEditForm(@PathVariable UUID id, Model model) {
        PersonalExpense expense = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
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

        PersonalExpense existingExpense = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
        if(!currentUser.getEmail().equals(existingExpense.getInputter())){
            throw  new OperationNotPermittedException("This is not your record.");
        }
        existingExpense.setAmount(expenseDetails.getAmount());
        existingExpense.setDate(expenseDetails.getDate());
        existingExpense.setTimeSpan(expenseDetails.getTimeSpan());
        repo.save(existingExpense);
        return refreshTableFragment(model, currentUser, pageable);
    }




    @GetMapping("/{id}/edit-request")
    @PreAuthorize("hasRole('INPUTTER')")
    public String showEditRequestForm(@PathVariable UUID id,
                                      @AuthenticationPrincipal User currentUser,
                                      Model model) {

        PersonalExpense expense = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!expense.getInputter().equals(currentUser.getEmail())) {
            throw new OperationNotPermittedException("This is not your record");
        }

        model.addAttribute("expense", expense);
        return "expense/expense-edit-request :: expense-edit-request-form";
    }


    private String refreshTableFragment(Model model, User currentUser, Pageable pageable) {
        Pageable sortedPageable =
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "date")
                );

        Page<PersonalExpense> expensePage = repo.findByInputter(currentUser.getEmail(),pageable);



        // 2. The Nuclear Option: Flatten the Page object into simple variables
        model.addAttribute("expenseList", expensePage.getContent());
        model.addAttribute("currentPage", expensePage.getNumber());
        model.addAttribute("totalPages", expensePage.getTotalPages());
        model.addAttribute("hasNext", expensePage.hasNext());
        model.addAttribute("hasPrev", expensePage.hasPrevious());

        // Keep this for any other logic, but we won't call methods on it in HTML
        model.addAttribute("expenses", expensePage);

        return "expense/expense :: expense-table-container";
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INPUTTER')")
    public String deleteExpense(@PathVariable UUID id,
                                @AuthenticationPrincipal User currentUser,
                                Model model,
                                @PageableDefault(size = 10) Pageable pageable) {

        PersonalExpense expense = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!expense.getInputter().equals(currentUser.getEmail())) {
            throw new OperationNotPermittedException("This is not your record.");
        }

        repo.delete(expense);

        // Refresh and return the table fragment
        return refreshTableFragment(model, currentUser, pageable);
    }

}