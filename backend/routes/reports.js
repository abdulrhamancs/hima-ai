
const express = require("express");
const router = express.Router();
const { randomUUID } = require("crypto");
const { createUserClient } = require("../config/supabase");
const { analyzeImage } = require("../config/gemini");
const upload = require("../config/multer");
const authMiddleware = require("../middleware/authMiddleware");

const EXT_BY_MIME = {
  "image/jpeg": "jpg",
  "image/png": "png",
  "image/webp": "webp",
  "image/gif": "gif",
};

const TYPE_KEYWORDS = [
  { type: "FIRE", keywords: ["حريق", "دخان", "fire", "smoke"] },
  { type: "ILLEGAL_LOGGING", keywords: ["احتطاب", "قطع أشجار", "قطع الأشجار", "logging", "deforestation"] },
  { type: "ILLEGAL_HUNTING", keywords: ["صيد جائر", "صيد", "hunting", "poaching"] },
  { type: "WASTE", keywords: ["نفايات", "تلوث", "قمامة", "waste", "pollution", "garbage"] },
  { type: "PLANT_DISEASE", keywords: ["آفة نباتية", "آفة", "مرض نباتي", "plant disease", "pest"] },
  { type: "INJURED_ANIMAL", keywords: ["حيوان مصاب", "مصاب", "injured"] },
  { type: "DEAD_ANIMAL", keywords: ["حيوان نافق", "نافق", "حيوان ميت", "dead animal", "carcass"] },
];

function mapIssueTypeToEnum(text) {
  if (!text) return "OTHER";
  const normalized = text.toLowerCase();
  for (const { type, keywords } of TYPE_KEYWORDS) {
    if (keywords.some((k) => normalized.includes(k.toLowerCase()))) {
      return type;
    }
  }
  return "OTHER";
}

function mapSeverity(riskLevel) {
  const upper = String(riskLevel || "").toUpperCase();
  return ["LOW", "MEDIUM", "HIGH", "CRITICAL"].includes(upper) ? upper : "LOW";
}

// POST /reports — image + description + location -> Gemini -> structured result -> saved report
router.post("/", authMiddleware, upload.single("image"), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: "image file is required" });
    }

    const { description } = req.body;
    const lat = parseFloat(req.body.latitude);
    const lng = parseFloat(req.body.longitude);

    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      return res.status(400).json({ error: "latitude and longitude are required and must be numbers" });
    }

    const analysis = await analyzeImage(req.file.buffer, req.file.mimetype);

    if (!analysis.is_recognizable) {
      return res.status(422).json({
        status: "error",
        error: "تعذر التعرف على محتوى الصورة. يرجى رفع صورة أوضح مرتبطة بالبيئة.",
      });
    }

    if (!analysis.is_environmental) {
      return res.status(422).json({
        status: "error",
        error: "الصورة غير مرتبطة بالمجال البيئي. يرجى رفع صورة مرتبطة بالبيئة أو الحياة البرية.",
      });
    }

    const userClient = createUserClient(req.token);

    const ext = EXT_BY_MIME[req.file.mimetype] || "jpg";
    const path = `${req.user.id}/${Date.now()}-${randomUUID()}.${ext}`;

    const { error: uploadError } = await userClient.storage
      .from("report-images")
      .upload(path, req.file.buffer, { contentType: req.file.mimetype });

    if (uploadError) {
      return res.status(500).json({ error: uploadError.message });
    }

    const { data: publicUrlData } = userClient.storage.from("report-images").getPublicUrl(path);

    const mergedDescription = description
      ? `${description}\n\n${analysis.description || ""}`.trim()
      : analysis.description || null;

    const { data: report, error: insertError } = await userClient
      .from("reports")
      .insert({
        user_id: req.user.id,
        image_url: publicUrlData.publicUrl,
        description: mergedDescription,
        type: mapIssueTypeToEnum(analysis.issue_type),
        severity: mapSeverity(analysis.risk_level),
        latitude: lat,
        longitude: lng,
        ai_analysis: analysis,
        confidence: analysis.confidence ?? null,
        recommended_action: analysis.recommendation ?? null,
        environmental_impact: analysis.environmental_impact ?? null,
      })
      .select()
      .single();

    if (insertError) {
      return res.status(500).json({ error: insertError.message });
    }

    res.status(201).json({ status: "success", report });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong while creating the report" });
  }
});

// GET /reports/map — lightweight markers for the map view
router.get("/map", authMiddleware, async (req, res) => {
  try {
    const { status, type } = req.query;
    const userClient = createUserClient(req.token);

    let query = userClient
      .from("reports")
      .select("id, latitude, longitude, type, severity, status, created_at")
      .order("created_at", { ascending: false });

    if (status) query = query.eq("status", String(status).toUpperCase());
    if (type) query = query.eq("type", String(type).toUpperCase());

    const { data, error } = await query;

    if (error) {
      return res.status(500).json({ error: error.message });
    }

    res.json({ status: "success", reports: data });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong while loading map reports" });
  }
});

// GET /reports — full history (own or all, with pagination)
router.get("/", authMiddleware, async (req, res) => {
  try {
    const { mine, status, type } = req.query;

    let limit = parseInt(req.query.limit, 10);
    if (!Number.isFinite(limit) || limit <= 0) limit = 50;
    limit = Math.min(limit, 200);

    let offset = parseInt(req.query.offset, 10);
    if (!Number.isFinite(offset) || offset < 0) offset = 0;

    const userClient = createUserClient(req.token);

    let query = userClient
      .from("reports")
      .select("*")
      .order("created_at", { ascending: false })
      .range(offset, offset + limit - 1);

    if (mine === "true") query = query.eq("user_id", req.user.id);
    if (status) query = query.eq("status", String(status).toUpperCase());
    if (type) query = query.eq("type", String(type).toUpperCase());

    const { data, error } = await query;

    if (error) {
      return res.status(500).json({ error: error.message });
    }

    res.json({ status: "success", reports: data });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong while loading reports" });
  }
});

// GET /reports/stats — current user's reported/resolved counts (for Profile screen)
router.get("/stats", authMiddleware, async (req, res) => {
  try {
    const userClient = createUserClient(req.token);

    const [reportedResult, resolvedResult] = await Promise.all([
      userClient.from("reports").select("*", { count: "exact", head: true }).eq("user_id", req.user.id),
      userClient.from("reports").select("*", { count: "exact", head: true }).eq("user_id", req.user.id).eq("status", "RESOLVED"),
    ]);

    if (reportedResult.error) {
      return res.status(500).json({ error: reportedResult.error.message });
    }
    if (resolvedResult.error) {
      return res.status(500).json({ error: resolvedResult.error.message });
    }

    res.json({
      status: "success",
      reported_count: reportedResult.count ?? 0,
      resolved_count: resolvedResult.count ?? 0,
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong while loading report stats" });
  }
});

// GET /reports/:id — single report detail
router.get("/:id", authMiddleware, async (req, res) => {
  try {
    const userClient = createUserClient(req.token);

    const { data, error } = await userClient
      .from("reports")
      .select("*")
      .eq("id", req.params.id)
      .single();

    if (error || !data) {
      return res.status(404).json({ status: "error", error: "Report not found" });
    }

    res.json({ status: "success", report: data });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong while loading the report" });
  }
});

module.exports = router;
