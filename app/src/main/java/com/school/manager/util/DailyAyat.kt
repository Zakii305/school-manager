package com.school.manager.util

import java.util.Calendar

data class Ayat(
    val arabic: String,
    val english: String,
    val reference: String
)

object DailyAyat {

    private val VERSES = listOf(
        Ayat(
            "وَقُل رَّبِّ زِدْنِي عِلْمًا",
            "And say, \"My Lord, increase me in knowledge.\"",
            "Surah Taha 20:114"
        ),
        Ayat(
            "يَرْفَعِ اللَّهُ الَّذِينَ آمَنُوا مِنكُمْ وَالَّذِينَ أُوتُوا الْعِلْمَ دَرَجَاتٍ",
            "Allah will raise those who have believed among you and those who were given knowledge, by degrees.",
            "Surah Al-Mujadila 58:11"
        ),
        Ayat(
            "وَمَا تَوْفِيقِي إِلَّا بِاللَّهِ ۚ عَلَيْهِ تَوَكَّلْتُ وَإِلَيْهِ أُنِيبُ",
            "And my success is not but through Allah. Upon Him I have relied, and to Him I return.",
            "Surah Hud 11:88"
        ),
        Ayat(
            "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "Indeed, with hardship comes ease.",
            "Surah Ash-Sharh 94:6"
        ),
        Ayat(
            "وَاصْبِرْ وَمَا صَبْرُكَ إِلَّا بِاللَّهِ",
            "And be patient, and your patience is not but through Allah.",
            "Surah An-Nahl 16:127"
        ),
        Ayat(
            "رَبَّنَا آتِنَا مِن لَّدُنكَ رَحْمَةً وَهَيِّئْ لَنَا مِنْ أَمْرِنَا رَشَدًا",
            "Our Lord, grant us mercy and guidance from Yourself.",
            "Surah Al-Kahf 18:10"
        ),
        Ayat(
            "اقْرَأْ بِاسْمِ رَبِّكَ الَّذِي خَلَقَ",
            "Read in the name of your Lord who created.",
            "Surah Al-Alaq 96:1"
        ),
        Ayat(
            "وَتَعَاوَنُوا عَلَى الْبِرِّ وَالتَّقْوَىٰ",
            "And cooperate in righteousness and piety.",
            "Surah Al-Ma'idah 5:2"
        ),
        Ayat(
            "وَلَا تَقْفُ مَا لَيْسَ لَكَ بِهِ عِلْمٌ",
            "And do not pursue that of which you have no knowledge.",
            "Surah Al-Isra 17:36"
        ),
        Ayat(
            "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
            "So remember Me; I will remember you. And be grateful to Me and do not deny Me.",
            "Surah Al-Baqarah 2:152"
        ),
        Ayat(
            "وَاللَّهُ يُحِبُّ الصَّابِرِينَ",
            "And Allah loves the patient.",
            "Surah Ali 'Imran 3:146"
        ),
        Ayat(
            "إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
            "Indeed, Allah is with the patient.",
            "Surah Al-Baqarah 2:153"
        ),
        Ayat(
            "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
            "My Lord, expand for me my chest and ease for me my task.",
            "Surah Taha 20:25-26"
        ),
        Ayat(
            "وَاللَّهُ بِكُلِّ شَيْءٍ عَلِيمٌ",
            "And Allah is Knowing of all things.",
            "Surah Al-Baqarah 2:29"
        ),
        Ayat(
            "إِنَّ اللَّهَ لَا يُغَيِّرُ مَا بِقَوْمٍ حَتَّىٰ يُغَيِّرُوا مَا بِأَنفُسِهِمْ",
            "Indeed, Allah will not change the condition of a people until they change what is in themselves.",
            "Surah Ar-Ra'd 13:11"
        ),
        Ayat(
            "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ",
            "And seek help through patience and prayer.",
            "Surah Al-Baqarah 2:45"
        ),
        Ayat(
            "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
            "Allah does not burden a soul beyond that it can bear.",
            "Surah Al-Baqarah 2:286"
        ),
        Ayat(
            "وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا",
            "And whoever fears Allah - He will make for him a way out.",
            "Surah At-Talaq 65:2"
        ),
        Ayat(
            "إِنَّ الْعِلْمَ نُورٌ",
            "Indeed, knowledge is light.",
            "Hadith tradition (paraphrase)"
        ),
        Ayat(
            "وَقُلْ رَبِّ أَعُوذُ بِكَ مِنْ هَمَزَاتِ الشَّيَاطِينِ",
            "Say, \"My Lord, I seek refuge in You from the incitements of the devils.\"",
            "Surah Al-Mu'minun 23:97"
        )
    )

    fun today(): Ayat {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return VERSES[day % VERSES.size]
    }
}
