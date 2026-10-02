import React from 'react';

interface SchoolLogoProps {
  className?: string;
  size?: number;
}

export const SchoolLogo: React.FC<SchoolLogoProps> = ({ className = "w-8 h-8", size }) => {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 128 128"
      width={size}
      height={size}
      fill="none"
      className={`shrink-0 ${className}`}
    >
      <defs>
        {/* Deep Royal Indigo Shield Gradient */}
        <linearGradient id="crestGradBg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#3730A3" />
          <stop offset="45%" stopColor="#1E1B4B" />
          <stop offset="100%" stopColor="#0B132B" />
        </linearGradient>

        {/* Radiant Gold Metal Gradient */}
        <linearGradient id="goldGradSheen" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#FEF08A" />
          <stop offset="35%" stopColor="#F59E0B" />
          <stop offset="85%" stopColor="#D97706" />
          <stop offset="100%" stopColor="#92400E" />
        </linearGradient>

        {/* Book Page Gradient */}
        <linearGradient id="pageWhiteGrad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stopColor="#FFFFFF" />
          <stop offset="100%" stopColor="#E2E8F0" />
        </linearGradient>

        {/* Drop Shadow Filter */}
        <filter id="softCrestShadow" x="-20%" y="-20%" width="140%" height="140%">
          <feDropShadow dx="0" dy="5" stdDeviation="4" floodColor="#000000" floodOpacity="0.4" />
        </filter>
      </defs>

      {/* Golden Laurel Wreath (Left Side) */}
      <g stroke="url(#goldGradSheen)" strokeWidth="1.8" strokeLinecap="round" fill="none">
        <path d="M22 64 C20 78 28 92 42 102" />
        <path d="M22 60 C18 56 16 50 18 46 C22 47 25 52 24 58" fill="url(#goldGradSheen)" />
        <path d="M19 72 C15 70 13 64 16 61 C19 63 22 67 20 71" fill="url(#goldGradSheen)" />
        <path d="M22 84 C18 83 17 77 21 74 C24 76 26 80 23 83" fill="url(#goldGradSheen)" />
        <path d="M28 95 C25 96 23 90 28 87 C31 89 32 94 29 95" fill="url(#goldGradSheen)" />
      </g>

      {/* Golden Laurel Wreath (Right Side) */}
      <g stroke="url(#goldGradSheen)" strokeWidth="1.8" strokeLinecap="round" fill="none">
        <path d="M106 64 C108 78 100 92 86 102" />
        <path d="M106 60 C110 56 112 50 110 46 C106 47 103 52 104 58" fill="url(#goldGradSheen)" />
        <path d="M109 72 C113 70 115 64 112 61 C109 63 106 67 108 71" fill="url(#goldGradSheen)" />
        <path d="M106 84 C110 83 111 77 107 74 C104 76 102 80 105 83" fill="url(#goldGradSheen)" />
        <path d="M100 95 C103 96 105 90 100 87 C97 89 96 94 99 95" fill="url(#goldGradSheen)" />
      </g>

      {/* Main Academic Shield Container */}
      <path
        d="M64 10 L102 26 C102 68 84 102 64 118 C44 102 26 68 26 26 L64 10 Z"
        fill="url(#crestGradBg)"
        stroke="url(#goldGradSheen)"
        strokeWidth="3.6"
        strokeLinejoin="round"
        filter="url(#softCrestShadow)"
      />

      {/* Inner Elegant Border */}
      <path
        d="M64 17 L95 29 C95 65 79 96 64 110 C49 96 33 65 33 29 L64 17 Z"
        fill="none"
        stroke="rgba(245, 158, 11, 0.45)"
        strokeWidth="1.6"
        strokeLinejoin="round"
      />

      {/* Academic Graduation Cap (Mortarboard) */}
      <g>
        {/* Cap Diamond Top */}
        <polygon
          points="64,26 90,37 64,48 38,37"
          fill="url(#goldGradSheen)"
          stroke="#78350F"
          strokeWidth="1"
        />
        {/* Cap Skull Base */}
        <path
          d="M48 43 C48 51 80 51 80 43 L80 47 C80 55 48 55 48 47 Z"
          fill="#1E1B4B"
          stroke="url(#goldGradSheen)"
          strokeWidth="1"
        />
        {/* Cap Tassel Button & Cord */}
        <circle cx="64" cy="37" r="2.2" fill="#FFFFFF" />
        <path
          d="M64 37 Q76 40 80 50"
          fill="none"
          stroke="#FEF08A"
          strokeWidth="1.6"
          strokeLinecap="round"
        />
        <circle cx="80" cy="51" r="2" fill="#F59E0B" />
      </g>

      {/* Central Star of Academic Excellence */}
      <polygon
        points="64,54 66.2,60.5 73,60.5 67.5,64.5 69.6,71 64,67 58.4,71 60.5,64.5 55,60.5 61.8,60.5"
        fill="url(#goldGradSheen)"
        stroke="#FFFFFF"
        strokeWidth="0.8"
      />

      {/* Open Book of Knowledge */}
      <g>
        {/* Left Page */}
        <path
          d="M40 73 C49 70 58 73 63 76 L63 94 C58 91 49 88 40 91 Z"
          fill="url(#pageWhiteGrad)"
          stroke="#CBD5E1"
          strokeWidth="1.2"
        />
        {/* Right Page */}
        <path
          d="M88 73 C79 70 70 73 65 76 L65 94 C70 91 79 88 88 91 Z"
          fill="url(#pageWhiteGrad)"
          stroke="#CBD5E1"
          strokeWidth="1.2"
        />
        {/* Book Spine */}
        <path
          d="M64 76 L64 95"
          stroke="#4F46E5"
          strokeWidth="2.2"
          strokeLinecap="round"
        />

        {/* Text lines on pages */}
        <line x1="45" y1="78" x2="57" y2="80" stroke="#94A3B8" strokeWidth="1" strokeLinecap="round" />
        <line x1="45" y1="82" x2="57" y2="84" stroke="#94A3B8" strokeWidth="1" strokeLinecap="round" />
        <line x1="45" y1="86" x2="54" y2="88" stroke="#94A3B8" strokeWidth="1" strokeLinecap="round" />

        <line x1="71" y1="80" x2="83" y2="78" stroke="#94A3B8" strokeWidth="1" strokeLinecap="round" />
        <line x1="71" y1="84" x2="83" y2="82" stroke="#94A3B8" strokeWidth="1" strokeLinecap="round" />
        <line x1="74" y1="88" x2="83" y2="86" stroke="#94A3B8" strokeWidth="1" strokeLinecap="round" />
      </g>

      {/* Golden Foundation Banner Ribbon at Bottom of Shield */}
      <g>
        <path
          d="M44 100 L64 96 L84 100 L80 105 L64 101 L48 105 Z"
          fill="url(#goldGradSheen)"
          stroke="#78350F"
          strokeWidth="0.8"
        />
      </g>
    </svg>
  );
};
