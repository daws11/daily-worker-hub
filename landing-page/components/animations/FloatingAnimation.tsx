/**
 * FloatingAnimation Component
 * Creates a gentle floating effect for hero mockup/elements
 */

'use client';

import { motion } from 'framer-motion';
import { float } from './variants';

interface FloatingAnimationProps {
  children: React.ReactNode;
  delay?: number;
  className?: string;
}

export function FloatingAnimation({
  children,
  delay = 0,
  className = '',
}: FloatingAnimationProps) {
  return (
    <motion.div
      initial="initial"
      animate="animate"
      variants={float}
      transition={{ delay }}
      className={className}
    >
      {children}
    </motion.div>
  );
}
