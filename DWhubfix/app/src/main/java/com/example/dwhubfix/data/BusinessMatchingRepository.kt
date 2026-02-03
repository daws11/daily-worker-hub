package com.example.dwhubfix.data

import android.content.Context
import org.osmdroid.util.GeoPoint
import com.example.dwhubfix.model.*
import com.example.dwhubfix.utils.calculateDistance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * BUSINESS WORKER MATCHING LOGIC
 * 
 * Based on matching-algorithm.md (Business Side):
 * 1. Scoring System (Distance, Skill, Rating, Reliability, Availability)
 * 2. Worker Filtering (Skills, Rating, Distance)
 * 3. Worker Prioritization (Sort by Score)
 * 
 * Based on business-model.md:
 * 1. Workers hired based on scores (highest first)
 * 2. Platform Commission (6%)
 * 3. Compliance Check (21 Days Rule)
 */

object BusinessMatchingRepository {
    
    /**
     * Get workers for job based on matching criteria
     * Returns list of worker candidates sorted by score (highest first)
     */
    suspend fun getWorkersForJob(
        context: Context,
        jobId: String,
        businessLocation: GeoPoint? = null
    ): Result<BusinessMatchingResult> = withContext(Dispatchers.IO) {
        authenticatedCall(context) {
            val businessId = SessionManager.getUserId(context) ?: throw Exception("No user ID")
            
            // 1. Fetch Job Details
            val job = SupabaseRepository.getJobById(context, jobId).getOrNull()
                ?: throw Exception("Job not found")
            
            // 2. Fetch All Available Workers
            val workers = SupabaseRepository.getAllWorkers(context).getOrNull() ?: emptyList()
            
            // 3. Get Business Job Applications (for Compliance Check)
            val businessJobApplications = getBusinessJobApplications(context, businessId).getOrNull() ?: emptyList()
            
            // 4. Filter Workers (Skills, Rating, Distance)
            val filteredWorkers = filterWorkers(
                workers = workers,
                job = job,
                businessJobApplications = businessJobApplications,
                businessLocation = businessLocation
            )
            
            // 5. Calculate Score for Each Worker
            val candidatesWithScores = filteredWorkers.map { worker ->
                val score = calculateBusinessWorkerScore(
                    worker = worker,
                    job = job,
                    businessLocation = businessLocation ?: GeoPoint(-8.5069, 115.2625) // Default to Bali center
                )
                
                val distance = if (worker.workerProfile?.latitude != null && worker.workerProfile?.longitude != null && businessLocation != null) {
                    calculateDistance(
                        businessLocation.latitude,
                        businessLocation.longitude,
                        worker.workerProfile!!.latitude!!,
                        worker.workerProfile!!.longitude!!
                    )
                } else {
                    Double.MAX_VALUE
                }
                
                val distanceString = if (distance != Double.MAX_VALUE) {
                    com.example.dwhubfix.utils.formatDistance(distance)
                } else {
                    "Unknown"
                }
                
                WorkerCandidate(
                    workerId = worker.id,
                    workerName = worker.workerProfile?.fullName ?: worker.fullName,
                    avatarUrl = worker.workerProfile?.avatarUrl,
                    skills = worker.workerSkills.map { it.skillName },
                    rating = worker.workerProfile?.rating ?: 0.0,
                    matchScore = score.totalScore,
                    breakdown = score,
                    distance = distanceString,
                    reliabilityScore = score.reliabilityScore,
                    noShowRate = worker.workerProfile?.noShowRate ?: 0.0,
                    isCompliant = checkWorkerCompliance(
                        worker = worker,
                        businessJobApplications = businessJobApplications
                    ),
                    totalShiftsCompleted = businessJobApplications.count { it.workerId == worker.id },
                    hourlyRate = job.wage?.div(8.0), // Simple calculation (8 hours shift)
                    hourlyRateString = if (job.wage != null) {
                        "Rp ${formatCurrency(job.wage.toInt() / 8)}rb"
                    } else {
                        null
                    },
                    availabilityStatus = if (worker.workerProfile?.isAvailable != false) {
                        "Tersedia"
                    } else {
                        "Sibuk"
                    },
                    lastActiveDate = worker.workerProfile?.lastActiveDate,
                    distanceValue = distance
                )
            }
            
            // 6. Sort by Score (Highest First)
            val prioritizedCandidates = candidatesWithScores.sortedByDescending { it.matchScore }
            
            // 7. Return Result
            val result = BusinessMatchingResult(
                jobId = job.id,
                candidates = prioritizedCandidates,
                totalCandidates = prioritizedCandidates.size,
                matchedCandidates = prioritizedCandidates.count { it.matchScore >= 70.0 }, // Candidates with score >= 70
                averageScore = if (prioritizedCandidates.isNotEmpty()) {
                    prioritizedCandidates.map { it.matchScore }.average()
                } else {
                    0.0
                },
                recommendedCandidate = prioritizedCandidates.firstOrNull()
            )
            
            Result.success(result)
        }
    }
    
