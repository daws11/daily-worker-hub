
import { createClient } from '@/utils/supabase/server'
import Link from 'next/link'
import { Eye } from 'lucide-react'

export default async function WorkerListPage() {
    const supabase = await createClient()

    // Fetch pending workers
    const { data: workers, error } = await supabase
        .from('profiles')
        .select(`
            *,
            worker_profiles (
                job_category,
                job_role
            )
        `)
        .eq('role', 'worker')
        .eq('verification_status', 'pending')
        // .eq('onboarding_status', 'completed') // Commented out for testing visibility
        .order('created_at', { ascending: false })

    if (error) {
        return <div className="text-red-500">Error loading workers</div>
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold tracking-tight text-gray-900">Worker Approvals</h2>
            </div>

            <div className="bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Name
                            </th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Job Category
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
                        {workers?.length === 0 ? (
                            <tr>
                                <td colSpan={4} className="px-6 py-4 text-center text-sm text-gray-500">
                                    No pending workers found.
                                </td>
                            </tr>
                        ) : (
                            workers?.map((worker) => {
                                const workerProfile = Array.isArray(worker.worker_profiles) 
                                    ? worker.worker_profiles[0] 
                                    : worker.worker_profiles
                                
                                const jobCategory = workerProfile?.job_category || worker.job_category
                                const jobRole = workerProfile?.job_role || worker.job_role

                                return (
                                <tr key={worker.id} className="hover:bg-gray-50 transition-colors">
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="flex items-center">
                                            <div className="h-10 w-10 flex-shrink-0">
                                                {worker.avatar_url ? (
                                                    <img className="h-10 w-10 rounded-full object-cover" src={worker.avatar_url} alt="" />
                                                ) : (
                                                    <div className="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center text-gray-500">
                                                        {worker.full_name?.charAt(0)}
                                                    </div>
                                                )}
                                            </div>
                                            <div className="ml-4">
                                                <div className="text-sm font-medium text-gray-900">{worker.full_name}</div>
                                                <div className="text-sm text-gray-500">{worker.phone_number}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="text-sm text-gray-900">{jobCategory || '-'}</div>
                                        <div className="text-sm text-gray-500">{jobRole || '-'}</div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {new Date(worker.created_at).toLocaleDateString()}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                        <Link
                                            href={`/dashboard/workers/${worker.id}`}
                                            className="text-indigo-600 hover:text-indigo-900 inline-flex items-center"
                                        >
                                            <Eye className="w-4 h-4 mr-1" />
                                            Review
                                        </Link>
                                    </td>
                                </tr>
                            )})
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    )
}
