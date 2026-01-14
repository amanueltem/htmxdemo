package com.aman.htmxdemo.deposit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DepositRepository  extends JpaRepository<Deposit, UUID> {
}
