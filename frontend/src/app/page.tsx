'use client';

import ChatInterface from '@/components/ChatInterface';
import Head from 'next/head';

export default function Home() {
  return (
    <>
      <Head>
        <title>AI Assistant</title>
        <meta name="description" content="AI Assistant - Chat and Image Generation" />
      </Head>
      <main className="min-h-screen bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center p-4">
        <ChatInterface />
      </main>
    </>
  );
}
