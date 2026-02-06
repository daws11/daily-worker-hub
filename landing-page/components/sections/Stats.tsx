"use client";

import { motion, useInView } from "framer-motion";
import { useEffect, useRef, useState } from "react";
import { TextGenerateEffect } from "@/components/ui/text-generate-effect";
import { Users, Building, Star, CheckCircle, TrendingUp, Award } from "lucide-react";
import { STATS } from "@/lib/constants";

// Counter component with animation
function Counter({ end, duration = 2000, suffix = "" }: { end: number; duration?: number; suffix?: string }) {
  const [count, setCount] = useState(0);
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, margin: "-100px" });

  useEffect(() => {
    if (!isInView) return;

    let startTime: number;
    let animationFrame: number;

    const animate = (timestamp: number) => {
      if (!startTime) startTime = timestamp;
      const progress = Math.min((timestamp - startTime) / duration, 1);
      
      // Easing function
      const easeOutQuart = 1 - Math.pow(1 - progress, 4);
      
      setCount(Math.floor(easeOutQuart * end));

      if (progress < 1) {
        animationFrame = requestAnimationFrame(animate);
      }
    };

    animationFrame = requestAnimationFrame(animate);

    return () => cancelAnimationFrame(animationFrame);
  }, [isInView, end, duration]);

  return <span ref={ref}>{count}{suffix}</span>;
}

export function Stats() {
  const mainStats = [
    { value: STATS.workers, label: 'Active Workers', icon: Users, color: 'from-green-500 to-emerald-500', trend: '+12%' },
    { value: STATS.businesses, label: 'Businesses', icon: Building, color: 'from-blue-500 to-cyan-500', trend: '+8%' },
    { value: STATS.jobsCompleted, label: 'Jobs Completed', icon: CheckCircle, color: 'from-purple-500 to-pink-500', trend: '+15%' },
    { value: STATS.averageRating * 10, label: 'Rating (★)', icon: Star, color: 'from-yellow-500 to-amber-500', suffix: '.0★', rawValue: STATS.averageRating, trend: '+0.1' }
  ];

  const communityStats = [
    { value: STATS.communityMembers, label: 'Community Members', icon: Users, color: 'from-indigo-500 to-violet-500' },
    { value: STATS.satisfactionRate, label: 'Satisfaction Rate', icon: TrendingUp, color: 'from-green-500 to-teal-500', suffix: '%' },
    { value: STATS.dailyActiveDiscussions, label: 'Daily Discussions', icon: Award, color: 'from-orange-500 to-red-500' },
    { value: STATS.resourcesShared, label: 'Resources Shared', icon: CheckCircle, color: 'from-blue-500 to-cyan-500' }
  ];

  return (
    <section className="py-24 relative overflow-hidden bg-gradient-to-b from-gray-50 to-white">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute top-20 left-1/4 w-96 h-96 bg-green-100 rounded-full blur-3xl opacity-40" />
        <div className="absolute bottom-20 right-1/4 w-96 h-96 bg-blue-100 rounded-full blur-3xl opacity-40" />
      </div>

      <div className="container mx-auto px-4 relative z-10">
        {/* Section header */}
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
            Trusted by
          </motion.span>
          
          <h2 className="text-4xl md:text-6xl font-bold mb-4">
            Numbers That{' '}
            <TextGenerateEffect
              words="Matter"
              className="text-transparent bg-clip-text bg-gradient-to-r from-green-600 to-blue-600"
              duration={0.3}
            />
          </h2>

          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            Real results from real people
          </p>
        </motion.div>

        {/* Main Stats Grid - With Counter Animation */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
          {mainStats.map((stat, index) => (
            <motion.div
              key={stat.label}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: '-100px' }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              whileHover={{ 
                scale: 1.05,
                y: -8,
                boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.15)'
              }}
              className="relative group bg-white p-8 rounded-2xl border border-gray-100 shadow-sm hover:shadow-xl transition-all duration-300"
            >
              {/* Icon Container */}
              <div className={`inline-flex p-4 rounded-xl bg-gradient-to-br ${stat.color} mb-4 group-hover:scale-110 transition-transform duration-300`}>
                <stat.icon className="w-6 h-6 text-white" />
              </div>

              {/* Counter */}
              <div className="mb-2">
                {stat.rawValue ? (
                  <div className="flex items-baseline gap-2">
                    <span className="text-5xl font-bold text-gray-900">
                      {stat.rawValue}
                    </span>
                    <span className="text-2xl text-yellow-500">★</span>
                  </div>
                ) : (
                  <div className="flex items-baseline gap-2">
                    <span className="text-5xl font-bold text-gray-900">
                      <Counter end={stat.value} duration={2000} suffix="+" />
                    </span>
                  </div>
                )}
              </div>

              {/* Label */}
              <p className="text-gray-600 font-medium mb-2">
                {stat.label}
              </p>

              {/* Trend indicator */}
              <motion.div
                initial={{ opacity: 0 }}
                whileInView={{ opacity: 1 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: 0.8 + index * 0.1 }}
                className="flex items-center gap-1 text-green-600 text-sm font-semibold"
              >
                <TrendingUp className="w-4 h-4" />
                <span>{stat.trend} this month</span>
              </motion.div>
            </motion.div>
          ))}
        </div>

        {/* Community Stats Grid */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.4 }}
          className="bg-gradient-to-br from-gray-900 to-gray-800 p-8 md:p-12 rounded-3xl"
        >
          <div className="text-center mb-8">
            <h3 className="text-2xl md:text-3xl font-bold text-white mb-2">
              Community Stats
            </h3>
            <p className="text-gray-400">
              See how our community is growing
            </p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            {communityStats.map((stat, index) => (
              <motion.div
                key={stat.label}
                initial={{ opacity: 0, scale: 0.8 }}
                whileInView={{ opacity: 1, scale: 1 }}
                viewport={{ once: true, margin: '-100px' }}
                transition={{ duration: 0.5, delay: 0.5 + index * 0.1 }}
                whileHover={{ scale: 1.05 }}
                className="text-center"
              >
                <div className={`inline-flex p-3 rounded-xl bg-gradient-to-br ${stat.color} mb-4 mx-auto`}>
                  <stat.icon className="w-5 h-5 text-white" />
                </div>

                <p className="text-3xl md:text-4xl font-bold text-white mb-2">
                  <Counter 
                    end={stat.value} 
                    duration={2000} 
                    suffix={stat.suffix || "+"}
                  />
                </p>

                <p className="text-sm text-gray-400">
                  {stat.label}
                </p>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* Simple CTA */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.8 }}
          className="text-center mt-12"
        >
          <motion.p
            className="text-gray-600 text-lg"
            whileHover={{ scale: 1.02 }}
          >
            Join <span className="font-bold text-green-600">500+</span> workers & <span className="font-bold text-blue-600">120+</span> businesses today
          </motion.p>
        </motion.div>
      </div>
    </section>
  );
}
