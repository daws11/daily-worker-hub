/**
 * HowItWorks Section
 * Community-centric steps for both workers and businesses
 */

'use client';

import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { UserPlus, Briefcase, CheckCircle, Clock, Wallet, Star, ArrowRight, Users } from 'lucide-react';

type TabType = 'workers' | 'businesses';

export function HowItWorks() {
  const [activeTab, setActiveTab] = useState<TabType>('workers');

  const workerSteps = [
    {
      icon: UserPlus,
      title: 'Register & Join Community',
      description: 'Upload foto & dokumen, otomatis bergabung ke komunitas. Dapat welcome pack & onboarding tips.',
      time: '30 detik',
      color: 'from-primary/20 to-primary/5',
      iconColor: 'text-primary',
      subPoints: ['Foto & dokumen upload', 'Auto-join komunitas', 'Welcome pack & tips'],
    },
    {
      icon: Briefcase,
      title: 'Connect & Apply',
      description: 'Cari job sesuai lokasi & skill. Diskusi dengan workers lain di komunitas dan dapat rekomendasi.',
      time: 'Real-time',
      color: 'from-accent/20 to-accent/5',
      iconColor: 'text-accent',
      subPoints: ['Job search by location & skill', 'Komunitas diskusi', 'Rekomendasi dari members'],
    },
    {
      icon: CheckCircle,
      title: 'Grow & Earn',
      description: 'Dapatkan rating untuk setiap job, payment otomatis ke wallet, dan bangun reputasi di komunitas.',
      time: 'Instant',
      color: 'from-community/20 to-community/5',
      iconColor: 'text-community',
      subPoints: ['Job rating system', 'Auto-payment ke wallet', 'Bangun reputasi komunitas'],
    },
  ];

  const businessSteps = [
    {
      icon: Briefcase,
      title: 'Post Job & Connect',
      description: 'Tentukan kebutuhan & budget. Share ke komunitas untuk lebih banyak applicants.',
      time: '2 menit',
      color: 'from-primary/20 to-primary/5',
      iconColor: 'text-primary',
      subPoints: ['Quick job posting', 'Tentukan kebutuhan & budget', 'Share ke komunitas'],
    },
    {
      icon: UserPlus,
      title: 'Find & Match Talent',
      description: 'Lihat rating & review dari komunitas. Filter berdasarkan skill & pengalaman.',
      time: 'Real-time',
      color: 'from-accent/20 to-accent/5',
      iconColor: 'text-accent',
      subPoints: ['Lihat komunitas rating & review', 'Filter by skill & pengalaman', 'Smart matching system'],
    },
    {
      icon: Star,
      title: 'Build Your Network',
      description: 'Rate workers, build roster favorites, dan join business networking di komunitas.',
      time: 'Ongoing',
      color: 'from-community/20 to-community/5',
      iconColor: 'text-community',
      subPoints: ['Rate workers & feedback', 'Build roster favorites', 'Join business networking'],
    },
  ];

  const steps = activeTab === 'workers' ? workerSteps : businessSteps;

  return (
    <section className="py-32 relative overflow-hidden">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-background via-secondary/20 to-background" />

      <div className="container mx-auto px-4 relative z-10">
        {/* Section header */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-100px' }}
          transition={{ duration: 0.6 }}
          className="text-center mb-16"
        >
          <div className="inline-flex items-center gap-2 mb-6">
            <div className="w-1 h-1 rounded-full bg-primary" />
            <span className="text-sm font-semibold text-primary uppercase tracking-wider">How It Works</span>
            <div className="w-1 h-1 rounded-full bg-primary" />
          </div>

          <h2 className="text-5xl md:text-7xl font-bold mb-6">
            Cara Kerja{' '}
            <span className="bg-gradient-to-r from-foreground via-primary to-accent bg-clip-text text-transparent">
              Community-Centric
            </span>
          </h2>

          <p className="text-xl text-muted-foreground max-w-3xl mx-auto">
            Simpel, cepat, dan berbasis komunitas
          </p>
        </motion.div>

        {/* Tab switcher */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="flex justify-center gap-4 mb-16"
        >
          {[
            { id: 'workers' as TabType, label: 'Untuk Workers', icon: UserPlus },
            { id: 'businesses' as TabType, label: 'Untuk Businesses', icon: Briefcase },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`relative px-8 py-4 rounded-full font-semibold text-base transition-all duration-300 ${
                activeTab === tab.id
                  ? 'bg-gradient-to-r from-primary to-accent text-white shadow-lg shadow-primary/25 scale-105'
                  : 'bg-background text-foreground border-2 border-border/50 hover:border-primary/50 hover:bg-secondary/30'
              }`}
            >
              <span className="relative z-10 flex items-center gap-3">
                <tab.icon className="h-5 w-5" />
                {tab.label}
              </span>
              {activeTab === tab.id && (
                <motion.div
                  layoutId="activeTab"
                  className="absolute inset-0 rounded-full"
                  initial={false}
                  transition={{ type: 'spring', stiffness: 500, damping: 30 }}
                />
              )}
            </button>
          ))}
        </motion.div>

        {/* Steps */}
        <AnimatePresence mode="wait">
          <motion.div
            key={activeTab}
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            transition={{ duration: 0.4 }}
            className="grid grid-cols-1 md:grid-cols-3 gap-8"
          >
            {steps.map((step, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -30 }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                whileHover={{ scale: 1.03, y: -8 }}
                className="relative group"
              >
                <div className="absolute inset-0 bg-gradient-to-br from-card via-card to-secondary/50 rounded-3xl blur-xl group-hover:opacity-80 transition-opacity" />
                <div className="relative bg-gradient-to-br bg-card rounded-3xl p-8 border border-border/50 group-hover:border-primary/30 transition-all duration-300">
                  {/* Step number */}
                  <div className="absolute -top-4 -left-4 w-14 h-14 rounded-full bg-gradient-to-br from-primary to-accent flex items-center justify-center text-white font-bold text-xl shadow-lg z-10">
                    {index + 1}
                  </div>

                  {/* Icon */}
                  <div className={`w-16 h-16 rounded-2xl bg-gradient-to-br ${step.color} flex items-center justify-center mb-6 group-hover:scale-110 transition-transform`}>
                    <step.icon className={`h-8 w-8 ${step.iconColor}`} />
                  </div>

                  {/* Content */}
                  <h3 className="text-2xl font-bold mb-4">{step.title}</h3>
                  <p className="text-lg text-muted-foreground leading-relaxed mb-6">
                    {step.description}
                  </p>

                  {/* Sub points bullet list */}
                  {step.subPoints && (
                    <ul className="space-y-2 mb-6">
                      {step.subPoints.map((point, i) => (
                        <li key={i} className="flex items-start gap-3">
                          <div className="w-1.5 h-1.5 rounded-full bg-primary/60 mt-2.5 shrink-0" />
                          <span className="text-base text-foreground">{point}</span>
                        </li>
                      ))}
                    </ul>
                  )}

                  {/* Time badge */}
                  <div className="flex items-center gap-2 text-sm font-semibold text-foreground">
                    <Clock className="h-4 w-4 text-primary" />
                    <span>{step.time}</span>
                  </div>

                  {/* Arrow connector (for desktop) */}
                  {index < steps.length - 1 && (
                    <motion.div
                      className="hidden md:block absolute -right-4 top-1/2 -translate-y-1/2"
                      animate={{ x: [0, 10, 0] }}
                      transition={{
                        duration: 2,
                        repeat: Infinity,
                        ease: 'easeInOut',
                      }}
                    >
                      <ArrowRight className="h-6 w-6 text-muted-foreground" />
                    </motion.div>
                  )}
                </div>
              </motion.div>
            ))}
          </motion.div>
        </AnimatePresence>

        {/* Highlight card */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-100px' }}
          transition={{ duration: 0.6, delay: 0.4 }}
          className="mt-20"
        >
          <div className="relative bg-gradient-to-br from-card via-card to-primary/5 backdrop-blur-md rounded-3xl p-12 border border-border/50 overflow-hidden">
            {/* Animated gradient overlay */}
            <motion.div
              className="absolute inset-0 bg-gradient-to-r from-primary/5 via-accent/5 to-community/5"
              animate={{
                opacity: [0.3, 0.6, 0.3],
              }}
              transition={{
                duration: 4,
                repeat: Infinity,
                ease: 'easeInOut',
              }}
            />

            <div className="relative z-10 grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
              <div>
                <div className="inline-flex items-center gap-2 mb-6">
                  <Wallet className="h-6 w-6 text-primary" />
                  <span className="text-sm font-semibold text-primary uppercase tracking-wider">
                    Community Benefits
                  </span>
                </div>

                <h3 className="text-4xl md:text-5xl font-bold mb-6">
                  Lebih Dari Sekadar{' '}
                  <span className="bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">
                    Matching
                  </span>
                </h3>

                <ul className="space-y-4">
                  {[
                    'Instant job matching dengan smart algorithm',
                    'Real-time payments ke digital wallet',
                    '24/7 community support & knowledge sharing',
                    'Verified profiles untuk peace of mind',
                    'Build network dengan workers & businesses',
                  ].map((item, index) => (
                    <motion.li
                      key={index}
                      initial={{ opacity: 0, x: -20 }}
                      whileInView={{ opacity: 1, x: 0 }}
                      viewport={{ once: true }}
                      transition={{ duration: 0.5, delay: 0.5 + index * 0.1 }}
                      className="flex items-start gap-4"
                    >
                      <CheckCircle className="h-6 w-6 text-green-500 shrink-0 mt-1" />
                      <span className="text-lg text-foreground">{item}</span>
                    </motion.li>
                  ))}
                </ul>
              </div>

              <div className="text-center">
                <div className="inline-flex flex-col items-center gap-6 p-8 bg-background/80 backdrop-blur-sm rounded-3xl border border-border/50">
                  <div className="w-20 h-20 rounded-full bg-gradient-to-br from-primary/20 to-accent/20 flex items-center justify-center">
                    <Users className="h-10 w-10 text-primary" />
                  </div>
                  <div>
                    <p className="text-5xl font-bold mb-2">
                      <span className="bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">
                        2000+
                      </span>
                    </p>
                    <p className="text-lg text-muted-foreground">Community Members</p>
                  </div>
                  <button className="w-full px-8 py-4 bg-gradient-to-r from-primary to-accent text-white font-semibold rounded-full hover:scale-105 transition-transform">
                    Join Community
                    <ArrowRight className="inline-block h-5 w-5 ml-2" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  );
}
