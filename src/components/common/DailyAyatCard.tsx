import React from 'react';
import { getTodayAyat } from '../../data/dailyAyat';
import { BookOpen } from 'lucide-react';

export const DailyAyatCard: React.FC = () => {
  const ayat = getTodayAyat();

  return (
    <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-[#4A148C] via-[#311B92] to-[#1A237E] p-6 text-white shadow-xl border border-purple-800/40">
      <div className="absolute top-0 right-0 -mt-10 -mr-10 w-44 h-44 rounded-full bg-amber-400/10 blur-3xl pointer-events-none" />
      <div className="absolute bottom-0 left-0 -mb-10 -ml-10 w-44 h-44 rounded-full bg-purple-500/20 blur-3xl pointer-events-none" />

      <div className="relative z-10 flex flex-col items-center text-center">
        <div className="text-amber-300 font-arabic text-xl sm:text-2xl mb-2 font-medium tracking-wide">
          بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
        </div>
        <div className="font-arabic text-2xl sm:text-3xl font-bold text-amber-100 my-2 leading-relaxed max-w-2xl px-2">
          {ayat.arabic}
        </div>
        <div className="w-24 h-0.5 bg-gradient-to-r from-transparent via-amber-400/80 to-transparent my-3" />
        <div className="text-sm sm:text-base text-purple-100 italic max-w-xl leading-relaxed font-serif px-4">
          &ldquo;{ayat.english}&rdquo;
        </div>
        <div className="mt-3 flex items-center gap-1.5 text-xs text-amber-300 font-medium px-3 py-1 rounded-full bg-white/10 backdrop-blur-xs border border-amber-300/30">
          <BookOpen className="w-3.5 h-3.5" />
          <span>{ayat.reference}</span>
        </div>
      </div>
    </div>
  );
};
