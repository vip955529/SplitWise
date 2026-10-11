package com.techvipin130524.splitmate.core.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    const val HOME = "home"

    const val GROUPS = "groups"
    const val CREATE_GROUP = "create_group"
    const val GROUP_DETAILS = "group_details/{groupId}"
    fun groupDetails(groupId: String) = "group_details/$groupId"

    const val FRIENDS = "friends"
    const val FRIEND_DETAILS = "friend_details/{friendId}"
    fun friendDetails(friendId: String) = "friend_details/$friendId"

    const val PROFILE = "profile"

    const val ADD_EXPENSE = "add_expense?groupId={groupId}"
    fun addExpense(groupId: String? = null) = if (groupId != null) "add_expense?groupId=$groupId" else "add_expense"

    const val EXPENSE_DETAILS = "expense_details/{expenseId}"
    fun expenseDetails(expenseId: String) = "expense_details/$expenseId"
}
