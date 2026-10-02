export interface Ayat {
  arabic: string;
  english: string;
  reference: string;
}

export const DAILY_VERSES: Ayat[] = [
  {
    arabic: "وَقُل رَّبِّ زِدْنِي عِلْمًا",
    english: "And say: \"My Lord, increase me in knowledge.\"",
    reference: "Surah Taha 20:114"
  },
  {
    arabic: "يَرْفَعِ اللَّهُ الَّذِينَ آمَنُوا مِنكُمْ وَالَّذِينَ أُوتُوا الْعِلْمَ دَرَجَاتٍ",
    english: "Allah will raise those who have believed among you and those who were given knowledge, by degrees.",
    reference: "Surah Al-Mujadila 58:11"
  },
  {
    arabic: "وَمَا تَوْفِيقِي إِلَّا بِاللَّهِ ۚ عَلَيْهِ تَوَكَّلْتُ وَإِلَيْهِ أُنِيبُ",
    english: "And my success is not but through Allah. Upon Him I have relied, and to Him I return.",
    reference: "Surah Hud 11:88"
  },
  {
    arabic: "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
    english: "Indeed, with hardship comes ease.",
    reference: "Surah Ash-Sharh 94:6"
  },
  {
    arabic: "اقْرَأْ بِاسْمِ رَبِّكَ الَّذِي خَلَقَ",
    english: "Read in the name of your Lord who created.",
    reference: "Surah Al-Alaq 96:1"
  },
  {
    arabic: "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
    english: "My Lord, expand for me my chest and ease for me my task.",
    reference: "Surah Taha 20:25-26"
  },
  {
    arabic: "إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
    english: "Indeed, Allah is with the patient.",
    reference: "Surah Al-Baqarah 2:153"
  }
];

export function getTodayAyat(): Ayat {
  const dayOfYear = Math.floor(
    (Date.now() - new Date(new Date().getFullYear(), 0, 0).getTime()) / 1000 / 60 / 60 / 24
  );
  return DAILY_VERSES[Math.abs(dayOfYear) % DAILY_VERSES.length];
}

export function getGreeting(): { greeting: string; emoji: string } {
  const hour = new Date().getHours();
  if (hour >= 5 && hour < 12) return { greeting: "Good morning", emoji: "☀️" };
  if (hour >= 12 && hour < 17) return { greeting: "Good afternoon", emoji: "🌤️" };
  if (hour >= 17 && hour < 21) return { greeting: "Good evening", emoji: "🌇" };
  return { greeting: "Good night", emoji: "🌙" };
}
