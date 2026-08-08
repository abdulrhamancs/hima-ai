
const express = require("express");
const cors = require("cors");
require("dotenv").config();
const { chatModel, visionModel } = require("./config/gemini");
const upload = require("./config/multer");
const authRoutes = require("./routes/auth");
const authMiddleware = require("./middleware/authMiddleware");

const app = express();
app.use(cors());
app.use(express.json());
app.use("/auth", authRoutes);

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

    const imagePart = {
      inlineData: {
        data: req.file.buffer.toString("base64"),
        mimeType: req.file.mimetype,
      },
    };

    const prompt = "حلل هذه الصورة. أولاً حدد هل محتواها واضح بدرجة كافية (is_recognizable)، وهل هي مرتبطة بالمجال البيئي أو المحميات الطبيعية (is_environmental) — يشمل ذلك الحيوانات والنباتات والتربة ومصادر المياه والتلوث والحرائق وأي أضرار أو ظواهر بيئية. إذا كانت الصورة غير واضحة أو غير مرتبطة بالبيئة، لا تكتب وصفًا أو تحليلاً تفصيليًا لها. فقط في حال كانت الصورة واضحة ومرتبطة بالبيئة، صف المشكلة البيئية إن وجدت مثل حريق أو آفة نباتية أو احتطاب أو صيد جائر.";

    const result = await visionModel.generateContent([prompt, imagePart]);
    const responseText = result.response.text();
    const analysis = JSON.parse(responseText);

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