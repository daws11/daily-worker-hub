
import { createClient } from '@/utils/supabase/server'
import Link from 'next/link'
import { Eye, MapPin } from 'lucide-react'

export default async function BusinessListPage() {
    const supabase = await createClient()

    // Fetch pending businesses
    const { data: businesses, error } = await supabase
        .from('profiles')
        .select(`
            *,
            business_profiles (
                business_name,
                job_category,
                address
            )
        `)
        .eq('role', 'business')
        .eq('verification_status', 'pending')
        // .eq('onboarding_status', 'completed')
        .order('created_at', { ascending: false })

    if (error) {
        return <div className="text-red-500">Error loading businesses</div>
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold tracking-tight text-gray-900">Business Approvals</h2>
            </div>

            <div className="bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Business Info
                            </th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Category
                            </th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Location
                            </th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Submission Date
                            </th>
                            <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Action
                            </th>
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {businesses?.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="px-6 py-4 text-center text-sm text-gray-500">
                                    No pending businesses found.
                                </td>
                            </tr>
                        ) : (
                            businesses?.map((business) => {
                                const businessProfile = Array.isArray(business.business_profiles)
                                    ? business.business_profiles[0]
                                    : business.business_profiles

                                const businessName = businessProfile?.business_name || business.business_name
                                const jobCategory = businessProfile?.job_category || business.job_category
                                const address = businessProfile?.address || business.address

                                return (
                                    <tr key={business.id} className="hover:bg-gray-50 transition-colors">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="flex items-center">
                                                <div className="h-10 w-10 flex-shrink-0">
                                                    {business.avatar_url ? (
                                                        <img className="h-10 w-10 rounded-full object-cover" src={business.avatar_url} alt="" />
                                                    ) : (
                                                        <div className="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-500 font-bold">
                                                            {businessName?.charAt(0) || 'B'}
                                                        </div>
                                                    )}
                                                </div>
                                                <div className="ml-4">
                                                    <div className="text-sm font-medium text-gray-900">{businessName}</div>
                                                    <div className="text-sm text-gray-500">{business.full_name} (Owner)</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {jobCategory || '-'}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm text-gray-900 flex items-center">
                                                <MapPin className="w-3 h-3 mr-1 text-gray-400" />
                                                {address ? (address.length > 20 ? address.substring(0, 20) + '...' : address) : '-'}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {new Date(business.created_at).toLocaleDateString()}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                            <Link
                                                href={`/dashboard/businesses/${business.id}`}
                                                className="text-indigo-600 hover:text-indigo-900 inline-flex items-center"
                                            >
                                                <Eye className="w-4 h-4 mr-1" />
                                                Review
                                            </Link>
                                        </td>
                                    </tr>
                                )
                            })
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    )
}
