var userLanguage = navigator.language || navigator.userLanguage;
var detectedLanguage = userLanguage.substring(0, 2); // Получение двухбуквенного кода языка
if (detectedLanguage === "en") {
    window.location.href = "EN/Brightness.htm";
} else if (detectedLanguage === "ru") {
    window.location.href = "RU/Brightness.htm";
} else {
    // По умолчанию используйте английский или другой язык по вашему выбору
    window.location.href = "EN/Brightness.htm";
}
