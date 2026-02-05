"use client";

import { motion, useScroll, useTransform, useReducedMotion } from "framer-motion";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { HeroHighlight, Highlight } from "@/components/ui/hero-highlight";
import { TextGenerateEffect } from "@/components/ui/text-generate-effect";
import { Download, Users, Building, Star, ArrowRight, Smartphone, Sparkles, CheckCircle, Wallet } from "lucide-react";
import { APP_STORES } from "@/lib/constants";
import { useLanguage } from "@/lib/language-context";

export function Hero() {
  const { scrollY } = useScroll();
  const y = useTransform(scrollY, [0, 500], [0, -100]);
  const opacity = useTransform(scrollY, [0, 300], [1, 0]);
  const { t } = useLanguage();
  const prefersReducedMotion = useReducedMotion();

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
      className="relative min-h-[90vh] md:min-h-screen flex items-center overflow-hidden"
      aria-labelledby="hero-heading"
    >
      <HeroHighlight className="min-h-[90vh] md:min-h-screen">
        <motion.div style={{ y, opacity }} className="w-full max-w-[1800px] mx-auto px-6 sm:px-10 lg:px-20 xl:px-28 py-16 md:py-24 relative z-10">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-20 xl:gap-28 items-center">
            {/* Left Content */}
            <div className="lg:col-span-7 xl:col-span-7 max-w-4xl">
              {/* Badge */}
              <motion.div
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.5 }}
                className="mb-8"
              >
                <Badge className="bg-gradient-to-r from-green-100 to-blue-100 text-green-700 border-green-200 text-sm font-medium px-4 py-2 rounded-full inline-flex items-center gap-2 hover:bg-green-200 transition-all duration-300">
                  <Sparkles className="w-4 h-4" />
                  {t('hero.badge')}
                </Badge>
              </motion.div>

              {/* Headline - Short & Punchy with TextGenerateEffect */}
              <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: 0.2 }}
                className="mb-10"
              >
                <h1 id="hero-heading" className="text-4xl md:text-5xl lg:text-6xl xl:text-7xl font-bold tracking-tight mb-3">
                  <TextGenerateEffect
                    words={t('hero.headline')}
                    className="text-4xl md:text-5xl lg:text-6xl xl:text-7xl font-bold tracking-tight"
                    duration={0.5}
                  />{' '}
                  <Highlight className="text-4xl md:text-5xl lg:text-6xl xl:text-7xl font-bold from-green-400 to-blue-400 dark:from-green-500 dark:to-blue-500">
                    {t('hero.headline.highlight')}
                  </Highlight>
                </h1>
              </motion.div>

              {/* Subheadline - Bullet points instead of paragraph */}
              <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: 0.4 }}
                className="space-y-4 mb-10"
              >
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {features.map((feature, index) => (
                    <motion.div
                      key={index}
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ duration: 0.5, delay: 0.5 + index * 0.1 }}
                      className="flex items-center gap-3"
                    >
                      <CheckCircle className="w-5 h-5 text-green-500 shrink-0" />
                      <span className="text-base md:text-lg text-gray-700">{feature}</span>
                    </motion.div>
                  ))}
                </div>
              </motion.div>

              {/* Action-Oriented CTAs */}
              <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: 0.8 }}
                className="mb-14 flex flex-wrap gap-4 md:gap-5"
              >
                <Button
                  asChild
                  size="lg"
                  className="bg-gradient-to-r from-green-600 to-blue-600 hover:from-green-700 hover:to-blue-700 text-white shadow-xl shadow-green-500/25 text-base md:text-lg px-6 md:px-8 py-6 rounded-full transition-all duration-300 hover:scale-105 hover:shadow-2xl group"
                >
                  <a
                    href={APP_STORES.googlePlay}
                    target="_blank"
                    rel="noopener noreferrer"
                    aria-label="Download app from Google Play Store to find jobs"
                  >
                    <Download className="mr-3 h-5 w-5" aria-hidden="true" />
                    {t('hero.cta.findJob')}
                    <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" aria-hidden="true" />
                  </a>
                </Button>
                <Button
                  asChild
                  size="lg"
                  variant="outline"
                  className="border-2 border-blue-600 text-blue-600 hover:bg-blue-600 hover:text-white text-base md:text-lg px-6 md:px-8 py-6 rounded-full transition-all duration-300 hover:scale-105"
                >
                  <a
                    href="#business"
                    aria-label="Learn how to post jobs for businesses"
                  >
                    <Building className="mr-3 h-5 w-5" aria-hidden="true" />
                    {t('hero.cta.postJob')}
                  </a>
                </Button>
              </motion.div>

              {/* Quick Stats - With Counter Animation */}
              <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: 1 }}
                className="flex flex-wrap gap-10 md:gap-12"
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
                    whileHover={{ scale: 1.05, y: -2 }}
                    className="text-center cursor-default"
                  >
                    <p className={`text-3xl md:text-4xl lg:text-5xl font-bold ${stat.color} mb-1`}>
                      {stat.value.toLocaleString()}{stat.suffix}
                    </p>
                    <p className="text-sm md:text-base text-gray-600">{stat.label}</p>
                  </motion.div>
                ))}
              </motion.div>
            </div>

            {/* Right - Phone Mockup with App Demo */}
            <motion.div
              initial={{ opacity: 0, x: 100, rotate: 5 }}
              animate={{ opacity: 1, x: 0, rotate: 0 }}
              transition={{ duration: 1, delay: 0.3 }}
              className="relative hidden md:flex lg:col-span-5 xl:col-span-5 justify-center lg:justify-end"
              aria-hidden="true"
            >
              {/* Floating Animation */}
              <motion.div
                animate={{
                  y: [0, -10, 0],
                }}
                transition={{
                  duration: 5,
                  repeat: Infinity,
                  ease: "easeInOut"
                }}
                className="relative max-w-[280px] md:max-w-xs lg:max-w-[360px] xl:max-w-md"
              >
                {/* Phone Mockup */}
                <div className="relative bg-gradient-to-b from-gray-900 to-gray-800 rounded-[2.5rem] p-2.5 shadow-2xl">
                  {/* Notch */}
                  <div className="absolute top-5 left-1/2 -translate-x-1/2 w-24 h-6 bg-black rounded-full" />

                  {/* Screen with Animated App Screens */}
                  <div className="relative bg-white rounded-[2rem] overflow-hidden aspect-[9/19.5]">
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
                            <div className="w-full h-full bg-gradient-to-br from-gray-50 to-white p-3">
                              {/* App Header */}
                              <div className="bg-gradient-to-r from-green-600 to-blue-600 p-4 pb-6 rounded-xl mb-3">
                                <div className="flex items-center justify-between mb-2">
                                  <div className="w-6 h-6 bg-white/20 rounded-full" />
                                  <div className="text-white text-xs font-semibold">Jobs</div>
                                  <div className="w-6 h-6 bg-white/20 rounded-full" />
                                </div>
                                <div className="h-4 bg-white/30 rounded-lg w-2/3" />
                              </div>

                              {/* Job Cards */}
                              <div className="space-y-2">
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
                                    className="bg-white p-2 rounded-lg shadow-sm border border-gray-100"
                                  >
                                    <div className="flex items-center gap-2">
                                      <div className="w-8 h-8 bg-gradient-to-br from-green-500 to-blue-500 rounded-md" />
                                      <div className="flex-1">
                                        <div className="h-3 bg-gray-800 rounded w-3/4 mb-1" />
                                        <div className="h-2 bg-gray-200 rounded w-1/2" />
                                      </div>
                                      <div className="text-right">
                                        <div className="h-3 bg-blue-600 rounded w-12 mb-1" />
                                        {job.urgent && <div className="h-1.5 bg-red-400 rounded-full w-8" />}
                                      </div>
                                    </div>
                                  </motion.div>
                                ))}
                              </div>

                              {/* Bottom Nav */}
                              <div className="absolute bottom-0 left-0 right-0 bg-white border-t border-gray-100 p-2">
                                <div className="flex justify-around">
                                  {[1, 2, 3, 4].map((i) => (
                                    <div key={i} className={`w-6 h-6 rounded-md ${i === 1 ? 'bg-[#E07A5F]' : 'bg-gray-100'}`} />
                                  ))}
                                </div>
                              </div>
                            </div>
                          )}

                          {/* Screen 2: Apply Success */}
                          {screen === 2 && (
                            <div className="w-full h-full bg-gradient-to-br from-gray-50 to-white p-3 flex flex-col items-center justify-center">
                              <motion.div
                                initial={{ scale: 0 }}
                                animate={{ scale: 1 }}
                                transition={{ type: "spring", delay: 0.2 }}
                                className="w-14 h-14 bg-gradient-to-br from-blue-500 to-green-500 rounded-full flex items-center justify-center mb-4"
                              >
                                <CheckCircle className="w-7 h-7 text-white" />
                              </motion.div>
                              <div className="h-4 bg-gray-800 rounded-lg w-32 mb-2" />
                              <div className="h-3 bg-gray-300 rounded w-40 mb-3" />
                              <div className="h-8 bg-[#E07A5F] rounded-full w-28" />
                            </div>
                          )}

                          {/* Screen 3: Payment Success */}
                          {screen === 3 && (
                            <div className="w-full h-full bg-gradient-to-br from-gray-50 to-white p-3 flex flex-col items-center justify-center">
                              <motion.div
                                initial={{ scale: 0 }}
                                animate={{ scale: 1 }}
                                transition={{ type: "spring", delay: 0.2 }}
                                className="w-14 h-14 bg-gradient-to-br from-green-500 to-yellow-500 rounded-full flex items-center justify-center mb-4"
                              >
                                <Wallet className="w-7 h-7 text-white" />
                              </motion.div>
                              <div className="h-4 bg-gray-800 rounded-lg w-36 mb-2" />
                              <div className="h-6 bg-green-600 rounded-lg w-24 mb-2" />
                              <div className="h-3 bg-gray-300 rounded w-40" />
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

              {/* Floating Elements - Updated (Smaller) */}
              <motion.div
                animate={{
                  y: [0, 10, 0],
                  rotate: [0, -3, 3, 0]
                }}
                transition={{
                  duration: 4,
                  repeat: Infinity,
                  ease: "easeInOut",
                  delay: 0.5
                }}
                className="absolute -top-6 -right-6 bg-white p-3 rounded-xl shadow-lg border border-gray-100"
              >
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center shrink-0">
                    <CheckCircle className="w-4 h-4 text-white" />
                  </div>
                  <div>
                    <p className="text-xs font-semibold">Job Matched!</p>
                    <p className="text-[10px] text-gray-500">Just now</p>
                  </div>
                </div>
              </motion.div>

              <motion.div
                animate={{
                  y: [0, -10, 0],
                  rotate: [0, 3, -3, 0]
                }}
                transition={{
                  duration: 5,
                  repeat: Infinity,
                  ease: "easeInOut",
                  delay: 1
                }}
                className="absolute -bottom-6 -left-6 bg-white p-3 rounded-xl shadow-lg border border-gray-100"
              >
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center shrink-0">
                    <Star className="w-4 h-4 text-white" />
                  </div>
                  <div>
                    <p className="text-xs font-semibold">5.0★ Rating</p>
                    <p className="text-[10px] text-gray-500">New review</p>
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
      </HeroHighlight>
    </section>
  );
}