    /**
     * Filter workers based on job requirements
     * Filter by Skills, Rating, Distance
     */
    private fun filterWorkers(
        workers: List<UserProfile>,
        job: Job,
        businessJobApplications: List<JobApplication>,
        businessLocation: GeoPoint?
    ): List<UserProfile> {
        return workers.filter { worker ->
            val workerProfile = worker.workerProfile ?: return@filter false
            
            // 1. Filter by Skills (Exact Match with Job Category)
            val jobCategory = job.category ?: ""
            val workerSkills = worker.workerSkills.map { it.skillName }.toSet()
            val skillMatch = jobCategory in workerSkills
            
            // 2. Filter by Rating (Min Rating 3.0)
            val ratingMatch = (workerProfile.rating ?: 0.0) >= 3.0
            
            // 3. Filter by Distance (Max 20 km)
            var distanceMatch = true
            if (businessLocation != null && workerProfile.latitude != null && workerProfile.longitude != null) {
                val distance = calculateDistance(
                    businessLocation.latitude,
                    businessLocation.longitude,
                    workerProfile.latitude!!,
                    workerProfile.longitude!!
                )
                distanceMatch = distance <= 20.0
            }
            
            // 4. Filter by Availability (Available Workers Only)
            val availabilityMatch = workerProfile.isAvailable != false
            
            // Combine Filters
            skillMatch && ratingMatch && distanceMatch && availabilityMatch
        }
    }
    
    /**
     * Business Side Worker Scoring Algorithm
     * 
     * Based on matching-algorithm.md (Business Side):
     * - Distance Score (25%) - Worker location to job location
     * - Skill Score (30%) - Exact match with required skill
     * - Rating Score (20%) - Worker's overall rating
     * - Reliability Score (15%) - Worker's no-show rate
     * - Availability Score (10%) - Worker's availability status
     */
    private fun calculateBusinessWorkerScore(
        worker: UserProfile,
        job: Job,
        businessLocation: GeoPoint
    ): WorkerScoreBreakdown {
        val workerProfile = worker.workerProfile ?: throw Exception("Worker profile not found")
        
        // 1. Distance Score (0-25)
        val distanceScore = if (workerProfile.latitude != null && workerProfile.longitude != null) {
            val distance = calculateDistance(
                workerProfile.latitude!!,
                workerProfile.longitude!!,
                businessLocation.latitude,
                businessLocation.longitude
            )
            when {
                distance < 2.0 -> 25.0
                distance in 2.0..5.0 -> 20.0
                distance in 5.0..10.0 -> 15.0
                distance in 10.0..20.0 -> 10.0
                else -> 0.0
            }
        } else {
            0.0
        }
        
        // 2. Skill Score (0-30)
        val jobCategory = job.category ?: ""
        val workerSkills = worker.workerSkills.map { it.skillName }.toSet()
        val skillScore = if (jobCategory in workerSkills) {
            30.0
        } else {
            0.0
        }
        
        // 3. Rating Score (0-20)
        val ratingScore = (workerProfile.rating ?: 0.0) / 5.0 * 20.0
        
        // 4. Reliability Score (0-15)
        val noShowRate = workerProfile.noShowRate ?: 0.0
        val reliabilityScore = (1.0 - noShowRate) * 15.0
        
        // 5. Availability Score (0-10)
        val availabilityScore = if (workerProfile.isAvailable != false) {
            10.0
        } else {
            0.0
        }
        
        // Total Score (0-100)
        val totalScore = distanceScore + skillScore + ratingScore + reliabilityScore + availabilityScore
        
        return WorkerScoreBreakdown(
            distanceScore = distanceScore,
            skillScore = skillScore,
            ratingScore = ratingScore,
            reliabilityScore = reliabilityScore,
            availabilityScore = availabilityScore
        )
    }
    
