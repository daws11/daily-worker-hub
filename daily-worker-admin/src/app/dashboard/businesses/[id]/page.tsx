
import { createClient } from '@/utils/supabase/server'
import { notFound } from 'next/navigation'
import { CheckCircle, XCircle, FileText, Building2, MapPin, Clock } from 'lucide-react'
import { approveBusiness, rejectBusiness } from '../actions'

export default async function BusinessDetailPage({ params }: { params: Promise<{ id: string }> }) {
    const id = (await params).id
    const supabase = await createClient()

    const { data: business } = await supabase
        .from('profiles')
        .select(`
            *,
            business_profiles (*),
            business_facilities (*)
        `)
        .eq('id', id)
        .single()

    if (!business) {
        notFound()
    }

    const approveAction = approveBusiness.bind(null, id)
    const rejectAction = rejectBusiness.bind(null, id, 'Rejected by admin')

    // Helper to extract flattened business data
    const businessProfile = Array.isArray(business.business_profiles)
        ? business.business_profiles[0]
        : business.business_profiles

    const businessName = businessProfile?.business_name || business.business_name
    const businessAddress = businessProfile?.address || business.address
    const jobCategory = businessProfile?.job_category || business.job_category
    const businessDesc = businessProfile?.business_description || business.business_description
    const openTime = businessProfile?.operating_hours_open || business.operating_hours_open
    const closeTime = businessProfile?.operating_hours_close || business.operating_hours_close
    const locationFront = businessProfile?.location_photo_front_url || business.location_photo_front_url
    const locationInside = businessProfile?.location_photo_inside_url || business.location_photo_inside_url
    const nibUrl = businessProfile?.nib_document_url || business.nib_document_url

    // Facilities: try table (business_facilities) then legacy array (profiles.facilities)
    const facilities = business.business_facilities && business.business_facilities.length > 0
        ? business.business_facilities
        : business.facilities

    return (
        <div className="space-y-8 max-w-5xl mx-auto">
            <div className="flex justify-between items-start">
                <div>
                    <h2 className="text-3xl font-bold tracking-tight text-gray-900">{businessName}</h2>
                    <p className="text-gray-500 mt-1">Submitted on {new Date(business.created_at).toLocaleDateString()}</p>
                </div>
                <div className="flex space-x-3">
                    <form action={rejectAction}>
                        <button
                            type="submit"
                            className="inline-flex items-center px-4 py-2 border border-red-300 shadow-sm text-sm font-medium rounded-md text-red-700 bg-white hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
                        >
                            <XCircle className="w-4 h-4 mr-2" />
                            Reject
                        </button>
                    </form>
                    <form action={approveAction}>
                        <button
                            type="submit"
                            className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
                        >
                            <CheckCircle className="w-4 h-4 mr-2" />
                            Approve Project
                        </button>
                    </form>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Business Info */}
                <div className="bg-white shadow rounded-lg p-6 space-y-4">
                    <h3 className="text-lg font-medium text-gray-900 border-b pb-2 flex items-center">
                        <Building2 className="w-5 h-5 mr-2 text-indigo-500" /> Business Information
                    </h3>
                    <dl className="grid grid-cols-1 gap-x-4 gap-y-4 sm:grid-cols-2">
                        <div className="sm:col-span-1">
                            <dt className="text-sm font-medium text-gray-500">Owner Name</dt>
                            <dd className="mt-1 text-sm text-gray-900">{business.full_name}</dd>
                        </div>
                        <div className="sm:col-span-1">
                            <dt className="text-sm font-medium text-gray-500">Phone</dt>
                            <dd className="mt-1 text-sm text-gray-900">{business.phone_number}</dd>
                        </div>
                        <div className="sm:col-span-2">
                            <dt className="text-sm font-medium text-gray-500">Address</dt>
                            <dd className="mt-1 text-sm text-gray-900 flex items-start">
                                <MapPin className="w-4 h-4 mr-1 text-gray-400 mt-0.5" />
                                {businessAddress || 'Not provided'}
                            </dd>
                        </div>
                        <div className="sm:col-span-2">
                            <dt className="text-sm font-medium text-gray-500">Category</dt>
                            <dd className="mt-1 text-sm text-gray-900">{jobCategory}</dd>
                        </div>
                        <div className="sm:col-span-2">
                            <dt className="text-sm font-medium text-gray-500">Description</dt>
                            <dd className="mt-1 text-sm text-gray-900">{businessDesc || '-'}</dd>
                        </div>
                    </dl>
                </div>

                {/* Operating Hours & Facilities */}
                <div className="bg-white shadow rounded-lg p-6 space-y-4">
                    <h3 className="text-lg font-medium text-gray-900 border-b pb-2 flex items-center">
                        <Clock className="w-5 h-5 mr-2 text-indigo-500" /> Operations & Details
                    </h3>
                    <dl className="grid grid-cols-1 gap-x-4 gap-y-4 sm:grid-cols-2">
                        <div className="sm:col-span-1">
                            <dt className="text-sm font-medium text-gray-500">Thinking Open</dt>
                            <dd className="mt-1 text-sm text-gray-900">{openTime || '-'}</dd>
                        </div>
                        <div className="sm:col-span-1">
                            <dt className="text-sm font-medium text-gray-500">Thinking Close</dt>
                            <dd className="mt-1 text-sm text-gray-900">{closeTime || '-'}</dd>
                        </div>
                        <div className="sm:col-span-2">
                            <dt className="text-sm font-medium text-gray-500">Facilities</dt>
                            <dd className="mt-1 text-sm text-gray-900 flex flex-wrap gap-2">
                                {facilities ? (
                                    Array.isArray(facilities) ? facilities.map((fac: any) => (
                                        <span key={fac.id || fac} className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                                            {typeof fac === 'string' ? fac : (fac.facility_name || fac.name)}
                                        </span>
                                    )) : JSON.stringify(facilities)
                                ) : '-'}
                            </dd>
                        </div>
                    </dl>
                </div>

                {/* Location & Legal Docs */}
                <div className="bg-white shadow rounded-lg p-6 space-y-4 md:col-span-2">
                    <h3 className="text-lg font-medium text-gray-900 border-b pb-2 flex items-center">
                        <MapPin className="w-5 h-5 mr-2 text-indigo-500" /> Location & Legitimacy
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div>
                            <p className="text-sm font-medium text-gray-500 mb-2">Location (Front)</p>
                            {locationFront ? (
                                <div className="aspect-video w-full bg-gray-100 rounded-lg overflow-hidden border">
                                    <img src={locationFront} alt="Front Location" className="w-full h-full object-cover" />
                                </div>
                            ) : (
                                <div className="h-32 bg-gray-100 rounded-lg flex items-center justify-center text-gray-400 border border-dashed">
                                    No Photo
                                </div>
                            )}
                        </div>
                        <div>
                            <p className="text-sm font-medium text-gray-500 mb-2">Location (Inside)</p>
                            {locationInside ? (
                                <div className="aspect-video w-full bg-gray-100 rounded-lg overflow-hidden border">
                                    <img src={locationInside} alt="Inside Location" className="w-full h-full object-cover" />
                                </div>
                            ) : (
                                <div className="h-32 bg-gray-100 rounded-lg flex items-center justify-center text-gray-400 border border-dashed">
                                    No Photo
                                </div>
                            )}
                        </div>
                        <div>
                            <p className="text-sm font-medium text-gray-500 mb-2">NIB Document</p>
                            {nibUrl ? (
                                <div className="h-32 flex flex-col items-center justify-center border rounded-lg bg-gray-50 p-4">
                                    <FileText className="w-8 h-8 text-gray-400 mb-2" />
                                    <a href={nibUrl} target="_blank" rel="noopener noreferrer" className="text-sm text-indigo-600 hover:text-indigo-900 underline text-center">
                                        View NIB Document
                                    </a>
                                </div>
                            ) : (
                                <div className="h-32 bg-gray-100 rounded-lg flex items-center justify-center text-gray-400 border border-dashed">
                                    No Document
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}
