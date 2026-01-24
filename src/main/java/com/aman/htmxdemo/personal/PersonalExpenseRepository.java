package com.aman.htmxdemo.personal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersonalExpenseRepository extends JpaRepository<PersonalExpense, UUID> {
    Page<PersonalExpense> findByInputter(String inputter, Pageable pageable);
}
