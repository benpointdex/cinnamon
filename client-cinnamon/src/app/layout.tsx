import type { Metadata } from "next";
import { Inter, Newsreader, JetBrains_Mono } from "next/font/google";
import "./globals.css";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

const newsreader = Newsreader({
  subsets: ["latin"],
  variable: "--font-newsreader",
  display: "swap",
  style: ["normal", "italic"],
});

const jetbrainsMono = JetBrains_Mono({
  subsets: ["latin"],
  variable: "--font-mono",
  display: "swap",
});

export const metadata: Metadata = {
  title: "Cinnamon — AI-Native Semantic Duplicate Code Detection (MCP)",
  description:
    "Detect exact, renamed, and semantic duplicate functions in real-time directly inside Cursor, Claude, and your terminal using AST parsers and pgvector.",
  keywords: [
    "MCP",
    "Model Context Protocol",
    "code duplicate detection",
    "AST parser",
    "pgvector",
    "Cursor AI",
    "Claude Desktop",
  ],
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html
      lang="en"
      className={`${inter.variable} ${newsreader.variable} ${jetbrainsMono.variable}`}
    >
      <body className="bg-[#F1ECE0] text-[#171512] selection:bg-[#E0447D] selection:text-white">
        {children}
      </body>
    </html>
  );
}