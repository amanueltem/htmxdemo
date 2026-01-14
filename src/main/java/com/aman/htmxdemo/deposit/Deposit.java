package com.aman.htmxdemo.deposit;

import com.aman.htmxdemo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "deposit")
public class Deposit  extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private LocalDate date;
    private Double amount;
}
