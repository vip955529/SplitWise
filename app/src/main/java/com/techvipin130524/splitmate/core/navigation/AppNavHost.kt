package com.techvipin130524.splitmate.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.techvipin130524.splitmate.core.designsystem.component.AppBottomBar
import com.techvipin130524.splitmate.feature.auth.presentation.AuthViewModel
import com.techvipin130524.splitmate.feature.auth.presentation.ForgotPasswordScreen
import com.techvipin130524.splitmate.feature.auth.presentation.LoginScreen
import com.techvipin130524.splitmate.feature.auth.presentation.RegisterScreen
import com.techvipin130524.splitmate.feature.auth.presentation.SessionState
import com.techvipin130524.splitmate.feature.auth.presentation.SplashScreen
import com.techvipin130524.splitmate.feature.expenses.AddExpenseScreen
import com.techvipin130524.splitmate.feature.expenses.ExpenseDetailScreen
import com.techvipin130524.splitmate.feature.friends.FriendDetailScreen
import com.techvipin130524.splitmate.feature.friends.FriendsScreen
import com.techvipin130524.splitmate.feature.groups.CreateGroupScreen
import com.techvipin130524.splitmate.feature.groups.GroupDetailScreen
import com.techvipin130524.splitmate.feature.groups.GroupsScreen
import com.techvipin130524.splitmate.feature.home.HomeScreen
import com.techvipin130524.splitmate.feature.profile.ProfileScreen
import com.techvipin130524.splitmate.feature.settlement.SettleUpDialog

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    var settleUpGroupId by remember { mutableStateOf<String?>(null) }

    val bottomBarRoutes = setOf(
        Routes.HOME,
        Routes.GROUPS,
        Routes.FRIENDS,
        Routes.PROFILE
    )

    LaunchedEffect(sessionState, currentRoute) {
        when (sessionState) {
            SessionState.Checking -> {
                // Stay on splash
            }
            SessionState.LoggedOut -> {
                val authRoutes = setOf(Routes.LOGIN, Routes.REGISTER, Routes.FORGOT_PASSWORD)
                if (currentRoute !in authRoutes) {
                    navController.navigate(Routes.LOGIN) {
                        currentRoute?.let { popUpTo(it) { inclusive = true } }
                        launchSingleTop = true
                    }
                }
            }
            is SessionState.LoggedIn -> {
                val authRoutes = setOf(Routes.SPLASH, Routes.LOGIN, Routes.REGISTER, Routes.FORGOT_PASSWORD)
                if (currentRoute in authRoutes) {
                    navController.navigate(Routes.HOME) {
                        currentRoute?.let { popUpTo(it) { inclusive = true } }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Routes.SPLASH) {
                SplashScreen()
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onRegisterClick = { navController.navigate(Routes.REGISTER) },
                    onForgotPasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD) }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onLoginClick = { navController.popBackStack() }
                )
            }

            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onBackToLogin = { navController.popBackStack() }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onAddExpenseClick = { navController.navigate(Routes.addExpense()) },
                    onCreateGroupClick = { navController.navigate(Routes.CREATE_GROUP) },
                    onGroupClick = { groupId -> navController.navigate(Routes.groupDetails(groupId)) }
                )
            }

            composable(Routes.GROUPS) {
                GroupsScreen(
                    onCreateGroupClick = { navController.navigate(Routes.CREATE_GROUP) },
                    onGroupClick = { groupId -> navController.navigate(Routes.groupDetails(groupId)) }
                )
            }

            composable(Routes.CREATE_GROUP) {
                CreateGroupScreen(
                    onBackClick = { navController.popBackStack() },
                    onGroupCreated = { groupId ->
                        navController.popBackStack()
                        navController.navigate(Routes.groupDetails(groupId))
                    }
                )
            }

            composable(
                route = Routes.GROUP_DETAILS,
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                GroupDetailScreen(
                    groupId = groupId,
                    onBackClick = { navController.popBackStack() },
                    onAddExpenseClick = { gId -> navController.navigate(Routes.addExpense(gId)) },
                    onSettleUpClick = { gId -> settleUpGroupId = gId }
                )
            }

            composable(Routes.FRIENDS) {
                FriendsScreen(
                    onFriendClick = { friendId -> navController.navigate(Routes.friendDetails(friendId)) }
                )
            }

            composable(
                route = Routes.FRIEND_DETAILS,
                arguments = listOf(navArgument("friendId") { type = NavType.StringType })
            ) { backStackEntry ->
                val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
                FriendDetailScreen(
                    friendId = friendId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogoutClick = {
                        authViewModel.logout()
                    }
                )
            }

            composable(
                route = Routes.ADD_EXPENSE,
                arguments = listOf(navArgument("groupId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")
                AddExpenseScreen(
                    initialGroupId = groupId,
                    onBackClick = { navController.popBackStack() },
                    onExpenseSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.EXPENSE_DETAILS,
                arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
                ExpenseDetailScreen(
                    expenseId = expenseId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }

    settleUpGroupId?.let { groupId ->
        SettleUpDialog(
            groupId = groupId,
            onDismiss = { settleUpGroupId = null },
            onSettled = { settleUpGroupId = null }
        )
    }
}
