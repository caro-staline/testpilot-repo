import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "TestPilot AI",
  description: "Next-gen test case generation and RAG documentation management.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full">
      <body className="h-full bg-background text-foreground antialiased selection:bg-primary/30">
        {/* Immersive Background Gradients */}
        <div className="fixed inset-0 z-[-1] overflow-hidden pointer-events-none">
          <div className="absolute top-[-20%] left-[-10%] w-[60%] h-[60%] bg-primary/10 blur-[150px] rounded-full animate-pulse"></div>
          <div className="absolute bottom-[-20%] right-[-10%] w-[60%] h-[60%] bg-secondary/10 blur-[150px] rounded-full animate-pulse" style={{ animationDelay: '2s' }}></div>
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[40%] h-[40%] bg-accent/5 blur-[120px] rounded-full"></div>
        </div>

        <nav className="glass sticky top-6 mx-6 z-50 pl-16 pr-32 py-8 flex flex-wrap justify-between items-center gap-10 shadow-2xl">
          <div className="flex items-center gap-5">
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-primary to-accent flex items-center justify-center font-black text-white text-4xl shadow-lg">T</div>
            <div className="text-5xl md:text-4xl font-black bg-gradient-to-r from-primary via-secondary to-accent bg-clip-text text-transparent tracking-tighter">
              TestPilot AI
            </div>
            <div className="text-sm md:text-base font-medium">Powered by RAG and State-of-the-Art LLMs</div>
          </div>
          <div className="size-0"></div>
          <div className="size-0"></div>
          <div className="size-0"></div>
          <div className="size-0"></div>
          <div className="flex flex-wrap gap-12 text-base font-bold tracking-wide mr-12">
            <a href="/" className="hover:text-primary transition-all relative group whitespace-nowrap">
              Testcase Generator
              <span className="absolute -bottom-1 left-0 w-0 h-0.5 bg-primary transition-all group-hover:w-full"></span>
            </a>
            <a href="/documents" className="hover:text-primary transition-all relative group whitespace-nowrap">
              Documents
              <span className="absolute -bottom-1 left-0 w-0 h-0.5 bg-primary transition-all group-hover:w-full"></span>
            </a>
          </div>
          <div className="size-0"></div>
        </nav>
        <main className="flex-1 px-6 py-10 md:px-12 md:py-16 max-w-7xl mx-auto w-full">
          {children}
        </main>

        <footer className="px-10 py-8 text-center text-slate-500 text-xs border-t border-glass-border mx-6">
          &copy; 2026 TestPilot AI • Built for high-stake quality assurance
        </footer>
      </body>
    </html>
  );
}
