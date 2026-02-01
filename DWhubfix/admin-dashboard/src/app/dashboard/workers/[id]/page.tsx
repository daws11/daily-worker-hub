
import { createClient } from '@/utils/supabase/server'
import { notFound } from 'next/navigation'
import { CheckCircle, XCircle, FileText, User, Briefcase, Calendar } from 'lucide-react'
import { approveWorker, rejectWorker } from '../actions'

export default async function WorkerDetailPage({ params }: { params: Promise<{ id: string }> }) {
    const id = (await params).id
    const supabase = await createClient()

    const { data: worker } = await supabase
        .from('profiles')
        .select(`
            *,
            worker_profiles (*),
            worker_skills (*)
        `)
        .eq('id', id)
        .single()

    if (!worker) {
        notFound()
    }

    const approveAction = approveWorker.bind(null, id)
    // For reject, treating it as a simple form action for now. 
    // A dynamic reason input would require a client component wrapper.
    const rejectAction = rejectWorker.bind(null, id, 'Rejected by admin')

    // Helper to extract flattened worker data
    const workerProfile = Array.isArray(worker.worker_profiles)
        ? worker.worker_profiles[0]
        : worker.worker_profiles

    // Skills can come from worker_skills table (new) or profiles (legacy)
    const skills = worker.worker_skills && worker.worker_skills.length > 0
        ? worker.worker_skills
        : worker.skills

    return (
        <div className="space-y-8 max-w-5xl mx-auto">
            <div className="flex justify-between items-start">
                <div>
                    <h2 className="text-3xl font-bold tracking-tight text-gray-900">{worker.full_name}</h2>
                    <p className="text-gray-500 mt-1">Submitted on {new Date(worker.created_at).toLocaleDateString()}</p>
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
                {/* Personal Info */}
                <div className="bg-white shadow rounded-lg p-6 space-y-4">
                    <h3 className="text-lg font-medium text-gray-900 border-b pb-2 flex items-center">
                        <User className="w-5 h-5 mr-2 text-indigo-500" /> Personal Information
                    </h3>
                    <dl className="grid grid-cols-1 gap-x-4 gap-y-4 sm:grid-cols-2">
                        <div className="sm:col-span-1">
                            <dt className="text-sm font-medium text-gray-500">Phone</dt>
                            <dd className="mt-1 text-sm text-gray-900">{worker.phone_number}</dd>
                        </div>
                        <div className="sm:col-span-1">
                            <dt className="text-sm font-medium text-gray-500">Address</dt>
                            <dd className="mt-1 text-sm text-gray-900">{workerProfile?.address || worker.address || 'Not provided'}</dd>
                        </div>
                        <div className="sm:col-span-2">
                            <dt className="text-sm font-medium text-gray-500">Bio/Description</dt>
                            <dd className="mt-1 text-sm text-gray-900">{worker.personal_description || worker.business_description || '-'}</dd>
                        </div>
                    </dl>
                </div>

                {/* Professional Info */}
                <div className="bg-white shadow rounded-lg p-6 space-y-4">
                    <h3 className="text-lg font-medium text-gray-900 border-b pb-2 flex items-center">
                        <Briefcase className="w-5 h-5 mr-2 text-indigo-500" /> Professional Profile
                    </h3>
                    <dl className="grid grid-cols-1 gap-x-4 gap-y-4 sm:grid-cols-2">
                        <div className="sm:col-span-1">
                            <dt className="text-sm font-medium text-gray-500">Category</dt>
                            <dd className="mt-1 text-sm text-gray-900">{workerProfile?.job_category || worker.job_category}</dd>
                        </div>
                        <div className="sm:col-span-1">
                            <dt className="text-sm font-medium text-gray-500">Role</dt>
                            <dd className="mt-1 text-sm text-gray-900">{workerProfile?.job_role || worker.job_role}</dd>
                        </div>
                        <div className="sm:col-span-1">
                            <dt className="text-sm font-medium text-gray-500">Years Experience</dt>
                            <dd className="mt-1 text-sm text-gray-900">{workerProfile?.years_experience || worker.years_experience} years</dd>
                        </div>
                        <div className="sm:col-span-2">
                            <dt className="text-sm font-medium text-gray-500">Skills</dt>
                            <dd className="mt-1 text-sm text-gray-900 flex flex-wrap gap-2">
                                {skills ? (
                                    Array.isArray(skills) ? skills.map((skill: any) => (
                                        <span key={skill.id || skill} className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800">
                                            {/* Handle both object from worker_skills (skill_name) and potentially legacy string/object */}
                                            {typeof skill === 'string' ? skill : (skill.skill_name || skill.name)}
                                        </span>
                                    )) : JSON.stringify(skills)
                                ) : '-'}
                            </dd>
                        </div>
                    </dl>
                </div>

                {/* Identity Verification */}
                <div className="bg-white shadow rounded-lg p-6 space-y-4 md:col-span-2">
                    <h3 className="text-lg font-medium text-gray-900 border-b pb-2 flex items-center">
                        <FileText className="w-5 h-5 mr-2 text-indigo-500" /> Identity Verification
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <p className="text-sm font-medium text-gray-500 mb-2">ID Card (KTP)</p>
                            {worker.id_card_url ? (
                                <div className="aspect-[1.58/1] w-full bg-gray-100 rounded-lg overflow-hidden border">
                                    <img src={worker.id_card_url} alt="ID Card" className="w-full h-full object-cover" />
                                </div>
                            ) : (
                                <div className="h-48 bg-gray-100 rounded-lg flex items-center justify-center text-gray-400 border border-dashed">
                                    No ID Card uploaded
                                </div>
                            )}
                        </div>
                        <div>
                            <p className="text-sm font-medium text-gray-500 mb-2">Selfie</p>
                            {worker.selfie_url ? (
                                <div className="aspect-square w-full max-w-xs mx-auto bg-gray-100 rounded-lg overflow-hidden border">
                                    <img src={worker.selfie_url} alt="Selfie" className="w-full h-full object-cover" />
                                </div>
                            ) : (
                                <div className="h-48 bg-gray-100 rounded-lg flex items-center justify-center text-gray-400 border border-dashed">
                                    No Selfie uploaded
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Documents */}
                <div className="bg-white shadow rounded-lg p-6 space-y-4 md:col-span-2">
                    <h3 className="text-lg font-medium text-gray-900 border-b pb-2 flex items-center">
                        <FileText className="w-5 h-5 mr-2 text-indigo-500" /> Supporting Documents
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {worker.experience_document_url && (
                            <div>
                                <p className="text-sm font-medium text-gray-500 mb-2">Experience Document</p>
                                <a href={worker.experience_document_url} target="_blank" rel="noopener noreferrer" className="text-indigo-600 hover:text-indigo-900 underline">
                                    View Document
                                </a>
                            </div>
                        )}
                        {worker.domicile_document_url && (
                            <div>
                                <p className="text-sm font-medium text-gray-500 mb-2">Domicile Document</p>
                                <a href={worker.domicile_document_url} target="_blank" rel="noopener noreferrer" className="text-indigo-600 hover:text-indigo-900 underline">
                                    View Document
                                </a>
                            </div>
                        )}
                        {!worker.experience_document_url && !worker.domicile_document_url && (
                            <p className="text-sm text-gray-500">No additional documents uploaded.</p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    )
}
