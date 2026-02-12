/**
 * CounterAnimation Component
 * Animated counter for statistics
 */

'use client';

import { motion, useInView } from 'framer-motion';
import { useEffect, useRef, useState } from 'react';
import { counter } from './variants';

interface CounterAnimationProps {
  from: number;
  to: number;
  suffix?: string;
  prefix?: string;
  duration?: number;
  className?: string;
}

export function CounterAnimation({
  from,
  to,
  suffix = '',
  prefix = '',
  duration = 2,
  className = '',
}: CounterAnimationProps) {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true });
  const [count, setCount] = useState(from);

  useEffect(() => {
    if (!isInView) return;

    let startTime: number | null = null;
    const animate = (currentTime: number) => {
      if (!startTime) startTime = currentTime;
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / (duration * 1000), 1);

      // Easing function (easeOut)
      const easeOut = 1 - Math.pow(1 - progress, 3);
      const currentCount = Math.floor(from + (to - from) * easeOut);

      setCount(currentCount);

      if (progress < 1) {
        requestAnimationFrame(animate);
      }
    };

    requestAnimationFrame(animate);
  }, [from, to, duration, isInView]);

  return (
    <motion.div
      ref={ref}
      initial="initial"
      animate={isInView ? 'animate' : 'initial'}
      variants={counter}
      className={className}
    >
      {prefix}
      {count.toLocaleString()}
      {suffix}
    </motion.div>
  );
}
