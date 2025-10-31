"use client";

import { useState } from "react";
import { Github } from "lucide-react";
import BadgeGenerator from "@/components/BadgeGenerator";
import ChartGenerator from "@/components/ChartGenerator";

export default function Home() {
  const [activeTab, setActiveTab] = useState<"badges" | "charts">("badges");

  return (
    <main className="min-h-screen bg-neutral-50">
      <header className="border-b border-neutral-200 bg-white">
        <div className="max-w-5xl mx-auto px-4 py-6">
          <div className="flex items-center gap-3">
            <Github className="w-8 h-8 text-neutral-900" />
            <h1 className="text-2xl font-semibold text-neutral-900">
              GitHub Badges & Charts
            </h1>
          </div>
        </div>
      </header>

      <section className="bg-white border-b border-neutral-200">
        <div className="max-w-5xl mx-auto px-4 py-12 text-center">
          <h2 className="text-3xl font-bold text-neutral-900 mb-3">
            README badges & charts
          </h2>
          <p className="text-neutral-600 max-w-xl mx-auto">
            Drop your repo URL below, grab some badges or charts, copy the code.
          </p>
        </div>
      </section>

      <div className="bg-white border-b border-neutral-200">
        <div className="max-w-5xl mx-auto px-4">
          <div className="flex gap-1">
            <button
              onClick={() => setActiveTab("badges")}
              className={`px-6 py-3 font-medium transition-colors ${
                activeTab === "badges"
                  ? "text-neutral-900 border-b-2 border-neutral-900"
                  : "text-neutral-500 hover:text-neutral-700"
              }`}
            >
              Badges
            </button>
            <button
              onClick={() => setActiveTab("charts")}
              className={`px-6 py-3 font-medium transition-colors ${
                activeTab === "charts"
                  ? "text-neutral-900 border-b-2 border-neutral-900"
                  : "text-neutral-500 hover:text-neutral-700"
              }`}
            >
              Charts
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-5xl mx-auto px-4 py-12">
        {activeTab === "badges" ? <BadgeGenerator /> : <ChartGenerator />}
      </div>


      <footer className="border-t border-neutral-200 bg-white mt-20">
        <div className="max-w-5xl mx-auto px-4 py-8 text-center text-neutral-500 text-sm">
          make your repos look good
        </div>
      </footer>
    </main>
  );
}
