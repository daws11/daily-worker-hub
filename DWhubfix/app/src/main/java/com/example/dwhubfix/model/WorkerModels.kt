package com.example.dwhubfix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User Profile - Base profile for all users
 */
@Serializable
data class UserProfile(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("role") val role: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // Nested profiles for joined data
    @SerialName("business_profile") val businessProfile: BusinessProfile? = null,
    @SerialName("worker_profile") val workerProfile: WorkerProfileNested? = null
)

/**
 * Worker Profile - Extended profile for workers
 */
@Serializable
data class WorkerProfile(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("rating") val rating: Double? = null,
    @SerialName("no_show_rate") val noShowRate: Double? = null,
    @SerialName("total_shifts") val totalShifts: Int? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("worker_skills") val workerSkills: List<WorkerSkill>? = null
)

/**
 * Worker Skill
 */
@Serializable
data class WorkerSkill(
    val id: String,
    @SerialName("worker_id") val workerId: String,
    @SerialName("skill_name") val skillName: String,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Business Profile - Extended profile for businesses
 */
@Serializable
data class BusinessProfile(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("business_name") val businessName: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("business_type") val businessType: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("rating") val rating: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Worker Profile with nested profile and skills
 */
@Serializable
data class WorkerProfileExtended(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("rating") val rating: Double? = null,
    @SerialName("no_show_rate") val noShowRate: Double? = null,
    @SerialName("total_shifts") val totalShifts: Int? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("worker_profile") val workerProfile: WorkerProfileNested? = null,
    @SerialName("worker_skills") val workerSkills: List<WorkerSkill>? = null
)

@Serializable
data class WorkerProfileNested(
    val id: String,
    @SerialName("rating") val rating: Double? = null
)
