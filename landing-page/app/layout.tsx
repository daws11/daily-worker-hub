import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { LanguageProvider } from "@/lib/language-context";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Daily Worker Hub - Platform Community-Based untuk Daily Workers di Bali",
  description: "Bergabunglah dengan komunitas terpercaya 500+ workers & 120+ businesses. Temukan talenta profesional, bangun network, dan tumbuh bersama di Bali.",
  keywords: "daily worker bali, hospitality community bali, platform komunitas pekerja harian, pkhl bali, daily worker hub",
  authors: [{ name: "Daily Worker Hub" }],
  creator: "Daily Worker Hub",
  openGraph: {
    type: "website",
    locale: "id_ID",
    url: "https://dailyworkerhub.id",
    title: "Daily Worker Hub - Platform Community-Based untuk Daily Workers di Bali",
    description: "Bergabunglah dengan komunitas terpercaya 500+ workers & 120+ businesses. Temukan talenta profesional, bangun network, dan tumbuh bersama di Bali.",
    siteName: "Daily Worker Hub",
  },
  twitter: {
    card: "summary_large_image",
    title: "Daily Worker Hub - Platform Community-Based untuk Daily Workers di Bali",
    description: "Bergabunglah dengan komunitas terpercaya 500+ workers & 120+ businesses. Temukan talenta profesional, bangun network, dan tumbuh bersama di Bali.",
  },
  icons: {
    icon: "/favicon.ico",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="id" className="scroll-smooth">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <LanguageProvider>
          {children}
        </LanguageProvider>
      </body>
    </html>
  );
}
