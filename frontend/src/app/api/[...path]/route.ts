import { NextRequest } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

export async function GET(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  const path = params.path.join('/');
  const searchParams = request.nextUrl.searchParams;
  
  const backendUrl = `${BACKEND_URL}/${path}?${searchParams.toString()}`;
  
  console.log('[API Proxy] Proxying to:', backendUrl);
  
  try {
    const response = await fetch(backendUrl, {
      headers: {
        'Accept': '*/*',
      },
    });
    
    console.log('[API Proxy] Response status:', response.status);
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('[API Proxy] Backend error:', errorText);
      return new Response(errorText || 'Backend error', { 
        status: response.status,
        headers: {
          'Content-Type': 'text/plain',
        },
      });
    }
    
    const contentType = response.headers.get('content-type');
    
    if (contentType?.includes('image')) {
      const buffer = await response.arrayBuffer();
      return new Response(buffer, {
        headers: {
          'Content-Type': contentType,
          'Cache-Control': 'public, max-age=3600',
        },
      });
    }
    
    const data = await response.text();
    return new Response(data, {
      headers: {
        'Content-Type': contentType || 'text/plain',
        'Cache-Control': 'public, max-age=3600',
      },
    });
  } catch (error) {
    console.error('[API Proxy] Fetch error:', error);
    return new Response(`Backend service unavailable: ${error}`, { status: 503 });
  }
}
