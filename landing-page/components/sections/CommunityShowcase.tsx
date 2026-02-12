/**
 * Community Showcase Section
 * Enhanced Live Activity Feed + Community Highlights
 */

'use client';

import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Users, MessageCircle, BookOpen, TrendingUp, Star, Sparkles, Share2, Zap, Heart, Briefcase, UserPlus, LucideIcon } from 'lucide-react';

type Activity = {
  user: string;
  action: string;
  time: string;
  type: string;
  icon: LucideIcon;
};

export function CommunityShowcase() {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [activeTab, setActiveTab] = useState<'live' | 'discussions' | 'resources'>('live');

  useEffect(() => {
    // Simulate real-time activities
    const mockActivities = [
      { user: 'Ketut Dewi', action: 'applied for a new job', time: 'Just now', type: 'job', icon: Briefcase },
      { user: 'Hotel Ubud', action: 'posted a new job', time: '2 sec ago', type: 'job', icon: Briefcase },
      { user: 'Made Surya', action: 'joined the community', time: '34 sec ago', type: 'join', icon: UserPlus },
      { user: 'Warung Bali', action: 'gave 5★ rating', time: '45 sec ago', type: 'rating', icon: Star },
      { user: 'Cafe Seminyak', action: 'hired a worker', time: '1 min ago', type: 'hire', icon: Users },
      { user: 'Komang Eka', action: 'shared a resource', time: '1 min ago', type: 'share', icon: Share2 },
      { user: 'Putu Ayu', action: 'started a discussion', time: '2 min ago', type: 'discussion', icon: MessageCircle },
    ];

    setActivities(mockActivities);

    const interval = setInterval(() => {
      const newActivity: Activity = {
        user: ['Ketut', 'Made', 'Wayan', 'Putu', 'Komang', 'Nyoman'][Math.floor(Math.random() * 7)] + ' ' +
              ['Dewi', 'Surya', 'Bagus', 'Ayu', 'Eka', 'Made', 'Putu'][Math.floor(Math.random() * 8)],
        action: [
          'applied for a job',
          'posted a new job',
          'shared a tip',
          'joined a discussion',
          'gave a 5★ rating',
          'completed 50 jobs',
          'started a new job',
        ][Math.floor(Math.random() * 7)],
        time: 'Just now',
        type: ['job', 'join', 'share', 'rating', 'discussion'][Math.floor(Math.random() * 5)],
        icon: [Briefcase, UserPlus, Share2, Star, MessageCircle][Math.floor(Math.random() * 5)],
      };

      setActivities((prev) => [newActivity, ...prev].slice(0, 8));
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  const communityHighlights = [
    {
      icon: TrendingUp,
      title: '2,000+ Members',
      description: 'Growing community of workers and businesses',
      color: 'from-primary/20 to-primary/5',
      iconColor: 'text-primary',
      stat: '+18% this month',
    },
    {
      icon: MessageCircle,
      title: '150+ Daily Discussions',
      description: 'Active conversations, knowledge sharing, and support',
      color: 'from-community/20 to-community/5',
      iconColor: 'text-community',
      stat: 'Active community',
    },
    {
      icon: BookOpen,
      title: '50+ Educational Resources',
      description: 'Tips, guides, and best practices shared daily',
      color: 'from-accent/20 to-accent/5',
      iconColor: 'text-accent',
      stat: 'Growing daily',
    },
  ];

  const featuredStories = [
    {
      user: 'Ketut Dewi',
      role: 'Housekeeping Staff',
      story: '"Di Daily Worker Hub, saya tidak cuma dapat job — saya juga temukan mentor, teman-teman yang share tips, bahkan rekomendasi job dari businesses yang mereka kenal. Kita saling bantu!"',
      rating: 5,
      avatar: '👩‍💼',
    },
    {
      user: 'Made Suryawan',
      role: 'Manager, Hotel Ubud',
      story: '"Daily Worker Hub membantu kami tidak hanya menemukan staff, tapi juga connect dengan businesses lain di Bali. Kami share best practices, bahkan partner dengan cafe lain saat busy season. This is truly a community!"',
      rating: 5,
      avatar: '👨‍💼',
    },
  ];

  return (
    <section id="community" className="py-32 relative overflow-hidden">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-background via-primary/[0.02] to-background" />

      <div className="container mx-auto px-4 relative z-10">
        {/* Section header */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-100px' }}
          transition={{ duration: 0.6 }}
          className="text-center mb-20"
        >
          <div className="inline-flex items-center gap-2 mb-6">
            <Sparkles className="h-6 w-6 text-community" />
            <span className="text-sm font-semibold text-community uppercase tracking-wider">Community-First Platform</span>
            <Sparkles className="h-6 w-6 text-community" />
          </div>

          <h2 className="text-5xl md:text-7xl font-bold mb-6">
            Vibrant{' '}
            <span className="bg-gradient-to-r from-community via-primary to-accent bg-clip-text text-transparent">
              Community
            </span>
          </h2>

          <p className="text-xl text-muted-foreground max-w-3xl mx-auto">
            More than just transactions — we're building connections that matter
          </p>
        </motion.div>

        {/* Tabs */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="flex justify-center gap-4 mb-16"
        >
          {[
            { id: 'live' as const, label: 'Live Activity', icon: Zap },
            { id: 'discussions' as const, label: 'Discussions', icon: MessageCircle },
            { id: 'resources' as const, label: 'Resources', icon: BookOpen },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`relative px-6 py-3 rounded-full font-semibold text-base transition-all duration-300 ${
                activeTab === tab.id
                  ? 'bg-gradient-to-r from-community to-primary text-white shadow-lg shadow-community/25 scale-105'
                  : 'bg-background text-foreground border-2 border-border/50 hover:border-community/50 hover:bg-secondary/30'
              }`}
            >
              <span className="relative z-10 flex items-center gap-3">
                <tab.icon className="h-5 w-5" />
                {tab.label}
              </span>
              {activeTab === tab.id && (
                <motion.div
                  layoutId="activeCommunityTab"
                  className="absolute inset-0 rounded-full"
                  initial={false}
                  transition={{ type: 'spring', stiffness: 500, damping: 30 }}
                />
              )}
            </button>
          ))}
        </motion.div>

        {/* Tab content */}
        <AnimatePresence mode="wait">
          {activeTab === 'live' && (
            <motion.div
              key="live"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.4 }}
              className="grid grid-cols-1 md:grid-cols-2 gap-12 mb-20"
            >
              {/* Live Activity Feed */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: 0.1 }}
                whileHover={{ scale: 1.02 }}
                className="relative bg-gradient-to-br from-card via-card to-secondary/50 backdrop-blur-md rounded-3xl p-6 border border-border/50 hover:border-primary/30 transition-all duration-300"
              >
                <div className="flex items-center gap-3 mb-6">
                  <div className="w-12 h-12 rounded-full bg-gradient-to-br from-primary/20 to-primary/10 flex items-center justify-center">
                    <Zap className="h-6 w-6 text-primary animate-pulse" />
                  </div>
                  <div>
                    <h3 className="text-xl font-bold">Live Activity Feed</h3>
                    <p className="text-sm text-muted-foreground">Real-time community engagement</p>
                  </div>
                  <div className="ml-auto">
                    <div className="flex items-center gap-2 px-3 py-1 bg-background/80 backdrop-blur-sm rounded-full border border-border/50">
                      <div className="w-2 h-2 rounded-full bg-primary animate-pulse" />
                      <span className="text-sm font-medium">{activities.length} active</span>
                    </div>
                  </div>
                </div>

                <div className="space-y-3 max-h-[400px] overflow-y-auto scrollbar-hide">
                  {activities.map((activity, index) => (
                    <motion.div
                      key={`${activity.user}-${index}`}
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ duration: 0.3 }}
                      className="flex items-start gap-4 p-4 bg-background/50 backdrop-blur-sm rounded-2xl border border-border/30"
                    >
                      <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary/10 to-primary/5 flex items-center justify-center shrink-0">
                        <activity.icon className="h-5 w-5 text-primary" />
                      </div>
                      <div className="flex-1">
                        <p className="text-sm">
                          <span className="font-semibold text-foreground">{activity.user}</span>
                          <span className="text-muted-foreground"> {activity.action}</span>
                        </p>
                        <p className="text-xs text-muted-foreground">{activity.time}</p>
                      </div>
                      <Heart className="h-4 w-4 text-red-500 shrink-0" />
                    </motion.div>
                  ))}
                </div>
              </motion.div>

              {/* Community Stats */}
              <div className="space-y-6">
                {communityHighlights.map((highlight, index) => (
                  <motion.div
                    key={index}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5, delay: index * 0.1 }}
                    whileHover={{ scale: 1.02, y: -5 }}
                    className="relative bg-gradient-to-br from-card via-card to-secondary/50 backdrop-blur-md rounded-3xl p-6 border border-border/50 hover:border-primary/30 transition-all duration-300"
                  >
                    <div className="absolute inset-0 bg-gradient-to-br from-card via-card to-secondary/50 rounded-3xl blur-xl hover:opacity-80 transition-opacity" />
                    <div className="relative">
                      <div className={`w-12 h-12 rounded-2xl bg-gradient-to-br ${highlight.color} flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
                        <highlight.icon className={`h-6 w-6 ${highlight.iconColor}`} />
                      </div>
                      <h3 className="text-xl font-bold mb-2">{highlight.title}</h3>
                      <p className="text-sm text-muted-foreground mb-3">{highlight.description}</p>
                      <div className="inline-flex items-center gap-2 text-xs font-semibold text-primary bg-primary/10 px-3 py-1 rounded-full">
                        {highlight.stat}
                      </div>
                    </div>
                  </motion.div>
                ))}
              </div>
            </motion.div>
          )}

          {activeTab === 'discussions' && (
            <motion.div
              key="discussions"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.4 }}
              className="grid grid-cols-1 md:grid-cols-2 gap-8"
            >
              {featuredStories.map((story, index) => (
                <motion.div
                  key={index}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.5, delay: index * 0.1 }}
                  whileHover={{ scale: 1.02, y: -5 }}
                  className="relative bg-gradient-to-br from-card via-card to-secondary/50 backdrop-blur-md rounded-3xl p-8 border border-border/50 hover:border-primary/30 transition-all duration-300"
                >
                  <div className="flex items-start gap-6 mb-6">
                    <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-community/20 to-primary/20 flex items-center justify-center shrink-0">
                      <span className="text-4xl">{story.avatar}</span>
                    </div>
                    <div className="flex-1">
                      <h4 className="text-xl font-bold mb-2">{story.user}</h4>
                      <p className="text-sm text-primary font-medium mb-3">{story.role}</p>
                      <p className="text-base text-muted-foreground leading-relaxed">
                        {story.story}
                      </p>
                      <div className="flex items-center gap-1 mt-4">
                        {[1, 2, 3, 4, 5].map((star) => (
                          <Star
                            key={star}
                            className={`h-5 w-5 ${star <= story.rating ? 'text-yellow-500' : 'text-muted-foreground/30'}`}
                            fill={star <= story.rating ? "currentColor" : "none"}
                          />
                        ))}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 pt-6 border-t border-border/50">
                    <Heart className="h-4 w-4 text-red-500" />
                    <span className="text-sm text-muted-foreground">
                      <span className="font-semibold text-foreground">Helpful?</span>
                    {' '}
                      <button className="text-primary hover:underline font-medium">
                        Like
                      </button>
                    </span>
                  </div>
                </motion.div>
              ))}
            </motion.div>
          )}

          {activeTab === 'resources' && (
            <motion.div
              key="resources"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.4 }}
              className="relative bg-gradient-to-br from-card via-card to-primary/5 backdrop-blur-md rounded-3xl p-8 border border-border/50"
            >
              <div className="absolute inset-0 bg-gradient-to-r from-primary/5 via-accent/5 to-community/5" />
              <div className="relative z-10">
                <h3 className="text-2xl font-bold mb-6 flex items-center gap-3">
                  <BookOpen className="h-8 w-8 text-accent" />
                  Educational Resources
                </h3>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  {[
                    { title: 'Worker Guides', count: 25, icon: Users },
                    { title: 'Job Tips', count: 15, icon: Briefcase },
                    { title: 'Best Practices', count: 10, icon: Star },
                  ].map((item, index) => (
                    <motion.div
                      key={index}
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.5, delay: index * 0.1 }}
                      whileHover={{ scale: 1.05, y: -5 }}
                      className="relative bg-background/80 backdrop-blur-sm rounded-2xl p-6 border border-border/50 hover:border-primary/30 transition-all duration-300"
                    >
                      <item.icon className="h-6 w-6 text-primary mb-4" />
                      <h4 className="text-lg font-bold mb-2">{item.title}</h4>
                      <p className="text-2xl font-bold text-primary">{item.count}+</p>
                      <p className="text-sm text-muted-foreground">Resources</p>
                    </motion.div>
                  ))}
                </div>

                <motion.button
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.5, delay: 0.4 }}
                  whileHover={{ scale: 1.05 }}
                  className="w-full mt-8 px-8 py-4 bg-gradient-to-r from-community to-primary text-white font-semibold rounded-full hover:scale-105 transition-all duration-300"
                >
                  Explore All Resources
                  <Share2 className="inline-block h-5 w-5 ml-2" />
                </motion.button>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* CTA Banner */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-100px' }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="mt-20"
        >
          <div className="relative bg-gradient-to-br from-card via-card to-primary/10 backdrop-blur-md rounded-3xl p-12 border border-border/50 overflow-hidden">
            <motion.div
              className="absolute inset-0 bg-gradient-to-r from-primary/10 via-accent/10 to-community/10"
              animate={{
                opacity: [0.3, 0.6, 0.3],
              }}
              transition={{
                duration: 4,
                repeat: Infinity,
                ease: 'easeInOut',
              }}
            />

            <div className="relative z-10 text-center">
              <div className="inline-flex items-center gap-3 mb-6">
                <Users className="h-8 w-8 text-primary" />
                <Heart className="h-8 w-8 text-red-500" />
                <Sparkles className="h-8 w-8 text-community" />
              </div>

              <h3 className="text-3xl md:text-4xl font-bold mb-6">
                Join Our{' '}
                <span className="bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">
                  Growing Community
                </span>
              </h3>

              <p className="text-xl text-muted-foreground max-w-3xl mx-auto mb-8">
                Connect with workers and businesses, share knowledge, and grow together in Bali's hospitality ecosystem
              </p>

              <motion.button
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: 0.4 }}
                whileHover={{ scale: 1.05 }}
                className="px-12 py-5 bg-gradient-to-r from-primary to-accent text-white text-lg font-semibold rounded-full hover:scale-105 transition-all duration-300 shadow-xl shadow-primary/25"
              >
                Join Community Now
                <TrendingUp className="inline-block h-6 w-6 ml-2" />
              </motion.button>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  );
}
