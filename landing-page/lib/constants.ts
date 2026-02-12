/**
 * Daily Worker Hub - Landing Page Constants
 * Brand colors, fonts, and other configuration values
 */

// Color Palette (matching mobile app)
export const COLORS = {
  // Primary Brand Colors
  primary: '#13EC5B', // Vibrant Green - semangat & pertumbuhan
  backgroundLight: '#F6F8F6', // Soft Greenish White
  backgroundDark: '#102216', // Deep Green - premium & trustworthy
  textLight: '#111813', // Dark Green/Black
  textDark: '#FFFFFF', // White
  textSecondary: '#61896F', // Muted Green - untuk secondary text

  // Accent Colors
  accent: '#FF6B35', // Warm Orange - untuk CTAs & highlights
  community: '#3B82F6', // Blue - untuk community features
  blue: '#3B82F6', // Blue - untuk CTAs & highlights
  purple: '#8B5CF6', // Purple - untuk highlights
} as const;

// Statistics for the landing page
export const STATS = {
  workers: 500,
  businesses: 120,
  jobsCompleted: 2000,
  averageRating: 4.8,

  // Community stats
  communityMembers: 2000,
  dailyActiveDiscussions: 150,
  resourcesShared: 50,
  satisfactionRate: 98,
} as const;

// Feature data
export const FEATURES = [
  {
    icon: 'Search',
    title: 'Smart Job Matching',
    description: 'Algoritma kami mencarikan worker paling cocok untuk kebutuhanmu',
    color: 'from-primary/20 to-primary/5',
    iconColor: 'text-primary',
  },
  {
    icon: 'ShieldCheck',
    title: 'Verified & Trusted Profiles',
    description: 'Semua worker terverifikasi dokumen & pengalaman untuk peace of mind',
    color: 'from-accent/20 to-accent/5',
    iconColor: 'text-accent',
  },
  {
    icon: 'Wallet',
    title: 'Instant & Secure Payments',
    description: 'Payment otomatis ke wallet. Tidak ada lagi gaji tertunda',
    color: 'from-community/20 to-community/5',
    iconColor: 'text-community',
  },
  {
    icon: 'Globe',
    title: 'PKHL Compliant Operations',
    description: 'Otomatis mematuhi regulasi PKHL Bali. Bebas dari legal risk',
    color: 'from-green-500/20 to-green-500/5',
    iconColor: 'text-green-500',
  },
  {
    icon: 'Star',
    title: 'Two-Way Rating System',
    description: 'Sistem rating transparan untuk kualitas yang lebih baik',
    color: 'from-yellow-500/20 to-yellow-500/5',
    iconColor: 'text-yellow-500',
  },
  {
    icon: 'Sparkles',
    title: 'Community Hub ⭐',
    description: 'Forum diskusi, resources sharing, networking events, dan support 24/7',
    color: 'from-community/20 to-primary/5',
    iconColor: 'text-community',
    highlight: true, // Highlight this as key feature
  },
] as const;

