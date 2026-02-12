'use server'

import { createClient } from '@/utils/supabase/server'
import { revalidatePath } from 'next/cache'
import { redirect } from 'next/navigation'

export async function approveWorker(id: string) {
    const supabase = await createClient()

    const { error } = await supabase
        .from('profiles')
        .update({ verification_status: 'verified' })
        .eq('id', id)

    if (error) {
        throw new Error('Failed to approve worker')
    }

    revalidatePath('/dashboard/workers')
    revalidatePath(`/dashboard/workers/${id}`)
    redirect('/dashboard/workers')
}

export async function rejectWorker(id: string, reason: string) {
    const supabase = await createClient()

    // Note: reason tracking might need another table or an audit log, 
    // but for now we just update the status as per brief.
    // Ideally, 'rejected_reason' column should exist or we send an email.
    // I will just update the status for now as requested.

    const { error } = await supabase
        .from('profiles')
        .update({ verification_status: 'rejected' })
        .eq('id', id)

    if (error) {
        throw new Error('Failed to reject worker')
    }

    revalidatePath('/dashboard/workers')
    revalidatePath(`/dashboard/workers/${id}`)
    redirect('/dashboard/workers')
}
