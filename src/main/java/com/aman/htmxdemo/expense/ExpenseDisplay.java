package com.aman.htmxdemo.expense;

import java.util.UUID;

public record ExpenseDisplay(
        UUID id,
        String date,
        String timeSpan,
        String amountFormatted,
        String status,
        String inputter,
        boolean isAuthorized,
        boolean canEdit,
        boolean canDelete,
        boolean canAuthorize,
        boolean canRequestEdit,
        boolean canAcceptEditRequest
) {}