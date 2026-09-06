import { NextResponse } from "next/server";

const BACKEND_URL =
  process.env.BACKEND_API_URL || "https://cinnamon-l7jf.onrender.com";

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { tenantId, code, email } = body;

    // Send both query params (for existing Render deployment) and body (for upgraded backend)
    const query = new URLSearchParams();
    if (tenantId) query.set("tenantId", tenantId);
    if (code) query.set("code", code);
    if (email) query.set("email", email);

    const res = await fetch(
      `${BACKEND_URL}/api/tenants/verify?${query.toString()}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ tenantId, code, email }),
      }
    );

    const text = await res.text();
    let data;
    try {
      data = JSON.parse(text);
    } catch {
      data = { message: text };
    }

    if (!res.ok) {
      return NextResponse.json(
        {
          error:
            data.message ||
            data.error ||
            "Invalid or expired verification code. Please try again.",
        },
        { status: res.status }
      );
    }

    return NextResponse.json(data, { status: 200 });
  } catch (error: unknown) {
    const err = error as Error;
    return NextResponse.json(
      { error: err.message || "Failed to verify code with server" },
      { status: 500 }
    );
  }
}
