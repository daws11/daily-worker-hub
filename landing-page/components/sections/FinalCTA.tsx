"use client";

import { motion } from "framer-motion";
import { ArrowRight, Shield, Star, Users, CheckCircle } from "lucide-react";
import { APP_STORES, STATS } from "@/lib/constants";

export function FinalCTA() {
  return (
    <section className="relative py-24 overflow-hidden bg-gradient-to-b from-white to-slate-50">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[800px] bg-gradient-to-r from-green-100 to-blue-100 rounded-full blur-3xl opacity-50" />
      </div>

      <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        {/* Main CTA */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="max-w-5xl mx-auto"
        >
          <div className="relative">
            {/* Gradient background */}
            <div className="absolute inset-0 bg-gradient-to-r from-green-600 via-blue-600 to-purple-600 rounded-3xl blur-2xl opacity-30" />
            
            <div className="relative bg-gradient-to-br from-gray-900 to-gray-800 p-10 md:p-16 rounded-3xl overflow-hidden">
              {/* Decorative circles */}
              <div className="absolute top-0 right-0 w-96 h-96 bg-green-500/10 rounded-full blur-3xl" />
              <div className="absolute bottom-0 left-0 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl" />

              <div className="relative z-10 text-center">
                <motion.div
                  initial={{ opacity: 0, scale: 0.8 }}
                  whileInView={{ opacity: 1, scale: 1 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.4, delay: 0.2 }}
                  className="inline-flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-green-500 to-emerald-500 rounded-full text-sm font-semibold mb-6"
                >
                  <Star className="w-4 h-4" />
                  Join 2000+ Happy Members
                </motion.div>

                <motion.h2
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.5, delay: 0.3 }}
                  className="text-4xl md:text-5xl lg:text-6xl font-bold text-white mb-6"
                >
                  Siap Bergabung dengan{' '}
                  <span className="text-transparent bg-clip-text bg-gradient-to-r from-green-400 to-blue-400">
                    Komunitas?
                  </span>
                </motion.h2>

                <motion.p
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.5, delay: 0.4 }}
                  className="text-lg text-gray-300 max-w-2xl mx-auto mb-10"
                >
                  Temukan talent terbaik atau peluang karier impian Anda. Bergabunglah dengan komunitas hospitality terbesar di Bali.
                </motion.p>

                {/* Dual CTAs */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.5, delay: 0.5 }}
                  className="flex flex-col sm:flex-row gap-4 justify-center mb-12"
                >
                  <motion.button
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    className="px-8 py-4 bg-gradient-to-r from-green-600 to-green-500 text-white font-semibold rounded-full shadow-lg hover:shadow-green-500/30 transition-all inline-flex items-center justify-center gap-2"
                  >
                    Daftar sebagai Worker
                    <ArrowRight className="w-5 h-5" />
                  </motion.button>
                  <motion.button
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    className="px-8 py-4 bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold rounded-full shadow-lg hover:shadow-blue-500/30 transition-all inline-flex items-center justify-center gap-2"
                  >
                    Daftar sebagai Bisnis
                    <ArrowRight className="w-5 h-5" />
                  </motion.button>
                </motion.div>

                {/* Trust Badges */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.5, delay: 0.6 }}
                  className="flex flex-wrap justify-center gap-6"
                >
                  <div className="flex items-center gap-2 text-gray-300">
                    <Users className="w-5 h-5 text-green-400" />
                    <span className="text-sm">{STATS.communityMembers}+ Members</span>
                  </div>
                  <div className="flex items-center gap-2 text-gray-300">
                    <Star className="w-5 h-5 text-yellow-400" />
                    <span className="text-sm">{STATS.averageRating}★ Rating</span>
                  </div>
                  <div className="flex items-center gap-2 text-gray-300">
                    <Shield className="w-5 h-5 text-blue-400" />
                    <span className="text-sm">Verified & Secure</span>
                  </div>
                </motion.div>
              </div>
            </div>
          </div>
        </motion.div>

        {/* Stats Grid */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.7 }}
          className="grid grid-cols-2 md:grid-cols-4 gap-6 max-w-4xl mx-auto mt-12"
        >
          {[
            { value: STATS.workers, label: 'Workers', icon: Users, color: 'text-green-400' },
            { value: STATS.businesses, label: 'Businesses', icon: Users, color: 'text-blue-400' },
            { value: STATS.jobsCompleted, label: 'Jobs', icon: Star, color: 'text-purple-400' },
            { value: `${STATS.averageRating}★`, label: 'Rating', icon: Star, color: 'text-yellow-400' }
          ].map((stat, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, scale: 0.8 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: 0.8 + index * 0.1 }}
              whileHover={{ scale: 1.05, y: -5 }}
              className="bg-white p-6 rounded-2xl shadow-lg text-center"
            >
              <stat.icon className={`w-8 h-8 ${stat.color} mx-auto mb-3`} />
              <p className="text-3xl font-bold text-gray-900 mb-1">
                {stat.value}+
              </p>
              <p className="text-sm text-gray-600">{stat.label}</p>
            </motion.div>
          ))}
        </motion.div>

        {/* Features Summary */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 1 }}
          className="mt-16 text-center"
        >
          <h3 className="text-2xl font-bold text-gray-900 mb-8">
            Apa yang Anda Dapatkan?
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl mx-auto">
            {[
              'Smart Job Matching dengan AI',
              'Komunitas 2000+ Workers & Businesses',
              'Payment Instant & Secure',
              'Sistem Rating Transparan',
              'PKHL Compliant',
              'Support 24/7'
            ].map((feature, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 10 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.3, delay: 1.1 + index * 0.1 }}
                className="flex items-center gap-3 bg-white p-4 rounded-xl shadow-sm"
              >
                <CheckCircle className="w-5 h-5 text-green-500 shrink-0" />
                <span className="text-sm font-medium text-gray-700">{feature}</span>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* App Download */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 1.2 }}
          className="text-center mt-16"
        >
          <p className="text-gray-600 mb-6">
            Download app sekarang dan mulai perjalanan Anda
          </p>
          <div className="flex flex-wrap justify-center gap-4">
            <motion.a
              href={APP_STORES.googlePlay}
              target="_blank"
              rel="noopener noreferrer"
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="flex items-center justify-center gap-3 px-6 py-3 bg-gray-900 text-white rounded-xl hover:bg-gray-800 transition-colors"
            >
              <svg className="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">
                <path d="M3,20.5V3.5C3,2.91 3.34,2.39 3.84,2.15L13.69,12L3.84,21.85C3.34,21.6 3,21.09 3,20.5M16.81,15.12L6.05,21.34L14.54,12.85L16.81,15.12M20.3,13.1L18.14,14.5L15.06,11.41L18.14,8.32L20.3,9.72C20.74,10 21,10.5 21,11C21,11.5 20.74,12 20.3,12.32V13.1M16.81,8.88L14.54,11.15L6.05,2.66L16.81,8.88Z" />
              </svg>
              <div className="text-left">
                <p className="text-xs text-gray-400">GET IT ON</p>
                <p className="text-sm font-bold">Google Play</p>
              </div>
            </motion.a>

            <motion.a
              href={APP_STORES.appleAppStore}
              target="_blank"
              rel="noopener noreferrer"
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="flex items-center justify-center gap-3 px-6 py-3 bg-gray-900 text-white rounded-xl hover:bg-gray-800 transition-colors"
            >
              <svg className="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">
                <path d="M18.71,19.5C17.88,20.74 17,21.95 15.66,21.97C14.32,22 13.89,21.18 12.37,21.18C10.84,21.18 10.37,21.95 9.09997,22C7.78997,22.05 6.79997,20.68 5.95997,19.47C4.24997,17 2.93997,12.45 4.69997,9.39C5.56997,7.87 7.12997,6.91 8.81997,6.88C10.1,6.86 11.32,7.75 12.11,7.75C12.89,7.75 14.37,6.68 15.92,6.84C16.57,6.87 18.39,7.1 19.56,8.82C19.47,8.88 17.39,10.1 17.41,12.63C17.44,15.65 20.06,16.66 20.09,16.67C20.06,16.74 19.67,18.11 18.71,19.5M13,3.5C13.73,2.67 14.94,2.04 15.94,2C16.07,3.17 15.6,4.35 14.9,5.19C14.21,6.04 13.07,6.7 11.95,6.61C11.8,5.37 12.36,4.26 13,3.5Z" />
              </svg>
              <div className="text-left">
                <p className="text-xs text-gray-400">Download on the</p>
                <p className="text-sm font-bold">App Store</p>
              </div>
            </motion.a>
          </div>
        </motion.div>
      </div>
    </section>
  );
}
