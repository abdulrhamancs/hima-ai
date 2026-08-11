

DELETE FROM public.reports WHERE image_url LIKE 'https://picsum.photos/seed/hima-demo-%';

DO $$
DECLARE
  v_user_id uuid;
BEGIN
  SELECT id INTO v_user_id FROM auth.users ORDER BY created_at ASC LIMIT 1;

  IF v_user_id IS NULL THEN
    RAISE EXCEPTION 'No account found in auth.users — sign up at least one user before seeding demo reports.';
  END IF;

  INSERT INTO public.reports (
    user_id, image_url, description, type, severity, status,
    latitude, longitude, ai_analysis, confidence,
    recommended_action, environmental_impact, created_at
  )
  VALUES


  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-fire-abha/800/600',
    'رُصد حريق نشط في الغطاء النباتي بمرتفعات عسير قرب أبها، مع انتشار دخان كثيف واتساع رقعة الاشتعال بسرعة بسبب الرياح.',
    'FIRE', 'CRITICAL', 'OPEN', 18.2164, 42.5053,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"حريق غطاء نباتي","risk_level":"CRITICAL","risk_score":94,"confidence":0.95,"description":"حريق نشط ينتشر في غطاء نباتي كثيف على المنحدرات الجبلية.","ai_explanation":"دخان كثيف وألسنة لهب مرئية بوضوح مع امتداد سريع بفعل الرياح الجبلية.","recommendation":"إخطار الدفاع المدني وفرق الإطفاء فوراً وإخلاء المسارات القريبة.","environmental_impact":"تهديد مباشر للغطاء النباتي المحلي والحياة البرية المحيطة، مع خطر انتشار الحريق لمناطق سكنية قريبة."}'::jsonb,
    0.95, 'إخطار الدفاع المدني وفرق الإطفاء فوراً وإخلاء المسارات القريبة.',
    'تهديد مباشر للغطاء النباتي المحلي والحياة البرية المحيطة، مع خطر انتشار الحريق لمناطق سكنية قريبة.',
    now() - interval '3 hours'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-fire-jazan/800/600',
    'دخان متصاعد من منطقة زراعية على أطراف جازان، يُشتبه بأنه ناتج عن حرق مخلفات زراعية خرج عن السيطرة.',
    'FIRE', 'HIGH', 'OPEN', 16.8892, 42.5611,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"حريق زراعي","risk_level":"HIGH","risk_score":76,"confidence":0.9,"description":"حريق متوسط الحجم في أرض زراعية مع دخان كثيف.","ai_explanation":"نمط الاحتراق واللون يتوافقان مع حرق مخلفات زراعية.","recommendation":"إرسال فريق ميداني للتحقق والسيطرة على النيران قبل امتدادها.","environmental_impact":"تلوث هوائي محلي وتهديد للمزارع المجاورة."}'::jsonb,
    0.9, 'إرسال فريق ميداني للتحقق والسيطرة على النيران قبل امتدادها.',
    'تلوث هوائي محلي وتهديد للمزارع المجاورة.',
    now() - interval '9 hours'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-fire-taif/800/600',
    'حريق محدود في أشجار جبلية قرب الطائف تمت السيطرة عليه من قبل فرق الدفاع المدني بعد ساعات من الرصد.',
    'FIRE', 'MEDIUM', 'RESOLVED', 21.2703, 40.4158,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"حريق محدود","risk_level":"MEDIUM","risk_score":52,"confidence":0.88,"description":"حريق صغير محتوى في منطقة شجرية جبلية.","ai_explanation":"رقعة الاشتعال محدودة ولا تظهر علامات امتداد نشط.","recommendation":"متابعة الموقع للتأكد من عدم تجدد الاشتعال.","environmental_impact":"أضرار محدودة ومؤقتة على الغطاء النباتي المحلي."}'::jsonb,
    0.88, 'متابعة الموقع للتأكد من عدم تجدد الاشتعال.',
    'أضرار محدودة ومؤقتة على الغطاء النباتي المحلي.',
    now() - interval '6 days'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-fire-ksrr/800/600',
    'رصد ألسنة لهب داخل حدود محمية الملك سلمان بن عبدالعزيز الملكية، في منطقة نباتية تُعد موطناً لحيوانات برية محمية.',
    'FIRE', 'HIGH', 'OPEN', 29.0154, 40.0287,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"حريق داخل محمية طبيعية","risk_level":"HIGH","risk_score":81,"confidence":0.93,"description":"حريق نشط داخل منطقة محمية ذات تنوع أحيائي حساس.","ai_explanation":"موقع الحريق يقع ضمن حدود منطقة محمية معروفة بموطن للحياة البرية.","recommendation":"تنسيق عاجل مع إدارة المحمية وفرق الإطفاء المتخصصة بالمناطق الطبيعية.","environmental_impact":"خطر مرتفع على الحياة البرية المحمية والتنوع النباتي النادر في المنطقة."}'::jsonb,
    0.93, 'تنسيق عاجل مع إدارة المحمية وفرق الإطفاء المتخصصة بالمناطق الطبيعية.',
    'خطر مرتفع على الحياة البرية المحمية والتنوع النباتي النادر في المنطقة.',
    now() - interval '1 day'
  ),


  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-waste-riyadh/800/600',
    'زجاجة بلاستيكية شفافة ملقاة على الرمال في محيط الرياض.',
    'WASTE', 'LOW', 'RESOLVED', 24.7136, 46.6753,
    '{"result_category":"recyclable_waste","is_recognizable":true,"is_environmental":true,"description":"زجاجة بلاستيكية فارغة (PET) ملقاة على الأرض.","confidence":0.92,"material_category":"بلاستيك","waste_type":"PET","disposal_classification":"قابل لإعادة التدوير","recyclable":true,"reusable":false,"repairable":false,"preferred_action":"إعادة التدوير","recycling_guidance":"يمكن التخلص منها في حاويات إعادة تدوير البلاستيك المخصصة.","ai_explanation":"شكل الزجاجة وغطاؤها يطابقان زجاجات المياه البلاستيكية الشائعة."}'::jsonb,
    0.92, 'التخلص منها عبر حاويات إعادة تدوير البلاستيك.',
    'تراكم بطيء للنفايات البلاستيكية غير القابلة للتحلل في البيئة الطبيعية.',
    now() - interval '8 hours'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-waste-jeddah/800/600',
    'تراكم أكياس ومخلفات بلاستيكية على كورنيش جدة بعد فعالية عامة.',
    'WASTE', 'LOW', 'OPEN', 21.4858, 39.1925,
    '{"result_category":"recyclable_waste","is_recognizable":true,"is_environmental":true,"description":"تجمع أكياس بلاستيكية ومخلفات تعبئة على الواجهة البحرية.","confidence":0.87,"material_category":"بلاستيك مختلط","waste_type":"أكياس وتغليف","disposal_classification":"يتطلب فرز قبل إعادة التدوير","recyclable":true,"reusable":false,"repairable":false,"preferred_action":"جمع وفرز","recycling_guidance":"فرز المواد القابلة لإعادة التدوير قبل نقلها لمركز الفرز البلدي.","ai_explanation":"كثافة المخلفات وتنوعها يوحيان بمخلفات ما بعد فعالية جماهيرية."}'::jsonb,
    0.87, 'تنسيق حملة نظافة ميدانية وفرز المخلفات القابلة لإعادة التدوير.',
    'تهديد للحياة البحرية القريبة من الساحل عبر الجريان السطحي للبلاستيك.',
    now() - interval '9 hours'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-waste-dammam/800/600',
    'مخلفات صلبة متناثرة قرب منطقة صناعية في الدمام.',
    'WASTE', 'LOW', 'OPEN', 26.4207, 50.0888,
    '{"result_category":"recyclable_waste","is_recognizable":true,"is_environmental":true,"description":"مخلفات صلبة متنوعة (بلاستيك ومعدن خفيف) قرب موقع صناعي.","confidence":0.83,"material_category":"مواد مختلطة","waste_type":"صناعي خفيف","disposal_classification":"يتطلب فرز متخصص","recyclable":true,"reusable":false,"repairable":false,"preferred_action":"جمع وفرز متخصص","recycling_guidance":"التواصل مع الجهة المسؤولة عن مخلفات المنطقة الصناعية.","ai_explanation":"طبيعة المخلفات ونوعها تتوافق مع مخلفات نشاط صناعي قريب."}'::jsonb,
    0.83, 'التواصل مع الجهة المسؤولة عن مخلفات المنطقة الصناعية لإزالتها.',
    'خطر تسرب مواد للتربة المحيطة إذا تُركت لفترة طويلة.',
    now() - interval '2 days'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-waste-khobar/800/600',
    'مخلفات تعبئة وتغليف متروكة على شاطئ الخبر.',
    'WASTE', 'LOW', 'RESOLVED', 26.2172, 50.1971,
    '{"result_category":"recyclable_waste","is_recognizable":true,"is_environmental":true,"description":"مخلفات تغليف كرتونية وبلاستيكية على الرمال الساحلية.","confidence":0.9,"material_category":"كرتون وبلاستيك","waste_type":"تغليف","disposal_classification":"قابل لإعادة التدوير جزئياً","recyclable":true,"reusable":false,"repairable":false,"preferred_action":"إعادة التدوير","recycling_guidance":"فصل الكرتون عن البلاستيك قبل التخلص منها.","ai_explanation":"نوع المواد وحالتها تشير إلى مخلفات نزهة شاطئية."}'::jsonb,
    0.9, 'فصل المواد وإعادة تدويرها عبر النقاط المخصصة.',
    'تلوث بصري وتهديد محدود للحياة البحرية الساحلية.',
    now() - interval '11 days'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-waste-yanbu/800/600',
    'زجاجات ومخلفات بلاستيكية متراكمة قرب شاطئ ينبع.',
    'WASTE', 'LOW', 'OPEN', 24.0895, 38.0618,
    '{"result_category":"recyclable_waste","is_recognizable":true,"is_environmental":true,"description":"تجمع زجاجات بلاستيكية فارغة على الشاطئ الرملي.","confidence":0.89,"material_category":"بلاستيك","waste_type":"PET","disposal_classification":"قابل لإعادة التدوير","recyclable":true,"reusable":false,"repairable":false,"preferred_action":"إعادة التدوير","recycling_guidance":"التخلص منها عبر حاويات إعادة تدوير البلاستيك الساحلية.","ai_explanation":"تطابق واضح مع زجاجات مياه بلاستيكية شائعة الاستخدام."}'::jsonb,
    0.89, 'التخلص منها عبر حاويات إعادة تدوير البلاستيك.',
    'تهديد تدريجي للحياة البحرية القريبة من الشاطئ.',
    now() - interval '4 days'
  ),


  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-pollution-dhahran/800/600',
    'بقعة زيتية ملحوظة على سطح المياه قرب الساحل الصناعي في الظهران.',
    'WATER_POLLUTION', 'HIGH', 'OPEN', 26.2361, 50.0393,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"تسرب زيتي","risk_level":"HIGH","risk_score":78,"confidence":0.91,"description":"طبقة زيتية عاكسة تغطي جزءاً من سطح المياه الساحلية.","ai_explanation":"اللون والانعكاس المميز على سطح الماء يطابقان تسرباً نفطياً أو زيتياً.","recommendation":"إخطار الجهات البيئية المختصة لاحتواء التسرب فوراً.","environmental_impact":"خطر مرتفع على الحياة البحرية وجودة المياه الساحلية."}'::jsonb,
    0.91, 'إخطار الجهات البيئية المختصة لاحتواء التسرب فوراً.',
    'خطر مرتفع على الحياة البحرية وجودة المياه الساحلية.',
    now() - interval '1 day'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-pollution-jeddah/800/600',
    'مياه صرف بلون غير طبيعي تتدفق باتجاه البحر الأحمر قرب جدة.',
    'WATER_POLLUTION', 'CRITICAL', 'OPEN', 21.4858, 39.1925,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"تصريف مياه ملوثة","risk_level":"CRITICAL","risk_score":90,"confidence":0.94,"description":"تدفق نشط لمياه ذات لون ورائحة غير طبيعيين باتجاه الشاطئ.","ai_explanation":"اللون الداكن وتدفق المياه بشكل مستمر يشيران لتصريف غير معالج.","recommendation":"وقف مصدر التصريف فوراً وإخطار الجهات البيئية المختصة.","environmental_impact":"تهديد مباشر وشديد للشعاب المرجانية والحياة البحرية في البحر الأحمر."}'::jsonb,
    0.94, 'وقف مصدر التصريف فوراً وإخطار الجهات البيئية المختصة.',
    'تهديد مباشر وشديد للشعاب المرجانية والحياة البحرية في البحر الأحمر.',
    now() - interval '5 hours'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-pollution-qassim/800/600',
    'تلوث محدود في مجرى وادي زراعي قرب بريدة، تمت معالجته بعد التحقق الميداني.',
    'WATER_POLLUTION', 'MEDIUM', 'RESOLVED', 26.3260, 43.9750,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"تلوث زراعي محدود","risk_level":"MEDIUM","risk_score":48,"confidence":0.85,"description":"تغير طفيف في لون المياه ضمن مجرى وادٍ زراعي.","ai_explanation":"التركيز المحدود للمادة الملوثة واحتواء الموقع الجغرافي يقللان من الخطورة.","recommendation":"متابعة جودة المياه دورياً في الموقع.","environmental_impact":"أثر محدود ومؤقت على النظام البيئي للمجرى المائي."}'::jsonb,
    0.85, 'متابعة جودة المياه دورياً في الموقع.',
    'أثر محدود ومؤقت على النظام البيئي للمجرى المائي.',
    now() - interval '12 days'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-pollution-najran/800/600',
    'مياه بها رواسب غير طبيعية عند نقطة تجمع مياه قرب نجران.',
    'WATER_POLLUTION', 'MEDIUM', 'OPEN', 17.4917, 44.1322,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"رواسب غير معتادة","risk_level":"MEDIUM","risk_score":55,"confidence":0.8,"description":"رواسب ذات لون غير معتاد متجمعة عند مصدر مياه محلي.","ai_explanation":"عدم وضوح مصدر الرواسب يستدعي تحققاً ميدانياً إضافياً.","recommendation":"إرسال فريق ميداني لأخذ عينة وتحليلها.","environmental_impact":"احتمال تأثر جودة المياه المستخدمة محلياً للري أو الشرب."}'::jsonb,
    0.8, 'إرسال فريق ميداني لأخذ عينة وتحليلها.',
    'احتمال تأثر جودة المياه المستخدمة محلياً للري أو الشرب.',
    now() - interval '3 days'
  ),

  
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-logging-albaha/800/600',
    'رُصدت عمليات قطع جائر لأشجار العرعر في غابات جبلية بمنطقة الباحة.',
    'ILLEGAL_LOGGING', 'HIGH', 'OPEN', 20.0129, 41.4677,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"قطع أشجار جائر","risk_level":"HIGH","risk_score":73,"confidence":0.89,"description":"جذوع أشجار عرعر مقطوعة حديثاً في منطقة غابية جبلية.","ai_explanation":"أثر القطع الحديث وأدوات القطع اليدوية ظاهرة على الجذوع.","recommendation":"إخطار الجهات المختصة بحماية الغابات وتشديد المراقبة الميدانية.","environmental_impact":"تهديد لأشجار العرعر المعمرة والغطاء النباتي الجبلي النادر."}'::jsonb,
    0.89, 'إخطار الجهات المختصة بحماية الغابات وتشديد المراقبة الميدانية.',
    'تهديد لأشجار العرعر المعمرة والغطاء النباتي الجبلي النادر.',
    now() - interval '2 days'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-logging-abha/800/600',
    'حالة احتطاب سابقة قرب أبها تمت معالجتها بعد تدخل الجهات المختصة.',
    'ILLEGAL_LOGGING', 'MEDIUM', 'RESOLVED', 18.2164, 42.5053,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"قطع أشجار محدود","risk_level":"MEDIUM","risk_score":45,"confidence":0.82,"description":"عدد محدود من الأشجار المقطوعة في منطقة جبلية.","ai_explanation":"نطاق القطع محدود جغرافياً ولا يظهر نشاطاً مستمراً.","recommendation":"إغلاق الملف بعد التأكد من وقف النشاط.","environmental_impact":"أثر محدود على الغطاء النباتي المحلي."}'::jsonb,
    0.82, 'إغلاق الملف بعد التأكد من وقف النشاط.',
    'أثر محدود على الغطاء النباتي المحلي.',
    now() - interval '15 days'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-logging-taif/800/600',
    'أشجار مقطوعة حديثاً بالقرب من طرق زراعية في الطائف.',
    'ILLEGAL_LOGGING', 'MEDIUM', 'OPEN', 21.2703, 40.4158,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"قطع أشجار","risk_level":"MEDIUM","risk_score":58,"confidence":0.84,"description":"جذوع أشجار مقطوعة حديثاً قرب طريق زراعي فرعي.","ai_explanation":"حداثة القطع واضحة من لون الجذع الداخلي.","recommendation":"إرسال فريق ميداني للتحقق من مصدر النشاط.","environmental_impact":"تدهور تدريجي للغطاء النباتي المحلي إذا استمر النشاط."}'::jsonb,
    0.84, 'إرسال فريق ميداني للتحقق من مصدر النشاط.',
    'تدهور تدريجي للغطاء النباتي المحلي إذا استمر النشاط.',
    now() - interval '6 hours'
  ),


  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-hunting-ksrr/800/600',
    'رُصد نشاط صيد جائر يستهدف حيوانات برية محمية داخل حدود محمية الملك سلمان بن عبدالعزيز الملكية.',
    'ILLEGAL_HUNTING', 'CRITICAL', 'OPEN', 29.0154, 40.0287,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"صيد جائر داخل محمية","risk_level":"CRITICAL","risk_score":92,"confidence":0.9,"description":"مركبة ومعدات صيد داخل حدود منطقة محمية لأنواع مهددة.","ai_explanation":"وجود معدات صيد داخل منطقة محظور فيها الصيد يشكل انتهاكاً مباشراً.","recommendation":"إبلاغ فوري لدوريات حماية المحمية والجهات الأمنية المختصة.","environmental_impact":"خطر مباشر على أنواع مهددة بالانقراض يجري حمايتها ضمن المحمية."}'::jsonb,
    0.9, 'إبلاغ فوري لدوريات حماية المحمية والجهات الأمنية المختصة.',
    'خطر مباشر على أنواع مهددة بالانقراض يجري حمايتها ضمن المحمية.',
    now() - interval '10 hours'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-hunting-hail/800/600',
    'آثار نشاط صيد غير مرخص قرب مناطق رعوية في حائل.',
    'ILLEGAL_HUNTING', 'HIGH', 'OPEN', 27.5219, 41.6907,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"صيد جائر","risk_level":"HIGH","risk_score":70,"confidence":0.83,"description":"آثار فخاخ ومعدات صيد يدوية في منطقة برية مفتوحة.","ai_explanation":"نوع المعدات وموقعها يتوافقان مع نشاط صيد غير مرخص.","recommendation":"تكثيف الدوريات الميدانية في المنطقة خلال الأيام القادمة.","environmental_impact":"تهديد للتوازن البيئي المحلي للحياة البرية في المنطقة."}'::jsonb,
    0.83, 'تكثيف الدوريات الميدانية في المنطقة خلال الأيام القادمة.',
    'تهديد للتوازن البيئي المحلي للحياة البرية في المنطقة.',
    now() - interval '2 days'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-hunting-tabuk/800/600',
    'بلاغ سابق عن نشاط صيد قرب تبوك تمت متابعته وإغلاقه دون رصد تكرار.',
    'ILLEGAL_HUNTING', 'MEDIUM', 'RESOLVED', 28.3998, 36.5715,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"نشاط صيد محدود","risk_level":"MEDIUM","risk_score":42,"confidence":0.78,"description":"مؤشرات محدودة على نشاط صيد غير مؤكد بالكامل.","ai_explanation":"الأدلة المرئية غير حاسمة لكنها تستدعي المتابعة الاحترازية.","recommendation":"إغلاق الملف بعد عدم رصد نشاط متكرر.","environmental_impact":"أثر غير مؤكد، تمت متابعته احترازياً."}'::jsonb,
    0.78, 'إغلاق الملف بعد عدم رصد نشاط متكرر.',
    'أثر غير مؤكد، تمت متابعته احترازياً.',
    now() - interval '18 days'
  ),

 
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-plant-qassim/800/600',
    'أعراض اصفرار وذبول على مجموعة نخيل في مزرعة قرب بريدة، يُحتمل أن تكون آفة نباتية منتشرة.',
    'PLANT_DISEASE', 'MEDIUM', 'OPEN', 26.3260, 43.9750,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"آفة نباتية محتملة","risk_level":"MEDIUM","risk_score":54,"confidence":0.81,"description":"اصفرار وذبول واضحين على سعف عدد من أشجار النخيل.","ai_explanation":"نمط الاصفرار المتماثل بين الأشجار المتجاورة يشير لانتشار آفة أو نقص تغذية حاد.","recommendation":"إرسال مختص زراعي لأخذ عينات وتحديد سبب الإصابة.","environmental_impact":"خطر انتشار الإصابة لبقية المزرعة إذا لم تُعالج مبكراً."}'::jsonb,
    0.81, 'إرسال مختص زراعي لأخذ عينات وتحديد سبب الإصابة.',
    'خطر انتشار الإصابة لبقية المزرعة إذا لم تُعالج مبكراً.',
    now() - interval '4 days'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-plant-medina/800/600',
    'ذبول ملحوظ في بستان نخيل قرب المدينة المنورة مع تساقط غير معتاد للسعف.',
    'PLANT_DISEASE', 'HIGH', 'OPEN', 24.5247, 39.5692,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"إصابة نباتية متقدمة","risk_level":"HIGH","risk_score":68,"confidence":0.86,"description":"ذبول متقدم وتساقط سعف على عدد كبير من أشجار النخيل.","ai_explanation":"شدة الأعراض وانتشارها بين عدة أشجار يوحيان بإصابة متقدمة تستدعي تدخلاً سريعاً.","recommendation":"تدخل زراعي عاجل لمنع انتشار الإصابة لبساتين مجاورة.","environmental_impact":"خطر مرتفع على إنتاجية بساتين النخيل في المنطقة."}'::jsonb,
    0.86, 'تدخل زراعي عاجل لمنع انتشار الإصابة لبساتين مجاورة.',
    'خطر مرتفع على إنتاجية بساتين النخيل في المنطقة.',
    now() - interval '1 day'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-plant-albaha/800/600',
    'إصابة نباتية محدودة على شجيرات جبلية بالباحة عولجت بعد الفحص الميداني.',
    'PLANT_DISEASE', 'LOW', 'RESOLVED', 20.0129, 41.4677,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"إصابة نباتية طفيفة","risk_level":"LOW","risk_score":28,"confidence":0.75,"description":"اصفرار طفيف ومحدود على عدد قليل من الشجيرات.","ai_explanation":"محدودية انتشار الأعراض تقلل من احتمال وجود آفة خطيرة.","recommendation":"مراقبة دورية دون تدخل عاجل.","environmental_impact":"أثر ضئيل ومحدود على الغطاء النباتي المحلي."}'::jsonb,
    0.75, 'مراقبة دورية دون تدخل عاجل.',
    'أثر ضئيل ومحدود على الغطاء النباتي المحلي.',
    now() - interval '9 days'
  ),

 
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-injured-najran/800/600',
    'حيوان بري مصاب رُصد بالقرب من طريق زراعي في نجران، يحتاج تدخلاً بيطرياً عاجلاً.',
    'INJURED_ANIMAL', 'HIGH', 'OPEN', 17.4917, 44.1322,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"حيوان مصاب","risk_level":"HIGH","risk_score":66,"confidence":0.87,"description":"حيوان بري يظهر عليه إصابة واضحة في أحد أطرافه.","ai_explanation":"وضعية الحيوان وصعوبة حركته تشيران لإصابة تستدعي تدخلاً سريعاً.","recommendation":"إرسال فريق بيطري ميداني في أقرب وقت ممكن.","environmental_impact":"خطر على سلامة الحيوان وقد يشير لخطر أوسع في محيطه (فخ أو حادث طريق)."}'::jsonb,
    0.87, 'إرسال فريق بيطري ميداني في أقرب وقت ممكن.',
    'خطر على سلامة الحيوان وقد يشير لخطر أوسع في محيطه.',
    now() - interval '7 hours'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-injured-hail/800/600',
    'رُصد حيوان بري يعرج بشكل واضح قرب منطقة رعوية في حائل.',
    'INJURED_ANIMAL', 'MEDIUM', 'OPEN', 27.5219, 41.6907,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"حيوان مصاب طفيف","risk_level":"MEDIUM","risk_score":47,"confidence":0.79,"description":"عرج ملحوظ في حركة الحيوان دون علامات إصابة حادة ظاهرة.","ai_explanation":"طبيعة الحركة تشير لإصابة طفيفة أو متوسطة.","recommendation":"متابعة عن بُعد وإرسال فريق إذا ساءت الحالة.","environmental_impact":"أثر محدود حالياً، يستدعي المتابعة."}'::jsonb,
    0.79, 'متابعة عن بُعد وإرسال فريق إذا ساءت الحالة.',
    'أثر محدود حالياً، يستدعي المتابعة.',
    now() - interval '3 days'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-injured-ksrr/800/600',
    'حيوان بري مصاب داخل محمية الملك سلمان تمت معالجته من قبل فريق بيطري متخصص.',
    'INJURED_ANIMAL', 'MEDIUM', 'RESOLVED', 29.0154, 40.0287,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"حيوان مصاب — تمت المعالجة","risk_level":"MEDIUM","risk_score":40,"confidence":0.84,"description":"إصابة متوسطة عولجت ميدانياً من قبل فريق بيطري متخصص.","ai_explanation":"تقرير الفريق الميداني يؤكد استقرار حالة الحيوان بعد العلاج.","recommendation":"إغلاق الملف مع متابعة روتينية.","environmental_impact":"لا يوجد أثر بيئي إضافي بعد المعالجة."}'::jsonb,
    0.84, 'إغلاق الملف مع متابعة روتينية.',
    'لا يوجد أثر بيئي إضافي بعد المعالجة.',
    now() - interval '13 days'
  ),

  
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-dead-tabuk/800/600',
    'عُثر على حيوان نافق قرب طريق سريع في تبوك، يستدعي ذلك فحصاً للتأكد من سبب النفوق.',
    'DEAD_ANIMAL', 'MEDIUM', 'OPEN', 28.3998, 36.5715,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"حيوان نافق","risk_level":"MEDIUM","risk_score":50,"confidence":0.82,"description":"حيوان نافق بالقرب من طريق سريع، على الأرجح نتيجة حادث طريق.","ai_explanation":"موقع الجثة وحالتها تتوافقان مع حادث دهس على الطريق.","recommendation":"إزالة الجثة والتخلص منها وفق الإجراءات الصحية المعتمدة.","environmental_impact":"خطر صحي محدود إذا تُركت لفترة طويلة قرب الطريق."}'::jsonb,
    0.82, 'إزالة الجثة والتخلص منها وفق الإجراءات الصحية المعتمدة.',
    'خطر صحي محدود إذا تُركت لفترة طويلة قرب الطريق.',
    now() - interval '2 days'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-dead-jazan/800/600',
    'عُثر على أسماك نافقة بأعداد غير معتادة على شاطئ جازان، ما قد يشير لتغير مفاجئ في جودة المياه.',
    'DEAD_ANIMAL', 'HIGH', 'OPEN', 16.8892, 42.5611,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"نفوق جماعي للأحياء المائية","risk_level":"HIGH","risk_score":72,"confidence":0.88,"description":"أعداد غير معتادة من الأسماك النافقة متجمعة على الشاطئ.","ai_explanation":"النفوق الجماعي غير المبرر يشير غالباً لتغير حاد في جودة المياه أو نقص الأكسجين.","recommendation":"أخذ عينات مياه فورية وإخطار الجهات البيئية المختصة.","environmental_impact":"مؤشر محتمل على تدهور بيئي أوسع في النظام البيئي الساحلي."}'::jsonb,
    0.88, 'أخذ عينات مياه فورية وإخطار الجهات البيئية المختصة.',
    'مؤشر محتمل على تدهور بيئي أوسع في النظام البيئي الساحلي.',
    now() - interval '16 hours'
  ),

  
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-other-mecca/800/600',
    'أضرار غير مصنّفة بوضوح في غطاء نباتي على أطراف مكة المكرمة، تستدعي تحققاً ميدانياً لتحديد السبب.',
    'OTHER', 'MEDIUM', 'OPEN', 21.3891, 39.8579,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"ضرر بيئي غير مصنّف","risk_level":"MEDIUM","risk_score":49,"confidence":0.72,"description":"أضرار ظاهرة على الغطاء النباتي دون سبب واضح من الصورة وحدها.","ai_explanation":"عدم وضوح السبب المباشر يستدعي تصنيفه ضمن الحالات العامة ريثما يتم التحقق الميداني.","recommendation":"إرسال فريق ميداني لتحديد سبب الضرر وتصنيفه بدقة.","environmental_impact":"أثر غير محدد حالياً، قيد التحقق."}'::jsonb,
    0.72, 'إرسال فريق ميداني لتحديد سبب الضرر وتصنيفه بدقة.',
    'أثر غير محدد حالياً، قيد التحقق.',
    now() - interval '5 days'
  ),
  (
    v_user_id, 'https://picsum.photos/seed/hima-demo-other-medina/800/600',
    'بلاغ عام سابق قرب المدينة المنورة عولج وأُغلق بعد تصنيفه كحالة غير حرجة.',
    'OTHER', 'LOW', 'RESOLVED', 24.5247, 39.5692,
    '{"result_category":"environmental_incident","is_recognizable":true,"is_environmental":true,"issue_type":"حالة عامة بسيطة","risk_level":"LOW","risk_score":22,"confidence":0.7,"description":"حالة بيئية طفيفة لا تستدعي إجراءً عاجلاً.","ai_explanation":"شدة الحالة منخفضة ولا تشير لخطر بيئي فعلي.","recommendation":"إغلاق الملف دون إجراء إضافي.","environmental_impact":"لا يوجد أثر بيئي يُذكر."}'::jsonb,
    0.7, 'إغلاق الملف دون إجراء إضافي.',
    'لا يوجد أثر بيئي يُذكر.',
    now() - interval '19 days'
  );

END $$;
