/**
 * CommunityFirstApproach Section
 * "What Makes Us Different" - Highlight community as USP
 */

'use client';

import { motion, AnimatePresence } from 'motion/react';
import { Shield, BookOpen, Network, Sparkles, ArrowRight, Users, Zap } from 'lucide-react';
import { BackgroundBeams } from '@/components/ui/background-beams';
import { BentoGrid, BentoGridItem } from '@/components/ui/bento-grid';

export function CommunityFirstApproach() {
  const pillars = [
    {
      icon: Shield,
      title: 'Built on Trust',
      description: 'Kita bukan sekadar menghubungkan workers dengan businesses. Kita membangun komunitas berbasis kepercayaan dimana setiap member saling mendukung dan mempertahankan reputasi.',
      color: 'from-primary/20 to-primary/5',
      iconColor: 'text-primary',
      number: '01',
    },
    {
      icon: BookOpen,
      title: 'Knowledge Sharing',
      description: 'Komunitas kami bukan hanya untuk mencari job. Workers share tips, strategies, dan best practices. Businesses share operational insights dan success stories. Together, we grow.',
      color: 'from-accent/20 to-accent/5',
      iconColor: 'text-accent',
      number: '02',
    },
    {
      icon: Network,
      title: 'Network Effect',
      description: 'Koneksi kamu bertambah seiring berjalannya waktu. Workers bertemu workers lain di komunitas, expand network mereka. Businesses menemukan partners dan suppliers. Ekosistem yang saling menguntungkan.',
      color: 'from-community/20 to-community/5',
      iconColor: 'text-community',
      number: '03',
    },
  ];

  return (
    <section className="py-32 relative overflow-hidden bg-gradient-to-b from-background via-primary/[0.02] to-background">
      {/* Background pattern */}
      <div className="absolute inset-0">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_50%,rgba(19,236,91,0.03)_0%,transparent_50%)]" />
      </div>

      <div className="container mx-auto px-4 relative z-10">
        {/* Section header */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-100px' }}
          transition={{ duration: 0.6 }}
          className="text-center mb-20"
        >
          <div className="inline-flex items-center gap-3 mb-6">
            <div className="w-2 h-2 rounded-full bg-primary" />
            <span className="text-sm font-semibold text-primary uppercase tracking-wider">What Makes Us Different</span>
            <div className="w-2 h-2 rounded-full bg-primary" />
          </div>

          <h2 className="text-5xl md:text-7xl font-bold mb-6">
            <span className="bg-gradient-to-r from-foreground via-primary to-accent bg-clip-text text-transparent">
              Community-First
            </span>
            {' '}
            Approach
          </h2>

          <p className="text-xl text-muted-foreground max-w-3xl mx-auto">
            Bukan sekadar marketplace — kami membangun{' '}
            <span className="text-foreground font-semibold">komunitas yang berarti</span>
          </p>
        </motion.div>

        {/* Pillars of Community */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-20">
          {pillars.map((pillar, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: '-50px' }}
              transition={{ duration: 0.5, delay: index * 0.15 }}
              whileHover={{ scale: 1.03, y: -8 }}
              className="relative group"
            >
              <div className="absolute inset-0 bg-gradient-to-br from-card via-card to-secondary/50 rounded-3xl blur-xl group-hover:opacity-80 transition-opacity" />
              <div className="relative bg-gradient-to-br bg-card rounded-3xl p-8 border border-border/50 group-hover:border-primary/30 transition-all duration-300">
                {/* Number badge */}
                <div className="absolute -top-4 -right-4 w-12 h-12 rounded-full bg-gradient-to-br from-primary to-accent flex items-center justify-center text-white font-bold text-lg shadow-lg">
                  {pillar.number}
                </div>

                <div className={`w-16 h-16 rounded-2xl bg-gradient-to-br ${pillar.color} flex items-center justify-center mb-6 group-hover:scale-110 transition-transform`}>
                  <pillar.icon className={`h-8 w-8 ${pillar.iconColor}`} />
                </div>

                <h3 className="text-2xl font-bold mb-4">{pillar.title}</h3>
                <p className="text-lg text-muted-foreground leading-relaxed">
                  {pillar.description}
                </p>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Interactive Network Section */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-100px' }}
          transition={{ duration: 0.6, delay: 0.3 }}
        >
          <div className="relative bg-gradient-to-br from-card via-card to-primary/5 backdrop-blur-md rounded-3xl p-12 border border-border/50 overflow-hidden">
            {/* BackgroundBeams Effect */}
            <BackgroundBeams className="opacity-30" />

            <div className="relative z-10">
              {/* Section Header */}
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5 }}
                className="flex items-center gap-6 mb-12"
              >
                <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-primary/20 to-accent/20 flex items-center justify-center shadow-lg">
                  <Network className="h-10 w-10 text-primary" />
                </div>
                <div>
                  <motion.h3
                    className="text-4xl font-bold mb-2 bg-gradient-to-r from-foreground via-primary to-accent bg-clip-text text-transparent"
                  >
                    Interactive Community Network
                  </motion.h3>
                  <p className="text-lg text-muted-foreground">
                    Hover over nodes untuk lihat koneksi aktif & statistik real-time
                  </p>
                </div>
              </motion.div>

              {/* Bento Grid for Network Stats */}
              <BentoGrid className="mb-12 max-w-5xl mx-auto">
                <BentoGridItem
                  header={
                    <div className="flex flex-col items-center justify-center h-full">
                      <motion.div
                        animate={{ scale: [1, 1.1, 1] }}
                        transition={{ duration: 2, repeat: Infinity }}
                        className="text-5xl font-bold bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent"
                      >
                        5K+
                      </motion.div>
                      <div className="text-sm text-muted-foreground mt-2">Active Workers</div>
                    </div>
                  }
                  icon={<Users className="h-6 w-6 text-primary" />}
                  title="Talent Pool"
                  description="Skilled professionals ready to collaborate"
                />
                <BentoGridItem
                  header={
                    <div className="flex flex-col items-center justify-center h-full">
                      <motion.div
                        animate={{ scale: [1, 1.1, 1] }}
                        transition={{ duration: 2, repeat: Infinity, delay: 0.3 }}
                        className="text-5xl font-bold bg-gradient-to-r from-accent to-community bg-clip-text text-transparent"
                      >
                        2K+
                      </motion.div>
                      <div className="text-sm text-muted-foreground mt-2">Business Partners</div>
                    </div>
                  }
                  icon={<BookOpen className="h-6 w-6 text-accent" />}
                  title="Business Network"
                  description="Growing businesses seeking talent"
                />
                <BentoGridItem
                  header={
                    <div className="flex flex-col items-center justify-center h-full">
                      <motion.div
                        animate={{ scale: [1, 1.1, 1] }}
                        transition={{ duration: 2, repeat: Infinity, delay: 0.6 }}
                        className="text-5xl font-bold bg-gradient-to-r from-community to-primary bg-clip-text text-transparent"
                      >
                        15K+
                      </motion.div>
                      <div className="text-sm text-muted-foreground mt-2">Connections Made</div>
                    </div>
                  }
                  icon={<Zap className="h-6 w-6 text-community" />}
                  title="Active Connections"
                  description="Successful matches & collaborations"
                />
              </BentoGrid>

              {/* Interactive Network Visualization */}
              <div className="relative h-[500px] w-full bg-gradient-to-br from-primary/[0.02] to-accent/[0.02] rounded-2xl overflow-hidden">
                {/* Animated grid background */}
                <div className="absolute inset-0 opacity-10">
                  <div className="absolute inset-0" style={{
                    backgroundImage: `
                      linear-gradient(rgba(19,236,91,0.1) 1px, transparent 1px),
                      linear-gradient(90deg, rgba(19,236,91,0.1) 1px, transparent 1px)
                    `,
                    backgroundSize: '40px 40px'
                  }} />
                </div>

                {/* Center hub with pulse effect */}
                <motion.div
                  className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-20"
                  animate={{
                    scale: [1, 1.05, 1],
                  }}
                  transition={{
                    duration: 3,
                    repeat: Infinity,
                    ease: 'easeInOut',
                  }}
                >
                  <motion.div
                    className="w-24 h-24 rounded-full bg-gradient-to-br from-primary to-accent flex items-center justify-center shadow-2xl relative"
                    whileHover={{ scale: 1.1 }}
                  >
                    <motion.div
                      className="absolute inset-0 rounded-full bg-gradient-to-br from-primary to-accent"
                      animate={{
                        scale: [1, 1.5, 2],
                        opacity: [0.5, 0.2, 0],
                      }}
                      transition={{
                        duration: 2,
                        repeat: Infinity,
                        ease: 'easeOut',
                      }}
                    />
                    <Users className="h-12 w-12 text-white relative z-10" />
                  </motion.div>

                  {/* Tooltip on hover */}
                  <AnimatePresence>
                    <motion.div
                      initial={{ opacity: 0, y: 10 }}
                      whileInView={{ opacity: 1, y: 0 }}
                      viewport={{ once: true }}
                      className="absolute top-full left-1/2 -translate-x-1/2 mt-4 bg-black/80 backdrop-blur-md rounded-lg px-4 py-2 text-white text-sm whitespace-nowrap"
                    >
                      DailyWorkerHub
                      <div className="absolute -top-1 left-1/2 -translate-x-1/2 w-2 h-2 bg-black/80 rotate-45" />
                    </motion.div>
                  </AnimatePresence>
                </motion.div>

                {/* Worker nodes with enhanced interactivity */}
                {[
                  { top: '15%', left: '20%', name: 'Designer', connections: 12, delay: 0 },
                  { top: '15%', right: '20%', name: 'Developer', connections: 18, delay: 0.15 },
                  { top: '45%', left: '8%', name: 'Writer', connections: 8, delay: 0.3 },
                  { top: '45%', right: '8%', name: 'Marketer', connections: 15, delay: 0.45 },
                  { top: '75%', left: '22%', name: 'VA', connections: 20, delay: 0.6 },
                  { top: '75%', right: '22%', name: ' Analyst', connections: 10, delay: 0.75 },
                ].map((node, index) => (
                  <motion.div
                    key={index}
                    className="absolute group cursor-pointer"
                    style={{ top: node.top, [node.left ? 'left' : 'right']: node.left || node.right }}
                    initial={{ scale: 0, opacity: 0 }}
                    whileInView={{ scale: 1, opacity: 1 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.5, delay: node.delay + 0.3 }}
                  >
                    {/* Connection line to center */}
                    <motion.div
                      className="absolute top-1/2 left-1/2 h-0.5 bg-gradient-to-r from-primary to-accent origin-center"
                      style={{
                        width: '100px',
                        transform: `translateX(${node.left ? '-50%' : '50%'}) rotate(${node.left ? -45 : 45}deg)`,
                        opacity: 0.2,
                      }}
                    />

                    {/* Node */}
                    <motion.div
                      className="w-14 h-14 rounded-full bg-gradient-to-br from-primary/90 to-accent/90 flex items-center justify-center shadow-lg relative z-10"
                      whileHover={{ scale: 1.2, y: -8 }}
                      whileTap={{ scale: 0.95 }}
                    >
                      <Users className="h-7 w-7 text-white" />
                      <motion.div
                        className="absolute -bottom-1 left-1/2 -translate-x-1/2 w-8 h-0.5 bg-primary rounded-full"
                        initial={{ scaleX: 0 }}
                        whileHover={{ scaleX: 1 }}
                      />
                    </motion.div>

                    {/* Tooltip */}
                    <AnimatePresence>
                      <motion.div
                        initial={{ opacity: 0, y: 10, scale: 0.9 }}
                        animate={{ opacity: 0, y: 10, scale: 0.9 }}
                        whileHover={{ opacity: 1, y: 0, scale: 1 }}
                        className="absolute top-full left-1/2 -translate-x-1/2 mt-3 bg-black/90 backdrop-blur-md rounded-lg px-3 py-2 text-white text-xs whitespace-nowrap pointer-events-none"
                      >
                        <div className="font-semibold">{node.name}</div>
                        <div className="text-gray-300">{node.connections} connections</div>
                        <div className="absolute -top-1 left-1/2 -translate-x-1/2 w-2 h-2 bg-black/90 rotate-45" />
                      </motion.div>
                    </AnimatePresence>
                  </motion.div>
                ))}

                {/* Business nodes */}
                {[
                  { top: '22%', left: '38%', name: 'Startup A', jobs: 5, delay: 0.1 },
                  { top: '22%', right: '38%', name: 'Agency B', jobs: 12, delay: 0.25 },
                  { top: '52%', left: '28%', name: 'Company C', jobs: 8, delay: 0.4 },
                  { top: '52%', right: '28%', name: 'Brand D', jobs: 15, delay: 0.55 },
                  { top: '82%', left: '42%', name: 'Enterprise E', jobs: 20, delay: 0.7 },
                  { top: '82%', right: '42%', name: 'Firm F', jobs: 6, delay: 0.85 },
                ].map((node, index) => (
                  <motion.div
                    key={`business-${index}`}
                    className="absolute group cursor-pointer"
                    style={{ top: node.top, [node.left ? 'left' : 'right']: node.left || node.right }}
                    initial={{ scale: 0, opacity: 0 }}
                    whileInView={{ scale: 1, opacity: 1 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.5, delay: node.delay + 0.3 }}
                  >
                    {/* Connection line */}
                    <motion.div
                      className="absolute top-1/2 left-1/2 h-0.5 bg-gradient-to-r from-community to-primary origin-center"
                      style={{
                        width: '80px',
                        transform: `translateX(${node.left ? '-50%' : '50%'}) rotate(${node.left ? -30 : 30}deg)`,
                        opacity: 0.15,
                      }}
                    />

                    {/* Node */}
                    <motion.div
                      className="w-12 h-12 rounded-full bg-gradient-to-br from-community/90 to-primary/90 flex items-center justify-center shadow-lg relative z-10"
                      whileHover={{ scale: 1.2, y: -8 }}
                      whileTap={{ scale: 0.95 }}
                    >
                      <BookOpen className="h-6 w-6 text-white" />
                    </motion.div>

                    {/* Tooltip */}
                    <motion.div
                      initial={{ opacity: 0, y: 10, scale: 0.9 }}
                      whileHover={{ opacity: 1, y: 0, scale: 1 }}
                      className="absolute top-full left-1/2 -translate-x-1/2 mt-3 bg-black/90 backdrop-blur-md rounded-lg px-3 py-2 text-white text-xs whitespace-nowrap pointer-events-none"
                    >
                      <div className="font-semibold">{node.name}</div>
                      <div className="text-gray-300">{node.jobs} active jobs</div>
                      <div className="absolute -top-1 left-1/2 -translate-x-1/2 w-2 h-2 bg-black/90 rotate-45" />
                    </motion.div>
                  </motion.div>
                ))}

                {/* Animated particles */}
                {Array.from({ length: 8 }).map((_, i) => (
                  <motion.div
                    key={i}
                    className="absolute w-2 h-2 rounded-full bg-primary/40"
                    initial={{
                      x: '50%',
                      y: '50%',
                      opacity: 0,
                    }}
                    animate={{
                      x: `${30 + Math.random() * 40}%`,
                      y: `${20 + Math.random() * 60}%`,
                      opacity: [0, 1, 0],
                    }}
                    transition={{
                      duration: 3 + Math.random() * 2,
                      repeat: Infinity,
                      delay: i * 0.5,
                      ease: 'easeInOut',
                    }}
                  />
                ))}
              </div>

              {/* Enhanced Legend */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: 1.5 }}
                className="flex items-center justify-center gap-8 mt-10"
              >
                <div className="flex items-center gap-3">
                  <div className="w-5 h-5 rounded-full bg-gradient-to-br from-primary to-accent shadow-lg" />
                  <span className="text-sm font-medium">Talented Workers</span>
                </div>
                <div className="flex items-center gap-3">
                  <div className="w-5 h-5 rounded-full bg-gradient-to-br from-community to-primary shadow-lg" />
                  <span className="text-sm font-medium">Growing Businesses</span>
                </div>
                <div className="flex items-center gap-3">
                  <motion.div
                    className="h-0.5 w-12 bg-gradient-to-r from-primary via-accent to-community"
                    animate={{ scaleX: [1, 1.2, 1] }}
                    transition={{ duration: 2, repeat: Infinity }}
                  />
                  <span className="text-sm font-medium">Active Connections</span>
                </div>
              </motion.div>

              {/* Call to Action */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: 1.7 }}
                className="text-center mt-10"
              >
                <motion.button
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  className="bg-gradient-to-r from-primary to-accent text-white px-8 py-4 rounded-full font-semibold shadow-lg hover:shadow-xl transition-shadow flex items-center gap-2 mx-auto"
                >
                  Join the Network
                  <ArrowRight className="h-5 w-5" />
                </motion.button>
              </motion.div>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  );
}