// FAQ Data
export const FAQS = {
  businesses: [
    {
      question: 'Apakah ada biaya untuk mendaftar bisnis?',
      answer: 'Tidak ada biaya pendaftaran untuk bisnis. Anda hanya membayar komisi 6% untuk setiap job yang berhasil diselesaikan.',
    },
    {
      question: 'Bagaimana sistem pembayaran bekerja?',
      answer: 'Payment otomatis ke wallet worker setelah job selesai. Bisnis deposit saldo ke wallet terlebih dahulu, dan sistem akan mendebit otomatis berdasarkan hourly rate yang disepakati.',
    },
    {
      question: 'Bagaimana jika worker tidak datang?',
      answer: 'Jika worker tidak datang tanpa konfirmasi (no-show), Anda bisa melaporkan di aplikasi dan saldo akan dikembalikan. Worker tersebut juga akan mendapatkan penalti pada no-show rate mereka.',
    },
    {
      question: 'Apa manfaat bergabung di komunitas?',
      answer: 'Komunitas kami memungkinkan Anda terhubung dengan bisnis hospitality lain, berbagi best practices, mendapatkan rekomendasi staff terpercaya, dan ikut networking events eksklusif.',
    },
    {
      question: 'Bagaimana community platform membantu bisnis saya?',
      answer: 'Di community platform, Anda bisa berdiskusi dengan bisnis lain, mendapatkan insights dari industry professionals, dan bahkan mencari partners untuk peak season.',
    },
  ],
  workers: [
    {
      question: 'Apakah ada biaya untuk download app?',
      answer: 'Daily Worker Hub app sepenuhnya gratis untuk didownload dan digunakan. Tidak ada biaya pendaftaran atau membership fee.',
    },
    {
      question: 'Bagaimana cara saya mendapatkan job?',
      answer: 'Cukup buat profile, cantumkan skills & pengalaman, lalu apply untuk job yang sesuai dengan kriteria Anda. Algoritma kami juga akan merekomendasikan job yang cocok.',
    },
    {
      question: 'Kapan saya akan dibayar?',
      answer: 'Payment otomatis ke wallet Anda setelah job selesai. Anda bisa withdraw kapan saja ke rekening bank atau e-wallet.',
    },
    {
      question: 'Apa itu 21 Days Rule dan bagaimana pengaruhnya?',
      answer: 'Sesuai regulasi PKHL Bali, worker maksimal bisa bekerja 20 hari untuk satu business dalam 30 hari. Sistem kami otomatis memblokir aplikasi jika sudah mencapai batas, agar Anda tetap compliant.',
    },
    {
      question: 'Bagaimana cara memaksimalkan benefit komunitas?',
      answer: 'Bergabunglah di forum diskusi, share pengalaman, minta tips dari workers berpengalaman, ikut webinar, dan bangun network dengan workers lain untuk mendapatkan rekomendasi job.',
    },
  ],
  general: [
    {
      question: 'Apa bedanya Daily Worker Hub dengan platform lain?',
      answer: 'Kami bukan sekadar marketplace. Kami membangun komunitas berbasis trust dimana workers dan businesses saling mendukung, belajar, dan tumbuh bersama. Platform kami juga fully compliant dengan regulasi PKHL Bali.',
    },
  ],
} as const;

// Testimonials
export const TESTIMONIALS = {
  workers: [
    {
      name: 'Ketut Dewi',
      role: 'Daily Worker',
      content: 'Di Daily Worker Hub, saya tidak cuma dapat job — saya juga temukan mentor, teman-teman yang share tips, bahkan rekomendasi job dari businesses yang mereka kenal. Kita saling bantu!',
      rating: 5,
      avatar: '👩‍💼',
    },
  ],
  businesses: [
    {
      name: 'Made Suryawan',
      role: 'Manager, Hotel Ubud',
      content: 'Daily Worker Hub membantu kami tidak hanya menemukan staff, tapi juga connect dengan businesses lain di Bali. Kami share best practices, bahkan partner dengan cafe lain saat busy season. This is truly a community!',
      rating: 5,
      avatar: '👨‍💼',
    },
  ],
} as const;

// Social links
export const SOCIAL_LINKS = {
  instagram: 'https://instagram.com/dailyworkerhub',
  facebook: 'https://facebook.com/dailyworkerhub',
  linkedin: 'https://linkedin.com/company/dailyworkerhub',
  whatsapp: 'https://wa.me/6281234567890',
} as const;

// Contact info
export const CONTACT_INFO = {
  email: 'hello@dailyworkerhub.id',
  phone: '+62 812 3456 7890',
  address: 'Jalan Raya Ubud No. 123, Bali, Indonesia',
} as const;

// App store URLs (placeholder - update with actual URLs when available)
export const APP_STORES = {
  googlePlay: 'https://play.google.com/store/apps/details?id=com.dailyworkerhub',
  appleAppStore: 'https://apps.apple.com/app/daily-worker-hub',
} as const;

// Navigation links
export const NAV_LINKS = [
  { label: 'Home', href: '#home' },
  { label: 'Fitur', href: '#features' },
  { label: 'Cara Kerja', href: '#how-it-works' },
  { label: 'Komunitas', href: '#community' },
  { label: 'FAQ', href: '#faq' },
] as const;

// Footer links
export const FOOTER_LINKS = {
  quickLinks: [
    { label: 'About', href: '#about' },
    { label: 'FAQ', href: '#faq' },
    { label: 'Contact', href: '#contact' },
    { label: 'Community Platform', href: 'https://community.dailyworkerhub.id' },
  ],
  legal: [
    { label: 'Privacy Policy', href: '/privacy' },
    { label: 'Terms of Service', href: '/terms' },
    { label: 'Community Guidelines', href: '/community-guidelines' },
  ],
} as const;

// Typography
export const TYPOGRAPHY = {
  heading: {
    fontFamily: 'var(--font-geist-sans)',
    fontWeight: 'bold',
  },
  body: {
    fontFamily: 'var(--font-geist-sans)',
  },
} as const;