    /**
     * Check if worker is compliant with 21 Days Rule (PP 35/2021)
     * Worker cannot work for same business > 20 days in last 30 days
     * 
     * This prevents permanent employment risk for business
     */
    private fun checkWorkerCompliance(
        worker: UserProfile,
        businessJobApplications: List<JobApplication>
    ): Boolean {
        val workerId = worker.id
        
        // Count days worked for this business in last 30 days
        val daysWorkedForBusiness = businessJobApplications.count { application ->
            application.workerId == workerId &&
            application.status in listOf("completed", "ongoing") &&
            application.startedAt?.let { startedAt ->
                try {
                    val startDate = LocalDate.parse(startedAt.substring(0, 10))
                    startDate.isAfter(LocalDate.now().minusDays(30))
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }
        
        // Compliant if <= 20 days
        return daysWorkedForBusiness <= 20
    }
    
    /**
     * Get Worker Availability Status
     * Returns availability status for sorting and display
     */
    private fun getWorkerAvailability(worker: UserProfile): WorkerAvailability {
        val workerProfile = worker.workerProfile ?: throw Exception("Worker profile not found")
        
        val isAvailable = workerProfile.isAvailable != false
        
        // Determine availability status
        val lastActiveDate = workerProfile.lastActiveDate
        val daysSinceActive = if (lastActiveDate != null) {
            try {
                val lastActive = LocalDate.parse(lastActiveDate.substring(0, 10))
                ChronoUnit.DAYS.between(lastActive, LocalDate.now()).toInt()
            } catch (e: Exception) {
                999 // Very long time ago
            }
        } else {
            999 // Never active
        }
        
        val status = when {
            !isAvailable -> "Sibuk"
            daysSinceActive <= 1 -> "Tersedia"
            daysSinceActive <= 3 -> "Sedang Bekerja"
            daysSinceActive <= 7 -> "Terakhir 7 hari"
            daysSinceActive <= 14 -> "Terakhir 14 hari"
            else -> "Tidak aktif"
        }
        
        return WorkerAvailability(
            isAvailable = isAvailable,
            preferredCategories = worker.workerSkills.map { it.skillName },
            maxShiftsPerMonth = workerProfile.maxShiftsPerMonth ?: 0,
            rating = workerProfile.rating ?: 0.0,
            noShowRate = workerProfile.noShowRate ?: 0.0
        )
    }
    
    /**
     * Get Business Job Applications (for Compliance Check)
     * Returns all job applications for this business
     */
    private suspend fun getBusinessJobApplications(context: Context, businessId: String): Result<List<JobApplication>> {
        val token = SessionManager.getToken(context) ?: throw Exception("Not authenticated")
        
        val columns = Columns.list(
            "id",
            "job_id",
            "worker_id",
            "status",
            "started_at",
            "jobs(*)", // Join with jobs table
            "worker_profiles(*)" // Join with worker_profiles table
        )
        
        return authenticatedCall {
            client.from("job_applications").select(columns = columns) {
                eq("business_id", businessId)
            }.decodeList<JobApplication>()
        }
    }
}
