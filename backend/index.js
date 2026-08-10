
const express = require("express");
const cors = require("cors");
require("dotenv").config();
const { chatModel, analyzeImage, mapIssueTypeToEnum, mapSeverity } = require("./config/gemini");
const upload = require("./config/multer");
const supabase = require("./config/supabase");
const authRoutes = require("./routes/auth");
const reportsRoutes = require("./routes/reports");
const firesRoutes = require("./routes/fires");
const protectedAreasRoutes = require("./routes/protectedAreas");
const authMiddleware = require("./middleware/authMiddleware");

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

    const { latitude, longitude, description: userDescription } = req.body;
    const userId = req.user.id;

    const analysis = await analyzeImage(req.file.buffer, req.file.mimetype, {
      description: userDescription,
      latitude,
      longitude,
    });

    if (!analysis.is_recognizable || !analysis.is_environmental) {
      return res.status(422).json({
        status: "error",
        error: "تعذر التعرف على محتوى الصورة. يرجى رفع صورة أوضح مرتبطة بالبيئة.",
      });
    }

    // A recyclable/reusable item is a circular-economy result, not a field
    // incident — it doesn't belong in the reports table (which drives the
    // map and History), so there's nothing to upload or persist here.
    if (analysis.result_category === "recyclable_waste") {
      return res.json({
        status: "success",
        result_category: "recyclable_waste",
        ai_result: analysis,
      });
    }

    // Scoped to this caller's own token — the default `supabase` client has
    // no user session, so RLS (`auth.uid() = user_id`) rejected every insert
    // regardless of the column-name fix below.
    const userSupabase = supabase.createUserClient(req.token);

    const fileName = `reports/${Date.now()}_${req.file.originalname}`;
    const { error: storageError } = await userSupabase
      .storage
      .from("report-images")
      .upload(fileName, req.file.buffer, { contentType: req.file.mimetype });

    if (storageError) {
      console.error("Storage Error:", storageError);
      return res.status(500).json({ error: "Something went wrong while uploading the image. Please try again." });
    }

    const { data: publicUrlData } = userSupabase.storage.from("report-images").getPublicUrl(fileName);
    const imageUrl = publicUrlData ? publicUrlData.publicUrl : "";

    const { data: dbData, error: dbError } = await userSupabase
      .from("reports")
      .insert({
        user_id: userId,
        image_url: imageUrl,
        latitude: latitude ? parseFloat(latitude) : null,
        longitude: longitude ? parseFloat(longitude) : null,
        description: analysis.description,
        type: mapIssueTypeToEnum(analysis.issue_type),
        severity: mapSeverity(analysis.risk_level),
        recommended_action: analysis.recommendation,
        confidence: analysis.confidence,
        environmental_impact: analysis.environmental_impact ?? null,
        ai_analysis: analysis,
        status: "OPEN"
      })
      .select()
      .single();

    if (dbError) {
      console.error("Database Insert Error:", dbError);
      // The raw Postgres/PostgREST message is for the server log, not the
      // client — it can include column/constraint names that mean nothing
      // to a ranger and shouldn't be exposed anyway.
      return res.status(500).json({ error: "Something went wrong while saving the report. Please try again." });
    }

    res.json({
      status: "success",
      result_category: "environmental_incident",
      message: "Report generated and saved successfully",
      report_id: dbData.id,
      ai_result: analysis,
      image_url: imageUrl
    });

  } catch (error) {
    // Logged in full for debugging; the client only ever gets a clean,
    // generic message — the real error (Gemini internals, stack traces,
    // API URLs) has no business reaching the app's UI.
    console.error(error);
    res.status(500).json({ error: "Something went wrong while analyzing the image. Please try again." });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
