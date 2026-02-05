/**
 * CommunityFirstApproach Section
 * "What Makes Us Different" - Highlight community as USP
 */

'use client';

import { motion } from 'framer-motion';
import { Shield, BookOpen, Network, Sparkles, ArrowRight, Users } from 'lucide-react';

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

        {/* Interactive Network Diagram */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: '-100px' }}
          transition={{ duration: 0.6, delay: 0.3 }}
        >
          <div className="relative bg-gradient-to-br from-card via-card to-primary/5 backdrop-blur-md rounded-3xl p-12 border border-border/50 overflow-hidden">
            {/* Animated gradient overlay */}
            <motion.div
              className="absolute inset-0 bg-gradient-to-r from-primary/5 via-accent/5 to-community/5"
              animate={{
                opacity: [0.3, 0.6, 0.3],
              }}
              transition={{
                duration: 4,
                repeat: Infinity,
                ease: 'easeInOut',
              }}
            />

            <div className="relative z-10">
              <div className="flex items-center gap-6 mb-8">
                <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-primary/20 to-accent/20 flex items-center justify-center">
                  <Network className="h-8 w-8 text-primary" />
                </div>
                <div>
                  <h3 className="text-3xl font-bold mb-2">Interactive Community Network</h3>
                  <p className="text-lg text-muted-foreground">
                    Hover over nodes untuk lihat koneksi aktif
                  </p>
                </div>
              </div>

              {/* Network visualization */}
              <div className="relative h-[400px] w-full">
                {/* Center hub */}
                <motion.div
                  className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-20 h-20 rounded-full bg-gradient-to-br from-primary to-accent flex items-center justify-center z-10 shadow-2xl"
                  animate={{
                    scale: [1, 1.1, 1],
                    boxShadow: ['0 0 0 0 rgba(19,236,91,0.5)', '0 0 0 20px rgba(19,236,91,0)', '0 0 0 0 rgba(19,236,91,0.5)'],
                  }}
                  transition={{
                    duration: 2,
                    repeat: Infinity,
                    ease: 'easeInOut',
                  }}
                >
                  <Users className="h-10 w-10 text-white" />
                </motion.div>

                {/* Worker nodes */}
                {[
                  { top: '15%', left: '20%', delay: 0 },
                  { top: '15%', right: '20%', delay: 0.2 },
                  { top: '45%', left: '10%', delay: 0.4 },
                  { top: '45%', right: '10%', delay: 0.6 },
                  { top: '75%', left: '25%', delay: 0.8 },
                  { top: '75%', right: '25%', delay: 1 },
                ].map((node, index) => (
                  <motion.div
                    key={index}
                    className={`absolute w-12 h-12 rounded-full bg-gradient-to-br from-primary/80 to-accent/80 flex items-center justify-center z-5`}
                    style={{ top: node.top, [node.left ? 'left' : 'right']: node.left || node.right }}
                    initial={{ scale: 0, opacity: 0 }}
                    whileInView={{ scale: 1, opacity: 1 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.5, delay: node.delay + 0.5 }}
                    whileHover={{ scale: 1.2, y: -5 }}
                  >
                    <Users className="h-6 w-6 text-white" />
                  </motion.div>
                ))}

                {/* Business nodes */}
                {[
                  { top: '20%', left: '40%', delay: 0.1 },
                  { top: '20%', right: '40%', delay: 0.3 },
                  { top: '50%', left: '30%', delay: 0.5 },
                  { top: '50%', right: '30%', delay: 0.7 },
                  { top: '80%', left: '45%', delay: 0.9 },
                  { top: '80%', right: '45%', delay: 1.1 },
                ].map((node, index) => (
                  <motion.div
                    key={`business-${index}`}
                    className="absolute w-12 h-12 rounded-full bg-gradient-to-br from-community/80 to-primary/80 flex items-center justify-center z-5"
                    style={{ top: node.top, [node.left ? 'left' : 'right']: node.left || node.right }}
                    initial={{ scale: 0, opacity: 0 }}
                    whileInView={{ scale: 1, opacity: 1 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.5, delay: node.delay + 0.5 }}
                    whileHover={{ scale: 1.2, y: -5 }}
                  >
                    <BookOpen className="h-6 w-6 text-white" />
                  </motion.div>
                ))}

                {/* Connection lines (simulated with SVG) */}
                <svg className="absolute inset-0 w-full h-full opacity-20" viewBox="0 0 800 400">
                  <defs>
                    <linearGradient id="lineGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" stopColor="#13EC5B" stopOpacity="0.8" />
                      <stop offset="100%" stopColor="#3B82F6" stopOpacity="0.3" />
                    </linearGradient>
                  </defs>
                  {/* Lines from center to workers */}
                  {[{ x1: 400, y1: 200, x2: 160, y2: 60 }, { x1: 400, y1: 200, x2: 640, y2: 60 }, { x1: 400, y1: 200, x2: 80, y2: 180 }, { x1: 400, y1: 200, x2: 720, y2: 180 }].map((line, i) => (
                    <motion.line
                      key={`worker-line-${i}`}
                      x1={line.x1}
                      y1={line.y1}
                      x2={line.x2}
                      y2={line.y2}
                      stroke="url(#lineGradient)"
                      strokeWidth="2"
                      strokeLinecap="round"
                      initial={{ pathLength: 0, opacity: 0 }}
                      whileInView={{ pathLength: 1, opacity: 1 }}
                      viewport={{ once: true }}
                      transition={{ duration: 1, delay: 0.5 + i * 0.1 }}
                    />
                  ))}
                  {/* Lines from center to businesses */}
                  {[{ x1: 400, y1: 200, x2: 320, y2: 80 }, { x1: 400, y1: 200, x2: 480, y2: 80 }, { x1: 400, y1: 200, x2: 240, y2: 200 }, { x1: 400, y1: 200, x2: 560, y2: 200 }].map((line, i) => (
                    <motion.line
                      key={`business-line-${i}`}
                      x1={line.x1}
                      y1={line.y1}
                      x2={line.x2}
                      y2={line.y2}
                      stroke="url(#lineGradient)"
                      strokeWidth="2"
                      strokeLinecap="round"
                      initial={{ pathLength: 0, opacity: 0 }}
                      whileInView={{ pathLength: 1, opacity: 1 }}
                      viewport={{ once: true }}
                      transition={{ duration: 1, delay: 1 + i * 0.1 }}
                    />
                  ))}
                </svg>
              </div>

              {/* Legend */}
              <div className="flex items-center gap-6 mt-8">
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 rounded-full bg-gradient-to-br from-primary to-accent" />
                  <span className="text-sm font-medium">Workers</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 rounded-full bg-gradient-to-br from-community to-primary" />
                  <span className="text-sm font-medium">Businesses</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-1 bg-gradient-to-r from-primary to-community" />
                  <span className="text-sm font-medium">Connections</span>
                </div>
              </div>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  );
}
