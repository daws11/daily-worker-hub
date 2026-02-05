"use client";

import React, { createContext, useContext, useState, useEffect } from 'react';

type Language = 'id' | 'en';
type Translations = Record<string, Record<string, string>>;

const translations: Translations = {
  id: {
    // Hero Section - Full Indonesian
    'hero.badge': 'Platform Community-Based #1 di Bali',
    'hero.headline': 'Hubungkan Talent dengan',
    'hero.headline.highlight': 'Peluang',
    'hero.feature.smartMatching': 'Smart AI Matching',
    'hero.feature.verifiedWorkers': 'Workers Terverifikasi',
    'hero.feature.instantPayments': 'Pembayaran Instan',
    'hero.feature.communitySupport': 'Dukungan Komunitas',
    'hero.feature.helpAvailable': 'Bantuan 24/7',
    'hero.cta.findJob': 'Cari Job Hari Ini',
    'hero.cta.postJob': 'Post Job Sekarang',
    'hero.stat.workers': 'Workers',
    'hero.stat.businesses': 'Bisnis',
    'hero.stat.jobs': 'Pekerjaan',

    // Navigation
    'nav.home': 'Beranda',
    'nav.features': 'Fitur',
    'nav.howItWorks': 'Cara Kerja',
    'nav.community': 'Komunitas',
    'nav.faq': 'FAQ',

    // Problem Section - Full Indonesian
    'problem.badge': 'Masalah',
    'problem.headline': 'Pernah Merasa',
    'problem.headline.highlight': 'Sendiri?',
    'problem.subtitle': 'Platform lain hanya menghubungkan. Kami membangun komunitas.',
    'problem.noCommunity.title': 'Tidak Ada Komunitas',
    'problem.noCommunity.description': 'Workers & bisnis merasa sendirian',
    'problem.noShow.title': 'Staff No-Show',
    'problem.noShow.description': 'Pembatalan tak terduga merugikan bisnis',
    'problem.limitedNetwork.title': 'Network Terbatas',
    'problem.limitedNetwork.description': 'Tidak ada referral atau koneksi terpercaya',
    'problem.different.title': 'Kami Berbeda',
    'problem.different.description': 'Komunitas dimana workers & bisnis saling mendukung, belajar, dan tumbuh bersama.',

    // For Workers - Full Indonesian
    'workers.badge': 'Untuk Workers',
    'workers.headline': 'Kenapa Workers',
    'workers.headline.highlight': 'Memilih Kami?',
    'workers.subtitle': 'Lebih dari sekadar platform job matching — kami adalah komunitas yang peduli pada pertumbuhan karier Anda',
    'workers.benefit.flexibleIncome': 'Penghasilan Fleksibel',
    'workers.benefit.flexibleIncome.desc': 'Kerja kapanpun mau, dapat sesuai kebutuhan. Kontrol penuh atas jadwal dan potensi penghasilan Anda.',
    'workers.benefit.communitySupport': 'Dukungan Komunitas',
    'workers.benefit.communitySupport.desc': 'Terhubung dengan 2000+ workers lainnya. Share tips, dapatkan saran, dan bangun persahabatan.',
    'workers.benefit.careerGrowth': 'Pertumbuhan Karier',
    'workers.benefit.careerGrowth.desc': 'Bangun reputasi, dapatkan rating lebih baik, dan buka peluang job premium.',
    'workers.benefit.perksRewards': 'Perks & Rewards',
    'workers.benefit.perksRewards.desc': 'Welcome pack, bonus referral, dan reward eksklusif member. Bergabung dan mulai dapat!',
    'workers.testimonials.title': 'Apa Kata Workers Kami?',
    'workers.download.title': 'Download App',
    'workers.download.headline': 'Mulai Karier Anda Sekarang!',
    'workers.download.description': 'Download Daily Worker Hub app dan bergabung dengan komunitas 2000+ workers. Gratis, tanpa biaya pendaftaran.',
    'workers.cta': 'Gabung Sekarang - Gratis!',

    // For Businesses - Full Indonesian
    'businesses.badge': 'Untuk Bisnis',
    'businesses.headline': 'Solusi Tepat untuk',
    'businesses.headline.highlight': 'Bisnis Hospitality',
    'businesses.subtitle': 'Temukan staff profesional, atur jadwal dengan mudah, dan fokus pada pertumbuhan bisnis Anda',
    'businesses.benefit.instantStaffing': 'Staffing Instan',
    'businesses.benefit.instantStaffing.desc': 'Dapatkan staff berkualitas & terverifikasi dalam hitungan menit. Tidak ada lagi proses hiring panjang.',
    'businesses.benefit.preVetted': 'Talent Ter-Vet',
    'businesses.benefit.preVetted.desc': 'Semua workers terverifikasi ID, pengalaman, dan riwayat rating.',
    'businesses.benefit.costEffective': 'Hemat Biaya',
    'businesses.benefit.costEffective.desc': 'Bayar hanya jam kerja. Tidak ada gaji, benefit, atau biaya overhead.',
    'businesses.benefit.easyManagement': 'Mudah Dikelola',
    'businesses.benefit.easyManagement.desc': 'Jadwalkan, lacak, dan bayar staff semuanya dari satu app yang intuitif.',
    'businesses.howItWorks': 'Mulai dalam 3 Langkah Mudah',
    'businesses.howItWorks.subtitle': 'Dari daftar ke staffing pertama, hanya butuh waktu kurang dari 5 menit',
    'businesses.step1.title': 'Buat Akun Bisnis',
    'businesses.step1.desc': 'Daftar gratis dalam 2 menit. Verifikasi bisnis Anda dan dapatkan akses ke 500+ workers.',
    'businesses.step2.title': 'Post Kebutuhan Job',
    'businesses.step2.desc': 'Tentukan role, tanggal, jam, dan rate. Algoritma kami akan matchkan worker terbaik.',
    'businesses.step3.title': 'Terima & Kelola',
    'businesses.step3.desc': 'Review aplikasi, konfirmasi worker, dan kelola staff semuanya dari app kami.',
    'businesses.testimonials.title': 'Apa Kata Bisnis Kami?',
    'businesses.pricing.title': 'Coba Gratis Tanpa Risiko',
    'businesses.pricing.subtitle': 'Daftar sekarang dan coba staffing platform kami selama 7 hari penuh. Tidak ada kartu kredit, tidak ada komitmen.',
    'businesses.pricing.badge': '7 Hari Gratis!',
    'businesses.pricing.commission': 'Komisi',
    'businesses.pricing.trial': 'Selama 7 hari trial',
    'businesses.pricing.after': 'Setelah trial:',
    'businesses.pricing.rate': 'Hanya 6% per job',
    'businesses.cta': 'Mulai Trial Gratis 7 Hari',

    // Footer
    'footer.quickLinks': 'Link Cepat',
    'footer.legal': 'Legal',
    'footer.about': 'Tentang',
    'footer.contact': 'Kontak',
    'footer.privacy': 'Kebijakan Privasi',
    'footer.terms': 'Syarat & Ketentuan',
    'footer.communityGuidelines': 'Panduan Komunitas',
    'footer.allRightsReserved': 'Semua Hak Dilindungi.',
  },
  en: {
    // Hero Section - Full English
    'hero.badge': '#1 Community-Based Platform in Bali',
    'hero.headline': 'Connect Talent with',
    'hero.headline.highlight': 'Opportunities',
    'hero.feature.smartMatching': 'Smart AI Matching',
    'hero.feature.verifiedWorkers': 'Verified Workers',
    'hero.feature.instantPayments': 'Instant Payments',
    'hero.feature.communitySupport': 'Community Support',
    'hero.feature.helpAvailable': '24/7 Help Available',
    'hero.cta.findJob': 'Find Job Today',
    'hero.cta.postJob': 'Post Job Now',
    'hero.stat.workers': 'Workers',
    'hero.stat.businesses': 'Businesses',
    'hero.stat.jobs': 'Jobs',

    // Navigation
    'nav.home': 'Home',
    'nav.features': 'Features',
    'nav.howItWorks': 'How It Works',
    'nav.community': 'Community',
    'nav.faq': 'FAQ',

    // Problem Section - Full English
    'problem.badge': 'The Problem',
    'problem.headline': 'Ever Feel',
    'problem.headline.highlight': 'Alone?',
    'problem.subtitle': 'Other platforms connect. We build community.',
    'problem.noCommunity.title': 'No Community',
    'problem.noCommunity.description': 'Workers & businesses feel alone',
    'problem.noShow.title': 'Staff No-Show',
    'problem.noShow.description': 'Unexpected cancellations hurt business',
    'problem.limitedNetwork.title': 'Limited Network',
    'problem.limitedNetwork.description': 'No referrals or trusted connections',
    'problem.different.title': 'We\'re Different',
    'problem.different.description': 'A community where workers & businesses support, learn, and grow together.',

    // For Workers - Full English
    'workers.badge': 'For Workers',
    'workers.headline': 'Why Workers Choose',
    'workers.headline.highlight': 'Us?',
    'workers.subtitle': 'More than just a job matching platform — we are a community that cares about your career growth',
    'workers.benefit.flexibleIncome': 'Flexible Income',
    'workers.benefit.flexibleIncome.desc': 'Work when you want, earn what you need. Complete control over your schedule and earning potential.',
    'workers.benefit.communitySupport': 'Community Support',
    'workers.benefit.communitySupport.desc': 'Connect with 2000+ fellow workers. Share tips, get advice, and build lasting friendships.',
    'workers.benefit.careerGrowth': 'Career Growth',
    'workers.benefit.careerGrowth.desc': 'Build your reputation, earn better ratings, and unlock premium job opportunities.',
    'workers.benefit.perksRewards': 'Perks & Rewards',
    'workers.benefit.perksRewards.desc': 'Welcome pack, referral bonuses, and exclusive member rewards. Join and start earning!',
    'workers.testimonials.title': 'What Workers Say About Us?',
    'workers.download.title': 'Download App',
    'workers.download.headline': 'Start Your Career Today!',
    'workers.download.description': 'Download Daily Worker Hub app and join our community of 2000+ workers. Free, no registration fee.',
    'workers.cta': 'Join Now - Free!',

    // For Businesses - Full English
    'businesses.badge': 'For Businesses',
    'businesses.headline': 'Perfect Solution for',
    'businesses.headline.highlight': 'Hospitality Business',
    'businesses.subtitle': 'Find professional staff, manage schedules easily, and focus on your business growth',
    'businesses.benefit.instantStaffing': 'Instant Staffing',
    'businesses.benefit.instantStaffing.desc': 'Get qualified, pre-vetted staff in minutes. No more long hiring processes.',
    'businesses.benefit.preVetted': 'Pre-Vetted Talent',
    'businesses.benefit.preVetted.desc': 'All workers verified with ID checks, experience, and rating history.',
    'businesses.benefit.costEffective': 'Cost Effective',
    'businesses.benefit.costEffective.desc': 'Pay only for hours worked. No salaries, benefits, or overhead costs.',
    'businesses.benefit.easyManagement': 'Easy Management',
    'businesses.benefit.easyManagement.desc': 'Schedule, track, and pay staff all from one intuitive app.',
    'businesses.howItWorks': 'Get Started in 3 Easy Steps',
    'businesses.howItWorks.subtitle': 'From sign up to first staffing, takes less than 5 minutes',
    'businesses.step1.title': 'Create Business Account',
    'businesses.step1.desc': 'Sign up free in 2 minutes. Verify your business and get access to 500+ workers.',
    'businesses.step2.title': 'Post Job Requirements',
    'businesses.step2.desc': 'Define role, date, time, and rate. Our algorithm will match you with best workers.',
    'businesses.step3.title': 'Accept & Manage',
    'businesses.step3.desc': 'Review applications, confirm workers, and manage staff all from our app.',
    'businesses.testimonials.title': 'What Businesses Say About Us?',
    'businesses.pricing.title': 'Try Free Without Risk',
    'businesses.pricing.subtitle': 'Sign up now and try our staffing platform for 7 days. No credit card, no commitment.',
    'businesses.pricing.badge': '7 Days Free!',
    'businesses.pricing.commission': 'Commission',
    'businesses.pricing.trial': 'During 7 days trial',
    'businesses.pricing.after': 'After trial:',
    'businesses.pricing.rate': 'Only 6% per job',
    'businesses.cta': 'Start 7 Days Free Trial',

    // Footer
    'footer.quickLinks': 'Quick Links',
    'footer.legal': 'Legal',
    'footer.about': 'About',
    'footer.contact': 'Contact',
    'footer.privacy': 'Privacy Policy',
    'footer.terms': 'Terms of Service',
    'footer.communityGuidelines': 'Community Guidelines',
    'footer.allRightsReserved': 'All Rights Reserved.',
  },
};

interface LanguageContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: string) => string;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [language, setLanguage] = useState<Language>('id');

  useEffect(() => {
    const savedLang = localStorage.getItem('language') as Language;
    if (savedLang && (savedLang === 'id' || savedLang === 'en')) {
      setLanguage(savedLang);
    }
  }, []);

  const handleSetLanguage = (lang: Language) => {
    setLanguage(lang);
    localStorage.setItem('language', lang);
  };

  const t = (key: string): string => {
    return translations[language][key] || key;
  };

  return (
    <LanguageContext.Provider value={{ language, setLanguage: handleSetLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (context === undefined) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
}
