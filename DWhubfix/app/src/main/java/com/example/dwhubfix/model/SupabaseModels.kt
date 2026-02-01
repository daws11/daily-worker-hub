package com.example.dwhubfix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val role: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("onboarding_status") val onboardingStatus: String? = null,
    @SerialName("verification_status") val verificationStatus: String? = null,
    
    // Joined tables (Null if not joined or not existent)
    @SerialName("worker_profiles") val workerProfile: WorkerProfile? = null,
    @SerialName("business_profiles") val businessProfile: BusinessProfile? = null,

    // NEW: Direct children of profiles
    @SerialName("worker_skills") val workerSkills: List<WorkerSkill> = emptyList(),
    @SerialName("business_facilities") val businessFacilities: List<BusinessFacility> = emptyList()
)

@Serializable
data class WorkerProfile(
    @SerialName("job_category") val jobCategory: String? = null,
    @SerialName("job_role") val jobRole: String? = null,
    @SerialName("years_experience") val yearsExperience: String? = null,
    @SerialName("work_history") val workHistory: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("address_photo_url") val addressPhotoUrl: String? = null,
    @SerialName("experience_document_url") val experienceDocumentUrl: String? = null,
    @SerialName("domicile_document_url") val domicileDocumentUrl: String? = null
)

@Serializable
data class BusinessProfile(
    @SerialName("business_name") val businessName: String? = null,
    @SerialName("job_category") val jobCategory: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("operating_hours_open") val operatingHoursOpen: String? = null,
    @SerialName("operating_hours_close") val operatingHoursClose: String? = null,
    @SerialName("business_description") val businessDescription: String? = null,
    @SerialName("nib_document_url") val nibDocumentUrl: String? = null,
    @SerialName("location_photo_front_url") val locationPhotoFrontUrl: String? = null,
    @SerialName("location_photo_inside_url") val locationPhotoInsideUrl: String? = null,
    @SerialName("worker_preferences") val workerPreferences: WorkerPreferences? = null
)

@Serializable
data class WorkerSkill(
    @SerialName("skill_name") val name: String,
    @SerialName("experience_level") val level: String = "Intermediate"
)

@Serializable
data class BusinessFacility(
    @SerialName("facility_name") val name: String
)

@Serializable
data class WorkerPreferences(
    val skills: List<String> = emptyList(),
    @SerialName("experience_level") val experienceLevel: String? = null,
    val languages: List<String> = emptyList(),
    @SerialName("priority_hiring") val priorityHiring: Boolean = false
)

@Serializable
data class Job(
    val id: String,
    @SerialName("business_id") val businessId: String,
    val title: String,
    val description: String? = null,
    val wage: Double? = null,
    @SerialName("wage_type") val wageType: String? = null,
    val location: String? = null,
    val category: String? = null,
    val status: String = "open",
    // We might want to join business info
    @SerialName("profiles") val businessInfo: UserProfile? = null
)
