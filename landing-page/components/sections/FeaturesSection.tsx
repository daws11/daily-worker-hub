"use client";

import { motion } from 'framer-motion';
import { Search, ShieldCheck, Wallet, Globe, Star, Sparkles, Zap, GraduationCap, BarChart3 } from 'lucide-react';
import { FEATURES } from '@/lib/constants';

// Simplified features with shorter copy
const simplifiedFeatures = [
  {
    icon: Zap,
    title: 'Smart Matching',
    description: 'AI finds the perfect worker for your job',
    color: 'from-green-500 to-emerald-500',
    bgColor: 'bg-green-50'
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
    description: 'Workers get paid immediately after job completion',
    color: 'from-purple-500 to-pink-500',
    bgColor: 'bg-purple-50'
  },
  {
    icon: Globe,
    title: 'PKHL Compliant',
    description: 'Automatically follows Bali labor regulations',
    color: 'from-orange-500 to-red-500',
    bgColor: 'bg-orange-50'
  },
  {
    icon: Star,
    title: 'Two-Way Ratings',
    description: 'Transparent feedback system builds trust',
    color: 'from-yellow-500 to-amber-500',
    bgColor: 'bg-yellow-50'
  },
  {
    icon: Sparkles,
    title: 'Community Hub',
    description: 'Connect, share tips, and grow together',
    color: 'from-indigo-500 to-violet-500',
    bgColor: 'bg-indigo-50',
    highlight: true
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
            Everything You{' '}
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-green-600 to-blue-600">
              Need
            </span>
          </h2>

          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            Simple. Fast. Trusted.
          </p>
        </motion.div>

        {/* Features grid - Card-based with short copy */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {simplifiedFeatures.map((feature, index) => (
            <motion.div
              key={feature.title}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: '-50px' }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              whileHover={{ 
                scale: 1.03,
                y: -8,
                boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.15)'
              }}
              className={`relative group ${feature.highlight ? 'md:col-span-2 lg:col-span-2' : ''}`}
            >
              <div className={`relative bg-white p-8 rounded-2xl border border-gray-100 shadow-sm hover:shadow-xl transition-all duration-300 h-full ${feature.bgColor}`}>
                
                {/* Feature icon */}
                <div className={`inline-flex p-3 rounded-xl bg-gradient-to-br ${feature.color} mb-4 group-hover:scale-110 transition-transform duration-300`}>
                  <feature.icon className="h-6 w-6 text-white" />
                </div>

                {/* Feature content - Short & punchy */}
                <h3 className="text-xl font-bold text-gray-900 mb-2">
                  {feature.title}
                </h3>
                <p className="text-gray-600 leading-relaxed">
                  {feature.description}
                </p>

                {/* Feature highlight badge */}
                {feature.highlight && (
                  <motion.div
                    initial={{ opacity: 0, scale: 0.8 }}
                    whileInView={{ opacity: 1, scale: 1 }}
                    viewport={{ once: true, margin: '-50px' }}
                    transition={{ duration: 0.5, delay: 0.2 }}
                    className="absolute top-6 right-6"
                  >
                    <Sparkles className="h-6 w-6 text-yellow-500" />
                  </motion.div>
                )}
              </div>
            </motion.div>
          ))}
        </div>

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
