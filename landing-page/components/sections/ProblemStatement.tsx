"use client";

import { motion } from 'framer-motion';
import { AlertCircle, Search, Users, Sparkles, ArrowRight } from 'lucide-react';
import { useLanguage } from '@/lib/language-context';

export function ProblemStatement() {
  const { t } = useLanguage();

  const challenges = [
    {
      icon: AlertCircle,
      title: t('problem.noCommunity.title'),
      description: t('problem.noCommunity.description'),
      color: 'from-red-500 to-orange-500'
    },
    {
      icon: Search,
      title: t('problem.noShow.title'),
      description: t('problem.noShow.description'),
      color: 'from-orange-500 to-yellow-500'
    },
    {
      icon: Users,
      title: t('problem.limitedNetwork.title'),
      description: t('problem.limitedNetwork.description'),
      color: 'from-yellow-500 to-green-500'
    }
  ];
  return (
    <section className="py-24 relative overflow-hidden bg-gradient-to-b from-white to-gray-50">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute top-20 right-1/4 w-96 h-96 bg-red-100 rounded-full blur-3xl opacity-30" />
        <div className="absolute bottom-20 left-1/4 w-96 h-96 bg-orange-100 rounded-full blur-3xl opacity-30" />
      </div>

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
            className="inline-block px-4 py-2 mb-4 text-sm font-semibold text-red-600 bg-red-50 rounded-full"
          >
            {t('problem.badge')}
          </motion.span>

          <h2 className="text-4xl md:text-6xl font-bold mb-4">
            {t('problem.headline')}{' '}
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-red-600 to-orange-600">
              {t('problem.headline.highlight')}
            </span>
          </h2>

          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            {t('problem.subtitle')}
          </p>
        </motion.div>

        {/* Challenges grid - Card-based with short copy */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          {challenges.map((challenge, index) => (
            <motion.div
              key={challenge.title}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: '-50px' }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              whileHover={{ 
                scale: 1.03,
                y: -8,
                boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.15)'
              }}
              className="relative group"
            >
              <div className={`absolute inset-0 bg-gradient-to-br ${challenge.color} rounded-2xl blur opacity-20 group-hover:opacity-40 transition-opacity duration-300`} />
              <div className="relative bg-white p-6 rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 h-full border border-gray-100">
                
                {/* Icon Container */}
                <div className={`inline-flex p-3 rounded-xl bg-gradient-to-br ${challenge.color} mb-4 group-hover:scale-110 transition-transform duration-300`}>
                  <challenge.icon className="w-5 h-5 text-white" />
                </div>

                {/* Content - Short & punchy */}
                <h3 className="text-xl font-bold text-gray-900 mb-2">
                  {challenge.title}
                </h3>
                <p className="text-gray-600 text-sm leading-relaxed">
                  {challenge.description}
                </p>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Transition card - Short copy */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.3 }}
        >
          <div className="relative bg-gradient-to-br from-gray-900 to-gray-800 p-8 md:p-12 rounded-3xl overflow-hidden">
            {/* Animated gradient overlay */}
            <motion.div
              className="absolute inset-0 bg-gradient-to-r from-green-500/10 via-blue-500/10 to-purple-500/10"
              animate={{
                opacity: [0.3, 0.6, 0.3],
              }}
              transition={{
                duration: 4,
                repeat: Infinity,
                ease: 'easeInOut',
              }}
            />

            <div className="relative z-10 text-center md:text-left md:flex md:items-center md:gap-8">
              <div className="shrink-0 mb-6 md:mb-0">
                <div className="w-20 h-20 rounded-3xl bg-gradient-to-br from-green-500/20 to-blue-500/20 flex items-center justify-center mx-auto md:mx-0">
                  <Sparkles className="h-10 w-10 text-green-400" />
                </div>
              </div>

              <div className="flex-1">
                <h3 className="text-2xl md:text-3xl font-bold text-white mb-3">
                  {t('problem.different.title')}
                </h3>

                <p className="text-gray-300 text-lg mb-6">
                  {t('problem.different.description')}
                </p>

                <div className="flex flex-wrap gap-3 justify-center md:justify-start">
                  <div className="px-4 py-2 bg-white/10 backdrop-blur-sm rounded-full border border-white/20">
                    <span className="text-sm font-medium text-white">📚 Knowledge</span>
                  </div>
                  <div className="px-4 py-2 bg-white/10 backdrop-blur-sm rounded-full border border-white/20">
                    <span className="text-sm font-medium text-white">🤝 Network</span>
                  </div>
                  <div className="px-4 py-2 bg-white/10 backdrop-blur-sm rounded-full border border-white/20">
                    <span className="text-sm font-medium text-white">💡 Support</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  );
}
