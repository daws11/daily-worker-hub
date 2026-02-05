"use client";

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { ChevronLeft, ChevronRight, Play } from "lucide-react";
import { APP_STORES } from "@/lib/constants";

const screenshots = [
  {
    title: "Home Screen - Job Feed",
    description: "Browse available jobs in your area with smart filters",
    color: "from-blue-500 to-cyan-500"
  },
  {
    title: "Worker View - Available Jobs",
    description: "Apply to jobs that match your skills and schedule",
    color: "from-green-500 to-emerald-500"
  },
  {
    title: "Business View - Post Job",
    description: "Create and manage job postings effortlessly",
    color: "from-purple-500 to-pink-500"
  },
  {
    title: "Real-Time Chat",
    description: "Instant messaging with businesses and workers",
    color: "from-orange-500 to-red-500"
  },
  {
    title: "Profile & Stats",
    description: "Track your ratings, reviews, and earnings",
    color: "from-indigo-500 to-violet-500"
  }
];

export function AppScreenshots() {
  const [currentIndex, setCurrentIndex] = useState(0);

  const nextSlide = () => {
    setCurrentIndex((prev) => (prev + 1) % screenshots.length);
  };

  const prevSlide = () => {
    setCurrentIndex((prev) => (prev - 1 + screenshots.length) % screenshots.length);
  };

  return (
    <section className="relative py-24 overflow-hidden bg-gradient-to-b from-white to-slate-50">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute top-20 right-1/4 w-96 h-96 bg-purple-100 rounded-full blur-3xl opacity-40" />
        <div className="absolute bottom-20 left-1/4 w-96 h-96 bg-blue-100 rounded-full blur-3xl opacity-40" />
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
            className="inline-block px-4 py-2 mb-4 text-sm font-semibold text-purple-600 bg-purple-50 rounded-full"
          >
            App Preview
          </motion.span>
          <h2 className="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
            Lihat App Kami dalam <span className="text-transparent bg-clip-text bg-gradient-to-r from-purple-600 to-blue-600">Aksi</span>
          </h2>
          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            Intuitive interface, powerful features. Everything you need at your fingertips
          </p>
        </motion.div>

        {/* Carousel */}
        <div className="relative max-w-5xl mx-auto">
          {/* Main Screenshot */}
          <AnimatePresence mode="wait">
            <motion.div
              key={currentIndex}
              initial={{ opacity: 0, x: 100 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -100 }}
              transition={{ duration: 0.5 }}
              className="relative"
            >
              {/* Phone Frame */}
              <div className="relative mx-auto max-w-sm">
                {/* Phone Mockup */}
                <div className="relative bg-gradient-to-b from-gray-900 to-gray-800 rounded-[3rem] p-3 shadow-2xl">
                  {/* Notch */}
                  <div className="absolute top-6 left-1/2 -translate-x-1/2 w-32 h-7 bg-black rounded-full" />

                  {/* Screen Content */}
                  <div className="relative bg-gradient-to-br from-gray-50 to-gray-100 rounded-[2.5rem] overflow-hidden aspect-[9/19]">
                    {/* App Header */}
                    <div className="bg-gradient-to-r from-green-600 to-blue-600 p-6 pb-12">
                      <div className="flex items-center justify-between mb-4">
                        <div className="w-8 h-8 bg-white/20 rounded-lg" />
                        <div className="w-8 h-8 bg-white/20 rounded-lg" />
                      </div>
                      <div className="h-6 bg-white/30 rounded w-3/4" />
                    </div>

                    {/* App Body - Placeholder for screenshot */}
                    <div className="p-6 space-y-4">
                      {Array.from({ length: 4 }).map((_, i) => (
                        <div key={i} className="bg-white p-4 rounded-xl shadow-sm">
                          <div className="flex items-start gap-3">
                            <div className="w-10 h-10 bg-gradient-to-r from-purple-500 to-pink-500 rounded-lg shrink-0" />
                            <div className="flex-1 space-y-2">
                              <div className="h-4 bg-gray-200 rounded w-3/4" />
                              <div className="h-3 bg-gray-100 rounded w-1/2" />
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* Bottom Nav */}
                    <div className="absolute bottom-0 left-0 right-0 bg-white border-t border-gray-200 p-4">
                      <div className="flex justify-around">
                        {Array.from({ length: 4 }).map((_, i) => (
                          <div key={i} className="w-8 h-8 bg-gray-100 rounded-lg" />
                        ))}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Shadow/Reflection */}
                <div className="absolute -bottom-8 left-1/2 -translate-x-1/2 w-3/4 h-8 bg-black/20 blur-xl rounded-full" />
              </div>

              {/* Info Card */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: 0.3 }}
                className="absolute top-0 right-0 -mr-4 mt-8 bg-white p-6 rounded-2xl shadow-xl max-w-xs hidden lg:block"
              >
                <div className={`inline-flex p-2 rounded-lg bg-gradient-to-r ${screenshots[currentIndex].color} mb-3`}>
                  <Play className="w-4 h-4 text-white" />
                </div>
                <h3 className="text-lg font-bold text-gray-900 mb-2">
                  {screenshots[currentIndex].title}
                </h3>
                <p className="text-sm text-gray-600">
                  {screenshots[currentIndex].description}
                </p>
              </motion.div>
            </motion.div>
          </AnimatePresence>

          {/* Navigation Buttons */}
          <div className="absolute top-1/2 left-0 right-0 -translate-y-1/2 flex justify-between -mx-4 px-4">
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
              onClick={prevSlide}
              className="p-3 bg-white rounded-full shadow-lg hover:shadow-xl transition-all"
            >
              <ChevronLeft className="w-6 h-6 text-gray-700" />
            </motion.button>
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
              onClick={nextSlide}
              className="p-3 bg-white rounded-full shadow-lg hover:shadow-xl transition-all"
            >
              <ChevronRight className="w-6 h-6 text-gray-700" />
            </motion.button>
          </div>

          {/* Navigation Dots */}
          <div className="flex justify-center gap-2 mt-8">
            {screenshots.map((_, index) => (
              <motion.button
                key={index}
                whileHover={{ scale: 1.2 }}
                whileTap={{ scale: 0.8 }}
                onClick={() => setCurrentIndex(index)}
                className={`w-3 h-3 rounded-full transition-all duration-300 ${
                  index === currentIndex
                    ? 'w-8 bg-gradient-to-r from-purple-600 to-blue-600'
                    : 'bg-gray-300 hover:bg-gray-400'
                }`}
              />
            ))}
          </div>

          {/* Mobile Info Card */}
          <div className="mt-8 text-center lg:hidden">
            <motion.div
              key={currentIndex}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="bg-white p-4 rounded-xl shadow-lg inline-block"
            >
              <h3 className="text-lg font-bold text-gray-900 mb-1">
                {screenshots[currentIndex].title}
              </h3>
              <p className="text-sm text-gray-600">
                {screenshots[currentIndex].description}
              </p>
            </motion.div>
          </div>
        </div>

        {/* Download CTA */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.4 }}
          className="text-center mt-16"
        >
          <p className="text-gray-600 mb-6">
            Download sekarang dan rasakan pengalaman baru
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
