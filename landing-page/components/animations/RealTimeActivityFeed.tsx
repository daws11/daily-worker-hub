/**
 * RealTimeActivityFeed Component
 * Live activity ticker showing real-time engagement
 * Inspired by WeTracked.io
 */

'use client';

import { motion } from 'framer-motion';
import { useEffect, useState } from 'react';
import { Clock, UserPlus, Briefcase, MessageCircle, Star } from 'lucide-react';

interface Activity {
  id: string;
  type: 'worker' | 'job' | 'community' | 'rating';
  user: string;
  action: string;
  time: string;
  icon: React.ElementType;
}

const MOCK_ACTIVITIES: Activity[] = [
  { id: '1', type: 'worker', user: 'Ketut Dewi', action: 'started a new job', time: '2 sec ago', icon: Briefcase },
  { id: '2', type: 'job', user: 'Hotel Ubud', action: 'posted a new job', time: '15 sec ago', icon: Briefcase },
  { id: '3', type: 'community', user: 'Made Surya', action: 'joined the community', time: '34 sec ago', icon: UserPlus },
  { id: '4', type: 'rating', user: 'Warung Bali', action: 'gave 5★ rating', time: '45 sec ago', icon: Star },
  { id: '5', type: 'job', user: 'Cafe Seminyak', action: 'hired a worker', time: '1 min ago', icon: Briefcase },
  { id: '6', type: 'community', user: 'Komang Eka', action: 'shared a resource', time: '1 min ago', icon: MessageCircle },
  { id: '7', type: 'worker', user: 'Putu Ayu', action: 'completed 50 jobs', time: '2 min ago', icon: Briefcase },
];

export function RealTimeActivityFeed() {
  const [activities, setActivities] = useState<Activity[]>(MOCK_ACTIVITIES);

  useEffect(() => {
    // Simulate real-time updates
    const interval = setInterval(() => {
      const newActivity: Activity = {
        id: Date.now().toString(),
        type: ['worker', 'job', 'community', 'rating'][Math.floor(Math.random() * 4)] as Activity['type'],
        user: ['Ketut', 'Made', 'Wayan', 'Putu', 'Komang', 'Nyoman', 'Desak'][Math.floor(Math.random() * 7)] + ' ' +
              ['Dewi', 'Surya', 'Bagus', 'Ayu', 'Eka', 'Made', 'Putu'][Math.floor(Math.random() * 7)],
        action: [
          'applied for a job',
          'completed a job',
          'joined the community',
          'posted a new job',
          'shared a tip',
        ][Math.floor(Math.random() * 5)],
        time: 'Just now',
        icon: [Briefcase, UserPlus, MessageCircle, Star][Math.floor(Math.random() * 4)],
      };

      setActivities((prev) => [newActivity, ...prev].slice(0, 7));
    }, 4000);

    return () => clearInterval(interval);
  }, []);

  return (
    <div className="w-full bg-card/50 backdrop-blur-sm border-y border-border/50">
      <div className="container mx-auto px-4 py-3">
        <div className="flex items-center gap-2 overflow-hidden">
          <div className="flex items-center gap-2 shrink-0">
            <div className="w-2 h-2 rounded-full bg-primary animate-pulse" />
            <span className="text-sm font-medium text-muted-foreground">Live Activity</span>
          </div>

          <div className="relative h-8 flex items-center overflow-hidden">
            <motion.div
              className="flex flex-col"
              animate={{ y: [0, -activities.length * 32] }}
              transition={{ duration: activities.length * 4, ease: 'linear', repeat: Infinity }}
            >
              {[...activities, ...activities].map((activity, index) => {
                const Icon = activity.icon;
                return (
                  <motion.div
                    key={`${activity.id}-${index}`}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3 }}
                    className="flex items-center gap-3 h-8 whitespace-nowrap"
                  >
                    <Icon className="h-4 w-4 text-primary" />
                    <span className="text-sm text-foreground">
                      <span className="font-medium">{activity.user}</span>
                      <span className="text-muted-foreground"> {activity.action}</span>
                    </span>
                    <span className="text-xs text-muted-foreground shrink-0">{activity.time}</span>
                  </motion.div>
                );
              })}
            </motion.div>
          </div>
        </div>
      </div>
    </div>
  );
}
