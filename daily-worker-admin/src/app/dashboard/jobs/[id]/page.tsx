import { createClient } from '@/utils/supabase/server'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { 
  Briefcase, 
  MapPin, 
  Clock, 
  Calendar, 
  DollarSign, 
  User, 
  Building2,
  ArrowLeft,
  CheckCircle,
  XCircle,
  AlertTriangle
} from "lucide-react"
import Link from "next/link"
import { format, formatDistanceToNow } from "date-fns"
import { notFound } from "next/navigation"

interface Job {
  id: string
  title: string
  description: string
  category: string
  wage: number
  location: string
  start_time: string
  end_time: string
  status: string
  is_urgent: boolean
  is_compliant: boolean
  required_skills: string[]
  created_at: string
  business_profiles: {
    id: string
    business_name: string
    address: string
    phone: string
  } | null
  job_assignments: Array<{
    id: string
    status: string
    started_at: string | null
    completed_at: string | null
    hours_worked: number | null
    wage_paid: number | null
    worker_profiles: {
      id: string
      full_name: string
      avatar_url: string | null
      phone: string
      skills: string[]
      rating: number
      reliability_score: number
    } | null
  }>
  job_applications: Array<{
    id: string
    status: string
    match_score: number | null
    compliance_status: boolean | null
    applied_at: string
    worker_profiles: {
      id: string
      full_name: string
      avatar_url: string | null
      skills: string[]
      rating: number
      reliability_score: number
    } | null
  }>
}

