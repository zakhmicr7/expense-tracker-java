package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> getExpensesForUser(long userId) {
        return expenseRepository.findAllByUserId(userId);
    }

    public Expense addExpense(Expense expense, long userId) {
        if (expense.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (expense.getCategory() == null || expense.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (expense.getDate() == null || expense.getDate().isBlank()) {
            throw new IllegalArgumentException("Date is required");
        }
        return expenseRepository.saveForUser(expense, userId);
    }

    public boolean deleteExpense(long id, long userId) {
        return expenseRepository.deleteByIdAndUserId(id, userId);
    }
}
