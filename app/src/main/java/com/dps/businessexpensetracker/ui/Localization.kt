package com.dps.businessexpensetracker.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(
    val tag: String,
    val nativeName: String,
    val englishName: String,
    val isRtl: Boolean = false
) {
    ENGLISH("en", "English", "English"),
    CHINESE("zh-CN", "简体中文", "Chinese (Simplified)"),
    SPANISH("es", "Español", "Spanish"),
    ARABIC("ar", "العربية", "Arabic", isRtl = true),
    HINDI("hi", "हिन्दी", "Hindi"),
    PORTUGUESE("pt-BR", "Português (Brasil)", "Portuguese (Brazil)"),
    FRENCH("fr", "Français", "French"),
    GERMAN("de", "Deutsch", "German"),
    JAPANESE("ja", "日本語", "Japanese"),
    TURKISH("tr", "Türkçe", "Turkish");

    companion object {
        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: ENGLISH
    }
}

object AppLanguagePrefs {
    private const val PREFS = "app_language_preferences"
    private const val LANGUAGE = "language_tag"
    private const val ONBOARDING_COMPLETE = "language_onboarding_complete"

    fun language(context: Context): AppLanguage = storedLanguageSettings(
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(LANGUAGE, null),
        onboardingComplete = false
    ).language

    fun saveLanguage(context: Context, language: AppLanguage) {
        val stored = storedLanguageSettings(language.tag, onboardingComplete = false)
            .withLanguage(language)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(LANGUAGE, stored.language.tag).apply()
    }