export default async function JobDetailPage({
  params,
}: {
  params: { id: string }
}) {
  const supabase = await createClient()

  // Fetch job with related data
  const { data: job, error } = await supabase
    .from('jobs')
    .select(`
      *,
      business_profiles (
        id,
        business_name,
        address,
        phone
      ),
      job_assignments (
        id,
        status,
        started_at,
        completed_at,
        hours_worked,
        wage_paid,
        worker_profiles (
          id,
          full_name,
          avatar_url,
          phone,
          skills,
          rating,
          reliability_score
        )
      ),
      job_applications (
        id,
        status,
        match_score,
        compliance_status,
        applied_at,
        worker_profiles (
          id,
          full_name,
          avatar_url,
          skills,
          rating,
          reliability_score
        )
      )
    `)
    .eq('id', params.id)
    .single()

  if (error || !job) {
    notFound()
  }

  const categoryColors: Record<string, string> = {
    driver: 'bg-blue-100 text-blue-800',
    cleaner: 'bg-green-100 text-green-800',
    cook: 'bg-yellow-100 text-yellow-800',
    steward: 'bg-red-100 text-red-800',
  }

  const statusColors: Record<string, string> = {
    open: 'bg-blue-100 text-blue-800',
    pending: 'bg-yellow-100 text-yellow-800',
    accepted: 'bg-purple-100 text-purple-800',
    ongoing: 'bg-indigo-100 text-indigo-800',
    completed: 'bg-green-100 text-green-800',
    cancelled: 'bg-gray-100 text-gray-800',
  }

  const assignment = job.job_assignments?.[0]
  const worker = assignment?.worker_profiles

  // Calculate analytics
  const totalApplications = job.job_applications?.length || 0
  const pendingApplications = job.job_applications?.filter(a => a.status === 'pending').length || 0
  const avgMatchScore = job.job_applications?.reduce((sum, a) => sum + (a.match_score || 0), 0) / totalApplications || 0
  const timeToFill = assignment 
    ? Math.floor((new Date(assignment.started_at || job.start_time).getTime() - new Date(job.created_at).getTime()) / (1000 * 60 * 60))
    : null

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <Link href="/dashboard/jobs">
          <Button variant="ghost" size="sm">
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Jobs
          </Button>
        </Link>
      </div>

      <div>
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">{job.title}</h1>
            <div className="flex items-center space-x-2 mt-2">
              <Badge className={categoryColors[job.category] || 'bg-gray-100 text-gray-800'}>
                {job.category}
              </Badge>
              <Badge className={statusColors[job.status] || 'bg-gray-100 text-gray-800'}>
                {job.status}
              </Badge>
              {job.is_urgent && (
                <Badge variant="destructive">Urgent</Badge>
              )}
              {!job.is_compliant && (
                <Badge variant="secondary">
                  <AlertTriangle className="w-3 h-3 mr-1" />
                  Non-Compliant
                </Badge>
              )}
            </div>
          </div>
          <div className="text-right">
            <div className="text-3xl font-bold text-green-600">
              Rp {job.wage.toLocaleString('id-ID')}
            </div>
            <div className="text-sm text-gray-500">Total wage</div>
          </div>
        </div>
      </div>

      {/* Analytics Cards */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Applications</CardTitle>
            <User className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalApplications}</div>
            <p className="text-xs text-gray-500">{pendingApplications} pending</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Avg Match Score</CardTitle>
            <CheckCircle className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {avgMatchScore.toFixed(1)}%
            </div>
            <p className="text-xs text-gray-500">Smart matching</p>
          </CardContent>
        </Card>

        {timeToFill && (
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-gray-500">Time to Fill</CardTitle>
              <Clock className="h-4 w-4 text-purple-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{timeToFill}h</div>
              <p className="text-xs text-gray-500">From posting</p>
            </CardContent>
          </Card>
        )}

        {assignment && (
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-gray-500">Hours Worked</CardTitle>
              <Clock className="h-4 w-4 text-orange-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{assignment.hours_worked || 0}h</div>
              <p className="text-xs text-gray-500">
                {assignment.wage_paid ? 'Paid' : 'Unpaid'}
              </p>
            </CardContent>
          </Card>
        )}
      </div>

      {/* Job Details */}
      <div className="grid gap-6 md:grid-cols-2">
        {/* Job Info */}
        <Card>
          <CardHeader>
            <CardTitle>Job Information</CardTitle>
            <CardDescription>Details about the job posting</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-start">
              <Briefcase className="w-5 h-5 text-gray-400 mt-0.5 mr-3" />
              <div>
                <div className="text-sm font-medium text-gray-900">Category</div>
                <div className="text-sm text-gray-600 capitalize">{job.category}</div>
              </div>
            </div>

            <div className="flex items-start">
              <MapPin className="w-5 h-5 text-gray-400 mt-0.5 mr-3" />
              <div>
                <div className="text-sm font-medium text-gray-900">Location</div>
                <div className="text-sm text-gray-600">{job.location}</div>
              </div>
            </div>

            <div className="flex items-start">
              <Calendar className="w-5 h-5 text-gray-400 mt-0.5 mr-3" />
              <div>
                <div className="text-sm font-medium text-gray-900">Start Time</div>
                <div className="text-sm text-gray-600">
                  {format(new Date(job.start_time), 'PPp')}
                </div>
              </div>
            </div>

            <div className="flex items-start">
              <Clock className="w-5 h-5 text-gray-400 mt-0.5 mr-3" />
              <div>
                <div className="text-sm font-medium text-gray-900">End Time</div>
                <div className="text-sm text-gray-600">
                  {format(new Date(job.end_time), 'PPp')}
                </div>
              </div>
            </div>

            <Separator />

            <div>
              <div className="text-sm font-medium text-gray-900 mb-2">Description</div>
              <div className="text-sm text-gray-600">{job.description}</div>
            </div>

            {job.required_skills && job.required_skills.length > 0 && (
              <div>
                <div className="text-sm font-medium text-gray-900 mb-2">Required Skills</div>
                <div className="flex flex-wrap gap-2">
                  {job.required_skills.map((skill, index) => (
                    <Badge key={index} variant="outline">
                      {skill}
                    </Badge>
                  ))}
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Business Info */}
        <Card>
          <CardHeader>
            <CardTitle>Business Information</CardTitle>
            <CardDescription>Business that posted this job</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-start">
              <Building2 className="w-5 h-5 text-gray-400 mt-0.5 mr-3" />
              <div>
                <div className="text-sm font-medium text-gray-900">Business Name</div>
                <div className="text-sm text-gray-600">{job.business_profiles?.business_name || 'Unknown'}</div>
              </div>
            </div>

            {job.business_profiles?.address && (
              <div className="flex items-start">
                <MapPin className="w-5 h-5 text-gray-400 mt-0.5 mr-3" />
                <div>
                  <div className="text-sm font-medium text-gray-900">Address</div>
                  <div className="text-sm text-gray-600">{job.business_profiles.address}</div>
                </div>
              </div>
            )}

            {job.business_profiles?.phone && (
              <div className="flex items-start">
                <User className="w-5 h-5 text-gray-400 mt-0.5 mr-3" />
                <div>
                  <div className="text-sm font-medium text-gray-900">Phone</div>
                  <div className="text-sm text-gray-600">{job.business_profiles.phone}</div>
                </div>
              </div>
            )}

            <Separator />

            <div>
              <div className="text-sm font-medium text-gray-900 mb-2">Posted</div>
              <div className="text-sm text-gray-600">
                {formatDistanceToNow(new Date(job.created_at), { addSuffix: true })}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Worker Assignment */}
      {worker && (
        <Card>
          <CardHeader>
            <CardTitle>Assigned Worker</CardTitle>
            <CardDescription>Worker assigned to this job</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex items-start space-x-4">
              <div className="h-16 w-16 rounded-full bg-blue-100 flex items-center justify-center">
                {worker.avatar_url ? (
                  <img src={worker.avatar_url} alt="" className="h-16 w-16 rounded-full object-cover" />
                ) : (
                  <span className="text-2xl font-bold text-blue-600">
                    {worker.full_name.charAt(0)}
                  </span>
                )}
              </div>
              <div className="flex-1">
                <div className="flex items-center space-x-2">
                  <h3 className="text-lg font-medium text-gray-900">{worker.full_name}</h3>
                  {worker.rating && (
                    <Badge variant="outline" className="text-yellow-600 border-yellow-600">
                      ★ {worker.rating.toFixed(1)}
                    </Badge>
                  )}
                </div>
                <div className="text-sm text-gray-500 mt-1">
                  Reliability Score: {worker.reliability_score || 0}%
                </div>
                {worker.skills && worker.skills.length > 0 && (
                  <div className="flex flex-wrap gap-2 mt-2">
                    {worker.skills.map((skill, index) => (
                      <Badge key={index} variant="secondary" className="text-xs">
                        {skill}
                      </Badge>
                    ))}
                  </div>
                )}
              </div>
              <div className="text-right">
                <div className="text-sm font-medium text-gray-900">
                  Assignment Status
                </div>
                <Badge 
                  className={
                    assignment.status === 'completed' 
                      ? 'bg-green-100 text-green-800' 
                      : assignment.status === 'ongoing'
                      ? 'bg-blue-100 text-blue-800'
                      : 'bg-gray-100 text-gray-800'
                  }
                >
                  {assignment.status}
                </Badge>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Applications */}
      {job.job_applications && job.job_applications.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Applications ({job.job_applications.length})</CardTitle>
            <CardDescription>Workers who applied to this job</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {job.job_applications.map((application) => (
                <div key={application.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div className="flex items-center space-x-4">
                    <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center">
                      {application.worker_profiles?.avatar_url ? (
                        <img 
                          src={application.worker_profiles.avatar_url} 
                          alt="" 
                          className="h-12 w-12 rounded-full object-cover" 
                        />
                      ) : (
                        <span className="text-lg font-bold text-blue-600">
                          {application.worker_profiles?.full_name.charAt(0) || '?'}
                        </span>
                      )}
                    </div>
                    <div>
                      <div className="font-medium text-gray-900">
                        {application.worker_profiles?.full_name || 'Unknown'}
                      </div>
                      <div className="text-sm text-gray-500 flex items-center mt-1">
                        <CheckCircle className="w-3 h-3 mr-1" />
                        Match Score: {application.match_score || 0}%
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center space-x-4">
                    {application.worker_profiles?.rating && (
                      <Badge variant="outline" className="text-yellow-600 border-yellow-600">
                        ★ {application.worker_profiles.rating.toFixed(1)}
                      </Badge>
                    )}
                    <Badge 
                      className={
                        application.status === 'accepted'
                          ? 'bg-green-100 text-green-800'
                          : application.status === 'pending'
                          ? 'bg-yellow-100 text-yellow-800'
                          : application.status === 'rejected'
                          ? 'bg-red-100 text-red-800'
                          : 'bg-gray-100 text-gray-800'
                      }
                    >
                      {application.status}
                    </Badge>
                    {!application.compliance_status && (
                      <Badge variant="secondary">
                        <AlertTriangle className="w-3 h-3 mr-1" />
                        Non-Compliant
                      </Badge>
                    )}
                    <div className="text-xs text-gray-500">
                      {formatDistanceToNow(new Date(application.applied_at), { addSuffix: true })}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
