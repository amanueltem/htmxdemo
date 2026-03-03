package com.aman.htmxdemo.expense;

import com.aman.htmxdemo.common.BaseEntity;
import com.aman.htmxdemo.group.GroupMember;
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
@Table(name = "expense")
public class Expense extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private LocalDate date;
    private Double amount;
    private String timeSpan;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private GroupMember group;
}

enum TimeSpan {
    BREAKFAST,
    LUNCH,
    DINNER,
    GROCERIES,
    OTHER
}
