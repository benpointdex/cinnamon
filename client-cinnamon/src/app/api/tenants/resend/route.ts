import { NextResponse } from "next/server";

const BACKEND_URL =
  process.env.BACKEND_API_URL || "https://cinnamon-l7jf.onrender.com";

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { tenantId, email } = body;

    const query = new URLSearchParams();
    if (tenantId) query.set("tenantId", tenantId);
    if (email) query.set("email", email);

    const res = await fetch(
      `${BACKEND_URL}/api/tenants/resend?${query.toString()}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ tenantId, email }),
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
            "Unable to resend verification code. Please check your email.",
        },
        { status: res.status }
      );
    }

    return NextResponse.json(data, { status: 200 });
  } catch (error: unknown) {
    const err = error as Error;
    return NextResponse.json(
      { error: err.message || "Failed to contact backend to resend code" },
      { status: 500 }
    );
  }
}
