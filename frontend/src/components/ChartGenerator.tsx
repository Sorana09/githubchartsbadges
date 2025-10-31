"use client";

import { useState } from "react";
import { Copy, Check, Eye } from "lucide-react";
import { API_BASE_URL } from "@/config/api";

const CHARTS = [
  {
    name: "Commits Yearly",
    endpoint: "/commits-yearly-graph-project",
    description: "Bar chart showing commits per year",
    params: [],
  },
  {
    name: "Commits Monthly (Line)",
    endpoint: "/commits-monthly-line-graph-project",
    description: "Line chart showing commits trend",
    params: [{ name: "months", default: 12, min: 1, max: 48 }],
  },
  {
    name: "Code Churn Monthly",
    endpoint: "/code-churn-monthly",
    description: "Lines added/removed per month",
    params: [{ name: "months", default: 12, min: 1, max: 48 }],
  },
  {
    name: "File Type Churn",
    endpoint: "/filetype-churn",
    description: "Code distribution by file type",
    params: [
      { name: "limit", default: 50, min: 10, max: 200 },
      { name: "topN", default: 8, min: 3, max: 15 },
    ],
  },
];

export default function ChartGenerator() {
  const [repoUrl, setRepoUrl] = useState("");
  const [theme, setTheme] = useState<"light" | "dark">("light");
  const [copiedIndex, setCopiedIndex] = useState<number | null>(null);
  const [isValid, setIsValid] = useState(false);
  const [params, setParams] = useState<Record<string, number>>({});
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

  const generateMarkdown = (endpoint: string, chartParams: any[], url?: string) => {
    const targetUrl = url || repoUrl;
    const encodedUrl = encodeURIComponent(targetUrl);
    let fullUrl = `${API_BASE_URL}${endpoint}?repoUrl=${encodedUrl}&theme=${theme}`;
    
    chartParams.forEach(param => {
      const value = params[`${endpoint}_${param.name}`] || param.default;
      fullUrl += `&${param.name}=${value}`;
    });
    
    return `![Chart](${fullUrl})`;
  };

  const getImageUrl = (endpoint: string, chartParams: any[], url?: string) => {
    const targetUrl = url || repoUrl;
    const encodedUrl = encodeURIComponent(targetUrl);
    let fullUrl = `${API_BASE_URL}${endpoint}?repoUrl=${encodedUrl}&theme=${theme}`;
    
    chartParams.forEach(param => {
      const value = params[`${endpoint}_${param.name}`] || param.default;
      fullUrl += `&${param.name}=${value}`;
    });
    
    return fullUrl;
  };

  const copyToClipboard = async (text: string, index: number) => {
    await navigator.clipboard.writeText(text);
    setCopiedIndex(index);
    setTimeout(() => setCopiedIndex(null), 2000);
  };

  const updateParam = (endpoint: string, paramName: string, value: number) => {
    setParams({
      ...params,
      [`${endpoint}_${paramName}`]: value,
    });
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
            Available Charts
          </h3>
          
          {CHARTS.map((chart, index) => {
            const markdown = generateMarkdown(chart.endpoint, chart.params);
            const imageUrl = getImageUrl(chart.endpoint, chart.params);
            
            return (
              <div
                key={chart.name}
                className="bg-white rounded-lg border border-neutral-200 p-6 shadow-sm"
              >
                <div className="mb-4">
                  <h4 className="font-medium text-neutral-900">{chart.name}</h4>
                  <p className="text-sm text-neutral-500 mt-1">{chart.description}</p>
                </div>

                {chart.params.length > 0 && (
                  <div className="mb-4 flex flex-wrap gap-4">
                    {chart.params.map(param => (
                      <div key={param.name} className="flex items-center gap-2">
                        <label className="text-sm text-neutral-700 capitalize">
                          {param.name}:
                        </label>
                        <input
                          type="number"
                          min={param.min}
                          max={param.max}
                          value={params[`${chart.endpoint}_${param.name}`] || param.default}
                          onChange={(e) => updateParam(chart.endpoint, param.name, parseInt(e.target.value))}
                          className="w-20 px-3 py-1 border border-neutral-300 rounded text-sm text-neutral-900"
                        />
                      </div>
                    ))}
                  </div>
                )}
                
                <div className="mb-4 bg-neutral-50 rounded-lg p-4 flex justify-center">
                  <img src={imageUrl} alt={chart.name} className="max-w-full h-auto" />
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
              Paste a GitHub repo URL above to see available charts
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
              <div className="bg-neutral-50 rounded-lg p-4 flex justify-center">
                <img
                  src={getImageUrl(CHARTS[0].endpoint, CHARTS[0].params, exampleUrl)}
                  alt="Example chart"
                  className="max-w-full h-auto"
                />
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
