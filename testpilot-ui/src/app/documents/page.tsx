'use client';

import { useEffect, useState } from 'react';
import { api, DocumentMetadata, DocumentChunk } from '@/lib/api';

export default function DocumentsPage() {
    const [documents, setDocuments] = useState<DocumentMetadata[]>([]);
    const [selectedDoc, setSelectedDoc] = useState<DocumentMetadata | null>(null);
    const [chunks, setChunks] = useState<DocumentChunk[]>([]);
    const [loading, setLoading] = useState(true);
    const [uploading, setUploading] = useState(false);

    useEffect(() => {
        loadDocuments();
    }, []);

    const loadDocuments = async () => {
        try {
            const data = await api.listDocuments();
            setDocuments(data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        setUploading(true);
        try {
            await api.uploadPdf(file);
            await loadDocuments();
        } catch (err) {
            alert('Upload failed');
        } finally {
            setUploading(false);
        }
    };

    const viewChunks = async (doc: DocumentMetadata) => {
        setSelectedDoc(doc);
        setChunks([]);
        try {
            const data = await api.listChunks(doc.id);
            setChunks(data);
        } catch (err) {
            console.error(err);
        }
    };

    return (
        <div className="animate-fade-in flex flex-col gap-12">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-6 border-b border-glass-border pb-10">
                <div>
                    <br />
                    <h1 className="text-3xl font-black mb-4 tracking-tight">Knowledge Base</h1>
                    <p className="text-1xl text-slate-400 max-w-xl font-small">Manage and audit the documentation ingested into the RAG system.</p>
                </div>
                <label className={`btn-premium shadow-lg ${uploading ? 'opacity-50 cursor-not-allowed' : ''}`}>
                    <svg className={`w-5 h-5 ${uploading ? 'animate-bounce' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                    </svg>
                    {uploading ? 'Ingesting PDF...' : 'Upload New Document'}
                    <input type="file" accept=".pdf" className="hidden" onChange={handleUpload} disabled={uploading} />
                </label>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
                <div className="lg:col-span-7">
                    <div className="glass p-8 min-h-[500px]">
                        <div className="flex justify-between items-center mb-8">
                            <h2 className="text-xl font-black flex items-center gap-3">
                                <span className="w-2 h-6 bg-primary rounded-full"></span>
                                Ingested Documents
                            </h2>
                            <span className="text-xs font-bold px-3 py-1 rounded-full bg-primary/10 text-primary border border-primary/20">
                                {documents.length} Total
                            </span>
                        </div>

                        {loading ? (
                            <div className="flex flex-col items-center justify-center py-24 gap-4">
                                <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
                                <div className="text-slate-500 font-bold uppercase tracking-widest text-[10px]">Scanning Repository</div>
                            </div>
                        ) : documents.length === 0 ? (
                            <div className="py-24 text-center">
                                <div className="text-5xl mb-6 opacity-20">📂</div>
                                <h4 className="text-lg font-bold mb-2">No documents found</h4>
                                <p className="text-slate-500 text-sm">Upload your first PDF to begin building context.</p>
                            </div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="premium-table">
                                    <thead>
                                        <tr>
                                            <th className="w-1/2 text-[10px]">Filename</th>
                                            <th className="hidden md:table-cell text-[10px]">Ingested At</th>
                                            <th className="text-right text-[10px]">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {documents.map((doc) => (
                                            <tr key={doc.id} className={`group ${selectedDoc?.id === doc.id ? 'ring-2 ring-primary/50 ring-inset' : ''}`}>
                                                <td>
                                                    <div className="flex items-center gap-3">
                                                        <div className="p-2 rounded-lg bg-red-500/10 text-red-400">
                                                            <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                                                                <path d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" />
                                                            </svg>
                                                        </div>
                                                        <div className="font-bold text-sm text-white group-hover:text-primary transition-colors truncate max-w-xs">
                                                            {doc.filename}
                                                        </div>
                                                    </div>
                                                </td>
                                                <td className="hidden md:table-cell">
                                                    <div className="text-slate-400 font-medium text-xs">
                                                        {new Date(doc.uploadedAt).toLocaleDateString()}
                                                    </div>
                                                    <div className="text-[9px] text-slate-600 font-bold uppercase">
                                                        {new Date(doc.uploadedAt).toLocaleTimeString()}
                                                    </div>
                                                </td>
                                                <td>
                                                    <button
                                                        onClick={() => viewChunks(doc)}
                                                        className="bg-white/5 hover:bg-primary hover:text-white text-slate-300 px-3 py-1.5 rounded-lg font-bold text-[13px] transition-all border border-glass-border hover:border-primary border-dashed"
                                                    >
                                                        Explore Chunks
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>

                <div className="lg:col-span-5">
                    <div className="glass p-8 sticky top-32 overflow-hidden flex flex-col h-[700px] border-l-4 border-l-secondary">
                        <h2 className="text-2xl font-black mb-2 flex items-center gap-3">
                            Chunk Audit
                        </h2>
                        <p className="text-xs text-slate-500 font-bold uppercase tracking-widest mb-8">Internal RAG Structure</p>

                        {!selectedDoc ? (
                            <div className="flex-1 flex flex-col items-center justify-center text-slate-500 text-center p-8 border-2 border-dashed border-glass-border rounded-2xl bg-white/[0.02]">
                                <div className="w-16 h-16 mb-6 rounded-full bg-slate-800 flex items-center justify-center opacity-40">
                                    <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                                    </svg>
                                </div>
                                <h3 className="text-lg font-bold mb-2">Selection Required</h3>
                                <p className="text-xs leading-relaxed">Select a document from the repository to inspect how it's indexed for the AI context window.</p>
                            </div>
                        ) : (
                            <div className="flex-1 flex flex-col gap-6 overflow-hidden">
                                <div className="p-4 bg-secondary/10 rounded-xl border border-secondary/20">
                                    <div className="text-[10px] uppercase font-black text-secondary mb-1">Active Context</div>
                                    <div className="text-sm font-bold text-white truncate">{selectedDoc.filename}</div>
                                    <div className="text-[10px] text-slate-500 mt-2 font-bold">{chunks.length} total shards</div>
                                </div>

                                <div className="flex-1 overflow-y-auto pr-2 flex flex-col gap-4">
                                    {chunks.length === 0 ? (
                                        <div className="animate-pulse flex flex-col gap-3">
                                            {[1, 2, 3, 4].map(i => <div key={i} className="h-24 bg-white/5 rounded-xl"></div>)}
                                        </div>
                                    ) : (
                                        chunks.map((chunk) => (
                                            <div key={chunk.id} className="p-4 bg-white/5 rounded-xl border border-glass-border hover:border-secondary/30 transition-all cursor-default group">
                                                <div className="flex justify-between items-center mb-3">
                                                    <span className="text-[10px] font-black text-slate-500 tracking-widest uppercase">Index {chunk.chunkIndex}</span>
                                                    <span className="w-2 h-2 rounded-full bg-secondary group-hover:animate-ping"></span>
                                                </div>
                                                <div className="text-sm text-slate-300 leading-relaxed font-medium line-clamp-4 hover:line-clamp-none transition-all duration-500">
                                                    {chunk.content}
                                                </div>
                                            </div>
                                        ))
                                    )}
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
