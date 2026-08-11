
const express = require("express");
const cors = require("cors");
require("dotenv").config();
const { chatModel, analyzeImage } = require("./config/gemini");
const upload = require("./config/multer");
const supabase = require("./config/supabase");
const authRoutes = require("./routes/auth");
const reportsRoutes = require("./routes/reports");
const firesRoutes = require("./routes/fires");
const protectedAreasRoutes = require("./routes/protectedAreas");
const authMiddleware = require("./middleware/authMiddleware");
const { mapIssueTypeToReportType, mapRiskLevelToSeverity } = require("./domain/reportClassification");

const app = express();
app.use(cors());
app.use(express.json());
app.use("/auth", authRoutes);
app.use("/reports", reportsRoutes);
app.use("/fires", firesRoutes);
app.use("/protected-areas", protectedAreasRoutes);

app.get("/", (req, res) => {
  res.json({
    status: "HimaAI Backend is running 🚀"
  });
});

app.post("/chat", async (req, res) => {
  try {
    const { message } = req.body;

    if (!message) {
      return res.status(400).json({ error: "message is required" });
    }

    const result = await chatModel.generateContent(message);
    const responseText = result.response.text();

    res.json({
      status: "success",
      reply: responseText
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong with Gemini API" });
  }
});

app.post("/analyze", authMiddleware, upload.single("image"), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: "image file is required" });
    }

    const { latitude, longitude, description: userDescription, language } = req.body;
    const userId = req.user.id;

    const parsedLatitude = Number.parseFloat(latitude);
    const parsedLongitude = Number.parseFloat(longitude);
    const analysis = await analyzeImage(req.file.buffer, req.file.mimetype, {
      description: userDescription,
      latitude: parsedLatitude,
      longitude: parsedLongitude,
      language,
    });

    if (!analysis.is_recognizable || !analysis.is_environmental) {
      return res.status(422).json({
        status: "error",
        error: "تعذر التعرف على محتوى الصورة. يرجى رفع صورة أوضح مرتبطة بالبيئة.",
      });
    }

    if (!["environmental_incident", "recyclable_waste"].includes(analysis.result_category)) {
      return res.status(502).json({
        status: "error",
        error: "The analysis returned an unsupported result category.",
      });
    }

    const userSupabase = supabase.createUserClient(req.token);

  
    const imageBucket = "report-images";
    const safeOriginalName = req.file.originalname.replace(/[^a-zA-Z0-9._-]/g, "_");
    const fileName = `${userId}/${Date.now()}-${safeOriginalName}`;
    const { error: storageError } = await userSupabase
      .storage
      .from(imageBucket)
      .upload(fileName, req.file.buffer, { contentType: req.file.mimetype });

    if (storageError) {
      console.error("Storage Error:", storageError);
      return res.status(500).json({ error: "Something went wrong while saving the evidence image. Please try again." });
    }

    const { data: publicUrlData } = userSupabase.storage.from(imageBucket).getPublicUrl(fileName);
    const imageUrl = publicUrlData ? publicUrlData.publicUrl : "";

   
    const { data: dbData, error: dbError } = await userSupabase
      .from("reports")
      .insert({
        user_id: userId,
        image_url: imageUrl,
        latitude: latitude ? parseFloat(latitude) : null,
        longitude: longitude ? parseFloat(longitude) : null,
        description: analysis.description,
        type: analysis.result_category === "recyclable_waste"
          ? "WASTE"
          : mapIssueTypeToReportType(analysis.issue_type),
        severity: analysis.result_category === "recyclable_waste"
          ? "LOW"
          : mapRiskLevelToSeverity(analysis.risk_level),
        recommended_action: analysis.recommendation,
        environmental_impact: analysis.environmental_impact ?? null,
        confidence: analysis.confidence,
        ai_analysis: analysis,
        status: "OPEN"
      })
      .select()
      .single();

    if (dbError) {
      console.error("Database Insert Error:", dbError);
    
      return res.status(500).json({ error: "Something went wrong while saving the report. Please try again." });
    }

    res.json({
      status: "success",
      result_category: analysis.result_category,
      message: "Report generated and saved successfully",
      report_id: dbData.id,
      ai_result: analysis,
      image_url: imageUrl,
      latitude: dbData.latitude,
      longitude: dbData.longitude,
      created_at: dbData.created_at
    });

  } catch (error) {
    if (error?.status === 429) {
      const quotaMessage = req.body?.language === "en"
        ? "AI analysis is temporarily unavailable because the daily service limit was reached. Please try again later."
        : "تحليل الذكاء الاصطناعي غير متاح مؤقتًا بسبب بلوغ الحد اليومي للخدمة. يرجى المحاولة لاحقًا.";
      return res.status(429).json({ error: quotaMessage });
    }

   
    console.error(error);
    res.status(500).json({ error: "Something went wrong while analyzing the image. Please try again." });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
