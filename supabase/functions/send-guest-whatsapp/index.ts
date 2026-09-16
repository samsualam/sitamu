import { createClient, type SupabaseClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Origin": "*",
};

type GuestVisit = {
  id: string;
  name: string;
  institution: string | null;
  phone: string;
  destination_id: number;
  purpose: string;
  status: string;
  whatsapp_status: string;
};

type RecipientContact = Record<string, unknown>;

const recipientNumberKeys = [
  "whatsapp_number",
  "phone_number",
  "phone",
  "number",
  "whatsapp",
];

function jsonResponse(body: Record<string, unknown>, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
  });
}

function getAdminClient(): SupabaseClient {
  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

  if (!supabaseUrl || !serviceRoleKey) {
    throw new Error("Konfigurasi Supabase server belum lengkap.");
  }

  return createClient(supabaseUrl, serviceRoleKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false,
    },
  });
}

function getRecipientNumber(contact: RecipientContact): string | null {
  for (const key of recipientNumberKeys) {
    const value = contact[key];
    if (typeof value === "string" && value.trim()) {
      return value.trim();
    }
  }

  return null;
}

async function markWhatsappFailed(
  supabase: SupabaseClient,
  visitId: string,
  message: string,
  status = 502,
): Promise<Response> {
  const { error } = await supabase
    .from("guest_visits")
    .update({ whatsapp_status: "FAILED" })
    .eq("id", visitId);

  if (error) {
    console.error("Gagal menyimpan whatsapp_status FAILED:", error.message);
  }

  return jsonResponse({ success: false, error: message }, status);
}

async function getCurrentVisit(
  supabase: SupabaseClient,
  visitId: string,
): Promise<GuestVisit | null> {
  const { data, error } = await supabase
    .from("guest_visits")
    .select("id, name, institution, phone, destination_id, purpose, status, whatsapp_status")
    .eq("id", visitId)
    .maybeSingle();

  if (error) {
    throw new Error(`Gagal mengambil data kunjungan: ${error.message}`);
  }

  return data as GuestVisit | null;
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (request.method !== "POST") {
    return jsonResponse({ success: false, error: "Method harus POST." }, 405);
  }

  let visitId: string;
  try {
    const body = await request.json();
    visitId = typeof body?.visitId === "string" ? body.visitId.trim() : "";
  } catch {
    return jsonResponse({ success: false, error: "Body JSON tidak valid." }, 400);
  }

  if (!visitId) {
    return jsonResponse({ success: false, error: "visitId wajib diisi." }, 400);
  }

  const fonnteToken = Deno.env.get("FONNTE_TOKEN");
  if (!fonnteToken) {
    return jsonResponse({ success: false, error: "FONNTE_TOKEN belum dikonfigurasi." }, 500);
  }

  let supabase: SupabaseClient;
  try {
    supabase = getAdminClient();
  } catch (error) {
    return jsonResponse({
      success: false,
      error: error instanceof Error ? error.message : "Konfigurasi Supabase server belum lengkap.",
    }, 500);
  }

  let visit: GuestVisit | null;
  try {
    visit = await getCurrentVisit(supabase, visitId);
  } catch (error) {
    return jsonResponse({
      success: false,
      error: error instanceof Error ? error.message : "Gagal mengambil data kunjungan.",
    }, 500);
  }

  if (!visit) {
    return jsonResponse({ success: false, error: "Data kunjungan tidak ditemukan." }, 404);
  }

  if (visit.status !== "APPROVED") {
    return jsonResponse({
      success: false,
      error: "WhatsApp hanya dapat dikirim untuk kunjungan berstatus APPROVED.",
    }, 409);
  }

  if (visit.whatsapp_status === "SENT") {
    return jsonResponse({ success: true, status: "SENT", duplicate: true });
  }

  const { data: claimedVisit, error: claimError } = await supabase
    .from("guest_visits")
    .update({ whatsapp_status: "SENDING" })
    .eq("id", visitId)
    .eq("status", "APPROVED")
    .neq("whatsapp_status", "SENT")
    .neq("whatsapp_status", "SENDING")
    .select("id")
    .maybeSingle();

  if (claimError) {
    return markWhatsappFailed(supabase, visitId, "Kunjungan tidak dapat diproses untuk pengiriman WhatsApp.", 500);
  }

  if (!claimedVisit) {
    const currentVisit = await getCurrentVisit(supabase, visitId);
    if (currentVisit?.whatsapp_status === "SENT") {
      return jsonResponse({ success: true, status: "SENT", duplicate: true });
    }
    if (currentVisit?.whatsapp_status === "SENDING") {
      return jsonResponse({ success: false, error: "Pengiriman WhatsApp sedang diproses." }, 409);
    }
    return jsonResponse({ success: false, error: "Kunjungan tidak dapat dikunci untuk diproses." }, 409);
  }

  const { data: contact, error: contactError } = await supabase
    .from("recipient_contacts")
    .select("*")
    .eq("destination_id", visit.destination_id)
    .limit(1)
    .maybeSingle();

  if (contactError) {
    return markWhatsappFailed(supabase, visitId, "Kontak WhatsApp tujuan tidak dapat diambil.");
  }

  if (!contact) {
    return markWhatsappFailed(supabase, visitId, "Kontak WhatsApp untuk tujuan tidak ditemukan.", 404);
  }

  const target = getRecipientNumber(contact as RecipientContact);
  if (!target) {
    return markWhatsappFailed(supabase, visitId, "Nomor WhatsApp tujuan tidak ditemukan pada recipient_contacts.", 422);
  }

  const message = [
    "Permintaan kunjungan telah disetujui.",
    "",
    `Nama Tamu: ${visit.name}`,
    `Instansi: ${visit.institution ?? "-"}`,
    `No. HP: ${visit.phone}`,
    `Keperluan: ${visit.purpose}`,
    "",
    "Silakan bersiap menerima tamu.",
  ].join("\n");

  let fonnteResponse: Response;
  let fonntePayload: unknown = null;
  try {
    const form = new URLSearchParams();
    form.set("target", target);
    form.set("message", message);
    form.set("countryCode", "62");

    fonnteResponse = await fetch("https://api.fonnte.com/send", {
      method: "POST",
      headers: {
        Authorization: fonnteToken,
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: form.toString(),
    });
    fonntePayload = await fonnteResponse.json().catch(() => null);
  } catch (error) {
    console.error("Request ke Fonnte gagal:", error instanceof Error ? error.message : error);
    return markWhatsappFailed(supabase, visitId, "Fonnte gagal mengirim pesan WhatsApp.");
  }

  const fonnteStatus =
    typeof fonntePayload === "object" && fonntePayload !== null && "status" in fonntePayload
      ? fonntePayload.status
      : null;
  const fonnteSucceeded =
    fonnteResponse.ok && (fonnteStatus === true || fonnteStatus === "true");

  if (!fonnteSucceeded) {
    console.error("Fonnte menolak pengiriman:", fonnteResponse.status, fonntePayload);
    return markWhatsappFailed(supabase, visitId, "Fonnte gagal mengirim pesan WhatsApp.");
  }

  const { error: sentError } = await supabase
    .from("guest_visits")
    .update({ whatsapp_status: "SENT" })
    .eq("id", visitId)
    .eq("status", "APPROVED");

  if (sentError) {
    return markWhatsappFailed(supabase, visitId, "Pesan terkirim, tetapi status WhatsApp gagal disimpan.", 500);
  }

  return jsonResponse({ success: true, status: "SENT" });
});
