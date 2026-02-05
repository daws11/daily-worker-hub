"use client";

import { motion, useScroll, useTransform } from "framer-motion";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Download, Users, Building, Star, ArrowRight, Smartphone, Sparkles, CheckCircle, Wallet } from "lucide-react";
import { APP_STORES } from "@/lib/constants";
import { useLanguage } from "@/lib/language-context";

export function Hero() {
  const { scrollY } = useScroll();
  const y = useTransform(scrollY, [0, 500], [0, -100]);
  const opacity = useTransform(scrollY, [0, 300], [1, 0]);
  const { t } = useLanguage();

  const features = [
    t('hero.feature.smartMatching'),
    t('hero.feature.verifiedWorkers'),
    t('hero.feature.instantPayments'),
    t('hero.feature.communitySupport'),
    t('hero.feature.helpAvailable')
  ];

  return (
    <section
      id="home"
      className="relative min-h-screen flex items-center overflow-hidden"
    >
      <motion.div style={{ y, opacity }} className="container mx-auto px-4 py-20 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {/* Left Content */}
          <div className="max-w-2xl">
            {/* Badge */}
            <motion.div
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5 }}
              className="mb-6"
            >
              <Badge className="bg-gradient-to-r from-green-100 to-blue-100 text-green-700 border-green-200 text-sm font-medium px-4 py-2 rounded-full inline-flex items-center gap-2 hover:bg-green-200 transition-all duration-300">
                <Sparkles className="w-4 h-4" />
                {t('hero.badge')}
              </Badge>
            </motion.div>

            {/* Headline - Short & Punchy */}
            <motion.h1
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.2 }}
              className="text-5xl md:text-6xl lg:text-7xl font-bold tracking-tight mb-6"
            >
              {t('hero.headline')}{' '}
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-green-600 to-blue-600">
                {t('hero.headline.highlight')}
              </span>
            </motion.h1>

            {/* Subheadline - Bullet points instead of paragraph */}
            <motion.div
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.4 }}
              className="space-y-3 mb-8"
            >
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {features.map((feature, index) => (
                  <motion.div
                    key={index}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.5, delay: 0.5 + index * 0.1 }}
                    className="flex items-center gap-2"
                  >
                    <CheckCircle className="w-5 h-5 text-green-500 shrink-0" />
                    <span className="text-lg text-gray-700">{feature}</span>
                  </motion.div>
                ))}
              </div>
            </motion.div>

            {/* Action-Oriented CTAs */}
            <motion.div
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.8 }}
              className="mb-12 flex flex-wrap gap-4"
            >
              <Button
                asChild
                size="lg"
                className="bg-gradient-to-r from-green-600 to-blue-600 hover:from-green-700 hover:to-blue-700 text-white shadow-xl shadow-green-500/25 text-lg px-8 py-6 rounded-full transition-all duration-300 hover:scale-105 hover:shadow-2xl group"
              >
                <a href={APP_STORES.googlePlay} target="_blank" rel="noopener noreferrer">
                  <Download className="mr-3 h-5 w-5" />
                  {t('hero.cta.findJob')}
                  <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
                </a>
              </Button>
              <Button
                asChild
                size="lg"
                variant="outline"
                className="border-2 border-blue-600 text-blue-600 hover:bg-blue-600 hover:text-white text-lg px-8 py-6 rounded-full transition-all duration-300 hover:scale-105"
              >
                <a href="#business">
                  <Building className="mr-3 h-5 w-5" />
                  {t('hero.cta.postJob')}
                </a>
              </Button>
            </motion.div>

            {/* Quick Stats - With Counter Animation */}
            <motion.div
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 1 }}
              className="flex flex-wrap gap-8"
            >
              {[
                { value: 500, label: t('hero.stat.workers'), color: 'text-green-600', suffix: '+' },
                { value: 120, label: t('hero.stat.businesses'), color: 'text-blue-600', suffix: '+' },
                { value: 2000, label: t('hero.stat.jobs'), color: 'text-purple-600', suffix: '+' }
              ].map((stat, index) => (
                <motion.div
                  key={index}
                  initial={{ opacity: 0, scale: 0.8 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ duration: 0.5, delay: 1.1 + index * 0.1 }}
                  whileHover={{ scale: 1.1, y: -2 }}
                  className="text-center cursor-default"
                >
                  <p className={`text-3xl md:text-4xl font-bold ${stat.color} mb-1`}>
                    {stat.value.toLocaleString()}{stat.suffix}
                  </p>
                  <p className="text-sm text-gray-600">{stat.label}</p>
                </motion.div>
              ))}
            </motion.div>
          </div>

          {/* Right - Phone Mockup with App Demo */}
          <motion.div
            initial={{ opacity: 0, x: 100, rotate: 5 }}
            animate={{ opacity: 1, x: 0, rotate: 0 }}
            transition={{ duration: 1, delay: 0.3 }}
            className="relative hidden lg:block"
          >
            {/* Floating Animation */}
            <motion.div
              animate={{
                y: [0, -15, 0],
              }}
              transition={{
                duration: 5,
                repeat: Infinity,
                ease: "easeInOut"
              }}
              className="relative max-w-md mx-auto"
            >
              {/* Phone Mockup */}
              <div className="relative bg-gradient-to-b from-gray-900 to-gray-800 rounded-[3rem] p-3 shadow-2xl">
                {/* Notch */}
                <div className="absolute top-6 left-1/2 -translate-x-1/2 w-32 h-7 bg-black rounded-full" />

                {/* Screen with Animated App Screens */}
                <div className="relative bg-white rounded-[2.5rem] overflow-hidden aspect-[9/19]">
                  {/* Animated App Screens - Job Posting Flow */}
                  <div className="absolute inset-0 flex">
                    {[1, 2, 3].map((screen) => (
                      <motion.div
                        key={screen}
                        initial={{ x: "0%" }}
                        animate={{
                          x: screen === 1 ? "-100%" : screen === 2 ? "-200%" : "-300%",
                        }}
                        transition={{
                          duration: 6,
                          repeat: Infinity,
                          ease: "easeInOut",
                          delay: screen * 2,
                        }}
                        className="w-full h-full flex-shrink-0"
                      >
                        {/* Screen 1: Job List */}
                        {screen === 1 && (
                          <div className="w-full h-full bg-gradient-to-br from-gray-50 to-white p-4">
                            {/* App Header */}
                            <div className="bg-gradient-to-r from-green-600 to-blue-600 p-5 pb-8 rounded-2xl mb-4">
                              <div className="flex items-center justify-between mb-3">
                                <div className="w-8 h-8 bg-white/20 rounded-full" />
                                <div className="text-white text-sm font-semibold">Jobs</div>
                                <div className="w-8 h-8 bg-white/20 rounded-full" />
                              </div>
                              <div className="h-6 bg-white/30 rounded-lg w-2/3" />
                            </div>

                            {/* Job Cards */}
                            <div className="space-y-3">
                              {[
                                { title: "Housekeeping", loc: "Ubud", rate: "Rp 150k", urgent: true },
                                { title: "Driver", loc: "Seminyak", rate: "Rp 200k", urgent: false },
                                { title: "Cook", loc: "Canggu", rate: "Rp 180k", urgent: true },
                              ].map((job, i) => (
                                <motion.div
                                  key={i}
                                  initial={{ opacity: 0, y: 10 }}
                                  animate={{ opacity: 1, y: 0 }}
                                  transition={{ delay: i * 0.1 }}
                                  className="bg-white p-3 rounded-xl shadow-sm border border-gray-100"
                                >
                                  <div className="flex items-center gap-3">
                                    <div className="w-10 h-10 bg-gradient-to-br from-green-500 to-blue-500 rounded-lg" />
                                    <div className="flex-1">
                                      <div className="h-4 bg-gray-800 rounded w-3/4 mb-1" />
                                      <div className="h-3 bg-gray-200 rounded w-1/2" />
                                    </div>
                                    <div className="text-right">
                                      <div className="h-4 bg-blue-600 rounded w-16 mb-1" />
                                      {job.urgent && <div className="h-2 bg-red-400 rounded-full w-10" />}
                                    </div>
                                  </div>
                                </motion.div>
                              ))}
                            </div>

                            {/* Bottom Nav */}
                            <div className="absolute bottom-0 left-0 right-0 bg-white border-t border-gray-100 p-3">
                              <div className="flex justify-around">
                                {[1, 2, 3, 4].map((i) => (
                                  <div key={i} className={`w-8 h-8 rounded-lg ${i === 1 ? 'bg-[#E07A5F]' : 'bg-gray-100'}`} />
                                ))}
                              </div>
                            </div>
                          </div>
                        )}

                        {/* Screen 2: Apply Success */}
                        {screen === 2 && (
                          <div className="w-full h-full bg-gradient-to-br from-gray-50 to-white p-4 flex flex-col items-center justify-center">
                            <motion.div
                              initial={{ scale: 0 }}
                              animate={{ scale: 1 }}
                              transition={{ type: "spring", delay: 0.2 }}
                              className="w-20 h-20 bg-gradient-to-br from-blue-500 to-green-500 rounded-full flex items-center justify-center mb-6"
                            >
                              <CheckCircle className="w-10 h-10 text-white" />
                            </motion.div>
                            <div className="h-6 bg-gray-800 rounded-lg w-40 mb-2" />
                            <div className="h-4 bg-gray-300 rounded w-56 mb-4" />
                            <div className="h-10 bg-[#E07A5F] rounded-full w-40" />
                          </div>
                        )}

                        {/* Screen 3: Payment Success */}
                        {screen === 3 && (
                          <div className="w-full h-full bg-gradient-to-br from-gray-50 to-white p-4 flex flex-col items-center justify-center">
                            <motion.div
                              initial={{ scale: 0 }}
                              animate={{ scale: 1 }}
                              transition={{ type: "spring", delay: 0.2 }}
                              className="w-20 h-20 bg-gradient-to-br from-green-500 to-yellow-500 rounded-full flex items-center justify-center mb-6"
                            >
                              <Wallet className="w-10 h-10 text-white" />
                            </motion.div>
                            <div className="h-6 bg-gray-800 rounded-lg w-44 mb-2" />
                            <div className="h-8 bg-green-600 rounded-lg w-32 mb-2" />
                            <div className="h-4 bg-gray-300 rounded w-48" />
                          </div>
                        )}
                      </motion.div>
                    ))}
                  </div>
                </div>
              </div>

              {/* Glow Effect */}
              <div className="absolute -inset-4 bg-gradient-to-r from-green-500/20 to-blue-500/20 blur-3xl rounded-full -z-10" />
            </motion.div>

            {/* Floating Elements - Updated */}
            <motion.div
              animate={{
                y: [0, 15, 0],
                rotate: [0, -5, 5, 0]
              }}
              transition={{
                duration: 4,
                repeat: Infinity,
                ease: "easeInOut",
                delay: 0.5
              }}
              className="absolute -top-8 -right-8 bg-white p-4 rounded-2xl shadow-xl border border-gray-100"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-green-500 rounded-full flex items-center justify-center">
                  <CheckCircle className="w-6 h-6 text-white" />
                </div>
                <div>
                  <p className="text-sm font-semibold">Job Matched!</p>
                  <p className="text-xs text-gray-500">Just now</p>
                </div>
              </div>
            </motion.div>

            <motion.div
              animate={{
                y: [0, -15, 0],
                rotate: [0, 5, -5, 0]
              }}
              transition={{
                duration: 5,
                repeat: Infinity,
                ease: "easeInOut",
                delay: 1
              }}
              className="absolute -bottom-8 -left-8 bg-white p-4 rounded-2xl shadow-xl border border-gray-100"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center">
                  <Star className="w-6 h-6 text-white" />
                </div>
                <div>
                  <p className="text-sm font-semibold">5.0★ Rating</p>
                  <p className="text-xs text-gray-500">New review</p>
                </div>
              </div>
            </motion.div>
          </motion.div>
        </div>
      </motion.div>

      {/* Scroll Indicator */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 1.5 }}
        className="absolute bottom-8 left-1/2 -translate-x-1/2"
      >
        <motion.div
          animate={{ y: [0, 10, 0] }}
          transition={{ duration: 1.5, repeat: Infinity, ease: 'easeInOut' }}
          className="flex flex-col items-center gap-2 text-gray-400"
        >
          <span className="text-sm font-medium">Scroll</span>
          <motion.div className="w-6 h-10 border-2 border-gray-300 rounded-full flex justify-center pt-2">
            <motion.div
              animate={{ y: [0, 12, 0] }}
              transition={{ duration: 1.5, repeat: Infinity }}
              className="w-1.5 h-3 bg-gray-400 rounded-full"
            />
          </motion.div>
        </motion.div>
      </motion.div>
    </section>
  );
}
