'use client';

import { useState } from 'react';
import { api, TestCase } from '@/lib/api';

export default function HomePage() {
  const [userStory, setUserStory] = useState('');
  const [screenshot, setScreenshot] = useState<File | null>(null);
  const [testCases, setTestCases] = useState<TestCase[]>([]);
  const [loading, setLoading] = useState(false);

  const handleGenerate = async () => {
    if (!userStory.trim()) return;
    setLoading(true);
    try {
      let results;
      if (screenshot) {
        results = await api.generateMultimodal(userStory, screenshot);
      } else {
        results = await api.generateTestCases(userStory);
      }
      setTestCases(results);
    } catch (err) {
      alert('Generation failed. Make sure the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    if (testCases.length === 0) return;
    try {
      const blob = await api.exportExcel(testCases);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `test_cases_${new Date().getTime()}.xlsx`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert('Export failed');
    }
  };

  return (
    <div className="animate-fade-in flex flex-col gap-16">
      <section className="text-left max-w-4xl flex flex-col gap-6">
        <br />
        <h1 className="text-3xl font-black tracking-tight leading-tight">
          Generate <span className="bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">Reliable</span> Test Cases in seconds
        </h1>
      </section>

      <section className="glass p-10 lg:p-12 w-full shadow-inner">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">
          <div className="flex flex-col gap-6">
            <div className="flex flex-col gap-3">
              <label className="text-base font-bold text-slate-200 flex items-center gap-2">
                <span className="w-6 h-6 rounded bg-primary/20 flex items-center justify-center text-primary text-xs">1</span>
                User Story / Feature Description
              </label>
              <textarea
                className="w-full h-56 bg-white/5 border border-glass-border rounded-2xl p-6 focus:ring-4 focus:ring-primary/20 focus:border-primary focus:outline-none transition-all placeholder:text-slate-600 text-lg leading-relaxed"
                placeholder="As a user, I want to be able to reset my password..."
                value={userStory}
                onChange={(e) => setUserStory(e.target.value)}
              />
            </div>
          </div>

          <div className="flex flex-col gap-6">
            <div className="flex flex-col gap-3">
              <label className="text-base font-bold text-slate-200 flex items-center gap-2">
                <span className="w-6 h-6 rounded bg-accent/20 flex items-center justify-center text-accent text-xs">2</span>
                UI Screenshot (Optional for Multimodal)
              </label>
              <div className={`h-56 border-2 border-dashed border-glass-border rounded-2xl p-8 transition-all relative flex flex-col items-center justify-center gap-4 ${screenshot ? 'bg-primary/10 border-primary/50' : 'hover:border-primary/40 hover:bg-white/5 cursor-pointer'}`}>
                <input
                  type="file"
                  accept="image/*"
                  className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                  onChange={(e) => setScreenshot(e.target.files?.[0] || null)}
                />
                <div className={`p-4 rounded-full ${screenshot ? 'bg-primary/20 text-primary' : 'bg-slate-800 text-slate-400'}`}>
                  <svg className="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <div className="text-center">
                  {screenshot ? (
                    <span className="text-primary font-bold text-lg">{screenshot.name}</span>
                  ) : (
                    <span className="text-slate-400 font-medium">Drag and drop UI image or click to browse</span>
                  )}
                  <p className="text-xs text-slate-500 mt-2">Supports PNG, JPG up to 10MB</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="flex justify-center mt-12">
          <button
            onClick={handleGenerate}
            disabled={loading || !userStory.trim()}
            className={`btn-premium px-16 py-4 text-xl shadow-2xl ${loading ? 'opacity-50' : ''}`}
          >
            {loading ? (
              <>
                <svg className="animate-spin -ml-1 mr-3 h-6 w-6 text-white" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Processing...
              </>
            ) : (
              screenshot ? 'Generate Multimodal' : 'Generate Test Cases'
            )}
          </button>
        </div>
      </section>

      {testCases.length > 0 && (
        <section className="animate-fade-in flex flex-col gap-10">
          <div className="flex justify-between items-end border-b border-glass-border pb-8">
            <div>
              <h2 className="text-4xl font-black mb-3">Generated Results</h2>
              <p className="text-lg text-slate-400 font-medium">Found {testCases.length} distinct test scenarios.</p>
            </div>
            <button onClick={handleExport} className="btn-premium !bg-emerald-600 hover:!bg-emerald-500 shadow-emerald-900/40">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Export to Excel
            </button>
          </div>

          <div className="overflow-x-auto pb-4">
            <table className="premium-table">
              <thead>
                <tr>
                  <th className="w-32">Testcase ID</th>
                  <th className="w-1/3">Target Scenario</th>
                  <th>Execution Details</th>
                </tr>
              </thead>
              <tbody>
                {testCases.map((tc) => (
                  <tr key={tc.id}>
                    <td className="align-top">
                      <span className="inline-block px-3 py-1 rounded bg-primary/10 text-primary font-mono text-xs border border-primary/20 uppercase tracking-widest">
                        {tc.id}
                      </span>
                    </td>
                    <td className="align-top">
                      <div className="font-black text-xl mb-2 text-white leading-snug">{tc.title}</div>
                      <div className="text-slate-400 text-sm leading-relaxed italic mb-4">"{tc.scenario}"</div>
                      {tc.preconditions?.length > 0 && (
                        <div className="mt-4 p-4 bg-white/5 rounded-xl border border-glass-border">
                          <div className="text-[10px] uppercase text-primary font-black mb-2 tracking-widest">Pre-requisites</div>
                          <ul className="text-xs space-y-2">
                            {tc.preconditions.map((p, i) => (
                              <li key={i} className="flex gap-2">
                                <span className="text-primary font-bold">↳</span> {p}
                              </li>
                            ))}
                          </ul>
                        </div>
                      )}
                    </td>
                    <td className="align-top">
                      <div className="flex flex-col gap-6">
                        <div>
                          <div className="text-[10px] uppercase text-slate-500 font-black mb-3 tracking-widest">Execution Steps</div>
                          <div className="flex flex-col gap-3">
                            {tc.steps.map((s, i) => (
                              <div key={i} className="flex gap-4 items-start bg-white/5 p-3 rounded-lg border border-transparent hover:border-glass-border transition-all">
                                <span className="w-6 h-6 flex-shrink-0 rounded-full bg-slate-800 flex items-center justify-center text-xs font-bold text-slate-400">{i + 1}</span>
                                <span className="text-base text-slate-200">{s}</span>
                              </div>
                            ))}
                          </div>
                        </div>
                        <div className="bg-emerald-500/10 border border-emerald-500/30 p-5 rounded-2xl">
                          <div className="text-[10px] uppercase text-emerald-400 font-black mb-2 tracking-widest">Verifiable Outcome</div>
                          <div className="text-base text-emerald-100 font-medium leading-relaxed">{tc.expectedResult}</div>
                        </div>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </div>
  );
}
