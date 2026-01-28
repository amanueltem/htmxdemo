package com.aman.htmxdemo.personal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "personal_expense")
public class PersonalExpense{
    @Id
    @GeneratedValue
    private UUID id;
    private LocalDate date;
    private Double amount;
    private String timeSpan;
    private String inputter;
}
enum TimeSpan {
    FOOD,
    DRINK,
    SHOWER,
    TAXI,
    FAMILY_SUPPORT,
    HAIRCUT,
    SHOE_CLEANING,
    BUY_CLOTHES,
    BUY_SHOES,
    OTHER
}