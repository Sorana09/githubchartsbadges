"use client";

import { useState } from "react";
import { Copy, Check, Eye } from "lucide-react";
import { API_BASE_URL } from "@/config/api";

const BADGES = [
  { name: "Stars", endpoint: "/badge/stars", color: "#f59e0b" },
  { name: "Forks", endpoint: "/badge/forks", color: "#22c55e" },
  { name: "Issues", endpoint: "/badge/issues", color: "#ef4444" },
  { name: "Watchers", endpoint: "/badge/watchers", color: "#6366f1" },
  { name: "Language", endpoint: "/badge/language", color: "#06b6d4" },
  { name: "Last Commit", endpoint: "/badge/last-commit", color: "#14b8a6" },
  { name: "Created", endpoint: "/badge/created", color: "#64748b" },
];

export default function BadgeGenerator() {
  const [repoUrl, setRepoUrl] = useState("");
  const [theme, setTheme] = useState<"light" | "dark">("light");
  const [copiedIndex, setCopiedIndex] = useState<number | null>(null);
  const [isValid, setIsValid] = useState(false);
  const [exampleUrl, setExampleUrl] = useState("https://github.com/facebook/react");

  const validateUrl = (url: string) => {
    const githubUrlPattern = /^https?:\/\/(www\.)?github\.com\/[\w-]+\/[\w.-]+\/?$/;
    const isValidUrl = githubUrlPattern.test(url);
    setIsValid(isValidUrl);
    return isValidUrl;
  };

  const handleUrlChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const url = e.target.value;
    setRepoUrl(url);
    validateUrl(url);
  };

  const generateMarkdown = (endpoint: string, url?: string) => {
    const targetUrl = url || repoUrl;
    const encodedUrl = encodeURIComponent(targetUrl);
    return `![Badge](${API_BASE_URL}${endpoint}?repoUrl=${encodedUrl}&theme=${theme})`;
  };

  const getImageUrl = (endpoint: string, url?: string) => {
    const targetUrl = url || repoUrl;
    const encodedUrl = encodeURIComponent(targetUrl);
    return `${API_BASE_URL}${endpoint}?repoUrl=${encodedUrl}&theme=${theme}`;
  };

  const copyToClipboard = async (text: string, index: number) => {
    await navigator.clipboard.writeText(text);
    setCopiedIndex(index);
    setTimeout(() => setCopiedIndex(null), 2000);
  };

  return (
    <div className="space-y-8">
      <div className="bg-white rounded-lg border border-neutral-200 p-6 shadow-sm">
        <label className="block text-sm font-medium text-neutral-700 mb-2">
          GitHub Repository URL
        </label>
        <input
          type="text"
          value={repoUrl}
          onChange={handleUrlChange}
          placeholder="https://github.com/username/repository"
          className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-neutral-900 focus:border-transparent text-neutral-900"
        />
        {repoUrl && !isValid && (
          <p className="text-red-500 text-sm mt-2">Please enter a valid GitHub repository URL</p>
        )}
        
        <div className="mt-4 flex items-center gap-4">
          <span className="text-sm font-medium text-neutral-700">Theme:</span>
          <div className="flex gap-2">
            <button
              onClick={() => setTheme("light")}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                theme === "light"
                  ? "bg-neutral-900 text-white"
                  : "bg-neutral-100 text-neutral-700 hover:bg-neutral-200"
              }`}
            >
              Light
            </button>
            <button
              onClick={() => setTheme("dark")}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                theme === "dark"
                  ? "bg-neutral-900 text-white"
                  : "bg-neutral-100 text-neutral-700 hover:bg-neutral-200"
              }`}
            >
              Dark
            </button>
          </div>
        </div>
      </div>

      {isValid && (
        <div className="space-y-4">
          <h3 className="text-lg font-semibold text-neutral-900">
            Available Badges
          </h3>
          
          {BADGES.map((badge, index) => {
            const markdown = generateMarkdown(badge.endpoint);
            const imageUrl = getImageUrl(badge.endpoint);
            
            return (
              <div
                key={badge.name}
                className="bg-white rounded-lg border border-neutral-200 p-6 shadow-sm"
              >
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <h4 className="font-medium text-neutral-900">{badge.name}</h4>
                    <p className="text-sm text-neutral-500 mt-1">{badge.endpoint}</p>
                  </div>
                  <div className="flex-shrink-0">
                    <img src={imageUrl} alt={badge.name} className="h-6" />
                  </div>
                </div>
                
                <div className="flex items-center gap-2">
                  <input
                    type="text"
                    value={markdown}
                    readOnly
                    className="flex-1 px-3 py-2 bg-neutral-50 border border-neutral-200 rounded text-sm text-neutral-700 font-mono"
                  />
                  <button
                    onClick={() => copyToClipboard(markdown, index)}
                    className="flex items-center gap-2 px-4 py-2 bg-neutral-900 text-white rounded-lg hover:bg-neutral-800 transition-colors"
                  >
                    {copiedIndex === index ? (
                      <>
                        <Check className="w-4 h-4" />
                        Copied
                      </>
                    ) : (
                      <>
                        <Copy className="w-4 h-4" />
                        Copy
                      </>
                    )}
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {!isValid && (
        <>
          <div className="bg-neutral-100 rounded-lg p-6 text-center">
            <p className="text-neutral-600 mb-4">
              Paste a GitHub repo URL above to see available badges
            </p>
            <button
              onClick={() => {
                setRepoUrl(exampleUrl);
                validateUrl(exampleUrl);
              }}
              className="inline-flex items-center gap-2 px-4 py-2 bg-neutral-900 text-white rounded-lg hover:bg-neutral-800 transition-colors"
            >
              <Eye className="w-4 h-4" />
              View Example
            </button>
          </div>

          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-neutral-900 text-center">
              Here's what you get
            </h3>
            <div className="bg-white rounded-lg border border-neutral-200 p-6">
              <p className="text-sm text-neutral-500 mb-3">Repository: {exampleUrl}</p>
              <div className="flex flex-wrap gap-3 justify-center">
                {BADGES.slice(0, 4).map((badge) => (
                  <img
                    key={badge.name}
                    src={getImageUrl(badge.endpoint, exampleUrl)}
                    alt={badge.name}
                    className="h-6"
                  />
                ))}
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
