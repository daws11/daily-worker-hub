package com.example.dwhubfix.data

import android.content.Context
import android.net.Uri
import com.example.dwhubfix.BuildConfig
import com.example.dwhubfix.model.*
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import org.json.JSONObject
import java.io.InputStream
import java.util.UUID

import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken

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
                // Manually import session from the stored token
                client.auth.importSession(
                    UserSession(
                        accessToken = token,
                        refreshToken = "", 
                        expiresIn = 604800,
                        tokenType = "Bearer",
                        user = null
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun <T> authenticatedCall(context: Context, block: suspend (String) -> T): Result<T> {
        val tokenRaw = SessionManager.getAccessToken(context)
        if (tokenRaw == null) return Result.failure(Exception("Not authenticated"))
        
        return try {
            ensureAuthenticated(context, tokenRaw)
            Result.success(block(tokenRaw))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(context: Context): Result<UserProfile> = withContext(Dispatchers.IO) {
        authenticatedCall(context) {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // FETCH JOINED DATA
            // fetching profiles with nested worker_profiles, business_profiles, and skills/facilities as siblings
            val columns = Columns.list(
                "*",
                "worker_profiles(*)",
                "worker_skills(*)",
                "business_profiles(*)",
                "business_facilities(*)"
            )
            
            val profile = client.from("profiles").select(columns = columns) {
                filter { eq("id", userId) }
            }.decodeSingle<UserProfile>()
            
            profile
        }
    }
    
    // Legacy support wrapper for UI that still expects JSONObject
    suspend fun getProfileJson(context: Context): Result<JSONObject> = withContext(Dispatchers.IO) {
        val result = getProfile(context)
        result.map { profile ->
            // Manual mapping back to flat JSON for legacy UI compatibility
            val json = JSONObject()
            json.put("id", profile.id)
            json.put("full_name", profile.fullName)
            json.put("role", profile.role)
            json.put("avatar_url", profile.avatarUrl)
            json.put("onboarding_status", profile.onboardingStatus)
            json.put("verification_status", profile.verificationStatus)
            
            profile.workerProfile?.let { wp ->
                json.put("job_category", wp.jobCategory)
                json.put("job_role", wp.jobRole)
                json.put("years_experience", wp.yearsExperience)
                json.put("work_history", wp.workHistory)
                json.put("address", wp.address)
                json.put("latitude", wp.latitude)
                json.put("longitude", wp.longitude)
                json.put("address_photo_url", wp.addressPhotoUrl)
                json.put("experience_document_url", wp.experienceDocumentUrl)
                json.put("domicile_document_url", wp.domicileDocumentUrl)
                
                // Skills List to JSON Array of Objects
                // Use profile.workerSkills since it's now at the top level
                if (profile.workerSkills.isNotEmpty()) {
                     val skillsArray = kotlinx.serialization.json.Json.encodeToString(profile.workerSkills)
                     // Logic to put it into JSON? For now, skipping complex object if not strictly needed by legacy UI,
                     // or mapping it if we know the field name the UI expects.
                     // Assuming 'skills' field isn't strictly required by the legacy flow receiving this JSONObject, 
                     // or the legacy flow handles it differently. 
                     // If it does need it, we should map it.
                     // json.put("skills", JSONArray(skillsArray)) // Requires robust conversion
                }
            }

            profile.businessProfile?.let { bp ->
                 json.put("business_name", bp.businessName)
                 json.put("job_category", bp.jobCategory)
                 json.put("address", bp.address)
                 json.put("latitude", bp.latitude)
                 json.put("longitude", bp.longitude)
                 json.put("operating_hours_open", bp.operatingHoursOpen)
                 json.put("operating_hours_close", bp.operatingHoursClose)
                 json.put("business_description", bp.businessDescription)
                 json.put("nib_document_url", bp.nibDocumentUrl)
                 json.put("location_photo_front_url", bp.locationPhotoFrontUrl)
                 json.put("location_photo_inside_url", bp.locationPhotoInsideUrl)
            }
            json
        }
    }

    suspend fun updateProfile(
        context: Context, 
        fullName: String? = null, 
        role: String? = null,
        avatarUrl: String? = null,
        jobCategory: String? = null, // Worker specific
        jobRole: String? = null,     // Worker specific
        currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        authenticatedCall(context) {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // 1. ENSURE Base Profile exists FIRST (UPSERT to create if not exists)
            val baseUpdate = buildJsonObject {
                put("id", userId)  // Required for upsert
                if (fullName != null) put("full_name", fullName)
                if (role != null) put("role", role)
                if (avatarUrl != null) put("avatar_url", avatarUrl)
                put("onboarding_status", "ongoing")
                if (currentStep != null) put("current_step", currentStep)
            }
            // Always upsert to ensure parent record exists
            client.from("profiles").upsert(baseUpdate)
            
            // 2. Update Worker Profile AFTER parent record exists
            if (role == "worker" && (jobCategory != null || jobRole != null)) {
                val workerUpdate = buildJsonObject {
                    put("id", userId)  // Foreign key to profiles.id
                    if (jobCategory != null) put("job_category", jobCategory)
                    if (jobRole != null) put("job_role", jobRole)
                }
                client.from("worker_profiles").upsert(workerUpdate)
            }
        }
    }
    
    suspend fun uploadFile(context: Context, uri: Uri, folder: String): Result<String> = withContext(Dispatchers.IO) {
         authenticatedCall(context) {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            val fileName = "${System.currentTimeMillis()}_${uri.lastPathSegment ?: "file"}"
            val path = "$userId/$folder/$fileName" 
            val byteArray = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw Exception("Could not read file")
            client.storage.from("documents").upload(path, byteArray) { upsert = false }
            client.storage.from("documents").publicUrl(path)
         }
    }

    suspend fun updateWorkerAddress(
        context: Context,
        address: String,
        latitude: Double,
        longitude: Double,
        photoUrl: String?,
        documentUrl: String?,
        currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        authenticatedCall(context) {
             val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
             
             // 1. ENSURE parent profile exists first
             val profileUpdate = buildJsonObject {
                 put("id", userId)
                 if (currentStep != null) put("current_step", currentStep)
             }
             client.from("profiles").upsert(profileUpdate)
             
             // 2. THEN upsert into worker_profiles
             val updateData = buildJsonObject {
                 put("id", userId)
                 put("address", address)
                 put("latitude", latitude)
                 put("longitude", longitude)
                 if (photoUrl != null) put("address_photo_url", photoUrl)
                 if (documentUrl != null) put("domicile_document_url", documentUrl)
             }
             client.from("worker_profiles").upsert(updateData)
             Unit
        }
    }

    suspend fun updateWorkerSkills(
        context: Context,
        skills: List<String>,
        experienceLevels: Map<String, String>,
        currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        authenticatedCall(context) {
             val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
             
             // 1. Delete old skills? Or is this additive?
             // For now, let's delete existing skills for this user to perform a full update
             client.from("worker_skills").delete {
                 filter { eq("profile_id", userId) }
             }
             
             // 2. Insert new skills
             val skillsPayload = skills.map { skillName ->
                 WorkerSkill(name = skillName, level = experienceLevels[skillName] ?: "Intermediate")
             }
             // We need to include profile_id. 
             // Since WorkerSkill data class doesn't have profile_id (it's a child in the model), 
             // we construct the JSON manually for insert
             val insertData = skills.map { skill ->
                 buildJsonObject {
                     put("profile_id", userId)
                     put("skill_name", skill)
                     put("experience_level", experienceLevels[skill] ?: "Intermediate")
                 }
             }
             client.from("worker_skills").insert(insertData)
             
             // Update step
             if (currentStep != null) {
                 val stepUpdate = buildJsonObject { put("current_step", currentStep) }
                 client.from("profiles").update(stepUpdate) {
                     filter { eq("id", userId) }
                 }
             }
        }
    }

    suspend fun signUpWithEmail(context: Context, email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            // Note: If email confirmation is enabled, session might be null initially.
            // If disabled, we might get a session immediately.
            // For now, we assume we want to handle immediate sign in or just return success.
            // Saving session if available:
            val session = client.auth.currentSessionOrNull()
            if (session != null) {
                SessionManager.saveSession(context, session.accessToken, session.user?.id ?: "")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(context: Context, email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val session = client.auth.currentSessionOrNull() ?: throw Exception("Login failed: No session")
            val userId = session.user?.id ?: throw Exception("Login failed: No user ID")
            
            SessionManager.saveSession(context, session.accessToken, userId)
            
            // Fetch role
             val result = getProfile(context)
             if (result.isSuccess) {
                 val profile = result.getOrNull()
                 Result.success(profile?.role ?: "worker")
             } else {
                 // Default to worker if profile fetch fails but auth succeeded (edge case)
                 Result.success("worker")
             }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                client.auth.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            SessionManager.clearSession(context)
        }
    }

    suspend fun getAvailableJobs(context: Context): Result<List<Job>> = withContext(Dispatchers.IO) {
        authenticatedCall(context) {
            // Select from jobs table with business info joined
            val columns = Columns.list("*", "profiles(*)")
            client.from("jobs").select(columns = columns).decodeList<Job>()
        }
    }

    // ... (Other refactored methods would follow similar pattern targeting business_profiles)
    // Stubbing updateBusinessBasicProfile for completeness of the immediate task context
     suspend fun updateBusinessBasicProfile(
        context: Context, businessName: String, category: String, logoUrl: String?, currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
         authenticatedCall(context) {
             val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
             
             // 1. ENSURE parent profile exists first (UPSERT)
             val profileUpdate = buildJsonObject {
                 put("id", userId)
                 if (logoUrl != null) put("avatar_url", logoUrl) 
                 if (currentStep != null) put("current_step", currentStep)
             }
             client.from("profiles").upsert(profileUpdate)
             
             // 2. THEN upsert business_profiles
             val businessUpdate = buildJsonObject {
                 put("id", userId)
                 put("business_name", businessName)
                 put("job_category", category)
             }
             client.from("business_profiles").upsert(businessUpdate)
             Unit
         }
     }
     
    suspend fun handleAuthRedirect(
        context: Context, 
        accessToken: String?, 
        refreshToken: String?,
        tokenType: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val validAccessToken = accessToken
            val validRefreshToken = refreshToken
            
            if (!validAccessToken.isNullOrEmpty()) {
                // 1. Manually extract userId (sub) from the JWT token
                var tokenExp: Long = 0
                val userId = try {
                    val parts = validAccessToken.split(".")
                    if (parts.size == 3) {
                        val payload = parts[1]
                        // Decode Base64 URL safe
                        val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
                        val decodedString = String(decodedBytes)
                        val jsonObject = JSONObject(decodedString)
                        tokenExp = jsonObject.optLong("exp", 0)
                        jsonObject.getString("sub")
                    } else {
                        throw Exception("Invalid JWT format")
                    }
                } catch (e: Exception) {
                    throw Exception("Failed to parse token: ${e.message}")
                }
                
                // 2. Store the token and userId
                // If we also have a refresh token, we should store it too (SessionManager needs update for refresh token support technically, but for now access token is key)
                SessionManager.clearSession(context)
                SessionManager.saveSession(context, validAccessToken, userId)
                
                // 3. Try to get user profile to extract role
                val result = getProfile(context)
                if (result.isSuccess) {
                    val profile = result.getOrNull()
                    Result.success(profile?.role ?: "worker")
                } else {
                    val originalError = result.exceptionOrNull()
                    val currentTime = System.currentTimeMillis() / 1000
                    val debugMessage = "Verify Failed: ${originalError?.message}\nExp: $tokenExp\nNow: $currentTime\nDiff: ${tokenExp - currentTime}"
                    Result.failure(Exception(debugMessage))
                }
            } else {
                 Result.failure(Exception("No access token found in redirect"))
            }
        } catch (e: Exception) {
             Result.failure(e)
        }
    }
     
     suspend fun uploadVerificationImage(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
         uploadFile(context, uri, "verification")
     }
     
     suspend fun updateProfileDocument(
         context: Context,
         documentType: String,
         documentUrl: String,
         currentStep: String? = null
     ): Result<Unit> = withContext(Dispatchers.IO) {
         authenticatedCall(context) {
             val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
             val role = SessionManager.getSelectedRole(context)
             
             // 1. ENSURE parent profile exists first
             val profileUpdate = buildJsonObject {
                 put("id", userId)
                 if (currentStep != null) put("current_step", currentStep)
             }
             client.from("profiles").upsert(profileUpdate)
             
             // 2. THEN upsert to child table
             val updateData = buildJsonObject {
                 put("id", userId)
                 when (documentType) {
                     "ktp" -> put("ktp_document_url", documentUrl)
                     "selfie" -> put("selfie_photo_url", documentUrl)
                     "nib" -> put("nib_document_url", documentUrl)
                     else -> put("${documentType}_document_url", documentUrl)
                 }
             }
             
             val table = if (role == "business") "business_profiles" else "worker_profiles"
             client.from(table).upsert(updateData)
             Unit
         }
     }
     
     suspend fun updateWorkerExperience(
        context: Context,
        experienceLevel: String,
        workHistory: String? = null,
        documentUrl: String? = null,
        currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        authenticatedCall(context) {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // 1. ENSURE parent profile exists first
            val profileUpdate = buildJsonObject {
                put("id", userId)
                if (currentStep != null) put("current_step", currentStep)
            }
            client.from("profiles").upsert(profileUpdate)
            
            // 2. THEN upsert worker_profiles
            val updateData = buildJsonObject {
                put("id", userId)
                put("years_experience", experienceLevel)
                if (workHistory != null) put("work_history", workHistory)
                if (documentUrl != null) put("experience_document_url", documentUrl)
            }
            client.from("worker_profiles").upsert(updateData)
            Unit
        }
    }

     suspend fun updateWorkerPortfolio(
         context: Context,
         portfolioUrls: List<String>,
         currentStep: String? = null
     ): Result<Unit> = withContext(Dispatchers.IO) {
         authenticatedCall(context) {
             val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")

             // 1. ENSURE parent profile exists first
             val profileUpdate = buildJsonObject {
                 put("id", userId)
                 if (currentStep != null) put("current_step", currentStep)
             }
             client.from("profiles").upsert(profileUpdate)

             // 2. THEN upsert worker_profiles
             val updateData = buildJsonObject {
                 put("id", userId)
                 putJsonArray("portfolio_urls") {
                     portfolioUrls.forEach { add(it) }
                 }
             }
             client.from("worker_profiles").upsert(updateData)
             Unit
         }
     }

     suspend fun updateBusinessLocation(
         context: Context,
         address: String,
         latitude: Double,
         longitude: Double,
         photoUrl: String?,
         currentStep: String? = null
     ): Result<Unit> = withContext(Dispatchers.IO) {
         authenticatedCall(context) {
             val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")

             // 1. ENSURE parent profile exists first
             val profileUpdate = buildJsonObject {
                 put("id", userId)
                 if (currentStep != null) put("current_step", currentStep)
             }
             client.from("profiles").upsert(profileUpdate)

             // 2. THEN upsert business_profiles
             val updateData = buildJsonObject {
                 put("id", userId)
                 put("address", address)
                 put("latitude", latitude)
                 put("longitude", longitude)
                 if (photoUrl != null) put("location_photo_front_url", photoUrl)
             }
             client.from("business_profiles").upsert(updateData)
             Unit
         }
     }

     suspend fun completeBusinessRegistration(
         context: Context
     ): Result<Unit> = withContext(Dispatchers.IO) {
         authenticatedCall(context) {
             val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")

             // Update onboarding status to completed
            val statusUpdate = buildJsonObject {
                put("onboarding_status", "completed")
                put("current_step", "business_verification_pending")
            }
            client.from("profiles").update(statusUpdate) { filter { eq("id", userId) } }
            Unit
         }
     }

     suspend fun updateBusinessPreferences(
        context: Context,
        selectedSkills: List<String>,
        experienceLevel: String,
        languages: List<String>,
        priorityHiring: Boolean,
        currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        authenticatedCall(context) {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // 1. ENSURE parent profile exists first
            val profileUpdate = buildJsonObject {
                put("id", userId)
                if (currentStep != null) put("current_step", currentStep)
            }
            client.from("profiles").upsert(profileUpdate)
            
            // 2. THEN upsert business_profiles
            val updateData = buildJsonObject {
                put("id", userId)
                putJsonObject("worker_preferences") {
                    putJsonArray("preferred_skills") {
                        selectedSkills.forEach { add(it) }
                    }
                    put("min_experience_level", experienceLevel)
                    putJsonArray("required_languages") {
                        languages.forEach { add(it) }
                    }
                    put("priority_hiring", priorityHiring)
                }
            }
            client.from("business_profiles").upsert(updateData)
            Unit
        }
    }
    
    suspend fun updateBusinessDocuments(
        context: Context,
        nibUrl: String?,
        locationFrontUrl: String?,
        locationInsideUrl: String?,
        currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        authenticatedCall(context) {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // 1. ENSURE parent profile exists first
            val profileUpdate = buildJsonObject {
                put("id", userId)
                if (currentStep != null) put("current_step", currentStep)
            }
            client.from("profiles").upsert(profileUpdate)
            
            // 2. THEN upsert business_profiles
            val updateData = buildJsonObject {
                put("id", userId)
                if (nibUrl != null) put("nib_document_url", nibUrl)
                if (locationFrontUrl != null) put("location_photo_front_url", locationFrontUrl)
                if (locationInsideUrl != null) put("location_photo_inside_url", locationInsideUrl)
            }
            client.from("business_profiles").upsert(updateData)
            Unit
        }
    }
    
    suspend fun updateBusinessDetails(
        context: Context,
        openTime: String?,
        closeTime: String?,
        description: String?,
        facilities: List<String>?,
        currentStep: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        authenticatedCall(context) {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // 1. ENSURE parent profile exists first
            val profileUpdate = buildJsonObject {
                put("id", userId)
                if (currentStep != null) put("current_step", currentStep)
            }
            client.from("profiles").upsert(profileUpdate)
            
            // 2. THEN upsert business_profiles
            val updateData = buildJsonObject {
                put("id", userId)
                if (openTime != null) put("operating_hours_open", openTime)
                if (closeTime != null) put("operating_hours_close", closeTime)
                if (description != null) put("business_description", description)
                if (facilities != null) {
                    putJsonArray("facilities") {
                        facilities.forEach { add(it) }
                    }
                }
            }
            client.from("business_profiles").upsert(updateData)
            Unit
        }
    }

    // ========================================
    // JOB APPLICATION METHODS
    // ========================================

    /**
     * Accept a job by calling the accept-job Edge Function
     * This will validate availability, create application, and create transaction
     */
    suspend fun acceptJob(context: Context, jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        authenticatedCall(context) { token ->
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            try {
                // Manually serialize body
                val jsonBody = buildJsonObject {
                    put("jobId", jobId)
                    put("workerId", userId)
                }.toString()
                
                val response = client.functions.invoke("accept-job", body = jsonBody)
                
                if (response.status.value !in 200..299) {
                    throw Exception("Failed to accept job: ${response.bodyAsText()}")
                }
                
                Unit
            } catch (e: Exception) {
                throw Exception("Failed to accept job: ${e.message}")
            }
        }
    }

    /**
     * Get worker's job applications filtered by status
     * @param statuses Variable number of status strings to filter by (e.g., "accepted", "ongoing")
     * If no statuses provided, returns all applications
     */
    suspend fun getMyJobs(context: Context, vararg statuses: String): Result<List<JobApplication>> = withContext(Dispatchers.IO) {
        authenticatedCall(context) {
            val userId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // Fetch job applications with nested job data
            val columns = Columns.list(
                "*",
                "job:jobs(*, profiles(*, business_profiles(*)))"
            )
            
            val query = client.from("job_applications").select(columns = columns) {
                filter { 
                    eq("worker_id", userId)
                    if (statuses.isNotEmpty()) {
                        isIn("status", statuses.toList())
                    }
                }
            }
            
            // Sort by created_at descending
            val results = query.decodeList<JobApplication>()
            results.sortedByDescending { it.createdAt }
        }
    }

    /**
     * Get worker's earnings summary by calling the calculate-earnings Edge Function
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
}
