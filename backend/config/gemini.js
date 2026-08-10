const { GoogleGenerativeAI } = require("@google/generative-ai");

if (!process.env.GEMINI_API_KEY) {
  throw new Error("GEMINI_API_KEY is missing in .env file");
}

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

// موديل عام للمحادثة النصية (بدون قيود على شكل الرد)
const chatModel = genAI.getGenerativeModel({ model: "gemini-3.6-flash" });

// موديل مخصص لتحليل الصور (يرجع JSON منظم فقط)
//
// نتيجتان ممكنتان يحددهما result_category:
//  - environmental_incident: حريق/احتطاب/مخلفات/تلوث مياه/آفة نباتية — الحقول
//    القديمة (issue_type, risk_score, risk_level, recommendation) كما هي، بلا تغيير.
//  - recyclable_waste: عنصر قابل لإعادة التدوير أو إعادة الاستخدام (بلاستيك،
//    زجاج، معدن، ورق) — حقول جديدة فقط (material_category,
//    disposal_classification, reuse_suggestion) تدعم اقتصاد دائري بسيط دون
//    التعامل معه كحادثة بيئية.
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
          description: "هل الصورة مرتبطة بالمجال البيئي، أو تُظهر عنصرًا قابلًا لإعادة التدوير/الاستخدام (اقتصاد دائري)؟ يشمل ذلك: حيوانات وكائنات برية، نباتات وأشجار، تربة وصخور وتضاريس، مصادر مياه، تلوث ونفايات وتسربات، حرائق ودخان، آثار أو أضرار بيئية، موائل طبيعية، أو عبوات/مواد قابلة لإعادة التدوير كالبلاستيك والزجاج والمعدن والورق. false لو الصورة تظهر بشكل أساسي: شخص فقط، سيارة فقط، مبنى أو غرفة داخلية بلا نفايات ظاهرة، جهاز إلكتروني يعمل، طعام، ملابس، أو مستند/ورقة بدون أي عنصر بيئي واضح",
        },
        result_category: {
          type: "string",
          enum: ["environmental_incident", "recyclable_waste"],
          description: "نوع النتيجة: environmental_incident لحادثة بيئية تحتاج بلاغًا (حريق، احتطاب، مخلفات، تلوث مياه، ضرر بيئي)، أو recyclable_waste لعنصر يومي قابل لإعادة التدوير أو إعادة الاستخدام (عبوة بلاستيكية، علبة معدنية، زجاجة، كرتون). يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        issue_type: {
          type: "string",
          description: "نوع المشكلة البيئية لحالة environmental_incident فقط — واحدة من: حريق، احتطاب، مخلفات (نفايات صلبة أو بلاستيكية)، تلوث مياه، آفة نباتية، أو حالة بيئية أخرى. يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
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
          description: "درجة الخطورة من 0 إلى 100 (لحالة environmental_incident فقط)",
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
          description: "الإجراء المستدام المقترح للتعامل مع الحالة (لحالة environmental_incident). لحالات المخلفات والتلوث، فضّل حلول الاقتصاد الدائري (الفرز، إعادة التدوير، إعادة الاستخدام، تقليل الهدر). لحالات الطوارئ (حريق، احتطاب، آفة نباتية)، اذكر الإجراء البيئي المناسب. يُملأ فقط لو is_environmental و is_recognizable كلاهما true",
        },
        material_category: {
          type: "string",
          description: "نوع المادة (لحالة recyclable_waste فقط) مثل: بلاستيك، زجاج، معدن، ورق وكرتون، عضوي، إلكتروني",
        },
        disposal_classification: {
          type: "string",
          description: "تصنيف التخلص (لحالة recyclable_waste فقط) مثل: قابل لإعادة التدوير، قابل لإعادة الاستخدام، نفايات خطرة",
        },
        reuse_suggestion: {
          type: "string",
          description: "اقتراح اختياري لإعادة استخدام إبداعي (لحالة recyclable_waste فقط) — اتركه فارغًا لو لا يوجد اقتراح مناسب لهذا العنصر بعينه",
        },
      },
      required: ["is_recognizable", "is_environmental", "confidence"],
    },
  },
});

const ANALYZE_IMAGE_PROMPT =
  "حلل هذه الصورة لتطبيق حِمى (Hima AI)، المستخدم لرصد الحالات البيئية بالمحميات الطبيعية ولتحديد العناصر القابلة لإعادة التدوير أو إعادة الاستخدام. أولاً حدد هل محتواها واضح بدرجة كافية (is_recognizable)، وهل هي مرتبطة بالمجال البيئي أو تُظهر عنصرًا قابلًا لإعادة التدوير/الاستخدام (is_environmental). إذا لم تكن كذلك، لا تكتب وصفًا أو تحليلاً تفصيليًا. فقط في حال كانت الصورة واضحة ومرتبطة بذلك، حدد result_category: (أ) environmental_incident لحادثة بيئية تحتاج بلاغًا — صنّف issue_type إلى واحدة من: حريق، احتطاب غير نظامي، مخلفات (نفايات صلبة أو بلاستيكية)، تلوث مياه، آفة نباتية، أو حالة بيئية أخرى؛ واملأ risk_score وrisk_level؛ أو (ب) recyclable_waste لعنصر يومي قابل لإعادة التدوير أو إعادة الاستخدام (بلاستيك، زجاج، معدن، ورق) — املأ material_category وdisposal_classification وreuse_suggestion الاختياري. بكلتا الحالتين، اذكر الأثر البيئي (environmental_impact) واقترح إجراءً مستدامًا (recommendation) — يفضَّل حلول الاقتصاد الدائري (فرز، تدوير، إعادة استخدام) لحالات المخلفات والعناصر القابلة لإعادة التدوير، ومعالجة المصدر واحتواء التسرب لحالات تلوث المياه، والإجراء البيئي المناسب لحالات الطوارئ الأخرى.";

// Shared by /analyze and POST /reports so both call Gemini identically.
// `context` (optional) folds the caller's own description/location into the
// prompt as extra hints — it never changes which fields come back.
async function analyzeImage(buffer, mimeType, context = {}) {
  const imagePart = {
    inlineData: {
      data: buffer.toString("base64"),
      mimeType,
    },
  };

  let prompt = ANALYZE_IMAGE_PROMPT;
  if (context.description) prompt += ` ملاحظة المستخدم: "${context.description}".`;
  if (context.latitude && context.longitude) prompt += ` إحداثيات الموقع: ${context.latitude}, ${context.longitude}.`;

  const result = await visionModel.generateContent([prompt, imagePart]);
  return JSON.parse(result.response.text());
}

// Gemini's issue_type is free text ("حريق"), not the enum the reports table's
// type column requires ("FIRE") — this maps between them. Shared by /analyze
// and POST /reports so both classify identically.
const TYPE_KEYWORDS = [
  { type: "FIRE", keywords: ["حريق", "دخان", "fire", "smoke"] },
  { type: "ILLEGAL_LOGGING", keywords: ["احتطاب", "قطع أشجار", "قطع الأشجار", "logging", "deforestation"] },
  { type: "WATER_POLLUTION", keywords: ["تلوث مياه", "تلوث", "تسرب", "pollution", "spill", "contamination"] },
  { type: "WASTE", keywords: ["مخلفات بلاستيكية", "مخلفات", "نفايات", "قمامة", "waste", "garbage", "plastic"] },
  { type: "PLANT_DISEASE", keywords: ["آفة نباتية", "آفة", "مرض نباتي", "plant disease", "pest"] },
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

module.exports = { chatModel, visionModel, analyzeImage, mapIssueTypeToEnum, mapSeverity };
