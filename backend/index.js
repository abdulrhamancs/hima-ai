const express = require("express");
const cors = require("cors");
require("dotenv").config();
const { chatModel, visionModel } = require("./config/gemini");
const upload = require("./config/multer");
const supabase = require("./config/supabase");
const authRoutes = require("./routes/auth");
const reportRoutes = require("./routes/reports");
const authMiddleware = require("./middleware/authMiddleware");

const app = express();

app.use(cors());
app.use(express.json());

app.use("/auth", authRoutes);
app.use("/reports", reportRoutes);

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

    const imagePart = {
      inlineData: {
        data: req.file.buffer.toString("base64"),
        mimeType: req.file.mimetype,
      },
    };

    let prompt = "Analyze this image as an environmental expert for nature reserves in Saudi Arabia. ";
    if (userDescription) prompt += `User description: "${userDescription}". `;
    if (latitude && longitude) prompt += `Location coordinates: ${latitude}, ${longitude}. `;
    prompt += "Determine environmental relevance, assess risk score, describe the issue, and provide one concise investigation_question for the field agent.";

    const result = await visionModel.generateContent([prompt, imagePart]);
    const responseText = result.response.text();
    const analysis = JSON.parse(responseText);

    if (!analysis.is_recognizable || !analysis.is_environmental) {
      return res.status(422).json({
        status: "error",
        error: "Image is not recognizable or not environmentally related",
      });
    }

    const fileName = `reports/${Date.now()}_${req.file.originalname}`;
    const { error: storageError } = await supabase
      .storage
      .from("incident-images")
      .upload(fileName, req.file.buffer, { contentType: req.file.mimetype });

    if (storageError) {
      console.error("Storage Error:", storageError);
    }

    const { data: publicUrlData } = supabase.storage.from("incident-images").getPublicUrl(fileName);
    const imageUrl = publicUrlData ? publicUrlData.publicUrl : "";

    const { data: dbData, error: dbError } = await supabase
      .from("reports")
      .insert({
        user_id: userId,
        image_url: imageUrl,
        latitude: latitude ? parseFloat(latitude) : null,
        longitude: longitude ? parseFloat(longitude) : null,
        user_description: userDescription || null,
        issue_type: analysis.issue_type,
        ai_description: analysis.description,
        risk_score: analysis.risk_score,
        risk_level: analysis.risk_level,
        confidence: analysis.confidence,
        recommendation: analysis.recommendation,
        investigation_question: analysis.investigation_question,
        status: "pending_question"
      })
      .select()
      .single();

    if (dbError) {
      console.error("Database Insert Error:", dbError);
      return res.status(500).json({ error: "Failed to persist report: " + dbError.message });
    }

    res.json({
      status: "success",
      message: "Report generated and saved successfully",
      report_id: dbData.id,
      ai_result: analysis,
      image_url: imageUrl
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({ error: error.message || "An unexpected error occurred" });
  }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});