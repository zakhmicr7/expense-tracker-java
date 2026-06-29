package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;

    public ExpenseController(ExpenseService expenseService, UserService userService) {
        this.expenseService = expenseService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getAll(Principal principal) {
        long userId = userService.getUserIdByUsername(principal.getName());
        return ResponseEntity.ok(expenseService.getExpensesForUser(userId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Expense expense, Principal principal) {
        try {
            long userId = userService.getUserIdByUsername(principal.getName());
            return ResponseEntity.ok(expenseService.addExpense(expense, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        long userId = userService.getUserIdByUsername(principal.getName());
        return expenseService.deleteExpense(id, userId)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
