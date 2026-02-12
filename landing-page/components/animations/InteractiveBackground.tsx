/**
 * InteractiveBackground Component
 * Animated gradient background with subtle movement
 * Creates a modern, living design
 * Accessibility: Respects prefers-reduced-motion
 */

'use client';

import { motion, useReducedMotion } from 'framer-motion';
import { useEffect, useState } from 'react';

export function InteractiveBackground() {
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });
  const prefersReducedMotion = useReducedMotion();

  useEffect(() => {
    // Skip mouse tracking if user prefers reduced motion
    if (prefersReducedMotion) return;

    const handleMouseMove = (e: MouseEvent) => {
      setMousePosition({
        x: (e.clientX / window.innerWidth) * 100,
        y: (e.clientY / window.innerHeight) * 100,
      });
    };

    window.addEventListener('mousemove', handleMouseMove);
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, [prefersReducedMotion]);

  // Don't render animation if reduced motion is preferred
  if (prefersReducedMotion) {
    return (
      <div className="fixed inset-0 pointer-events-none -z-10 overflow-hidden">
        {/* Static gradient background for reduced motion */}
        <div className="absolute inset-0 bg-gradient-to-br from-primary/5 via-accent/5 to-community/5" />
      </div>
    );
  }

  return (
    <div className="fixed inset-0 pointer-events-none -z-10 overflow-hidden">
      {/* Gradient orbs */}
      <motion.div
        className="absolute w-[800px] h-[800px] rounded-full bg-primary/10 blur-3xl"
        animate={{
          x: [0, 200, 0],
          y: [0, 100, 0],
        }}
        transition={{
          duration: 20,
          repeat: Infinity,
          ease: 'easeInOut',
        }}
        style={{
          left: `${mousePosition.x * 0.5}%`,
          top: `${mousePosition.y * 0.5}%`,
        }}
      />

      <motion.div
        className="absolute w-[600px] h-[600px] rounded-full bg-accent/10 blur-3xl"
        animate={{
          x: [0, -150, 0],
          y: [0, -80, 0],
        }}
        transition={{
          duration: 15,
          repeat: Infinity,
          ease: 'easeInOut',
          delay: 1,
        }}
        style={{
          right: `${(100 - mousePosition.x) * 0.4}%`,
          top: `${mousePosition.y * 0.4}%`,
        }}
      />

      <motion.div
        className="absolute w-[500px] h-[500px] rounded-full bg-community/10 blur-3xl"
        animate={{
          x: [0, 100, 0],
          y: [0, -120, 0],
        }}
        transition={{
          duration: 18,
          repeat: Infinity,
          ease: 'easeInOut',
          delay: 2,
        }}
        style={{
          left: '40%',
          bottom: `${mousePosition.y * 0.3}%`,
        }}
      />

      {/* Grid pattern */}
      <div className="absolute inset-0 bg-[linear-gradient(rgba(19,236,91,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(19,236,91,0.03)_1px,transparent_1px)] bg-[size:60px_60px] [mask-image:radial-gradient(ellipse_at_center,black_40%,transparent_80%)]" />
    </div>
  );
}
