"use client";

import { motion } from 'framer-motion';
import { useLanguage } from '@/lib/language-context';
import { Globe } from 'lucide-react';

export function LanguageSwitcher() {
  const { language, setLanguage } = useLanguage();

  return (
    <div className="flex items-center gap-2">
      <Globe className="w-4 h-4 text-gray-600" />
      <div className="flex items-center gap-1 bg-gray-100 rounded-full p-1">
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={() => setLanguage('id')}
          className={`px-3 py-1 rounded-full text-xs font-medium transition-all duration-300 ${
            language === 'id'
              ? 'bg-green-600 text-white shadow-md'
              : 'text-gray-600 hover:bg-gray-200'
          }`}
        >
          ID
        </motion.button>
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={() => setLanguage('en')}
          className={`px-3 py-1 rounded-full text-xs font-medium transition-all duration-300 ${
            language === 'en'
              ? 'bg-green-600 text-white shadow-md'
              : 'text-gray-600 hover:bg-gray-200'
          }`}
        >
          EN
        </motion.button>
      </div>
    </div>
  );
}
