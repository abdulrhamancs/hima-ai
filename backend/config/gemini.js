const { GoogleGenerativeAI } = require("@google/generative-ai");

if (!process.env.GEMINI_API_KEY) {
  throw new Error("GEMINI_API_KEY is missing in .env file");
}

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

// موديل عام للمحادثة النصية (بدون قيود على شكل الرد)
const chatModel = genAI.getGenerativeModel({ model: "gemini-3.6-flash" });

// موديل مخصص لتحليل الصور (يرجع JSON منظم فقط)
const visionModel = genAI.getGenerativeModel({
  model: "gemini-3.6-flash",
  generationConfig: {
    responseMimeType: "application/json",
    responseSchema: {
      type: "object",
      properties: {
        is_recognizable: {
          type: "boolean",
          description: "هل محتوى الصورة واضح بدرجة كافية لتحديده؟ false لو الصورة ضبابية، مظلمة جدًا، أو غير مفهومة",
        },
        is_environmental: {
          type: "boolean",
          description: "هل الصورة مرتبطة بالمجال البيئي أو المحميات الطبيعية؟ يشمل ذلك: حيوانات وكائنات برية، نباتات وأشجار، تربة وصخور وتضاريس، مصادر مياه (أودية وبحيرات)، تلوث ونفايات وتسربات، حرائق ودخان، آثار أو أضرار بيئية، موائل طبيعية، أو أي مشهد له ارتباط واضح بالبيئة أو المحميات الطبيعية. false لو الصورة تظهر بشكل أساسي: شخص فقط، سيارة فقط، مبنى أو غرفة داخلية، جهاز إلكتروني، طعام، ملابس، أو مستند/ورقة بدون أي عنصر بيئي واضح",
        },
        issue_type: {
          type: "string",
          description: "نوع المشكلة البيئية المكتشفة (مثل: حريق، آفة نباتية، احتطاب، صيد جائر، جفاف، لا يوجد). يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        description: {
          type: "string",
          description: "وصف موجز لما تُظهره الصورة. يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        risk_score: {
          type: "number",
          description: "درجة الخطورة من 0 إلى 100. يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        risk_level: {
          type: "string",
          enum: ["Low", "Medium", "High", "Critical"],
        },
        confidence: {
          type: "number",
          description: "نسبة الثقة في التحليل من 0 إلى 100",
        },
        recommendation: {
          type: "string",
          description: "الإجراء المقترح للتعامل مع الحالة. يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
      },
      required: ["is_recognizable", "is_environmental", "confidence"],
    },
  },
});

module.exports = { chatModel, visionModel };