/**
 * Navbar Component
 * Responsive navigation with mobile menu and language switcher
 * Accessibility: ARIA labels, skip link, keyboard navigation support
 */

'use client';

import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { LanguageSwitcher } from '@/components/ui/language-switcher';
import { useLanguage } from '@/lib/language-context';
import { Menu, X, Download } from 'lucide-react';
import { APP_STORES } from '@/lib/constants';

export function Navbar() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const { t } = useLanguage();

  // Close mobile menu on escape key
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isMobileMenuOpen) {
        setIsMobileMenuOpen(false);
      }
    };
    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isMobileMenuOpen]);

  // Trap focus in mobile menu when open
  useEffect(() => {
    if (isMobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isMobileMenuOpen]);

  const navLinks = [
    { href: '#home', label: t('nav.home') },
    { href: '#features', label: t('nav.features') },
    { href: '#how-it-works', label: t('nav.howItWorks') },
    { href: '#community', label: t('nav.community') },
    { href: '#faq', label: t('nav.faq') },
  ];

  return (
    <>
      {/* Skip Navigation Link - Accessibility */}
      <a
        href="#main-content"
        className="sr-only focus:not(.sr-only) focus:absolute focus:top-4 focus:left-4 focus:z-[100] focus:px-4 focus:py-2 focus:bg-primary focus:text-primary-foreground focus:rounded-lg focus:font-medium focus:shadow-lg"
      >
        Skip to main content
      </a>

      <nav
        className="fixed top-0 left-0 right-0 z-50 bg-background/80 backdrop-blur-md border-b border-border/50"
        aria-label="Main navigation"
      >
        <div className="container mx-auto px-3 sm:px-4 md:px-6 lg:px-8 py-3 md:py-4">
          <div className="flex items-center justify-between">
          {/* Logo */}
          <motion.a
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            href="#home"
            className="flex items-center gap-2 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 rounded-lg"
            aria-label="Daily Worker Hub - Go to home"
          >
            <div className="w-9 h-9 md:w-10 md:h-10 rounded-xl bg-gradient-to-br from-primary to-primary/80 flex items-center justify-center shrink-0">
              <span className="text-white text-lg md:text-xl font-bold">DW</span>
            </div>
            <div className="hidden sm:block">
              <h1 className="text-base md:text-lg font-bold">Daily Worker Hub</h1>
              <p className="text-[10px] md:text-xs text-muted-foreground">Community-Based Platform</p>
            </div>
          </motion.a>

          {/* Desktop Navigation - Improved for medium screens */}
          <motion.nav
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="hidden lg:flex items-center gap-6 xl:gap-8"
            aria-label="Page navigation"
          >
            {navLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 rounded-md px-2 py-1"
              >
                {link.label}
              </a>
            ))}
          </motion.nav>

          {/* Medium Navigation (Tablet) - Compact horizontal */}
          <motion.nav
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="hidden md:flex lg:hidden items-center gap-4"
            aria-label="Tablet navigation"
          >
            {navLinks.slice(0, 3).map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 rounded-md px-2 py-1 whitespace-nowrap"
              >
                {link.label}
              </a>
            ))}
          </motion.nav>

          {/* CTA Buttons & Language Switcher - Improved for medium */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.2 }}
            className="hidden md:flex items-center gap-2 lg:gap-3"
          >
            <LanguageSwitcher />
            <Button
              asChild
              size="sm"
              className="bg-primary hover:bg-primary/90 text-white text-xs md:text-sm px-3 md:px-4"
            >
              <a href={APP_STORES.googlePlay} target="_blank" rel="noopener noreferrer">
                <Download className="mr-1.5 md:mr-2 h-3.5 w-3.5 md:h-4 md:w-4" />
                <span className="hidden sm:inline">Download App</span>
                <span className="sm:hidden">Get App</span>
              </a>
            </Button>
          </motion.div>

          {/* Mobile/Tablet Menu Button - Show on smaller screens */}
          <motion.button
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="lg:hidden p-2 md:p-3 hover:bg-secondary rounded-lg transition-colors min-w-[40px] min-h-[40px] md:min-w-[44px] md:min-h-[44px] focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2"
            aria-expanded={isMobileMenuOpen}
            aria-controls="mobile-menu"
            aria-label={isMobileMenuOpen ? 'Close menu' : 'Open menu'}
          >
            {isMobileMenuOpen ? <X className="h-5 w-5 md:h-6 md:w-6" /> : <Menu className="h-5 w-5 md:h-6 md:w-6" />}
          </motion.button>
        </div>
      </div>

      {/* Mobile/Tablet Menu - Improved for medium screens */}
      <AnimatePresence>
        {isMobileMenuOpen && (
          <motion.div
            id="mobile-menu"
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.3 }}
            className="lg:hidden border-t border-border/50 bg-background"
            role="navigation"
            aria-label="Mobile navigation"
          >
            {/* Tablet: Horizontal scrollable links */}
            <div className="hidden md:flex container mx-auto px-6 md:px-8 lg:px-12 py-4 overflow-x-auto">
              <div className="flex items-center gap-2 md:gap-3">
                {navLinks.map((link, index) => (
                  <motion.a
                    key={link.href}
                    href={link.href}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: 0.1 + index * 0.05 }}
                    onClick={() => setIsMobileMenuOpen(false)}
                    className="text-sm font-medium py-2 px-4 hover:text-primary hover:bg-primary/10 transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-inset rounded-full whitespace-nowrap flex items-center"
                  >
                    {link.label}
                  </motion.a>
                ))}
              </div>
            </div>

            {/* Mobile: Full vertical menu */}
            <div className="md:hidden container mx-auto px-4 sm:px-6 py-5 sm:py-6 space-y-3 sm:space-y-4">
              <div className="mb-4">
                <LanguageSwitcher />
              </div>

              {navLinks.map((link, index) => (
                <motion.a
                  key={link.href}
                  href={link.href}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.1 + index * 0.05 }}
                  onClick={() => setIsMobileMenuOpen(false)}
                  className="block text-base sm:text-lg font-medium py-3 px-2 hover:text-primary transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-inset rounded-md min-h-[44px] flex items-center"
                >
                  {link.label}
                </motion.a>
              ))}

              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.2 }}
                className="pt-4 border-t border-border/50"
              >
                <Button
                  asChild
                  className="w-full bg-primary hover:bg-primary/90 text-white"
                >
                  <a href={APP_STORES.googlePlay} target="_blank" rel="noopener noreferrer">
                    <Download className="mr-2 h-4 w-4" />
                    Download App
                  </a>
                </Button>
              </motion.div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
    </>
  );
}
