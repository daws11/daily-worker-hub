"use client";

import { motion } from 'framer-motion';
import { BentoGrid, BentoGridItem } from '@/components/ui/bento-grid';
import { TextGenerateEffect } from '@/components/ui/text-generate-effect';
import { Search, ShieldCheck, Wallet, Globe, Star, Sparkles, Zap, GraduationCap, BarChart3 } from 'lucide-react';
import { FEATURES } from '@/lib/constants';

// Simplified features with shorter copy - configured for BentoGrid
const simplifiedFeatures = [
  {
    icon: Zap,
    title: 'Smart Matching',
    description: 'AI finds the perfect worker for your job',
    color: 'from-green-500 to-emerald-500',
    bgColor: 'bg-green-50',
    className: 'md:col-span-2'
  },
  {
    icon: ShieldCheck,
    title: 'Verified Profiles',
    description: 'ID-checked workers with proven track records',
    color: 'from-blue-500 to-cyan-500',
    bgColor: 'bg-blue-50'
  },
  {
    icon: Wallet,
    title: 'Instant Payment',
    description: 'Workers get paid immediately',
    color: 'from-purple-500 to-pink-500',
    bgColor: 'bg-purple-50'
  },
  {
    icon: Globe,
    title: 'PKHL Compliant',
    description: 'Follows Bali labor regulations',
    color: 'from-orange-500 to-red-500',
    bgColor: 'bg-orange-50'
  },
  {
    icon: Star,
    title: 'Two-Way Ratings',
    description: 'Transparent feedback builds trust',
    color: 'from-yellow-500 to-amber-500',
    bgColor: 'bg-yellow-50'
  },
  {
    icon: Sparkles,
    title: 'Community Hub',
    description: 'Connect, share tips, and grow together',
    color: 'from-indigo-500 to-violet-500',
    bgColor: 'bg-indigo-50',
    className: 'md:col-span-2 md:row-span-2'
  }
];

export function FeaturesSection() {
  return (
    <section id="features" className="py-24 relative overflow-hidden">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-white via-gray-50 to-white" />

      <div className="container mx-auto px-4 relative z-10">
        {/* Section header - Short & punchy */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="text-center mb-16"
        >
          <motion.span
            initial={{ opacity: 0, scale: 0.8 }}
            whileInView={{ opacity: 1, scale: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.4, delay: 0.2 }}
            className="inline-block px-4 py-2 mb-4 text-sm font-semibold text-green-600 bg-green-50 rounded-full"
          >
            Why Choose Us
          </motion.span>

          <h2 className="text-4xl md:text-6xl font-bold mb-4">
            <TextGenerateEffect
              words="Everything You Need"
              className="text-4xl md:text-6xl font-bold"
              duration={0.4}
            />
          </h2>

          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            Simple. Fast. Trusted.
          </p>
        </motion.div>

        {/* Features grid - BentoGrid with animated cards */}
        <BentoGrid className="max-w-7xl mx-auto">
          {simplifiedFeatures.map((feature, index) => (
            <BentoGridItem
              key={feature.title}
              title={feature.title}
              description={feature.description}
              header={
                <div className={`flex h-full min-h-[6rem] flex-col justify-between rounded-t-xl bg-gradient-to-br ${feature.color} p-4`}>
                  <feature.icon className="h-6 w-6 text-white" />
                </div>
              }
              icon={
                <div className={`inline-flex p-2 rounded-lg bg-gradient-to-br ${feature.color}`}>
                  <feature.icon className="h-4 w-4 text-white" />
                </div>
              }
              className={feature.className || ''}
            />
          ))}
        </BentoGrid>

        {/* Simple CTA */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.6 }}
          className="text-center mt-16"
        >
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="px-8 py-4 bg-gradient-to-r from-green-600 to-blue-600 text-white font-semibold rounded-full shadow-lg hover:shadow-xl transition-all duration-300"
          >
            See It in Action
          </motion.button>
        </motion.div>
      </div>
    </section>
  );
}
