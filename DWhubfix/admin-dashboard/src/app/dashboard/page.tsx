
import { createClient } from '@/utils/supabase/server'
// import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card' // Removed unused import
import { Users, UserCheck, Building2, AlertCircle } from 'lucide-react'

export default async function DashboardPage() {
    const supabase = await createClient()

    // Fetch Stats
    const { count: pendingCount } = await supabase
        .from('profiles')
        .select('*', { count: 'exact', head: true })
        .eq('verification_status', 'pending')

    const { count: verifiedWorkerCount } = await supabase
        .from('profiles')
        .select('*', { count: 'exact', head: true })
        .eq('role', 'worker')
        .eq('verification_status', 'verified')

    const { count: verifiedBusinessCount } = await supabase
        .from('profiles')
        .select('*', { count: 'exact', head: true })
        .eq('role', 'business')
        .eq('verification_status', 'verified')

    return (
        <div className="space-y-8">
            <div>
                <h2 className="text-3xl font-bold tracking-tight text-gray-900">Dashboard</h2>
                <p className="text-gray-500">Overview of verification status.</p>
            </div>

            <div className="grid gap-4 md:grid-cols-3">
                {/* Pending Approval */}
                <div className="rounded-xl border bg-white text-card-foreground shadow">
                    <div className="p-6 flex flex-row items-center justify-between space-y-0 pb-2">
                        <h3 className="tracking-tight text-sm font-medium text-gray-500">
                            Pending Approvals
                        </h3>
                        <AlertCircle className="h-4 w-4 text-orange-500" />
                    </div>
                    <div className="p-6 pt-0">
                        <div className="text-2xl font-bold">{pendingCount || 0}</div>
                        <p className="text-xs text-gray-500">
                            Users waiting for verification
                        </p>
                    </div>
                </div>

                {/* Verified Workers */}
                <div className="rounded-xl border bg-white text-card-foreground shadow">
                    <div className="p-6 flex flex-row items-center justify-between space-y-0 pb-2">
                        <h3 className="tracking-tight text-sm font-medium text-gray-500">
                            Verified Workers
                        </h3>
                        <UserCheck className="h-4 w-4 text-green-500" />
                    </div>
                    <div className="p-6 pt-0">
                        <div className="text-2xl font-bold">{verifiedWorkerCount || 0}</div>
                        <p className="text-xs text-gray-500">
                            Active verified workers
                        </p>
                    </div>
                </div>

                {/* Verified Businesses */}
                <div className="rounded-xl border bg-white text-card-foreground shadow">
                    <div className="p-6 flex flex-row items-center justify-between space-y-0 pb-2">
                        <h3 className="tracking-tight text-sm font-medium text-gray-500">
                            Verified Businesses
                        </h3>
                        <Building2 className="h-4 w-4 text-blue-500" />
                    </div>
                    <div className="p-6 pt-0">
                        <div className="text-2xl font-bold">{verifiedBusinessCount || 0}</div>
                        <p className="text-xs text-gray-500">
                            Active verified businesses
                        </p>
                    </div>
                </div>
            </div>
        </div>
    )
}
