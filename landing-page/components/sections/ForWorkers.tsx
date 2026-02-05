"use client";

import { motion } from "framer-motion";
import {
  Wallet,
  Users,
  TrendingUp,
  Gift,
  Star,
  ArrowRight,
  Play
} from "lucide-react";
import { APP_STORES } from "@/lib/constants";
import { useLanguage } from "@/lib/language-context";

export function ForWorkers() {
  const { t } = useLanguage();

  const benefits = [
    {
      icon: Wallet,
      title: t('workers.benefit.flexibleIncome'),
      description: t('workers.benefit.flexibleIncome.desc'),
      color: "from-green-500 to-emerald-500"
    },
    {
      icon: Users,
      title: t('workers.benefit.communitySupport'),
      description: t('workers.benefit.communitySupport.desc'),
      color: "from-blue-500 to-cyan-500"
    },
    {
      icon: TrendingUp,
      title: t('workers.benefit.careerGrowth'),
      description: t('workers.benefit.careerGrowth.desc'),
      color: "from-purple-500 to-pink-500"
    },
    {
      icon: Gift,
      title: t('workers.benefit.perksRewards'),
      description: t('workers.benefit.perksRewards.desc'),
      color: "from-orange-500 to-red-500"
    }
  ];
  return (
    <section className="relative py-24 overflow-hidden bg-gradient-to-b from-white to-slate-50">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute top-40 right-1/4 w-96 h-96 bg-green-100 rounded-full blur-3xl opacity-40" />
        <div className="absolute bottom-40 left-1/4 w-96 h-96 bg-blue-100 rounded-full blur-3xl opacity-40" />
      </div>

      <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        {/* Section Header */}
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
            {t('workers.badge')}
          </motion.span>
          <h2 className="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
            {t('workers.headline')} <span className="text-transparent bg-clip-text bg-gradient-to-r from-green-600 to-blue-600">{t('workers.headline.highlight')}</span>
          </h2>
          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            {t('workers.subtitle')}
          </p>
        </motion.div>

        {/* Benefits Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-20">
          {benefits.map((benefit, index) => (
            <motion.div
              key={benefit.title}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              whileHover={{ scale: 1.05, y: -5 }}
              className="group relative"
            >
              <div className="absolute inset-0 bg-gradient-to-r from-green-600 to-blue-600 rounded-2xl blur opacity-20 group-hover:opacity-40 transition-opacity duration-300" />
              <div className="relative bg-white p-6 rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 h-full border border-gray-100">
                {/* Icon Container */}
                <div className={`inline-flex p-3 rounded-xl bg-gradient-to-r ${benefit.color} mb-4 group-hover:scale-110 transition-transform duration-300`}>
                  <benefit.icon className="w-5 h-5 text-white" />
                </div>

                {/* Content */}
                <h3 className="text-lg font-bold text-gray-900 mb-2">
                  {benefit.title}
                </h3>
                <p className="text-sm text-gray-600 leading-relaxed">
                  {benefit.description}
                </p>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Testimonials & App Download */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {/* Testimonials - More Authentic */}
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6 }}
          >
            <h3 className="text-2xl font-bold text-gray-900 mb-6">
              Apa Kata Workers Kami?
            </h3>
            <div className="space-y-4">
              {[
                {
                  name: "Ketut Dewi",
                  role: "Housekeeping · Ubud",
                  avatar: "👩‍💼",
                  rating: 5,
                  content: "Dapat job lebih mudah dan bayaran cepat. Komunitasnya juga sangat membantu!"
                },
                {
                  name: "Made Suryawan",
                  role: "Driver · Seminyak",
                  avatar: "👨‍💼",
                  rating: 5,
                  content: "Sudah 3 bulan di sini. Rating bagus = job lebih banyak. Recommended!"
                },
                {
                  name: "Wayan Ari",
                  role: "Cook · Canggu",
                  avatar: "👨‍🍳",
                  rating: 5,
                  content: "Tips dari komunitas sangat membantu karier saya. Terima kasih!"
                }
              ].map((testimonial, index) => (
                <motion.div
                  key={index}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.5, delay: index * 0.15 }}
                  whileHover={{ 
                    scale: 1.02,
                    y: -4,
                    boxShadow: '0 20px 40px -12px rgba(0, 0, 0, 0.15)'
                  }}
                  className="bg-white p-5 rounded-2xl shadow-sm border border-gray-100 hover:shadow-xl transition-all duration-300 cursor-default"
                >
                  <div className="flex items-start gap-4">
                    <div className="w-12 h-12 rounded-full bg-gradient-to-r from-green-500 to-blue-500 flex items-center justify-center text-2xl shrink-0">
                      {testimonial.avatar}
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center gap-1 mb-1">
                        {[...Array(testimonial.rating)].map((_, i) => (
                          <Star key={i} className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                        ))}
                      </div>
                      <p className="text-gray-700 mb-2 leading-relaxed text-sm">
                        "{testimonial.content}"
                      </p>
                      <p className="font-semibold text-gray-900 text-sm">{testimonial.name}</p>
                      <p className="text-xs text-gray-500">{testimonial.role}</p>
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>
          </motion.div>

          {/* App Download CTA */}
          <motion.div
            initial={{ opacity: 0, x: 30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6 }}
          >
            <div className="relative">
              <div className="absolute inset-0 bg-gradient-to-r from-green-600 to-blue-600 rounded-3xl blur-2xl opacity-20" />
              <div className="relative bg-gradient-to-br from-gray-900 to-gray-800 p-10 rounded-3xl text-white overflow-hidden">
                {/* Decorative elements */}
                <div className="absolute top-0 right-0 w-64 h-64 bg-green-500/10 rounded-full blur-3xl" />
                <div className="absolute bottom-0 left-0 w-64 h-64 bg-blue-500/10 rounded-full blur-3xl" />

                <div className="relative z-10">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="p-3 bg-gradient-to-r from-green-500 to-blue-500 rounded-xl">
                      <Play className="w-6 h-6" />
                    </div>
                    <span className="text-lg font-semibold">Download App</span>
                  </div>

                  <h3 className="text-3xl font-bold mb-4">
                    Mulai Karier Anda Sekarang!
                  </h3>

                  <p className="text-gray-300 mb-8 leading-relaxed">
                    Download Daily Worker Hub app dan bergabung dengan komunitas 2000+ workers. Gratis, tanpa biaya pendaftaran.
                  </p>

                  {/* Stats */}
                  <div className="grid grid-cols-3 gap-4 mb-8">
                    <div className="text-center">
                      <p className="text-3xl font-bold text-green-400 mb-1">500+</p>
                      <p className="text-xs text-gray-400">Workers</p>
                    </div>
                    <div className="text-center">
                      <p className="text-3xl font-bold text-blue-400 mb-1">2000+</p>
                      <p className="text-xs text-gray-400">Jobs</p>
                    </div>
                    <div className="text-center">
                      <p className="text-3xl font-bold text-yellow-400 mb-1">4.8★</p>
                      <p className="text-xs text-gray-400">Rating</p>
                    </div>
                  </div>

                  {/* Download Buttons */}
                  <div className="space-y-3">
                    <motion.a
                      href={APP_STORES.googlePlay}
                      target="_blank"
                      rel="noopener noreferrer"
                      whileHover={{ scale: 1.02 }}
                      whileTap={{ scale: 0.98 }}
                      className="flex items-center justify-center gap-3 w-full py-4 bg-white text-gray-900 rounded-xl hover:bg-gray-50 transition-colors"
                    >
                      <svg className="w-8 h-8" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M3,20.5V3.5C3,2.91 3.34,2.39 3.84,2.15L13.69,12L3.84,21.85C3.34,21.6 3,21.09 3,20.5M16.81,15.12L6.05,21.34L14.54,12.85L16.81,15.12M20.3,13.1L18.14,14.5L15.06,11.41L18.14,8.32L20.3,9.72C20.74,10 21,10.5 21,11C21,11.5 20.74,12 20.3,12.32V13.1M16.81,8.88L14.54,11.15L6.05,2.66L16.81,8.88Z" />
                      </svg>
                      <div className="text-left">
                        <p className="text-xs text-gray-600">GET IT ON</p>
                        <p className="text-sm font-bold">Google Play</p>
                      </div>
                    </motion.a>

                    <motion.a
                      href={APP_STORES.appleAppStore}
                      target="_blank"
                      rel="noopener noreferrer"
                      whileHover={{ scale: 1.02 }}
                      whileTap={{ scale: 0.98 }}
                      className="flex items-center justify-center gap-3 w-full py-4 bg-white text-gray-900 rounded-xl hover:bg-gray-50 transition-colors"
                    >
                      <svg className="w-8 h-8" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M18.71,19.5C17.88,20.74 17,21.95 15.66,21.97C14.32,22 13.89,21.18 12.37,21.18C10.84,21.18 10.37,21.95 9.09997,22C7.78997,22.05 6.79997,20.68 5.95997,19.47C4.24997,17 2.93997,12.45 4.69997,9.39C5.56997,7.87 7.12997,6.91 8.81997,6.88C10.1,6.86 11.32,7.75 12.11,7.75C12.89,7.75 14.37,6.68 15.92,6.84C16.57,6.87 18.39,7.1 19.56,8.82C19.47,8.88 17.39,10.1 17.41,12.63C17.44,15.65 20.06,16.66 20.09,16.67C20.06,16.74 19.67,18.11 18.71,19.5M13,3.5C13.73,2.67 14.94,2.04 15.94,2C16.07,3.17 15.6,4.35 14.9,5.19C14.21,6.04 13.07,6.7 11.95,6.61C11.8,5.37 12.36,4.26 13,3.5Z" />
                      </svg>
                      <div className="text-left">
                        <p className="text-xs text-gray-600">Download on the</p>
                        <p className="text-sm font-bold">App Store</p>
                      </div>
                    </motion.a>
                  </div>
                </div>
              </div>
            </div>
          </motion.div>
        </div>

        {/* Bottom CTA */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.4 }}
          className="text-center mt-16"
        >
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="px-8 py-4 bg-gradient-to-r from-green-600 to-blue-600 text-white font-semibold rounded-full shadow-lg hover:shadow-xl transition-all duration-300 inline-flex items-center gap-2"
          >
            Gabung Sekarang - Gratis!
            <ArrowRight className="w-5 h-5" />
          </motion.button>
        </motion.div>
      </div>
    </section>
  );
}
