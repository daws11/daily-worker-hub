package com.example.dwhubfix.data

import android.content.Context
import android.net.Uri
import com.example.dwhubfix.BuildConfig
import com.example.dwhubfix.model.*
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.StorageFile
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import org.json.JSONObject
import org.json.JSONException
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import io.github.jan.supabase.auth.user.UserSession

object SupabaseRepository {

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Functions) {
            serializer = KotlinXSerializer(Json { 
                ignoreUnknownKeys = true 
                encodeDefaults = true
            })
        }
    }

    private suspend fun ensureAuthenticated(context: Context, token: String) {
        if (client.auth.currentSessionOrNull() == null) {
            try {
                // Check if token is valid
                val jsonBody = buildJsonObject {
                    put("token", token)
                }.toString()
                
                val response = client.functions.invoke("verify-token", body = jsonBody)
                if (response.status.value !in 200..299) {
                    throw Exception("Invalid token")
                }
            } catch (e: Exception) {
                // Token invalid, clear session
                SessionManager.clearToken(context)
            }
        }
    }

    private suspend inline fun <reified T : Any> authenticatedCall(context: Context, crossinline block: suspend (token: String) -> T): Result<T> {
        val token = SessionManager.getToken(context) ?: return Result.failure(Exception("Not authenticated"))
        
        ensureAuthenticated(context, token)
        
        return withContext(Dispatchers.IO) {
            try {
                val result = block(token)
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend inline fun <reified T : Any> authenticatedCall(context: Context, crossinline block: suspend () -> T): Result<T> {
        val token = SessionManager.getToken(context) ?: return Result.failure(Exception("Not authenticated"))
        
        return authenticatedCall(context) { block(token) }
    }

    // ========================================
    // AUTH METHODS
    // ========================================

    suspend fun login(context: Context, email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = client.auth.signInWith(IDToken) {
                email = email
                password = password
                provider = IDToken
            }
            
            val session = response.data
            SessionManager.saveToken(context, session.accessToken ?: "")
            
            // Fetch user profile to get role
            val userId = response.data.user?.id ?: ""
            val userProfile = getProfile(context, session.accessToken ?: "").getOrNull()
            
            if (userProfile != null) {
                SessionManager.saveRole(context, userProfile.role ?: "worker")
            }
            
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWorker(context: Context, registerData: WorkerRegistrationData): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = client.auth.signUpWith(IDToken(Email) {
                email = registerData.email
                password = registerData.password
                data = buildJsonObject {
                    put("full_name", registerData.fullName)
                    put("phone_number", registerData.phoneNumber)
                    put("role", "worker")
                }.toString()
            }
            
            val userId = response.data.user?.id ?: ""
            SessionManager.saveToken(context, response.data.accessToken ?: "")
            SessionManager.saveRole(context, "worker")
            
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerBusiness(context: Context, registerData: BusinessRegistrationData): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = client.auth.signUpWith(IDToken(Email) {
                email = registerData.email
                password = registerData.password
                data = buildJsonObject {
                    put("business_name", registerData.businessName)
                    put("phone_number", registerData.phoneNumber)
                    put("role", "business")
                }.toString()
            }
            
            val userId = response.data.user?.id ?: ""
            SessionManager.saveToken(context, response.data.accessToken ?: "")
            SessionManager.saveRole(context, "business")
            
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.auth.signOut()
            SessionManager.clearToken(context)
            SessionManager.clearRole(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // PROFILE METHODS
    // ========================================

    suspend fun getProfile(context: Context, token: String? = null): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val authToken = token ?: SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            
            val columns = Columns.list(
                "*",
                "worker_profiles(*)",
                "business_profiles(*)",
                "worker_skills(*)",
                "business_facilities(*)"
            )
            
            val response = client.from("profiles")
                .select(columns = columns)
                .goRequestBuilder()
                .buildRequest {
                    header("Authorization", "Bearer $authToken")
                }
                .decodeSingle<UserProfile>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWorkerProfile(context: Context, profile: WorkerProfileUpdate): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            
            val jsonBody = buildJsonObject {
                put("full_name", profile.fullName)
                put("phone_number", profile.phoneNumber)
                put("address", profile.address)
                put("job_category", profile.jobCategory)
                put("job_role", profile.jobRole)
                put("years_experience", profile.yearsExperience)
            }.toString()
            
            client.from("worker_profiles").update(
                data = jsonBody
            ) {
                header("Authorization", "Bearer $token")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBusinessProfile(context: Context, profile: BusinessProfileUpdate): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            
            val jsonBody = buildJsonObject {
                put("business_name", profile.businessName)
                put("address", profile.address)
                put("job_category", profile.jobCategory)
                put("nib_document_url", profile.nibDocumentUrl)
                put("operating_hours_open", profile.operatingHoursOpen)
                put("operating_hours_close", profile.operatingHoursClose)
                put("business_description", profile.businessDescription)
            }.toString()
            
            client.from("business_profiles").update(
                data = jsonBody
            ) {
                header("Authorization", "Bearer $token")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // WORKER SKILLS METHODS
    // ========================================

    suspend fun addWorkerSkill(context: Context, skill: String, experienceLevel: String = "Beginner"): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            
            val jsonBody = buildJsonObject {
                put("skill_name", skill)
                put("experience_level", experienceLevel)
            }.toString()
            
            client.from("worker_skills").insert(
                data = jsonBody
            ) {
                header("Authorization", "Bearer $token")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkerSkills(context: Context): Result<List<WorkerSkill>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            
            client.from("worker_skills")
                .select()
                .goRequestBuilder()
                .buildRequest {
                    header("Authorization", "Bearer $token")
                }
                .decodeList<WorkerSkill>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWorkerSkill(context: Context, skillId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            
            client.from("worker_skills").delete() {
                header("Authorization", "Bearer $token")
            }.decodeJson()
            
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
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            
            val columns = Columns.list(
                "*",
                "worker_profiles(*)",
                "worker_skills(*)",
                "business_profiles(*)"
            )
            
            val response = client.from("jobs")
                .select(columns = columns)
                .goRequestBuilder()
                .buildRequest {
                    header("Authorization", "Bearer $token")
                }
                .decodeList<Job>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableJobs(context: Context): Result<List<Job>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            
            val columns = Columns.list(
                "*",
                "worker_profiles(*)",
                "worker_skills(*)",
                "business_profiles(*)"
            )
            
            val response = client.from("jobs").select(columns = columns) {
                filter { 
                    eq("status", "open") 
                }
            }.decodeList<Job>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun postJob(context: Context, jobData: JobPostingData): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            val jsonBody = buildJsonObject {
                put("business_id", userId)
                put("title", jobData.title)
                put("description", jobData.description)
                put("wage", jobData.wage)
                put("wage_type", jobData.wageType)
                put("location", jobData.location)
                put("category", jobData.category)
                put("worker_count", jobData.workerCount)
                put("start_time", jobData.startTime)
                put("end_time", jobData.endTime)
                put("shift_date", jobData.shiftDate)
                put("is_urgent", jobData.isUrgent)
                put("status", "open")
            }.toString()
            
            val response = client.from("jobs").insert(
                data = jsonBody
            ) {
                header("Authorization", "Bearer $token")
            }.decodeJson()
            
            // Extract the inserted job ID
            val insertedJobId = response.optString("id", "")
            
            Result.success(insertedJobId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJobById(context: Context, jobId: String): Result<Job> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            
            val columns = Columns.list(
                "*",
                "worker_profiles(*)",
                "worker_skills(*)",
                "business_profiles(*)"
            )
            
            val response = client.from("jobs")
                .select(columns = columns)
                .goRequestBuilder()
                .buildRequest {
                    header("Authorization", "Bearer $token")
                    eq("id", jobId)
                }
                .decodeSingle<Job>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // JOB APPLICATION METHODS
    // ========================================

    suspend fun applyForJob(context: Context, jobId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // Check if job is open and worker is not already hired for this job
            val jobResult = getJobById(context, jobId)
            if (jobResult.isFailure) throw Exception("Job not found")
            
            val job = jobResult.getOrThrow()
            
            // Check compliance (21 Days Rule - PP 35/2021)
            val workerHistory = getWorkerHistory(context, token).getOrNull() ?: emptyList()
            
            // Check if worker has worked for this business in last 30 days
            val businessId = job.businessId ?: ""
            val daysWorkedForBusiness = workerHistory.count { application ->
                application.businessId == businessId &&
                application.status in listOf("completed", "ongoing") &&
                LocalDate.parse(application.startedAt!!.substring(0, 10))
                    .isAfter(LocalDate.now().minusDays(30))
            }
            
            // 21 Days Rule: Block if > 20 days
            if (daysWorkedForBusiness > 20) {
                throw Exception("Anda telah melebihi batas 21 hari bekerja untuk klien ini sesuai aturan PKHL PP 35/2021. Silakan tunggu atau cari pekerjaan lain.")
            }
            
            val jsonBody = buildJsonObject {
                put("job_id", jobId)
                put("worker_id", userId)
                put("status", "pending")
            }.toString()
            
            val response = client.from("job_applications").insert(
                data = jsonBody
            ) {
                header("Authorization", "Bearer $token")
            }.decodeJson()
            
            Result.success(response.optString("id", ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptJob(context: Context, jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // Call Edge Function to handle business acceptance, wallet deduction, and transaction creation
            val jsonBody = buildJsonObject {
                put("jobId", jobId)
                put("workerId", userId)
            }.toString()
            
            val response = client.functions.invoke("accept-job", body = jsonBody)
            
            if (response.status.value !in 200..299) {
                throw Exception("Failed to accept job: ${response.bodyAsText()}")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkerJobs(context: Context): Result<List<JobApplication>> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            val columns = Columns.list(
                "*",
                "jobs(*, profiles(*, business_profiles(*))"
            )
            
            val response = client.from("job_applications").select(columns = columns) {
                filter { 
                    eq("worker_id", userId)
                    isNot("status", "rejected")
                }
            }.decodeList<JobApplication>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getWorkerHistory(context: Context, token: String): Result<List<JobApplication>> = withContext(Dispatchers.IO) {
        try {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            val columns = Columns.list(
                "*",
                "jobs(*)",
                "job_applications.jobs(*)" // Join with jobs table
            )
            
            val thirtyDaysAgo = LocalDate.now().minusDays(30).toString()
            
            val response = client.from("job_applications").select(columns = columns) {
                filter { 
                    eq("worker_id", userId)
                    gte("created_at", thirtyDaysAgo)
                }
            }.decodeList<JobApplication>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // BUSINESS STATS & JOBS METHODS (NEW)
    // ========================================

    /**
     * Get Business Stats for Dashboard
     * Based on business-model.md Section 4.1
     * 
     * Returns:
     * - activeShiftsToday: Jobs with status "accepted" or "ongoing" today
     * - workersHiredThisWeek: Job applications with status "completed" in last 7 days
     * - totalSpendingThisMonth: Sum of wages for jobs with status "completed" this month
     * - pendingPatches: Workers awaiting acceptance (status "pending")
     */
    suspend fun getBusinessStats(context: Context): Result<BusinessStats> = withContext(Dispatchers.IO) {
        authenticatedCall(context) { token ->
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // Get today's date string
            val today = LocalDate.now().toString()
            val thisMonthStart = LocalDate.now().withDayOfMonth(1).toString()
            val lastWeekStart = LocalDate.now().minusDays(7).toString()
            
            // 1. Fetch Active Shifts Today
            val todayJobs = client.from("jobs").select() {
                filter { 
                    eq("business_id", userId)
                    isIn("status", listOf("accepted", "ongoing"))
                    gte("shift_date", today)
                    lt("shift_date", LocalDate.now().plusDays(1).toString())
                }
            }.decodeList<Job>()
            
            val activeShiftsToday = todayJobs.size
            
            // 2. Fetch Workers Hired This Week (Completed jobs in last 7 days)
            val recentApplications = client.from("job_applications").select() {
                filter { 
                    eq("business_id", userId)
                    eq("status", "completed")
                    gte("created_at", lastWeekStart)
                }
            }.decodeList<JobApplication>()
            
            val workersHiredThisWeek = recentApplications.size
            
            // 3. Calculate Total Spending This Month
            val thisMonthJobs = client.from("jobs").select() {
                filter { 
                    eq("business_id", userId)
                    eq("status", "completed")
                    gte("shift_date", thisMonthStart)
                    lt("shift_date", LocalDate.now().plusMonths(1).toString())
                }
            }.decodeList<Job>()
            
            var totalSpendingThisMonth = 0.0
            
            thisMonthJobs.forEach { job ->
                totalSpendingThisMonth += job.wage ?: 0.0
            }
            
            // 4. Calculate Pending Patches (Workers awaiting acceptance)
            val pendingApplications = client.from("job_applications").select() {
                filter { 
                    eq("business_id", userId)
                    eq("status", "pending")
                }
            }.decodeList<JobApplication>()
            
            val pendingPatches = pendingApplications.size
            
            // 5. Check Business Location for Rate Bali Suggestion
            val userProfile = getProfile(context, token).getOrNull()
            val businessLocation = userProfile?.businessProfile?.location?.lowercase() ?: ""
            
            val rateBaliSuggestion = when {
                businessLocation.contains("badung") -> RateBaliSuggestion("Badung", 3534339.0, 168302.0, "Area pariwisata utama (Badung) - UMK tertinggi Bali")
                businessLocation.contains("denpasar") -> RateBaliSuggestion("Denpasar", 3298117.0, 157053.0, "Ibu kota provinsi (Denpasar) - UMK Bali")
                businessLocation.contains("gianyar") -> RateBaliSuggestion("Gianyar", 3119080.0, 148527.0, "Area wisata budaya & pusat pertanian (Gianyar) - UMK Bali")
                businessLocation.contains("tabanan") -> RateBaliSuggestion("Tabanan", 3176080.0, 151240.0, "Wilayah dengan destinasi wisata (Tabanan) - UMK Bali")
                else -> null
            }
            
            // 6. Get Wallet Balance (from wallet_balance table or calculate from transactions)
            val walletBalance = 0.0 // Placeholder - TODO: Fetch from wallet_balance table
            
            val stats = BusinessStats(
                activeShiftsToday = activeShiftsToday,
                workersHiredThisWeek = workersHiredThisWeek,
                totalSpendingThisMonth = totalSpendingThisMonth,
                pendingPatches = pendingPatches,
                rateBaliSuggestion = rateBaliSuggestion,
                walletBalance = walletBalance
            )
            
            Result.success(stats)
        }
    }

    /**
     * Get Jobs Posted by This Business
     * Based on business-model.md Section 3.1
     * 
     * Returns jobs filtered for this business only
     */
    suspend fun getBusinessJobs(context: Context): Result<List<Job>> = withContext(Dispatchers.IO) {
        authenticatedCall(context) { token ->
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // Fetch jobs for this business only
            val columns = Columns.list(
                "*",
                "worker_profiles(*)",
                "worker_skills(*)",
                "business_profiles(*)")
            )
            
            val response = client.from("jobs").select(columns = columns) {
                filter { 
                    eq("business_id", userId)
                }
            }.decodeList<Job>()
            
            Result.success(response)
        }
    }

    // ========================================
    // WALLET & TRANSACTION METHODS
    // ========================================

    suspend fun getWalletBalance(context: Context): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            val response = client.from("wallet_balance")
                .select()
                .goRequestBuilder()
                .buildRequest {
                    header("Authorization", "Bearer $token")
                    eq("user_id", userId)
                }
                .decodeSingle<WalletBalance>()
            
            Result.success(response?.balance ?: 0.0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun topUpWallet(context: Context, amount: Double, paymentMethod: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // Record transaction
            val jsonBody = buildJsonObject {
                put("user_id", userId)
                put("amount", amount)
                put("payment_method", paymentMethod)
                put("type", "deposit")
                put("status", "pending")
            }.toString()
            
            client.from("transactions").insert(
                data = jsonBody
            ) {
                header("Authorization", "Bearer $token")
            }.decodeJson()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun withdrawFromWallet(context: Context, amount: Double, bankAccountNumber: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // Check balance
            val balance = getWalletBalance(context).getOrThrow()
            
            if (balance < amount) {
                throw Exception("Saldo tidak mencukup")
            }
            
            // Record withdrawal request
            val jsonBody = buildJsonObject {
                put("user_id", userId)
                put("amount", amount)
                put("bank_account_number", bankAccountNumber)
                put("type", "withdrawal")
                put("status", "pending")
            }.toString()
            
            client.from("transactions").insert(
                data = jsonBody
            ) {
                header("Authorization", "Bearer $token")
            }.decodeJson()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // NOTIFICATION PREFERENCES METHODS
    // ========================================

    /**
     * Update user's notification preferences
     * These are stored in the profiles table in notification_preferences JSONB column
     */
    suspend fun updateNotificationPreferences(
        context: Context,
        pushEnabled: Boolean? = null,
        jobAlertsEnabled: Boolean? = null,
        applicationUpdatesEnabled: Boolean? = null,
        promotionalEnabled: Boolean? = null,
        alertDistance: String? = null,
        alertCategories: List<String>? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // Build notification_preferences JSONB object
            val prefsObject = buildJsonObject {
                if (pushEnabled != null) put("push_enabled", pushEnabled)
                if (jobAlertsEnabled != null) put("job_alerts_enabled", jobAlertsEnabled)
                if (applicationUpdatesEnabled != null) put("application_updates_enabled", applicationUpdatesEnabled)
                if (promotionalEnabled != null) put("promotional_enabled", promotionalEnabled)
                if (alertDistance != null) put("alert_distance", alertDistance)
                if (alertCategories != null) {
                    putJsonArray("alert_categories") {
                        alertCategories.forEach { add(it) }
                    }
                }
            }
            
            // Update profile with new notification preferences
            val jsonBody = buildJsonObject {
                put("notification_preferences", prefsObject)
            }.toString()
            
            client.from("profiles").update(
                data = jsonBody
            ) {
                header("Authorization", "Bearer $token")
                    eq("id", userId)
                }
            }.decodeJson()
            
            Unit
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // IMAGE UPLOAD METHODS
    // ========================================

    suspend fun uploadImage(context: Context, imageUri: Uri, folder: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
            
            // Read image as bytes
            val inputStream: InputStream = context.contentResolver.openInputStream(imageUri) ?: throw Exception("Unable to open image")
            val bytes = inputStream.readBytes()
            
            val fileName = "${UUID.randomUUID()}.jpg"
            
            val uploadResponse = client.storage
                .from(folder)
                .upload(fileName, bytes, {
                    upsert { it }
                })
            
            val publicUrl = "${BuildConfig.SUPABASE_STORAGE_URL}/${folder}/${fileName}"
            
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================
    // EARNINGS SUMMARY (FOR WORKER - PRIORITY 3)
    // ========================================

    /**
     * Get worker's earnings summary by calling calculate-earnings Edge Function
     * Returns total earnings, pending earnings, and transaction history
     */
    suspend fun getEarnings(context: Context): Result<EarningsSummary> = withContext(Dispatchers.IO) {
        authenticatedCall(context) { token ->
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            try {
                val jsonBody = buildJsonObject {
                    put("workerId", userId)
                }.toString()
                
                val response = client.functions.invoke("calculate-earnings", body = jsonBody)
                
                if (response.status.value !in 200..299) {
                    throw Exception("Failed to get earnings")
                }
                
                val responseString = response.bodyAsText()
                
                val json = Json { ignoreUnknownKeys = true }
                json.decodeFromString<EarningsSummary>(responseString)
            } catch (e: Exception) {
                throw Exception("Failed to get earnings: ${e.message}")
            }
        }
    }

    /**
     * Get worker's earnings summary by calling calculate-earnings Edge Function
     * Returns total earnings, pending earnings, and transaction history
     */
    suspend fun getWorkerEarningsSummary(context: Context): Result<EarningsSummary> = withContext(Dispatchers.IO) {
        authenticatedCall(context) { token ->
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            try {
                val jsonBody = buildJsonObject {
                    put("workerId", userId)
                }.toString()
                
                val response = client.functions.invoke("calculate-earnings", body = jsonBody)
                
                if (response.status.value !in 200..299) {
                    throw Exception("Failed to get earnings")
                }
                
                val responseString = response.bodyAsText()
                
                val json = Json { ignoreUnknownKeys = true }
                json.decodeFromString<EarningsSummary>(responseString)
            } catch (e: Exception) {
                throw Exception("Failed to get earnings: ${e.message}")
            }
        }
    }
}
