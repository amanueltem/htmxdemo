package com.aman.htmxdemo.deposit;

import java.util.UUID;

public record DepositDisplay(
        UUID id,
        String date,
        String amountFormatted,
        String status,
        String inputter,
        String authorizer,
        boolean canEdit,
        boolean canDelete,
        boolean canAuthorize,
        boolean canRequestEdit,
        boolean canAcceptEditRequest
) {}