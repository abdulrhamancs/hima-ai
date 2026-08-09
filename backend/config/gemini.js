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
          description: "نوع المشكلة البيئية المكتشفة — واحدة من: حريق، احتطاب، صيد جائر، نفايات أو تلوث، آفة نباتية، حيوان مصاب، حيوان نافق، أو حالة بيئية أخرى. يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        description: {
          type: "string",
          description: "وصف موجز لما تُظهره الصورة. يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        environmental_impact: {
          type: "string",
          description: "الأثر البيئي المترتب على هذه الحالة (مثل: تؤثر على الحياة الفطرية، تلوث مصادر المياه، تدهور التربة). يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
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
          description: "الإجراء المستدام المقترح للتعامل مع الحالة. لحالات النفايات والتلوث، فضّل حلول الاقتصاد الدائري (الفرز، إعادة التدوير، إعادة الاستخدام، تقليل الهدر). لحالات الطوارئ (حريق، صيد جائر، حيوان مصاب أو نافق، آفة نباتية)، اذكر الإجراء البيئي المناسب. يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
      },
      required: ["is_recognizable", "is_environmental", "confidence"],
    },
  },
});

const ANALYZE_IMAGE_PROMPT =
  "حلل هذه الصورة. أولاً حدد هل محتواها واضح بدرجة كافية (is_recognizable)، وهل هي مرتبطة بالمجال البيئي أو المحميات الطبيعية (is_environmental) — يشمل ذلك الحيوانات والنباتات والتربة ومصادر المياه والتلوث والحرائق وأي أضرار أو ظواهر بيئية. إذا كانت الصورة غير واضحة أو غير مرتبطة بالبيئة، لا تكتب وصفًا أو تحليلاً تفصيليًا لها. فقط في حال كانت الصورة واضحة ومرتبطة بالبيئة: صنّف المشكلة البيئية إلى واحدة من هذه الفئات بالتحديد (اذكر اسم الفئة بوضوح ضمن issue_type): حريق، احتطاب، صيد جائر، نفايات أو تلوث، آفة نباتية، حيوان مصاب، حيوان نافق، أو حالة بيئية أخرى لا تندرج تحت ما سبق. اذكر الأثر البيئي المترتب عليها، واقترح إجراءً مستدامًا للتعامل معها — يفضَّل حلول الاقتصاد الدائري (فرز، تدوير، إعادة استخدام) لحالات النفايات والتلوث، والإجراء البيئي المناسب لحالات الطوارئ.";

// Shared by /analyze and POST /reports so both call Gemini identically.
async function analyzeImage(buffer, mimeType) {
  const imagePart = {
    inlineData: {
      data: buffer.toString("base64"),
      mimeType,
    },
  };

  const result = await visionModel.generateContent([ANALYZE_IMAGE_PROMPT, imagePart]);
  return JSON.parse(result.response.text());
}

module.exports = { chatModel, visionModel, analyzeImage };