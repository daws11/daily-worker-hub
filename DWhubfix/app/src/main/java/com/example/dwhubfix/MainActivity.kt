package com.example.dwhubfix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dwhubfix.ui.SplashScreen
import com.example.dwhubfix.ui.RoleSelectionScreen
import com.example.dwhubfix.ui.WorkerBasicProfileScreen
import com.example.dwhubfix.ui.WorkerDocumentUploadScreen
import com.example.dwhubfix.ui.WorkerFaceVerificationScreen
import com.example.dwhubfix.ui.WorkerAddressVerificationScreen
import com.example.dwhubfix.ui.WorkerSkillSelectionScreen
import com.example.dwhubfix.ui.WorkerExperienceLevelScreen
import com.example.dwhubfix.ui.WorkerPortfolioUploadScreen
import com.example.dwhubfix.ui.WorkerVerificationPendingScreen
// import com.example.dwhubfix.ui.WorkerOtpScreen // Removed
import com.example.dwhubfix.ui.LoginScreen
import com.example.dwhubfix.ui.WorkerRegistrationScreen
import com.example.dwhubfix.ui.WorkerWelcomeScreen
import com.example.dwhubfix.ui.BusinessWelcomeScreen
import com.example.dwhubfix.ui.BusinessRegistrationScreen
// import com.example.dwhubfix.ui.BusinessOtpScreen // Removed
import com.example.dwhubfix.ui.BusinessBasicProfileScreen
import com.example.dwhubfix.ui.BusinessDocumentUploadScreen
import com.example.dwhubfix.ui.BusinessLocationVerificationScreen
import com.example.dwhubfix.ui.BusinessDetailsScreen
import com.example.dwhubfix.ui.BusinessWorkerPreferenceScreen
import com.example.dwhubfix.ui.BusinessFinalReviewScreen
import com.example.dwhubfix.ui.BusinessVerificationPendingScreen
import com.example.dwhubfix.ui.theme.DailyWorkerHubTheme
import com.example.dwhubfix.data.SessionManager
import com.example.dwhubfix.data.SupabaseRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyWorkerHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Deep Link Handling
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val activity = context as? android.app.Activity
                    val intent = activity?.intent
                    
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        if (intent?.action == android.content.Intent.ACTION_VIEW && intent.data?.scheme == "dwhub") {
                            val data = intent.data
                            var accessToken: String? = null
                            var refreshToken: String? = null
                            
                            // 1. Check Fragment (Hash) for Implicit Flow tokens (standard Supabase)
                            // Format: #access_token=...&refresh_token=...&provider_token=...
                            val fragment = data?.fragment
                            if (!fragment.isNullOrEmpty()) {
                                // Use explicit limit=2 to avoid splitting on '=' inside the token itself (if any)
                                val params = fragment.split("&").mapNotNull { param ->
                                    val parts = param.split("=", limit = 2)
                                    if (parts.size == 2) {
                                        val key = parts[0]
                                        val value = try {
                                            java.net.URLDecoder.decode(parts[1], "UTF-8")
                                        } catch (e: Exception) { parts[1] } // Fallback if decode fails
                                        key to value
                                    } else {
                                        null
                                    }
                                }.toMap()
                                
                                accessToken = params["access_token"]
                                refreshToken = params["refresh_token"]
                            }
                            
                            // 2. Fallback: Check Query Params (e.g. PKCE 'code' or legacy 'token')
                            if (accessToken.isNullOrEmpty()) {
                                // Magic link sometimes sends 'token' as query param
                                val qToken = data?.getQueryParameter("token")
                                if (!qToken.isNullOrEmpty()) {
                                     accessToken = qToken
                                }
                                
                                // PKCE flow sends 'code'. We aren't fully handling PKCE exchange here yet,
                                // but if we were, we'd capture it.
                                // val code = data?.getQueryParameter("code")
                            }

                            if (!accessToken.isNullOrEmpty() || !refreshToken.isNullOrEmpty()) {
                                val route = "verification_processing?accessToken=$accessToken&refreshToken=$refreshToken"
                                navController.navigate(route)
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = "splash") {
                        composable(
                            route = "verification_processing?accessToken={accessToken}&refreshToken={refreshToken}",
                            arguments = listOf(
                                androidx.navigation.navArgument("accessToken") { 
                                    type = androidx.navigation.NavType.StringType 
                                    nullable = true
                                    defaultValue = null
                                },
                                androidx.navigation.navArgument("refreshToken") { 
                                    type = androidx.navigation.NavType.StringType 
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val accessToken = backStackEntry.arguments?.getString("accessToken")
                            val refreshToken = backStackEntry.arguments?.getString("refreshToken")
                            
                            com.example.dwhubfix.ui.VerificationProcessingScreen(
                                accessToken = accessToken,
                                refreshToken = refreshToken,
                                onVerificationSuccess = { role ->
                                    if (role == "worker") {
                                        SessionManager.saveSelectedRole(context, "worker")
                                        SessionManager.saveCurrentStep(context, "worker_basic_profile")
                                        navController.navigate("worker_basic_profile") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    } else if (role == "business") {
                                        SessionManager.saveSelectedRole(context, "business")
                                        SessionManager.saveCurrentStep(context, "business_basic_profile")
                                        navController.navigate("business_basic_profile") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("role_selection")
                                    }
                                },
                                onVerificationFailed = { error ->
                                    // Navigate back to safe state on error
                                    navController.navigate("role_selection")
                                }
                            )
                        }

                        composable("splash") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val scope = androidx.compose.runtime.rememberCoroutineScope()

                            SplashScreen(
                                onSplashFinished = {
                                    scope.launch {
                                        val token = SessionManager.getAccessToken(context)
                                        val savedStep = SessionManager.getCurrentStep(context)
                                        val savedRole = SessionManager.getSelectedRole(context)

                                        // Use local saved values for navigation
                                        if (token != null) {
                                            navController.navigate(savedStep ?: "role_selection") {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        } else {
                                            // No token: user might be in pre-login onboarding
                                            if (savedStep != null) {
                                                navController.navigate(savedStep) {
                                                    popUpTo("splash") { inclusive = true }
                                                }
                                            } else if (savedRole != null) {
                                                val start = if (savedRole == "worker") "worker_welcome" else "business_welcome"
                                                navController.navigate(start) {
                                                    popUpTo("splash") { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate("role_selection") {
                                                    popUpTo("splash") { inclusive = true }
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        
                        composable("role_selection") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            RoleSelectionScreen(
                                onWorkerSelected = {
                                    SessionManager.saveSelectedRole(context, "worker")
                                    SessionManager.saveCurrentStep(context, "worker_welcome")
                                    navController.navigate("worker_welcome") {
                                        popUpTo("role_selection") { inclusive = true }
                                    }
                                },
                                onBusinessSelected = {
                                    SessionManager.saveSelectedRole(context, "business")
                                    SessionManager.saveCurrentStep(context, "business_welcome")
                                    navController.navigate("business_welcome") {
                                        popUpTo("role_selection") { inclusive = true }
                                    }
                                },
                                onLoginClick = {
                                    navController.navigate("login")
                                }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onLoginSuccess = { role ->
                                    navController.navigate("splash") {
                                        popUpTo("role_selection") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("worker_welcome") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            WorkerWelcomeScreen(
                                onNavigateToRegistration = {
                                    SessionManager.saveCurrentStep(context, "worker_registration")
                                    navController.navigate("worker_registration")
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                },
                                onNavigateBack = {
                                    // Exit app if trying to go back from welcome after role selection
                                    activity?.finish()
                                }
                            )
                        }
                        
                        composable("business_welcome") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            BusinessWelcomeScreen(
                                onNavigateToRegistration = {
                                    SessionManager.saveCurrentStep(context, "business_registration")
                                    navController.navigate("business_registration")
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                },
                                onNavigateBack = {
                                    activity?.finish()
                                }
                            )
                        }

                        composable("business_registration") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            BusinessRegistrationScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateNext = { email ->
                                    // Signed up successfully
                                    SessionManager.saveCurrentStep(context, "business_basic_profile")
                                    navController.navigate("business_basic_profile")
                                }
                            )
                        }

                        // business_otp route removed

                        composable("business_basic_profile") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            BusinessBasicProfileScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateNext = {
                                    SessionManager.saveCurrentStep(context, "business_document_upload")
                                    navController.navigate("business_document_upload")
                                }
                            )
                        }

                        composable("business_document_upload") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            BusinessDocumentUploadScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateNext = {
                                    SessionManager.saveCurrentStep(context, "business_location_verification")
                                    navController.navigate("business_location_verification")
                                }
                            )
                        }

                        composable("business_location_verification") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            BusinessLocationVerificationScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onVerificationSuccess = {
                                    SessionManager.saveCurrentStep(context, "business_details")
                                    navController.navigate("business_details")
                                }
                            )
                        }

                        composable("business_details") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            BusinessDetailsScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateNext = {
                                    SessionManager.saveCurrentStep(context, "business_worker_preference")
                                    navController.navigate("business_worker_preference")
                                }
                            )
                        }

                        composable("business_worker_preference") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            BusinessWorkerPreferenceScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateNext = {
                                    SessionManager.saveCurrentStep(context, "business_final_review")
                                    navController.navigate("business_final_review")
                                }
                            )
                        }

                        composable("business_final_review") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            BusinessFinalReviewScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onSubmit = {
                                    SessionManager.saveCurrentStep(context, "business_verification_pending")
                                    navController.navigate("business_verification_pending")
                                }
                            )
                        }

                        composable("business_verification_pending") {
                            BusinessVerificationPendingScreen(
                                onNavigateHome = {
                                    navController.navigate("business_dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("business_dashboard") {
                            com.example.dwhubfix.ui.dashboard.BusinessDashboardNavigation(
                                onNavigateBack = {
                                    navController.navigate("role_selection") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("worker_registration") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            WorkerRegistrationScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateNext = { email ->
                                    // Signed up successfully
                                    SessionManager.saveCurrentStep(context, "worker_basic_profile")
                                    navController.navigate("worker_basic_profile")
                                }
                            )
                        }

                        // worker_otp route removed

                        composable("worker_basic_profile") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            WorkerBasicProfileScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateNext = {
                                    SessionManager.saveCurrentStep(context, "worker_document_upload")
                                    navController.navigate("worker_document_upload")
                                }
                            )
                        }

                        composable("worker_document_upload") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            WorkerDocumentUploadScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateNext = {
                                    SessionManager.saveCurrentStep(context, "worker_face_verification")
                                    navController.navigate("worker_face_verification")
                                }
                            )
                        }

                        composable("worker_face_verification") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            WorkerFaceVerificationScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onVerificationSuccess = {
                                    SessionManager.saveCurrentStep(context, "worker_address_verification")
                                    navController.navigate("worker_address_verification")
                                }
                            )
                        }

                        composable("worker_address_verification") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            WorkerAddressVerificationScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onVerificationSuccess = {
                                    SessionManager.saveCurrentStep(context, "worker_skill_selection")
                                    navController.navigate("worker_skill_selection")
                                }
                            )
                        }

                        composable("worker_skill_selection") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            WorkerSkillSelectionScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateNext = {
                                    SessionManager.saveCurrentStep(context, "worker_experience_level")
                                    navController.navigate("worker_experience_level")
                                }
                            )
                        }

                        composable("worker_experience_level") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            WorkerExperienceLevelScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateNext = {
                                    SessionManager.saveCurrentStep(context, "worker_portfolio_upload")
                                    navController.navigate("worker_portfolio_upload")
                                }
                            )
                        }

                        composable("worker_portfolio_upload") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            WorkerPortfolioUploadScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateNext = {
                                    SessionManager.saveCurrentStep(context, "worker_verification_pending")
                                    navController.navigate("worker_verification_pending")
                                }
                            )
                        }

                        composable("worker_verification_pending") {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            WorkerVerificationPendingScreen(
                                onNavigateHome = {
                                    navController.navigate("worker_dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("worker_dashboard") {
                            com.example.dwhubfix.ui.dashboard.WorkerDashboardNavigation(
                                onNavigateBack = {
                                    // Handle logout or exit
                                    navController.navigate("role_selection") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
