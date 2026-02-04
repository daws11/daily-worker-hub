import Link from 'next/link'
import { LayoutDashboard, Users, Building2, Briefcase, BarChart3, LogOut } from 'lucide-react'
import { createClient } from '@/utils/supabase/server'
import { redirect } from 'next/navigation'

export default async function DashboardLayout({
    children,
}: {
    children: React.ReactNode
}) {
    const supabase = await createClient()

    const {
        data: { user },
    } = await supabase.auth.getUser()

    if (!user) {
        redirect('/login')
    }

    const signOut = async () => {
        'use server'
        const supabase = await createClient()
        await supabase.auth.signOut()
        redirect('/login')
    }

    return (
        <div className="flex h-screen bg-gray-100">
            {/* Sidebar */}
            <aside className="w-64 bg-white shadow-md hidden md:block">
                <div className="p-6">
                    <h1 className="text-2xl font-bold text-gray-800">DWH Admin</h1>
                </div>
                <nav className="mt-6">
                    <Link
                        href="/dashboard"
                        className="flex items-center px-6 py-3 text-gray-700 hover:bg-gray-100 hover:text-indigo-600 transition-colors"
                    >
                        <LayoutDashboard className="w-5 h-5 mr-3" />
                        Overview
                    </Link>
                    <Link
                        href="/dashboard/workers"
                        className="flex items-center px-6 py-3 text-gray-700 hover:bg-gray-100 hover:text-indigo-600 transition-colors"
                    >
                        <Users className="w-5 h-5 mr-3" />
                        Workers
                    </Link>
                    <Link
                        href="/dashboard/businesses"
                        className="flex items-center px-6 py-3 text-gray-700 hover:bg-gray-100 hover:text-indigo-600 transition-colors"
                    >
                        <Building2 className="w-5 h-5 mr-3" />
                        Businesses
                    </Link>
                    <Link
                        href="/dashboard/jobs"
                        className="flex items-center px-6 py-3 text-gray-700 hover:bg-gray-100 hover:text-indigo-600 transition-colors"
                    >
                        <Briefcase className="w-5 h-5 mr-3" />
                        Jobs
                    </Link>
                    <Link
                        href="/dashboard/analytics"
                        className="flex items-center px-6 py-3 text-gray-700 hover:bg-gray-100 hover:text-indigo-600 transition-colors"
                    >
                        <BarChart3 className="w-5 h-5 mr-3" />
                        Analytics
                    </Link>
                </nav>
                <div className="absolute bottom-0 w-64 p-6 border-t border-gray-200">
                    <div className="flex items-center mb-4">
                        <div className="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-bold">
                            {user.email?.charAt(0).toUpperCase()}
                        </div>
                        <div className="ml-3">
                            <p className="text-sm font-medium text-gray-700 truncate w-40">
                                {user.email}
                            </p>
                        </div>
                    </div>
                    <form action={signOut}>
                        <button
                            type="submit"
                            className="flex items-center w-full px-4 py-2 text-sm text-red-600 bg-red-50 rounded-md hover:bg-red-100"
                        >
                            <LogOut className="w-4 h-4 mr-2" />
                            Sign Out
                        </button>
                    </form>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 overflow-y-auto p-8">{children}</main>
        </div>
    )
}
