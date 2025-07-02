import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  const body = await req.json();
  const backendUrl = process.env.NEXT_PUBLIC_API_URL || '';

  if (!backendUrl) {
    return NextResponse.json({ error: 'Backend URL not configured' }, { status: 500 });
  }

  try {
    const response = await fetch(`${backendUrl}/api/image`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const error = await response.text();
      return NextResponse.json({ error }, { status: response.status });
    }

    const data = await response.json();
    // Map imageUrls[0] to imageUrl for frontend compatibility
    return NextResponse.json({
      ...data,
      imageUrl: Array.isArray(data.imageUrls) ? data.imageUrls[0] : undefined,
    });
  } catch (error: unknown) {
    let message = 'Unknown error';
    if (error instanceof Error) {
      message = error.message;
    }
    return NextResponse.json({ error: message }, { status: 500 });
  }
} 