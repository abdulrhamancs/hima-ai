
const express = require("express");
const cors = require("cors");
require("dotenv").config();
const { chatModel, analyzeImage } = require("./config/gemini");
const upload = require("./config/multer");
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

    res.json({
      status: "success",
      issue_type: analysis.issue_type,
      description: analysis.description,
      risk_score: analysis.risk_score,
      risk_level: analysis.risk_level,
      confidence: analysis.confidence,
      recommendation: analysis.recommendation,
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong while analyzing the image" });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});