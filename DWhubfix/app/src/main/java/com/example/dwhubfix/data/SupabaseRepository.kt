package com.example.dwhubfix.data

import android.content.Context
import android.net.Uri
import com.example.dwhubfix.BuildConfig
import com.example.dwhubfix.model.Booking
import com.example.dwhubfix.model.WorkerStats
import com.example.dwhubfix.model.Job
import com.example.dwhubfix.model.BusinessStats
import com.example.dwhubfix.model.JobApplication
import com.example.dwhubfix.model.UserProfile
import com.example.dwhubfix.model.EarningsSummary
import com.example.dwhubfix.model.Transaction
import com.example.dwhubfix.model.toBusinessStats
import com.example.dwhubfix.model.toJob
import com.example.dwhubfix.model.toJobList
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.time.LocalDate
import java.util.UUID

object SupabaseRepository {

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    // ========================================
    // AUTH METHODS
    // ========================================

    suspend fun login(context: Context, email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val sessionInfo = client.auth.sessionManager.loadSession()
            val userId = sessionInfo?.user?.id ?: ""
            val accessToken = sessionInfo?.accessToken ?: ""
            SessionManager.saveSession(context, accessToken, userId)

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWorker(context: Context, fullName: String, email: String, password: String, phoneNumber: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val sessionInfo = client.auth.sessionManager.loadSession()
            val userId = sessionInfo?.user?.id ?: ""
            val accessToken = sessionInfo?.accessToken ?: ""
            SessionManager.saveSession(context, accessToken, userId)
            SessionManager.saveSelectedRole(context, "worker")

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerBusiness(context: Context, businessName: String, email: String, password: String, phoneNumber: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val sessionInfo = client.auth.sessionManager.loadSession()
            val userId = sessionInfo?.user?.id ?: ""
            val accessToken = sessionInfo?.accessToken ?: ""
            SessionManager.saveSession(context, accessToken, userId)
            SessionManager.saveSelectedRole(context, "business")

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.auth.signOut()
            SessionManager.clearSession(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // PROFILE METHODS
    // ========================================

    suspend fun getProfile(context: Context): Result<Map<String, Any?>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val response = client.from("profiles")
                .select()
                .decodeSingle<Map<String, Any?>>()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user profile as UserProfile object
     */
    suspend fun getUserProfile(context: Context): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val response = client.from("profiles")
                .select()
                .decodeSingle<Map<String, Any?>>()

            val profile = UserProfile(
                id = response["id"] as? String ?: "",
                fullName = response["full_name"] as? String,
                email = response["email"] as? String,
                phoneNumber = response["phone_number"] as? String,
                avatarUrl = response["avatar_url"] as? String,
                role = response["role"] as? String,
                createdAt = response["created_at"] as? String,
                updatedAt = response["updated_at"] as? String
            )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWorkerProfile(context: Context, profile: Map<String, Any?>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            client.from("worker_profiles").update(profile)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBusinessProfile(context: Context, profile: Map<String, Any?>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            client.from("business_profiles").update(profile)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // JOB METHODS
    // ========================================

    suspend fun getJobs(context: Context): Result<List<Job>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val response = client.from("jobs")
                .select()
                .decodeList<Map<String, Any?>>()

            Result.success(response.toJobList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableJobs(context: Context): Result<List<Job>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val response = client.from("jobs").select() {
                filter {
                    eq("status", "open")
                }
            }.decodeList<Map<String, Any?>>()

            Result.success(response.toJobList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun postJob(context: Context, jobData: Map<String, Any?>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val response = client.from("jobs").insert(jobData) {
                select()
            }.decodeSingle<Map<String, Any?>>()

            val jobId = response["id"] as? String ?: ""
            Result.success(jobId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJobById(context: Context, jobId: String): Result<Job> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val response = client.from("jobs").select() {
                filter { eq("id", jobId) }
            }.decodeSingle<Map<String, Any?>>()

            Result.success(response.toJob())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptJob(context: Context, jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")

            val applicationData = mapOf(
                "job_id" to jobId,
                "worker_id" to userId,
                "status" to "accepted"
            )

            client.from("job_applications").insert(applicationData)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteJob(context: Context, jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            client.from("jobs").delete {
                filter { eq("id", jobId) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // JOB APPLICATION METHODS
    // ========================================

    suspend fun applyForJob(context: Context, jobId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val jobResult = getJobById(context, jobId)
            if (jobResult.isFailure) throw Exception("Job not found")

            val applicationData = mapOf(
                "job_id" to jobId,
                "status" to "pending"
            )

            val response = client.from("job_applications").insert(applicationData) {
                select()
            }.decodeSingle<Map<String, Any?>>()

            val applicationId = response["id"] as? String ?: ""
            Result.success(applicationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkerJobs(context: Context): Result<List<Map<String, Any?>>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val response = client.from("job_applications").select()
                .decodeList<Map<String, Any?>>()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get worker's job applications with optional status filtering
     * Used by MyJobsScreen
     */
    suspend fun getMyJobs(context: Context, vararg statuses: String): Result<List<JobApplication>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")

            val response = if (statuses.isNotEmpty()) {
                client.from("job_applications").select() {
                    filter {
                        eq("worker_id", userId)
                        isIn("status", statuses.toList())
                    }
                }.decodeList<Map<String, Any?>>()
            } else {
                client.from("job_applications").select() {
                    filter {
                        eq("worker_id", userId)
                    }
                }.decodeList<Map<String, Any?>>()
            }

            // Convert to JobApplication objects
            val applications = response.map { appMap ->
                JobApplication(
                    id = appMap["id"] as? String ?: "",
                    status = appMap["status"] as? String ?: "pending",
                    jobId = appMap["job_id"] as? String ?: "",
                    workerId = appMap["worker_id"] as? String ?: "",
                    createdAt = appMap["created_at"] as? String,
                    updatedAt = appMap["updated_at"] as? String,
                    job = null // Job details would need to be fetched separately
                )
            }

            Result.success(applications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // BUSINESS STATS & JOBS METHODS
    // ========================================

    suspend fun getBusinessStats(context: Context): Result<BusinessStats> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val today = LocalDate.now().toString()

            // Get active shifts today
            val todayJobs = client.from("jobs").select() {
                filter {
                    gte("shift_date", today)
                }
            }.decodeList<Map<String, Any?>>()

            val activeShiftsToday = todayJobs.size

            // Get pending applications
            val pendingApplications = client.from("job_applications").select() {
                filter {
                    eq("status", "pending")
                }
            }.decodeList<Map<String, Any?>>()

            val pendingPatches = pendingApplications.size

            val stats = mapOf(
                "active_shifts_today" to activeShiftsToday,
                "pending_patches" to pendingPatches,
                "workers_hired_this_week" to 0,
                "total_spending_this_month" to 0.0,
                "wallet_balance" to 0.0
            )

            Result.success(stats.toBusinessStats())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBusinessJobs(context: Context): Result<List<Job>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val response = client.from("jobs").select()
                .decodeList<Map<String, Any?>>()

            Result.success(response.toJobList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // WALLET & TRANSACTION METHODS
    // ========================================

    suspend fun getWalletBalance(context: Context): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val response = client.from("wallets")
                .select()
                .decodeSingle<Map<String, Any?>>()

            val balance = (response["balance"] as? Number)?.toDouble() ?: 0.0
            Result.success(balance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // IMAGE UPLOAD METHODS
    // ========================================

    suspend fun uploadImage(context: Context, imageUri: Uri, folder: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")

            val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw Exception("Unable to open image")
            val bytes = inputStream.readBytes()

            val fileName = "${UUID.randomUUID()}.jpg"

            client.storage.from(folder).upload(fileName, bytes)

            val publicUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/${folder}/${fileName}"

            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // LEGACY FUNCTION ALIASES (for backward compatibility)
    // ========================================

    /**
     * Legacy alias for signUpWith(Email)
     * Simply wraps the existing registerWorker functionality
     */
    suspend fun signUpWithEmail(context: Context, email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val sessionInfo = client.auth.sessionManager.loadSession()
            val userId = sessionInfo?.user?.id ?: ""
            val accessToken = sessionInfo?.accessToken ?: ""
            SessionManager.saveSession(context, accessToken, userId)

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Legacy alias for login
     * Simply wraps the existing login functionality
     */
    suspend fun signInWithEmail(context: Context, email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val sessionInfo = client.auth.sessionManager.loadSession()
            val userId = sessionInfo?.user?.id ?: ""
            val accessToken = sessionInfo?.accessToken ?: ""
            SessionManager.saveSession(context, accessToken, userId)

            // Get role from profiles or return default
            val role = try {
                val profile = client.from("profiles").select() {
                    filter { eq("id", userId) }
                }.decodeSingle<Map<String, Any?>>()
                profile["role"] as? String ?: "worker"
            } catch (e: Exception) {
                "worker"
            }

            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Legacy alias for uploadImage
     */
    suspend fun uploadFile(context: Context, imageUri: Uri, folder: String): Result<String> {
        return uploadImage(context, imageUri, folder)
    }

    /**
     * Legacy alias for getProfile
     * Returns profile as Map (compatible with JSONObject-like usage)
     */
    suspend fun getProfileJson(context: Context): Result<Map<String, Any?>> = withContext(Dispatchers.IO) {
        try {
            val profileResult = getProfile(context)
            if (profileResult.isSuccess) {
                Result.success(profileResult.getOrNull() ?: emptyMap())
            } else {
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Failed to get profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Legacy updateProfile function
     * Routes to updateWorkerProfile or updateBusinessProfile based on role
     */
    suspend fun updateProfile(
        context: Context,
        fullName: String? = null,
        role: String? = null,
        avatarUrl: String? = null,
        jobCategory: String? = null,
        jobRole: String? = null,
        currentStep: String? = null,
        phoneNumber: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getAccessToken(context) ?: throw Exception("Not authenticated")
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")

            // Determine role based on parameter or session
            val userRole = role ?: SessionManager.getSelectedRole(context) ?: "worker"

            val profileData = mutableMapOf<String, Any?>()
            fullName?.let { profileData["full_name"] = it }
            avatarUrl?.let { profileData["avatar_url"] = it }
            jobCategory?.let { profileData["job_category"] = it }
            jobRole?.let { profileData["job_role"] = it }
            currentStep?.let { profileData["current_step"] = it }
            phoneNumber?.let { profileData["phone_number"] = it }

            if (userRole == "worker") {
                client.from("worker_profiles").update(profileData) {
                    filter { eq("user_id", userId) }
                }
            } else {
                client.from("business_profiles").update(profileData) {
                    filter { eq("user_id", userId) }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Handle auth redirect for email verification
     */
    suspend fun handleAuthRedirect(
        context: Context,
        accessToken: String?,
        refreshToken: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!accessToken.isNullOrEmpty()) {
                // Set the session from the redirect
                SessionManager.saveSession(context, accessToken, "")
                Result.success(Unit)
            } else {
                Result.failure(Exception("No access token provided"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // WORKER STATS & BOOKINGS METHODS
    // ========================================

    /**
     * Get worker statistics
     */
    suspend fun getWorkerStats(userId: String? = null): WorkerStats = withContext(Dispatchers.IO) {
        try {
            // Return default stats for now - this would be fetched from the database
            WorkerStats(
                totalShiftsCompleted = 0,
                totalEarnings = 0L,
                walletBalance = 0L,
                frozenAmount = 0L,
                ratingAvg = 0.0,
                ratingCount = 0,
                reliabilityScore = 100.0,
                tier = "bronze"
            )
        } catch (e: Exception) {
            WorkerStats()
        }
    }

    /**
     * Get worker bookings
     */
    suspend fun getWorkerBookings(
        userId: String,
        limit: Int = 10
    ): List<Booking> = withContext(Dispatchers.IO) {
        try {
            // Return empty list for now - this would be fetched from the database
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get worker earnings summary
     */
    suspend fun getEarnings(context: Context): Result<EarningsSummary> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")

            // Return default earnings for now - this would be fetched from the database
            val earnings = EarningsSummary(
                totalEarnings = 0,
                pendingEarnings = 0,
                availableBalance = 0,
                totalJobs = 0,
                totalCommission = 0,
                transactions = emptyList()
            )
            Result.success(earnings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // WORKER ONBOARDING FUNCTIONS
    // ========================================

    /**
     * Update worker address
     */
    suspend fun updateWorkerAddress(
        context: Context,
        fullAddress: String? = null,
        province: String? = null,
        city: String? = null,
        district: String? = null,
        postalCode: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        address: String? = null,
        photoUrl: String? = null,
        documentUrl: String? = null,
        currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val profileData = mutableMapOf<String, Any?>()

            fullAddress?.let { profileData["full_address"] = it }
            province?.let { profileData["province"] = it }
            city?.let { profileData["city"] = it }
            district?.let { profileData["district"] = it }
            postalCode?.let { profileData["postal_code"] = it }
            latitude?.let { profileData["latitude"] = it }
            longitude?.let { profileData["longitude"] = it }
            address?.let { profileData["address"] = it }
            photoUrl?.let { profileData["address_photo_url"] = it }
            documentUrl?.let { profileData["address_document_url"] = it }
            currentStep?.let { profileData["current_step"] = it }

            client.from("worker_profiles").update(profileData) {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update profile document (ID card, selfie, etc.)
     */
    suspend fun updateProfileDocument(
        context: Context,
        documentType: String,
        documentUrl: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val profileData = mapOf(documentType to documentUrl)
            client.from("worker_profiles").update(profileData) {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update worker experience level
     */
    suspend fun updateWorkerExperience(
        context: Context,
        experienceYears: Int,
        workHistory: List<String>,
        documentUrl: String?,
        currentStep: String? = null,
        experienceLevel: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val profileData = mutableMapOf<String, Any?>(
                "experience_years" to experienceYears,
                "work_history" to workHistory
            )
            documentUrl?.let { profileData["experience_document_url"] = it }
            currentStep?.let { profileData["current_step"] = it }
            experienceLevel?.let { profileData["experience_level"] = it }

            client.from("worker_profiles").update(profileData) {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update worker skills
     */
    suspend fun updateWorkerSkills(
        context: Context,
        skills: List<String>,
        skillExperienceLevels: Map<String, String> = emptyMap(),
        currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val profileData = mutableMapOf<String, Any?>(
                "skills" to skills
            )
            currentStep?.let { profileData["current_step"] = it }
            client.from("worker_profiles").update(profileData) {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update worker portfolio
     */
    suspend fun updateWorkerPortfolio(
        context: Context,
        portfolioUrls: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val profileData = mapOf("portfolio_urls" to portfolioUrls)
            client.from("worker_profiles").update(profileData) {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload verification image (selfie, documents)
     */
    suspend fun uploadVerificationImage(
        context: Context,
        imageUri: android.net.Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            uploadImage(context, imageUri, "verification_documents")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // BUSINESS ONBOARDING FUNCTIONS
    // ========================================

    /**
     * Update business basic profile
     */
    suspend fun updateBusinessBasicProfile(
        context: Context,
        businessName: String,
        category: String? = null,
        logoUrl: String? = null,
        currentStep: String? = null,
        businessType: String? = null,
        contactPerson: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val profileData = mutableMapOf<String, Any?>(
                "business_name" to businessName
            )
            category?.let { profileData["category"] = it }
            logoUrl?.let { profileData["logo_url"] = it }
            currentStep?.let { profileData["current_step"] = it }
            businessType?.let { profileData["business_type"] = it }
            contactPerson?.let { profileData["contact_person"] = it }

            client.from("business_profiles").update(profileData) {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update business documents
     */
    suspend fun updateBusinessDocuments(
        context: Context,
        documentType: String? = null,
        documentUrl: String? = null,
        documentsList: List<String>? = null,
        nibUrl: String? = null,
        locationFrontUrl: String? = null,
        locationInsideUrl: String? = null,
        currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val profileData = mutableMapOf<String, Any?>()

            documentType?.let { if (documentUrl != null) profileData[documentType] = documentUrl }
            documentsList?.let { profileData["documents"] = it }
            nibUrl?.let { profileData["nib_url"] = it }
            locationFrontUrl?.let { profileData["location_front_url"] = it }
            locationInsideUrl?.let { profileData["location_inside_url"] = it }
            currentStep?.let { profileData["current_step"] = it }

            client.from("business_profiles").update(profileData) {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update business location
     */
    suspend fun updateBusinessLocation(
        context: Context,
        address: String,
        latitude: Double? = null,
        longitude: Double? = null,
        photoUrl: String? = null,
        currentStep: String? = null,
        province: String? = null,
        city: String? = null,
        district: String? = null,
        postalCode: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val profileData = mutableMapOf<String, Any?>(
                "address" to address
            )
            latitude?.let { profileData["latitude"] = it }
            longitude?.let { profileData["longitude"] = it }
            photoUrl?.let { profileData["location_photo_url"] = it }
            currentStep?.let { profileData["current_step"] = it }
            province?.let { profileData["province"] = it }
            city?.let { profileData["city"] = it }
            district?.let { profileData["district"] = it }
            postalCode?.let { profileData["postal_code"] = it }

            client.from("business_profiles").update(profileData) {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update business details
     */
    suspend fun updateBusinessDetails(
        context: Context,
        openTime: String? = null,
        closeTime: String? = null,
        description: String? = null,
        facilities: List<String>? = null,
        currentStep: String? = null,
        operatingHours: Map<String, String>? = null,
        website: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val profileData = mutableMapOf<String, Any?>()

            openTime?.let { profileData["open_time"] = it }
            closeTime?.let { profileData["close_time"] = it }
            description?.let { profileData["description"] = it }
            facilities?.let { profileData["facilities"] = it }
            currentStep?.let { profileData["current_step"] = it }
            operatingHours?.let { profileData["operating_hours"] = it }
            website?.let { profileData["website"] = it }

            client.from("business_profiles").update(profileData) {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update business preferences
     */
    suspend fun updateBusinessPreferences(
        context: Context,
        preferredWorkerCategories: List<String>? = null,
        minimumRating: Double? = null,
        autoAcceptEnabled: Boolean? = null,
        selectedSkills: List<String>? = null,
        experienceLevel: String? = null,
        languages: List<String>? = null,
        priorityHiring: Boolean? = null,
        currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val profileData = mutableMapOf<String, Any?>()

            preferredWorkerCategories?.let { profileData["preferred_worker_categories"] = it }
            minimumRating?.let { profileData["minimum_rating"] = it }
            autoAcceptEnabled?.let { profileData["auto_accept_enabled"] = it }
            selectedSkills?.let { profileData["selected_skills"] = it }
            experienceLevel?.let { profileData["experience_level"] = it }
            languages?.let { profileData["languages"] = it }
            priorityHiring?.let { profileData["priority_hiring"] = it }
            currentStep?.let { profileData["current_step"] = it }

            client.from("business_profiles").update(profileData) {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Complete business registration
     */
    suspend fun completeBusinessRegistration(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val profileData = mapOf("registration_complete" to true, "status" to "active")
            client.from("business_profiles").update(profileData) {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // NOTIFICATION PREFERENCES METHODS
    // ========================================

    /**
     * Notification preferences data class
     */
    data class NotificationPreferences(
        val pushEnabled: Boolean = true,
        val jobAlertsEnabled: Boolean = true,
        val applicationUpdatesEnabled: Boolean = true,
        val promotionalEnabled: Boolean = false,
        val alertDistance: String = "10 km",
        val alertCategories: List<String> = listOf("Semua")
    )

    /**
     * Update notification preferences
     * Used by NotificationSettingsScreen
     */
    suspend fun updateNotificationPreferences(
        context: Context,
        pushEnabled: Boolean,
        jobAlertsEnabled: Boolean,
        applicationUpdatesEnabled: Boolean,
        promotionalEnabled: Boolean,
        alertDistance: String,
        alertCategories: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val role = SessionManager.getSelectedRole(context) ?: "worker"

            val prefsData = mapOf(
                "notification_preferences" to mapOf(
                    "push_enabled" to pushEnabled,
                    "job_alerts_enabled" to jobAlertsEnabled,
                    "application_updates_enabled" to applicationUpdatesEnabled,
                    "promotional_enabled" to promotionalEnabled,
                    "alert_distance" to alertDistance,
                    "alert_categories" to alertCategories
                )
            )

            if (role == "worker") {
                client.from("worker_profiles").update(prefsData) {
                    filter { eq("user_id", userId) }
                }
            } else {
                client.from("business_profiles").update(prefsData) {
                    filter { eq("user_id", userId) }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get notification preferences from profile
     */
    suspend fun getNotificationPreferences(context: Context): Result<NotificationPreferences> = withContext(Dispatchers.IO) {
        try {
            val profileResult = getProfile(context)
            if (profileResult.isSuccess) {
                val profile = profileResult.getOrNull() ?: emptyMap()
                val prefsMap = profile["notification_preferences"] as? Map<String, Any?>

                val prefs = if (prefsMap != null) {
                    NotificationPreferences(
                        pushEnabled = prefsMap["push_enabled"] as? Boolean ?: true,
                        jobAlertsEnabled = prefsMap["job_alerts_enabled"] as? Boolean ?: true,
                        applicationUpdatesEnabled = prefsMap["application_updates_enabled"] as? Boolean ?: true,
                        promotionalEnabled = prefsMap["promotional_enabled"] as? Boolean ?: false,
                        alertDistance = prefsMap["alert_distance"] as? String ?: "10 km",
                        alertCategories = prefsMap["alert_categories"] as? List<String> ?: listOf("Semua")
                    )
                } else {
                    NotificationPreferences()
                }

                Result.success(prefs)
            } else {
                Result.success(NotificationPreferences()) // Return defaults on failure
            }
        } catch (e: Exception) {
            Result.success(NotificationPreferences()) // Return defaults on error
        }
    }
}
