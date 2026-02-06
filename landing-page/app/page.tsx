import { Navbar } from '@/components/sections/Navbar';
import { Hero } from '@/components/sections/Hero';
import { Stats } from '@/components/sections/Stats';
import { ProblemStatement } from '@/components/sections/ProblemStatement';
import { CommunityFirstApproach } from '@/components/sections/CommunityFirstApproach';
import { HowItWorks } from '@/components/sections/HowItWorks';
import { FeaturesSection } from '@/components/sections/FeaturesSection';
import { CommunityShowcase } from '@/components/sections/CommunityShowcase';
import { ForWorkers } from '@/components/sections/ForWorkers';
import { ForBusinesses } from '@/components/sections/ForBusinesses';
import { AppScreenshots } from '@/components/sections/AppScreenshots';
import { FAQ } from '@/components/sections/FAQ';
import { FinalCTA } from '@/components/sections/FinalCTA';
import { Footer } from '@/components/sections/Footer';
import { InteractiveBackground, RealTimeActivityFeed } from '@/components/animations';

export default function Home() {
  return (
    <main id="main-content" className="min-h-screen relative" tabIndex={-1}>
      <InteractiveBackground />
      <Navbar />
      <RealTimeActivityFeed />
      <Hero />
      <Stats />
      <ProblemStatement />
      <CommunityFirstApproach />
      <HowItWorks />
      <FeaturesSection />
      <ForWorkers />
      <ForBusinesses />
      <AppScreenshots />
      <CommunityShowcase />
      <FAQ />
      <FinalCTA />
      <Footer />
    </main>
  );
}
