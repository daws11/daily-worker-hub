"use client";

import { motion, useScroll, useTransform, useReducedMotion, AnimatePresence } from "framer-motion";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { HeroHighlight, Highlight } from "@/components/ui/hero-highlight";
import { TextGenerateEffect } from "@/components/ui/text-generate-effect";
import { BackgroundBeams } from "@/components/ui/background-beams";
import { Download, Building, Star, ArrowRight, Sparkles, CheckCircle, Wallet } from "lucide-react";
import { APP_STORES } from "@/lib/constants";
import { useLanguage } from "@/lib/language-context";
import { useState, useEffect } from "react";

export function Hero() {
  const { scrollY } = useScroll();
  const y = useTransform(scrollY, [0, 500], [0, -100]);
  const opacity = useTransform(scrollY, [0, 300], [1, 0]);
  const { t } = useLanguage();
  const prefersReducedMotion = useReducedMotion();

  const [currentScreen, setCurrentScreen] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentScreen((prev) => (prev + 1) % 3);
    }, 4000);
    return () => clearInterval(timer);
  }, []);

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
      {/* Aceternity UI BackgroundBeams - Hero section background */}
      <div className="absolute inset-0 pointer-events-none -z-10 overflow-hidden opacity-40">
        <BackgroundBeams />
      </div>

      <HeroHighlight className="min-h-[90vh] md:min-h-screen">
        <motion.div style={{ y, opacity }} className="w-full max-w-[1440px] mx-auto px-4 sm:px-6 md:px-10 lg:px-16 xl:px-24 py-12 md:py-16 lg:py-24 relative z-10">
          {/* Medium Screen Layout: Side by side with better proportions */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 md:gap-10 lg:gap-16 xl:gap-24 items-center">
            {/* Left Content */}
            <div className="max-w-2xl md:max-w-xl lg:max-w-3xl order-2 md:order-1">
              {/* Badge */}
              <motion.div
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.5 }}
                className="mb-6 md:mb-8"
              >
                <Badge className="bg-gradient-to-r from-green-100 to-blue-100 text-green-700 border-green-200 text-xs md:text-sm font-medium px-3 md:px-4 py-1.5 md:py-2 rounded-full inline-flex items-center gap-2 hover:bg-green-200 transition-all duration-300">
                  <Sparkles className="w-3.5 h-3.5 md:w-4 md:h-4" />
                  {t('hero.badge')}
                </Badge>
              </motion.div>

              {/* Headline */}
              <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: 0.2 }}
                className="mb-6 md:mb-10"
              >
                <h1 id="hero-heading" className="text-3xl sm:text-4xl md:text-4xl lg:text-5xl xl:text-6xl font-bold tracking-tight mb-2 md:mb-3">
                  <TextGenerateEffect
                    words={t('hero.headline')}
                    className="text-3xl sm:text-4xl md:text-4xl lg:text-5xl xl:text-6xl font-bold tracking-tight"
                    duration={0.5}
                  />{' '}
                  <Highlight className="text-3xl sm:text-4xl md:text-4xl lg:text-5xl xl:text-6xl font-bold from-green-400 to-blue-400 dark:from-green-500 dark:to-blue-500">
                    {t('hero.headline.highlight')}
                  </Highlight>
                </h1>
              </motion.div>

              {/* Features */}
              <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: 0.4 }}
                className="space-y-3 md:space-y-4 mb-6 md:mb-10"
              >
                <div className="grid grid-cols-1 md:grid-cols-1 lg:grid-cols-2 gap-3 md:gap-4">
                  {features.map((feature, index) => (
                    <motion.div
                      key={index}
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ duration: 0.5, delay: 0.5 + index * 0.1 }}
                      className="flex items-center gap-2 md:gap-3"
                    >
                      <CheckCircle className="w-4 h-4 md:w-5 md:h-5 text-green-500 shrink-0" />
                      <span className="text-sm md:text-base lg:text-lg text-gray-700">{feature}</span>
                    </motion.div>
                  ))}
                </div>
              </motion.div>

              {/* CTAs */}
              <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: 0.8 }}
                className="mb-8 md:mb-14 flex flex-col sm:flex-row gap-3 md:gap-4"
              >
                <Button
                  asChild
                  size="lg"
                  className="bg-gradient-to-r from-green-600 to-blue-600 hover:from-green-700 hover:to-blue-700 text-white shadow-xl shadow-green-500/25 text-sm md:text-base lg:text-lg px-5 md:px-6 lg:px-8 py-5 md:py-6 rounded-full transition-all duration-300 hover:scale-105 hover:shadow-2xl group"
                >
                  <a
                    href={APP_STORES.googlePlay}
                    target="_blank"
                    rel="noopener noreferrer"
                    aria-label="Download app from Google Play Store"
                  >
                    <Download className="mr-2 h-4 w-4 md:mr-3 md:h-5 md:w-5" aria-hidden="true" />
                    <span className="truncate">{t('hero.cta.findJob')}</span>
                    <ArrowRight className="ml-1 h-4 w-4 md:ml-2 md:h-5 md:w-5 group-hover:translate-x-1 transition-transform" aria-hidden="true" />
                  </a>
                </Button>
                <Button
                  asChild
                  size="lg"
                  variant="outline"
                  className="border-2 border-blue-600 text-blue-600 hover:bg-blue-600 hover:text-white text-sm md:text-base lg:text-lg px-5 md:px-6 lg:px-8 py-5 md:py-6 rounded-full transition-all duration-300 hover:scale-105"
                >
                  <a href="#business" aria-label="Learn how to post jobs">
                    <Building className="mr-2 h-4 w-4 md:mr-3 md:h-5 md:w-5" aria-hidden="true" />
                    <span className="hidden sm:inline">{t('hero.cta.postJob')}</span>
                    <span className="sm:hidden">Post Job</span>
                  </a>
                </Button>
              </motion.div>

              {/* Stats - Optimized for medium screens */}
              <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8, delay: 1 }}
                className="flex justify-between md:justify-start gap-4 md:gap-8 lg:gap-12"
              >
                {[
                  { value: 500, label: 'Workers', color: 'text-green-600', suffix: '+' },
                  { value: 120, label: 'Businesses', color: 'text-blue-600', suffix: '+' },
                  { value: 2000, label: 'Jobs', color: 'text-purple-600', suffix: '+' }
                ].map((stat, index) => (
                  <motion.div
                    key={index}
                    initial={{ opacity: 0, scale: 0.8 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ duration: 0.5, delay: 1.1 + index * 0.1 }}
                    whileHover={{ scale: 1.05, y: -2 }}
                    className="text-center cursor-default flex-1 md:flex-none"
                  >
                    <p className={`text-xl md:text-2xl lg:text-4xl font-bold ${stat.color} mb-0.5 md:mb-1`}>
                      {stat.value.toLocaleString()}{stat.suffix}
                    </p>
                    <p className="text-xs md:text-sm lg:text-base text-gray-600">{stat.label}</p>
                  </motion.div>
                ))}
              </motion.div>
            </div>

            {/* Right - Phone Mockup - Improved medium display */}
            <motion.div
              initial={{ opacity: 0, x: 100, rotate: 5 }}
              animate={{ opacity: 1, x: 0, rotate: 0 }}
              transition={{ duration: 1, delay: 0.3 }}
              className="relative hidden md:flex justify-center items-center order-1 md:order-2"
              aria-hidden="true"
            >
              {/* Floating Animation Wrapper - Optimized for medium screens */}
              <motion.div
                animate={{ y: [0, -15, 0] }}
                transition={{
                  duration: 6,
                  repeat: Infinity,
                  ease: "easeInOut"
                }}
                className="relative w-full max-w-[240px] md:max-w-[260px] lg:max-w-[300px] xl:max-w-[340px]"
              >
                {/* Phone Body */}
                <div className="relative bg-gray-900 rounded-[3rem] p-3 shadow-2xl border-[6px] border-gray-900">
                  {/* Notch */}
                  <div className="absolute top-0 left-1/2 -translate-x-1/2 w-32 h-7 bg-gray-900 rounded-b-2xl z-20" />
                  
                  {/* Screen Container */}
                  <div className="relative bg-white rounded-[2rem] md:rounded-[2.5rem] overflow-hidden aspect-[9/19.5]">

                    {/* Animated Screens */}
                    <AnimatePresence mode="wait">
                      {currentScreen === 0 && (
                        <motion.div
                          key="screen-1"
                          initial={{ opacity: 0, x: 50 }}
                          animate={{ opacity: 1, x: 0 }}
                          exit={{ opacity: 0, x: -50 }}
                          transition={{ duration: 0.5 }}
                          className="absolute inset-0 bg-gray-50 flex flex-col"
                        >
                          {/* Header */}
                          <div className="bg-gradient-to-r from-green-600 to-blue-600 p-4 md:p-6 pt-8 md:pt-12 pb-4 md:pb-8 rounded-b-2xl md:rounded-b-3xl shadow-md">
                            <div className="flex justify-between items-center mb-2 md:mb-4">
                              <div className="w-6 h-6 md:w-8 md:h-8 bg-white/20 rounded-full" />
                              <div className="text-white text-xs md:text-base font-semibold">Find Jobs</div>
                              <div className="w-6 h-6 md:w-8 md:h-8 bg-white/20 rounded-full" />
                            </div>
                            <div className="h-1.5 md:h-2 bg-white/30 rounded-full w-1/3 mb-1 md:mb-2" />
                            <div className="h-1.5 md:h-2 bg-white/30 rounded-full w-2/3" />
                          </div>

                          {/* Job List - Optimized for medium */}
                          <div className="p-2 md:p-4 space-y-2 md:space-y-3 overflow-hidden">
                             {[
                                { title: "Housekeeping", loc: "Ubud", rate: "150k", tag: "Urgent" },
                                { title: "Driver", loc: "Kuta", rate: "200k", tag: "New" },
                                { title: "Chef Assistant", loc: "Canggu", rate: "180k", tag: "Hot" },
                                { title: "Gardener", loc: "Sanur", rate: "120k", tag: "" },
                              ].map((job, i) => (
                                <motion.div
                                  key={i}
                                  initial={{ opacity: 0, y: 20 }}
                                  animate={{ opacity: 1, y: 0 }}
                                  transition={{ delay: 0.2 + i * 0.1 }}
                                  className="bg-white p-2 md:p-3 rounded-lg md:rounded-xl shadow-sm border border-gray-100 flex items-center gap-2 md:gap-3"
                                >
                                  <div className="w-8 h-8 md:w-10 md:h-10 rounded-lg bg-gradient-to-br from-green-100 to-blue-100 flex-shrink-0" />
                                  <div className="flex-1 min-w-0">
                                    <div className="h-2 md:h-3 bg-gray-800 rounded w-16 md:w-24 mb-1 md:mb-1.5" />
                                    <div className="h-1.5 md:h-2 bg-gray-300 rounded w-12 md:w-16" />
                                  </div>
                                  <div className="text-right flex-shrink-0">
                                    <div className="bg-green-100 text-green-700 text-[8px] md:text-[10px] px-1.5 md:px-2 py-0.5 rounded-full font-medium">
                                      {job.rate}
                                    </div>
                                  </div>
                                </motion.div>
                              ))}
                          </div>
                        </motion.div>
                      )}

                      {currentScreen === 1 && (
                        <motion.div
                          key="screen-2"
                          initial={{ opacity: 0, scale: 0.9 }}
                          animate={{ opacity: 1, scale: 1 }}
                          exit={{ opacity: 0, scale: 1.1 }}
                          transition={{ duration: 0.5 }}
                          className="absolute inset-0 bg-white flex flex-col items-center justify-center p-4 md:p-6 text-center"
                        >
                          <motion.div
                            initial={{ scale: 0 }}
                            animate={{ scale: 1 }}
                            transition={{ type: "spring", stiffness: 200, delay: 0.2 }}
                            className="w-14 h-14 md:w-20 md:h-20 bg-green-100 rounded-full flex items-center justify-center mb-4 md:mb-6"
                          >
                            <CheckCircle className="w-7 h-7 md:w-10 md:h-10 text-green-600" />
                          </motion.div>
                          <h3 className="text-base md:text-xl font-bold text-gray-900 mb-1 md:mb-2">Application Sent!</h3>
                          <p className="text-gray-500 text-[10px] md:text-sm mb-4 md:mb-8 px-2">Good luck! The employer will review your profile shortly.</p>
                          <div className="w-full bg-gray-100 h-8 md:h-12 rounded-xl" />
                        </motion.div>
                      )}

                      {currentScreen === 2 && (
                        <motion.div
                          key="screen-3"
                          initial={{ opacity: 0, y: 50 }}
                          animate={{ opacity: 1, y: 0 }}
                          exit={{ opacity: 0, y: -50 }}
                          transition={{ duration: 0.5 }}
                          className="absolute inset-0 bg-gradient-to-br from-green-50 to-blue-50 flex flex-col items-center justify-center p-4 md:p-6"
                        >
                          <div className="w-full bg-white p-4 md:p-6 rounded-xl md:rounded-2xl shadow-lg border border-gray-100">
                             <div className="flex items-center gap-2 md:gap-3 mb-4 md:mb-6">
                               <div className="w-8 h-8 md:w-10 md:h-10 bg-blue-100 rounded-full flex items-center justify-center">
                                 <Wallet className="w-4 h-4 md:w-5 md:h-5 text-blue-600" />
                               </div>
                               <div className="text-left">
                                 <div className="text-[10px] md:text-xs text-gray-500">Total Balance</div>
                                 <div className="font-bold text-sm md:text-lg">Rp 2.500.000</div>
                               </div>
                             </div>
                             <div className="space-y-2 md:space-y-3">
                               <div className="h-8 md:h-10 bg-gray-50 rounded-lg w-full" />
                               <div className="h-8 md:h-10 bg-gray-50 rounded-lg w-full" />
                             </div>
                          </div>
                          <motion.div
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ delay: 0.5 }}
                            className="mt-4 md:mt-6 bg-green-600 text-white px-4 md:px-6 py-1.5 md:py-2 rounded-full text-[10px] md:text-sm font-medium shadow-lg shadow-green-200"
                          >
                            Payment Received 💸
                          </motion.div>
                        </motion.div>
                      )}
                    </AnimatePresence>

                    {/* Bottom Navigation Indicator - Optimized for medium */}
                    <div className="absolute bottom-3 md:bottom-4 left-1/2 -translate-x-1/2 flex gap-1.5 md:gap-2 z-10">
                      {[0, 1, 2].map((i) => (
                        <div
                          key={i}
                          className={`w-1.5 h-1.5 md:w-2 md:h-2 rounded-full transition-colors duration-300 ${
                            i === currentScreen ? 'bg-green-500' : 'bg-gray-300'
                          }`}
                        />
                      ))}
                    </div>
                  </div>
                </div>

                {/* Floating Elements - Optimized for medium screens */}
                <motion.div
                  animate={{ y: [0, 10, 0], rotate: [0, -3, 3, 0] }}
                  transition={{ duration: 4, repeat: Infinity, ease: "easeInOut", delay: 0.5 }}
                  className="absolute top-6 md:top-10 -right-6 md:-right-8 bg-white p-2 md:p-3 rounded-xl md:rounded-2xl shadow-lg md:shadow-xl border border-gray-100 flex items-center gap-2 md:gap-3 z-20"
                >
                  <div className="bg-green-100 p-1.5 md:p-2 rounded-full">
                    <CheckCircle className="w-4 h-4 md:w-5 md:h-5 text-green-600" />
                  </div>
                  <div className="hidden sm:block">
                    <p className="text-[10px] md:text-xs font-bold text-gray-900">Job Matched!</p>
                    <p className="text-[8px] md:text-[10px] text-gray-500">Just now</p>
                  </div>
                </motion.div>

                <motion.div
                  animate={{ y: [0, -10, 0], rotate: [0, 3, -3, 0] }}
                  transition={{ duration: 5, repeat: Infinity, ease: "easeInOut", delay: 1 }}
                  className="absolute bottom-16 md:bottom-20 -left-6 md:-left-8 bg-white p-2 md:p-3 rounded-xl md:rounded-2xl shadow-lg md:shadow-xl border border-gray-100 flex items-center gap-2 md:gap-3 z-20"
                >
                   <div className="bg-yellow-100 p-1.5 md:p-2 rounded-full">
                    <Star className="w-4 h-4 md:w-5 md:h-5 text-yellow-600" />
                  </div>
                  <div className="hidden sm:block">
                    <p className="text-[10px] md:text-xs font-bold text-gray-900">5.0 Rating</p>
                    <p className="text-[8px] md:text-[10px] text-gray-500">New review</p>
                  </div>
                </motion.div>

                {/* Glow Behind Phone */}
                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[120%] h-[120%] bg-gradient-to-tr from-green-400/30 to-blue-400/30 blur-[80px] -z-10 rounded-full" />
              </motion.div>
            </motion.div>
          </div>
        </motion.div>

        {/* Scroll Indicator - Optimized for medium */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 1.5 }}
          className="absolute bottom-6 md:bottom-8 left-1/2 -translate-x-1/2"
        >
          <motion.div
            animate={{ y: [0, 10, 0] }}
            transition={{ duration: 1.5, repeat: Infinity, ease: 'easeInOut' }}
            className="flex flex-col items-center gap-1.5 md:gap-2 text-gray-400"
          >
            <span className="text-xs md:text-sm font-medium">Scroll</span>
            <motion.div className="w-5 h-8 md:w-6 md:h-10 border-2 border-gray-300 rounded-full flex justify-center pt-1.5 md:pt-2">
              <motion.div
                animate={{ y: [0, 10, 0] }}
                transition={{ duration: 1.5, repeat: Infinity }}
                className="w-1 h-2 md:w-1.5 md:h-3 bg-gray-400 rounded-full"
              />
            </motion.div>
          </motion.div>
        </motion.div>
      </HeroHighlight>
    </section>
  );
}
