"use client";

import { motion } from "framer-motion";
import {
  Zap,
  ShieldCheck,
  DollarSign,
  Smartphone,
  Star,
  ArrowRight,
  Clock,
  CheckCircle
} from "lucide-react";
import { useLanguage } from "@/lib/language-context";

export function ForBusinesses() {
  const { t } = useLanguage();

  const benefits = [
    {
      icon: Zap,
      title: t('businesses.benefit.instantStaffing'),
      description: t('businesses.benefit.instantStaffing.desc'),
      color: "from-orange-500 to-red-500"
    },
    {
      icon: ShieldCheck,
      title: t('businesses.benefit.preVetted'),
      description: t('businesses.benefit.preVetted.desc'),
      color: "from-green-500 to-emerald-500"
    },
    {
      icon: DollarSign,
      title: t('businesses.benefit.costEffective'),
      description: t('businesses.benefit.costEffective.desc'),
      color: "from-blue-500 to-cyan-500"
    },
    {
      icon: Smartphone,
      title: t('businesses.benefit.easyManagement'),
      description: t('businesses.benefit.easyManagement.desc'),
      color: "from-purple-500 to-pink-500"
    }
  ];
  return (
    <section className="relative py-24 overflow-hidden bg-gradient-to-b from-slate-50 to-white">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute top-40 left-1/4 w-96 h-96 bg-orange-100 rounded-full blur-3xl opacity-40" />
        <div className="absolute bottom-40 right-1/4 w-96 h-96 bg-purple-100 rounded-full blur-3xl opacity-40" />
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
            className="inline-block px-4 py-2 mb-4 text-sm font-semibold text-orange-600 bg-orange-50 rounded-full"
          >
            {t('businesses.badge')}
          </motion.span>
          <h2 className="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
            {t('businesses.headline')} <span className="text-transparent bg-clip-text bg-gradient-to-r from-orange-600 to-purple-600">{t('businesses.headline.highlight')}</span>
          </h2>
          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            {t('businesses.subtitle')}
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
              <div className="absolute inset-0 bg-gradient-to-r from-orange-600 to-purple-600 rounded-2xl blur opacity-20 group-hover:opacity-40 transition-opacity duration-300" />
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

        {/* How It Works for Businesses */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="bg-white p-8 md:p-12 rounded-3xl shadow-lg border border-gray-100 mb-20"
        >
          <div className="text-center mb-12">
            <h3 className="text-2xl md:text-3xl font-bold text-gray-900 mb-4">
              {t('businesses.howItWorks')}
            </h3>
            <p className="text-gray-600 max-w-2xl mx-auto">
              {t('businesses.howItWorks.subtitle')}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {[
              {
                step: "01",
                title: t('businesses.step1.title'),
                description: t('businesses.step1.desc'),
                icon: CheckCircle
              },
              {
                step: "02",
                title: t('businesses.step2.title'),
                description: t('businesses.step2.desc'),
                icon: Clock
              },
              {
                step: "03",
                title: t('businesses.step3.title'),
                description: t('businesses.step3.desc'),
                icon: Zap
              }
            ].map((step, index) => (
              <motion.div
                key={step.step}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: index * 0.15 }}
                className="relative"
              >
                <div className="absolute top-0 left-0 text-6xl font-bold text-gray-100 -z-10">
                  {step.step}
                </div>
                <div className="bg-gradient-to-br from-orange-50 to-purple-50 p-6 rounded-2xl border border-gray-100">
                  <step.icon className="w-8 h-8 text-orange-600 mb-4" />
                  <h4 className="text-xl font-bold text-gray-900 mb-3">
                    {step.title}
                  </h4>
                  <p className="text-gray-600 leading-relaxed">
                    {step.description}
                  </p>
                </div>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* Testimonials & Pricing */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {/* Testimonials - More Authentic */}
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6 }}
          >
            <h3 className="text-2xl font-bold text-gray-900 mb-6">
              Apa Kata Bisnis Kami?
            </h3>
            <div className="space-y-4">
              {[
                {
                  name: "Made Suryawan",
                  role: "Manager · Hotel Ubud",
                  avatar: "👨‍💼",
                  rating: 5,
                  content: "Staff quality bagus dan selalu tepat waktu. Komunitasnya juga bermanfaat!"
                },
                {
                  name: "Ni Luh Putri",
                  role: "Owner · Cafe Seminyak",
                  avatar: "👩‍💼",
                  rating: 5,
                  content: "Booking cepat, pembayaran aman, dan rating sistem membantu pilih yang terbaik."
                },
                {
                  name: "Kadeh",
                  role: "Chef · Villa Canggu",
                  avatar: "👨‍🍳",
                  rating: 5,
                  content: "Sudah pakai 6 bulan. Tidak ada masalah, staffnya profesional semua."
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
                    <div className="w-12 h-12 rounded-full bg-gradient-to-r from-orange-500 to-purple-500 flex items-center justify-center text-2xl shrink-0">
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

          {/* Pricing CTA */}
          <motion.div
            initial={{ opacity: 0, x: 30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6 }}
          >
            <div className="relative">
              <div className="absolute inset-0 bg-gradient-to-r from-orange-600 to-purple-600 rounded-3xl blur-2xl opacity-20" />
              <div className="relative bg-gradient-to-br from-gray-900 to-gray-800 p-10 rounded-3xl text-white overflow-hidden">
                {/* Decorative elements */}
                <div className="absolute top-0 right-0 w-64 h-64 bg-orange-500/10 rounded-full blur-3xl" />
                <div className="absolute bottom-0 left-0 w-64 h-64 bg-purple-500/10 rounded-full blur-3xl" />

                <div className="relative z-10">
                  <div className="inline-block px-3 py-1 bg-gradient-to-r from-green-500 to-emerald-500 rounded-full text-sm font-semibold mb-6">
                    {t('businesses.pricing.badge')}
                  </div>

                  <h3 className="text-3xl font-bold mb-4">
                    {t('businesses.pricing.title')}
                  </h3>

                  <p className="text-gray-300 mb-6 leading-relaxed">
                    {t('businesses.pricing.subtitle')}
                  </p>

                  {/* Pricing */}
                  <div className="bg-white/10 backdrop-blur-sm p-6 rounded-2xl mb-8">
                    <div className="flex items-baseline gap-2 mb-4">
                      <span className="text-4xl font-bold">0%</span>
                      <span className="text-gray-400">{t('businesses.pricing.commission')}</span>
                    </div>
                    <p className="text-sm text-gray-400 mb-4">{t('businesses.pricing.trial')}</p>
                    <div className="flex items-center gap-2 text-sm text-gray-300">
                      <span className="text-green-400 font-semibold">{t('businesses.pricing.after')}</span>
                      <span>{t('businesses.pricing.rate')}</span>
                    </div>
                  </div>

                  {/* Features included */}
                  <div className="space-y-3 mb-8">
                    {[
                      "Akses ke 500+ pre-vetted workers",
                      "Unlimited job postings",
                      "Real-time staff tracking",
                      "Instant payment system",
                      "24/7 support",
                      "PKHL compliant"
                    ].map((feature, index) => (
                      <div key={index} className="flex items-center gap-3">
                        <CheckCircle className="w-5 h-5 text-green-400 shrink-0" />
                        <span className="text-sm">{feature}</span>
                      </div>
                    ))}
                  </div>

                  {/* CTA Button */}
                  <motion.button
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    className="w-full py-4 bg-gradient-to-r from-orange-500 to-purple-500 text-white font-semibold rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 flex items-center justify-center gap-2"
                  >
                    {t('businesses.cta')}
                    <ArrowRight className="w-5 h-5" />
                  </motion.button>
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
          <p className="text-gray-600 mb-4">
            Pertanyaan? Hubungi tim kami di
          </p>
          <motion.a
            href="https://wa.me/6281234567890"
            target="_blank"
            rel="noopener noreferrer"
            whileHover={{ scale: 1.05 }}
            className="inline-flex items-center gap-2 text-green-600 hover:text-green-700 font-semibold"
          >
            <span>+62 812 3456 7890</span>
            <ArrowRight className="w-4 h-4" />
          </motion.a>
        </motion.div>
      </div>
    </section>
  );
}
