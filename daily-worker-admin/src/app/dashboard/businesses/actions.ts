'use server'

import { createClient } from '@/utils/supabase/server'
import { revalidatePath } from 'next/cache'
import { redirect } from 'next/navigation'

export async function approveBusiness(id: string) {
    const supabase = await createClient()

    const { error } = await supabase
        .from('profiles')
        .update({ verification_status: 'verified' })
        .eq('id', id)

    if (error) {
        throw new Error('Failed to approve business')
    }

    revalidatePath('/dashboard/businesses')
    revalidatePath(`/dashboard/businesses/${id}`)
    redirect('/dashboard/businesses')
}

export async function rejectBusiness(id: string, reason: string) {
    const supabase = await createClient()

    const { error } = await supabase
        .from('profiles')
        .update({ verification_status: 'rejected' })
        .eq('id', id)

    if (error) {
        throw new Error('Failed to reject business')
    }

    revalidatePath('/dashboard/businesses')
    revalidatePath(`/dashboard/businesses/${id}`)
    redirect('/dashboard/businesses')
}
