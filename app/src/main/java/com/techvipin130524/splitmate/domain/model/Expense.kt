package com.techvipin130524.splitmate.domain.model

enum class SplitType {
    EQUAL,
    EXACT,
    PERCENTAGE,
    SHARES
}

enum class ExpenseCategory(val displayName: String, val iconName: String) {
    FOOD("Food & Drinks", "restaurant"),
    TRANSPORT("Transport & Cabs", "directions_car"),
    HOTEL("Hotel & Stay", "hotel"),
    ENTERTAINMENT("Entertainment", "movie"),
    GROCERIES("Groceries", "shopping_cart"),
    RENT("Rent", "home"),
    BILLS("Bills & Utilities", "receipt"),
    SHOPPING("Shopping", "shopping_bag"),
    OTHER("Other", "more_horiz")
}

enum class RecurrenceFrequency(val displayName: String) {
    NONE("One-time"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

data class Expense(
    val id: String = "",
    val groupId: String = "",
    val description: String = "",
    val totalAmount: Long = 0L, // Amount in minor units (paise)
    val currency: String = "INR",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val recurrence: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val note: String = "",
    val receiptImageUrl: String? = null,
    val createdBy: String = "",
    val createdByName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val participants: List<ExpenseParticipant> = emptyList(),
    val payers: List<ExpensePayer> = emptyList()
)

data class ExpenseParticipant(
    val id: String = "",
    val expenseId: String = "",
    val userId: String = "",
    val userName: String = "",
    val splitType: SplitType = SplitType.EQUAL,
    val amount: Long = 0L, // In paise
    val percentage: Double = 0.0,
    val shares: Int = 1
)

data class ExpensePayer(
    val id: String = "",
    val expenseId: String = "",
    val userId: String = "",
    val userName: String = "",
    val amount: Long = 0L // In paise
)
