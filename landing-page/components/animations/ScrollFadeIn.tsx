/**
 * ScrollFadeIn Component
 * Wraps children with scroll-triggered fade-in animation
 */

'use client';

import { motion, useInView } from 'framer-motion';
import { useRef } from 'react';
import { scrollReveal } from './variants';

interface ScrollFadeInProps {
  children: React.ReactNode;
  delay?: number;
  className?: string;
  threshold?: number;
}

export function ScrollFadeIn({
  children,
  delay = 0,
  className = '',
  threshold = 0.1,
}: ScrollFadeInProps) {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true, amount: threshold });

  return (
    <motion.div
      ref={ref}
      initial="hidden"
      animate={isInView ? 'visible' : 'hidden'}
      variants={scrollReveal}
      transition={{ delay }}
      className={className}
    >
      {children}
    </motion.div>
  );
}