    fun isOnboardingComplete(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(ONBOARDING_COMPLETE, false)

    fun completeOnboarding(context: Context, language: AppLanguage) {
        val stored = storedLanguageSettings(language.tag, onboardingComplete = false)
            .completingOnboarding(language)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(LANGUAGE, stored.language.tag)
            .putBoolean(ONBOARDING_COMPLETE, stored.onboardingComplete)
            .apply()
    }
}

data class StoredLanguageSettings(
    val language: AppLanguage,
    val onboardingComplete: Boolean
)

fun storedLanguageSettings(tag: String?, onboardingComplete: Boolean): StoredLanguageSettings =
    StoredLanguageSettings(AppLanguage.fromTag(tag), onboardingComplete)

fun StoredLanguageSettings.withLanguage(language: AppLanguage): StoredLanguageSettings =
    copy(language = language)

fun StoredLanguageSettings.completingOnboarding(language: AppLanguage): StoredLanguageSettings =
    copy(language = language, onboardingComplete = true)

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

@Composable
fun tr(english: String): String = translate(LocalAppLanguage.current, english)

fun translate(language: AppLanguage, english: String): String =
    translations[language]?.get(english) ?: extendedTranslations[language]?.get(english) ?: english

private val translations: Map<AppLanguage, Map<String, String>> = mapOf(
    AppLanguage.CHINESE to mapOf(
        "Business Tracker" to "业务记账",
        "Expenses" to "支出", "Sales" to "销售", "Language" to "语言",
        "Choose your language" to "选择您的语言", "Continue" to "继续",
        "Welcome" to "欢迎", "Track business with confidence" to "安心管理您的业务",
        "Your data stays on this device" to "您的数据保留在此设备上",
        "No account, no ads and no automatic cloud upload." to "无需账户、无广告，也不会自动上传到云端。",
        "Ready to begin?" to "准备开始了吗？", "Start tracking" to "开始记账",
        "You can change language anytime from the menu." to "您可以随时从菜单更改语言。",
        "Export records" to "导出记录", "Export current view as CSV" to "将当前视图导出为 CSV",
        "Export current view as HTML" to "将当前视图导出为 HTML", "Choose backup file" to "选择备份文件",
        "Update backup now" to "立即更新备份", "Restore from backup" to "从备份恢复",
        "Turn off automatic backup" to "关闭自动备份", "Replay app tour" to "重播应用导览",
        "Search vendor, invoice, notes" to "搜索供应商、发票、备注", "Search customer, reference" to "搜索客户、参考号",
        "Add expense" to "添加支出", "Add sale" to "添加销售", "No expenses yet" to "暂无支出",
        "No sales yet" to "暂无销售", "Clear" to "清除", "All" to "全部", "Status" to "状态",
        "Category" to "类别", "Channel" to "渠道", "Payment" to "付款方式", "Date" to "日期",
        "Vendor" to "供应商", "Amount (INR)" to "金额 (INR)", "Submitted by" to "提交人",
        "Invoice number" to "发票编号", "Notes" to "备注", "Save" to "保存", "Cancel" to "取消",
        "Edit" to "编辑", "Duplicate" to "复制", "Delete" to "删除", "Back" to "返回",
        "Next" to "下一步", "Get started" to "开始使用", "Skip tour" to "跳过导览"
    ),
    AppLanguage.SPANISH to mapOf(
        "Business Tracker" to "Control empresarial", "Expenses" to "Gastos", "Sales" to "Ventas", "Language" to "Idioma",
        "Choose your language" to "Elige tu idioma", "Continue" to "Continuar", "Welcome" to "Bienvenido",
        "Track business with confidence" to "Controla tu negocio con confianza", "Your data stays on this device" to "Tus datos permanecen en este dispositivo",
        "No account, no ads and no automatic cloud upload." to "Sin cuenta, sin anuncios y sin carga automática a la nube.",
        "Ready to begin?" to "¿Listo para empezar?", "Start tracking" to "Empezar", "You can change language anytime from the menu." to "Puedes cambiar el idioma en cualquier momento desde el menú.",
        "Export records" to "Exportar registros", "Export current view as CSV" to "Exportar vista actual como CSV", "Export current view as HTML" to "Exportar vista actual como HTML",
        "Choose backup file" to "Elegir archivo de copia", "Update backup now" to "Actualizar copia ahora", "Restore from backup" to "Restaurar copia",
        "Turn off automatic backup" to "Desactivar copia automática", "Replay app tour" to "Repetir recorrido", "Search vendor, invoice, notes" to "Buscar proveedor, factura o notas",
        "Search customer, reference" to "Buscar cliente o referencia", "Add expense" to "Añadir gasto", "Add sale" to "Añadir venta", "No expenses yet" to "Aún no hay gastos",
        "No sales yet" to "Aún no hay ventas", "Clear" to "Limpiar", "All" to "Todos", "Status" to "Estado", "Category" to "Categoría", "Channel" to "Canal",
        "Payment" to "Pago", "Date" to "Fecha", "Vendor" to "Proveedor", "Amount (INR)" to "Importe (INR)", "Submitted by" to "Registrado por",
        "Invoice number" to "Número de factura", "Notes" to "Notas", "Save" to "Guardar", "Cancel" to "Cancelar", "Edit" to "Editar", "Duplicate" to "Duplicar",
        "Delete" to "Eliminar", "Back" to "Atrás", "Next" to "Siguiente", "Get started" to "Comenzar", "Skip tour" to "Omitir recorrido"
    ),
    AppLanguage.ARABIC to mapOf(
        "Business Tracker" to "متابعة الأعمال", "Expenses" to "المصروفات", "Sales" to "المبيعات", "Language" to "اللغة",
        "Choose your language" to "اختر لغتك", "Continue" to "متابعة", "Welcome" to "مرحباً", "Track business with confidence" to "أدر أعمالك بثقة",
        "Your data stays on this device" to "تبقى بياناتك على هذا الجهاز", "No account, no ads and no automatic cloud upload." to "لا حساب ولا إعلانات ولا رفع تلقائي إلى السحابة.",
        "Ready to begin?" to "هل أنت مستعد للبدء؟", "Start tracking" to "ابدأ المتابعة", "You can change language anytime from the menu." to "يمكنك تغيير اللغة في أي وقت من القائمة.",
        "Export records" to "تصدير السجلات", "Export current view as CSV" to "تصدير العرض الحالي بصيغة CSV", "Export current view as HTML" to "تصدير العرض الحالي بصيغة HTML",
        "Choose backup file" to "اختر ملف النسخة الاحتياطية", "Update backup now" to "حدّث النسخة الآن", "Restore from backup" to "استعادة من نسخة احتياطية",
        "Turn off automatic backup" to "إيقاف النسخ التلقائي", "Replay app tour" to "إعادة جولة التطبيق", "Search vendor, invoice, notes" to "ابحث عن المورد أو الفاتورة أو الملاحظات",
        "Search customer, reference" to "ابحث عن العميل أو المرجع", "Add expense" to "إضافة مصروف", "Add sale" to "إضافة عملية بيع", "No expenses yet" to "لا توجد مصروفات بعد",
        "No sales yet" to "لا توجد مبيعات بعد", "Clear" to "مسح", "All" to "الكل", "Status" to "الحالة", "Category" to "الفئة", "Channel" to "القناة",
        "Payment" to "الدفع", "Date" to "التاريخ", "Vendor" to "المورد", "Amount (INR)" to "المبلغ (INR)", "Submitted by" to "أُدخل بواسطة",
        "Invoice number" to "رقم الفاتورة", "Notes" to "ملاحظات", "Save" to "حفظ", "Cancel" to "إلغاء", "Edit" to "تعديل", "Duplicate" to "نسخ",
        "Delete" to "حذف", "Back" to "رجوع", "Next" to "التالي", "Get started" to "ابدأ", "Skip tour" to "تخطي الجولة"
    ),
    AppLanguage.HINDI to mapOf(
        "Business Tracker" to "व्यवसाय ट्रैकर", "Expenses" to "खर्च", "Sales" to "बिक्री", "Language" to "भाषा", "Choose your language" to "अपनी भाषा चुनें",
        "Continue" to "आगे बढ़ें", "Welcome" to "स्वागत है", "Track business with confidence" to "भरोसे के साथ व्यवसाय संभालें", "Your data stays on this device" to "आपका डेटा इसी डिवाइस पर रहता है",
        "No account, no ads and no automatic cloud upload." to "न खाता, न विज्ञापन और न अपने-आप क्लाउड अपलोड।", "Ready to begin?" to "शुरू करने के लिए तैयार?", "Start tracking" to "ट्रैक करना शुरू करें",
        "You can change language anytime from the menu." to "आप मेन्यू से कभी भी भाषा बदल सकते हैं।", "Export records" to "रिकॉर्ड निर्यात करें", "Choose backup file" to "बैकअप फ़ाइल चुनें",
        "Restore from backup" to "बैकअप से बहाल करें", "Replay app tour" to "ऐप टूर फिर चलाएँ", "Search vendor, invoice, notes" to "विक्रेता, चालान, नोट खोजें", "Add expense" to "खर्च जोड़ें",
        "Add sale" to "बिक्री जोड़ें", "No expenses yet" to "अभी कोई खर्च नहीं", "No sales yet" to "अभी कोई बिक्री नहीं", "Clear" to "साफ़ करें", "All" to "सभी", "Status" to "स्थिति",
        "Category" to "श्रेणी", "Channel" to "चैनल", "Payment" to "भुगतान", "Date" to "तारीख", "Vendor" to "विक्रेता", "Amount (INR)" to "राशि (INR)", "Submitted by" to "दर्ज करने वाला",
        "Invoice number" to "चालान संख्या", "Notes" to "नोट्स", "Save" to "सहेजें", "Cancel" to "रद्द करें", "Edit" to "संपादित करें", "Delete" to "हटाएँ", "Back" to "पीछे", "Next" to "अगला", "Get started" to "शुरू करें"
    ),
    AppLanguage.PORTUGUESE to mapOf(
        "Business Tracker" to "Controle empresarial", "Expenses" to "Despesas", "Sales" to "Vendas", "Language" to "Idioma", "Choose your language" to "Escolha seu idioma",
        "Continue" to "Continuar", "Welcome" to "Boas-vindas", "Track business with confidence" to "Gerencie seu negócio com confiança", "Your data stays on this device" to "Seus dados ficam neste dispositivo",
        "No account, no ads and no automatic cloud upload." to "Sem conta, sem anúncios e sem envio automático para a nuvem.", "Ready to begin?" to "Pronto para começar?", "Start tracking" to "Começar",
        "You can change language anytime from the menu." to "Você pode alterar o idioma a qualquer momento no menu.", "Export records" to "Exportar registros", "Choose backup file" to "Escolher arquivo de backup",
        "Restore from backup" to "Restaurar backup", "Replay app tour" to "Repetir tour do app", "Search vendor, invoice, notes" to "Buscar fornecedor, nota ou observações", "Add expense" to "Adicionar despesa",
        "Add sale" to "Adicionar venda", "No expenses yet" to "Ainda não há despesas", "No sales yet" to "Ainda não há vendas", "Clear" to "Limpar", "All" to "Todos", "Status" to "Status",
        "Category" to "Categoria", "Channel" to "Canal", "Payment" to "Pagamento", "Date" to "Data", "Vendor" to "Fornecedor", "Amount (INR)" to "Valor (INR)", "Submitted by" to "Registrado por",
        "Invoice number" to "Número da nota", "Notes" to "Observações", "Save" to "Salvar", "Cancel" to "Cancelar", "Edit" to "Editar", "Duplicate" to "Duplicar", "Delete" to "Excluir", "Back" to "Voltar", "Next" to "Próximo", "Get started" to "Começar"
    ),
    AppLanguage.FRENCH to mapOf(
        "Business Tracker" to "Suivi d’entreprise", "Expenses" to "Dépenses", "Sales" to "Ventes", "Language" to "Langue", "Choose your language" to "Choisissez votre langue",
        "Continue" to "Continuer", "Welcome" to "Bienvenue", "Track business with confidence" to "Gérez votre entreprise en toute confiance", "Your data stays on this device" to "Vos données restent sur cet appareil",
        "No account, no ads and no automatic cloud upload." to "Aucun compte, aucune publicité et aucun envoi automatique vers le cloud.", "Ready to begin?" to "Prêt à commencer ?", "Start tracking" to "Commencer",
        "You can change language anytime from the menu." to "Vous pouvez changer de langue à tout moment depuis le menu.", "Export records" to "Exporter les données", "Choose backup file" to "Choisir le fichier de sauvegarde",
        "Restore from backup" to "Restaurer une sauvegarde", "Replay app tour" to "Rejouer la visite", "Search vendor, invoice, notes" to "Rechercher fournisseur, facture, notes", "Add expense" to "Ajouter une dépense",
        "Add sale" to "Ajouter une vente", "No expenses yet" to "Aucune dépense", "No sales yet" to "Aucune vente", "Clear" to "Effacer", "All" to "Tous", "Status" to "Statut",
        "Category" to "Catégorie", "Channel" to "Canal", "Payment" to "Paiement", "Date" to "Date", "Vendor" to "Fournisseur", "Amount (INR)" to "Montant (INR)", "Submitted by" to "Saisi par",
        "Invoice number" to "Numéro de facture", "Notes" to "Notes", "Save" to "Enregistrer", "Cancel" to "Annuler", "Edit" to "Modifier", "Duplicate" to "Dupliquer", "Delete" to "Supprimer", "Back" to "Retour", "Next" to "Suivant", "Get started" to "Commencer"
    ),
    AppLanguage.GERMAN to mapOf(
        "Business Tracker" to "Geschäftsübersicht", "Expenses" to "Ausgaben", "Sales" to "Verkäufe", "Language" to "Sprache", "Choose your language" to "Sprache auswählen",
        "Continue" to "Weiter", "Welcome" to "Willkommen", "Track business with confidence" to "Geschäft sicher im Blick behalten", "Your data stays on this device" to "Ihre Daten bleiben auf diesem Gerät",
        "No account, no ads and no automatic cloud upload." to "Kein Konto, keine Werbung und kein automatischer Cloud-Upload.", "Ready to begin?" to "Bereit anzufangen?", "Start tracking" to "Jetzt starten",
        "You can change language anytime from the menu." to "Die Sprache kann jederzeit im Menü geändert werden.", "Export records" to "Datensätze exportieren", "Export current view as CSV" to "Aktuelle Ansicht als CSV exportieren",
        "Export current view as HTML" to "Aktuelle Ansicht als HTML exportieren", "Choose backup file" to "Sicherungsdatei auswählen", "Update backup now" to "Sicherung jetzt aktualisieren",
        "Restore from backup" to "Aus Sicherung wiederherstellen", "Turn off automatic backup" to "Automatische Sicherung ausschalten", "Replay app tour" to "App-Tour wiederholen",
        "Search vendor, invoice, notes" to "Lieferant, Rechnung oder Notizen suchen", "Search customer, reference" to "Kunde oder Referenz suchen", "Add expense" to "Ausgabe hinzufügen", "Add sale" to "Verkauf hinzufügen",
        "No expenses yet" to "Noch keine Ausgaben", "No sales yet" to "Noch keine Verkäufe", "Clear" to "Löschen", "All" to "Alle", "Status" to "Status", "Category" to "Kategorie", "Channel" to "Kanal",
        "Payment" to "Zahlung", "Date" to "Datum", "Vendor" to "Lieferant", "Amount (INR)" to "Betrag (INR)", "Submitted by" to "Erfasst von", "Invoice number" to "Rechnungsnummer",
        "Notes" to "Notizen", "Save" to "Speichern", "Cancel" to "Abbrechen", "Edit" to "Bearbeiten", "Duplicate" to "Duplizieren", "Delete" to "Löschen", "Back" to "Zurück", "Next" to "Weiter",
        "Get started" to "Loslegen", "Skip tour" to "Tour überspringen"
    ),
    AppLanguage.JAPANESE to mapOf(
        "Business Tracker" to "ビジネス管理", "Expenses" to "経費", "Sales" to "売上", "Language" to "言語", "Choose your language" to "言語を選択",
        "Continue" to "続ける", "Welcome" to "ようこそ", "Track business with confidence" to "安心して事業を管理", "Your data stays on this device" to "データはこの端末内に保存されます",
        "No account, no ads and no automatic cloud upload." to "アカウント不要、広告なし、自動クラウド送信なし。", "Ready to begin?" to "始める準備はできましたか？", "Start tracking" to "記録を始める",
        "You can change language anytime from the menu." to "言語はメニューからいつでも変更できます。", "Export records" to "記録を出力", "Choose backup file" to "バックアップファイルを選択",
        "Restore from backup" to "バックアップから復元", "Replay app tour" to "アプリツアーを再表示", "Search vendor, invoice, notes" to "仕入先、請求書、メモを検索", "Add expense" to "経費を追加",
        "Add sale" to "売上を追加", "No expenses yet" to "経費はまだありません", "No sales yet" to "売上はまだありません", "Clear" to "クリア", "All" to "すべて", "Status" to "状態",
        "Category" to "カテゴリ", "Channel" to "販売経路", "Payment" to "支払方法", "Date" to "日付", "Vendor" to "仕入先", "Amount (INR)" to "金額 (INR)", "Submitted by" to "入力者",
        "Invoice number" to "請求書番号", "Notes" to "メモ", "Save" to "保存", "Cancel" to "キャンセル", "Edit" to "編集", "Duplicate" to "複製", "Delete" to "削除", "Back" to "戻る", "Next" to "次へ", "Get started" to "開始"
    ),
    AppLanguage.TURKISH to mapOf(
        "Business Tracker" to "İşletme Takibi", "Expenses" to "Giderler", "Sales" to "Satışlar", "Language" to "Dil", "Choose your language" to "Dilinizi seçin",
        "Continue" to "Devam", "Welcome" to "Hoş geldiniz", "Track business with confidence" to "İşletmenizi güvenle takip edin", "Your data stays on this device" to "Verileriniz bu cihazda kalır",
        "No account, no ads and no automatic cloud upload." to "Hesap yok, reklam yok ve otomatik bulut yüklemesi yok.", "Ready to begin?" to "Başlamaya hazır mısınız?", "Start tracking" to "Takibe başla",
        "You can change language anytime from the menu." to "Dili menüden istediğiniz zaman değiştirebilirsiniz.", "Export records" to "Kayıtları dışa aktar", "Export current view as CSV" to "Geçerli görünümü CSV olarak aktar",
        "Export current view as HTML" to "Geçerli görünümü HTML olarak aktar", "Choose backup file" to "Yedek dosyası seç", "Update backup now" to "Yedeği şimdi güncelle",
        "Restore from backup" to "Yedekten geri yükle", "Turn off automatic backup" to "Otomatik yedeklemeyi kapat", "Replay app tour" to "Uygulama turunu tekrarla",
        "Search vendor, invoice, notes" to "Satıcı, fatura veya not ara", "Search customer, reference" to "Müşteri veya referans ara", "Add expense" to "Gider ekle", "Add sale" to "Satış ekle",
        "No expenses yet" to "Henüz gider yok", "No sales yet" to "Henüz satış yok", "Clear" to "Temizle", "All" to "Tümü", "Status" to "Durum", "Category" to "Kategori", "Channel" to "Kanal",
        "Payment" to "Ödeme", "Date" to "Tarih", "Vendor" to "Satıcı", "Amount (INR)" to "Tutar (INR)", "Submitted by" to "Kaydeden", "Invoice number" to "Fatura numarası",
        "Notes" to "Notlar", "Save" to "Kaydet", "Cancel" to "İptal", "Edit" to "Düzenle", "Duplicate" to "Çoğalt", "Delete" to "Sil", "Back" to "Geri", "Next" to "İleri",
        "Get started" to "Başla", "Skip tour" to "Turu atla"
    )
)

private val extendedTranslations: Map<AppLanguage, Map<String, String>> = mapOf(
    AppLanguage.GERMAN to mapOf(
        "Settings" to "Einstellungen", "Business profile" to "Unternehmensprofil",
        "Business name (optional)" to "Unternehmensname (optional)", "Home currency" to "Hauptwährung",
        "App preferences" to "App-Einstellungen", "Data and privacy" to "Daten und Datenschutz",
        "Review the dashboard and key actions" to "Dashboard und wichtige Aktionen ansehen",
        "Local-first storage" to "Lokale Datenspeicherung", "Expenses, sales and scans stay on this device." to "Ausgaben, Verkäufe und Scans bleiben auf diesem Gerät.",
        "Automatic backup" to "Automatische Sicherung", "On" to "Ein", "Off — choose a backup file from the dashboard menu" to "Aus — Sicherungsdatei im Dashboard-Menü auswählen",
        "Currency changes how amounts are displayed; it does not convert saved values." to "Die Währung ändert nur die Anzeige; gespeicherte Werte werden nicht umgerechnet.",
        "Operating cashflow" to "Operativer Cashflow", "This month cashflow" to "Cashflow diesen Monat",
        "Today sales" to "Heutige Verkäufe", "Received" to "Eingegangen", "Paid expenses" to "Bezahlte Ausgaben", "Pending sales" to "Offene Verkäufe",
        "Daily sales" to "Tagesverkäufe", "No matching expenses" to "Keine passenden Ausgaben", "No matching sales" to "Keine passenden Verkäufe",
        "Clear filters" to "Filter löschen", "Sort" to "Sortierung", "Sale status" to "Verkaufsstatus", "Sale sort" to "Verkaufssortierung",
        "Draft" to "Entwurf", "For review" to "Zur Prüfung", "Approved" to "Genehmigt", "Paid" to "Bezahlt", "Rejected" to "Abgelehnt",
        "Pending" to "Ausstehend", "Refunded" to "Erstattet", "Office" to "Büro", "Travel" to "Reisen", "Meals" to "Verpflegung",
        "Utilities" to "Nebenkosten", "Software" to "Software", "Inventory" to "Warenbestand", "Marketing" to "Marketing", "Professional fees" to "Honorare", "Taxes" to "Steuern", "Other" to "Sonstiges",
        "Cash" to "Bar", "Card" to "Karte", "Bank transfer" to "Überweisung", "Cheque" to "Scheck", "Store / counter" to "Laden / Kasse", "Online" to "Online", "Wholesale" to "Großhandel", "Service" to "Dienstleistung",
        "Add an expense" to "Ausgabe hinzufügen", "Scan invoice" to "Rechnung scannen", "Import invoice image" to "Rechnungsbild importieren", "Enter manually" to "Manuell eingeben",
        "Amount" to "Betrag", "Total sale" to "Gesamtverkauf", "Tax" to "Steuer", "Tax amount" to "Steuerbetrag", "Discount" to "Rabatt",
        "Your business" to "Ihr Unternehmen", "Confirm your home currency" to "Hauptwährung bestätigen",
        "Business name is optional. You can change it later in Settings." to "Der Unternehmensname ist optional. Sie können ihn später in den Einstellungen ändern.",
        "Confirm and start" to "Bestätigen und starten",
        "Received sales minus paid expenses. Not accounting profit." to "Eingegangene Verkäufe minus bezahlte Ausgaben. Das ist kein buchhalterischer Gewinn.",
        "Quick start" to "Schnellstart", "Set business profile" to "Unternehmensprofil festlegen",
        "Add your first sale" to "Ersten Verkauf hinzufügen", "Add your first expense" to "Erste Ausgabe hinzufügen",
        "Choose a backup file" to "Sicherungsdatei auswählen", "Required" to "Erforderlich", "Optional" to "Optional",
        "Required fields must be filled in. Optional fields can be left blank." to "Pflichtfelder müssen ausgefüllt werden. Optionale Felder können leer bleiben.",
        "Customer or sale label" to "Kunde oder Verkaufsbezeichnung", "Sold by" to "Verkauft von", "Quantity" to "Menge",
        "Invoice / order reference" to "Rechnungs- oder Auftragsreferenz", "Supplier GSTIN" to "Lieferanten-GSTIN",
        "Attachment" to "Anhang", "Done" to "Erledigt", "Not done" to "Offen", "15 characters" to "15 Zeichen"
    ),
    AppLanguage.TURKISH to mapOf(
        "Settings" to "Ayarlar", "Business profile" to "İşletme profili", "Business name (optional)" to "İşletme adı (isteğe bağlı)", "Home currency" to "Ana para birimi",
        "App preferences" to "Uygulama tercihleri", "Data and privacy" to "Veri ve gizlilik", "Review the dashboard and key actions" to "Kontrol panelini ve temel işlemleri gözden geçirin",
        "Local-first storage" to "Cihazda yerel saklama", "Expenses, sales and scans stay on this device." to "Giderler, satışlar ve taramalar bu cihazda kalır.",
        "Automatic backup" to "Otomatik yedekleme", "On" to "Açık", "Off — choose a backup file from the dashboard menu" to "Kapalı — kontrol paneli menüsünden yedek dosyası seçin",
        "Currency changes how amounts are displayed; it does not convert saved values." to "Para birimi yalnızca gösterimi değiştirir; kayıtlı değerleri dönüştürmez.",
        "Operating cashflow" to "Faaliyet nakit akışı", "This month cashflow" to "Bu ay nakit akışı", "Today sales" to "Bugünkü satışlar", "Received" to "Tahsil edildi",
        "Paid expenses" to "Ödenen giderler", "Pending sales" to "Bekleyen satışlar", "Daily sales" to "Günlük satışlar", "No matching expenses" to "Eşleşen gider yok", "No matching sales" to "Eşleşen satış yok",
        "Clear filters" to "Filtreleri temizle", "Sort" to "Sırala", "Sale status" to "Satış durumu", "Sale sort" to "Satış sıralaması",
        "Draft" to "Taslak", "For review" to "İncelemede", "Approved" to "Onaylandı", "Paid" to "Ödendi", "Rejected" to "Reddedildi", "Pending" to "Bekliyor", "Refunded" to "İade edildi",
        "Office" to "Ofis", "Travel" to "Seyahat", "Meals" to "Yemek", "Utilities" to "Faturalar", "Software" to "Yazılım", "Inventory" to "Stok", "Marketing" to "Pazarlama", "Professional fees" to "Profesyonel hizmetler", "Taxes" to "Vergiler", "Other" to "Diğer",
        "Cash" to "Nakit", "Card" to "Kart", "Bank transfer" to "Banka havalesi", "Cheque" to "Çek", "Store / counter" to "Mağaza / kasa", "Online" to "Çevrimiçi", "Wholesale" to "Toptan", "Service" to "Hizmet",
        "Add an expense" to "Gider ekle", "Scan invoice" to "Fatura tara", "Import invoice image" to "Fatura görseli içe aktar", "Enter manually" to "Elle gir",
        "Amount" to "Tutar", "Total sale" to "Toplam satış", "Tax" to "Vergi", "Tax amount" to "Vergi tutarı", "Discount" to "İndirim",
        "Your business" to "İşletmeniz", "Confirm your home currency" to "Ana para birimini onaylayın",
        "Business name is optional. You can change it later in Settings." to "İşletme adı isteğe bağlıdır. Daha sonra Ayarlar'dan değiştirebilirsiniz.",
        "Confirm and start" to "Onayla ve başla",
        "Received sales minus paid expenses. Not accounting profit." to "Tahsil edilen satışlar eksi ödenen giderler. Bu, muhasebe kârı değildir.",
        "Quick start" to "Hızlı başlangıç", "Set business profile" to "İşletme profilini ayarla",
        "Add your first sale" to "İlk satışınızı ekleyin", "Add your first expense" to "İlk giderinizi ekleyin",
        "Choose a backup file" to "Bir yedek dosyası seçin", "Required" to "Zorunlu", "Optional" to "İsteğe bağlı",
        "Required fields must be filled in. Optional fields can be left blank." to "Zorunlu alanlar doldurulmalıdır. İsteğe bağlı alanlar boş bırakılabilir.",
        "Customer or sale label" to "Müşteri veya satış etiketi", "Sold by" to "Satan kişi", "Quantity" to "Miktar",
        "Invoice / order reference" to "Fatura veya sipariş referansı", "Supplier GSTIN" to "Tedarikçi GSTIN",
        "Attachment" to "Ek", "Done" to "Tamamlandı", "Not done" to "Tamamlanmadı", "15 characters" to "15 karakter"
    )
)
